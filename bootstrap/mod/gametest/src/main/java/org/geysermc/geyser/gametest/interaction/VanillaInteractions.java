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

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The Java client's right click logic ({@code MultiPlayerGameMode#performUseItemOn} and {@code MultiPlayerGameMode#useItem}),
 * calling into vanilla block and item code, without sending packets. Run it with a player living in a
 * client-side level so that vanilla takes its client-side branches.
 */
public final class VanillaInteractions {

    /**
     * Mirrors {@code MultiPlayerGameMode#performUseItemOn}. The feature flag check is left out, as everything in the
     * game test world is enabled.
     */
    public static InteractionResult useItemOn(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (player.gameMode() == GameType.SPECTATOR) {
            return InteractionResult.CONSUME;
        }

        boolean haveSomethingInOurHands = !player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty();
        boolean suppressUsingBlock = player.isSecondaryUseActive() && haveSomethingInOurHands;
        if (!suppressUsingBlock) {
            BlockState blockState = level.getBlockState(hit.getBlockPos());
            InteractionResult itemUse = blockState.useItemOn(player.getItemInHand(hand), level, player, hand, hit);
            if (itemUse.consumesAction()) {
                return itemUse;
            }

            if (itemUse instanceof InteractionResult.TryEmptyHandInteraction && hand == InteractionHand.MAIN_HAND) {
                InteractionResult use = blockState.useWithoutItem(level, player, hit);
                if (use.consumesAction()) {
                    return use;
                }
            }
        }

        if (!itemStack.isEmpty() && !player.getCooldowns().isOnCooldown(itemStack)) {
            return itemStack.useOn(new UseOnContext(player, hand, hit));
        }
        return InteractionResult.PASS;
    }

    /**
     * Mirrors {@code MultiPlayerGameMode#useItem}. {@code Minecraft#startUseItem} only calls it for non-empty stacks.
     */
    public static InteractionResult useItem(Player player, Level level, InteractionHand hand) {
        if (player.gameMode() == GameType.SPECTATOR) {
            return InteractionResult.PASS;
        }
        ItemStack itemStack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(itemStack)) {
            return InteractionResult.PASS;
        }
        return itemStack.use(level, player, hand);
    }

    private VanillaInteractions() {
    }
}
