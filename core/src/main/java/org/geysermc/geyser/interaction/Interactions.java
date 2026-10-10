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
import org.geysermc.geyser.interaction.item.ItemUseInteractions;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.util.InteractionResult;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;

/**
 * The four interaction hooks Java Edition has for right-clicking. Handlers are registered per vanilla class name
 * in {@link InteractionRegistry}; which blocks and items use which vanilla class is generated data.
 * <p>
 * Handlers must only predict the result the Java client would come to; they must never send packets or change state.
 */
public final class Interactions {

    /**
     * Mirrors {@code BlockBehaviour#useItemOn}. Called for both hands, unless the player is sneaking with an item in either hand.
     */
    @FunctionalInterface
    public interface BlockUseItemOn {
        BlockUseItemOn DEFAULT = (context, state) -> InteractionResult.TRY_WITH_EMPTY_HAND;

        @NonNull InteractionResult useItemOn(@NonNull UseOnContext context, @NonNull BlockState state);
    }

    /**
     * Mirrors {@code BlockBehaviour#useWithoutItem}. Only called for the main hand, after {@link BlockUseItemOn}
     * returned {@link InteractionResult#TRY_WITH_EMPTY_HAND}. The context's stack is the main hand item.
     */
    @FunctionalInterface
    public interface BlockUseWithoutItem {
        BlockUseWithoutItem DEFAULT = (context, state) -> InteractionResult.PASS;

        @NonNull InteractionResult useWithoutItem(@NonNull UseOnContext context, @NonNull BlockState state);
    }

    /**
     * Mirrors {@code Item#useOn}: using a non-empty item on a block, after the block itself did not consume the click.
     */
    @FunctionalInterface
    public interface ItemUseOn {
        ItemUseOn DEFAULT = context -> InteractionResult.PASS;

        @NonNull InteractionResult useOn(@NonNull UseOnContext context);
    }

    /**
     * Mirrors {@code Item#use}: using a non-empty item, either in the air or after no block interaction consumed the click.
     */
    @FunctionalInterface
    public interface ItemUse {
        ItemUse DEFAULT = ItemUseInteractions::defaultUse;

        @NonNull InteractionResult use(@NonNull InteractionContext player, @NonNull Hand hand, @NonNull GeyserItemStack stack);
    }

    private Interactions() {
    }
}
