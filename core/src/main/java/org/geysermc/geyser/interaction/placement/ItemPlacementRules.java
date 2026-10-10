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
import org.geysermc.geyser.interaction.InteractionRegistry;
import org.geysermc.geyser.item.type.BlockItem;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.registry.BlockRegistries;
import org.geysermc.geyser.registry.Registries;
import org.geysermc.geyser.session.cache.tags.BlockTag;

/**
 * Placement overrides of {@code BlockItem} subclasses: {@code StandingAndWallBlockItem} (torches, signs, hanging signs,
 * banners, skulls, coral fans), {@code ScaffoldingBlockItem} and {@code GameMasterBlockItem}.
 * <p>
 * {@code DoubleHighBlockItem} and {@code BedItem} only override {@code placeBlock} (the second half is checked by
 * the blocks' {@code getStateForPlacement}), and {@code PlaceOnWaterBlockItem} and {@code SolidBucketItem} only
 * {@code use}/{@code useOn}, so they use the {@code BlockItem} defaults.
 */
public final class ItemPlacementRules {

    public static void register(InteractionRegistry.Registrar registrar) {
        registrar.itemPlacement("StandingAndWallBlockItem", STANDING_AND_WALL);
        // HangingSignItem only overrides StandingAndWallBlockItem#canPlace(LevelReader, BlockState, BlockPos), which is
        // handled above; registered in case the hook data binds hanging signs to it
        // TODO verify against 26.2: HangingSignItem and StandingAndWallBlockItem are reconstructed
        registrar.itemPlacement("HangingSignItem", STANDING_AND_WALL);
        registrar.itemPlacement("ScaffoldingBlockItem", SCAFFOLDING);
        registrar.itemPlacement("GameMasterBlockItem", GAME_MASTER);
    }

    /**
     * {@code StandingAndWallBlockItem#getPlacementState}: the standing block if the player looks at the attachment
     * direction first (down; up for hanging signs), otherwise the wall block, whichever can be placed first.
     * Vanilla uses neither {@code BlockItem#canPlace} nor {@code mustSurvive} here.
     */
    private static final Placements.ItemPlacement STANDING_AND_WALL = new Placements.ItemPlacement() {
        @Override
        public @Nullable BlockState getPlacementState(@NonNull PlacementContext context, @NonNull Block block) {
            InteractionContext player = context.player();
            Vector3i pos = context.clickedPos();
            boolean hanging = player.isInTag(BlockTag.CEILING_HANGING_SIGNS, block.javaId());
            Direction attachmentDirection = hanging ? Direction.UP : Direction.DOWN;

            Block wallBlock = wallBlock(context.itemInHand().asItem());
            BlockState wallState = wallBlock == null ? null : PlacementPrediction.stateForPlacement(context, wallBlock);
            BlockState toPlace = null;
            for (Direction direction : context.nearestLookingDirections()) {
                if (direction == attachmentDirection.reversed()) {
                    continue;
                }
                BlockState possibleState = direction == attachmentDirection ? PlacementPrediction.stateForPlacement(context, block) : wallState;
                if (possibleState != null && canPlaceAttached(player, possibleState, pos)) {
                    toPlace = possibleState;
                    break;
                }
            }
            return toPlace != null && !PlacementPrediction.isObstructedByEntity(player, toPlace, pos) ? toPlace : null;
        }
    };

    /**
     * {@code StandingAndWallBlockItem#canPlace(level, state, pos)}, with the {@code HangingSignItem} override.
     */
    private static boolean canPlaceAttached(InteractionContext player, BlockState state, Vector3i pos) {
        if (player.isInTag(BlockTag.WALL_HANGING_SIGNS, state.block().javaId()) && !RulesMZAttached.wallHangingSignCanPlace(player, state, pos)) {
            return false;
        }
        return PlacementPrediction.canSurvive(player, state, pos);
    }

    /**
     * The wall variant of each standing-and-wall item, by item ID. Geyser registers the wall block as an additional block
     * of the same item.
     */
    private static Block[] wallBlocks;

    private static @Nullable Block wallBlock(@NonNull Item item) {
        Block[] blocks = wallBlocks;
        if (blocks == null) {
            blocks = new Block[Registries.JAVA_ITEMS.get().size()];
            for (Block candidate : BlockRegistries.JAVA_BLOCKS.get()) {
                if (candidate.asItem() instanceof BlockItem blockItem && blockItem.getBlock() != candidate) {
                    int id = blockItem.javaId();
                    if (id >= 0 && id < blocks.length && blocks[id] == null) {
                        blocks[id] = candidate;
                    }
                }
            }
            wallBlocks = blocks;
        }
        int id = item.javaId();
        return id >= 0 && id < blocks.length ? blocks[id] : null;
    }

    /**
     * {@code ScaffoldingBlockItem}: clicking scaffolding extends it upwards (or sideways when sneaking or clicking its top);
     * it doesn't have to survive where it is placed (it falls instead).
     * TODO verify against 26.2: reconstructed
     */
    private static final Placements.ItemPlacement SCAFFOLDING = new Placements.ItemPlacement() {
        /**
         * Bounds the search upwards; vanilla stops at the first non-scaffolding block (void air above the world).
         */
        private static final int MAX_STEPS = 1024;

        @Override
        public @Nullable PlacementContext updatePlacementContext(@NonNull PlacementContext context, @NonNull Block block) {
            InteractionContext player = context.player();
            Vector3i pos = context.clickedPos();
            BlockState state = context.blockAt(pos);
            if (!state.is(block)) {
                return RulesMZShapes.scaffoldingDistance(player, pos) == 7 ? null : context;
            }

            Direction direction;
            if (context.isSecondaryUseActive()) {
                direction = context.isInside() ? context.clickedFace().reversed() : context.clickedFace();
            } else {
                direction = context.clickedFace() == Direction.UP ? context.horizontalDirection() : Direction.UP;
            }

            int horizontalSteps = 0;
            Vector3i cursor = RulesMZWorld.relative(pos, direction);
            // The client skips the world bounds check, the placement then fails at Level#setBlock
            for (int steps = 0; horizontalSteps < 7 && steps < MAX_STEPS; steps++) {
                state = context.blockAt(cursor);
                if (!state.is(block)) {
                    if (RulesMZWorld.canBeReplaced(context, state)) {
                        return PlacementContext.at(context, cursor, direction);
                    }
                    return null;
                }
                cursor = RulesMZWorld.relative(cursor, direction);
                if (direction.isHorizontal()) {
                    horizontalSteps++;
                }
            }
            return null;
        }

        @Override
        public boolean mustSurvive() {
            return false;
        }
    };

    /**
     * {@code GameMasterBlockItem#getPlacementState}: only players that can use game master blocks can place them.
     */
    private static final Placements.ItemPlacement GAME_MASTER = new Placements.ItemPlacement() {
        @Override
        public @Nullable BlockState getPlacementState(@NonNull PlacementContext context, @NonNull Block block) {
            if (!context.player().canUseGameMasterBlocks()) {
                return null;
            }
            return Placements.ItemPlacement.super.getPlacementState(context, block);
        }
    };

    private ItemPlacementRules() {
    }
}
