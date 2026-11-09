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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import org.cloudburstmc.protocol.bedrock.netty.BedrockBatchWrapper;
import org.cloudburstmc.protocol.bedrock.netty.BedrockPacketWrapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetherNetBatchEncoderTest {

    private static BedrockPacketWrapper packet(int size) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(size);
        buf.writeZero(size);
        BedrockPacketWrapper wrapper = BedrockPacketWrapper.create();
        wrapper.setPacketBuffer(buf);
        return wrapper;
    }

    private static List<Integer> flushAndSizes(EmbeddedChannel channel) {
        channel.flush();
        List<Integer> sizes = new ArrayList<>();
        Object out;
        while ((out = channel.readOutbound()) != null) {
            BedrockBatchWrapper batch = (BedrockBatchWrapper) out;
            sizes.add(batch.getUncompressed().readableBytes());
            ReferenceCountUtil.release(batch);
        }
        return sizes;
    }

    @Test
    void batchesStopAtTheCap() {
        NetherNetStats stats = new NetherNetStats();
        EmbeddedChannel channel = new EmbeddedChannel(new NetherNetBatchEncoder(stats, null, 10_000));
        for (int i = 0; i < 5; i++) {
            channel.write(packet(3000)); // 3002 bytes each with the VarInt length
        }
        // Three fit in 10000 bytes, the other two go in a second batch
        assertEquals(List.of(9006, 6004), flushAndSizes(channel));
        NetherNetStats.Batching batching = statsOf(stats);
        assertEquals(10_000, batching.maxBatchBytes());
        assertEquals(1, batching.batchCuts());
        assertEquals(0, batching.oversizedPackets());
        channel.finishAndReleaseAll();
    }

    @Test
    void packetOverTheCapGoesAlone() {
        NetherNetStats stats = new NetherNetStats();
        EmbeddedChannel channel = new EmbeddedChannel(new NetherNetBatchEncoder(stats, null, 10_000));
        channel.write(packet(100));
        channel.write(packet(20_000));
        channel.write(packet(100));
        assertEquals(List.of(101, 20_003, 101), flushAndSizes(channel));
        NetherNetStats.Batching batching = statsOf(stats);
        assertEquals(1, batching.oversizedPackets());
        assertEquals(2, batching.batchCuts());
        channel.finishAndReleaseAll();
    }

    @Test
    void failedBatchWritesAreCounted() {
        NetherNetStats stats = new NetherNetStats();
        // Fails every write, as compression or the channel's segment limit would
        ChannelOutboundHandlerAdapter failing = new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                ReferenceCountUtil.release(msg);
                promise.setFailure(new IllegalArgumentException("needs 300 segments"));
            }
        };
        EmbeddedChannel channel = new EmbeddedChannel(failing, new NetherNetBatchEncoder(stats, null, 10_000));
        channel.write(packet(100));
        channel.write(packet(100));
        channel.flush();
        NetherNetStats.Batching batching = statsOf(stats);
        assertEquals(1, batching.writeFailures());
        assertEquals(2, batching.packetsInFailedWrites());
        assertTrue(batching.lastWriteFailure().contains("needs 300 segments"), batching.lastWriteFailure());
        channel.finishAndReleaseAll();
    }

    @Test
    void segmentsMatchNetherNetChannel() {
        assertEquals(0, NetherNetStats.segments(0));
        assertEquals(1, NetherNetStats.segments(9999));
        assertEquals(2, NetherNetStats.segments(10_000));
        assertEquals(256, NetherNetStats.segments(256L * 9999));
    }

    private static NetherNetStats.Batching statsOf(NetherNetStats stats) {
        return stats.batching(-1);
    }
}
