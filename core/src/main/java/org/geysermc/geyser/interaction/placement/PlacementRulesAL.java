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
import org.geysermc.geyser.interaction.InteractionTables;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.ChestType;
import org.geysermc.geyser.level.block.property.IntegerProperty;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.Axis;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.session.cache.tags.BlockTag;
import org.geysermc.geyser.session.cache.tags.Tag;

import static org.geysermc.geyser.interaction.placement.RulesALWorld.above;
import static org.geysermc.geyser.interaction.placement.RulesALWorld.below;
import static org.geysermc.geyser.interaction.placement.RulesALWorld.canSupportCenterDown;
import static org.geysermc.geyser.interaction.placement.RulesALWorld.canSupportCenterUp;
import static org.geysermc.geyser.interaction.placement.RulesALWorld.canSupportRigidBlock;
import static org.geysermc.geyser.interaction.placement.RulesALWorld.is;
import static org.geysermc.geyser.interaction.placement.RulesALWorld.isAir;
import static org.geysermc.geyser.interaction.placement.RulesALWorld.isFaceSturdy;
import static org.geysermc.geyser.interaction.placement.RulesALWorld.isSolid;
import static org.geysermc.geyser.interaction.placement.RulesALWorld.relative;

/**
 * Placement rules ({@code canSurvive}, {@code getStateForPlacement}, {@code canBeReplaced}) of vanilla block classes A to L.
 * <p>
 * Only what decides whether a placement succeeds is modelled: {@code getStateForPlacement} overrides that only set
 * properties which neither {@code canSurvive} nor the collision shape depend on (rotation, waterlogging, ...) are not
 * registered. Placement states only set the properties that matter.
 * <p>
 * Not registered, because the method cannot change whether a block item placement succeeds:
 * <ul>
 *     <li>{@code getStateForPlacement} setting only properties irrelevant to survival and collision (other methods of
 *     these classes may be registered): AbstractFurnaceBlock, AbstractSkullBlock, BannerBlock,
 *     BarrelBlock, BarrierBlock, BaseCoralPlantTypeBlock, BaseRailBlock, BeehiveBlock, BigDripleafBlock,
 *     CalibratedSculkSensorBlock, CampfireBlock, CarvedPumpkinBlock, CeilingHangingSignBlock, ChiseledBookShelfBlock,
 *     CommandBlock, ConcretePowderBlock, ConduitBlock, CopperGolemStatueBlock, CoralBlock, CrafterBlock,
 *     CreakingHeartBlock, DecoratedPotBlock, DiodeBlock, DispenserBlock, DriedGhastBlock, EnderChestBlock,
 *     EndPortalFrameBlock, FlowerBedBlock, GlazedTerracottaBlock, GrowingPlantBlock, HangingRootsBlock,
 *     HeavyCoreBlock, HugeMushroomBlock, InfestedRotatedPillarBlock, JigsawBlock, LeafLitterBlock, LeavesBlock,
 *     LecternBlock, LoomBlock</li>
 *     <li>blocks no block item places: AttachedStemBlock ({@code mayPlaceOn}), BaseFireBlock and FireBlock,
 *     BigDripleafStemBlock, BubbleColumnBlock, CandleCakeBlock ({@code canSurvive})</li>
 * </ul>
 */
public final class PlacementRulesAL {
    /**
     * Kept in fields, so that other blocks can be recognised by their bound handler (vanilla uses {@code instanceof}).
     */
    private static final Placements.BlockStateForPlacement IRON_BARS = PlacementRulesAL::ironBarsStateForPlacement;

    public static void register(InteractionRegistry.Registrar registrar) {
        // See the class documentation
        registrar.sameAsDefault(InteractionRegistry.BLOCK_STATE_FOR_PLACEMENT, "AbstractFurnaceBlock", "AbstractSkullBlock",
            "BannerBlock", "BarrelBlock", "BarrierBlock", "BaseCoralPlantTypeBlock", "BaseRailBlock", "BeehiveBlock",
            "BigDripleafBlock", "CalibratedSculkSensorBlock", "CampfireBlock", "CarvedPumpkinBlock", "CeilingHangingSignBlock",
            "ChiseledBookShelfBlock", "CommandBlock", "ConcretePowderBlock", "ConduitBlock", "CopperGolemStatueBlock",
            "CoralBlock", "CrafterBlock", "CreakingHeartBlock", "DecoratedPotBlock", "DiodeBlock", "DispenserBlock",
            "DriedGhastBlock", "EnderChestBlock", "EndPortalFrameBlock", "FlowerBedBlock", "GlazedTerracottaBlock",
            "GrowingPlantBlock", "HangingRootsBlock", "HeavyCoreBlock", "HugeMushroomBlock", "InfestedRotatedPillarBlock",
            "JigsawBlock", "LeafLitterBlock", "LeavesBlock", "LecternBlock", "LoomBlock", "BaseFireBlock", "FireBlock");
        registrar.sameAsDefault(InteractionRegistry.BLOCK_CAN_SURVIVE, "BaseFireBlock", "FireBlock", "BigDripleafStemBlock",
            "BubbleColumnBlock", "CandleCakeBlock");

        registrar.blockCanSurvive("AmethystClusterBlock", PlacementRulesAL::canSurviveOnFacing);
        registrar.blockStateForPlacement("AmethystClusterBlock",
            (context, block) -> InteractionTables.defaultState(block).withValue(Properties.FACING, context.clickedFace()));

        registrar.blockStateForPlacement("AnvilBlock",
            (context, block) -> InteractionTables.defaultState(block).withValue(Properties.HORIZONTAL_FACING, context.horizontalDirection().clockWise()));

        registrar.blockCanSurvive("BambooSaplingBlock", (player, state, pos) -> is(player, below(player, pos), BlockTag.SUPPORTS_BAMBOO));
        registrar.blockCanSurvive("BambooStalkBlock", (player, state, pos) -> is(player, below(player, pos), BlockTag.SUPPORTS_BAMBOO));
        registrar.blockStateForPlacement("BambooStalkBlock", PlacementRulesAL::bambooStateForPlacement);

        registrar.blockCanSurvive("BannerBlock", (player, state, pos) -> isSolid(below(player, pos)));

        registrar.blockCanSurvive("BaseCoralPlantTypeBlock", (player, state, pos) -> isFaceSturdy(below(player, pos), Direction.UP));
        registrar.blockCanSurvive("BaseCoralWallFanBlock", PlacementRulesAL::canSurviveOnHorizontalFacing);
        registrar.blockStateForPlacement("BaseCoralWallFanBlock",
            (context, block) -> firstSurvivingHorizontal(context, InteractionTables.defaultState(block), true));

        registrar.blockCanSurvive("BasePressurePlateBlock", (player, state, pos) -> {
            BlockState below = below(player, pos);
            return canSupportRigidBlock(below) || canSupportCenterUp(below);
        });
        registrar.blockCanSurvive("BaseRailBlock", (player, state, pos) -> canSupportRigidBlock(below(player, pos)));
        registrar.blockCanSurvive("BaseTorchBlock", (player, state, pos) -> canSupportCenterUp(below(player, pos)));

        registrar.blockStateForPlacement("BedBlock", PlacementRulesAL::bedStateForPlacement);

        registrar.blockCanSurvive("BellBlock", PlacementRulesAL::bellCanSurvive);
        registrar.blockStateForPlacement("BellBlock", PlacementRulesAL::bellStateForPlacement);

        registrar.blockCanSurvive("BigDripleafBlock", (player, state, pos) -> {
            BlockState below = below(player, pos);
            return below.is(state.block()) || below.is(Blocks.BIG_DRIPLEAF_STEM) || is(player, below, BlockTag.SUPPORTS_BIG_DRIPLEAF);
        });

        registrar.blockCanSurvive("CactusBlock", PlacementRulesAL::cactusCanSurvive);
        registrar.blockCanSurvive("CakeBlock", (player, state, pos) -> isSolid(below(player, pos)));

        registrar.blockCanSurvive("CandleBlock", (player, state, pos) -> canSupportCenterUp(below(player, pos)));
        registrar.blockStateForPlacement("CandleBlock", PlacementRulesAL::candleStateForPlacement);
        registrar.blockCanBeReplaced("CandleBlock", (context, state, replacingClickedOnBlock) ->
            canAddToStack(context, state, Properties.CANDLES) || Placements.BlockCanBeReplaced.DEFAULT.canBeReplaced(context, state, replacingClickedOnBlock));

        registrar.blockCanSurvive("CarpetBlock", (player, state, pos) -> !isAir(below(player, pos)));

        registrar.blockCanSurvive("CeilingHangingSignBlock", (player, state, pos) -> canSupportCenterDown(above(player, pos)));

        // RotatedPillarBlock#getStateForPlacement (the chain's collision depends on the axis); waterlogging is ignored
        registrar.blockStateForPlacement("ChainBlock",
            (context, block) -> InteractionTables.defaultState(block).withValue(Properties.AXIS, context.clickedFace().getAxis()));

        registrar.blockStateForPlacement("ChestBlock", (context, block) -> chestStateForPlacement(context, block, false));
        registrar.blockStateForPlacement("CopperChestBlock", (context, block) -> chestStateForPlacement(context, block, true));

        registrar.blockCanSurvive("ChorusFlowerBlock", PlacementRulesAL::chorusFlowerCanSurvive);
        registrar.blockCanSurvive("ChorusPlantBlock", PlacementRulesAL::chorusPlantCanSurvive);
        registrar.blockStateForPlacement("ChorusPlantBlock", PlacementRulesAL::chorusPlantStateForPlacement);

        registrar.blockCanSurvive("CocoaBlock", (player, state, pos) ->
            is(player, relative(player, pos, state.getValue(Properties.HORIZONTAL_FACING)), BlockTag.SUPPORTS_COCOA));
        registrar.blockStateForPlacement("CocoaBlock",
            (context, block) -> firstSurvivingHorizontal(context, InteractionTables.defaultState(block), false));

        // CropBlock#hasSufficientLight is not modelled: Geyser does not know light levels
        registrar.blockCanSurvive("CropBlock", (player, state, pos) -> is(player, below(player, pos), BlockTag.SUPPORTS_CROPS));

        // TODO verify against 26.2: DiodeBlock#canSurviveOn is not in the dumps; assumed to be a rigid top face
        registrar.blockCanSurvive("DiodeBlock", (player, state, pos) -> canSupportRigidBlock(below(player, pos)));

        registrar.blockCanSurvive("DirtPathBlock", (player, state, pos) -> {
            BlockState above = above(player, pos);
            return !isSolid(above) || isFenceGate(above);
        });
        registrar.blockStateForPlacement("DirtPathBlock", PlacementRulesAL::stateOrDirt);

        registrar.blockCanSurvive("DoorBlock", PlacementRulesAL::doorCanSurvive);
        registrar.blockStateForPlacement("DoorBlock", PlacementRulesAL::doorStateForPlacement);

        registrar.blockCanSurvive("DoublePlantBlock", (player, state, pos) ->
            doublePlantCanSurvive(player, state, pos, (p, below, belowPos) -> is(p, below, BlockTag.SUPPORTS_VEGETATION)));
        registrar.blockStateForPlacement("DoublePlantBlock", PlacementRulesAL::doublePlantStateForPlacement);

        registrar.blockStateForPlacement("EndRodBlock", PlacementRulesAL::endRodStateForPlacement);

        registrar.blockCanSurvive("FaceAttachedHorizontalDirectionalBlock", PlacementRulesAL::faceAttachedCanSurvive);
        registrar.blockStateForPlacement("FaceAttachedHorizontalDirectionalBlock", PlacementRulesAL::faceAttachedStateForPlacement);
        registrar.blockCanSurvive("GrindstoneBlock", (player, state, pos) -> true);

        registrar.blockCanSurvive("FarmlandBlock", (player, state, pos) -> {
            BlockState above = above(player, pos);
            // TODO verify against 26.2: FarmlandBlock#shouldMaintainFarmland is not in the dumps
            return !isSolid(above) || is(player, above, BlockTag.MAINTAINS_FARMLAND);
        });
        registrar.blockStateForPlacement("FarmlandBlock", PlacementRulesAL::stateOrDirt);

        registrar.blockStateForPlacement("FenceBlock", PlacementRulesAL::fenceStateForPlacement);
        // Opening by a redstone signal is not modelled (an open gate has no collision)
        registrar.blockStateForPlacement("FenceGateBlock",
            (context, block) -> InteractionTables.defaultState(block).withValue(Properties.HORIZONTAL_FACING, context.horizontalDirection()));
        registrar.blockStateForPlacement("IronBarsBlock", IRON_BARS);

        registrar.blockCanBeReplaced("FlowerBedBlock", (context, state, replacingClickedOnBlock) ->
            canAddToStack(context, state, Properties.FLOWER_AMOUNT) || Placements.BlockCanBeReplaced.DEFAULT.canBeReplaced(context, state, replacingClickedOnBlock));
        registrar.blockCanBeReplaced("LeafLitterBlock", (context, state, replacingClickedOnBlock) ->
            canAddToStack(context, state, Properties.SEGMENT_AMOUNT) || Placements.BlockCanBeReplaced.DEFAULT.canBeReplaced(context, state, replacingClickedOnBlock));
        registrar.blockCanSurvive("LeafLitterBlock", (player, state, pos) -> isFaceSturdy(below(player, pos), Direction.UP));

        registrar.blockCanSurvive("FrogspawnBlock", (player, state, pos) ->
            onWaterSurface(player, pos, BlockTag.SUPPORTS_FROGSPAWN));

        registrar.blockCanSurvive("GrowingPlantBlock", PlacementRulesAL::growingPlantCanSurvive);
        registrar.blockCanBeReplaced("GrowingPlantBodyBlock", PlacementRulesAL::growingPlantBodyCanBeReplaced);
        registrar.blockStateForPlacement("KelpBlock", (context, block) ->
            RulesALWorld.hasFullWater(context.blockAt(context.clickedPos())) ? InteractionTables.defaultState(block) : null);

        registrar.blockCanSurvive("HangingMossBlock", (player, state, pos) -> {
            // TODO verify against 26.2: HangingMossBlock#canStayAtPosition is not in the dumps
            // MultifaceBlock#canAttachTo the block above, or more hanging moss
            BlockState above = above(player, pos);
            return isFaceSturdy(above, Direction.DOWN) || InteractionTables.StateFlag.COLLISION_FULL_DOWN.test(above)
                || above.is(state.block());
        });

        registrar.blockCanSurvive("HangingRootsBlock", (player, state, pos) -> isFaceSturdy(above(player, pos), Direction.DOWN));

        registrar.blockStateForPlacement("HopperBlock", (context, block) -> {
            Direction direction = context.clickedFace().reversed();
            return InteractionTables.defaultState(block).withValue(Properties.FACING_HOPPER, direction.isVertical() ? Direction.DOWN : direction);
        });

        registrar.blockCanSurvive("LadderBlock", PlacementRulesAL::canSurviveOnHorizontalFacing);
        registrar.blockStateForPlacement("LadderBlock", PlacementRulesAL::ladderStateForPlacement);

        registrar.blockCanSurvive("LanternBlock", (player, state, pos) -> state.getValue(Properties.HANGING)
            ? canSupportCenterDown(above(player, pos))
            : canSupportCenterUp(below(player, pos)));
        registrar.blockStateForPlacement("LanternBlock", PlacementRulesAL::lanternStateForPlacement);

        registrar.blockStateForPlacement("LightningRodBlock",
            (context, block) -> InteractionTables.defaultState(block).withValue(Properties.FACING, context.clickedFace()));
    }

    /**
     * {@code mayPlaceOn} of a plant: whether it can stand on the given block below it.
     */
    @FunctionalInterface
    public interface MayPlaceOn {
        boolean mayPlaceOn(@NonNull InteractionContext player, @NonNull BlockState below, @NonNull Vector3i belowPos);
    }

    /**
     * Mirrors the {@code mayPlaceOn} overrides of the {@code VegetationBlock} subclasses A to L that use
     * {@code VegetationBlock#canSurvive} (whose handler is bound under {@code VegetationBlock}): AzaleaBlock,
     * CactusFlowerBlock, DryVegetationBlock (and its subclasses) and LilyPadBlock. AttachedStemBlock is left out, since no
     * item places attached stems.
     *
     * @param plant the plant's block
     * @return whether the plant may be placed on the block below, or null if its {@code mayPlaceOn} isn't one of these
     */
    public static @Nullable Boolean vegetationMayPlaceOn(@NonNull InteractionContext player, @NonNull Block plant,
                                                         @NonNull BlockState below, @NonNull Vector3i belowPos) {
        if (plant == Blocks.AZALEA || plant == Blocks.FLOWERING_AZALEA) {
            return is(player, below, BlockTag.SUPPORTS_AZALEA);
        }
        if (plant == Blocks.CACTUS_FLOWER) {
            return is(player, below, BlockTag.SUPPORT_OVERRIDE_CACTUS_FLOWER) || canSupportCenterUp(below);
        }
        if (plant == Blocks.DEAD_BUSH || plant == Blocks.SHORT_DRY_GRASS || plant == Blocks.TALL_DRY_GRASS) {
            return is(player, below, BlockTag.SUPPORTS_DRY_VEGETATION);
        }
        if (plant == Blocks.LILY_PAD) {
            return onWaterSurface(player, belowPos.up(), BlockTag.SUPPORTS_LILY_PAD);
        }
        return null;
    }

    /**
     * Mirrors {@code LilyPadBlock#mayPlaceOn} and {@code FrogspawnBlock#mayPlaceOn}: the block below has the right fluid
     * or is in the given block tag, and the plant's position has no fluid.
     * <p>
     * TODO verify against 26.2: the fluid tags ({@code supports_lily_pad}, {@code supports_frogspawn}) are assumed to
     * contain water sources only, like the fluid check before the tags were introduced.
     */
    private static boolean onWaterSurface(InteractionContext player, Vector3i pos, Tag<Block> supportTag) {
        BlockState below = below(player, pos);
        return (RulesALWorld.hasWaterSource(below) || is(player, below, supportTag)) && RulesALWorld.hasNoFluid(player.blockAt(pos));
    }

    /**
     * Mirrors {@code DoublePlantBlock#canSurvive}, for subclasses calling it with their own {@code mayPlaceOn}.
     */
    public static boolean doublePlantCanSurvive(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos,
                                                @NonNull MayPlaceOn mayPlaceOn) {
        Vector3i belowPos = pos.down();
        BlockState below = player.blockAt(belowPos);
        if ("upper".equals(state.getValue(Properties.DOUBLE_BLOCK_HALF))) {
            return below.is(state.block()) && "lower".equals(below.getValue(Properties.DOUBLE_BLOCK_HALF));
        }
        // VegetationBlock#canSurvive
        return mayPlaceOn.mayPlaceOn(player, below, belowPos);
    }

    /**
     * Mirrors {@code DoublePlantBlock#getStateForPlacement}, for subclasses calling it: the upper half must fit as well.
     */
    public static @Nullable BlockState doublePlantStateForPlacement(@NonNull PlacementContext context, @NonNull Block block) {
        Vector3i pos = context.clickedPos();
        if (pos.getY() < context.player().maxY() && RulesALWorld.canBeReplaced(context, pos.up())) {
            return InteractionTables.defaultState(block).withValue(Properties.DOUBLE_BLOCK_HALF, "lower");
        }
        return null;
    }

    /**
     * Mirrors {@code FaceAttachedHorizontalDirectionalBlock#canAttach}: whether the block in the given direction has a
     * sturdy face towards the position.
     */
    public static boolean canAttach(@NonNull InteractionContext player, @NonNull Vector3i pos, @NonNull Direction direction) {
        return isFaceSturdy(relative(player, pos, direction), direction.reversed());
    }

    /**
     * Mirrors {@code Block#isExceptionForConnection}: blocks that fences, walls and panes don't connect to.
     * <p>
     * TODO verify against 26.2: not in the dumps.
     */
    public static boolean isExceptionForConnection(@NonNull InteractionContext player, @NonNull BlockState state) {
        Block block = state.block();
        // Only leaves have the persistent property (vanilla checks instanceof LeavesBlock)
        return state.getValueNullable(Properties.PERSISTENT) != null || block == Blocks.BARRIER || block == Blocks.CARVED_PUMPKIN
            || block == Blocks.JACK_O_LANTERN || block == Blocks.MELON || block == Blocks.PUMPKIN || is(player, state, BlockTag.SHULKER_BOXES);
    }

    /**
     * @return whether the block is a fence gate (vanilla: {@code instanceof FenceGateBlock}); only fence gates have the
     * in_wall property
     */
    private static boolean isFenceGate(BlockState state) {
        return state.getValueNullable(Properties.IN_WALL) != null;
    }

    /**
     * Mirrors the {@code canSurvive} of blocks attached to the block behind their (six-way) facing, e.g.
     * {@code AmethystClusterBlock}.
     */
    private static boolean canSurviveOnFacing(InteractionContext player, BlockState state, Vector3i pos) {
        Direction facing = state.getValue(Properties.FACING);
        return isFaceSturdy(relative(player, pos, facing.reversed()), facing);
    }

    /**
     * Mirrors the {@code canSurvive} of blocks attached to the block behind their horizontal facing:
     * {@code BaseCoralWallFanBlock}, {@code LadderBlock#canAttachTo}.
     */
    private static boolean canSurviveOnHorizontalFacing(InteractionContext player, BlockState state, Vector3i pos) {
        Direction facing = state.getValue(Properties.HORIZONTAL_FACING);
        return isFaceSturdy(relative(player, pos, facing.reversed()), facing);
    }

    /**
     * The first horizontal direction the player looks at for which the block survives, like {@code CocoaBlock},
     * {@code BaseCoralWallFanBlock} and {@code LadderBlock}.
     *
     * @param opposite whether the facing points away from the looking direction (wall fans, ladders) instead of
     *                 towards it (cocoa)
     */
    private static @Nullable BlockState firstSurvivingHorizontal(PlacementContext context, BlockState state, boolean opposite) {
        Vector3i pos = context.clickedPos();
        for (Direction direction : context.nearestLookingDirections()) {
            if (direction.isHorizontal()) {
                BlockState candidate = state.withValue(Properties.HORIZONTAL_FACING, opposite ? direction.reversed() : direction);
                if (PlacementPrediction.canSurvive(context.player(), candidate, pos)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /**
     * Mirrors {@code SegmentableBlock#canBeReplaced} and the stacking part of {@code CandleBlock#canBeReplaced}: one
     * more of the block can be added.
     */
    private static boolean canAddToStack(PlacementContext context, BlockState state, IntegerProperty amount) {
        return !context.isSecondaryUseActive() && context.itemInHand().is(state.block().asItem()) && state.getValue(amount) < 4;
    }

    private static @Nullable BlockState bambooStateForPlacement(PlacementContext context, Block block) {
        Vector3i pos = context.clickedPos();
        InteractionContext player = context.player();
        if (!RulesALWorld.hasNoFluid(player.blockAt(pos))) {
            return null;
        }
        BlockState below = below(player, pos);
        if (!is(player, below, BlockTag.SUPPORTS_BAMBOO)) {
            return null;
        }
        if (below.is(Blocks.BAMBOO_SAPLING)) {
            return InteractionTables.defaultState(block).withValue(Properties.AGE_1, 0);
        }
        if (below.is(Blocks.BAMBOO)) {
            return InteractionTables.defaultState(block).withValue(Properties.AGE_1, below.getValue(Properties.AGE_1) > 0 ? 1 : 0);
        }
        BlockState above = above(player, pos);
        if (above.is(Blocks.BAMBOO)) {
            return InteractionTables.defaultState(block).withValue(Properties.AGE_1, above.getValue(Properties.AGE_1));
        }
        return InteractionTables.defaultState(Blocks.BAMBOO_SAPLING);
    }

    private static @Nullable BlockState bedStateForPlacement(PlacementContext context, Block block) {
        Direction facing = context.horizontalDirection();
        // TODO the world border check for the head is not modelled
        if (RulesALWorld.canBeReplaced(context, context.clickedPos().add(facing.getUnitVector()))) {
            return InteractionTables.defaultState(block).withValue(Properties.HORIZONTAL_FACING, facing);
        }
        return null;
    }

    private static boolean bellCanSurvive(InteractionContext player, BlockState state, Vector3i pos) {
        return switch (state.getValue(Properties.BELL_ATTACHMENT)) {
            case "floor" -> canAttach(player, pos, Direction.DOWN);
            case "ceiling" -> canSupportCenterDown(above(player, pos));
            default -> canAttach(player, pos, state.getValue(Properties.HORIZONTAL_FACING));
        };
    }

    private static @Nullable BlockState bellStateForPlacement(PlacementContext context, Block block) {
        InteractionContext player = context.player();
        Vector3i pos = context.clickedPos();
        Direction clickedFace = context.clickedFace();
        if (clickedFace.isVertical()) {
            BlockState state = InteractionTables.defaultState(block)
                .withValue(Properties.BELL_ATTACHMENT, clickedFace == Direction.DOWN ? "ceiling" : "floor")
                .withValue(Properties.HORIZONTAL_FACING, context.horizontalDirection());
            return PlacementPrediction.canSurvive(player, state, pos) ? state : null;
        }

        boolean doubleAttached = clickedFace.getAxis() == Axis.X
            ? canAttach(player, pos, Direction.WEST) && canAttach(player, pos, Direction.EAST)
            : canAttach(player, pos, Direction.NORTH) && canAttach(player, pos, Direction.SOUTH);
        BlockState state = InteractionTables.defaultState(block)
            .withValue(Properties.HORIZONTAL_FACING, clickedFace.reversed())
            .withValue(Properties.BELL_ATTACHMENT, doubleAttached ? "double_wall" : "single_wall");
        if (PlacementPrediction.canSurvive(player, state, pos)) {
            return state;
        }
        state = state.withValue(Properties.BELL_ATTACHMENT, canAttach(player, pos, Direction.DOWN) ? "floor" : "ceiling");
        return PlacementPrediction.canSurvive(player, state, pos) ? state : null;
    }

    private static boolean cactusCanSurvive(InteractionContext player, BlockState state, Vector3i pos) {
        for (Direction direction : Direction.HORIZONTAL) {
            BlockState neighbour = relative(player, pos, direction);
            if (isSolid(neighbour) || RulesALWorld.hasLava(neighbour)) {
                return false;
            }
        }
        BlockState below = below(player, pos);
        return (below.is(state.block()) || is(player, below, BlockTag.SUPPORTS_CACTUS)) && !RulesALWorld.isLiquidBlock(above(player, pos));
    }

    private static BlockState candleStateForPlacement(PlacementContext context, Block block) {
        BlockState existing = context.blockAt(context.clickedPos());
        if (existing.is(block)) {
            // BlockState#cycle
            int candles = existing.getValue(Properties.CANDLES);
            return existing.withValue(Properties.CANDLES, candles >= 4 ? 1 : candles + 1);
        }
        return InteractionTables.defaultState(block);
    }

    /**
     * Mirrors {@code ChestBlock#getStateForPlacement}: the chest type decides the collision shape.
     *
     * @param copper whether this is a copper chest, which connects to any copper chest
     */
    private static BlockState chestStateForPlacement(PlacementContext context, Block block, boolean copper) {
        InteractionContext player = context.player();
        Vector3i pos = context.clickedPos();
        ChestType type = ChestType.SINGLE;
        Direction facing = context.horizontalDirection().reversed();
        boolean secondaryUse = context.isSecondaryUseActive();
        Direction clickedFace = context.clickedFace();
        if (clickedFace.isHorizontal() && secondaryUse) {
            Direction partnerFacing = chestPartnerFacing(player, pos, clickedFace.reversed(), block, copper);
            if (partnerFacing != null && partnerFacing.getAxis() != clickedFace.getAxis()) {
                facing = partnerFacing;
                type = partnerFacing.counterClockWise() == clickedFace.reversed() ? ChestType.RIGHT : ChestType.LEFT;
            }
        }
        if (type == ChestType.SINGLE && !secondaryUse) {
            // ChestBlock#getChestType
            if (facing == chestPartnerFacing(player, pos, facing.clockWise(), block, copper)) {
                type = ChestType.LEFT;
            } else if (facing == chestPartnerFacing(player, pos, facing.counterClockWise(), block, copper)) {
                type = ChestType.RIGHT;
            }
        }
        // CopperChestBlock#getLeastOxidizedChestOfConnectedBlocks may pick another copper chest; the shape is the same
        return InteractionTables.defaultState(block).withValue(Properties.HORIZONTAL_FACING, facing).withValue(Properties.CHEST_TYPE, type);
    }

    /**
     * Mirrors {@code ChestBlock#candidatePartnerFacing}.
     * <p>
     * TODO verify against 26.2: {@code CopperChestBlock#chestCanConnectTo} is not in the dumps; assumed to accept any
     * block of the copper_chests tag.
     */
    private static @Nullable Direction chestPartnerFacing(InteractionContext player, Vector3i pos, Direction direction, Block block, boolean copper) {
        BlockState state = relative(player, pos, direction);
        boolean canConnect = copper ? is(player, state, BlockTag.COPPER_CHESTS) && state.getValueNullable(Properties.CHEST_TYPE) != null : state.is(block);
        return canConnect && state.getValue(Properties.CHEST_TYPE) == ChestType.SINGLE ? state.getValue(Properties.HORIZONTAL_FACING) : null;
    }

    private static boolean chorusFlowerCanSurvive(InteractionContext player, BlockState state, Vector3i pos) {
        BlockState below = below(player, pos);
        if (below.is(Blocks.CHORUS_PLANT) || is(player, below, BlockTag.SUPPORTS_CHORUS_FLOWER)) {
            return true;
        }
        if (!isAir(below)) {
            return false;
        }
        boolean oneNeighbour = false;
        for (Direction direction : Direction.HORIZONTAL) {
            BlockState neighbour = relative(player, pos, direction);
            if (neighbour.is(Blocks.CHORUS_PLANT)) {
                if (oneNeighbour) {
                    return false;
                }
                oneNeighbour = true;
            } else if (!isAir(neighbour)) {
                return false;
            }
        }
        return oneNeighbour;
    }

    private static boolean chorusPlantCanSurvive(InteractionContext player, BlockState state, Vector3i pos) {
        Block plant = state.block();
        BlockState below = below(player, pos);
        boolean blockAboveOrBelow = !isAir(above(player, pos)) && !isAir(below);
        for (Direction direction : Direction.HORIZONTAL) {
            Vector3i neighbourPos = pos.add(direction.getUnitVector());
            if (player.blockAt(neighbourPos).is(plant)) {
                if (blockAboveOrBelow) {
                    return false;
                }
                BlockState neighbourBelow = below(player, neighbourPos);
                if (neighbourBelow.is(plant) || is(player, neighbourBelow, BlockTag.SUPPORTS_CHORUS_PLANT)) {
                    return true;
                }
            }
        }
        return below.is(plant) || is(player, below, BlockTag.SUPPORTS_CHORUS_PLANT);
    }

    /**
     * Mirrors {@code ChorusPlantBlock#getStateWithConnections}: the connections decide the collision shape.
     * <p>
     * TODO verify against 26.2: not in the dumps.
     */
    private static BlockState chorusPlantStateForPlacement(PlacementContext context, Block block) {
        InteractionContext player = context.player();
        Vector3i pos = context.clickedPos();
        BlockState below = below(player, pos);
        return InteractionTables.defaultState(block)
            .withValue(Properties.DOWN, connectsToChorus(below, block) || is(player, below, BlockTag.SUPPORTS_CHORUS_PLANT))
            .withValue(Properties.UP, connectsToChorus(above(player, pos), block))
            .withValue(Properties.NORTH, connectsToChorus(relative(player, pos, Direction.NORTH), block))
            .withValue(Properties.EAST, connectsToChorus(relative(player, pos, Direction.EAST), block))
            .withValue(Properties.SOUTH, connectsToChorus(relative(player, pos, Direction.SOUTH), block))
            .withValue(Properties.WEST, connectsToChorus(relative(player, pos, Direction.WEST), block));
    }

    private static boolean connectsToChorus(BlockState state, Block plant) {
        return state.is(plant) || state.is(Blocks.CHORUS_FLOWER);
    }

    /**
     * {@code DirtPathBlock} and {@code FarmlandBlock}: turn into dirt if they can't survive.
     */
    private static BlockState stateOrDirt(PlacementContext context, Block block) {
        BlockState state = InteractionTables.defaultState(block);
        return PlacementPrediction.canSurvive(context.player(), state, context.clickedPos()) ? state : InteractionTables.defaultState(Blocks.DIRT);
    }

    private static boolean doorCanSurvive(InteractionContext player, BlockState state, Vector3i pos) {
        BlockState below = below(player, pos);
        return "lower".equals(state.getValue(Properties.DOUBLE_BLOCK_HALF)) ? isFaceSturdy(below, Direction.UP) : below.is(state.block());
    }

    /**
     * Mirrors {@code DoorBlock#getStateForPlacement}. Doors opened by a redstone signal are not modelled, so the hinge
     * (which only changes the shape of an open door) is left out.
     */
    private static @Nullable BlockState doorStateForPlacement(PlacementContext context, Block block) {
        Vector3i pos = context.clickedPos();
        if (pos.getY() < context.player().maxY() && RulesALWorld.canBeReplaced(context, pos.up())) {
            return InteractionTables.defaultState(block)
                .withValue(Properties.HORIZONTAL_FACING, context.horizontalDirection())
                .withValue(Properties.DOUBLE_BLOCK_HALF, "lower");
        }
        return null;
    }

    private static BlockState endRodStateForPlacement(PlacementContext context, Block block) {
        Direction clickedFace = context.clickedFace();
        BlockState behind = relative(context.player(), context.clickedPos(), clickedFace.reversed());
        boolean againstOpposite = behind.is(block) && behind.getValue(Properties.FACING) == clickedFace;
        return InteractionTables.defaultState(block).withValue(Properties.FACING, againstOpposite ? clickedFace.reversed() : clickedFace);
    }

    private static boolean faceAttachedCanSurvive(InteractionContext player, BlockState state, Vector3i pos) {
        // getConnectedDirection(state).getOpposite()
        Direction direction = switch (state.getValue(Properties.ATTACH_FACE)) {
            case "ceiling" -> Direction.UP;
            case "floor" -> Direction.DOWN;
            default -> state.getValue(Properties.HORIZONTAL_FACING).reversed();
        };
        return canAttach(player, pos, direction);
    }

    private static @Nullable BlockState faceAttachedStateForPlacement(PlacementContext context, Block block) {
        Vector3i pos = context.clickedPos();
        for (Direction direction : context.nearestLookingDirections()) {
            BlockState state;
            if (direction.isVertical()) {
                state = InteractionTables.defaultState(block)
                    .withValue(Properties.ATTACH_FACE, direction == Direction.UP ? "ceiling" : "floor")
                    .withValue(Properties.HORIZONTAL_FACING, context.horizontalDirection());
            } else {
                state = InteractionTables.defaultState(block)
                    .withValue(Properties.ATTACH_FACE, "wall")
                    .withValue(Properties.HORIZONTAL_FACING, direction.reversed());
            }
            if (PlacementPrediction.canSurvive(context.player(), state, pos)) {
                return state;
            }
        }
        return null;
    }

    /**
     * Mirrors {@code FenceBlock#getStateForPlacement}: the connections decide the collision shape.
     */
    private static BlockState fenceStateForPlacement(PlacementContext context, Block block) {
        InteractionContext player = context.player();
        Vector3i pos = context.clickedPos();
        boolean wooden = player.isInTag(BlockTag.WOODEN_FENCES, block.javaId());
        return InteractionTables.defaultState(block)
            .withValue(Properties.NORTH, fenceConnectsTo(player, pos, Direction.NORTH, wooden))
            .withValue(Properties.EAST, fenceConnectsTo(player, pos, Direction.EAST, wooden))
            .withValue(Properties.SOUTH, fenceConnectsTo(player, pos, Direction.SOUTH, wooden))
            .withValue(Properties.WEST, fenceConnectsTo(player, pos, Direction.WEST, wooden));
    }

    /**
     * Mirrors {@code FenceBlock#connectsTo} for the neighbour in the given direction.
     */
    private static boolean fenceConnectsTo(InteractionContext player, Vector3i pos, Direction direction, boolean wooden) {
        BlockState neighbour = relative(player, pos, direction);
        Direction towardsFence = direction.reversed();
        // FenceBlock#isSameFence
        if (is(player, neighbour, BlockTag.FENCES) && is(player, neighbour, BlockTag.WOODEN_FENCES) == wooden) {
            return true;
        }
        // FenceGateBlock#connectsToDirection
        if (isFenceGate(neighbour) && neighbour.getValue(Properties.HORIZONTAL_FACING).getAxis() == towardsFence.clockWise().getAxis()) {
            return true;
        }
        return !isExceptionForConnection(player, neighbour) && isFaceSturdy(neighbour, towardsFence);
    }

    /**
     * Mirrors {@code IronBarsBlock#getStateForPlacement} (also glass panes): the connections decide the collision shape.
     */
    private static BlockState ironBarsStateForPlacement(PlacementContext context, Block block) {
        InteractionContext player = context.player();
        Vector3i pos = context.clickedPos();
        return InteractionTables.defaultState(block)
            .withValue(Properties.NORTH, ironBarsAttachTo(player, pos, Direction.NORTH))
            .withValue(Properties.SOUTH, ironBarsAttachTo(player, pos, Direction.SOUTH))
            .withValue(Properties.WEST, ironBarsAttachTo(player, pos, Direction.WEST))
            .withValue(Properties.EAST, ironBarsAttachTo(player, pos, Direction.EAST));
    }

    /**
     * Mirrors {@code IronBarsBlock#attachsTo} for the neighbour in the given direction.
     * <p>
     * TODO verify against 26.2: not in the dumps.
     */
    private static boolean ironBarsAttachTo(InteractionContext player, Vector3i pos, Direction direction) {
        BlockState neighbour = relative(player, pos, direction);
        // instanceof IronBarsBlock: bound to the same placement handler
        if (InteractionRegistry.blockStateForPlacement(neighbour.block()) == IRON_BARS || is(player, neighbour, BlockTag.WALLS)) {
            return true;
        }
        return !isExceptionForConnection(player, neighbour) && isFaceSturdy(neighbour, direction.reversed());
    }

    /**
     * Mirrors {@code GrowingPlantBlock#canSurvive} with the growth direction, head and body block of each vanilla
     * growing plant.
     */
    private static boolean growingPlantCanSurvive(InteractionContext player, BlockState state, Vector3i pos) {
        Block block = state.block();
        Block head;
        Block body;
        Direction growthDirection;
        if (block == Blocks.KELP || block == Blocks.KELP_PLANT) {
            head = Blocks.KELP;
            body = Blocks.KELP_PLANT;
            growthDirection = Direction.UP;
        } else if (block == Blocks.WEEPING_VINES || block == Blocks.WEEPING_VINES_PLANT) {
            head = Blocks.WEEPING_VINES;
            body = Blocks.WEEPING_VINES_PLANT;
            growthDirection = Direction.DOWN;
        } else if (block == Blocks.TWISTING_VINES || block == Blocks.TWISTING_VINES_PLANT) {
            head = Blocks.TWISTING_VINES;
            body = Blocks.TWISTING_VINES_PLANT;
            growthDirection = Direction.UP;
        } else if (block == Blocks.CAVE_VINES || block == Blocks.CAVE_VINES_PLANT) {
            head = Blocks.CAVE_VINES;
            body = Blocks.CAVE_VINES_PLANT;
            growthDirection = Direction.DOWN;
        } else {
            // Unknown growing plant
            return true;
        }
        BlockState attachedTo = relative(player, pos, growthDirection.reversed());
        // GrowingPlantBlock#canAttachTo; TODO verify against 26.2: only kelp overrides it (not in the dumps)
        if (head == Blocks.KELP && is(player, attachedTo, BlockTag.CANNOT_SUPPORT_KELP)) {
            return false;
        }
        return attachedTo.is(head) || attachedTo.is(body) || isFaceSturdy(attachedTo, growthDirection);
    }

    /**
     * Mirrors {@code GrowingPlantBodyBlock#canBeReplaced}: the plant's head item can't replace its body.
     */
    private static boolean growingPlantBodyCanBeReplaced(PlacementContext context, BlockState state, boolean replacingClickedOnBlock) {
        if (!Placements.BlockCanBeReplaced.DEFAULT.canBeReplaced(context, state, replacingClickedOnBlock)) {
            return false;
        }
        Block block = state.block();
        Block head;
        if (block == Blocks.KELP_PLANT) {
            head = Blocks.KELP;
        } else if (block == Blocks.WEEPING_VINES_PLANT) {
            head = Blocks.WEEPING_VINES;
        } else if (block == Blocks.TWISTING_VINES_PLANT) {
            head = Blocks.TWISTING_VINES;
        } else if (block == Blocks.CAVE_VINES_PLANT) {
            head = Blocks.CAVE_VINES;
        } else {
            return true;
        }
        GeyserItemStack stack = context.itemInHand();
        return !stack.is(head.asItem());
    }

    private static @Nullable BlockState ladderStateForPlacement(PlacementContext context, Block block) {
        if (!context.replacingClickedOnBlock()) {
            // Not against the back of another ladder
            BlockState clicked = context.blockAt(context.hit().position());
            if (clicked.is(block) && clicked.getValue(Properties.HORIZONTAL_FACING) == context.clickedFace()) {
                return null;
            }
        }
        return firstSurvivingHorizontal(context, InteractionTables.defaultState(block), true);
    }

    private static @Nullable BlockState lanternStateForPlacement(PlacementContext context, Block block) {
        for (Direction direction : context.nearestLookingDirections()) {
            if (direction.isVertical()) {
                BlockState state = InteractionTables.defaultState(block).withValue(Properties.HANGING, direction == Direction.UP);
                if (PlacementPrediction.canSurvive(context.player(), state, context.clickedPos())) {
                    return state;
                }
            }
        }
        return null;
    }

    private PlacementRulesAL() {
    }
}
