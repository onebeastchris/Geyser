/*
 * Copyright (c) 2025 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.api.item.creative;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.item.custom.NonVanillaCustomItemData;
import org.geysermc.geyser.api.util.CreativeCategory;

/**
 * Represents a single item inside either a {@link CreativeInventoryCategory}, or
 * inside a {@link CreativeInventoryGroup}.
 */
public interface CreativeInventoryItem extends CreativeInventoryElement {

    /**
     * The bedrock item identifier of this item.
     *
     * @return the identifier
     */
    @NonNull String identifier();

    /**'
     * The creative category this item is in.
     *
     * @return the creative category, if one is set
     */
    @Nullable CreativeCategory category();

    /**
     * The creative item group that this item is in.
     *
     * @return the creative item group, if one is set
     */
    @Nullable
    CreativeInventoryGroup group();

    static Builder createBuilder(String identifier) {
        GeyserApi.api().provider(Builder.class, identifier);
    }

    default CreativeInventoryItem of(String item) {
        return createBuilder().identifier(item).build();
    }

    default CreativeInventoryItem of(NonVanillaCustomItemData item) {
        return of(item.identifier());
    }

    interface Builder {

        Builder category(@Nullable CreativeCategory category);

        Builder group(@Nullable CreativeInventoryGroup group);

        CreativeInventoryItem build();
    }
}
