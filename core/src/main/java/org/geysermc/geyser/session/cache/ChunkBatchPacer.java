/*
 * Copyright (c) 2026 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Geyser
 */

package org.geysermc.geyser.session.cache;

import io.netty.channel.Channel;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.level.ServerboundChunkBatchReceivedPacket;

import java.util.concurrent.TimeUnit;

/**
 * Paces how fast the Java server sends chunks, like the vanilla client's {@code ChunkBatchSizeCalculator} does.
 * <p>
 * The Java server sends chunks in batches and waits for the client to acknowledge them, together with how many
 * chunks per tick it wants. Geyser used to acknowledge every batch right away with 20 chunks per tick, regardless of
 * whether the Bedrock connection could carry that. On slower connections (mobile, NetherNet relays) the chunks then
 * pile up in the send queue, everything behind them is delayed, and by the time chunks arrive the player may have
 * moved on, so the client drops them as being outside of the chunk publisher area.
 * <p>
 * To measure how long the Bedrock client takes to receive a batch, a network stack latency packet is sent when the
 * batch starts and another one when it finishes. The client answers both in order, after processing everything
 * sent before them, so the time between the two answers is how long the batch took to arrive (the round trip time
 * cancels out). The batch is acknowledged once the second answer arrives, and the Bedrock channel is writable.
 */
public final class ChunkBatchPacer {
    private static final boolean ENABLED = Boolean.parseBoolean(System.getProperty("Geyser.ChunkBatchPacing", "true"));
    /**
     * What Geyser always answered before; still used when pacing is disabled or the player isn't spawned yet.
     */
    private static final float UNPACED_CHUNKS_PER_TICK = 20.0F;
    /**
     * How much of each 50ms tick may be spent receiving chunks, leaving room for everything else.
     */
    private static final double TARGET_NANOS_PER_TICK = TimeUnit.MILLISECONDS.toNanos(25);
    /**
     * Starts out at about 9 chunks per tick, which is what the vanilla server starts with.
     */
    private static final double INITIAL_NANOS_PER_CHUNK = TARGET_NANOS_PER_TICK / 9;
    private static final int MAX_SAMPLES = 49;
    private static final float MIN_CHUNKS_PER_TICK = 0.5F;
    private static final float MAX_CHUNKS_PER_TICK = 64.0F;
    /**
     * Acknowledge anyway if the client takes this long to answer, so chunk loading never stalls completely.
     */
    private static final long ACK_TIMEOUT_MILLIS = 2000;
    /**
     * How often to check again whether the Bedrock channel became writable, and for how long at most.
     */
    private static final long WRITABILITY_RECHECK_MILLIS = 50;
    private static final long MAX_WRITABILITY_WAIT_MILLIS = 10_000;

    private final GeyserSession session;

    private double aggregatedNanosPerChunk = INITIAL_NANOS_PER_CHUNK;
    private int samples;
    private Batch currentBatch;

    public ChunkBatchPacer(GeyserSession session) {
        this.session = session;
    }

    /**
     * Called when the Java server starts sending a batch of chunks.
     */
    public void onBatchStart() {
        if (!ENABLED || !session.isSpawned()) {
            // While loading in, the client may not answer quickly; don't hold up the first chunks
            currentBatch = null;
            return;
        }
        Batch batch = new Batch();
        currentBatch = batch;
        session.sendNetworkLatencyStackPacket(0, true, () -> batch.startReceivedNanos = System.nanoTime());
    }

    /**
     * Called when the Java server finished sending a batch of chunks, after all chunks of it have been translated.
     */
    public void onBatchFinished(int batchSize) {
        Batch batch = currentBatch;
        currentBatch = null;
        if (batch == null) {
            session.sendDownstreamGamePacket(new ServerboundChunkBatchReceivedPacket(UNPACED_CHUNKS_PER_TICK));
            return;
        }

        session.sendNetworkLatencyStackPacket(0, true, () -> {
            if (batch.startReceivedNanos != 0 && batchSize > 0) {
                addSample((double) (System.nanoTime() - batch.startReceivedNanos) / batchSize);
            }
            acknowledge(batch);
        });
        session.scheduleInEventLoop(() -> {
            if (!batch.acknowledged) {
                session.getGeyser().getLogger().debug("Bedrock client took over " + ACK_TIMEOUT_MILLIS
                    + "ms to receive a chunk batch; acknowledging it anyway");
                acknowledge(batch);
            }
        }, ACK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
    }

    private void acknowledge(Batch batch) {
        if (batch.acknowledged || session.isClosed()) {
            return;
        }
        // Transports report a send backlog by turning unwritable (NetherNet does so based on its buffered amount);
        // wait for that to clear, so we don't ask for more chunks while the previous ones are still queued
        if (!isUpstreamWritable() && batch.writabilityWaitMillis < MAX_WRITABILITY_WAIT_MILLIS) {
            if (!batch.waitingForWritability) {
                batch.waitingForWritability = true;
                session.scheduleInEventLoop(() -> {
                    batch.waitingForWritability = false;
                    batch.writabilityWaitMillis += WRITABILITY_RECHECK_MILLIS;
                    acknowledge(batch);
                }, WRITABILITY_RECHECK_MILLIS, TimeUnit.MILLISECONDS);
            }
            return;
        }
        batch.acknowledged = true;
        session.sendDownstreamGamePacket(new ServerboundChunkBatchReceivedPacket(desiredChunksPerTick()));
    }

    private boolean isUpstreamWritable() {
        try {
            Channel channel = session.getUpstream().getSession().getPeer().getChannel();
            return channel == null || channel.isWritable();
        } catch (RuntimeException e) {
            // Not connected (anymore)
            return true;
        }
    }

    private void addSample(double nanosPerChunk) {
        // Same smoothing as the vanilla client: ignore outliers beyond 3x, then a running average over up to 50 samples
        double clamped = Math.max(aggregatedNanosPerChunk / 3, Math.min(nanosPerChunk, aggregatedNanosPerChunk * 3));
        aggregatedNanosPerChunk = (aggregatedNanosPerChunk * samples + clamped) / (samples + 1);
        samples = Math.min(MAX_SAMPLES, samples + 1);
    }

    public float desiredChunksPerTick() {
        float desired = (float) (TARGET_NANOS_PER_TICK / aggregatedNanosPerChunk);
        return Math.max(MIN_CHUNKS_PER_TICK, Math.min(desired, MAX_CHUNKS_PER_TICK));
    }

    private static final class Batch {
        private long startReceivedNanos;
        private boolean acknowledged;
        private boolean waitingForWritability;
        private long writabilityWaitMillis;
    }
}
