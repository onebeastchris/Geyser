/*
 * Copyright (c) 2024 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.platform.mod.mixin.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import org.geysermc.geyser.GeyserImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(PacketDecoder.class)
public class PacketDecoderMixin {

    @Final
    @Shadow
    private ProtocolInfo<?> protocolInfo;

//    @Redirect(method = "decode", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/Packet;type()Lnet/minecraft/network/protocol/PacketType;"))
//    private PacketType<?> geyser$decode(Packet<?> instance) {
//        if (instance instanceof ServerboundCustomPayloadPacket packet) {
//            GeyserImpl.getInstance().getLogger().error("Received serverbound custom payload packet: " + packet.payload().type().id() + " " + packet.payload().type().getClass().getSimpleName());
//        }
//        return instance.type();
//    }

    @Inject(method = "decode", locals = LocalCapture.CAPTURE_FAILSOFT, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;decode(Ljava/lang/Object;)Ljava/lang/Object;"))
    private void geyser$decodqere(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list, CallbackInfo ci, int i) {
        ByteBuf original = byteBuf.copy();
        ByteBuf copyOriginal = byteBuf.copy();
        Packet<?> packet = this.protocolInfo.codec().decode(copyOriginal);
        if (packet instanceof ServerboundCustomPayloadPacket) {
            //if (copyOriginal.readableBytes() > 0) {
            GeyserImpl.getInstance().getLogger().error("Received: " + packet.type().id());
            GeyserImpl.getInstance().getLogger().error(ByteBufUtil.prettyHexDump(original));
            //}
        }
        original.release();
        copyOriginal.release();
    }
}
