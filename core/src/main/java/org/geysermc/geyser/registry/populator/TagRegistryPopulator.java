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

package org.geysermc.geyser.registry.populator;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.geyser.registry.BedrockPalettes;
import org.geysermc.geyser.registry.Registries;
import org.geysermc.geyser.registry.java.BuiltInJavaRegistries;
import org.geysermc.geyser.registry.type.ItemMapping;
import org.geysermc.geyser.registry.type.ItemMappings;
import org.geysermc.geyser.util.MinecraftKey;
import org.geysermc.paletter.Palette;
import org.geysermc.paletter.data.DataVersion;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public final class TagRegistryPopulator {
    public static void populate() {
        Hash.Strategy<int[]> hashStrategy = new Hash.Strategy<>() {
            // Necessary so arrays can actually be compared
            @Override
            public int hashCode(int[] o) {
                return Arrays.hashCode(o);
            }

            @Override
            public boolean equals(int[] a, int[] b) {
                return Arrays.equals(a, b);
            }
        };

        ItemMappings mappings = Registries.ITEMS.get();
        for (DataVersion version : BedrockPalettes.versions()) {
            Palette bedrockPalette = BedrockPalettes.palette(version.protocolVersion());
            // The item tags of the vanilla Bedrock version, by tag. Not the layered palette: that would leak the tags
            // of custom items (e.g. geyser:is_custom) into the lookup.
            Map<String, List<String>> bedrockTags = BedrockPalettes.paletter().vanilla(version).items().tags();

            var javaItemsToBedrockTag = new Object2ObjectOpenCustomHashMap<int[], String>(hashStrategy);

            // Some tags have identical members (e.g. boat/boats); the last one put wins, so go alphabetically
            // to keep the winner deterministic.
            for (var entry : new TreeMap<>(bedrockTags).entrySet()) {
                List<String> value = entry.getValue();
                if (value.isEmpty() || value.size() == 1) {
                    // For our usecase, we don't need this. Empty values are worthless; one value can just be a reference
                    // to the item itself, instead of the tag.
                    continue;
                }

                // In some cases, the int list will need to be minimized
                IntList javaNetworkIds = new IntArrayList(value.size());
                for (int i = 0; i < value.size(); i++) {
                    String bedrockIdentifier = value.get(i);
                    Optional<Item> javaItem = BuiltInJavaRegistries.ITEM.get(MinecraftKey.key(bedrockIdentifier));
                    if (javaItem.isEmpty()) {
                        // Time to search the long way around.
                        for (ItemMapping mapping : mappings.getItems()) {
                            if (mapping.identifier(bedrockPalette).equals(bedrockIdentifier)) {
                                javaItem = Optional.of(mapping.getJavaItem());
                                break;
                            }
                        }
                    }
                    if (javaItem.isEmpty()) {
                        // Triggers for Bedrock-only spawn eggs. We don't care.
                        continue;
                    }

                    javaNetworkIds.add(javaItem.get().javaId());
                }

                int[] javaNetworkIdArray = javaNetworkIds.toIntArray();
                // Sort IDs so equality checks just have to match if each is equal and not necessarily an order difference.
                Arrays.sort(javaNetworkIdArray);

                javaItemsToBedrockTag.put(javaNetworkIdArray, entry.getKey());
            }

            javaItemsToBedrockTag.trim();
            Registries.TAGS.register(version.protocolVersion(), javaItemsToBedrockTag);
        }
    }
}
