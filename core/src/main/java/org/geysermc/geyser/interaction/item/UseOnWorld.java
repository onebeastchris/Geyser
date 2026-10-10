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

package org.geysermc.geyser.interaction.item;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.interaction.InteractionContext;
import org.geysermc.geyser.interaction.InteractionTables;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.Axis;
import org.geysermc.geyser.level.physics.BoundingBox;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.registry.BlockRegistries;
import org.geysermc.geyser.session.cache.tags.BlockTag;
import org.geysermc.geyser.session.cache.tags.Tag;
import org.geysermc.geyser.translator.collision.BlockCollision;
import org.geysermc.geyser.util.BlockUtils;

/**
 * Small world queries shared by the {@code Item#useOn} predictions. Some of them approximate vanilla block
 * properties Geyser does not know (such as sturdy faces) using the block collision boxes Geyser does know.
 */
final class UseOnWorld {
    private static final double EPSILON = 1.0E-7;

    /**
     * Mirrors {@code BlockState#isAir}.
     */
    static boolean isAir(@NonNull BlockState state) {
        Block block = state.block();
        return block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR;
    }

    static @NonNull BlockState above(@NonNull InteractionContext player, @NonNull Vector3i pos) {
        return player.blockAt(pos.add(0, 1, 0));
    }

    static @NonNull BlockState below(@NonNull InteractionContext player, @NonNull Vector3i pos) {
        return player.blockAt(pos.add(0, -1, 0));
    }

    static boolean isInTag(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Tag<Block> tag) {
        return player.isInTag(tag, state.block().javaId());
    }

    /**
     * Mirrors {@code LevelHeightAccessor#isOutsideBuildHeight}.
     */
    static boolean isOutsideBuildHeight(@NonNull InteractionContext player, int y) {
        return y < player.minY() || y > player.maxY();
    }

    /**
     * Mirrors {@code state.is(Blocks.WATER) && fluidState.isFull()}: a water source or falling water.
     */
    static boolean isFullWater(@NonNull BlockState state) {
        if (!state.is(Blocks.WATER)) {
            return false;
        }
        int level = state.getValue(Properties.LEVEL, 0);
        return level == 0 || level >= 8;
    }

    /**
     * Approximates {@code FluidState#isSource}: a water source block, or any waterlogged state.
     */
    static boolean isWaterSource(@NonNull BlockState state) {
        if (state.is(Blocks.WATER)) {
            return state.getValue(Properties.LEVEL, 0) == 0;
        }
        return BlockRegistries.WATERLOGGED.get().get(state.javaId());
    }

    /**
     * Approximates {@code FluidState#isEmpty}.
     */
    static boolean hasNoFluid(@NonNull BlockState state) {
        return !state.is(Blocks.WATER) && !state.is(Blocks.LAVA) && !BlockRegistries.WATERLOGGED.get().get(state.javaId());
    }

    /**
     * Mirrors {@code BlockState#isFaceSturdy} (full support type), from the generated interaction tables.
     */
    static boolean isFaceSturdy(@NonNull BlockState state, @NonNull Direction face) {
        return InteractionTables.isFaceSturdy(state, face);
    }

    /**
     * Mirrors the legacy "solid" flag ({@code BlockState#isSolid}), from the generated interaction tables.
     */
    static boolean isSolid(@NonNull BlockState state) {
        return InteractionTables.StateFlag.SOLID.test(state);
    }

    /**
     * Approximates the block part of {@code CollisionGetter#noCollision}: whether any block collision box intersects
     * the given box (in world coordinates). Like vanilla, the blocks around the box are checked as well, for collision
     * shapes taller than a block (fences, walls).
     */
    static boolean hasBlockCollision(@NonNull InteractionContext player, @NonNull BoundingBox box) {
        int minX = (int) Math.floor(box.getMin(Axis.X) - EPSILON) - 1;
        int minY = (int) Math.floor(box.getMin(Axis.Y) - EPSILON) - 1;
        int minZ = (int) Math.floor(box.getMin(Axis.Z) - EPSILON) - 1;
        int maxX = (int) Math.floor(box.getMax(Axis.X) + EPSILON) + 1;
        int maxY = (int) Math.floor(box.getMax(Axis.Y) + EPSILON) + 1;
        int maxZ = (int) Math.floor(box.getMax(Axis.Z) + EPSILON) + 1;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockCollision collision = BlockUtils.getCollision(player.blockAt(Vector3i.from(x, y, z)).javaId());
                    if (collision != null && collision.checkIntersection(x, y, z, box)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Mirrors {@code Direction#fromYRot}, as used by {@code Entity#getDirection}.
     */
    static @NonNull Direction horizontalDirection(float yaw) {
        return switch ((int) Math.floor(yaw / 90.0 + 0.5) & 3) {
            case 0 -> Direction.SOUTH;
            case 1 -> Direction.WEST;
            case 2 -> Direction.NORTH;
            default -> Direction.EAST;
        };
    }

    /**
     * Approximates {@code BaseFireBlock#canBePlacedAt}: the position must be air, and fire must survive there. That is the
     * case on soul fire base blocks, on top of a sturdy face, or next to a flammable block ({@code FireBlock#canSurvive}).
     * Nether portal shapes are not detected (lighting one usually happens on top of a sturdy obsidian face anyway).
     */
    static boolean canPlaceFireAt(@NonNull InteractionContext player, @NonNull Vector3i pos) {
        if (!isAir(player.blockAt(pos))) {
            return false;
        }
        BlockState below = below(player, pos);
        if (isInTag(player, below, BlockTag.SOUL_FIRE_BASE_BLOCKS) || isFaceSturdy(below, Direction.UP)) {
            return true;
        }
        for (Direction direction : Direction.VALUES) {
            if (InteractionTables.StateFlag.FLAMMABLE.test(player.blockAt(pos.add(direction.getUnitVector())))) {
                return true;
            }
        }
        return false;
    }

    private UseOnWorld() {
    }
}
