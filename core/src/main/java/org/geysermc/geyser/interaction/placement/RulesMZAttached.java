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
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.session.cache.tags.BlockTag;

import static org.geysermc.geyser.interaction.placement.RulesMZWorld.facing;
import static org.geysermc.geyser.interaction.placement.RulesMZWorld.faceProperty;
import static org.geysermc.geyser.interaction.placement.RulesMZWorld.relative;
import static org.geysermc.geyser.interaction.placement.RulesMZWorld.withFacing;

/**
 * Placement rules of blocks attached to a neighbour: wall torches, signs, banners and skulls, tripwire hooks, wall
 * hanging signs, vines, multiface blocks (glow lichen, sculk veins, resin clumps) and piston heads.
 */
public final class RulesMZAttached {

    /**
     * The shared shape of {@code WallTorchBlock}, {@code WallSignBlock}, {@code WallBannerBlock} and
     * {@code TripWireHookBlock#getStateForPlacement}: facing away from the first horizontal direction the player looks
     * at (towards the clicked block first) in which the state survives.
     */
    public static @Nullable BlockState wallStateForPlacement(@NonNull PlacementContext context, @NonNull Block block) {
        BlockState state = block.defaultBlockState();
        Vector3i pos = context.clickedPos();
        for (Direction direction : context.nearestLookingDirections()) {
            if (direction.isHorizontal()) {
                state = withFacing(state, direction.reversed());
                if (PlacementPrediction.canSurvive(context.player(), state, pos)) {
                    return state;
                }
            }
        }
        return null;
    }

    /**
     * {@code WallTorchBlock#canSurvive(level, pos, facing)}: the block behind must have a sturdy face towards the torch.
     */
    public static boolean isSturdyBehind(@NonNull InteractionContext player, @NonNull Vector3i pos, @NonNull Direction facing) {
        return RulesMZWorld.isFaceSturdy(player.blockAt(relative(pos, facing.reversed())), facing);
    }

    /**
     * {@code WallTorchBlock#canSurvive} and {@code RedstoneWallTorchBlock#canSurvive}.
     */
    public static boolean wallTorchCanSurvive(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos) {
        Direction facing = facing(state);
        return facing != null && isSturdyBehind(player, pos, facing);
    }

    /**
     * {@code RedstoneWallTorchBlock#getStateForPlacement}: the facing a wall torch would get.
     */
    public static @Nullable BlockState redstoneWallTorchStateForPlacement(@NonNull PlacementContext context, @NonNull Block block) {
        BlockState wallTorch = wallStateForPlacement(context, Blocks.WALL_TORCH);
        if (wallTorch == null) {
            return null;
        }
        Direction facing = facing(wallTorch);
        return facing == null ? null : withFacing(block.defaultBlockState(), facing);
    }

    /**
     * {@code WallSignBlock#canSurvive} and {@code WallBannerBlock#canSurvive}: the block behind must be solid.
     */
    public static boolean solidBehindCanSurvive(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos) {
        Direction facing = facing(state);
        return facing != null && InteractionTables.StateFlag.SOLID.test(player.blockAt(relative(pos, facing.reversed())));
    }

    /**
     * {@code StandingSignBlock#canSurvive}: the block below must be solid.
     */
    public static boolean standingSignCanSurvive(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos) {
        return InteractionTables.StateFlag.SOLID.test(RulesMZWorld.below(player, pos));
    }

    /**
     * {@code TripWireHookBlock#canSurvive}: horizontal, with a sturdy face behind.
     */
    public static boolean tripWireHookCanSurvive(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos) {
        Direction facing = facing(state);
        return facing != null && facing.isHorizontal() && isSturdyBehind(player, pos, facing);
    }

    /**
     * {@code WallSkullBlock#getStateForPlacement}: facing away from the first horizontal direction the player looks at in
     * which the neighbour can't be replaced.
     */
    public static @Nullable BlockState wallSkullStateForPlacement(@NonNull PlacementContext context, @NonNull Block block) {
        BlockState state = block.defaultBlockState();
        Vector3i pos = context.clickedPos();
        for (Direction direction : context.nearestLookingDirections()) {
            if (direction.isHorizontal()) {
                state = withFacing(state, direction.reversed());
                if (!RulesMZWorld.canBeReplaced(context, context.blockAt(relative(pos, direction)))) {
                    return state;
                }
            }
        }
        return null;
    }

    /**
     * {@code WallHangingSignBlock#getStateForPlacement}: like other wall blocks, but only along the clicked face, and it
     * needs something to hang from on one side ({@link #wallHangingSignCanPlace}).
     */
    public static @Nullable BlockState wallHangingSignStateForPlacement(@NonNull PlacementContext context, @NonNull Block block) {
        BlockState state = block.defaultBlockState();
        Vector3i pos = context.clickedPos();
        InteractionContext player = context.player();
        Direction clickedFace = context.clickedFace();
        for (Direction direction : context.nearestLookingDirections()) {
            if (direction.isHorizontal() && direction.getAxis() != clickedFace.getAxis()) {
                state = withFacing(state, direction.reversed());
                if (PlacementPrediction.canSurvive(player, state, pos) && wallHangingSignCanPlace(player, state, pos)) {
                    return state;
                }
            }
        }
        return null;
    }

    /**
     * {@code WallHangingSignBlock#canPlace}: a sturdy face or another wall hanging sign on the left or right.
     */
    public static boolean wallHangingSignCanPlace(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos) {
        Direction facing = facing(state);
        if (facing == null || !facing.isHorizontal()) {
            return false;
        }
        Direction clockWise = facing.clockWise();
        Direction counterClockWise = facing.counterClockWise();
        return wallHangingSignCanAttachTo(player, facing, relative(pos, clockWise), counterClockWise)
            || wallHangingSignCanAttachTo(player, facing, relative(pos, counterClockWise), clockWise);
    }

    private static boolean wallHangingSignCanAttachTo(InteractionContext player, Direction facing, Vector3i attachPos, Direction attachFace) {
        BlockState attachState = player.blockAt(attachPos);
        if (RulesMZWorld.is(player, attachState, BlockTag.WALL_HANGING_SIGNS)) {
            Direction attachFacing = facing(attachState);
            return attachFacing != null && attachFacing.getAxis() == facing.getAxis();
        }
        return RulesMZWorld.isFaceSturdy(attachState, attachFace);
    }

    /**
     * {@code PistonHeadBlock#canSurvive}: in front of an extended piston of the right type, or a moving piston.
     */
    public static boolean pistonHeadCanSurvive(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos) {
        Direction facing = facing(state);
        if (facing == null) {
            return false;
        }
        BlockState base = player.blockAt(relative(pos, facing.reversed()));
        // TODO verify against 26.2: PistonHeadBlock#isFittingBase is reconstructed
        Block piston = "sticky".equals(state.getValue(Properties.PISTON_TYPE)) ? Blocks.STICKY_PISTON : Blocks.PISTON;
        if (base.is(piston)) {
            return base.getValue(Properties.EXTENDED, false) && facing(base) == facing;
        }
        return base.is(Blocks.MOVING_PISTON) && facing(base) == facing;
    }

    /*
     * Vines
     */

    /**
     * {@code VineBlock#isAcceptableNeighbour}: {@code MultifaceBlock#canAttachTo} of the neighbour in the given direction.
     */
    private static boolean isAcceptableNeighbour(InteractionContext player, Vector3i neighbourPos, Direction directionToNeighbour) {
        return InteractionTables.canAttachTo(player.blockAt(neighbourPos), directionToNeighbour.reversed());
    }

    /**
     * {@code VineBlock#canSupportAtFace}.
     */
    private static boolean vineCanSupportAtFace(InteractionContext player, Block vine, Vector3i pos, Direction direction) {
        if (direction == Direction.DOWN) {
            return false;
        }
        if (isAcceptableNeighbour(player, relative(pos, direction), direction)) {
            return true;
        }
        if (direction.isVertical()) {
            return false;
        }
        BlockState aboveState = RulesMZWorld.above(player, pos);
        return aboveState.is(vine) && aboveState.getValue(faceProperty(direction), false);
    }

    /**
     * {@code VineBlock#canSurvive}: some face is left after {@code VineBlock#getUpdatedState} removed the unsupported ones.
     */
    public static boolean vineCanSurvive(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos) {
        // Like vanilla's getUpdatedState, the top face checks the top face (not the bottom) of the block above
        // TODO verify against 26.2: VineBlock#getUpdatedState is reconstructed
        if (state.getValue(Properties.UP, false) && isAcceptableNeighbour(player, pos.add(0, 1, 0), Direction.DOWN)) {
            return true;
        }
        Block vine = state.block();
        BlockState aboveState = null;
        for (Direction direction : Direction.HORIZONTAL) {
            if (!state.getValue(faceProperty(direction), false)) {
                continue;
            }
            if (vineCanSupportAtFace(player, vine, pos, direction)) {
                return true;
            }
            if (aboveState == null) {
                aboveState = RulesMZWorld.above(player, pos);
            }
            if (aboveState.is(vine) && aboveState.getValue(faceProperty(direction), false)) {
                return true;
            }
        }
        return false;
    }

    private static int countVineFaces(BlockState state) {
        int faces = 0;
        if (state.getValue(Properties.UP, false)) {
            faces++;
        }
        for (Direction direction : Direction.HORIZONTAL) {
            if (state.getValue(faceProperty(direction), false)) {
                faces++;
            }
        }
        return faces;
    }

    /**
     * {@code VineBlock#canBeReplaced}: a vine with a free face can be extended. The state checked is always the one at
     * the clicked position at the time of the check, so it is the vine itself.
     */
    public static boolean vineCanBeReplaced(@NonNull PlacementContext context, @NonNull BlockState state, boolean replacingClickedOnBlock) {
        return countVineFaces(state) < 5;
    }

    /**
     * {@code VineBlock#getStateForPlacement}: adds the first supported face the player looks at, to the clicked vine or a
     * new one.
     */
    public static @Nullable BlockState vineStateForPlacement(@NonNull PlacementContext context, @NonNull Block block) {
        Vector3i pos = context.clickedPos();
        InteractionContext player = context.player();
        BlockState clickedState = context.blockAt(pos);
        boolean clickedVine = clickedState.is(block);
        BlockState result = clickedVine ? clickedState : block.defaultBlockState();
        for (Direction direction : context.nearestLookingDirections()) {
            if (direction == Direction.DOWN) {
                continue;
            }
            boolean faceOccupied = clickedVine && clickedState.getValue(faceProperty(direction), false);
            if (!faceOccupied && vineCanSupportAtFace(player, block, pos, direction)) {
                return result.withValue(faceProperty(direction), true);
            }
        }
        return clickedVine ? result : null;
    }

    /*
     * Multiface blocks
     */

    private static boolean hasFace(BlockState state, Direction direction) {
        return state.getValue(faceProperty(direction), false);
    }

    /**
     * {@code MultifaceBlock#canAttachTo(level, pos, direction)}.
     */
    public static boolean multifaceCanAttachTo(@NonNull InteractionContext player, @NonNull Vector3i pos, @NonNull Direction directionToNeighbour) {
        return InteractionTables.canAttachTo(player.blockAt(relative(pos, directionToNeighbour)), directionToNeighbour.reversed());
    }

    /**
     * {@code MultifaceBlock#canSurvive}: at least one face, all attached.
     */
    public static boolean multifaceCanSurvive(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos) {
        boolean hasAtLeastOneFace = false;
        for (Direction direction : Direction.VALUES) {
            if (hasFace(state, direction)) {
                if (!multifaceCanAttachTo(player, pos, direction)) {
                    return false;
                }
                hasAtLeastOneFace = true;
            }
        }
        return hasAtLeastOneFace;
    }

    /**
     * {@code MultifaceBlock#canBeReplaced}: replaceable by anything but itself, and by itself while a face is free.
     */
    public static boolean multifaceCanBeReplaced(@NonNull PlacementContext context, @NonNull BlockState state, boolean replacingClickedOnBlock) {
        if (!context.itemInHand().is(state.block().asItem())) {
            return true;
        }
        for (Direction direction : Direction.VALUES) {
            if (!hasFace(state, direction)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@code MultifaceBlock#getStateForPlacement}: adds the first attachable face the player looks at.
     */
    public static @Nullable BlockState multifaceStateForPlacement(@NonNull PlacementContext context, @NonNull Block block) {
        Vector3i pos = context.clickedPos();
        InteractionContext player = context.player();
        BlockState oldState = context.blockAt(pos);
        boolean oldIsThis = oldState.is(block);
        for (Direction direction : context.nearestLookingDirections()) {
            // MultifaceBlock#isValidStateForPlacement; all faces of the vanilla multiface blocks are supported
            // TODO verify against 26.2: isValidStateForPlacement/isFaceSupported are reconstructed
            if (oldIsThis && hasFace(oldState, direction)) {
                continue;
            }
            if (!multifaceCanAttachTo(player, pos, direction)) {
                continue;
            }
            BlockState newState = oldIsThis ? oldState : block.defaultBlockState();
            return newState.withValue(faceProperty(direction), true);
        }
        return null;
    }

    private RulesMZAttached() {
    }
}
