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

package org.geysermc.geyser.item.type;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponents;
import org.geysermc.paletter.vanilla.BedrockItemVariant;
import org.geysermc.paletter.vanilla.BedrockItems;

import java.util.ArrayList;
import java.util.List;

public class OminousBottleItem extends Item {
    /**
     * The highest amplifier; Bedrock has a damage value per amplifier, Java a component
     */
    private static final int MAX_AMPLIFIER = 4;

    public OminousBottleItem(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    /**
     * Amplifier 0 is the item's own variant
     */
    @Override
    public @NonNull List<BedrockVariant> bedrockVariants() {
        List<BedrockVariant> variants = new ArrayList<>();
        for (int amplifier = 1; amplifier <= MAX_AMPLIFIER; amplifier++) {
            int value = amplifier;
            variants.add(new BedrockVariant(BedrockItems.OMINOUS_BOTTLE.variantOf(amplifier),
                components -> components.put(DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER, value)));
        }
        return variants;
    }

    @Override
    public @Nullable BedrockItemVariant bedrockVariant(@Nullable DataComponents components) {
        // Null components are amplifier 0 (Java 1.21)
        Integer amplifier = components == null ? null : components.get(DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER);
        if (amplifier == null || amplifier < 1 || amplifier > MAX_AMPLIFIER) {
            return null;
        }
        return BedrockItems.OMINOUS_BOTTLE.variantOf(amplifier);
    }
}
