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

package org.geysermc.geyser.gametest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import org.geysermc.geyser.gametest.interaction.InteractionClassData;
import org.geysermc.geyser.gametest.tests.BlockInteractionTestInstance;
import org.geysermc.geyser.gametest.tests.EntityMetadataTest;
import org.geysermc.geyser.gametest.tests.ItemInteractionTestInstance;
import org.geysermc.geyser.gametest.tests.PlacementTestInstance;
import org.geysermc.geyser.gametest.tests.ResolvableComponentLoadingTestInstance;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final  class GeyserGameTests {
    private static final List<EntityType<?>> UNSUPPORTED_ENTITY_TYPES = List.of(EntityTypes.BLOCK_DISPLAY, EntityTypes.ITEM_DISPLAY, EntityTypes.MARKER);
    /**
     * Items whose interactions are driven by data components only (they use the default {@code Item#use}), tested in groups.
     */
    private static final Map<String, List<String>> COMPONENT_DRIVEN_ITEMS = Map.of(
        "food", List.of("bread", "apple", "golden_apple", "enchanted_golden_apple", "chorus_fruit", "cooked_beef", "rotten_flesh",
            "dried_kelp", "cookie", "honey_bottle", "milk_bucket", "suspicious_stew", "ominous_bottle"),
        "equippable", List.of("leather_helmet", "iron_chestplate", "diamond_leggings", "netherite_boots", "turtle_helmet", "elytra",
            "carved_pumpkin", "player_head", "wolf_armor", "saddle", "leather_horse_armor"),
        "blocks_attacks", List.of("shield"),
        "kinetic_weapon", List.of("iron_spear", "netherite_spear"),
        "none", List.of("totem_of_undying", "diamond", "mace", "nether_star")
    );

    /**
     * The placement tests are split by the first letter of the item identifier to keep each test reasonably small.
     */
    private static final List<String> PLACEMENT_LETTER_GROUPS = List.of("ab", "c", "defg", "hijklm", "nopqr", "s", "tuvwxyz");

    private GeyserGameTests() {}

    private static ResourceKey<GameTestInstance> createKey(String name) {
        return ResourceKey.create(Registries.TEST_INSTANCE, Identifier.fromNamespaceAndPath("geyser", name));
    }

    private static ResourceKey<GameTestInstance> createKey(Identifier testType, String name) {
        return createKey(testType.getPath() + "/" + name);
    }

    private static ResourceKey<GameTestInstance> createSingletonKey(Identifier testType) {
        return createKey(testType.getPath());
    }

    private static void registerSingletonTest(HolderGetter<TestEnvironmentDefinition<?>> testEnvironments, FabricDynamicRegistryProvider.Entries entries, GeyserGameTestTypes.SingletonTestType type, boolean required) {
        entries.add(createSingletonKey(type.type()), type.constructor().create(testEnvironments, required));
    }

    private static void registerSingletonTest(HolderGetter<TestEnvironmentDefinition<?>> testEnvironments, FabricDynamicRegistryProvider.Entries entries, GeyserGameTestTypes.SingletonTestType type) {
        registerSingletonTest(testEnvironments, entries, type, true);
    }

    private static void registerEntityTypeTests(HolderGetter<TestEnvironmentDefinition<?>> testEnvironments, FabricDynamicRegistryProvider.Entries entries) {
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            entries.add(createKey(GeyserGameTestTypes.ENTITY_METADATA, BuiltInRegistries.ENTITY_TYPE.getKey(entityType).getPath()),
                new EntityMetadataTest(testEnvironments, !UNSUPPORTED_ENTITY_TYPES.contains(entityType), entityType));
        }
    }

    private static void registerResolvableComponentLoadingTests(HolderGetter<TestEnvironmentDefinition<?>> testEnvironments, GeyserGameTestPlatform platform, FabricDynamicRegistryProvider.Entries entries) {
        for (Holder<Item> item : findItemsWithResolvableComponents(platform)) {
            entries.add(createKey(GeyserGameTestTypes.RESOLVABLE_COMPONENTS, item.unwrapKey().orElseThrow().identifier().getPath()),
                new ResolvableComponentLoadingTestInstance(testEnvironments, true, item));
        }
    }

    private static void registerInteractionTests(HolderGetter<TestEnvironmentDefinition<?>> testEnvironments, FabricDynamicRegistryProvider.Entries entries) {
        registerSingletonTest(testEnvironments, entries, GeyserGameTestTypes.INTERACTION_CLASSES);
        registerSingletonTest(testEnvironments, entries, GeyserGameTestTypes.INTERACTION_TABLES);

        for (String vanillaClass : InteractionClassData.blockClasses()) {
            entries.add(createKey(GeyserGameTestTypes.INTERACTION_BLOCK, InteractionClassData.snakeCase(vanillaClass)),
                new BlockInteractionTestInstance(testEnvironments, true, vanillaClass));
        }
        for (String vanillaClass : InteractionClassData.itemClasses()) {
            if (InteractionClassData.itemsOf(vanillaClass).isEmpty()) {
                continue;
            }
            entries.add(createKey(GeyserGameTestTypes.INTERACTION_ITEM, InteractionClassData.snakeCase(vanillaClass)),
                new ItemInteractionTestInstance(testEnvironments, true, Optional.of(vanillaClass), List.of()));
        }
        for (String letters : PLACEMENT_LETTER_GROUPS) {
            entries.add(createKey(GeyserGameTestTypes.INTERACTION_PLACEMENT, letters), new PlacementTestInstance(testEnvironments, true, letters));
        }
        registerSingletonTest(testEnvironments, entries, GeyserGameTestTypes.BLOCK_STATE_WITH_VALUE);
        registerSingletonTest(testEnvironments, entries, GeyserGameTestTypes.ENCHANTMENT_CONVERSION);
        COMPONENT_DRIVEN_ITEMS.forEach((name, ids) -> entries.add(createKey(GeyserGameTestTypes.INTERACTION_ITEM, "component_" + name),
            new ItemInteractionTestInstance(testEnvironments, true, Optional.empty(), ids.stream()
                .map(id -> BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(id)))
                .toList())));
    }

    public static void bootstrap(HolderLookup.Provider registries, FabricDynamicRegistryProvider.Entries entries) {
        GeyserGameTestPlatform platform = new GeyserGameTestPlatform();
        HolderGetter<TestEnvironmentDefinition<?>> testEnvironments = registries.lookupOrThrow(Registries.TEST_ENVIRONMENT);

        registerEntityTypeTests(testEnvironments, entries);
        registerSingletonTest(testEnvironments, entries, GeyserGameTestTypes.REQUIRED_COMPONENTS_FOR_HASHING);
        registerSingletonTest(testEnvironments, entries, GeyserGameTestTypes.MINECRAFT_VERSION);
        registerSingletonTest(testEnvironments, entries, GeyserGameTestTypes.ENCHANTMENT_EFFECTS);
        registerResolvableComponentLoadingTests(testEnvironments, platform, entries);
        registerInteractionTests(testEnvironments, entries);
    }

    private static List<Holder<Item>> findItemsWithResolvableComponents(GeyserGameTestPlatform platform) {
        try (InputStream stream = platform.resolveResource("mappings/resolvable_item_data_components.json")) {
            assert stream != null;
            JsonElement rootElement = JsonParser.parseReader(new InputStreamReader(stream));
            JsonArray items = rootElement.getAsJsonObject().get("value").getAsJsonArray();

            List<Holder<Item>> itemsWithResolvableComponents = new ArrayList<>();

            for (int i = 0; i < items.size(); i++) {
                JsonElement item = items.get(i);
                JsonArray itemComponentArray = item.getAsJsonArray();
                if (!itemComponentArray.isEmpty()) {
                    itemsWithResolvableComponents.add(Objects.requireNonNull(BuiltInRegistries.ITEM.asHolderIdMap().byId(i)));
                }
            }

            return itemsWithResolvableComponents;
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
