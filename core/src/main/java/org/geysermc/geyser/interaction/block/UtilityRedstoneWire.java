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

package org.geysermc.geyser.interaction.block;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.interaction.InteractionContext;
import org.geysermc.geyser.interaction.InteractionTables;
import org.geysermc.geyser.interaction.UseOnContext;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.BasicEnumProperty;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.block.type.TrapDoorBlock;
import org.geysermc.geyser.level.physics.Axis;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.util.InteractionResult;


/**
 * Client-side prediction of toggling redstone wire between a dot and a cross ({@code RedStoneWireBlock#useWithoutItem}).
 * <p>
 * Vanilla only succeeds if the toggle changes the wire's state, which depends on the wire's connections to its
 * neighbours. Those are recomputed here from the world; the per-state block properties involved (conductor, signal
 * source, sturdy top face) come from the generated {@link InteractionTables}.
 */
final class UtilityRedstoneWire {

    static @NonNull InteractionResult useWithoutItem(@NonNull UseOnContext context, @NonNull BlockState state) {
        InteractionContext player = context.player();
        if (!player.mayBuild()) {
            return InteractionResult.PASS;
        }

        boolean north = isConnected(state, Properties.NORTH_REDSTONE);
        boolean east = isConnected(state, Properties.EAST_REDSTONE);
        boolean south = isConnected(state, Properties.SOUTH_REDSTONE);
        boolean west = isConnected(state, Properties.WEST_REDSTONE);
        if (!north && !east && !south && !west) {
            // A dot always turns into a cross or a line, so its state always changes
            return InteractionResult.SUCCESS;
        }
        if (!north || !east || !south || !west) {
            // Neither a dot nor a cross: nothing to toggle
            return InteractionResult.PASS;
        }

        // A cross turns into a dot or a line, unless the wire really connects to all four sides, in which case
        // its recomputed state stays the same cross.
        // Approximation: we only check whether each side connects, and assume the side/up values of a fully connected
        // cross match the neighbours (the server keeps them updated), instead of comparing them too.
        Vector3i pos = context.clickedPos();
        boolean canConnectUp = !isRedstoneConductor(player.blockAt(pos.add(0, 1, 0)));
        for (Direction direction : Direction.HORIZONTAL) {
            if (!connectsTo(player, pos, direction, canConnectUp)) {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private static boolean isConnected(BlockState state, BasicEnumProperty property) {
        String side = state.getValue(property);
        return side != null && !"none".equals(side);
    }

    /**
     * Mirrors {@code RedStoneWireBlock#getConnectingSide}, only returning whether the side is connected.
     */
    private static boolean connectsTo(InteractionContext player, Vector3i pos, Direction direction, boolean canConnectUp) {
        Vector3i relativePos = pos.add(direction.getUnitVector());
        BlockState relativeState = player.blockAt(relativePos);
        if (canConnectUp && player.blockAt(relativePos.add(0, 1, 0)).is(Blocks.REDSTONE_WIRE)
            && (relativeState.block() instanceof TrapDoorBlock || canSurviveOn(relativeState))) {
            // Wire one block higher
            return true;
        }

        if (shouldConnectTo(relativeState, direction)) {
            return true;
        }
        // Wire one block lower
        return !isRedstoneConductor(relativeState) && player.blockAt(relativePos.add(0, -1, 0)).is(Blocks.REDSTONE_WIRE);
    }

    /**
     * Mirrors {@code RedStoneWireBlock#shouldConnectTo} with a horizontal direction.
     */
    private static boolean shouldConnectTo(BlockState state, Direction direction) {
        Block block = state.block();
        if (block == Blocks.REDSTONE_WIRE) {
            return true;
        }
        if (block == Blocks.REPEATER) {
            Direction facing = state.getValue(Properties.HORIZONTAL_FACING);
            return facing != null && facing.getAxis() == direction.getAxis();
        }
        if (block == Blocks.OBSERVER) {
            return state.getValue(Properties.FACING) == direction;
        }
        return InteractionTables.StateFlag.SIGNAL_SOURCE.test(state);
    }

    private static boolean isRedstoneConductor(BlockState state) {
        return InteractionTables.StateFlag.REDSTONE_CONDUCTOR.test(state);
    }

    /**
     * Mirrors {@code RedStoneWireBlock#canSurviveOn}: the top face is sturdy, or the block is a hopper.
     */
    private static boolean canSurviveOn(BlockState state) {
        return state.is(Blocks.HOPPER) || InteractionTables.StateFlag.STURDY_UP.test(state);
    }

    private UtilityRedstoneWire() {
    }
}
