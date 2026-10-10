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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.math.vector.Vector3d;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.registry.BlockRegistries;
import org.geysermc.geyser.registry.Registries;
import org.geysermc.geyser.util.JsonUtils;

import java.io.InputStream;
import java.util.Arrays;
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
        SOLID("solid"),
        /**
         * {@code BlockStateBase#isSolidRender}.
         */
        SOLID_RENDER("solid_render"),
        /**
         * The top face of the collision shape is not empty ({@code getCollisionShape(...).getFaceShape(UP)}).
         */
        COLLISION_FACE_UP("collision_face_up"),
        /**
         * The top face of the collision shape is full ({@code Block#isFaceFull(getCollisionShape(...), UP)}).
         */
        COLLISION_FULL_UP("collision_full_up"),
        /**
         * {@code MultifaceBlock#canAttachTo}: a multiface block or vine can attach to this face of the state, per face.
         */
        ATTACH_DOWN("attach_down"),
        ATTACH_UP("attach_up"),
        ATTACH_NORTH("attach_north"),
        ATTACH_SOUTH("attach_south"),
        ATTACH_WEST("attach_west"),
        ATTACH_EAST("attach_east"),
        /**
         * The vanilla default state of its block ({@code Block#defaultBlockState}), which is not always the first state.
         * Use {@link #defaultState(Block)}.
         */
        DEFAULT_STATE("default_state"),
        /**
         * {@code BlockStateBase#propagatesSkylightDown}.
         */
        PROPAGATES_SKYLIGHT_DOWN("propagates_skylight_down");

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
     * Per Java block ID: the maximum horizontal and vertical random offset ({@code BlockBehaviour.OffsetType}), or
     * NaN for blocks without an offset.
     */
    private static float[] maxHorizontalOffsets = new float[0];
    private static float[] maxVerticalOffsets = new float[0];
    /**
     * Per Java block ID: whether the outline/collision shape moves with the random offset.
     */
    private static BitSet outlineOffsets = new BitSet();
    private static BitSet collisionOffsets = new BitSet();

    /**
     * Mirrors {@code BlockStateBase#getOffset}: the random offset of a block's shapes at the given position (flowers,
     * grass, bamboo, ...), or null if the block has none.
     */
    public static @Nullable Vector3d offset(@NonNull BlockState state, int x, int z) {
        int id = state.block().javaId();
        if (id >= maxHorizontalOffsets.length || Float.isNaN(maxHorizontalOffsets[id])) {
            return null;
        }
        float maxHorizontal = maxHorizontalOffsets[id];
        float maxVertical = maxVerticalOffsets[id];
        // BlockBehaviour.Properties#offsetType
        long seed = seed(x, 0, z);
        double offsetX = Math.clamp(((float) (seed & 15L) / 15.0F - 0.5) * 0.5, -maxHorizontal, maxHorizontal);
        double offsetY = maxVertical == 0 ? 0 : ((float) (seed >> 4 & 15L) / 15.0F - 1.0) * maxVertical;
        double offsetZ = Math.clamp(((float) (seed >> 8 & 15L) / 15.0F - 0.5) * 0.5, -maxHorizontal, maxHorizontal);
        return Vector3d.from(offsetX, offsetY, offsetZ);
    }

    /**
     * The offset to add to the shapes of {@link org.geysermc.geyser.registry.BlockRegistries#SHAPES} (outline) or
     * {@link org.geysermc.geyser.registry.BlockRegistries#COLLISIONS} to get the shape at the given position. For
     * blocks whose shapes move with their random offset, those shapes were taken at the origin, so they already include
     * the offset there.
     *
     * @param collision whether the collision shape is moved, instead of the outline shape
     * @return the offset, or null if the shape doesn't move
     */
    public static @Nullable Vector3d shapeOffset(@NonNull BlockState state, int x, int z, boolean collision) {
        int id = state.block().javaId();
        if (!(collision ? collisionOffsets : outlineOffsets).get(id)) {
            return null;
        }
        Vector3d offset = offset(state, x, z);
        if (offset == null) {
            return null;
        }
        return offset.sub(offset(state, 0, 0));
    }

    /**
     * Mirrors {@code Mth#getSeed}.
     */
    private static long seed(int x, int y, int z) {
        long seed = (long) (x * 3129871) ^ (long) z * 116129781L ^ (long) y;
        seed = seed * seed * 42317861L + seed * 11L;
        return seed >> 16;
    }

    /**
     * Vanilla default state ID per Java block ID; see {@link StateFlag#DEFAULT_STATE}.
     */
    private static int[] defaultStates = new int[0];

    /**
     * Mirrors {@code Block#defaultBlockState}. Unlike {@link Block#defaultBlockState()}, which is the first state of the
     * block, this is the default state vanilla uses, e.g. for placement.
     */
    public static @NonNull BlockState defaultState(@NonNull Block block) {
        int id = block.javaId();
        if (id < defaultStates.length && defaultStates[id] != -1) {
            return BlockState.of(defaultStates[id]);
        }
        return block.defaultBlockState();
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
     * @param face the face of the state something would attach to
     * @return whether a multiface block or vine can attach to the given face of the state ({@code MultifaceBlock#canAttachTo}
     * with the direction towards the state being the opposite of the face)
     */
    public static boolean canAttachTo(@NonNull BlockState state, @NonNull Direction face) {
        return switch (face) {
            case UP -> StateFlag.ATTACH_UP.test(state);
            case DOWN -> StateFlag.ATTACH_DOWN.test(state);
            case NORTH -> StateFlag.ATTACH_NORTH.test(state);
            case SOUTH -> StateFlag.ATTACH_SOUTH.test(state);
            case WEST -> StateFlag.ATTACH_WEST.test(state);
            case EAST -> StateFlag.ATTACH_EAST.test(state);
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

        int[] defaults = new int[BlockRegistries.JAVA_BLOCKS.get().size()];
        Arrays.fill(defaults, -1);
        BitSet defaultFlags = StateFlag.DEFAULT_STATE.states;
        for (int id = defaultFlags.nextSetBit(0); id >= 0; id = defaultFlags.nextSetBit(id + 1)) {
            BlockState state = BlockState.of(id);
            if (state.block().javaId() < defaults.length) {
                defaults[state.block().javaId()] = id;
            }
        }
        defaultStates = defaults;
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

        float[] horizontal = new float[BlockRegistries.JAVA_BLOCKS.get().size()];
        float[] vertical = new float[horizontal.length];
        BitSet outline = new BitSet();
        BitSet collision = new BitSet();
        Arrays.fill(horizontal, Float.NaN);
        JsonObject offsets = data.getAsJsonObject("block_offsets");
        if (offsets != null) {
            for (Map.Entry<String, JsonElement> entry : offsets.entrySet()) {
                Block block = blocksById.get(entry.getKey());
                if (block != null && block.javaId() < horizontal.length) {
                    JsonArray values = entry.getValue().getAsJsonArray();
                    horizontal[block.javaId()] = values.get(0).getAsFloat();
                    vertical[block.javaId()] = values.get(1).getAsFloat();
                    outline.set(block.javaId(), values.size() > 2 && values.get(2).getAsInt() == 1);
                    collision.set(block.javaId(), values.size() > 3 && values.get(3).getAsInt() == 1);
                }
            }
        }
        maxHorizontalOffsets = horizontal;
        maxVerticalOffsets = vertical;
        outlineOffsets = outline;
        collisionOffsets = collision;

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
