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
import dev.opencollab.paletter.Palette;
import dev.opencollab.paletter.block.RegisteredBlockState;
import dev.opencollab.paletter.item.ResolvedItem;
import dev.opencollab.paletter.vanilla.BedrockBlockState;
import dev.opencollab.paletter.vanilla.BedrockItemVariant;
import dev.opencollab.paletter.vanilla.BedrockItems;


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

    /**
     * @return the item definition this mapping is sent as in the palette, fallbacks applied
     */
    public @NonNull ItemDefinition definition(@NonNull Palette palette) {
        return palette.resolve(this.bedrockVariant).definition();
    }

    /**
     * @return the damage this mapping is sent with in the palette, fallbacks applied
     */
    public int damage(@NonNull Palette palette) {
        return palette.resolve(this.bedrockVariant).aux();
    }

    public @NonNull String identifier(@NonNull Palette palette) {
        return definition(palette).getIdentifier();
    }

    public ItemData.@NonNull Builder itemData(@NonNull Palette palette, int count) {
        // One resolution and no intermediate stack: this runs for every stack sent
        ResolvedItem resolved = palette.resolve(this.bedrockVariant);
        ItemData.Builder builder = ItemData.builder().definition(resolved.definition()).damage(resolved.aux()).count(count);
        if (this.bedrockBlockState != null) {
            // The block of the item, or of what it falls back to; as Palette#encode does for a stack with a block state
            RegisteredBlockState block = resolved.block();
            if (!resolved.fallback() && block != null) {
                block = palette.blockDefinition(this.bedrockBlockState, block);
            }
            builder.blockDefinition(block);
        }
        // Else no block: not a block item here, even if the item it falls back to is one
        return builder;
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
        return palette.resolve(this.bedrockVariant).fallback();
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
