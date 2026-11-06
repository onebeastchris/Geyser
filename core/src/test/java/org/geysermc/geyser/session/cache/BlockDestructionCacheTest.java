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

import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.LevelEvent;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.packet.LevelEventPacket;
import org.geysermc.geyser.session.GeyserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class BlockDestructionCacheTest {
    private static final Vector3i POS = Vector3i.from(1, 2, 3);
    private static final Vector3i OTHER_POS = Vector3i.from(4, 5, 6);

    private final List<LevelEventPacket> sent = new ArrayList<>();
    private BlockDestructionCache cache;
    private int ticks;

    @BeforeEach
    void setUp() {
        GeyserSession session = mock(GeyserSession.class);
        doAnswer(invocation -> {
            BedrockPacket packet = invocation.getArgument(0);
            if (packet instanceof LevelEventPacket levelEvent) {
                sent.add(levelEvent);
            }
            return null;
        }).when(session).sendUpstreamPacket(any());
        cache = new BlockDestructionCache(session);
        ticks = 0;
    }

    private void advanceTo(int tick) {
        while (ticks < tick) {
            ticks++;
            cache.tick();
        }
    }

    private void progress(int breakerId, Vector3i pos, int progress, int tick) {
        advanceTo(tick);
        cache.destroyBlockProgress(breakerId, pos, progress);
    }

    private LevelEventPacket single() {
        assertEquals(1, sent.size(), sent::toString);
        LevelEventPacket packet = sent.getFirst();
        sent.clear();
        return packet;
    }

    @Test
    void rendersHighestProgressLikeJava() {
        progress(1, POS, 6, 0);
        progress(2, POS, 2, 1);
        assertEquals(6, cache.renderedProgress(POS));
        progress(2, POS, 8, 2);
        assertEquals(8, cache.renderedProgress(POS));
        progress(2, POS, -1, 3);
        assertEquals(6, cache.renderedProgress(POS));
        progress(1, POS, 10, 4);
        assertNull(cache.renderedProgress(POS));
    }

    @Test
    void staleProgressRemovedOnTwentyTickCheck() {
        progress(1, POS, 0, 0);
        sent.clear();
        // LevelRenderer#tick: only checked every 20 ticks, and removed once more than 400 ticks old
        advanceTo(BlockDestructionCache.STALE_TICKS);
        assertEquals(0, cache.renderedProgress(POS));
        advanceTo(BlockDestructionCache.STALE_TICKS + BlockDestructionCache.STALE_CHECK_INTERVAL - 1);
        assertEquals(0, cache.renderedProgress(POS));
        advanceTo(BlockDestructionCache.STALE_TICKS + BlockDestructionCache.STALE_CHECK_INTERVAL);
        assertNull(cache.renderedProgress(POS));
        assertEquals(LevelEvent.BLOCK_STOP_BREAK, single().getType());
    }

    @Test
    void steadyBreakReachesNextStageOnTime() {
        progress(1, POS, 0, 0);
        assertEquals(LevelEvent.BLOCK_START_BREAK, single().getType());

        progress(1, POS, 1, 4);
        LevelEventPacket update = single();
        assertEquals(LevelEvent.BLOCK_UPDATE_BREAK, update.getType());
        // Displayed progress is ~0 after a slow start; reach 0.2 in 4 ticks
        int rate = update.getData();
        assertTrue(Math.abs(rate - 0.2 * 65535 / 4) < 50, "rate " + rate);
    }

    @Test
    void newBreakerAtStaleSameStageStillStarts() {
        // Breaker 1 leaves a block at stage 0 without a reset (e.g. the reset was sent while we were out of range)
        progress(1, POS, 0, 0);
        sent.clear();
        // Long after, a different breaker starts on the same block - the stale entry has expired like on Java
        advanceTo(500);
        assertEquals(LevelEvent.BLOCK_STOP_BREAK, single().getType());
        progress(2, POS, 0, 501);
        assertEquals(LevelEvent.BLOCK_START_BREAK, single().getType());
    }

    @Test
    void staleProgressDoesNotFreezeNewBreak() {
        progress(1, POS, 0, 0);
        progress(1, POS, 1, 2);
        advanceTo(10);
        sent.clear();
        // Out of range for a while, the server's reset never reached us; a new attempt from the same breaker later on
        advanceTo(1000);
        assertEquals(LevelEvent.BLOCK_STOP_BREAK, single().getType());
        progress(1, POS, 0, 1000);
        assertEquals(LevelEvent.BLOCK_START_BREAK, single().getType());
        progress(1, POS, 1, 1004);
        LevelEventPacket update = single();
        assertEquals(LevelEvent.BLOCK_UPDATE_BREAK, update.getType());
        assertTrue(update.getData() > BlockDestructionCache.HOLD_RATE);
    }

    @Test
    void interleavedBreakersShowHighestStageWithoutRestarting() {
        progress(1, POS, 6, 0);
        sent.clear();
        progress(2, POS, 2, 1);
        progress(1, POS, 7, 3);
        progress(2, POS, 3, 4);
        // Only breaker 1 changing the highest stage triggers an update
        assertEquals(LevelEvent.BLOCK_UPDATE_BREAK, single().getType());
    }

    @Test
    void leaderResetDropsToRemainingBreaker() {
        progress(1, POS, 6, 0);
        progress(2, POS, 2, 1);
        sent.clear();
        progress(1, POS, -1, 2);
        // Java now renders breaker 2's lower stage; Bedrock needs a restart for that
        assertEquals(LevelEvent.BLOCK_START_BREAK, single().getType());
        progress(2, POS, -1, 3);
        assertEquals(LevelEvent.BLOCK_STOP_BREAK, single().getType());
    }

    @Test
    void batchedPacketsDoNotRushAnimation() {
        progress(1, POS, 0, 0);
        progress(1, POS, 1, 10);
        progress(1, POS, 2, 20);
        sent.clear();
        // Two stages arrive in the same tick after lag
        progress(1, POS, 3, 40);
        progress(1, POS, 4, 40);
        // Averaged over the whole attempt: 10 ticks per stage, so never close to the max rate
        for (LevelEventPacket packet : sent) {
            assertTrue(packet.getData() < 65535 / 10, "rate " + packet.getData());
        }
    }

    @Test
    void overshootHoldsInsteadOfAborting() {
        progress(1, POS, 0, 0);
        progress(1, POS, 1, 2);
        sent.clear();
        // Expected stage 2 at tick 4, but breaking slowed down
        advanceTo(39);
        LevelEventPacket hold = single();
        assertEquals(LevelEvent.BLOCK_UPDATE_BREAK, hold.getType());
        assertEquals(BlockDestructionCache.HOLD_RATE, hold.getData());

        progress(1, POS, 2, 40);
        assertTrue(single().getData() > BlockDestructionCache.HOLD_RATE);
    }

    @Test
    void midBreakStartCatchesUpThenHolds() {
        progress(1, POS, 6, 0);
        LevelEventPacket start = single();
        assertEquals(LevelEvent.BLOCK_START_BREAK, start.getType());
        // Catch up to 0.6 in a few ticks rather than crawling from zero
        assertTrue(start.getData() >= 0.6 * 65535 / BlockDestructionCache.CATCH_UP_TICKS, "rate " + start.getData());

        advanceTo(BlockDestructionCache.CATCH_UP_TICKS + 1);
        assertEquals(BlockDestructionCache.HOLD_RATE, single().getData());
    }

    @Test
    void breakerMovingToAnotherBlockClearsPrevious() {
        progress(1, POS, 3, 0);
        sent.clear();
        progress(1, OTHER_POS, 0, 2);
        assertEquals(2, sent.size());
        assertEquals(LevelEvent.BLOCK_STOP_BREAK, sent.get(0).getType());
        assertEquals(POS.toFloat(), sent.get(0).getPosition());
        assertEquals(LevelEvent.BLOCK_START_BREAK, sent.get(1).getType());
        assertEquals(OTHER_POS.toFloat(), sent.get(1).getPosition());
        assertNull(cache.renderedProgress(POS));
    }

    @Test
    void neverSendsZeroRate() {
        progress(1, POS, 0, 0);
        progress(1, POS, 1, 1);
        progress(1, POS, 2, 1);
        progress(1, POS, 3, 100);
        advanceTo(200);
        for (LevelEventPacket packet : sent) {
            if (packet.getType() != LevelEvent.BLOCK_STOP_BREAK) {
                assertTrue(packet.getData() > 0, sent::toString);
            }
        }
    }
}
