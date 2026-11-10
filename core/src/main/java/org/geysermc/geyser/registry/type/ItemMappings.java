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

package org.geysermc.geyser.registry.type;

import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Builder;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.api.block.custom.CustomBlockData;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.paletter.Palette;
import org.geysermc.paletter.block.RegisteredBlockState;
import org.geysermc.paletter.item.BedrockItemStack;
import org.geysermc.paletter.item.RegisteredItem;
import org.geysermc.paletter.vanilla.BedrockBlockState;
import org.geysermc.paletter.vanilla.BedrockItemType;
import org.geysermc.paletter.vanilla.BedrockItemVariant;
import org.geysermc.paletter.vanilla.BedrockItems;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Java items to Bedrock items. Version independent: the tables are keys of the newest Bedrock version
 * ({@link BedrockItemVariant}, {@link BedrockBlockState}). What a key is sent as, in a Bedrock version, is the palette's
 * job: a session converts through {@code session.palette()}.
 */
@Builder
@Value
public class ItemMappings {

    /**
     * What a Bedrock stack is identified as: the item variant, and the block when it is not the item's default block.
     */
    public record ItemKey(@NonNull BedrockItemVariant variant, @Nullable BedrockBlockState block) {
    }

    /**
     * The Bedrock item of each light level; Bedrock has one item per level where Java has one with a level state
     */
    public static final List<BedrockItemType> LIGHT_BLOCKS = List.of(BedrockItems.LIGHT_BLOCK_0, BedrockItems.LIGHT_BLOCK_1,
        BedrockItems.LIGHT_BLOCK_2, BedrockItems.LIGHT_BLOCK_3, BedrockItems.LIGHT_BLOCK_4, BedrockItems.LIGHT_BLOCK_5,
        BedrockItems.LIGHT_BLOCK_6, BedrockItems.LIGHT_BLOCK_7, BedrockItems.LIGHT_BLOCK_8, BedrockItems.LIGHT_BLOCK_9,
        BedrockItems.LIGHT_BLOCK_10, BedrockItems.LIGHT_BLOCK_11, BedrockItems.LIGHT_BLOCK_12, BedrockItems.LIGHT_BLOCK_13,
        BedrockItems.LIGHT_BLOCK_14, BedrockItems.LIGHT_BLOCK_15);

    /**
     * The light block mappings by their Bedrock item. Java has one light item with a level state.
     */
    public static final Map<BedrockItemType, ItemMapping> LIGHT_BLOCK_MAPPINGS = lightBlockMappings();

    /**
     * A unique exception as this is an item in Bedrock, but not in Java.
     */
    public static final ItemMapping LODESTONE_COMPASS = ItemMapping.builder()
        .javaItem(Items.COMPASS)
        .customItemDefinitions(null)
        .bedrockVariant(BedrockItems.LODESTONE_COMPASS.base())
        .build();

    private static Map<BedrockItemType, ItemMapping> lightBlockMappings() {
        Map<BedrockItemType, ItemMapping> mappings = new Object2ObjectOpenHashMap<>();
        for (BedrockItemType lightBlock : LIGHT_BLOCKS) {
            mappings.put(lightBlock, ItemMapping.builder()
                .javaItem(Items.LIGHT)
                .customItemDefinitions(null)
                .bedrockVariant(lightBlock.base())
                .build());
        }
        return mappings;
    }

    Map<String, ItemMapping> cachedJavaMappings = new WeakHashMap<>();

    ItemMapping[] items;

    /**
     * What a decoded stack is (item variant, and block when it is not the item's default block) to the Java item that
     * claims it, for {@link #getMapping(ItemData, Palette)}. The item's mapping is {@link #getMapping(Item)}.
     */
    Map<ItemKey, Item> bedrockToJava;
    /**
     * The Bedrock items of buckets (bucket items, not milk).
     */
    Set<BedrockItemType> buckets;
    /**
     * The Bedrock item of each custom block's item form.
     */
    Object2ObjectMap<CustomBlockData, BedrockItemType> customBlockItems;

    IntSet nonVanillaCustomItemIds;

    /**
     * Gets an {@link ItemMapping} from the given {@link GeyserItemStack}.
     *
     * @param itemStack the itemstack
     * @return an item entry from the given item stack
     */
    public ItemMapping getMapping(@NonNull GeyserItemStack itemStack) {
        return this.getMapping(itemStack.getJavaId());
    }

    /**
     * Gets an {@link ItemMapping} from the given {@link ItemStack}.
     *
     * @param itemStack the itemstack
     * @return an item entry from the given java edition item stack
     */
    @NonNull
    public ItemMapping getMapping(@NonNull ItemStack itemStack) {
        return this.getMapping(itemStack.getId());
    }

    /**
     * Gets an {@link ItemMapping} from the given Minecraft: Java
     * Edition id.
     *
     * @param javaId the id
     * @return an item entry from the given java edition identifier
     */
    @NonNull
    public ItemMapping getMapping(int javaId) {
        return javaId >= 0 && javaId < this.items.length ? this.items[javaId] : ItemMapping.AIR;
    }

    @NonNull
    public ItemMapping getMapping(Item javaItem) {
        return getMapping(javaItem.javaId());
    }

    /**
     * Gets an {@link ItemMapping} from the given Minecraft: Java Edition
     * block state identifier.
     *
     * @param javaIdentifier the block state identifier
     * @return an item entry from the given java edition identifier
     */
    @Nullable
    public ItemMapping getMapping(String javaIdentifier) {
        return this.cachedJavaMappings.computeIfAbsent(javaIdentifier, key -> {
            for (ItemMapping mapping : this.items) {
                if (mapping.getJavaItem().javaKey().asString().equals(key)) {
                    return mapping;
                }
            }
            return null;
        });
    }

    /**
     * Gets an {@link ItemMapping} from the given {@link ItemData}, identified with the key based tables.
     *
     * @param data the item data
     * @param palette the palette the data comes from
     * @return an item entry from the given item data
     */
    @NonNull
    public ItemMapping getMapping(@NonNull ItemData data, @NonNull Palette palette) {
        ItemDefinition definition = data.getDefinition();
        if (definition == null || ItemDefinition.AIR.equals(definition)) {
            return ItemMapping.AIR;
        }

        BedrockItemStack stack = palette.decode(data);
        if (stack == null) {
            GeyserImpl.getInstance().getLogger().debug("Missing mapping for bedrock item " + data);
            return ItemMapping.AIR;
        }

        BedrockItemVariant variant = stack.variant();
        BedrockItemType type = variant.type();
        if (type.equals(BedrockItems.LODESTONE_COMPASS)) {
            return LODESTONE_COMPASS;
        }
        ItemMapping lightBlock = LIGHT_BLOCK_MAPPINGS.get(type);
        if (lightBlock != null) {
            return lightBlock;
        }

        Item item;
        if (data.getDamage() == 0 && hasBlock(data, variant, palette)) {
            // Only the exact block of a block item (the state is null for the default block of the item)
            item = bedrockToJava.get(new ItemKey(variant, stack.blockState()));
            if (item != null && getMapping(item).getBedrockBlockState() == null) {
                item = null;
            }
        } else {
            item = bedrockToJava.get(new ItemKey(variant, null));
            if (item == null && variant.aux() != 0) {
                // The base variant of an item whose damage varies
                item = bedrockToJava.get(new ItemKey(type.base(), null));
            }
            // Paletter falls back to an item's base variant; Geyser only accepts the exact damage value
            // (unless the damage of the item varies)
            if (item != null && !(item.ignoreDamage() || item == Items.SUSPICIOUS_STEW || getMapping(item).getBedrockVariant().aux() == variant.aux())) {
                item = null;
            }
        }
        if (item != null) {
            return getMapping(item);
        }

        GeyserImpl.getInstance().getLogger().debug("Missing mapping for bedrock item " + data);
        return ItemMapping.AIR;
    }

    /**
     * Bedrock sends block runtime id 0 for items that have no block, and that is a block (for example cyan terracotta
     * in 1.21.110). It is only the block of the stack if the item itself has that block.
     *
     * @return whether the stack has a block
     */
    private static boolean hasBlock(ItemData data, BedrockItemVariant variant, Palette palette) {
        BlockDefinition block = data.getBlockDefinition();
        if (block == null) {
            return false;
        }
        if (block.getRuntimeId() != 0) {
            return true;
        }
        RegisteredBlockState own = palette.blockOf(variant);
        return own != null && own.getRuntimeId() == 0;
    }

    public boolean isBucket(@NonNull ItemDefinition definition) {
        return definition instanceof RegisteredItem item && buckets.contains(item.key());
    }

    public @Nullable BedrockItemType customBlockItem(@NonNull CustomBlockData block) {
        return customBlockItems.get(block);
    }
}
