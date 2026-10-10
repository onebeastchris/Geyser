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
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;

/**
 * The hooks Java Edition has for placing blocks. Like {@link org.geysermc.geyser.interaction.Interactions}, handlers are
 * registered per vanilla class name, and which blocks/items use which class is generated data.
 * <p>
 * Handlers must only predict; they must never send packets or change state.
 */
public final class Placements {

    /**
     * Mirrors {@code BlockBehaviour#canSurvive}.
     */
    @FunctionalInterface
    public interface BlockCanSurvive {
        BlockCanSurvive DEFAULT = (player, state, pos) -> true;

        boolean canSurvive(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos);
    }

    /**
     * Mirrors {@code Block#getStateForPlacement}. Returning null means the block cannot be placed.
     */
    @FunctionalInterface
    public interface BlockStateForPlacement {
        BlockStateForPlacement DEFAULT = (context, block) -> block.defaultBlockState();

        @Nullable BlockState getStateForPlacement(@NonNull PlacementContext context, @NonNull Block block);
    }

    /**
     * Mirrors {@code BlockBehaviour#canBeReplaced(BlockState, BlockPlaceContext)}.
     */
    @FunctionalInterface
    public interface BlockCanBeReplaced {
        BlockCanBeReplaced DEFAULT = (context, state, replacingClickedOnBlock) -> {
            GeyserItemStack stack = context.itemInHand();
            return InteractionTables.StateFlag.REPLACEABLE.test(state) && (stack.isEmpty() || !stack.is(state.block().asItem()));
        };

        /**
         * @param replacingClickedOnBlock mirrors {@code BlockPlaceContext#replacingClickedOnBlock()} at the time of the
         *                                check: true while the clicked block itself is checked
         */
        boolean canBeReplaced(@NonNull PlacementContext context, @NonNull BlockState state, boolean replacingClickedOnBlock);
    }

    /**
     * The placement methods {@code BlockItem} subclasses override.
     */
    public interface ItemPlacement {
        ItemPlacement DEFAULT = new ItemPlacement() {};

        /**
         * Mirrors {@code BlockItem#updatePlacementContext}. Returning null means the block cannot be placed.
         */
        default @Nullable PlacementContext updatePlacementContext(@NonNull PlacementContext context, @NonNull Block block) {
            return context;
        }

        /**
         * Mirrors {@code BlockItem#getPlacementState}. Returning null means the block cannot be placed.
         */
        default @Nullable BlockState getPlacementState(@NonNull PlacementContext context, @NonNull Block block) {
            BlockState state = PlacementPrediction.stateForPlacement(context, block);
            return state != null && canPlace(context, state) ? state : null;
        }

        /**
         * Mirrors {@code BlockItem#canPlace}.
         */
        default boolean canPlace(@NonNull PlacementContext context, @NonNull BlockState state) {
            return (!mustSurvive() || PlacementPrediction.canSurvive(context.player(), state, context.clickedPos()))
                && !PlacementPrediction.isObstructedByEntity(context.player(), state, context.clickedPos());
        }

        /**
         * Mirrors {@code BlockItem#mustSurvive}.
         */
        default boolean mustSurvive() {
            return true;
        }
    }

    private Placements() {
    }
}
