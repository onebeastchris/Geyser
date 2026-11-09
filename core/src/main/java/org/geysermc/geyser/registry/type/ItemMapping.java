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

import com.google.common.collect.SortedSetMultimap;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.geysermc.geyser.item.GeyserCustomMappingData;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.item.type.BlockItem;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.paletter.Palette;
import org.geysermc.paletter.item.BedrockItemStack;
import org.geysermc.paletter.vanilla.BedrockBlockState;
import org.geysermc.paletter.vanilla.BedrockItemVariant;
import org.geysermc.paletter.vanilla.BedrockItems;


@Value
@Builder
@EqualsAndHashCode
@ToString
public class ItemMapping {
    public static final ItemMapping AIR = ItemMapping.builder()
            .javaItem(Items.AIR)
            .bedrockVariant(BedrockItems.AIR.base())
            .build();

    /**
     * The translation string of a non-vanilla custom item. Vanilla items that the Bedrock version lacks are translated
     * with their Java name, see {@link #translationString(Palette)}.
     */
    String translationString;

    /**
     * A map of item models and all of their custom items, sorted from most definition predicates to least, which is important when matching predicates.
     */
    @Nullable
    SortedSetMultimap<Key, GeyserCustomMappingData> customItemDefinitions;

    @Builder.Default
    boolean containsV1Mappings = false;

    @NonNull
    Item javaItem;

    /**
     * What Bedrock item variant this Java item is sent as: the newest version's key, version independent. For
     * non-vanilla custom items and custom block items taking over a vanilla block item, the custom item's key.
     */
    @NonNull
    BedrockItemVariant bedrockVariant;

    /**
     * The block state a block item is sent with: Paletter's block for the variant, or the key of the custom block
     * taking over the item. Null for items that are no block items.
     */
    @Nullable
    BedrockBlockState bedrockBlockState;

    /**
     * Whether the Java item has no Bedrock counterpart (it is sent as {@link #bedrockVariant}, but Bedrock stacks of
     * it are not this item). Items with a built-in stand-in are not java-only.
     */
    boolean javaOnly;

    /**
     * Gets if this item is a block.
     *
     * @return if this item is a block
     */
    public boolean isBlock() {
        return this.bedrockBlockState != null;
    }

    /**
     * Gets if this item has a translation string present in the palette.
     *
     * @return if this item has a translation string present
     */
    public boolean hasTranslation(@NonNull Palette palette) {
        return translationString(palette) != null;
    }

    private @NonNull ItemData encode(@NonNull Palette palette, int count) {
        ItemData encoded = palette.encode(BedrockItemStack.of(this.bedrockVariant, count).withBlockState(this.bedrockBlockState));
        if (this.bedrockBlockState == null && encoded.getBlockDefinition() != null) {
            // Not a block item here, even if the item it falls back to is one
            return encoded.toBuilder().blockDefinition(null).build();
        }
        return encoded;
    }

    /**
     * @return the item definition this mapping is sent as in the palette, fallbacks applied
     */
    public @NonNull ItemDefinition definition(@NonNull Palette palette) {
        ItemDefinition definition = encode(palette, 1).getDefinition();
        return definition == null ? ItemDefinition.AIR : definition;
    }

    /**
     * @return the damage this mapping is sent with in the palette, fallbacks applied
     */
    public int damage(@NonNull Palette palette) {
        return encode(palette, 1).getDamage();
    }

    public @NonNull String identifier(@NonNull Palette palette) {
        return definition(palette).getIdentifier();
    }

    public ItemData.@NonNull Builder itemData(@NonNull Palette palette, int count) {
        return encode(palette, count).toBuilder();
    }

    /**
     * @return whether this item has no counterpart in the palette: it is {@link #javaOnly}, or the palette sends its
     * variant as another item
     */
    public boolean isJavaOnlyIn(@NonNull Palette palette) {
        if (this.javaOnly) {
            return true;
        }
        if (this == AIR) {
            return false;
        }
        return palette.isFallback(this.bedrockVariant);
    }

    /**
     * @return "block."/"item." and the Java key if the item has no counterpart in the palette (see {@link #isJavaOnlyIn}),
     * else the stored translation string
     */
    public @Nullable String translationString(@NonNull Palette palette) {
        if (isJavaOnlyIn(palette)) {
            return (this.javaItem instanceof BlockItem ? "block." : "item.") + this.javaItem.javaKey().asString().replace(":", ".");
        }
        return this.translationString;
    }
}
