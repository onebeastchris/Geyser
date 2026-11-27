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

package org.geysermc.geyser.item.type;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.scoreboard.network.util.GeyserMockContext;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.BlockStateProperties;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponents;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.LodestoneTracker;
import dev.opencollab.paletter.vanilla.BedrockItemVariant;
import dev.opencollab.paletter.vanilla.BedrockItems;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Bedrock variants items send stacks as ({@link Item#bedrockVariant(DataComponents)}) are declared
 * ({@link Item#bedrockVariants()}) and map back to the same item and components.
 */
class BedrockVariantsTest {

    @Test
    void declaredVariantsRoundTrip() {
        // Loading the items needs a Geyser instance
        GeyserMockContext.mockContext(BedrockVariantsTest::declaredVariantsRoundTripCheck);
    }

    private static void declaredVariantsRoundTripCheck() {
        Map<BedrockItemVariant, Item> ownVariants = new Object2ObjectOpenHashMap<>();
        for (Item item : items()) {
            if (item.bedrockType() != null) {
                ownVariants.putIfAbsent(item.bedrockType().variant(item.bedrockData()), item);
            }
        }

        // What the item mappings map, not only the variants the data names
        Map<BedrockItemVariant, Item> variantOwners = new Object2ObjectOpenHashMap<>();
        for (Item item : items()) {
            for (BedrockItemVariant variant : item.bedrockVariants()) {
                assertNull(variantOwners.put(variant, item), variant + " is sent by two items");
                assertNull(ownVariants.get(variant), variant + " is also the own variant of an item");

                DataComponents components = new DataComponents(new HashMap<>());
                item.toJava(variant, components);
                assertEquals(variant, item.bedrockVariant(components), "restored " + variant + " of " + item.javaKey() + " is sent as another variant");
            }
        }
        assertFalse(variantOwners.isEmpty());
    }

    @Test
    void everySentVariantIsDeclared() {
        // Loading the items needs a Geyser instance
        GeyserMockContext.mockContext(BedrockVariantsTest::everySentVariantIsDeclaredCheck);
    }

    private static void everySentVariantIsDeclaredCheck() {
        for (int level = 0; level <= 15; level++) {
            DataComponents components = new DataComponents(new HashMap<>());
            components.put(DataComponentTypes.BLOCK_STATE, new BlockStateProperties(Map.of(Properties.LEVEL.name(), String.valueOf(level))));
            assertDeclared(Items.LIGHT, components);
        }
        for (int amplifier = 0; amplifier <= 4; amplifier++) {
            DataComponents components = new DataComponents(new HashMap<>());
            components.put(DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER, amplifier);
            assertDeclared(Items.OMINOUS_BOTTLE, components);
        }
        DataComponents tracker = new DataComponents(new HashMap<>());
        tracker.put(DataComponentTypes.LODESTONE_TRACKER, new LodestoneTracker(null, true));
        assertDeclared(Items.COMPASS, tracker);
    }

    private static void assertDeclared(Item item, DataComponents components) {
        BedrockItemVariant variant = item.bedrockVariant(components);
        if (variant != null) {
            assertTrue(item.bedrockVariants().contains(variant), item.javaKey() + " sends " + variant + " without declaring it");
        }
    }

    @Test
    void stacksWithoutComponentsUseTheDefault() {
        // Loading the items needs a Geyser instance
        GeyserMockContext.mockContext(BedrockVariantsTest::stacksWithoutComponentsUseTheDefaultCheck);
    }

    private static void stacksWithoutComponentsUseTheDefaultCheck() {
        assertNull(Items.COMPASS.bedrockVariant(null));
        assertNull(Items.OMINOUS_BOTTLE.bedrockVariant(null));
        assertEquals(BedrockItems.LIGHT_BLOCK_15.base(), Items.LIGHT.bedrockVariant(null));
    }

    @Test
    void explorerMapsHaveTheirOwnVariant() {
        // Loading the items needs a Geyser instance
        GeyserMockContext.mockContext(BedrockVariantsTest::explorerMapsHaveTheirOwnVariantCheck);
    }

    private static void explorerMapsHaveTheirOwnVariantCheck() {
        Map<BedrockItemVariant, Item> maps = new Object2ObjectOpenHashMap<>();
        for (Item item : items()) {
            if (item instanceof MapItem && item.bedrockType() != null) {
                BedrockItemVariant variant = item.bedrockType().variant(item.bedrockData());
                assertNull(maps.put(variant, item), "two maps are sent as " + variant);
            }
        }
    }

    /**
     * The vanilla items, from the constants of {@link Items} (the item registry is only iterable once frozen)
     */
    private static List<Item> items() {
        List<Item> items = new ArrayList<>();
        for (Field field : Items.class.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) && Item.class.isAssignableFrom(field.getType())) {
                try {
                    items.add((Item) field.get(null));
                } catch (IllegalAccessException e) {
                    throw new AssertionError(e);
                }
            }
        }
        return items;
    }
}
