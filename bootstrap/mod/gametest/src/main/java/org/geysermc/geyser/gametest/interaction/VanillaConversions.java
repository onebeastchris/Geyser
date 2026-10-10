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

package org.geysermc.geyser.gametest.interaction;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.cloudburstmc.math.vector.Vector3d;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.item.components.resolvable.ResolvableComponentGetter;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;

/**
 * Conversions between vanilla and Geyser/MCPL types.
 */
public final class VanillaConversions {

    /**
     * Converts a vanilla stack to a Geyser stack by encoding it with the vanilla network codec and decoding it with MCPL,
     * like Geyser receives it from a server.
     */
    public static GeyserItemStack toGeyser(ItemStack stack, RegistryAccess registries, ResolvableComponentGetter componentGetter) {
        if (stack.isEmpty()) {
            return GeyserItemStack.EMPTY;
        }
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
        try {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, stack); // TODO verify signature (ItemStack.OPTIONAL_STREAM_CODEC)
            org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack mcpl = MinecraftTypes.readOptionalItemStack(buffer);
            if (mcpl == null) {
                return GeyserItemStack.EMPTY;
            }
            return GeyserItemStack.of(componentGetter, mcpl.getId(), mcpl.getAmount(), mcpl.getDataComponentsPatch());
        } finally {
            buffer.release();
        }
    }

    public static org.geysermc.geyser.level.block.type.BlockState toGeyser(net.minecraft.world.level.block.state.BlockState state) {
        return org.geysermc.geyser.level.block.type.BlockState.of(Block.getId(state));
    }

    public static Hand toGeyser(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? Hand.MAIN_HAND : Hand.OFF_HAND;
    }

    public static Direction toGeyser(net.minecraft.core.Direction direction) {
        return Direction.valueOf(direction.name());
    }

    public static GameMode toGeyser(GameType gameType) {
        return GameMode.byId(gameType.getId());
    }

    public static Vector3i toGeyser(BlockPos pos) {
        return Vector3i.from(pos.getX(), pos.getY(), pos.getZ());
    }

    public static Vector3d toGeyser(Vec3 vec) {
        return Vector3d.from(vec.x, vec.y, vec.z);
    }

    public static BlockPos toVanilla(Vector3i pos) {
        return new BlockPos(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Maps a vanilla interaction result to Geyser's enum. {@code heldItemTransformedTo} is not modelled by Geyser.
     */
    public static org.geysermc.geyser.util.InteractionResult toGeyser(InteractionResult result) {
        if (result instanceof InteractionResult.Success success) {
            return switch (success.swingSource()) {
                case CLIENT -> org.geysermc.geyser.util.InteractionResult.SUCCESS;
                case SERVER -> org.geysermc.geyser.util.InteractionResult.SUCCESS_SERVER;
                case NONE -> org.geysermc.geyser.util.InteractionResult.CONSUME;
            };
        } else if (result instanceof InteractionResult.Fail) {
            return org.geysermc.geyser.util.InteractionResult.FAIL;
        } else if (result instanceof InteractionResult.TryEmptyHandInteraction) {
            return org.geysermc.geyser.util.InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        return org.geysermc.geyser.util.InteractionResult.PASS;
    }

    private VanillaConversions() {
    }
}
