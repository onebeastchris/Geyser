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


package org.geysermc.geyser.registry.populator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.geysermc.geyser.level.block.property.Property;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.paletter.vanilla.BedrockBlockState;

import java.util.Locale;
import java.util.Map;

/**
 * The Java block state to Bedrock block state mappings of {@code mappings/blocks.json}, in their reduced form (format 3,
 * specified by {@code FORMAT.md} in the mappings repository): per Java block, how the Bedrock name and each Bedrock
 * state value follow from the Java properties they depend on, instead of one Bedrock state per Java state.
 * <pre>
 * {"format": 3, "tables": {"$1": ...}, "blocks": {"&lt;java path&gt;": entry | "$id"}}
 * entry: {"name": value, "states": {"&lt;bedrock state&gt;": value}}
 * value: constant | "$id" | {"by": [java properties], "values": {"&lt;java values joined by ,&gt;": constant}}
 * </pre>
 * Constants are typed: booleans become bytes, integers ints and strings strings. A Java block that is absent has the same
 * name on Bedrock and no states. Strings starting with {@code $} are references into {@code tables}.
 */
final class JavaBlockMappingTable {
    static final int FORMAT = 3;

    private final JsonObject sharedTables;
    private final JsonObject blocks;
    /**
     * The decoded tables, per Java block, built on first use
     */
    private final Object2ObjectMap<Block, BlockTable> tables = new Object2ObjectOpenHashMap<>();

    private JavaBlockMappingTable(JsonObject sharedTables, JsonObject blocks) {
        this.sharedTables = sharedTables;
        this.blocks = blocks;
    }

    static JavaBlockMappingTable of(JsonObject root) {
        int format = root.has("format") ? root.get("format").getAsInt() : -1;
        if (format != FORMAT) {
            throw new IllegalArgumentException("Unsupported blocks.json format " + format + ", expected " + FORMAT);
        }
        JsonObject shared = root.has("tables") ? root.getAsJsonObject("tables") : new JsonObject();
        return new JavaBlockMappingTable(shared, root.getAsJsonObject("blocks"));
    }

    /**
     * A Bedrock block state as the mappings describe it: the full name and typed state values (booleans are bytes)
     */
    record RawState(String name, Map<String, Object> states) {
    }

    /**
     * @return the key of a vanilla Java block state in the newest Bedrock version, or null if Paletter's data has no such
     * state
     */
    @Nullable BedrockBlockState bedrockState(BlockState state) {
        RawState raw = rawState(state);
        NbtMapBuilder states = NbtMap.builder();
        for (Map.Entry<String, Object> entry : raw.states().entrySet()) {
            states.put(entry.getKey(), entry.getValue());
        }
        // No NbtMap leaves this class
        return BedrockBlockState.vanilla(NbtMap.builder()
            .putString("name", raw.name())
            .putCompound("states", states.build())
            .build());
    }

    /**
     * @return the Bedrock block state {@code {name, states}} of a vanilla Java block state, of the newest Bedrock version
     */
    RawState rawState(BlockState state) {
        BlockTable table = tables.computeIfAbsent(state.block(), (Block block) -> table(block));
        Map<String, Object> states = new Object2ObjectLinkedOpenHashMap<>();
        for (StateTable stateTable : table.states) {
            states.put(stateTable.key, stateTable.values.get(state));
        }
        return new RawState("minecraft:" + (table.name == null ? state.block().javaIdentifier().value() : table.name.get(state)), states);
    }

    /**
     * Follows a {@code $} reference (a string starting with {@code $} is always one)
     */
    private JsonElement resolve(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String string = element.getAsString();
            if (string.startsWith("$")) {
                JsonElement target = sharedTables.get(string);
                if (target == null) {
                    throw new IllegalArgumentException("blocks.json refers to unknown table " + string);
                }
                return target;
            }
        }
        return element;
    }

    private BlockTable table(Block block) {
        JsonElement blockElement = blocks.get(block.javaIdentifier().value());
        if (blockElement == null) {
            return BlockTable.IDENTITY;
        }
        JsonObject entry = resolve(blockElement).getAsJsonObject();
        JsonElement name = entry.get("name");
        JsonObject statesObject = entry.has("states") ? entry.getAsJsonObject("states") : new JsonObject();
        StateTable[] states = new StateTable[statesObject.size()];
        int i = 0;
        for (Map.Entry<String, JsonElement> state : statesObject.entrySet()) {
            states[i++] = new StateTable(state.getKey(), valueTable(block, state.getValue()));
        }
        return new BlockTable(name == null ? null : valueTable(block, name), states);
    }

    private ValueTable valueTable(Block block, JsonElement element) {
        element = resolve(element);
        if (element.isJsonPrimitive()) {
            return new ValueTable(constant(element.getAsJsonPrimitive()), new Property<?>[0], null);
        }
        JsonObject table = element.getAsJsonObject();
        JsonArray by = table.getAsJsonArray("by");
        Property<?>[] properties = new Property<?>[by.size()];
        for (int i = 0; i < properties.length; i++) {
            properties[i] = property(block, by.get(i).getAsString());
        }
        Map<String, Object> values = new Object2ObjectOpenHashMap<>();
        for (Map.Entry<String, JsonElement> value : table.getAsJsonObject("values").entrySet()) {
            values.put(value.getKey(), constant(value.getValue().getAsJsonPrimitive()));
        }
        return new ValueTable(null, properties, values);
    }

    /**
     * Booleans are Bedrock bytes, integers ints, strings strings
     */
    private static Object constant(JsonPrimitive primitive) {
        if (primitive.isBoolean()) {
            return (byte) (primitive.getAsBoolean() ? 1 : 0);
        }
        if (primitive.isNumber()) {
            return primitive.getAsInt();
        }
        return primitive.getAsString();
    }

    private static Property<?> property(Block block, String name) {
        Property<?>[] properties = block.propertyKeys();
        if (properties != null) {
            for (Property<?> property : properties) {
                if (property.name().equals(name)) {
                    return property;
                }
            }
        }
        throw new IllegalArgumentException("blocks.json refers to unknown property " + name + " of " + block.javaIdentifier());
    }

    private record BlockTable(@Nullable ValueTable name, StateTable[] states) {
        static final BlockTable IDENTITY = new BlockTable(null, new StateTable[0]);
    }

    private record StateTable(String key, ValueTable values) {
    }

    /**
     * A constant, or a value keyed by the Java values of some properties
     */
    private record ValueTable(@Nullable Object constant, Property<?>[] properties, @Nullable Map<String, Object> values) {

        @SuppressWarnings("unchecked")
        <T> @NonNull T get(BlockState state) {
            if (values == null) {
                return (T) constant;
            }
            String key;
            if (properties.length == 1) {
                key = valueName(state, properties[0]);
            } else {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < properties.length; i++) {
                    if (i > 0) {
                        builder.append(',');
                    }
                    builder.append(valueName(state, properties[i]));
                }
                key = builder.toString();
            }
            Object value = values.get(key);
            if (value == null) {
                throw new IllegalStateException("blocks.json has no value for " + state + " (" + key + ")");
            }
            return (T) value;
        }

        /**
         * The Java name of a property value, as in {@link BlockState#toString()}
         */
        private static String valueName(BlockState state, Property<?> property) {
            return state.getValue(property).toString().toLowerCase(Locale.ROOT);
        }
    }
}
