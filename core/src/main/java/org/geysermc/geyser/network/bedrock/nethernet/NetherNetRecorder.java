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

package org.geysermc.geyser.network.bedrock.nethernet;

import io.netty.util.concurrent.ScheduledFuture;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.netty.channel.nethernet.NetherNetChannel;
import tel.schich.libdatachannel.SctpStats;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Samples one connection's SCTP state every 20 ms into a CSV file, for the nethernet record command.
 * <p>
 * Made to tell spurious retransmission timeouts from real ones: usrsctp only restarts its retransmission timer when a
 * SACK arrives with a gap in it, so while data stays in flight a timer runs out every one or two RTOs however promptly
 * the client acknowledges. In a recording that shows as timeouts spaced 400-800 ms apart with rtt unchanged and
 * unacked never reaching 0, and cwnd dropping to one MTU after some of them.
 */
public final class NetherNetRecorder {
    public static final long INTERVAL_MILLIS = 20;
    static final String HEADER = "t_ms,rtt_ms,rto_ms,cwnd,peer_rwnd,unacked_chunks,timeouts,queued_bytes,"
        + "bytes_out,messages_out,packets_out,bytes_in,game_rtt_ms,writable";

    private final NetherNetChannel channel;
    private final NetherNetStats stats;
    private final BufferedWriter writer;
    private final Path file;
    private final long startNanos = System.nanoTime();
    private final long endNanos;
    private final Consumer<String> onDone;
    private final Summary summary = new Summary();
    private @Nullable ScheduledFuture<?> task;
    private boolean closed;

    private NetherNetRecorder(NetherNetChannel channel, NetherNetStats stats, Path file, BufferedWriter writer,
                              int seconds, Consumer<String> onDone) {
        this.channel = channel;
        this.stats = stats;
        this.file = file;
        this.writer = writer;
        this.endNanos = startNanos + TimeUnit.SECONDS.toNanos(seconds);
        this.onDone = onDone;
    }

    /**
     * @param header comment lines written ahead of the CSV header, without the leading #
     * @param onDone receives a one-line summary once the recording ends
     * @return the file being written
     */
    public static Path start(NetherNetChannel channel, NetherNetStats stats, Path file, List<String> header,
                             int seconds, Consumer<String> onDone) throws IOException {
        Files.createDirectories(file.getParent());
        BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8);
        for (String line : header) {
            writer.write("# " + line);
            writer.newLine();
        }
        writer.write(HEADER);
        writer.newLine();
        NetherNetRecorder recorder = new NetherNetRecorder(channel, stats, file, writer, seconds, onDone);
        recorder.task = channel.eventLoop().scheduleAtFixedRate(recorder::sample, 0, INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
        return file;
    }

    private void sample() {
        if (closed) {
            return;
        }
        long now = System.nanoTime();
        @Nullable SctpStats sctp = channel.isActive() ? channel.sctpStats() : null;
        if (sctp == null || now >= endNanos) {
            finish(sctp == null ? "the connection closed" : null);
            return;
        }
        long t = TimeUnit.NANOSECONDS.toMillis(now - startNanos);
        try {
            writer.write(t + "," + sctp.rtt().toMillis() + "," + sctp.rto().toMillis() + "," + sctp.congestionWindow()
                + "," + sctp.peerReceiveWindow() + "," + sctp.unackedChunks() + "," + sctp.dataTimeouts()
                + "," + NetherNetQueue.queuedBytes(channel) + "," + stats.bytesOutTotal() + "," + stats.messagesOutTotal()
                + "," + stats.packetsOutTotal() + "," + stats.bytesInTotal() + "," + stats.lastGameRoundTripMillis()
                + "," + (channel.isWritable() ? 1 : 0));
            writer.newLine();
        } catch (IOException e) {
            finish("writing failed: " + e.getMessage());
            return;
        }
        summary.add(t, sctp.unackedChunks(), sctp.dataTimeouts(), sctp.congestionWindow());
    }

    private void finish(@Nullable String reason) {
        if (closed) {
            return;
        }
        closed = true;
        if (task != null) {
            task.cancel(false);
        }
        try {
            writer.close();
        } catch (IOException ignored) {
        }
        onDone.accept("Recorded " + file + (reason == null ? "" : " (stopped early: " + reason + ")") + ": " + summary);
    }

    /**
     * What a recording shows at a glance: how often timeouts came, how far apart, and whether data was in flight
     * all along, which is when usrsctp's timer runs out without any loss.
     */
    static final class Summary {
        private long samples;
        private long inFlightSamples;
        private long firstTimeouts = -1;
        private long lastTimeouts;
        private long lastTimeoutAt = -1;
        private final List<Long> spacings = new ArrayList<>();
        private long collapses;
        private long lastCwnd = -1;
        private long lastMillis;

        void add(long millis, long unackedChunks, long dataTimeouts, long congestionWindow) {
            samples++;
            lastMillis = millis;
            if (unackedChunks > 0) {
                inFlightSamples++;
            }
            if (firstTimeouts < 0) {
                firstTimeouts = dataTimeouts;
                lastTimeouts = firstTimeouts;
            } else if (dataTimeouts > lastTimeouts) {
                if (lastTimeoutAt >= 0) {
                    spacings.add(millis - lastTimeoutAt);
                }
                lastTimeoutAt = millis;
                lastTimeouts = dataTimeouts;
            }
            // One MTU is about 1.2 KB; a window that falls under 2 KB from above twice that was cut by a timeout
            if (lastCwnd > 2400 && congestionWindow < 2000) {
                collapses++;
            }
            lastCwnd = congestionWindow;
        }

        @Override
        public String toString() {
            long timeouts = firstTimeouts < 0 ? 0 : lastTimeouts - firstTimeouts;
            String spacing = "n/a";
            if (!spacings.isEmpty()) {
                List<Long> sorted = new ArrayList<>(spacings);
                sorted.sort(null);
                spacing = sorted.get(sorted.size() / 2) + " ms";
            }
            double seconds = Math.max(lastMillis, 1) / 1000.0;
            return String.format(java.util.Locale.ROOT,
                "%d samples over %.1f s, %d timeouts (%.2f/s, median %s apart), cwnd cut to one MTU %d times, data in flight %.0f%% of the time",
                samples, seconds, timeouts, timeouts / seconds, spacing, collapses,
                samples == 0 ? 0.0 : 100.0 * inFlightSamples / samples);
        }
    }
}
