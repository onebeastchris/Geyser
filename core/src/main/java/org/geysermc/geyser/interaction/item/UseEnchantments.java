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
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.item.enchantment.Enchantment;
import org.geysermc.geyser.item.enchantment.EnchantmentComponent;
import org.geysermc.geyser.session.cache.registry.JavaRegistries;
import org.geysermc.geyser.session.cache.registry.JavaRegistry;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.ItemEnchantments;

/**
 * Enchantment effect lookups for item interactions.
 */
final class UseEnchantments {

    /**
     * Mirrors {@code EnchantmentHelper#has}: whether any enchantment on the stack (not stored enchantments) has the
     * given effect. Effects are only tracked by presence, see {@link EnchantmentComponent}.
     */
    static boolean has(@NonNull InteractionContext player, @NonNull GeyserItemStack stack, @NonNull EnchantmentComponent effect) {
        if (stack.isEmpty()) {
            return false;
        }
        ItemEnchantments enchantments = stack.getComponent(DataComponentTypes.ENCHANTMENTS);
        if (enchantments == null || enchantments.getEnchantments().isEmpty()) {
            return false;
        }
        JavaRegistry<Enchantment> registry = player.registries().registry(JavaRegistries.ENCHANTMENT);
        for (int id : enchantments.getEnchantments().keySet()) {
            Enchantment enchantment = registry.byId(id);
            if (enchantment != null && enchantment.effects().contains(effect)) {
                return true;
            }
        }
        return false;
    }

    private UseEnchantments() {
    }
}
