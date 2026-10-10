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

package org.geysermc.geyser.interaction.item;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.geyser.interaction.InteractionContext;
import org.geysermc.geyser.interaction.InteractionRegistry;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.util.InteractionResult;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.Consumable;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.Equippable;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.FoodProperties;

/**
 * Item interactions without a targeted block ({@code Item#use}). Most items are driven purely by their data components,
 * which the server sends us, see {@link #defaultUse}.
 */
public final class ItemUseInteractions {

    public static void register(InteractionRegistry.Registrar registrar) {
    }

    /**
     * Mirrors {@code Item#use}, which is entirely driven by data components.
     */
    public static @NonNull InteractionResult defaultUse(@NonNull InteractionContext player, @NonNull Hand hand, @NonNull GeyserItemStack stack) {
        Consumable consumable = stack.getComponent(DataComponentTypes.CONSUMABLE);
        if (consumable != null) {
            return startConsuming(player, stack);
        }

        Equippable equippable = stack.getComponent(DataComponentTypes.EQUIPPABLE);
        if (equippable != null && equippable.swappable()) {
            return EquipmentInteractions.swapWithEquipmentSlot(player, stack, equippable);
        }

        if (stack.getComponent(DataComponentTypes.BLOCKS_ATTACKS) != null) {
            return InteractionResult.CONSUME;
        }

        if (stack.getComponent(DataComponentTypes.KINETIC_WEAPON) != null) {
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    /**
     * Mirrors {@code Consumable#startConsuming}. Both consuming over time and instant consumption result in
     * {@link InteractionResult#CONSUME} on the client.
     */
    public static @NonNull InteractionResult startConsuming(@NonNull InteractionContext player, @NonNull GeyserItemStack stack) {
        FoodProperties food = stack.getComponent(DataComponentTypes.FOOD);
        if (food != null && !player.canEat(food.isCanAlwaysEat())) {
            return InteractionResult.FAIL;
        }
        return InteractionResult.CONSUME;
    }

    private ItemUseInteractions() {
    }
}
