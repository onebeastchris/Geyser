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

package org.geysermc.geyser.interaction;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.util.InteractionResult;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;

/**
 * Predicts what the Java client would conclude when right-clicking, without any side effects.
 * See {@code MultiPlayerGameMode#performUseItemOn} and {@code MultiPlayerGameMode#useItem}.
 */
public final class InteractionSimulator {

    /**
     * Predicts the result of the Java client clicking a block with the given hand. The Java client sends a
     * {@code ServerboundUseItemOnPacket} for every call of this method.
     */
    public static @NonNull InteractionResult useItemOn(@NonNull InteractionContext player, @NonNull Hand hand, @NonNull BlockHit hit) {
        if (player.gameMode() == GameMode.SPECTATOR) {
            return InteractionResult.CONSUME;
        }

        GeyserItemStack stack = player.itemInHand(hand);
        UseOnContext context = new UseOnContext(player, hand, stack, hit);

        boolean holdingSomething = !player.itemInHand(Hand.MAIN_HAND).isEmpty() || !player.itemInHand(Hand.OFF_HAND).isEmpty();
        if (!(player.isSecondaryUseActive() && holdingSomething)) {
            BlockState state = player.blockAt(hit.position());
            InteractionResult result = InteractionRegistry.blockUseItemOn(state.block()).useItemOn(context, state);
            if (result.consumesAction()) {
                return result;
            }

            if (result == InteractionResult.TRY_WITH_EMPTY_HAND && hand == Hand.MAIN_HAND) {
                result = InteractionRegistry.blockUseWithoutItem(state.block()).useWithoutItem(context, state);
                if (result.consumesAction()) {
                    return result;
                }
            }
        }

        if (stack.isEmpty() || player.isOnCooldown(stack)) {
            return InteractionResult.PASS;
        }
        return itemUseOn(context);
    }

    /**
     * Mirrors {@code ItemStack#useOn}.
     */
    public static @NonNull InteractionResult itemUseOn(@NonNull UseOnContext context) {
        InteractionContext player = context.player();
        if (!player.mayBuild() && !player.canPlaceOnInAdventureMode(context.stack(), context.clickedPos())) {
            return InteractionResult.PASS;
        }
        return InteractionRegistry.itemUseOn(context.stack().asItem()).useOn(context);
    }

    /**
     * Predicts the result of the Java client using the item in the given hand. The Java client sends a
     * {@code ServerboundUseItemPacket} for every call of this method, except in spectator mode, where
     * nothing is sent and the result is always {@link InteractionResult#PASS}.
     */
    public static @NonNull InteractionResult useItem(@NonNull InteractionContext player, @NonNull Hand hand) {
        if (player.gameMode() == GameMode.SPECTATOR) {
            return InteractionResult.PASS;
        }

        GeyserItemStack stack = player.itemInHand(hand);
        if (player.isOnCooldown(stack)) {
            return InteractionResult.PASS;
        }
        return InteractionRegistry.itemUse(stack.asItem()).use(player, hand, stack);
    }

    private InteractionSimulator() {
    }
}
