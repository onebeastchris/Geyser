/*
 * Copyright (c) 2019-2026 GeyserMC. http://geysermc.org
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

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.LevelEvent;
import org.cloudburstmc.protocol.bedrock.packet.LevelEventPacket;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.util.BlockUtils;

import java.util.Iterator;

/**
 * Block destruction progress of other entities, translated to the Bedrock crack animation.
 * <p>
 * The tracking half of this class is a port of the Java client (Mojang mappings):
 * {@code ClientPacketListener#handleBlockDestruction} calls {@code ClientLevel#destroyBlockProgress}, which
 * delegates to {@code LevelRenderer#destroyBlockProgress}. {@code LevelRenderer} keeps
 * {@code destroyingBlocks} (breaker id to progress) and {@code destructionProgress} (position to a sorted set of
 * progress), drops progress that was not updated for 400 ticks in {@code LevelRenderer#tick}, and renders
 * {@code destructionProgress.get(pos).last()} - the highest stage at a position.
 * Names and control flow are kept as close as possible to the Java client so the two can be compared easily.
 * <p>
 * The Geyser-specific half ({@link #updateBedrockProgress} and below) exists because Java renders the stage it
 * received, while Bedrock animates the cracks itself from a per-tick progress rate. We estimate the rate from each
 * breaker's stage history and aim to reach the next stage when the next Java update is expected. If it arrives late,
 * the animation is held at that stage - just like the Java client keeps rendering the last stage it received.
 */
public final class BlockDestructionCache {
    /**
     * {@code LevelRenderer#tick}: progress not updated for this many ticks is removed.
     */
    static final int STALE_TICKS = 400;
    /**
     * {@code LevelRenderer#tick}: how often stale progress is checked for.
     */
    static final int STALE_CHECK_INTERVAL = 20;
    /**
     * {@code LevelRenderer#destroyBlockProgress}: progress outside [0, 10) removes the breaker's progress.
     */
    static final int MAX_PROGRESS = 10;

    /**
     * Geyser: Bedrock's full destruction progress, per tick.
     */
    static final int MAX_RATE = 65535;
    /**
     * Geyser: used while we know nothing about how fast a block is being broken (5 minutes for the full block).
     */
    static final int UNKNOWN_RATE = MAX_RATE / 6000;
    /**
     * Geyser: holds the Bedrock animation in place. Data 0 is avoided, as that is used to abort breaking.
     */
    static final int HOLD_RATE = 1;
    /**
     * Geyser: ticks used to catch the Bedrock animation up to the stage Java is already rendering.
     */
    static final int CATCH_UP_TICKS = 5;

    private final GeyserSession session;

    /**
     * {@code LevelRenderer#destroyingBlocks}
     */
    private final Int2ObjectMap<BlockDestructionProgress> destroyingBlocks = new Int2ObjectOpenHashMap<>();
    /**
     * {@code LevelRenderer#destructionProgress} - Java keys this by {@code BlockPos#asLong}.
     */
    private final Object2ObjectMap<Vector3i, ObjectSortedSet<BlockDestructionProgress>> destructionProgress = new Object2ObjectOpenHashMap<>();
    /**
     * {@code LevelRenderer#ticks}
     */
    private int ticks;

    /**
     * Geyser: what the Bedrock client is currently animating, per position.
     */
    private final Object2ObjectMap<Vector3i, BedrockProgress> bedrockProgress = new Object2ObjectOpenHashMap<>();

    public BlockDestructionCache(GeyserSession session) {
        this.session = session;
    }

    /**
     * {@code LevelRenderer#tick} - must be called once per client tick.
     */
    public void tick() {
        this.ticks++;
        if (this.ticks % STALE_CHECK_INTERVAL == 0) {
            Iterator<BlockDestructionProgress> iterator = this.destroyingBlocks.values().iterator();

            while (iterator.hasNext()) {
                BlockDestructionProgress progress = iterator.next();
                int updatedRenderTick = progress.getUpdatedRenderTick();
                if (this.ticks - updatedRenderTick > STALE_TICKS) {
                    iterator.remove();
                    this.removeProgress(progress);
                    this.updateBedrockProgress(progress.getPos());
                }
            }
        }

        this.tickBedrockProgress();
    }

    /**
     * {@code LevelRenderer#removeProgress}
     */
    private void removeProgress(BlockDestructionProgress progress) {
        Vector3i pos = progress.getPos();
        ObjectSortedSet<BlockDestructionProgress> set = this.destructionProgress.get(pos);
        set.remove(progress);
        if (set.isEmpty()) {
            this.destructionProgress.remove(pos);
        }
    }

    /**
     * {@code LevelRenderer#destroyBlockProgress}
     */
    public void destroyBlockProgress(int breakerId, Vector3i pos, int progress) {
        if (progress >= 0 && progress < MAX_PROGRESS) {
            BlockDestructionProgress existing = this.destroyingBlocks.get(breakerId);
            if (existing != null) {
                this.removeProgress(existing);
            }

            if (existing == null || !existing.getPos().equals(pos)) {
                // Geyser: Java renders the old position without this breaker from now on
                if (existing != null) {
                    this.updateBedrockProgress(existing.getPos());
                }
                existing = new BlockDestructionProgress(breakerId, pos);
                this.destroyingBlocks.put(breakerId, existing);
            }

            existing.updateTimings(progress, this.ticks); // Geyser
            existing.setProgress(progress);
            existing.updateTick(this.ticks);
            this.destructionProgress.computeIfAbsent(existing.getPos(), p -> new ObjectRBTreeSet<>()).add(existing);
            this.updateBedrockProgress(pos);
        } else {
            BlockDestructionProgress removed = this.destroyingBlocks.remove(breakerId);
            if (removed != null) {
                this.removeProgress(removed);
                this.updateBedrockProgress(removed.getPos());
            }
        }
    }

    /**
     * Clears everything, e.g. on a dimension change. Like the Java client, which creates a new level and renderer
     * state, no packets are sent - the Bedrock client forgets the animations on its own.
     */
    public void reset() {
        this.destroyingBlocks.clear();
        this.destructionProgress.clear();
        this.bedrockProgress.clear();
    }

    // Everything below is Geyser-specific: turning the progress Java would render into the Bedrock crack animation.

    /**
     * Brings the Bedrock animation in line with what the Java client would render at this position:
     * {@code destructionProgress.get(pos).last()}.
     */
    private void updateBedrockProgress(Vector3i pos) {
        ObjectSortedSet<BlockDestructionProgress> set = this.destructionProgress.get(pos);
        BlockDestructionProgress rendered = set == null ? null : set.last();
        BedrockProgress shown = this.bedrockProgress.get(pos);

        if (rendered == null) {
            if (shown != null) {
                this.bedrockProgress.remove(pos);
                BlockUtils.sendBedrockStopBlockBreak(this.session, pos.toFloat());
            }
            return;
        }

        int stage = rendered.getProgress();
        if (shown == null || stage < shown.stage) {
            // Nothing shown yet, or Java now renders a lower stage - Bedrock can only lower the animation by restarting it
            if (shown == null) {
                shown = new BedrockProgress();
                this.bedrockProgress.put(pos, shown);
            }
            shown.stage = stage;
            shown.set(this.ticks, 0, 0);
            shown.aim(rendered, this.ticks);
            this.sendLevelEvent(LevelEvent.BLOCK_START_BREAK, pos, shown.rate);
        } else if (stage > shown.stage) {
            shown.set(this.ticks, shown.progressAt(this.ticks), shown.rate);
            shown.stage = stage;
            shown.aim(rendered, this.ticks);
            this.sendLevelEvent(LevelEvent.BLOCK_UPDATE_BREAK, pos, shown.rate);
        }
        // Same stage (keep-alive or another breaker): Java renders no change either
    }

    /**
     * Holds Bedrock animations that reached the stage we expected the next Java update for.
     */
    private void tickBedrockProgress() {
        for (Object2ObjectMap.Entry<Vector3i, BedrockProgress> entry : this.bedrockProgress.object2ObjectEntrySet()) {
            BedrockProgress shown = entry.getValue();
            if (shown.rate > HOLD_RATE && shown.progressAt(this.ticks) >= shown.holdProgress) {
                shown.set(this.ticks, shown.holdProgress, HOLD_RATE);
                this.sendLevelEvent(LevelEvent.BLOCK_UPDATE_BREAK, entry.getKey(), HOLD_RATE);
            }
        }
    }

    private void sendLevelEvent(LevelEvent type, Vector3i pos, int data) {
        LevelEventPacket packet = new LevelEventPacket();
        packet.setType(type);
        packet.setPosition(pos.toFloat());
        packet.setData(data);
        this.session.sendUpstreamPacket(packet);
    }

    /**
     * Port of {@code net.minecraft.server.level.BlockDestructionProgress}, plus Geyser-specific timings
     * used to estimate the breaking speed.
     */
    private static final class BlockDestructionProgress implements Comparable<BlockDestructionProgress> {
        private final int id;
        private final Vector3i pos;
        private int progress;
        private int updatedRenderTick;

        /**
         * Geyser: stage and tick of the first update in the current break attempt.
         */
        private int firstProgress = -1;
        private int firstProgressTick;
        /**
         * Geyser: tick the current stage was first received; keep-alive updates for the same stage don't change it.
         */
        private int progressTick;

        private BlockDestructionProgress(int id, Vector3i pos) {
            this.id = id;
            this.pos = pos;
        }

        private Vector3i getPos() {
            return this.pos;
        }

        private void setProgress(int progress) {
            if (progress > MAX_PROGRESS) {
                progress = MAX_PROGRESS;
            }

            this.progress = progress;
        }

        private int getProgress() {
            return this.progress;
        }

        private void updateTick(int tick) {
            this.updatedRenderTick = tick;
        }

        private int getUpdatedRenderTick() {
            return this.updatedRenderTick;
        }

        /**
         * Geyser: must be called before {@link #setProgress(int)}.
         * A lower stage means a new attempt on the same block, so earlier timings don't apply anymore.
         */
        private void updateTimings(int progress, int tick) {
            if (this.firstProgress == -1 || progress < this.progress) {
                this.firstProgress = progress;
                this.firstProgressTick = tick;
                this.progressTick = tick;
            } else if (progress > this.progress) {
                this.progressTick = tick;
            }
        }

        /**
         * Geyser: average ticks per stage over the whole attempt, so that a few batched packets don't skew the estimate.
         *
         * @return the estimate, or NaN if no stage change was observed yet
         */
        private double ticksPerStage() {
            if (this.progress <= this.firstProgress) {
                return Double.NaN;
            }
            return Math.max(1, (double) (this.progressTick - this.firstProgressTick) / (this.progress - this.firstProgress));
        }

        @Override
        public boolean equals(Object o) {
            return this == o || o instanceof BlockDestructionProgress other && this.id == other.id;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(this.id);
        }

        @Override
        public int compareTo(BlockDestructionProgress other) {
            return this.progress != other.progress ? Integer.compare(this.progress, other.progress) : Integer.compare(this.id, other.id);
        }
    }

    /**
     * Geyser: the crack animation the Bedrock client is running at a position.
     */
    private static final class BedrockProgress {
        private int stage;
        private int tick;
        private double progress;
        private int rate;
        private double holdProgress;

        private void set(int tick, double progress, int rate) {
            this.tick = tick;
            this.progress = progress;
            this.rate = rate;
        }

        private double progressAt(int tick) {
            return Math.min(1, this.progress + (double) this.rate * Math.max(0, tick - this.tick) / MAX_RATE);
        }

        /**
         * Picks the rate and hold point, based on the progress the client is currently showing.
         */
        private void aim(BlockDestructionProgress rendered, int tick) {
            double current = this.progress;
            double stageStart = (double) this.stage / MAX_PROGRESS;
            double nextStage = (double) (this.stage + 1) / MAX_PROGRESS;
            double ticksPerStage = rendered.ticksPerStage();

            // Reach the next stage around when we expect the next Java update
            double nextRate = Double.isNaN(ticksPerStage) ? Double.NaN : (nextStage - current) * MAX_RATE / ticksPerStage;
            // Behind what Java already renders (e.g. started mid-break) - catch up quickly
            double catchUpRate = current < stageStart ? (stageStart - current) * MAX_RATE / CATCH_UP_TICKS : 0;

            double newRate;
            if (!Double.isNaN(nextRate) && nextRate >= catchUpRate) {
                newRate = nextRate;
                this.holdProgress = nextStage;
            } else if (catchUpRate > 0) {
                // Then wait at the current stage for the next update, like Java does
                newRate = catchUpRate;
                this.holdProgress = stageStart;
            } else {
                newRate = UNKNOWN_RATE;
                this.holdProgress = nextStage;
            }

            if (current >= this.holdProgress) {
                this.holdProgress = current;
                this.set(tick, current, HOLD_RATE);
            } else {
                this.set(tick, current, (int) Math.clamp((long) Math.ceil(newRate), HOLD_RATE, MAX_RATE));
            }
        }
    }

    // Visible for testing
    @Nullable Integer renderedProgress(Vector3i pos) {
        ObjectSortedSet<BlockDestructionProgress> set = this.destructionProgress.get(pos);
        return set == null ? null : set.last().getProgress();
    }
}
