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

package org.geysermc.geyser.interaction;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.registry.BlockRegistries;
import org.geysermc.geyser.registry.Registries;
import org.geysermc.geyser.util.JsonUtils;

import java.io.InputStream;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Static facts about vanilla blocks and items that interactions depend on, but that the server never sends us
 * (e.g. which blocks an axe can strip). Read from {@value #DATA_FILE}, which is generated and validated by the game
 * test module from the vanilla registries, so it stays correct across Minecraft updates.
 * <p>
 * All lookups are bit set lookups by Java network ID.
 */
public final class InteractionTables {
    public static final String DATA_FILE = "java/interaction_tables.json";

    /**
     * Per block state flags.
     */
    public enum StateFlag {
        REDSTONE_CONDUCTOR("redstone_conductor"),
        SIGNAL_SOURCE("signal_source"),
        /**
         * {@code BlockStateBase#isFaceSturdy} with the full support type, per face.
         */
        STURDY_UP("sturdy_up"),
        STURDY_DOWN("sturdy_down"),
        STURDY_NORTH("sturdy_north"),
        STURDY_SOUTH("sturdy_south"),
        STURDY_WEST("sturdy_west"),
        STURDY_EAST("sturdy_east"),
        /**
         * {@code Block#canSupportCenter} for the top and bottom faces.
         */
        CENTER_UP("center_up"),
        CENTER_DOWN("center_down"),
        /**
         * {@code Block#canSupportRigidBlock}: the top face is sturdy with the rigid support type.
         */
        RIGID_UP("rigid_up"),
        /**
         * {@code Block#isFaceFull} of the collision shape for the bottom face (e.g. {@code MultifaceBlock#canAttachTo}).
         */
        COLLISION_FULL_DOWN("collision_full_down"),
        /**
         * {@code FireBlock#canBurn}: fire can spread to this state.
         */
        FLAMMABLE("flammable"),
        REPLACEABLE("replaceable"),
        /**
         * Legacy {@code BlockStateBase#isSolid}, used e.g. for liquid placement.
         */
        SOLID("solid");

        private final String key;
        private BitSet states = new BitSet();

        StateFlag(String key) {
            this.key = key;
        }

        public boolean test(@NonNull BlockState state) {
            return states.get(state.javaId());
        }
    }

    /**
     * @return whether the given face of the state is sturdy (full support type)
     */
    public static boolean isFaceSturdy(@NonNull BlockState state, @NonNull Direction face) {
        return switch (face) {
            case UP -> StateFlag.STURDY_UP.test(state);
            case DOWN -> StateFlag.STURDY_DOWN.test(state);
            case NORTH -> StateFlag.STURDY_NORTH.test(state);
            case SOUTH -> StateFlag.STURDY_SOUTH.test(state);
            case WEST -> StateFlag.STURDY_WEST.test(state);
            case EAST -> StateFlag.STURDY_EAST.test(state);
        };
    }

    /**
     * Per block flags.
     */
    public enum BlockFlag {
        AXE_STRIPPABLE("axe_strippable"),
        AXE_SCRAPABLE("axe_scrapable"),
        AXE_WAX_OFF("axe_wax_off"),
        HONEYCOMB_WAXABLE("honeycomb_waxable"),
        HOE_TILLABLE("hoe_tillable"),
        SHOVEL_FLATTENABLE("shovel_flattenable"),
        BONEMEALABLE("bonemealable"),
        NOT_OPENABLE_BY_HAND("not_openable_by_hand");

        private final String key;
        private BitSet blocks = new BitSet();

        BlockFlag(String key) {
            this.key = key;
        }

        public boolean test(@NonNull Block block) {
            return blocks.get(block.javaId());
        }

        public boolean test(@NonNull BlockState state) {
            return test(state.block());
        }
    }

    /**
     * Per item flags.
     */
    public enum ItemFlag {
        COMPOSTABLE("compostable"),
        SIGN_APPLICATOR("sign_applicator");

        private final String key;
        private BitSet items = new BitSet();

        ItemFlag(String key) {
            this.key = key;
        }

        public boolean test(@NonNull Item item) {
            return items.get(item.javaId());
        }
    }

    public static void populate() {
        JsonObject data;
        try (InputStream stream = GeyserImpl.getInstance().getBootstrap().getResourceOrThrow(DATA_FILE)) {
            data = JsonUtils.fromJson(stream);
        } catch (Exception e) {
            throw new AssertionError("Unable to load interaction tables", e);
        }

        JsonObject states = data.getAsJsonObject("block_states");
        for (StateFlag flag : StateFlag.values()) {
            BitSet set = new BitSet();
            JsonArray ranges = states == null ? null : states.getAsJsonArray(flag.key);
            if (ranges != null) {
                for (JsonElement range : ranges) {
                    JsonArray bounds = range.getAsJsonArray();
                    set.set(bounds.get(0).getAsInt(), bounds.get(1).getAsInt() + 1);
                }
            }
            flag.states = set;
        }

        Map<String, Block> blocksById = new HashMap<>();
        for (Block block : BlockRegistries.JAVA_BLOCKS.get()) {
            blocksById.put(block.javaIdentifier().asString(), block);
        }
        JsonObject blocks = data.getAsJsonObject("blocks");
        for (BlockFlag flag : BlockFlag.values()) {
            BitSet set = new BitSet();
            JsonArray ids = blocks == null ? null : blocks.getAsJsonArray(flag.key);
            if (ids != null) {
                for (JsonElement id : ids) {
                    Block block = blocksById.get(id.getAsString());
                    if (block != null) {
                        set.set(block.javaId());
                    }
                }
            }
            flag.blocks = set;
        }

        JsonObject items = data.getAsJsonObject("items");
        for (ItemFlag flag : ItemFlag.values()) {
            BitSet set = new BitSet();
            JsonArray ids = items == null ? null : items.getAsJsonArray(flag.key);
            if (ids != null) {
                for (JsonElement id : ids) {
                    Item item = Registries.JAVA_ITEM_IDENTIFIERS.get(id.getAsString());
                    if (item != null) {
                        set.set(item.javaId());
                    }
                }
            }
            flag.items = set;
        }
    }

    private InteractionTables() {
    }
}
