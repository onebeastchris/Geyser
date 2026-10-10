/*
 * Copyright (c) 2019-2026 GeyserMC. http://geysermc.org
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
package org.geysermc.geyser.gametest.interaction.hints;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;

/**
 * Derives scenario inputs from vanilla (registries, tags, block and item classes and vanilla's own tables), so that
 * the scenarios follow vanilla when it changes instead of being hardcoded lists.
 */
final class VanillaSources {

    /**
     * @return every item in the item tag
     */
    static List<Item> items(TagKey<Item> tag) {
        return items(item -> new ItemStack(item).is(tag));
    }

    static List<Item> items(Predicate<Item> predicate) {
        List<Item> items = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (predicate.test(item)) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * @return every block in the block tag
     */
    static List<Block> blocks(TagKey<Block> tag) {
        return blocks(block -> block.defaultBlockState().is(tag));
    }

    static List<Block> blocks(Predicate<Block> predicate) {
        List<Block> blocks = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (predicate.test(block)) {
                blocks.add(block);
            }
        }
        return blocks;
    }

    /**
     * @return at most {@code max} evenly spread elements, always including the first. Unordered collections (e.g. the
     * keys of vanilla's hash maps) are sorted by identifier first, so every run tests the same elements.
     */
    static <T> List<T> sample(Collection<T> values, int max) {
        List<T> list = new ArrayList<>(values);
        if (!(values instanceof List)) {
            list.sort(Comparator.comparing(VanillaSources::identifier));
        }
        if (list.size() <= max) {
            return list;
        }
        List<T> result = new ArrayList<>(max);
        for (int i = 0; i < max; i++) {
            result.add(list.get(i * list.size() / max));
        }
        return result;
    }

    private static String identifier(Object value) {
        if (value instanceof Block block) {
            return BuiltInRegistries.BLOCK.getKey(block).toString();
        }
        if (value instanceof Item item) {
            return BuiltInRegistries.ITEM.getKey(item).toString();
        }
        return String.valueOf(value);
    }

    static ItemStack[] stacks(Collection<? extends ItemLike> items) {
        return items.stream().map(ItemStack::new).toArray(ItemStack[]::new);
    }

    static ItemStack[] stacks(ItemLike... items) {
        return stacks(List.of(items));
    }

    static Block[] array(Collection<Block> blocks) {
        return blocks.toArray(Block[]::new);
    }

    /**
     * Combines block lists, keeping the order and dropping duplicates.
     */
    @SafeVarargs
    static Block[] concat(Collection<Block>... lists) {
        LinkedHashSet<Block> blocks = new LinkedHashSet<>();
        for (Collection<Block> list : lists) {
            blocks.addAll(list);
        }
        return blocks.toArray(Block[]::new);
    }

    /**
     * Looks up a block by ID, for the few blocks that are part of a scenario's design rather than of vanilla data
     * (e.g. a plain stone block as a negative case).
     */
    static Block block(String path) {
        return BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(path));
    }

    static Item item(String path) {
        return BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(path));
    }

    private VanillaSources() {
    }
}
