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
     * Mirrors {@code BlockState#isFaceSturdy} (full support type). Exact for the top face, which comes from the generated
     * interaction tables; other faces are approximated by checking whether one of the collision boxes of the state covers
     * the entire face.
     */
    static boolean isFaceSturdy(@NonNull BlockState state, @NonNull Direction face) {
        if (face == Direction.UP) {
            return InteractionTables.StateFlag.STURDY_UP.test(state);
        }
        BlockCollision collision = BlockUtils.getCollision(state.javaId());
        if (collision == null) {
            return false;
        }
        Axis axis = face.getAxis();
        Vector3i normal = face.getUnitVector();
        boolean positive = normal.getX() + normal.getY() + normal.getZ() > 0;
        for (BoundingBox box : collision.getBoundingBoxes()) {
            boolean touchesFace = positive ? box.getMax(axis) >= 1 - EPSILON : box.getMin(axis) <= EPSILON;
            if (touchesFace && coversFace(box, axis)) {
                return true;
            }
        }
        return false;
    }

    private static boolean coversFace(BoundingBox box, Axis faceAxis) {
        for (Axis axis : Axis.VALUES) {
            if (axis != faceAxis && (box.getMin(axis) > EPSILON || box.getMax(axis) < 1 - EPSILON)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Approximates the legacy "solid" flag ({@code BlockState#isSolid}) by checking whether the state has any collision.
     */
    static boolean hasCollision(@NonNull BlockState state) {
        BlockCollision collision = BlockUtils.getCollision(state.javaId());
        return collision != null && collision.getBoundingBoxes().length > 0;
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
     * case on soul fire base blocks, on top of a sturdy face (exact), or next to a flammable block (approximated).
     * <p>
     * Vanilla's flammability table is hardcoded in {@code FireBlock} and not available here, so only a few tags of common
     * flammable blocks are checked. Nether portal shapes are not detected either (lighting one usually happens on top
     * of a sturdy obsidian face anyway). Mispredictions only affect the arm swing ({@code SUCCESS} vs {@code FAIL}).
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
            if (isLikelyFlammable(player, player.blockAt(pos.add(direction.getUnitVector())))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isLikelyFlammable(InteractionContext player, BlockState state) {
        return isInTag(player, state, BlockTag.PLANKS) || isInTag(player, state, BlockTag.LOGS)
            || isInTag(player, state, BlockTag.LEAVES) || isInTag(player, state, BlockTag.WOOL)
            || isInTag(player, state, BlockTag.WOOL_CARPETS);
    }

    private UseOnWorld() {
    }
}
