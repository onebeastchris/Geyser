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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.geyser.interaction.InteractionContext;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.item.enchantment.EnchantmentComponent;
import org.geysermc.geyser.session.cache.registry.JavaRegistries;
import org.geysermc.geyser.util.InteractionResult;
import org.geysermc.mcprotocollib.protocol.data.game.entity.EquipmentSlot;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponents;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.Equippable;

import java.util.Objects;

/**
 * Equipment related item interactions.
 */
public final class EquipmentInteractions {
    /**
     * Network ID of {@code minecraft:player} in the entity type registry, which is what equippable items check against.
     */
    private static final int PLAYER_ENTITY_TYPE = EntityType.PLAYER.ordinal();

    /**
     * Mirrors {@code Equippable#swapWithEquipmentSlot}, which swaps the used item with the item in its equipment slot.
     */
    public static @NonNull InteractionResult swapWithEquipmentSlot(@NonNull InteractionContext player, @NonNull GeyserItemStack stack, @NonNull Equippable equippable) {
        EquipmentSlot slot = equippable.slot();
        if (!canPlayerUseSlot(slot) || !canBeEquippedByPlayer(player, equippable)) {
            return InteractionResult.PASS;
        }

        GeyserItemStack inSlot = player.itemBySlot(slot);
        boolean creative = player.gameMode() == GameMode.CREATIVE;
        if ((creative || !UseEnchantments.has(player, inSlot, EnchantmentComponent.PREVENT_ARMOR_CHANGE))
            && !isSameItemSameComponents(stack, inSlot)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    /**
     * Mirrors {@code Player#canUseSlot}: players have no body armor slot.
     * <p>
     * Approximation: the 26.2 source of {@code Player#canUseSlot} was not available; whether players can use the
     * saddle slot is not known. Vanilla saddle-slot items only allow saddleable mobs, so this only matters for
     * custom equippable items without allowed entities.
     */
    private static boolean canPlayerUseSlot(@NonNull EquipmentSlot slot) {
        return slot != EquipmentSlot.BODY;
    }

    /**
     * Mirrors {@code Equippable#canBeEquippedBy} for the player entity type.
     */
    private static boolean canBeEquippedByPlayer(@NonNull InteractionContext player, @NonNull Equippable equippable) {
        return equippable.allowedEntities() == null
            || player.isInHolderSet(equippable.allowedEntities(), JavaRegistries.ENTITY_TYPE, PLAYER_ENTITY_TYPE);
    }

    /**
     * Mirrors {@code ItemStack#isSameItemSameComponents}. Compares the component patches sent by the server; the server
     * never sends patch entries equal to the base components, so this is equivalent to comparing all components.
     */
    static boolean isSameItemSameComponents(@NonNull GeyserItemStack first, @NonNull GeyserItemStack second) {
        if (first.getJavaId() != second.getJavaId()) {
            return false;
        }
        if (first.isEmpty() && second.isEmpty()) {
            return true;
        }
        return Objects.equals(patchOrNull(first.getComponents()), patchOrNull(second.getComponents()));
    }

    private static @Nullable DataComponents patchOrNull(@Nullable DataComponents components) {
        return components == null || components.getDataComponents().isEmpty() ? null : components;
    }

    private EquipmentInteractions() {
    }
}
