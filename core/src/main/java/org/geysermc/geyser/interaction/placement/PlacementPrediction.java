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
import org.geysermc.geyser.interaction.UseOnContext;
import org.geysermc.geyser.interaction.item.ItemUseInteractions;
import org.geysermc.geyser.item.type.BlockItem;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.BoundingBox;
import org.geysermc.geyser.translator.collision.BlockCollision;
import org.geysermc.geyser.util.BlockUtils;
import org.geysermc.geyser.util.InteractionResult;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;

/**
 * Predicts {@code BlockItem#useOn}: placing the block of a block item, which results in {@link InteractionResult#SUCCESS}
 * if the block would be placed and {@link InteractionResult#FAIL} otherwise.
 * <p>
 * Follows {@code BlockItem#place}: the {@link PlacementContext} selects the position, then the block's
 * {@code getStateForPlacement} and {@code canSurvive}, and the item's {@code BlockItem} overrides are consulted through
 * handlers registered per vanilla class (see {@link Placements}).
 * <p>
 * Not modelled: feature flags, the world border, and the block's collision with other blocks (vanilla does not check that
 * either; only entities obstruct placement).
 */
public final class PlacementPrediction {

    /**
     * Mirrors {@code BlockItem#useOn}.
     */
    public static @NonNull InteractionResult useOn(@NonNull UseOnContext context) {
        InteractionResult result = place(context);
        if (!result.consumesAction() && context.stack().getComponent(DataComponentTypes.CONSUMABLE) != null) {
            // Item#use, not the item's own use override
            return ItemUseInteractions.defaultUse(context.player(), context.hand(), context.stack());
        }
        return result;
    }

    /**
     * Mirrors {@code BlockItem#place}.
     */
    public static @NonNull InteractionResult place(@NonNull UseOnContext useOnContext) {
        Item item = useOnContext.stack().asItem();
        if (!(item instanceof BlockItem blockItem)) {
            // Not expected for a vanilla block item; nothing more is known
            return InteractionResult.SUCCESS;
        }
        Block block = blockItem.getBlock();

        PlacementContext context = PlacementContext.of(useOnContext);
        if (!context.canPlace()) {
            return InteractionResult.FAIL;
        }

        Placements.ItemPlacement placement = InteractionRegistry.itemPlacement(item);
        context = placement.updatePlacementContext(context, block);
        if (context == null) {
            return InteractionResult.FAIL;
        }

        BlockState state = placement.getPlacementState(context, block);
        if (state == null) {
            return InteractionResult.FAIL;
        }

        Vector3i pos = context.clickedPos();
        InteractionContext player = context.player();
        if (pos.getY() < player.minY() || pos.getY() > player.maxY()) {
            // Level#setBlock refuses to place blocks outside the world
            return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Mirrors {@code Block#getStateForPlacement} of the given block.
     */
    public static @Nullable BlockState stateForPlacement(@NonNull PlacementContext context, @NonNull Block block) {
        return InteractionRegistry.blockStateForPlacement(block).getStateForPlacement(context, block);
    }

    /**
     * Mirrors {@code BlockState#canSurvive}.
     */
    public static boolean canSurvive(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos) {
        return InteractionRegistry.blockCanSurvive(state.block()).canSurvive(player, state, pos);
    }

    /**
     * Mirrors the entity part of {@code Level#isUnobstructed(BlockState, BlockPos, CollisionContext)}: whether an entity
     * that blocks building intersects the collision of the state at the position.
     */
    public static boolean isObstructedByEntity(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos) {
        BlockCollision collision = BlockUtils.getCollision(state.javaId());
        if (collision == null) {
            return false;
        }
        for (BoundingBox box : collision.getBoundingBoxes()) {
            BoundingBox worldBox = new BoundingBox(box.getMiddleX() + pos.getX(), box.getMiddleY() + pos.getY(),
                box.getMiddleZ() + pos.getZ(), box.getSizeX(), box.getSizeY(), box.getSizeZ());
            if (player.hasEntityInBox(worldBox, true)) {
                return true;
            }
        }
        return false;
    }

    private PlacementPrediction() {
    }
}
