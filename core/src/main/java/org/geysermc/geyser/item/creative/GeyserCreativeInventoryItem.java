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

package org.geysermc.geyser.item.creative;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.geysermc.geyser.api.item.creative.CreativeInventoryItem;
import org.geysermc.geyser.api.item.creative.CreativeInventoryGroup;
import org.geysermc.geyser.api.util.CreativeCategory;

import java.util.Objects;

public record GeyserCreativeInventoryItem(
    ItemDefinition definition,
    @Nullable CreativeCategory category,
    CreativeInventoryGroup group
) implements CreativeInventoryItem {

    @Override
    public @NonNull String identifier() {
        return definition.getIdentifier();
    }

    @Override
    public boolean isGroup() {
        return false;
    }

    @Override
    public boolean isItem() {
        return true;
    }

    @Override
    public CreativeInventoryGroup asCreativeItemGroup() {
        throw new IllegalStateException("Not a CreativeInventoryGroup! Use #isGroup / #isItem before using this method.");
    }

    @Override
    public CreativeInventoryItem asCreativeInventoryItem() {
        return this;
    }

    public static class Builder implements CreativeInventoryItem.Builder {
        private @NonNull ItemDefinition definition;
        private @Nullable CreativeCategory category;
        private @Nullable CreativeInventoryGroup group;

        public Builder(@NonNull String identifier) {
            Objects.requireNonNull(identifier, "Identifier cannot be null!");
            definition = ItemDefinition.AIR; // TODO
        }

        @Override
        public CreativeInventoryItem.Builder category(@Nullable CreativeCategory category) {
            this.category = category;
            return this;
        }

        @Override
        public CreativeInventoryItem.Builder group(@Nullable CreativeInventoryGroup group) {
            this.group = group;
            return this;
        }

        @Override
        public CreativeInventoryItem build() {
            return new GeyserCreativeInventoryItem(definition, category, group);
        }
    }
}
