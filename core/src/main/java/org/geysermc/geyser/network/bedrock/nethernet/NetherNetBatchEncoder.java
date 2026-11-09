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
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.netty.channel.TransportChannel;
import org.cloudburstmc.protocol.bedrock.netty.BedrockBatchWrapper;
import org.cloudburstmc.protocol.bedrock.netty.BedrockPacketWrapper;
import org.cloudburstmc.protocol.bedrock.netty.codec.batch.BedrockBatchEncoder;
import org.cloudburstmc.protocol.common.util.VarInts;
import org.geysermc.geyser.GeyserLogger;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Takes the place of Cloudburst's {@link BedrockBatchEncoder} on NetherNet: batches every packet written between
 * flushes, like RakNet, with two changes.
 * <ul>
 *     <li>Batches stop at {@link #MAX_BATCH_BYTES} uncompressed (128 KiB unless {@code -DGeyser.NetherNetMaxBatchBytes}
 *     says otherwise). Cloudburst's only limit is the transport's, about 2.5 MB on NetherNet: a message the client
 *     reassembles from up to 256 segments, for which zlib also allocates eight times the input. A smaller batch also
 *     holds less behind it on the ordered channel. A packet larger than the cap still goes alone.</li>
 *     <li>Each batch's write is watched. Cloudburst writes batches with a promise no one listens to, so a batch that
 *     fails to compress, or that the channel refuses for needing too many segments, disappears without a trace.
 *     Failures are counted in {@link NetherNetStats} and logged.</li>
 * </ul>
 */
public class NetherNetBatchEncoder extends ChannelOutboundHandlerAdapter {
    public static final int MAX_BATCH_BYTES = Math.max(1024, Integer.getInteger("Geyser.NetherNetMaxBatchBytes", 128 << 10));
    /** Failures logged as warnings per connection; the rest only count, so a broken connection can't flood the log */
    static final int LOGGED_FAILURES = 5;

    private final Queue<BedrockPacketWrapper> messages = new ArrayDeque<>();
    private final NetherNetStats stats;
    private final @Nullable GeyserLogger logger;
    private final int maxBatchBytes;
    private int loggedFailures;

    public NetherNetBatchEncoder(NetherNetStats stats, @Nullable GeyserLogger logger) {
        this(stats, logger, MAX_BATCH_BYTES);
    }

    NetherNetBatchEncoder(NetherNetStats stats, @Nullable GeyserLogger logger, int maxBatchBytes) {
        this.stats = stats;
        this.logger = logger;
        this.maxBatchBytes = maxBatchBytes;
        stats.maxBatchBytes(maxBatchBytes);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof BedrockPacketWrapper packet)) {
            super.write(ctx, msg, promise);
            return;
        }
        // As in Cloudburst: the packet's own promise completes once it is queued, the batch is watched instead
        messages.add(packet);
        promise.trySuccess();
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        if (!messages.isEmpty()) {
            int limit = Math.min(maxBatchBytes, transportLimit(ctx));
            while (!messages.isEmpty()) {
                writeBatch(ctx, limit);
            }
        }
        super.flush(ctx);
    }

    /** What Cloudburst's encoder allows: the transport's largest message, less room for compression to grow it */
    private static int transportLimit(ChannelHandlerContext ctx) {
        if (!(ctx.channel() instanceof TransportChannel transport)) {
            return Integer.MAX_VALUE;
        }
        int maxMessageSize = transport.maxMessageSize();
        return maxMessageSize - maxMessageSize / 64;
    }

    private void writeBatch(ChannelHandlerContext ctx, int limit) {
        CompositeByteBuf buf = ctx.alloc().compositeDirectBuffer(messages.size() * 2);
        BedrockBatchWrapper batch = BedrockBatchWrapper.newInstance();
        try {
            BedrockPacketWrapper packet;
            while ((packet = messages.peek()) != null) {
                ByteBuf message = packet.getPacketBuffer();
                if (message != null) {
                    int length = message.readableBytes();
                    int framed = VarInts.sizeOfUnsignedInt(length) + length;
                    if (buf.isReadable() && buf.readableBytes() + framed > limit) {
                        stats.batchCut();
                        break;
                    }
                    if (!buf.isReadable() && framed > limit) {
                        stats.oversizedPacket();
                    }
                }
                messages.poll();
                try {
                    if (message == null) {
                        throw new IllegalArgumentException("BedrockPacket is not encoded");
                    }
                    BedrockBatchEncoder.addPrefixed(ctx, buf, message, packet.getReservedPrefixBytes());
                    batch.addPacket(packet.retain());
                } finally {
                    packet.release();
                }
            }

            int packets = batch.getPackets().size();
            int bytes = buf.readableBytes();
            batch.setUncompressed(buf.retain());
            ctx.write(batch.retain()).addListener(future -> onWritten((ChannelFuture) future, packets, bytes));
        } finally {
            buf.release();
            batch.release();
        }
    }

    private void onWritten(ChannelFuture future, int packets, int bytes) {
        if (future.isSuccess()) {
            return;
        }
        Throwable cause = future.cause();
        stats.writeFailed(packets, cause);
        if (logger != null && loggedFailures < LOGGED_FAILURES) {
            loggedFailures++;
            logger.warning("[NetherNet] A batch of " + packets + " packets (" + bytes + " bytes uncompressed) to "
                + future.channel().remoteAddress() + " could not be sent: " + cause
                + (loggedFailures == LOGGED_FAILURES ? ". Further failures on this connection are only counted." : ""));
            logger.debug("[NetherNet] Batch write failure: " + cause);
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        BedrockPacketWrapper message;
        while ((message = messages.poll()) != null) {
            message.release();
        }
        super.handlerRemoved(ctx);
    }
}
