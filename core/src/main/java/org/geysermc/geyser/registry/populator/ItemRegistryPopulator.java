/*
 * Copyright (c) 2019-2022 GeyserMC. http://geysermc.org
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

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SortedSetMultimap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.nbt.NbtMap;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.api.block.custom.CustomBlockData;
import org.geysermc.geyser.api.block.custom.NonVanillaCustomBlockData;
import org.geysermc.geyser.api.item.custom.v2.CustomItemBedrockOptions;
import org.geysermc.geyser.api.item.custom.v2.CustomItemDefinition;
import org.geysermc.geyser.api.item.custom.v2.NonVanillaCustomItemDefinition;
import org.geysermc.geyser.api.item.custom.v2.component.geyser.GeyserBlockPlacer;
import org.geysermc.geyser.api.item.custom.v2.component.geyser.GeyserItemDataComponents;
import org.geysermc.geyser.api.predicate.MinecraftPredicate;
import org.geysermc.geyser.api.predicate.context.item.ItemPredicateContext;
import org.geysermc.geyser.api.util.CreativeCategory;
import org.geysermc.geyser.api.util.Identifier;
import org.geysermc.geyser.item.GeyserCustomMappingData;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.item.TooltipOptions;
import org.geysermc.geyser.item.custom.GeyserCustomItemDefinition;
import org.geysermc.geyser.item.custom.impl.predicates.GeyserRangeDispatchPredicate;
import org.geysermc.geyser.item.exception.InvalidItemComponentsException;
import org.geysermc.geyser.item.type.BlockItem;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.registry.BedrockPalettes;
import org.geysermc.geyser.registry.BlockRegistries;
import org.geysermc.geyser.registry.Registries;
import org.geysermc.geyser.registry.java.BuiltInJavaRegistries;
import org.geysermc.geyser.registry.mappings.BuiltInMappings;
import org.geysermc.geyser.registry.type.ItemMapping;
import org.geysermc.geyser.registry.type.ItemMappings;
import org.geysermc.geyser.registry.type.NonVanillaItemRegistration;
import org.geysermc.geyser.translator.item.BedrockItemBuilder;
import org.geysermc.geyser.util.MinecraftKey;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponents;
import dev.opencollab.paletter.Lookup;
import dev.opencollab.paletter.Palette;
import dev.opencollab.paletter.PaletteException;
import dev.opencollab.paletter.PaletteLayer;
import dev.opencollab.paletter.Paletter;
import dev.opencollab.paletter.creative.ItemMatcher;
import dev.opencollab.paletter.data.DataVersion;
import dev.opencollab.paletter.item.CustomItem;
import dev.opencollab.paletter.item.component.AllowOffHand;
import dev.opencollab.paletter.item.component.ItemDisplayName;
import dev.opencollab.paletter.item.component.Icon;
import dev.opencollab.paletter.item.component.ItemComponent;
import dev.opencollab.paletter.item.component.MaxStackSize;
import dev.opencollab.paletter.item.component.Projectile;
import dev.opencollab.paletter.item.component.ThrowableItem;
import dev.opencollab.paletter.vanilla.BedrockBlockState;
import dev.opencollab.paletter.vanilla.BedrockItemType;
import dev.opencollab.paletter.vanilla.BedrockItemVariant;
import dev.opencollab.paletter.vanilla.BedrockItems;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Populates the item registries.
 */
public class ItemRegistryPopulator {

    static final String ITEM_LAYER = "geyser:items";

    public static void populate() {
        // Java Edition to Bedrock Edition item mappings, from the generated Items class
        List<Item> items = vanillaItems();

        // Custom items are defined once the block registries are set (as GeyserDefineCustomItemsEvent always was)
        Multimap<Identifier, CustomItemDefinition> customItems = MultimapBuilder.hashKeys().arrayListValues().build();
        Multimap<Identifier, NonVanillaCustomItemDefinition> nonVanillaCustomItems = MultimapBuilder.hashKeys().arrayListValues().build();
        Map<CustomItemDefinition, String> sources = CustomItemRegistryPopulator.populate(items, customItems, nonVanillaCustomItems);
        Map<CustomItemDefinition, CustomItem> layerItems = customLayerItems(items, customItems, nonVanillaCustomItems, sources);

        // All layers are built first, so every version compiles one palette
        Paletter paletter = buildPaletter(items, layerItems, sources);
        Map<CustomItemDefinition, BedrockItemType> customItemTypes = new Object2ObjectOpenHashMap<>();
        layerItems.forEach((definition, item) -> customItemTypes.put(definition, item.type()));

        // What each Java item is sent as does not depend on the version: the keys are the newest version's. What a
        // version sends for a key is the palette's (session.palette()), so nothing is stored per version.
        Palette newest = paletter.palette(paletter.latest());
        Map<Item, ItemKeys> itemKeys = vanillaItemKeys(items, customItems, newest);
        Set<BedrockItemType> bucketTypes = new ObjectOpenHashSet<>();
        for (Map.Entry<Item, ItemKeys> entry : itemKeys.entrySet()) {
            String key = entry.getKey().javaKey().asString();
            if (key.contains("bucket") && !key.contains("milk")) {
                bucketTypes.add(entry.getValue().variant().type());
            }
        }
        Object2ObjectMap<CustomBlockData, BedrockItemType> customBlockItems = customBlockItemTypes(itemKeys, newest);
        // The keys of non-vanilla custom items, by Java item
        Map<Item, ItemKeys> nonVanillaKeys = new Object2ObjectOpenHashMap<>();

        List<ItemMapping> mappings = new ObjectArrayList<>(BuiltInJavaRegistries.ITEM.size());
        while (BuiltInJavaRegistries.ITEM.size() >= mappings.size()) {
            mappings.add(ItemMapping.AIR);
        }

        for (Item javaItem : items) {
            ItemKeys keys = itemKeys.get(javaItem);

            // Always a variant of the Bedrock item (validated by the mappings generator against Paletter's data)
            ItemMapping.ItemMappingBuilder mappingBuilder = ItemMapping.builder()
                    .javaItem(javaItem)
                    .bedrockVariant(keys.variant())
                    .bedrockBlockState(keys.block())
                    .javaOnly(keys.javaOnly());

            // Add the custom item properties, if applicable
            boolean containsOldMappings = false;
            SortedSetMultimap<Key, GeyserCustomMappingData> customItemDefinitions;
            Collection<CustomItemDefinition> customItemsToLoad = customItems.get(MinecraftKey.keyToIdentifier(javaItem.javaKey()));
            if (!customItemsToLoad.isEmpty()) {
                customItemDefinitions = MultimapBuilder.hashKeys(customItemsToLoad.size()).treeSetValues(new CustomItemDefinitionComparator()).build();

                for (CustomItemDefinition customItem : customItemsToLoad) {
                    BedrockItemType customType = customItemTypes.get(customItem);
                    if (customType == null) {
                        // Rejected (and logged) while building the item layer
                        continue;
                    }
                    GeyserCustomMappingData customMapping = new GeyserCustomMappingData(customItem, customType);

                    // See CustomItemTranslator for a full reason; but essentially, some servers have v1 mappings AND send item_models
                    // Converted v1 mappings assume a vanilla model is used... tadaa
                    if (customItem instanceof GeyserCustomItemDefinition customItemDefinition && customItemDefinition.isOldConvertedItem()) {
                        containsOldMappings = true;
                    }

                    // ComponentItemData - used to register some custom properties
                    customItemDefinitions.put(MinecraftKey.identifierToKey(customItem.model()), customMapping);
                }
            } else {
                customItemDefinitions = null;
            }
            mappingBuilder.customItemDefinitions(customItemDefinitions);
            mappingBuilder.containsV1Mappings(containsOldMappings);

            ItemMapping mapping = mappingBuilder.build();

            mappings.set(javaItem.javaId(), mapping);
        }

        final IntSet nonVanillaCustomItemIds = new IntOpenHashSet();

        // Register any completely custom items given to us
        IntSet registeredJavaIds = new IntOpenHashSet(); // Used to check for duplicate item java ids
        for (NonVanillaCustomItemDefinition customItem : nonVanillaCustomItems.values()) {
            if (!registeredJavaIds.add(customItem.javaId())) {
                // This should never happen since we validate for this in the CustomItemRegistryPopulator
                throw new IllegalStateException("Custom item java id " + customItem.javaId() + " already exists and was registered again!");
            }

            BedrockItemType customType = customItemTypes.get(customItem);
            if (customType == null) {
                // Rejected (and logged) while building the item layer
                continue;
            }
            try {
                NonVanillaItemRegistration registration = CustomItemRegistryPopulator.registerCustomItem(customItem);

                ItemMapping mapping = ItemMapping.builder()
                    .translationString(registration.translationString())
                    .javaItem(registration.javaItem())
                    .bedrockVariant(customType.base())
                    .build();
                Item javaItem = registration.javaItem();
                nonVanillaKeys.putIfAbsent(javaItem, new ItemKeys(mapping.getBedrockVariant(), null, false));
                while (javaItem.javaId() >= mappings.size()) {
                    // Fill with empty to get to the correct size
                    mappings.add(ItemMapping.AIR);
                }
                mappings.set(javaItem.javaId(), mapping);

                nonVanillaCustomItemIds.add(javaItem.javaId());

            } catch (InvalidItemComponentsException exception) {
                GeyserImpl.getInstance().getLogger().error("Not registering non-vanilla custom item (identifier=" + customItem.identifier() + ")!", exception);
            }
        }

        Map<ItemMappings.ItemKey, Item> bedrockToJava = reverseMappings(mappings, itemKeys, nonVanillaKeys, newest);
        Registries.ITEMS.set(ItemMappings.builder()
            .items(mappings.toArray(new ItemMapping[0]))
            .bedrockToJava(bedrockToJava)
            .variantMappings(variantMappings(newest))
            .buckets(bucketTypes)
            .customBlockItems(customBlockItems)
            .nonVanillaCustomItemIds(nonVanillaCustomItemIds)
            .build());
    }

    /**
     * What a Java item is sent as. Version independent: the newest version's keys.
     *
     * @param variant the Bedrock item variant
     * @param block the block state of a block item
     * @param javaOnly whether the item has no Bedrock counterpart (stand-ins excluded)
     */
    private record ItemKeys(BedrockItemVariant variant, @Nullable BedrockBlockState block, boolean javaOnly) {
    }

    private static Map<Item, ItemKeys> vanillaItemKeys(List<Item> items, Multimap<Identifier, CustomItemDefinition> customItems, Palette newest) {
        // The test blocks are java-only only when both are off, as before Paletter
        boolean customBlocks = GeyserImpl.getInstance().config().gameplay().enableCustomContent()
            || GeyserImpl.getInstance().config().gameplay().enableIntegratedPack();
        Map<Item, ItemKeys> keys = new Object2ObjectLinkedOpenHashMap<>();
        for (Item javaItem : items) {
            BedrockItemType type = javaItem.bedrockType();
            BedrockItemVariant variant = type.variant(javaItem.bedrockData());

            // Items without a Bedrock counterpart, unless Geyser registers a custom item or block for them
            boolean javaOnly = javaItem.javaOnly()
                && !(javaItem instanceof BlockItem ? customBlocks : customItems.containsKey(MinecraftKey.keyToIdentifier(javaItem.javaKey())));

            BedrockBlockState block = null;
            if (javaItem instanceof BlockItem) {
                // Which block a block item is sent with is Paletter's decision; structure blocks and voids are sent
                // without one, or they cannot be used in recipes: https://github.com/GeyserMC/Geyser/issues/2890
                boolean noBlock = type == BedrockItems.STRUCTURE_BLOCK || type == BedrockItems.STRUCTURE_VOID;
                block = noBlock ? null : newest.blockOf(variant);

                // Custom blocks replacing the block of this item take over the item too
                CustomBlockData customBlockData = BlockRegistries.CUSTOM_BLOCK_ITEM_OVERRIDES.getOrDefault(javaItem.javaKey().asString(), null);
                if (customBlockData != null && customBlockData.includedInCreativeInventory()) {
                    BedrockBlockState customBlock = BlockRegistries.CUSTOM_BLOCK_STATE_KEYS.get().get(customBlockData.defaultBlockState());
                    if (customBlock != null) {
                        block = customBlock;
                        BedrockItemType customType = BlockRegistries.CUSTOM_PALETTER_BLOCKS.get(customBlockData).itemType();
                        if (newest.itemDefinition(customType.identifier()) != null) {
                            // The custom block takes over the item, registered in the item layer
                            keys.put(javaItem, new ItemKeys(customType.base(), customBlock, javaOnly));
                            continue;
                        }
                    }
                }
            }
            keys.put(javaItem, new ItemKeys(variant, block, javaOnly));
        }
        return keys;
    }

    private static Object2ObjectMap<CustomBlockData, BedrockItemType> customBlockItemTypes(Map<Item, ItemKeys> itemKeys, Palette newest) {
        Object2ObjectMap<CustomBlockData, BedrockItemType> types = new Object2ObjectOpenHashMap<>();
        for (Map.Entry<Item, ItemKeys> entry : itemKeys.entrySet()) {
            if (entry.getKey() instanceof BlockItem) {
                CustomBlockData customBlockData = BlockRegistries.CUSTOM_BLOCK_ITEM_OVERRIDES.getOrDefault(entry.getKey().javaKey().asString(), null);
                BedrockItemType type = entry.getValue().variant().type();
                if (customBlockData != null && customBlockData.identifier().equals(type.identifier())) {
                    types.put(customBlockData, type);
                }
            }
        }
        BlockRegistries.CUSTOM_PALETTER_BLOCKS.get().forEach((customBlock, paletterBlock) -> {
            // We might've registered it already with the vanilla blocks so check first
            // Non-vanilla custom blocks will be handled in the item registry
            if (types.containsKey(customBlock) || customBlock instanceof NonVanillaCustomBlockData) {
                return;
            }
            BedrockItemType type = paletterBlock.itemType();
            if (newest.itemDefinition(type.identifier()) != null) {
                types.put(customBlock, type);
            }
        });
        return types;
    }

    /**
     * Bedrock to Java in key space: what a decoded stack is (item variant, and block when it is not
     * the item's default one) to Java item. Client stacks are identified with {@link Palette#decode}, which returns
     * the newest keys.
     */
    private static Map<ItemMappings.ItemKey, Item> reverseMappings(List<ItemMapping> mappings, Map<Item, ItemKeys> vanillaKeys,
                                                                   Map<Item, ItemKeys> nonVanillaKeys, Palette newest) {
        Map<Item, ItemKeys> itemKeys = new Object2ObjectOpenHashMap<>(vanillaKeys);
        itemKeys.putAll(nonVanillaKeys);

        Map<ItemMappings.ItemKey, Item> bedrockToJava = new Object2ObjectLinkedOpenHashMap<>();
        for (ItemMapping mapping : mappings) {
            Item javaItem = mapping.getJavaItem();
            ItemKeys keys = itemKeys.get(javaItem);
            if (mapping == ItemMapping.AIR || keys == null || keys.javaOnly()) {
                // From a Bedrock item data, we aren't getting one of these items
                continue;
            }
            // A Java item Geyser represents with a custom item claims that custom item instead (it would
            // otherwise claim the Bedrock item its mapping falls back to, e.g. furnace minecarts the hopper minecart)
            Identifier standIn = BuiltInMappings.standIn(javaItem);
            // The key the stand-in owns, if it is registered
            BedrockItemVariant standInVariant = standIn == null ? null : newest.itemVariant(standIn.toString(), 0, Lookup.EXACT);
            if (standInVariant != null) {
                claim(bedrockToJava, new ItemMappings.ItemKey(standInVariant, null), mapping);
                continue;
            }

            // Claim what a stack of this mapping is identified as; block items are sent with damage 0
            claim(bedrockToJava, new ItemMappings.ItemKey(keys.variant(), null), mapping);
        }

        // Stacks without a block (or of items that are no block items): the first mapping with this variant
        for (ItemMapping mapping : mappings) {
            ItemKeys keys = itemKeys.get(mapping.getJavaItem());
            if (mapping == ItemMapping.AIR || keys == null || keys.javaOnly() || BuiltInMappings.standIn(mapping.getJavaItem()) != null) {
                continue;
            }
            bedrockToJava.putIfAbsent(new ItemMappings.ItemKey(keys.variant(), null), mapping.getJavaItem());
        }

        // Items whose damage values vary on Bedrock (durability, stew effects) also claim their other variants
        for (Map.Entry<ItemMappings.ItemKey, Item> entry : new ArrayList<>(bedrockToJava.entrySet())) {
            Item javaItem = entry.getValue();
            ItemMappings.ItemKey key = entry.getKey();
            if (key.block() == null && key.variant().aux() == 0 && (javaItem.ignoreDamage() || javaItem == Items.SUSPICIOUS_STEW)) {
                for (BedrockItemVariant variant : key.variant().type().variants()) {
                    bedrockToJava.putIfAbsent(new ItemMappings.ItemKey(variant, null), javaItem);
                }
            }
        }

        return bedrockToJava;
    }

    /**
     * Lets a Java item mapping claim a Bedrock item key. Two Java items may be sent as the same Bedrock item (this can
     * be intended); Bedrock stacks of it are then the first one, in registration order.
     */
    private static void claim(Map<ItemMappings.ItemKey, Item> bedrockToJava, ItemMappings.ItemKey key, ItemMapping mapping) {
        Item owner = bedrockToJava.putIfAbsent(key, mapping.getJavaItem());
        if (owner != null && owner != mapping.getJavaItem()) {
            GeyserImpl.getInstance().getLogger().debug("Java items " + owner.javaKey() + " and " + mapping.getJavaItem().javaKey()
                + " are both sent as Bedrock " + key.variant() + " (block " + key.block() + "); Bedrock stacks of it are " + owner.javaKey());
        }
    }

    /**
     * The mapping of every Bedrock variant a Java item sends some of its stacks as ({@link Item#bedrockVariants()}).
     */
    private static Map<BedrockItemVariant, ItemMapping> variantMappings(Palette newest) {
        Map<BedrockItemVariant, ItemMapping> variantMappings = new Object2ObjectOpenHashMap<>();
        for (Item item : BuiltInJavaRegistries.ITEM) {
            for (BedrockItemVariant variant : item.bedrockVariants()) {
                ItemMapping mapping = ItemMapping.builder()
                    .javaItem(item)
                    .bedrockVariant(variant)
                    .bedrockBlockState(item instanceof BlockItem ? newest.blockOf(variant) : null)
                    .build();
                ItemMapping existing = variantMappings.put(variant, mapping);
                if (existing != null) {
                    throw new IllegalStateException("Java items " + existing.getJavaItem().javaKey() + " and " + item.javaKey()
                        + " both send stacks as the Bedrock variant " + variant);
                }
            }
        }
        return variantMappings;
    }

    /**
     * Every vanilla Java item, from the generated {@link Items} class (formerly items.json), in alphabetical order of their
     * Java identifier (the order of items.json, which the populators depend on).
     */
    private static List<Item> vanillaItems() {
        List<Item> items = new ArrayList<>();
        for (Item item : BuiltInJavaRegistries.ITEM) {
            if (item.bedrockType() == null) {
                throw new IllegalStateException("Vanilla Java item " + item.javaKey() + " has no Bedrock item");
            }
            if (!item.bedrockType().isAvailable()) {
                // A palette would send the default fallback for it
                throw new IllegalStateException("The Bedrock item of vanilla Java item " + item.javaKey() + ", " + item.bedrockType().identifier()
                    + ", is not in the Bedrock " + BedrockPalettes.newestBundledVersion() + " data! The Java item mappings must be regenerated against the bundled Paletter version.");
            }
            items.add(item);
        }
        items.sort(Comparator.comparing(item -> item.javaKey().asString()));
        return items;
    }

    /**
     * Translates every custom item into its Paletter form, in the order their runtime ids are assigned. Item NBT is
     * version independent, so this runs once; Paletter compiles the item layer for every version.
     *
     * @param sources the extension or mappings file each custom item comes from
     * @return the custom item definitions Paletter takes, with their Paletter items
     */
    private static Map<CustomItemDefinition, CustomItem> customLayerItems(List<Item> items,
                                                                         Multimap<Identifier, CustomItemDefinition> customItems,
                                                                         Multimap<Identifier, NonVanillaCustomItemDefinition> nonVanillaCustomItems,
                                                                         Map<CustomItemDefinition, String> sources) {
        Map<CustomItemDefinition, CustomItem> registered = new Object2ObjectLinkedOpenHashMap<>();
        Set<Identifier> registeredCustomItems = new ObjectOpenHashSet<>(); // This is used to check for duplicate item names
        for (Item javaItem : items) {
            for (CustomItemDefinition customItem : customItems.get(MinecraftKey.keyToIdentifier(javaItem.javaKey()))) {
                Identifier customItemIdentifier = customItem.bedrockIdentifier();
                if (!registeredCustomItems.add(customItemIdentifier)) {
                    GeyserImpl.getInstance().getLogger().error("Custom item '" + customItemIdentifier + "' already exists and was registered again! Skipping...");
                    continue;
                }

                try {
                    List<ItemComponent> components = CustomItemRegistryPopulator.registerCustomItem(javaItem, customItem, true);
                    CustomItem paletterItem = CustomItemRegistryPopulator.menuCategory(CustomItem.builder(customItemIdentifier.toString()).components(components),
                        customItem.bedrockOptions().creativeCategory(), customItem.bedrockOptions().creativeGroup()).build();
                    registered.put(customItem, paletterItem);
                } catch (InvalidItemComponentsException exception) {
                    GeyserImpl.getInstance().getLogger().error("Not registering custom item (bedrock identifier=" + customItem.bedrockIdentifier() + ")!", exception);
                } catch (IllegalArgumentException exception) {
                    CustomContentSources.warnRejected("item", customItemIdentifier.toString(), sources.get(customItem), exception);
                }
            }
        }

        for (NonVanillaCustomItemDefinition customItem : nonVanillaCustomItems.values()) {
            try {
                List<ItemComponent> components = CustomItemRegistryPopulator.nonVanillaItemComponents(customItem);
                CustomItem paletterItem = CustomItemRegistryPopulator.menuCategory(CustomItem.builder(customItem.bedrockIdentifier().toString()).components(components),
                    customItem.bedrockOptions().creativeCategory(), customItem.bedrockOptions().creativeGroup()).build();
                registered.put(customItem, paletterItem);
            } catch (InvalidItemComponentsException exception) {
                GeyserImpl.getInstance().getLogger().error("Not registering non-vanilla custom item (identifier=" + customItem.identifier() + ")!", exception);
            } catch (IllegalArgumentException exception) {
                CustomContentSources.warnRejected("item", customItem.bedrockIdentifier().toString(), sources.get(customItem), exception);
            }
        }
        return registered;
    }

    /**
     * Builds Geyser's item layer: vanilla item overrides, creative edits and the custom items.
     */
    private static PaletteLayer itemLayer(List<Item> items, Collection<CustomItem> customItems) {
        PaletteLayer.Builder layer = PaletteLayer.builder(ITEM_LAYER);

        // Some item on Bedrock Edition have a different stack size, so we're changing that through the component.
        // This resolve https://github.com/GeyserMC/Geyser/issues/5612 and https://github.com/GeyserMC/Geyser/issues/4905
        layer.overrideItem(BedrockItems.CAKE, item -> item.modifyComponents(dataDrivenOverride(1, null, null, false).toArray(ItemComponent[]::new)));
        // You have to change the item version to data driven for armor stand else this won't work.
        layer.overrideItem(BedrockItems.ARMOR_STAND, item -> item.dataDriven()
            .modifyComponents(dataDrivenOverride(16, "armor_stand", "item.armor_stand.name", false).toArray(ItemComponent[]::new)));
        // For fireworks rocket, we purposely make this item data driven so now bedrock won't do client-sided boosting
        // and now we can control fireworks boost ourselves! This resolve https://github.com/GeyserMC/Geyser/issues/5409
        layer.overrideItem(BedrockItems.FIREWORK_ROCKET, item -> item.dataDriven()
            .modifyComponents(dataDrivenOverride(64, "fireworks", "item.fireworks.name", true).toArray(ItemComponent[]::new)));

        // The locator map is its own item on Bedrock only (an empty map with damage 2); Java has no equivalent
        layer.creative(editor -> editor.remove(ItemMatcher.of(BedrockItems.EMPTY_MAP.variant(2))));

        // Custom blocks replacing the block of an item take over the item too: the custom block owns an item that
        // takes the place of the vanilla item in the creative inventory (see CustomBlockRegistryPopulator)
        List<String> replacedCreativeItems = new ArrayList<>();
        for (Item javaItem : items) {
            if (javaItem instanceof BlockItem) {
                CustomBlockData customBlockData = BlockRegistries.CUSTOM_BLOCK_ITEM_OVERRIDES.getOrDefault(javaItem.javaKey().asString(), null);
                // A non-vanilla custom block override keeps the vanilla item
                if (customBlockData != null && customBlockData.includedInCreativeInventory() && !(customBlockData instanceof NonVanillaCustomBlockData)) {
                    replacedCreativeItems.add(javaItem.bedrockType().identifier());
                }
            }
        }

        for (CustomItem customItem : customItems) {
            layer.item(customItem);
        }

        layer.creative(editor -> {
            // Since the fireworks tag now won't show up due to this is being a data driven item, we have to translate it to lore ourselves
            editor.replace(ItemMatcher.anyVariantOf(BedrockItems.FIREWORK_ROCKET), stack -> stack.withNbt(fireworkLore(stack.nbt())));
            for (String identifier : replacedCreativeItems) {
                editor.remove(ItemMatcher.id(identifier).and(ItemMatcher.aux(0)));
            }
        });
        return layer.build();
    }

    /**
     * Builds the Paletter with the custom block and item layers. Content Paletter rejects only when compiling (a
     * {@link PaletteException} that names it, e.g. an identifier used twice) is left out with a warning, and compiling
     * starts again without it: one bad entry of an extension does not stop Geyser.
     */
    private static Paletter buildPaletter(List<Item> items, Map<CustomItemDefinition, CustomItem> layerItems, Map<CustomItemDefinition, String> sources) {
        boolean blocksLeftOut = false;
        while (true) {
            try {
                Paletter paletter = BlockRegistryPopulator.buildPaletter(itemLayer(items, layerItems.values()));
                BlockRegistryPopulator.initializePalettes(paletter, blocksLeftOut);
                return paletter;
            } catch (PaletteException exception) {
                String identifier = exception.identifier();
                if (identifier != null && ITEM_LAYER.equals(exception.layer())
                    && layerItems.entrySet().removeIf(entry -> {
                        if (!entry.getValue().identifier().equals(identifier)) {
                            return false;
                        }
                        CustomContentSources.warnRejected("item", identifier, sources.get(entry.getKey()), exception);
                        return true;
                    })) {
                    continue;
                }
                if (identifier != null && BlockRegistryPopulator.BLOCK_LAYER.equals(exception.layer())
                    && CustomBlockRegistryPopulator.leaveOut(identifier, exception)) {
                    blocksLeftOut = true;
                    continue;
                }
                throw exception;
            }
        }
    }

    private static @Nullable NbtMap fireworkLore(@Nullable NbtMap tag) {
        if (tag == null) {
            return null;
        }
        DataComponents components = new DataComponents(new HashMap<>());
        Items.FIREWORK_ROCKET.translateNbtToJava(null, tag, components, null);
        BedrockItemBuilder builder = new BedrockItemBuilder();
        Items.FIREWORK_ROCKET.translateComponentsToBedrock(null, components, TooltipOptions.ALL_SHOWN, builder);
        return builder.build();
    }

    /**
     * The components Geyser gives vanilla items it makes data driven (cake stack size, armor stand, firework rocket).
     */
    private static List<ItemComponent> dataDrivenOverride(int maxStackSize, @Nullable String texture, @Nullable String displayName, boolean throwable) {
        List<ItemComponent> components = new ArrayList<>();
        if (texture != null) {
            components.add(Icon.of(texture));
        }
        if (displayName != null) {
            components.add(ItemDisplayName.builder().value(displayName).build());
        }
        components.add(AllowOffHand.of(true));
        components.add(MaxStackSize.of(maxStackSize));
        if (throwable) {
            components.add(ThrowableItem.builder().doSwingAnimation(true).build());
            components.add(Projectile.builder().projectileEntity("minecraft:snowball").build());
        }
        return components;
    }

    /**
     * Registers the item form of a custom skull as a custom item, giving it the icon of the block it places,
     * wearability, and the player head stack size. The latter two are inherited from the vanilla player head components.
     */
    static List<ItemComponent> skullItemComponents(String identifier) throws InvalidItemComponentsException {
        CustomItemDefinition definition = new GeyserCustomItemDefinition.Builder(Identifier.of(identifier), Identifier.of("minecraft", "player_head"))
            .geyserComponent(GeyserItemDataComponents.BLOCK_PLACER, GeyserBlockPlacer.of(Identifier.of(identifier), true))
            .displayName("item.player_head.name")
            .bedrockOptions(CustomItemBedrockOptions.builder().creativeCategory(CreativeCategory.EQUIPMENT))
            .build();
        return CustomItemRegistryPopulator.registerCustomItem(Items.PLAYER_HEAD, definition, true);
    }

    /**
     * Compares custom item definitions based on their predicates:
     *
     * <ol>
     *     <li>First by checking their priority values, higher priority values going first.</li>
     *     <li>Then by checking if they both have a similar range dispatch predicate, the one with the highest threshold going first.</li>
     *     <li>Lastly by the amount of predicates, from most to least.</li>
     * </ol>
     *
     * <p>If two definitions are the same, 0 is returned. If the model of two definitions differs, they are compared using their bedrock identifier, since
     * predicates won't matter there. If two definitions have the same model, same priority, same amount of predicates and no range dispatch preference, they are compared
     * using their bedrock identifier.</p>
     *
     * <p>As such, it is important that bedrock identifiers are always unique when using this comparator. This comparator can regard 2 definitions as equal if their
     * bedrock identifier is equal.</p>
     *
     * <p>Please note! The range dispatch predicate sorting only works when two definitions both only have one range dispatch predicate, both of which have the same properties
     * (property, index, normalized, negated). If there's a more complicated setup with e.g. multiple range dispatch predicates, priority values should be used to ensure
     * proper sorting.</p>
     */
    private static class CustomItemDefinitionComparator implements Comparator<GeyserCustomMappingData> {

        @Override
        public int compare(GeyserCustomMappingData firstData, GeyserCustomMappingData secondData) {
            // Important to remember when reading through this code: returning a positive number means the second definition will be checked before the first.
            // Returning a negative number means the first definition will be checked before the second.
            // Returning zero means the definitions are equal.

            CustomItemDefinition first = firstData.definition();
            CustomItemDefinition second = secondData.definition();
            if (first.equals(second)) {
                // If equal, return 0
                return 0;
            } else if (!first.model().equals(second.model())) {
                // If models differ, compare by bedrock identifier, which should always be unique
                return first.bedrockIdentifier().toString().compareTo(second.bedrockIdentifier().toString());
            }

            if (first.priority() != second.priority()) {
                // If priority differs, prefer the higher priority value
                return second.priority() - first.priority();
            }

            if (first.predicates().size() != second.predicates().size()) {
                // If predicate size differs, there's no need to check for range dispatch predicates.
                // Prefer the one with the most amount of predicates
                return second.predicates().size() - first.predicates().size();
            }

            // Loop through the predicates of the first definition and look for a range dispatch predicate
            for (MinecraftPredicate<? super ItemPredicateContext> predicate : first.predicates()) {
                if (predicate instanceof GeyserRangeDispatchPredicate(GeyserRangeDispatchPredicate.GeyserRangeDispatchProperty rangeProperty, double threshold, int index, boolean normalized, boolean negated)) {
                    // Look for a similar predicate in the other definition's predicates
                    Optional<GeyserRangeDispatchPredicate> other = second.predicates().stream()
                        .filter(GeyserRangeDispatchPredicate.class::isInstance)
                        .map(GeyserRangeDispatchPredicate.class::cast)
                        .filter(otherPredicate ->
                            otherPredicate.rangeProperty() == rangeProperty
                            && otherPredicate.index() == index
                            && otherPredicate.normalized() == normalized
                            && otherPredicate.negated() == negated)
                        .findFirst();

                    if (other.isPresent()) {
                        // If a similar predicate was found, check if they're negated
                        // If they are, prefer the one with the lowest threshold
                        // If they aren't, prefer the one with the highest threshold
                        double thresholdComparison = negated ? threshold - other.get().threshold() : other.get().threshold() - threshold;
                        if (thresholdComparison != 0.0) {
                            return thresholdComparison < 0 ? -1 : 1;
                        }
                    }
                }
            }

            if (first.predicates().size() == second.predicates().size()) {
                // If there's no preferred range predicate order and they both have the same amount of predicates, compare by bedrock identifier
                return first.bedrockIdentifier().toString().compareTo(second.bedrockIdentifier().toString());
            }

            // Prefer the one with the most amount of predicates
            return second.predicates().size() - first.predicates().size();
        }
    }
}
