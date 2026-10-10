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

package org.geysermc.geyser.interaction.placement;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.interaction.InteractionContext;
import org.geysermc.geyser.interaction.InteractionTables;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.BasicEnumProperty;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.session.cache.tags.BlockTag;

import static org.geysermc.geyser.interaction.placement.RulesMZWorld.facing;
import static org.geysermc.geyser.interaction.placement.RulesMZWorld.is;
import static org.geysermc.geyser.interaction.placement.RulesMZWorld.relative;
import static org.geysermc.geyser.interaction.placement.RulesMZWorld.withFacing;
import static org.geysermc.geyser.interaction.placement.RulesMZWorld.withIfPresent;

/**
 * Placement rules of blocks whose placement state depends on where they are clicked or on their neighbours: slabs,
 * stairs, walls, trapdoors, snow layers, scaffolding, speleothems (pointed dripstone), redstone wire, and the blocks
 * that only face the player. For blocks that always survive, the state only matters for its collision, which decides
 * whether an entity is in the way.
 */
public final class RulesMZShapes {

    /**
     * Mirrors the vanilla rule for the half of slabs and stairs: the bottom half when the top face or the lower half of a
     * side was clicked.
     */
    private static boolean placesBottomHalf(PlacementContext context, Vector3i pos) {
        Direction clickedFace = context.clickedFace();
        return clickedFace != Direction.DOWN && (clickedFace == Direction.UP || !RulesMZWorld.clickedUpperHalf(context, pos));
    }

    /*
     * Slabs
     */

    /**
     * {@code SlabBlock#getStateForPlacement}: completes the clicked slab, or places the half that was clicked.
     */
    public static @NonNull BlockState slabStateForPlacement(@NonNull PlacementContext context, @NonNull Block block) {
        Vector3i pos = context.clickedPos();
        BlockState replaced = context.blockAt(pos);
        if (replaced.is(block)) {
            return withIfPresent(replaced, Properties.SLAB_TYPE, "double");
        }
        return withIfPresent(InteractionTables.defaultState(block), Properties.SLAB_TYPE, placesBottomHalf(context, pos) ? "bottom" : "top");
    }

    /**
     * {@code SlabBlock#canBeReplaced}: a slab can be completed with the same item, from the side it is missing.
     */
    public static boolean slabCanBeReplaced(@NonNull PlacementContext context, @NonNull BlockState state, boolean replacingClickedOnBlock) {
        String type = state.getValue(Properties.SLAB_TYPE);
        if (type == null || "double".equals(type) || !context.itemInHand().is(state.block().asItem())) {
            return false;
        }
        if (!replacingClickedOnBlock) {
            return true;
        }
        boolean upperHalf = RulesMZWorld.clickedUpperHalf(context, RulesMZWorld.clickedPos(context, true));
        Direction clickedFace = context.clickedFace();
        if ("bottom".equals(type)) {
            return clickedFace == Direction.UP || upperHalf && clickedFace.isHorizontal();
        }
        return clickedFace == Direction.DOWN || !upperHalf && clickedFace.isHorizontal();
    }

    /*
     * Stairs
     */

    private static boolean isStairs(BlockState state) {
        return state.getValue(Properties.STAIRS_SHAPE) != null;
    }

    /**
     * {@code StairBlock#getStateForPlacement}: facing the player, with the clicked half and the shape from the
     * neighbouring stairs.
     */
    public static @NonNull BlockState stairStateForPlacement(@NonNull PlacementContext context, @NonNull Block block) {
        Vector3i pos = context.clickedPos();
        BlockState state = withFacing(InteractionTables.defaultState(block), context.horizontalDirection());
        state = withIfPresent(state, Properties.HALF, placesBottomHalf(context, pos) ? "bottom" : "top");
        return withIfPresent(state, Properties.STAIRS_SHAPE, stairsShape(context.player(), state, pos));
    }

    /**
     * {@code StairBlock#getStairsShape}.
     * TODO verify against 26.2: reconstructed
     */
    private static String stairsShape(InteractionContext player, BlockState state, Vector3i pos) {
        Direction facing = facing(state);
        String half = state.getValue(Properties.HALF);
        if (facing == null || half == null || !facing.isHorizontal()) {
            return "straight";
        }
        BlockState behind = player.blockAt(relative(pos, facing));
        if (isStairs(behind) && half.equals(behind.getValue(Properties.HALF))) {
            Direction behindFacing = facing(behind);
            if (behindFacing != null && behindFacing.isHorizontal() && behindFacing.getAxis() != facing.getAxis()
                && canTakeShape(player, facing, half, pos, behindFacing.reversed())) {
                return behindFacing == facing.counterClockWise() ? "outer_left" : "outer_right";
            }
        }
        BlockState front = player.blockAt(relative(pos, facing.reversed()));
        if (isStairs(front) && half.equals(front.getValue(Properties.HALF))) {
            Direction frontFacing = facing(front);
            if (frontFacing != null && frontFacing.isHorizontal() && frontFacing.getAxis() != facing.getAxis()
                && canTakeShape(player, facing, half, pos, frontFacing)) {
                return frontFacing == facing.counterClockWise() ? "inner_left" : "inner_right";
            }
        }
        return "straight";
    }

    private static boolean canTakeShape(InteractionContext player, Direction facing, String half, Vector3i pos, Direction neighbour) {
        BlockState neighbourState = player.blockAt(relative(pos, neighbour));
        return !isStairs(neighbourState) || facing(neighbourState) != facing || !half.equals(neighbourState.getValue(Properties.HALF));
    }

    /*
     * Walls
     */

    private static final BasicEnumProperty[] WALL_SIDES = {Properties.NORTH_WALL, Properties.EAST_WALL, Properties.SOUTH_WALL, Properties.WEST_WALL};
    private static final Direction[] WALL_DIRECTIONS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    /**
     * {@code WallBlock#getStateForPlacement}: connections to the neighbours and the post. Only the parts that change the
     * collision are modelled: whether a side connects (low and tall sides collide alike) and whether there is a post.
     * TODO verify against 26.2: WallBlock#connectsTo/updateShape are reconstructed; the shape of the block above is not
     * known, so posts raised by a partially covering block above are approximated by the wall_post_override tag.
     */
    public static @NonNull BlockState wallStateForPlacement(@NonNull PlacementContext context, @NonNull Block block) {
        InteractionContext player = context.player();
        Vector3i pos = context.clickedPos();
        BlockState state = InteractionTables.defaultState(block);
        boolean[] connected = new boolean[4];
        for (int i = 0; i < 4; i++) {
            Direction direction = WALL_DIRECTIONS[i];
            BlockState neighbour = player.blockAt(relative(pos, direction));
            connected[i] = wallConnectsTo(player, neighbour, direction.reversed());
            state = withIfPresent(state, WALL_SIDES[i], connected[i] ? "low" : "none");
        }
        boolean north = connected[0];
        boolean east = connected[1];
        boolean south = connected[2];
        boolean west = connected[3];
        BlockState above = RulesMZWorld.above(player, pos);
        boolean post;
        if (above.getValue(Properties.NORTH_WALL) != null && above.getValue(Properties.UP, false)) {
            // A wall post above
            post = true;
        } else if (north == south && east == west && (north || east)) {
            // A straight wall or a crossing: a post only if something above needs one
            post = is(player, above, BlockTag.WALL_POST_OVERRIDE);
        } else {
            post = true;
        }
        return withIfPresent(state, Properties.UP, post);
    }

    /**
     * {@code WallBlock#connectsTo}.
     *
     * @param side the face of the neighbour towards the wall
     */
    private static boolean wallConnectsTo(InteractionContext player, BlockState neighbour, Direction side) {
        if (is(player, neighbour, BlockTag.WALLS) || RulesMZWorld.is(player, neighbour, BlockTag.BARS) || isPane(neighbour)) {
            return true;
        }
        if (is(player, neighbour, BlockTag.FENCE_GATES)) {
            // FenceGateBlock#connectsToDirection
            Direction gateFacing = facing(neighbour);
            if (gateFacing != null && gateFacing.isHorizontal() && gateFacing.getAxis() == side.clockWise().getAxis()) {
                return true;
            }
        }
        return !PlacementRulesAL.isExceptionForConnection(player, neighbour) && RulesMZWorld.isFaceSturdy(neighbour, side);
    }

    /**
     * Approximates {@code instanceof IronBarsBlock} for glass panes, which have no vanilla tag.
     */
    private static boolean isPane(BlockState state) {
        return state.block().javaIdentifier().value().endsWith("glass_pane");
    }

    /*
     * Trapdoors
     */

    /**
     * {@code TrapDoorBlock#getStateForPlacement}: against the clicked side, or facing the player on the top or bottom.
     * Geyser does not know redstone signals, so it is always placed closed.
     */
    public static @NonNull BlockState trapDoorStateForPlacement(@NonNull PlacementContext context, @NonNull Block block) {
        BlockState state = InteractionTables.defaultState(block);
        Direction clickedFace = context.clickedFace();
        if (!context.replacingClickedOnBlock() && clickedFace.isHorizontal()) {
            state = withFacing(state, clickedFace);
            state = withIfPresent(state, Properties.HALF, RulesMZWorld.clickedUpperHalf(context, context.clickedPos()) ? "top" : "bottom");
        } else {
            state = withFacing(state, context.horizontalDirection().reversed());
            state = withIfPresent(state, Properties.HALF, clickedFace == Direction.UP ? "bottom" : "top");
        }
        return state;
    }

    /*
     * Snow layers
     */

    /**
     * {@code SnowLayerBlock#canSurvive}.
     */
    public static boolean snowLayerCanSurvive(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos) {
        BlockState belowState = RulesMZWorld.below(player, pos);
        if (is(player, belowState, BlockTag.CANNOT_SUPPORT_SNOW_LAYER)) {
            return false;
        }
        if (is(player, belowState, BlockTag.SUPPORT_OVERRIDE_SNOW_LAYER)) {
            return true;
        }
        return InteractionTables.StateFlag.COLLISION_FULL_UP.test(belowState)
            || belowState.is(state.block()) && belowState.getValue(Properties.LAYERS, 0) == 8;
    }

    /**
     * {@code SnowLayerBlock#canBeReplaced}: a single layer is replaceable; layers are added from above with the same item.
     */
    public static boolean snowLayerCanBeReplaced(@NonNull PlacementContext context, @NonNull BlockState state, boolean replacingClickedOnBlock) {
        int layers = state.getValue(Properties.LAYERS, 1);
        if (!context.itemInHand().is(state.block().asItem()) || layers >= 8) {
            return layers == 1;
        }
        return !replacingClickedOnBlock || context.clickedFace() == Direction.UP;
    }

    /**
     * {@code SnowLayerBlock#getStateForPlacement}: one layer more on the clicked snow.
     */
    public static @NonNull BlockState snowLayerStateForPlacement(@NonNull PlacementContext context, @NonNull Block block) {
        BlockState state = context.blockAt(context.clickedPos());
        if (state.is(block)) {
            return state.withValue(Properties.LAYERS, Math.min(8, state.getValue(Properties.LAYERS) + 1));
        }
        return InteractionTables.defaultState(block);
    }

    /*
     * Scaffolding
     */

    /**
     * {@code ScaffoldingBlock#getDistance}: 0 on a sturdy block, otherwise one more than the closest supported scaffolding
     * below or next to it (7 = unsupported).
     * TODO verify against 26.2: reconstructed
     */
    public static int scaffoldingDistance(@NonNull InteractionContext player, @NonNull Vector3i pos) {
        BlockState belowState = RulesMZWorld.below(player, pos);
        int distance = 7;
        if (belowState.is(Blocks.SCAFFOLDING)) {
            distance = belowState.getValue(Properties.STABILITY_DISTANCE, 7);
        } else if (RulesMZWorld.isFaceSturdy(belowState, Direction.UP)) {
            return 0;
        }
        for (Direction direction : Direction.HORIZONTAL) {
            BlockState neighbour = player.blockAt(relative(pos, direction));
            if (neighbour.is(Blocks.SCAFFOLDING)) {
                distance = Math.min(distance, neighbour.getValue(Properties.STABILITY_DISTANCE, 7) + 1);
                if (distance == 1) {
                    break;
                }
            }
        }
        return distance;
    }

    /**
     * {@code ScaffoldingBlock#canSurvive}.
     */
    public static boolean scaffoldingCanSurvive(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos) {
        return scaffoldingDistance(player, pos) < 7;
    }

    /**
     * {@code ScaffoldingBlock#canBeReplaced}: only by scaffolding (which then extends it).
     */
    public static boolean scaffoldingCanBeReplaced(@NonNull PlacementContext context, @NonNull BlockState state, boolean replacingClickedOnBlock) {
        return context.itemInHand().is(state.block().asItem());
    }

    /**
     * {@code ScaffoldingBlock#getStateForPlacement}.
     */
    public static @NonNull BlockState scaffoldingStateForPlacement(@NonNull PlacementContext context, @NonNull Block block) {
        Vector3i pos = context.clickedPos();
        int distance = scaffoldingDistance(context.player(), pos);
        BlockState state = withIfPresent(InteractionTables.defaultState(block), Properties.STABILITY_DISTANCE, distance);
        // ScaffoldingBlock#isBottom
        boolean bottom = distance > 0 && !RulesMZWorld.below(context.player(), pos).is(block);
        return withIfPresent(state, Properties.BOTTOM, bottom);
    }

    /*
     * Speleothems (pointed dripstone, sulfur spikes)
     */

    /**
     * {@code SpeleothemBlock#isValidSpeleothemPlacement}: a sturdy face, or the same speleothem pointing the same way,
     * behind the tip.
     * TODO verify against 26.2: reconstructed from PointedDripstoneBlock#isValidPointedDripstonePlacement
     */
    private static boolean isValidSpeleothemPlacement(InteractionContext player, Block block, Vector3i pos, Direction tipDirection) {
        BlockState behindState = player.blockAt(relative(pos, tipDirection.reversed()));
        return RulesMZWorld.isFaceSturdy(behindState, tipDirection)
            || behindState.is(block) && behindState.getValue(Properties.VERTICAL_DIRECTION) == tipDirection;
    }

    /**
     * {@code SpeleothemBlock#canSurvive}.
     */
    public static boolean speleothemCanSurvive(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos) {
        Direction tipDirection = state.getValue(Properties.VERTICAL_DIRECTION);
        return tipDirection != null && isValidSpeleothemPlacement(player, state.block(), pos, tipDirection);
    }

    /**
     * {@code SpeleothemBlock#getStateForPlacement}: pointing away from the player vertically, or the other way if only
     * that is supported. The thickness is not modelled.
     */
    public static @Nullable BlockState speleothemStateForPlacement(@NonNull PlacementContext context, @NonNull Block block) {
        InteractionContext player = context.player();
        Vector3i pos = context.clickedPos();
        Direction tipDirection = context.nearestLookingVerticalDirection().reversed();
        if (!isValidSpeleothemPlacement(player, block, pos, tipDirection)) {
            tipDirection = tipDirection.reversed();
            if (!isValidSpeleothemPlacement(player, block, pos, tipDirection)) {
                return null;
            }
        }
        return withIfPresent(InteractionTables.defaultState(block), Properties.VERTICAL_DIRECTION, tipDirection);
    }

    /*
     * Others
     */

    /**
     * {@code RedStoneWireBlock#canSurvive}: on a sturdy top face or a hopper.
     */
    public static boolean redstoneWireCanSurvive(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos) {
        BlockState belowState = RulesMZWorld.below(player, pos);
        return RulesMZWorld.isFaceSturdy(belowState, Direction.UP) || belowState.is(Blocks.HOPPER);
    }

    /**
     * {@code SoulFireBlock#canSurvive}.
     */
    public static boolean soulFireCanSurvive(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos) {
        return is(player, RulesMZWorld.below(player, pos), BlockTag.SOUL_FIRE_BASE_BLOCKS);
    }

    /**
     * Facing away from the player horizontally ({@code StonecutterBlock}, {@code ShelfBlock}, {@code VaultBlock},
     * {@code DiodeBlock}/{@code RepeaterBlock}).
     */
    public static @NonNull BlockState facingAwayHorizontally(@NonNull PlacementContext context, @NonNull Block block) {
        return withFacing(InteractionTables.defaultState(block), context.horizontalDirection().reversed());
    }

    /**
     * {@code PistonBaseBlock#getStateForPlacement}: the face points towards the player.
     */
    public static @NonNull BlockState pistonStateForPlacement(@NonNull PlacementContext context, @NonNull Block block) {
        return withFacing(InteractionTables.defaultState(block), context.nearestLookingDirection().reversed());
    }

    /**
     * {@code ObserverBlock#getStateForPlacement}: the face points away from the player.
     */
    public static @NonNull BlockState observerStateForPlacement(@NonNull PlacementContext context, @NonNull Block block) {
        return withFacing(InteractionTables.defaultState(block), context.nearestLookingDirection());
    }

    /**
     * {@code ShulkerBoxBlock#getStateForPlacement}: opening towards the clicked face.
     */
    public static @NonNull BlockState shulkerBoxStateForPlacement(@NonNull PlacementContext context, @NonNull Block block) {
        return withFacing(InteractionTables.defaultState(block), context.clickedFace());
    }

    /**
     * {@code RotatedPillarBlock#getStateForPlacement}: along the axis of the clicked face.
     */
    public static @NonNull BlockState rotatedPillarStateForPlacement(@NonNull PlacementContext context, @NonNull Block block) {
        return withIfPresent(InteractionTables.defaultState(block), Properties.AXIS, context.clickedFace().getAxis());
    }

    private RulesMZShapes() {
    }
}
