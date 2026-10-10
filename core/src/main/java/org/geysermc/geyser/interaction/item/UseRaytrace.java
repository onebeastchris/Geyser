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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.math.GenericMath;
import org.cloudburstmc.math.vector.Vector3d;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.interaction.BlockHit;
import org.geysermc.geyser.interaction.InteractionContext;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.BoundingBox;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.registry.BlockRegistries;

/**
 * Mirrors {@code Item#getPlayerPOVHitResult}: a raytrace from the player's eyes along their view, as far as their
 * block interaction range, against block outline shapes and optionally fluids ({@code BlockGetter#clip} with
 * {@code ClipContext.Block.OUTLINE}).
 * <p>
 * Approximations: block shapes come from {@link BlockRegistries#SHAPES}, so random per-position offsets (flowers,
 * grass, ...) and context dependent shapes are not taken into account, interaction shape overrides of the hit face
 * ({@code BlockGetter#clipWithInteractionOverride}) are ignored, and fluid heights are computed per position instead
 * of being cached per fluid state.
 * <p>
 * One instance is created per raytrace to hold the ray; nothing else is allocated besides block lookups and the result.
 */
final class UseRaytrace {
    private static final double EPSILON = 1.0E-7;

    private final InteractionContext player;
    private final FluidMode fluidMode;

    private final double fromX;
    private final double fromY;
    private final double fromZ;
    private final double deltaX;
    private final double deltaY;
    private final double deltaZ;

    // Scratch state of the shape currently being clipped, mirrors the scale reference of AABB#clip
    private double clipScale;
    private @Nullable Direction clipDirection;

    // Best hit within the block currently being checked
    private double hitScale;
    private @Nullable Direction hitDirection;
    private boolean hitInside;

    /**
     * Mirrors {@code ClipContext.Fluid}.
     */
    enum FluidMode {
        NONE,
        SOURCE_ONLY,
        ANY
    }

    private UseRaytrace(InteractionContext player, FluidMode fluidMode) {
        this.player = player;
        this.fluidMode = fluidMode;

        Vector3d position = player.position();
        this.fromX = position.getX();
        this.fromY = position.getY() + player.eyeHeight();
        this.fromZ = position.getZ();

        // Entity#calculateViewVector, scaled by Player#blockInteractionRange
        float xRad = player.pitch() * ((float) Math.PI / 180.0F);
        float yRad = -player.yaw() * ((float) Math.PI / 180.0F);
        float yCos = (float) Math.cos(yRad);
        float ySin = (float) Math.sin(yRad);
        float xCos = (float) Math.cos(xRad);
        float xSin = (float) Math.sin(xRad);
        double range = player.blockInteractionRange();
        this.deltaX = (ySin * xCos) * range;
        this.deltaY = (-xSin) * range;
        this.deltaZ = (yCos * xCos) * range;
    }

    /**
     * @return the block or fluid hit, or null if the raytrace missed
     */
    static @Nullable BlockHit clip(@NonNull InteractionContext player, @NonNull FluidMode fluidMode) {
        return new UseRaytrace(player, fluidMode).traverse();
    }

    /**
     * Mirrors {@code BlockHitResult#miss} as returned by {@code BlockGetter#clip} when nothing was hit: positioned at
     * the end of the ray, facing back to the player.
     */
    static @NonNull BlockHit miss(@NonNull InteractionContext player) {
        UseRaytrace ray = new UseRaytrace(player, FluidMode.NONE);
        double toX = ray.fromX + ray.deltaX;
        double toY = ray.fromY + ray.deltaY;
        double toZ = ray.fromZ + ray.deltaZ;
        return new BlockHit(Vector3i.from(GenericMath.floor(toX), GenericMath.floor(toY), GenericMath.floor(toZ)),
            approximateNearest(-ray.deltaX, -ray.deltaY, -ray.deltaZ), Vector3d.from(toX, toY, toZ), false);
    }

    /**
     * Mirrors {@code BlockGetter#traverseBlocks}.
     */
    private @Nullable BlockHit traverse() {
        if (deltaX == 0 && deltaY == 0 && deltaZ == 0) {
            return null;
        }
        double toX = fromX + deltaX;
        double toY = fromY + deltaY;
        double toZ = fromZ + deltaZ;

        // Slightly extend the ray on both ends
        double endX = toX - EPSILON * (fromX - toX);
        double endY = toY - EPSILON * (fromY - toY);
        double endZ = toZ - EPSILON * (fromZ - toZ);
        double startX = fromX - EPSILON * (toX - fromX);
        double startY = fromY - EPSILON * (toY - fromY);
        double startZ = fromZ - EPSILON * (toZ - fromZ);

        int blockX = GenericMath.floor(startX);
        int blockY = GenericMath.floor(startY);
        int blockZ = GenericMath.floor(startZ);
        BlockHit hit = clipBlock(blockX, blockY, blockZ);
        if (hit != null) {
            return hit;
        }

        double dx = endX - startX;
        double dy = endY - startY;
        double dz = endZ - startZ;
        int signX = sign(dx);
        int signY = sign(dy);
        int signZ = sign(dz);
        double tDeltaX = signX == 0 ? Double.MAX_VALUE : signX / dx;
        double tDeltaY = signY == 0 ? Double.MAX_VALUE : signY / dy;
        double tDeltaZ = signZ == 0 ? Double.MAX_VALUE : signZ / dz;
        double tX = tDeltaX * (signX > 0 ? 1.0 - frac(startX) : frac(startX));
        double tY = tDeltaY * (signY > 0 ? 1.0 - frac(startY) : frac(startY));
        double tZ = tDeltaZ * (signZ > 0 ? 1.0 - frac(startZ) : frac(startZ));

        while (tX <= 1.0 || tY <= 1.0 || tZ <= 1.0) {
            if (tX < tY) {
                if (tX < tZ) {
                    blockX += signX;
                    tX += tDeltaX;
                } else {
                    blockZ += signZ;
                    tZ += tDeltaZ;
                }
            } else if (tY < tZ) {
                blockY += signY;
                tY += tDeltaY;
            } else {
                blockZ += signZ;
                tZ += tDeltaZ;
            }

            hit = clipBlock(blockX, blockY, blockZ);
            if (hit != null) {
                return hit;
            }
        }
        return null;
    }

    /**
     * Clips the block outline and the fluid at a position, and returns the closer hit of both.
     */
    private @Nullable BlockHit clipBlock(int x, int y, int z) {
        Vector3i position = Vector3i.from(x, y, z);
        BlockState state = player.blockAt(position);

        hitScale = Double.MAX_VALUE;
        hitDirection = null;
        hitInside = false;

        BoundingBox[] shape = BlockRegistries.SHAPES.get(state.javaId());
        if (shape != null && shape.length > 0) {
            clipShape(shape, 0, x, y, z);
        }

        if (fluidMode != FluidMode.NONE) {
            int fluid = fluidType(state);
            if (fluid != NO_FLUID && (fluidMode == FluidMode.ANY || isSourceFluid(state))) {
                BlockState above = player.blockAt(position.up());
                double height = fluidType(above) == fluid ? 1.0 : fluidAmount(state) / 9.0F;
                clipShape(null, height, x, y, z);
            }
        }

        if (hitDirection == null) {
            return null;
        }
        return new BlockHit(position, hitDirection,
            Vector3d.from(fromX + hitScale * deltaX, fromY + hitScale * deltaY, fromZ + hitScale * deltaZ), hitInside);
    }

    /**
     * Mirrors {@code VoxelShape#clip}, either for outline boxes or, if {@code boxes} is null, for a fluid box of the
     * given height. Updates the best hit of the current block if this shape is hit closer.
     */
    private void clipShape(BoundingBox @Nullable [] boxes, double fluidHeight, int x, int y, int z) {
        if (deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ < EPSILON) {
            return;
        }

        // The ray starts inside the shape
        double testX = fromX + deltaX * 0.001 - x;
        double testY = fromY + deltaY * 0.001 - y;
        double testZ = fromZ + deltaZ * 0.001 - z;
        boolean inside = false;
        if (boxes != null) {
            for (BoundingBox box : boxes) {
                if (contains(minX(box), maxX(box), testX) && contains(minY(box), maxY(box), testY) && contains(minZ(box), maxZ(box), testZ)) {
                    inside = true;
                    break;
                }
            }
        } else {
            inside = contains(0, 1, testX) && contains(0, fluidHeight, testY) && contains(0, 1, testZ);
        }
        if (inside) {
            accept(0.001, approximateNearest(deltaX, deltaY, deltaZ).reversed(), true);
            return;
        }

        // AABB#clip
        clipScale = 1.0;
        clipDirection = null;
        if (boxes != null) {
            for (BoundingBox box : boxes) {
                clipBox(x + minX(box), y + minY(box), z + minZ(box), x + maxX(box), y + maxY(box), z + maxZ(box));
            }
        } else {
            clipBox(x, y, z, x + 1, y + fluidHeight, z + 1);
        }
        if (clipDirection != null) {
            accept(clipScale, clipDirection, false);
        }
    }

    private static double minX(BoundingBox box) {
        return box.getMiddleX() - box.getSizeX() / 2;
    }

    private static double maxX(BoundingBox box) {
        return box.getMiddleX() + box.getSizeX() / 2;
    }

    private static double minY(BoundingBox box) {
        return box.getMiddleY() - box.getSizeY() / 2;
    }

    private static double maxY(BoundingBox box) {
        return box.getMiddleY() + box.getSizeY() / 2;
    }

    private static double minZ(BoundingBox box) {
        return box.getMiddleZ() - box.getSizeZ() / 2;
    }

    private static double maxZ(BoundingBox box) {
        return box.getMiddleZ() + box.getSizeZ() / 2;
    }

    private void accept(double scale, Direction direction, boolean inside) {
        // BlockGetter#clip prefers the block outline hit if both are equally far away; the outline is always clipped first
        if (hitDirection == null || scale < hitScale) {
            hitScale = scale;
            hitDirection = direction;
            hitInside = inside;
        }
    }

    /**
     * Mirrors {@code AABB#getDirection}.
     */
    private void clipBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        if (deltaX > EPSILON) {
            clipPoint(deltaX, deltaY, deltaZ, minX, minY, maxY, minZ, maxZ, Direction.WEST, fromX, fromY, fromZ);
        } else if (deltaX < -EPSILON) {
            clipPoint(deltaX, deltaY, deltaZ, maxX, minY, maxY, minZ, maxZ, Direction.EAST, fromX, fromY, fromZ);
        }

        if (deltaY > EPSILON) {
            clipPoint(deltaY, deltaZ, deltaX, minY, minZ, maxZ, minX, maxX, Direction.DOWN, fromY, fromZ, fromX);
        } else if (deltaY < -EPSILON) {
            clipPoint(deltaY, deltaZ, deltaX, maxY, minZ, maxZ, minX, maxX, Direction.UP, fromY, fromZ, fromX);
        }

        if (deltaZ > EPSILON) {
            clipPoint(deltaZ, deltaX, deltaY, minZ, minX, maxX, minY, maxY, Direction.NORTH, fromZ, fromX, fromY);
        } else if (deltaZ < -EPSILON) {
            clipPoint(deltaZ, deltaX, deltaY, maxZ, minX, maxX, minY, maxY, Direction.SOUTH, fromZ, fromX, fromY);
        }
    }

    /**
     * Mirrors {@code AABB#clipPoint}.
     */
    private void clipPoint(double da, double db, double dc, double point, double minB, double maxB, double minC, double maxC,
                           Direction direction, double fromA, double fromB, double fromC) {
        double scale = (point - fromA) / da;
        double pb = fromB + scale * db;
        double pc = fromC + scale * dc;
        if (0.0 < scale && scale < clipScale && minB - EPSILON < pb && pb < maxB + EPSILON && minC - EPSILON < pc && pc < maxC + EPSILON) {
            clipScale = scale;
            clipDirection = direction;
        }
    }

    private static boolean contains(double min, double max, double value) {
        return value >= min && value < max;
    }

    private static int sign(double value) {
        return value == 0.0 ? 0 : (value > 0.0 ? 1 : -1);
    }

    private static double frac(double value) {
        return value - Math.floor(value);
    }

    /**
     * Mirrors {@code Direction#getApproximateNearest}.
     */
    static @NonNull Direction approximateNearest(double x, double y, double z) {
        Direction result = Direction.NORTH;
        float highest = Float.MIN_VALUE;
        for (Direction direction : Direction.VALUES) {
            Vector3i normal = direction.getUnitVector();
            float dot = (float) x * normal.getX() + (float) y * normal.getY() + (float) z * normal.getZ();
            if (dot > highest) {
                highest = dot;
                result = direction;
            }
        }
        return result;
    }

    static final int NO_FLUID = 0;
    static final int WATER = 1;
    static final int LAVA = 2;

    /**
     * @return the type of the block's fluid state: {@link #NO_FLUID}, {@link #WATER} or {@link #LAVA}
     */
    static int fluidType(@NonNull BlockState state) {
        if (state.is(Blocks.WATER) || BlockRegistries.WATERLOGGED.get().get(state.javaId())) {
            return WATER;
        }
        return state.is(Blocks.LAVA) ? LAVA : NO_FLUID;
    }

    /**
     * Mirrors {@code FluidState#isSource}.
     */
    static boolean isSourceFluid(@NonNull BlockState state) {
        if (state.is(Blocks.WATER) || state.is(Blocks.LAVA)) {
            return state.getValue(Properties.LEVEL) == 0;
        }
        // Waterlogged blocks, bubble columns, kelp and seagrass contain a water source
        return BlockRegistries.WATERLOGGED.get().get(state.javaId());
    }

    /**
     * Mirrors {@code FluidState#getAmount}: 8 for sources and falling fluids, less for flowing fluids.
     */
    static int fluidAmount(@NonNull BlockState state) {
        if (state.is(Blocks.WATER) || state.is(Blocks.LAVA)) {
            int level = state.getValue(Properties.LEVEL);
            return level == 0 || level >= 8 ? 8 : 8 - level;
        }
        return fluidType(state) == NO_FLUID ? 0 : 8;
    }
}
