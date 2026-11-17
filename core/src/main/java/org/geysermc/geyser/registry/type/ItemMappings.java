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
import org.geysermc.geyser.item.type.BlockItem;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.geyser.registry.java.BuiltInJavaRegistries;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.paletter.Palette;
import org.geysermc.paletter.block.RegisteredBlockState;
import org.geysermc.paletter.item.BedrockItemStack;
import org.geysermc.paletter.item.ResolvedItem;
import org.geysermc.paletter.item.RegisteredItem;
import org.geysermc.paletter.vanilla.BedrockBlockState;
import org.geysermc.paletter.vanilla.BedrockItemType;
import org.geysermc.paletter.vanilla.BedrockItemVariant;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
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

    Map<String, ItemMapping> cachedJavaMappings = new WeakHashMap<>();

    ItemMapping[] items;

    /**
     * What a decoded stack is (item variant, and block when it is not the item's default block) to the Java item that
     * claims it, for {@link #getMapping(ItemData, Palette)}. The item's mapping is {@link #getMapping(Item)}.
     */
    Map<ItemKey, Item> bedrockToJava;
    /**
     * The newest Palette, for the block of a variant.
     */
    Palette newest;
    /**
     * The mapping of a Bedrock variant, empty if no item sends stacks as it ({@link Item#isBedrockVariant(BedrockItemVariant)}),
     * by variant. Filled on lookup. Concurrent as sessions share it (fastutil has no concurrent map).
     */
    @Builder.Default
    Map<BedrockItemVariant, Optional<ItemMapping>> variantMappings = new ConcurrentHashMap<>();
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
     * @param variant a variant of an item ({@link Item#isBedrockVariant(BedrockItemVariant)})
     * @return the mapping of the variant
     */
    @NonNull
    public ItemMapping getVariantMapping(@NonNull BedrockItemVariant variant) {
        ItemMapping mapping = variantMapping(variant);
        if (mapping == null) {
            throw new IllegalArgumentException("No item sends stacks as the Bedrock variant " + variant);
        }
        return mapping;
    }

    /**
     * @return the mapping of the variant if an item sends stacks as it, otherwise null
     */
    @Nullable
    private ItemMapping variantMapping(@NonNull BedrockItemVariant variant) {
        return this.variantMappings.computeIfAbsent(variant, key -> {
            Item owner = null;
            for (Item item : BuiltInJavaRegistries.ITEM) {
                if (item.isBedrockVariant(key)) {
                    if (owner != null) {
                        throw new IllegalStateException("Java items " + owner.javaKey() + " and " + item.javaKey()
                            + " both send stacks as the Bedrock variant " + key);
                    }
                    owner = item;
                }
            }
            if (owner == null) {
                return Optional.empty();
            }
            return Optional.of(ItemMapping.builder()
                .javaItem(owner)
                .bedrockVariant(key)
                .bedrockBlockState(owner instanceof BlockItem ? this.newest.blockOf(key) : null)
                .build());
        }).orElse(null);
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
        ItemMapping variantMapping = variantMapping(variant);
        if (variantMapping != null) {
            return variantMapping;
        }
        BedrockItemType type = variant.type();

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
        ResolvedItem resolved = palette.resolve(variant);
        RegisteredBlockState own = resolved == null ? null : resolved.block();
        return own != null && own.getRuntimeId() == 0;
    }

    public boolean isBucket(@NonNull ItemDefinition definition) {
        return definition instanceof RegisteredItem item && buckets.contains(item.key());
    }

    public @Nullable BedrockItemType customBlockItem(@NonNull CustomBlockData block) {
        return customBlockItems.get(block);
    }
}
