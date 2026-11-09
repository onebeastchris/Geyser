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

import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.level.ServerboundChunkBatchReceivedPacket;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChunkBatchPacerTest {

    private final List<Runnable> pings = new ArrayList<>();

    private GeyserSession session(boolean spawned) {
        GeyserSession session = mock(GeyserSession.class);
        when(session.isSpawned()).thenReturn(spawned);
        doAnswer(invocation -> pings.add(invocation.getArgument(2)))
            .when(session).sendNetworkLatencyStackPacket(anyLong(), anyBoolean(), any());
        return session;
    }

    private static float ackedChunksPerTick(GeyserSession session, int times) {
        ArgumentCaptor<ServerboundChunkBatchReceivedPacket> captor = ArgumentCaptor.forClass(ServerboundChunkBatchReceivedPacket.class);
        verify(session, times(times)).sendDownstreamGamePacket(captor.capture());
        return captor.getValue().getDesiredChunksPerTick();
    }

    @Test
    void acknowledgesImmediatelyWhileNotSpawned() {
        GeyserSession session = session(false);
        ChunkBatchPacer pacer = new ChunkBatchPacer(session);

        pacer.onBatchStart();
        pacer.onBatchFinished(9);

        assertTrue(pings.isEmpty());
        assertEquals(20.0F, ackedChunksPerTick(session, 1));
    }

    @Test
    void waitsForTheClientBeforeAcknowledging() {
        GeyserSession session = session(true);
        ChunkBatchPacer pacer = new ChunkBatchPacer(session);

        pacer.onBatchStart();
        pacer.onBatchFinished(9);
        assertEquals(2, pings.size());
        verify(session, never()).sendDownstreamGamePacket(any());

        pings.get(0).run();
        verify(session, never()).sendDownstreamGamePacket(any());
        pings.get(1).run();
        ackedChunksPerTick(session, 1);

        // A late duplicate (timeout already fired, for example) doesn't acknowledge twice
        pings.get(1).run();
        ackedChunksPerTick(session, 1);
    }

    @Test
    void slowsDownWhenBatchesArriveSlowly() throws InterruptedException {
        GeyserSession session = session(true);
        ChunkBatchPacer pacer = new ChunkBatchPacer(session);
        float initial = pacer.desiredChunksPerTick();

        for (int i = 0; i < 5; i++) {
            pings.clear();
            pacer.onBatchStart();
            pacer.onBatchFinished(2);
            pings.get(0).run();
            Thread.sleep(40); // 20ms per chunk, so under 2 chunks per tick
            pings.get(1).run();
        }

        assertTrue(pacer.desiredChunksPerTick() < initial,
            "expected fewer than " + initial + " chunks per tick, got " + pacer.desiredChunksPerTick());
    }
}
