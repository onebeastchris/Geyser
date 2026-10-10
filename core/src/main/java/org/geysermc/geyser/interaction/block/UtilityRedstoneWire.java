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
import org.geysermc.geyser.interaction.UseOnContext;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.BasicEnumProperty;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.block.type.TrapDoorBlock;
import org.geysermc.geyser.level.physics.Axis;
import org.geysermc.geyser.level.physics.BoundingBox;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.session.cache.tags.BlockTag;
import org.geysermc.geyser.translator.collision.BlockCollision;
import org.geysermc.geyser.util.BlockUtils;
import org.geysermc.geyser.util.InteractionResult;

import java.util.Arrays;

/**
 * Client-side prediction of toggling redstone wire between a dot and a cross ({@code RedStoneWireBlock#useWithoutItem}).
 * <p>
 * Vanilla only succeeds if the toggle changes the wire's state, which depends on the wire's connections to its
 * neighbours. Those are recomputed here from the world, using Geyser's collision data where vanilla uses block shapes
 * and a list of blocks where vanilla uses per-block properties.
 */
final class UtilityRedstoneWire {
    private static final double EPSILON = 1.0E-7;
    private static final BoundingBox[] NO_BOXES = new BoundingBox[0];

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
        boolean canConnectUp = !isRedstoneConductor(player, player.blockAt(pos.add(0, 1, 0)));
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
            && (relativeState.block() instanceof TrapDoorBlock || canSurviveOn(player, relativeState))) {
            // Wire one block higher
            return true;
        }

        if (shouldConnectTo(player, relativeState, direction)) {
            return true;
        }
        // Wire one block lower
        return !isRedstoneConductor(player, relativeState) && player.blockAt(relativePos.add(0, -1, 0)).is(Blocks.REDSTONE_WIRE);
    }

    /**
     * Mirrors {@code RedStoneWireBlock#shouldConnectTo} with a horizontal direction.
     */
    private static boolean shouldConnectTo(InteractionContext player, BlockState state, Direction direction) {
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
        return isSignalSource(player, block);
    }

    /**
     * Blocks overriding {@code BlockBehaviour#isSignalSource} to return true. Redstone wire, repeaters and observers
     * are handled by {@link #shouldConnectTo(InteractionContext, BlockState, Direction)}.
     */
    private static boolean isSignalSource(InteractionContext player, Block block) {
        if (block == Blocks.REDSTONE_TORCH || block == Blocks.REDSTONE_WALL_TORCH || block == Blocks.REDSTONE_BLOCK
            || block == Blocks.LEVER || block == Blocks.COMPARATOR || block == Blocks.DAYLIGHT_DETECTOR
            || block == Blocks.DETECTOR_RAIL || block == Blocks.TRAPPED_CHEST || block == Blocks.TRIPWIRE_HOOK
            || block == Blocks.TARGET || block == Blocks.LECTERN || block == Blocks.JUKEBOX
            || block == Blocks.SCULK_SENSOR || block == Blocks.CALIBRATED_SCULK_SENSOR) {
            return true;
        }
        int id = block.javaId();
        return player.isInTag(BlockTag.BUTTONS, id) || player.isInTag(BlockTag.PRESSURE_PLATES, id)
            || player.isInTag(BlockTag.LIGHTNING_RODS, id);
    }

    /**
     * Mirrors {@code BlockStateBase#isRedstoneConductor}: by default, blocks with a full collision shape conduct.
     * Approximation: only the best known per-block overrides are mirrored.
     */
    private static boolean isRedstoneConductor(InteractionContext player, BlockState state) {
        Block block = state.block();
        if (block == Blocks.SOUL_SAND || block == Blocks.MUD) {
            return true;
        }
        if (block == Blocks.PISTON || block == Blocks.STICKY_PISTON || block == Blocks.OBSERVER
            || block == Blocks.REDSTONE_BLOCK || block == Blocks.BEACON || player.isInTag(BlockTag.LEAVES, block.javaId())) {
            return false;
        }
        String name = block.javaIdentifier().value();
        if (name.endsWith("glass") || name.endsWith("copper_grate")) {
            // Glass, stained glass, tinted glass and copper grates
            return false;
        }

        BoundingBox[] boxes = collisionBoxes(state);
        return boxes.length == 1 && isFullCube(boxes[0]);
    }

    /**
     * Mirrors {@code RedStoneWireBlock#canSurviveOn}: the top face is sturdy, or the block is a hopper.
     */
    private static boolean canSurviveOn(InteractionContext player, BlockState state) {
        Block block = state.block();
        if (block == Blocks.HOPPER || block == Blocks.SOUL_SAND || block == Blocks.MUD) {
            // Soul sand and mud have a full support shape despite their smaller collision
            return true;
        }
        if (block == Blocks.SNOW) {
            // The support shape of snow is one layer higher than its collision
            return state.getValue(Properties.LAYERS, 1) == 8;
        }
        if (player.isInTag(BlockTag.LEAVES, block.javaId())) {
            // Leaves have an empty support shape
            return false;
        }
        return isTopFaceFull(collisionBoxes(state));
    }

    private static BoundingBox[] collisionBoxes(BlockState state) {
        BlockCollision collision = BlockUtils.getCollision(state.javaId());
        return collision == null ? NO_BOXES : collision.getBoundingBoxes();
    }

    private static boolean isFullCube(BoundingBox box) {
        return box.getMin(Axis.X) <= EPSILON
            && box.getMin(Axis.Y) <= EPSILON
            && box.getMin(Axis.Z) <= EPSILON
            && box.getMax(Axis.X) >= 1 - EPSILON
            && box.getMax(Axis.Y) >= 1 - EPSILON
            && box.getMax(Axis.Z) >= 1 - EPSILON;
    }

    /**
     * Approximates {@code BlockStateBase#isFaceSturdy(level, pos, Direction.UP)} using collision boxes: whether the
     * boxes reaching the top of the block cover the whole top face.
     */
    private static boolean isTopFaceFull(BoundingBox[] boxes) {
        if (boxes.length == 0) {
            return false;
        }
        if (boxes.length == 1) {
            BoundingBox box = boxes[0];
            return reachesTop(box)
                && box.getMin(Axis.X) <= EPSILON && box.getMax(Axis.X) >= 1 - EPSILON
                && box.getMin(Axis.Z) <= EPSILON && box.getMax(Axis.Z) >= 1 - EPSILON;
        }

        // Several boxes: split the top face into cells along every box edge and check that each cell is covered
        double[] xs = new double[boxes.length * 2 + 2];
        double[] zs = new double[boxes.length * 2 + 2];
        int count = 0;
        xs[count] = 0;
        zs[count++] = 0;
        xs[count] = 1;
        zs[count++] = 1;
        for (BoundingBox box : boxes) {
            if (reachesTop(box)) {
                xs[count] = clamp(box.getMin(Axis.X));
                zs[count++] = clamp(box.getMin(Axis.Z));
                xs[count] = clamp(box.getMax(Axis.X));
                zs[count++] = clamp(box.getMax(Axis.Z));
            }
        }
        Arrays.sort(xs, 0, count);
        Arrays.sort(zs, 0, count);

        for (int i = 0; i < count - 1; i++) {
            if (xs[i + 1] - xs[i] <= EPSILON) {
                continue;
            }
            double x = (xs[i] + xs[i + 1]) / 2;
            for (int j = 0; j < count - 1; j++) {
                if (zs[j + 1] - zs[j] <= EPSILON) {
                    continue;
                }
                double z = (zs[j] + zs[j + 1]) / 2;
                if (!isCoveredOnTop(boxes, x, z)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isCoveredOnTop(BoundingBox[] boxes, double x, double z) {
        for (BoundingBox box : boxes) {
            if (reachesTop(box)
                && box.getMin(Axis.X) <= x && x <= box.getMax(Axis.X)
                && box.getMin(Axis.Z) <= z && z <= box.getMax(Axis.Z)) {
                return true;
            }
        }
        return false;
    }

    private static boolean reachesTop(BoundingBox box) {
        return box.getMax(Axis.Y) >= 1 - EPSILON
            && box.getMin(Axis.Y) < 1 - EPSILON;
    }

    private static double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }

    private UtilityRedstoneWire() {
    }
}
