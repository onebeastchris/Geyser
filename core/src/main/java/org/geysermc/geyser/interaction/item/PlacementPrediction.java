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
import org.geysermc.geyser.interaction.BlockHit;
import org.geysermc.geyser.interaction.InteractionContext;
import org.geysermc.geyser.interaction.InteractionTables;
import org.geysermc.geyser.interaction.UseOnContext;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.item.type.BlockItem;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.BoundingBox;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.session.cache.tags.BlockTag;
import org.geysermc.geyser.translator.collision.BlockCollision;
import org.geysermc.geyser.util.BlockUtils;
import org.geysermc.geyser.util.InteractionResult;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;

import static org.geysermc.geyser.interaction.item.UseOnWorld.isInTag;

/**
 * Predicts {@code BlockItem#useOn}: placing the block of a block item.
 * <p>
 * Vanilla places the block client-side and reports {@link InteractionResult#SUCCESS} if that worked, and
 * {@link InteractionResult#FAIL} otherwise. Both stop the processing of the click, so mispredicting between them only
 * changes whether the arm swings. The exception are block items with a {@code consumable} component (sweet berries,
 * glow berries, carrots, potatoes), which are eaten if placement failed; for those, survival is checked precisely.
 * <p>
 * Fully predicting placement would need every block's {@code getStateForPlacement}, {@code canSurvive} and collision
 * shape. This prediction is faithful for the parts that matter most and approximates the rest:
 * <ul>
 *     <li>exact: the {@code BlockPlaceContext} position selection, including {@code canBeReplaced} of slabs, snow layers,
 *     candles, sea pickles, turtle eggs, flower beds, leaf litter, vines, multiface blocks and scaffolding;
 *     replaceable states ({@code BlockState#canBeReplaced()}) come from the generated interaction tables</li>
 *     <li>exact: world height limits ({@code Level#setBlock} refuses positions outside the world)</li>
 *     <li>exact: double blocks (doors, tall plants) and beds need their second position to be replaceable;
 *     game master blocks need the permission to use them</li>
 *     <li>exact (but light is ignored): survival of the consumable block items' blocks</li>
 *     <li>approximate: entities obstructing the placement are checked against the collision of the block's
 *     <i>default</i> state, not the state that would be placed</li>
 *     <li>not modelled: survival of all other blocks, other {@code getStateForPlacement} failures (e.g. wall-only
 *     blocks without a wall), world border, feature flags, scaffolding's placement redirection</li>
 * </ul>
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
    public static @NonNull InteractionResult place(@NonNull UseOnContext context) {
        InteractionContext player = context.player();
        BlockHit hit = context.hit();

        // BlockPlaceContext: replace the clicked block if possible, otherwise place next to it
        boolean replaceClicked = canBeReplaced(context, context.clickedState(), hit.position(), true);
        Vector3i pos = replaceClicked ? hit.position() : hit.adjacentPosition();
        if (!replaceClicked && !canBeReplaced(context, player.blockAt(pos), pos, false)) {
            return InteractionResult.FAIL;
        }

        if (UseOnWorld.isOutsideBuildHeight(player, pos.getY())) {
            // Level#setBlock refuses to place blocks outside the world
            return InteractionResult.FAIL;
        }

        if (!(context.stack().asItem() instanceof BlockItem blockItem)) {
            // Not expected for a vanilla block item; nothing more is known
            return InteractionResult.SUCCESS;
        }
        Block block = blockItem.getBlock();
        if (!hasStateForPlacement(context, block, pos, replaceClicked)
            || !canSurvive(player, block, pos)
            || isObstructedByEntity(player, block, pos)) {
            return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Mirrors {@code BlockState#canBeReplaced(BlockPlaceContext)}.
     *
     * @param state the state that would be replaced
     * @param statePos the position of that state
     * @param replacingClicked mirrors {@code BlockPlaceContext#replacingClickedOnBlock}, which is true while the
     *                         clicked block itself is checked
     */
    static boolean canBeReplaced(@NonNull UseOnContext context, @NonNull BlockState state, @NonNull Vector3i statePos, boolean replacingClicked) {
        Block block = state.block();
        GeyserItemStack stack = context.stack();
        boolean sameItem = stack.is(block.asItem());

        // SlabBlock: completing a slab into a double slab
        String slabType = state.getValue(Properties.SLAB_TYPE);
        if (slabType != null) {
            if ("double".equals(slabType) || !sameItem) {
                return false;
            }
            if (!replacingClicked) {
                return true;
            }
            Direction face = context.clickedFace();
            boolean upperHalf = context.hit().location().getY() - statePos.getY() > 0.5;
            return "bottom".equals(slabType)
                ? face == Direction.UP || upperHalf && face.isHorizontal()
                : face == Direction.DOWN || !upperHalf && face.isHorizontal();
        }

        if (block == Blocks.SNOW) {
            // SnowLayerBlock: adding layers from above
            int layers = state.getValue(Properties.LAYERS);
            if (!sameItem || layers >= 8) {
                return layers == 1;
            }
            return !replacingClicked || context.clickedFace() == Direction.UP;
        }

        if (block == Blocks.SCAFFOLDING) {
            // ScaffoldingBlock
            return sameItem;
        }

        if (block == Blocks.VINE && countVineFaces(state) < 5) {
            // VineBlock: a vine with free faces can always be replaced (or extended)
            return true;
        }

        if (block == Blocks.GLOW_LICHEN || block == Blocks.SCULK_VEIN || block == Blocks.RESIN_CLUMP) {
            // MultifaceBlock
            return !sameItem || hasVacantFace(state);
        }

        // CandleBlock, SeaPickleBlock, TurtleEggBlock, FlowerBedBlock/LeafLitterBlock (SegmentableBlock): adding to the stack
        Integer amount = stackedAmount(state);
        if (amount != null && sameItem && amount < 4 && !context.isSecondaryUseActive()) {
            return true;
        }

        // BlockBehaviour#canBeReplaced
        return InteractionTables.StateFlag.REPLACEABLE.test(state) && !sameItem;
    }

    private static Integer stackedAmount(BlockState state) {
        Integer amount = state.getValue(Properties.CANDLES);
        if (amount == null) {
            amount = state.getValue(Properties.PICKLES);
        }
        if (amount == null) {
            amount = state.getValue(Properties.EGGS);
        }
        if (amount == null) {
            amount = state.getValue(Properties.FLOWER_AMOUNT);
        }
        if (amount == null) {
            amount = state.getValue(Properties.SEGMENT_AMOUNT);
        }
        return amount;
    }

    private static int countVineFaces(BlockState state) {
        int faces = 0;
        if (state.getValue(Properties.UP, false)) faces++;
        if (state.getValue(Properties.NORTH, false)) faces++;
        if (state.getValue(Properties.EAST, false)) faces++;
        if (state.getValue(Properties.SOUTH, false)) faces++;
        if (state.getValue(Properties.WEST, false)) faces++;
        return faces;
    }

    private static boolean hasVacantFace(BlockState state) {
        return !state.getValue(Properties.UP, false) || !state.getValue(Properties.DOWN, false)
            || !state.getValue(Properties.NORTH, false) || !state.getValue(Properties.EAST, false)
            || !state.getValue(Properties.SOUTH, false) || !state.getValue(Properties.WEST, false);
    }

    /**
     * The {@code getStateForPlacement}/{@code getPlacementState} failures that are cheap to predict.
     */
    private static boolean hasStateForPlacement(UseOnContext context, Block block, Vector3i pos, boolean replaceClicked) {
        InteractionContext player = context.player();
        BlockState defaultState = block.defaultBlockState();

        // DoorBlock, DoublePlantBlock: the upper half must fit as well (pitcher crops only place their lower half)
        if (defaultState.getValue(Properties.DOUBLE_BLOCK_HALF) != null && block != Blocks.PITCHER_CROP) {
            Vector3i abovePos = pos.add(0, 1, 0);
            return pos.getY() < player.maxY() && canBeReplaced(context, player.blockAt(abovePos), abovePos, replaceClicked);
        }

        // BedBlock: the head goes in the direction the player is looking
        if (defaultState.getValue(Properties.BED_PART) != null) {
            Vector3i headPos = pos.add(UseOnWorld.horizontalDirection(player.yaw()).getUnitVector());
            return canBeReplaced(context, player.blockAt(headPos), headPos, replaceClicked);
        }

        // GameMasterBlockItem
        if (isGameMasterBlock(block)) {
            return player.canUseGameMasterBlocks();
        }
        return true;
    }

    private static boolean isGameMasterBlock(Block block) {
        return block == Blocks.COMMAND_BLOCK || block == Blocks.CHAIN_COMMAND_BLOCK || block == Blocks.REPEATING_COMMAND_BLOCK
            || block == Blocks.STRUCTURE_BLOCK || block == Blocks.JIGSAW || block == Blocks.TEST_BLOCK
            || block == Blocks.TEST_INSTANCE_BLOCK;
    }

    /**
     * Mirrors {@code BlockState#canSurvive} for the blocks of the vanilla block items with a {@code consumable}
     * component (where a failed placement results in eating instead), and for the other crops. All other blocks are
     * assumed to survive.
     */
    private static boolean canSurvive(InteractionContext player, Block block, Vector3i pos) {
        if (block == Blocks.SWEET_BERRY_BUSH) {
            // VegetationBlock#mayPlaceOn
            return isInTag(player, UseOnWorld.below(player, pos), BlockTag.SUPPORTS_VEGETATION);
        }
        if (block == Blocks.CARROTS || block == Blocks.POTATOES || block == Blocks.WHEAT || block == Blocks.BEETROOTS) {
            // CropBlock#mayPlaceOn; CropBlock#hasSufficientLight is not modelled since Geyser does not know light levels
            return isInTag(player, UseOnWorld.below(player, pos), BlockTag.SUPPORTS_CROPS);
        }
        if (block == Blocks.CAVE_VINES) {
            // GrowingPlantHeadBlock#canSurvive: attached to the block above
            BlockState attached = UseOnWorld.above(player, pos);
            return attached.is(Blocks.CAVE_VINES) || attached.is(Blocks.CAVE_VINES_PLANT) || UseOnWorld.isFaceSturdy(attached, Direction.DOWN);
        }
        return true;
    }

    /**
     * Approximates the entity part of {@code Level#isUnobstructed}, using the collision of the block's default state.
     */
    private static boolean isObstructedByEntity(InteractionContext player, Block block, Vector3i pos) {
        BlockCollision collision = BlockUtils.getCollision(block.defaultBlockState().javaId());
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
