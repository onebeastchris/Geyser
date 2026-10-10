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

package org.geysermc.geyser.gametest.interaction;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.geysermc.geyser.interaction.InteractionRegistry;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Computes which vanilla class implements each interaction hook of every block and item, from the live registries.
 * This is the content of {@link InteractionRegistry#DATA_FILE}.
 * <p>
 * For each block/item, the class hierarchy is walked up to (excluding) {@code BlockBehaviour}/{@code Item}; the closest
 * class that declares the hook method is the vanilla class handling it. Blocks and items using the default are omitted.
 */
public final class InteractionClassData {

    public enum Hook {
        BLOCK_USE_ITEM_ON(InteractionRegistry.BLOCK_USE_ITEM_ON, BlockBehaviour.class, method("useItemOn",
            ItemStack.class, BlockState.class, Level.class, BlockPos.class, Player.class, InteractionHand.class, BlockHitResult.class)),
        BLOCK_USE_WITHOUT_ITEM(InteractionRegistry.BLOCK_USE_WITHOUT_ITEM, BlockBehaviour.class, method("useWithoutItem",
            BlockState.class, Level.class, BlockPos.class, Player.class, BlockHitResult.class)),
        ITEM_USE(InteractionRegistry.ITEM_USE, Item.class, method("use", Level.class, Player.class, InteractionHand.class)),
        ITEM_USE_ON(InteractionRegistry.ITEM_USE_ON, Item.class, method("useOn", UseOnContext.class)),
        BLOCK_CAN_SURVIVE(InteractionRegistry.BLOCK_CAN_SURVIVE, BlockBehaviour.class, method("canSurvive",
            BlockState.class, LevelReader.class, BlockPos.class)),
        BLOCK_STATE_FOR_PLACEMENT(InteractionRegistry.BLOCK_STATE_FOR_PLACEMENT, Block.class, method("getStateForPlacement",
            BlockPlaceContext.class)),
        BLOCK_CAN_BE_REPLACED(InteractionRegistry.BLOCK_CAN_BE_REPLACED, BlockBehaviour.class, method("canBeReplaced",
            BlockState.class, BlockPlaceContext.class)),
        /**
         * Any of the placement methods of {@code BlockItem} subclasses.
         */
        ITEM_PLACEMENT(InteractionRegistry.ITEM_PLACEMENT, BlockItem.class,
            method("getPlacementState", BlockPlaceContext.class),
            method("updatePlacementContext", BlockPlaceContext.class),
            method("canPlace", BlockPlaceContext.class, BlockState.class),
            method("mustSurvive"));

        private final String key;
        private final Class<?> baseClass;
        private final MethodSignature[] methods;

        Hook(String key, Class<?> baseClass, MethodSignature... methods) {
            this.key = key;
            this.baseClass = baseClass;
            this.methods = methods;
        }

        public String key() {
            return key;
        }

        public boolean isBlockHook() {
            return BlockBehaviour.class.isAssignableFrom(baseClass);
        }

        /**
         * @return the closest class in the hierarchy declaring this hook, or null if the default is used
         */
        public @Nullable Class<?> declaringClass(Class<?> runtimeClass) {
            if (!baseClass.isAssignableFrom(runtimeClass)) {
                return null;
            }
            for (Class<?> clazz = runtimeClass; clazz != null && clazz != baseClass; clazz = clazz.getSuperclass()) {
                for (MethodSignature signature : methods) {
                    try {
                        clazz.getDeclaredMethod(signature.name(), signature.parameters());
                        return clazz;
                    } catch (NoSuchMethodException ignored) {
                    }
                }
            }
            return null;
        }
    }

    private record MethodSignature(String name, Class<?>... parameters) {
    }

    private static MethodSignature method(String name, Class<?>... parameters) {
        return new MethodSignature(name, parameters);
    }

    /**
     * @return per hook key, per vanilla class name, the sorted identifiers of the blocks/items using it
     */
    public static SortedMap<String, SortedMap<String, SortedSet<String>>> compute() {
        SortedMap<String, SortedMap<String, SortedSet<String>>> result = new TreeMap<>();
        for (Hook hook : Hook.values()) {
            SortedMap<String, SortedSet<String>> classes = new TreeMap<>();
            if (hook.isBlockHook()) {
                for (Block block : BuiltInRegistries.BLOCK) {
                    add(classes, hook.declaringClass(block.getClass()), BuiltInRegistries.BLOCK.getKey(block).toString());
                }
            } else {
                for (Item item : BuiltInRegistries.ITEM) {
                    add(classes, hook.declaringClass(item.getClass()), BuiltInRegistries.ITEM.getKey(item).toString());
                }
            }
            result.put(hook.key(), classes);
        }
        return result;
    }

    private static void add(Map<String, SortedSet<String>> classes, @Nullable Class<?> declaringClass, String identifier) {
        if (declaringClass != null) {
            classes.computeIfAbsent(declaringClass.getSimpleName(), $ -> new TreeSet<>()).add(identifier);
        }
    }

    public static JsonObject toJson(SortedMap<String, SortedMap<String, SortedSet<String>>> data) {
        JsonObject root = new JsonObject();
        data.forEach((hook, classes) -> {
            JsonObject hookObject = new JsonObject();
            classes.forEach((vanillaClass, identifiers) -> {
                JsonArray array = new JsonArray();
                identifiers.forEach(array::add);
                hookObject.add(vanillaClass, array);
            });
            root.add(hook, hookObject);
        });
        return root;
    }

    /**
     * @return all blocks with a non-default hook implemented by the given vanilla class, sorted by identifier
     */
    public static List<Block> blocksOf(String vanillaClass) {
        List<Block> blocks = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (isClass(Hook.BLOCK_USE_ITEM_ON.declaringClass(block.getClass()), vanillaClass)
                || isClass(Hook.BLOCK_USE_WITHOUT_ITEM.declaringClass(block.getClass()), vanillaClass)) {
                blocks.add(block);
            }
        }
        blocks.sort((a, b) -> BuiltInRegistries.BLOCK.getKey(a).toString().compareTo(BuiltInRegistries.BLOCK.getKey(b).toString()));
        return blocks;
    }

    /**
     * @return all items with a non-default hook implemented by the given vanilla class, sorted by identifier
     */
    public static List<Item> itemsOf(String vanillaClass) {
        List<Item> items = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (isClass(Hook.ITEM_USE.declaringClass(item.getClass()), vanillaClass)
                || isClass(Hook.ITEM_USE_ON.declaringClass(item.getClass()), vanillaClass)) {
                items.add(item);
            }
        }
        items.sort((a, b) -> BuiltInRegistries.ITEM.getKey(a).toString().compareTo(BuiltInRegistries.ITEM.getKey(b).toString()));
        return items;
    }

    /**
     * @return all vanilla class names that implement a block hook, sorted
     */
    public static SortedSet<String> blockClasses() {
        SortedSet<String> classes = new TreeSet<>();
        SortedMap<String, SortedMap<String, SortedSet<String>>> data = compute();
        classes.addAll(data.get(Hook.BLOCK_USE_ITEM_ON.key()).keySet());
        classes.addAll(data.get(Hook.BLOCK_USE_WITHOUT_ITEM.key()).keySet());
        return classes;
    }

    /**
     * @return all vanilla class names that implement an item hook, sorted
     */
    public static SortedSet<String> itemClasses() {
        SortedSet<String> classes = new TreeSet<>();
        SortedMap<String, SortedMap<String, SortedSet<String>>> data = compute();
        classes.addAll(data.get(Hook.ITEM_USE.key()).keySet());
        classes.addAll(data.get(Hook.ITEM_USE_ON.key()).keySet());
        return classes;
    }

    /**
     * @return the simple names of the class and its superclasses, up to and including {@code stopAt}
     */
    public static List<String> hierarchyNames(Class<?> clazz, Class<?> stopAt) {
        List<String> names = new ArrayList<>();
        for (Class<?> current = clazz; current != null; current = current.getSuperclass()) {
            names.add(current.getSimpleName());
            if (current == stopAt) {
                break;
            }
        }
        return names;
    }

    private static boolean isClass(@Nullable Class<?> clazz, String simpleName) {
        return clazz != null && clazz.getSimpleName().equals(simpleName);
    }

    /**
     * Converts a class name to the snake case used in test names, e.g. {@code RedStoneOreBlock} to {@code red_stone_ore_block}.
     */
    public static String snakeCase(String className) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < className.length(); i++) {
            char c = className.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    builder.append('_');
                }
                builder.append(Character.toLowerCase(c));
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private InteractionClassData() {
    }
}
