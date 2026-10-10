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
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignApplicator;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import org.geysermc.geyser.gametest.mixin.AxeItemAccessor;
import org.geysermc.geyser.gametest.mixin.FireBlockInvoker;
import org.geysermc.geyser.gametest.mixin.HoeItemAccessor;
import org.geysermc.geyser.gametest.mixin.ShovelItemAccessor;
import org.geysermc.geyser.gametest.mixin.TrapDoorBlockInvoker;
import org.geysermc.geyser.interaction.InteractionTables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;

/**
 * Computes the content of {@link InteractionTables#DATA_FILE} from the live vanilla registries.
 * <p>
 * Protected vanilla members ({@code AxeItem.STRIPPABLES}, {@code HoeItem.TILLABLES}, {@code ShovelItem.FLATTENABLES},
 * {@code FireBlock#canBurn}, {@code TrapDoorBlock#getType}) are read through mixin accessors, so a renamed member
 * fails when the mixins are applied instead of silently producing wrong data.
 */
public final class InteractionTablesData {

    public static JsonObject compute() {
        JsonObject root = new JsonObject();
        root.add("block_states", blockStates());
        root.add("blocks", blocks());
        root.add("items", items());
        return root;
    }

    /**
     * {@code FireBlock#canBurn} is protected.
     */
    private static boolean canBurn(BlockState state) {
        return ((FireBlockInvoker) net.minecraft.world.level.block.Blocks.FIRE).geyser$canBurn(state);
    }

    private static JsonObject blockStates() {
        Map<String, Predicate<BlockState>> flags = new LinkedHashMap<>();
        flags.put("redstone_conductor", state -> state.isRedstoneConductor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
        flags.put("signal_source", BlockState::isSignalSource);
        flags.put("sturdy_up", state -> state.isFaceSturdy(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, Direction.UP));
        flags.put("replaceable", BlockState::canBeReplaced);
        flags.put("solid", BlockState::isSolid);
        flags.put("sturdy_down", state -> state.isFaceSturdy(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, Direction.DOWN));
        flags.put("sturdy_north", state -> state.isFaceSturdy(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, Direction.NORTH));
        flags.put("sturdy_south", state -> state.isFaceSturdy(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, Direction.SOUTH));
        flags.put("sturdy_west", state -> state.isFaceSturdy(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, Direction.WEST));
        flags.put("sturdy_east", state -> state.isFaceSturdy(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, Direction.EAST));
        flags.put("center_up", state -> state.isFaceSturdy(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, Direction.UP, SupportType.CENTER));
        flags.put("center_down", state -> state.isFaceSturdy(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, Direction.DOWN, SupportType.CENTER));
        flags.put("rigid_up", state -> state.isFaceSturdy(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, Direction.UP, SupportType.RIGID));
        flags.put("flammable", InteractionTablesData::canBurn);
        flags.put("solid_render", BlockState::isSolidRender);
        flags.put("collision_face_up", state -> !state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).getFaceShape(Direction.UP).isEmpty());
        flags.put("collision_full_up", state -> Block.isFaceFull(state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO), Direction.UP));
        for (Direction face : Direction.values()) {
            // The direction passed to canAttachTo points from the attaching block towards this state
            flags.put("attach_" + face.getSerializedName(),
                state -> MultifaceBlock.canAttachTo(EmptyBlockGetter.INSTANCE, face.getOpposite(), BlockPos.ZERO, state));
        }
        flags.put("collision_full_down", state -> Block.isFaceFull(state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO), Direction.DOWN));
        flags.put("default_state", state -> state == state.getBlock().defaultBlockState());

        int stateCount = Block.BLOCK_STATE_REGISTRY.size();
        JsonObject result = new JsonObject();
        flags.forEach((key, flag) -> {
            JsonArray ranges = new JsonArray();
            int start = -1;
            for (int id = 0; id <= stateCount; id++) {
                boolean set = id < stateCount && flag.test(Block.stateById(id));
                if (set && start == -1) {
                    start = id;
                } else if (!set && start != -1) {
                    JsonArray range = new JsonArray();
                    range.add(start);
                    range.add(id - 1);
                    ranges.add(range);
                    start = -1;
                }
            }
            result.add(key, ranges);
        });
        return result;
    }

    private static JsonObject blocks() {
        JsonObject result = new JsonObject();
        result.add("axe_strippable", blockIds(AxeItemAccessor.getStrippables().keySet()));
        result.add("axe_scrapable", blockIds(filterBlocks(block -> WeatheringCopper.getPrevious(block.defaultBlockState()).isPresent())));
        result.add("axe_wax_off", blockIds(HoneycombItem.WAXABLES.get().values()));
        result.add("honeycomb_waxable", blockIds(HoneycombItem.WAXABLES.get().keySet()));
        result.add("hoe_tillable", blockIds(HoeItemAccessor.getTillables().keySet()));
        result.add("shovel_flattenable", blockIds(ShovelItemAccessor.getFlattenables().keySet()));
        result.add("bonemealable", blockIds(filterBlocks(block -> block instanceof BonemealableBlock)));
        result.add("not_openable_by_hand", blockIds(filterBlocks(InteractionTablesData::isNotOpenableByHand)));
        return result;
    }

    private static JsonObject items() {
        JsonObject result = new JsonObject();
        SortedSet<String> compostable = new TreeSet<>();
        for (ItemLike item : ComposterBlock.COMPOSTABLES.keySet()) {
            compostable.add(BuiltInRegistries.ITEM.getKey(item.asItem()).toString());
        }
        result.add("compostable", toArray(compostable));

        SortedSet<String> signApplicators = new TreeSet<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof SignApplicator) {
                signApplicators.add(BuiltInRegistries.ITEM.getKey(item).toString());
            }
        }
        result.add("sign_applicator", toArray(signApplicators));
        return result;
    }

    /**
     * Doors and trapdoors whose block set type can't be opened by hand (e.g. iron); fence gates can always be opened.
     */
    private static boolean isNotOpenableByHand(Block block) {
        if (block instanceof DoorBlock door) {
            return !door.type().canOpenByHand();
        }
        if (block instanceof TrapDoorBlock) {
            // TrapDoorBlock#getType is protected
            return !((TrapDoorBlockInvoker) block).geyser$getType().canOpenByHand();
        }
        return false;
    }

    private static List<Block> filterBlocks(Predicate<Block> predicate) {
        List<Block> blocks = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (predicate.test(block)) {
                blocks.add(block);
            }
        }
        return blocks;
    }

    private static JsonArray blockIds(Collection<Block> blocks) {
        SortedSet<String> ids = new TreeSet<>();
        for (Block block : blocks) {
            ids.add(BuiltInRegistries.BLOCK.getKey(block).toString());
        }
        return toArray(ids);
    }

    private static JsonArray toArray(SortedSet<String> values) {
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        return array;
    }

    private InteractionTablesData() {
    }
}
