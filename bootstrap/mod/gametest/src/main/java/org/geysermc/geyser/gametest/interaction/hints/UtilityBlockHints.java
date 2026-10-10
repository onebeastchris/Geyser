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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.block.state.properties.SlabType;
import org.geysermc.geyser.gametest.interaction.ScenarioHints;

/**
 * Scenario hints for {@code org.geysermc.geyser.interaction.block.UtilityBlockInteractions}.
 */
public final class UtilityBlockHints {
    /**
     * Places blocks exactly as given: no shape updates, so states set up here are not recomputed from their neighbours.
     */
    private static final int FLAGS = 2 | 16; // Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE
    private static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    public static void register(ScenarioHints hints) {
        // Buttons: pressed buttons consume the click without swinging
        hints.blockSetup("ButtonBlock", "powered", with(BlockStateProperties.POWERED, true));
        hints.blockItems("ButtonBlock", new ItemStack(Items.STICK));
        hints.blockSetup("LeverBlock", "powered", with(BlockStateProperties.POWERED, true));

        // Doors and trapdoors: iron ones cannot be opened by hand (the harness tests every door/trapdoor block)
        hints.blockSetup("DoorBlock", "open", with(BlockStateProperties.OPEN, true));
        hints.blockSetup("DoorBlock", "upper_half", with(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
        hints.blockSetup("TrapDoorBlock", "open", with(BlockStateProperties.OPEN, true));
        hints.blockSetup("FenceGateBlock", "open", with(BlockStateProperties.OPEN, true));
        hints.blockSetup("FenceGateBlock", "facing_east", with(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));

        // Fences pass on the client; leads are handled by the item
        hints.blockItems("FenceBlock", new ItemStack(Items.LEAD), new ItemStack(Items.STICK));

        // Note blocks: mob heads on the top face skip the note block, other items and faces tune it
        hints.blockItems("NoteBlock", new ItemStack(Items.ZOMBIE_HEAD), new ItemStack(Items.PLAYER_HEAD),
            new ItemStack(Items.SKELETON_SKULL), new ItemStack(Items.DRAGON_HEAD), new ItemStack(Items.STICK),
            new ItemStack(Items.STONE));

        // Redstone components that need mayBuild (game modes are covered by the generic scenarios)
        hints.blockSetup("ComparatorBlock", "subtract", with(BlockStateProperties.MODE_COMPARATOR, ComparatorMode.SUBTRACT));
        hints.blockSetup("RepeaterBlock", "delay_4", with(BlockStateProperties.DELAY, 4));
        hints.blockSetup("RepeaterBlock", "locked", with(BlockStateProperties.LOCKED, true));
        hints.blockSetup("DaylightDetectorBlock", "inverted", with(BlockStateProperties.INVERTED, true));

        // Bells: whether the click rings depends on attachment, facing, clicked face and click height
        hints.blockSetup("BellBlock", "floor_east", bell(BellAttachType.FLOOR, Direction.EAST));
        hints.blockSetup("BellBlock", "ceiling", bell(BellAttachType.CEILING, Direction.NORTH));
        hints.blockSetup("BellBlock", "single_wall_north", bell(BellAttachType.SINGLE_WALL, Direction.NORTH));
        hints.blockSetup("BellBlock", "double_wall_east", bell(BellAttachType.DOUBLE_WALL, Direction.EAST));

        // Server-side only checks, the client always succeeds
        hints.blockSetup("ChestBlock", "blocked", (level, pos) -> level.setBlock(pos.above(), Blocks.STONE.defaultBlockState(), FLAGS));
        hints.blockSetup("EnderChestBlock", "blocked", (level, pos) -> level.setBlock(pos.above(), Blocks.STONE.defaultBlockState(), FLAGS));
        hints.blockSetup("ShulkerBoxBlock", "blocked", (level, pos) -> level.setBlock(pos.above(), Blocks.STONE.defaultBlockState(), FLAGS));
        hints.blockSetup("BedBlock", "head", with(BlockStateProperties.BED_PART, BedPart.HEAD));
        hints.blockSetup("BedBlock", "occupied", with(BlockStateProperties.OCCUPIED, true));

        registerRedstoneWire(hints);
    }

    private static void registerRedstoneWire(ScenarioHints hints) {
        RedstoneSide none = RedstoneSide.NONE;
        RedstoneSide side = RedstoneSide.SIDE;
        RedstoneSide up = RedstoneSide.UP;

        // Dots always toggle; lines never toggle; a cross only toggles if it is not connected on all sides
        hints.blockSetup("RedStoneWireBlock", "dot", (level, pos) -> setWire(level, pos, none, none, none, none));
        hints.blockSetup("RedStoneWireBlock", "line", (level, pos) -> setWire(level, pos, side, none, side, none));
        hints.blockSetup("RedStoneWireBlock", "corner", (level, pos) -> setWire(level, pos, side, side, none, none));
        hints.blockSetup("RedStoneWireBlock", "cross_isolated", (level, pos) -> setWire(level, pos, side, side, side, side));
        hints.blockSetup("RedStoneWireBlock", "cross_wire_three_sides", (level, pos) -> {
            for (int i = 0; i < 3; i++) {
                placeWire(level, pos.relative(HORIZONTAL[i]));
            }
            setWire(level, pos, side, side, side, side);
        });
        hints.blockSetup("RedStoneWireBlock", "cross_wire_all_sides", (level, pos) -> {
            for (Direction direction : HORIZONTAL) {
                placeWire(level, pos.relative(direction));
            }
            setWire(level, pos, side, side, side, side);
        });
        hints.blockSetup("RedStoneWireBlock", "cross_wire_below", (level, pos) -> {
            for (Direction direction : HORIZONTAL) {
                BlockPos relative = pos.relative(direction);
                level.setBlock(relative, Blocks.AIR.defaultBlockState(), FLAGS);
                placeWire(level, relative.below());
            }
            setWire(level, pos, side, side, side, side);
        });
        hints.blockSetup("RedStoneWireBlock", "cross_wire_below_under_stone", (level, pos) -> {
            // The neighbours conduct redstone, so the wire cannot go down
            for (Direction direction : HORIZONTAL) {
                BlockPos relative = pos.relative(direction);
                level.setBlock(relative, Blocks.STONE.defaultBlockState(), FLAGS);
                placeWire(level, relative.below());
            }
            setWire(level, pos, side, side, side, side);
        });
        hints.blockSetup("RedStoneWireBlock", "cross_wire_above", (level, pos) ->
            wireAbove(level, pos, Blocks.STONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), up));
        hints.blockSetup("RedStoneWireBlock", "cross_wire_above_covered_by_stone", (level, pos) ->
            wireAbove(level, pos, Blocks.STONE.defaultBlockState(), Blocks.STONE.defaultBlockState(), up));
        hints.blockSetup("RedStoneWireBlock", "cross_wire_above_covered_by_glass", (level, pos) ->
            wireAbove(level, pos, Blocks.STONE.defaultBlockState(), Blocks.GLASS.defaultBlockState(), up));
        hints.blockSetup("RedStoneWireBlock", "cross_wire_above_top_slab", (level, pos) ->
            wireAbove(level, pos, Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP), Blocks.AIR.defaultBlockState(), side));
        hints.blockSetup("RedStoneWireBlock", "cross_wire_above_bottom_slab", (level, pos) ->
            wireAbove(level, pos, Blocks.STONE_SLAB.defaultBlockState(), Blocks.AIR.defaultBlockState(), side));
        hints.blockSetup("RedStoneWireBlock", "cross_wire_above_glass", (level, pos) ->
            wireAbove(level, pos, Blocks.GLASS.defaultBlockState(), Blocks.AIR.defaultBlockState(), up));
        hints.blockSetup("RedStoneWireBlock", "cross_wire_above_trapdoor", (level, pos) ->
            wireAbove(level, pos, Blocks.OAK_TRAPDOOR.defaultBlockState(), Blocks.AIR.defaultBlockState(), side));
        hints.blockSetup("RedStoneWireBlock", "cross_wire_above_hopper", (level, pos) ->
            wireAbove(level, pos, Blocks.HOPPER.defaultBlockState(), Blocks.AIR.defaultBlockState(), side));
        hints.blockSetup("RedStoneWireBlock", "cross_wire_above_leaves", (level, pos) ->
            wireAbove(level, pos, Blocks.OAK_LEAVES.defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true),
                Blocks.AIR.defaultBlockState(), side));
        hints.blockSetup("RedStoneWireBlock", "cross_signal_sources", (level, pos) -> {
            // Repeater in line, observer facing away from the wire, lever and redstone torch
            level.setBlock(pos.north(), Blocks.REPEATER.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH), FLAGS);
            level.setBlock(pos.east(), Blocks.OBSERVER.defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.EAST), FLAGS);
            level.setBlock(pos.south(), Blocks.LEVER.defaultBlockState(), FLAGS);
            level.setBlock(pos.west(), Blocks.REDSTONE_TORCH.defaultBlockState(), FLAGS);
            setWire(level, pos, side, side, side, side);
        });
        hints.blockSetup("RedStoneWireBlock", "cross_signal_sources_misaligned", (level, pos) -> {
            // Repeater across the wire and observer facing the wire do not connect
            level.setBlock(pos.north(), Blocks.REPEATER.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST), FLAGS);
            level.setBlock(pos.east(), Blocks.OBSERVER.defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.WEST), FLAGS);
            level.setBlock(pos.south(), Blocks.STONE_BUTTON.defaultBlockState(), FLAGS);
            level.setBlock(pos.west(), Blocks.TARGET.defaultBlockState(), FLAGS);
            setWire(level, pos, side, side, side, side);
        });
        hints.blockSetup("RedStoneWireBlock", "cross_more_signal_sources", (level, pos) -> {
            level.setBlock(pos.north(), Blocks.COMPARATOR.defaultBlockState(), FLAGS);
            level.setBlock(pos.east(), net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.withDefaultNamespace("lightning_rod")).defaultBlockState(), FLAGS);
            level.setBlock(pos.south(), Blocks.OAK_PRESSURE_PLATE.defaultBlockState(), FLAGS);
            level.setBlock(pos.west(), Blocks.DAYLIGHT_DETECTOR.defaultBlockState(), FLAGS);
            setWire(level, pos, side, side, side, side);
        });
        hints.blockSetup("RedStoneWireBlock", "cross_non_sources", (level, pos) -> {
            level.setBlock(pos.north(), Blocks.REDSTONE_LAMP.defaultBlockState(), FLAGS);
            level.setBlock(pos.east(), Blocks.REDSTONE_WIRE.defaultBlockState(), FLAGS);
            level.setBlock(pos.south(), Blocks.NOTE_BLOCK.defaultBlockState(), FLAGS);
            level.setBlock(pos.west(), Blocks.REDSTONE_WIRE.defaultBlockState(), FLAGS);
            setWire(level, pos, side, side, side, side);
        });
    }

    private static <T extends Comparable<T>> ScenarioHints.BlockSetup with(Property<T> property, T value) {
        return (level, pos) -> {
            BlockState state = level.getBlockState(pos);
            if (state.hasProperty(property)) {
                level.setBlock(pos, state.setValue(property, value), FLAGS);
            }
        };
    }

    private static ScenarioHints.BlockSetup bell(BellAttachType attachment, Direction facing) {
        return (level, pos) -> level.setBlock(pos, level.getBlockState(pos)
            .setValue(BlockStateProperties.BELL_ATTACHMENT, attachment)
            .setValue(BlockStateProperties.HORIZONTAL_FACING, facing), FLAGS);
    }

    /**
     * Surrounds the wire with the given block, with redstone wire on top of it, and covers the wire with the given block.
     */
    private static void wireAbove(ServerLevel level, BlockPos pos, BlockState neighbour, BlockState cover, RedstoneSide expectedSide) {
        level.setBlock(pos.above(), cover, FLAGS);
        for (Direction direction : HORIZONTAL) {
            BlockPos relative = pos.relative(direction);
            level.setBlock(relative, neighbour, FLAGS);
            level.setBlock(relative.above(), Blocks.REDSTONE_WIRE.defaultBlockState(), FLAGS);
        }
        setWire(level, pos, expectedSide, expectedSide, expectedSide, expectedSide);
    }

    private static void placeWire(ServerLevel level, BlockPos pos) {
        level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), FLAGS);
        level.setBlock(pos, Blocks.REDSTONE_WIRE.defaultBlockState(), FLAGS);
    }

    private static void setWire(ServerLevel level, BlockPos pos, RedstoneSide north, RedstoneSide east, RedstoneSide south, RedstoneSide west) {
        level.setBlock(pos, Blocks.REDSTONE_WIRE.defaultBlockState()
            .setValue(BlockStateProperties.NORTH_REDSTONE, north)
            .setValue(BlockStateProperties.EAST_REDSTONE, east)
            .setValue(BlockStateProperties.SOUTH_REDSTONE, south)
            .setValue(BlockStateProperties.WEST_REDSTONE, west), FLAGS);
    }

    private UtilityBlockHints() {
    }
}
