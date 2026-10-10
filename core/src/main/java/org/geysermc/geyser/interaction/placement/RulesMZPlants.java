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
import org.geysermc.geyser.level.block.property.IntegerProperty;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.session.cache.tags.BlockTag;
import org.geysermc.geyser.session.cache.tags.Tag;

import java.util.IdentityHashMap;
import java.util.Map;

import static org.geysermc.geyser.interaction.placement.RulesMZWorld.below;
import static org.geysermc.geyser.interaction.placement.RulesMZWorld.is;

/**
 * Placement rules of plants and other small blocks that stack or depend on the block below them: vegetation
 * ({@code VegetationBlock#canSurvive} with the {@code mayPlaceOn} overrides of its subclasses), sea pickles, turtle eggs,
 * sugar cane, dripleaves, seagrass, ...
 * <p>
 * Geyser does not know light levels, so light requirements (mushrooms, pitcher crops) are assumed to be met.
 */
public final class RulesMZPlants {

    /**
     * Mirrors {@code VegetationBlock#mayPlaceOn} (or an override) of one block.
     */
    @FunctionalInterface
    public interface MayPlaceOn {
        /**
         * @param state the state below the plant
         * @param pos the position of that state
         */
        boolean mayPlaceOn(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos);
    }

    private static final MayPlaceOn VEGETATION = tag(BlockTag.SUPPORTS_VEGETATION);
    /**
     * {@code mayPlaceOn} is not a hook of the generated class data, so the overrides are listed per block.
     */
    private static final Map<Block, MayPlaceOn> MAY_PLACE_ON = new IdentityHashMap<>();

    static {
        // AzaleaBlock, CactusFlowerBlock, DryVegetationBlock and LilyPadBlock: see PlacementRulesAL#vegetationMayPlaceOn
        // StemBlock and AttachedStemBlock take their support tag as a constructor argument
        // TODO verify against 26.2 (Blocks registration): the per-stem tags are assumed
        MAY_PLACE_ON.put(Blocks.PUMPKIN_STEM, tag(BlockTag.SUPPORTS_PUMPKIN_STEM));
        MAY_PLACE_ON.put(Blocks.ATTACHED_PUMPKIN_STEM, tag(BlockTag.SUPPORTS_PUMPKIN_STEM));
        MAY_PLACE_ON.put(Blocks.MELON_STEM, tag(BlockTag.SUPPORTS_MELON_STEM));
        MAY_PLACE_ON.put(Blocks.ATTACHED_MELON_STEM, tag(BlockTag.SUPPORTS_MELON_STEM));
        // NetherFungusBlock and NetherRootsBlock take their support tag as a constructor argument
        // TODO verify against 26.2 (Blocks registration)
        MAY_PLACE_ON.put(Blocks.CRIMSON_FUNGUS, tag(BlockTag.SUPPORTS_CRIMSON_FUNGUS));
        MAY_PLACE_ON.put(Blocks.WARPED_FUNGUS, tag(BlockTag.SUPPORTS_WARPED_FUNGUS));
        MAY_PLACE_ON.put(Blocks.CRIMSON_ROOTS, tag(BlockTag.SUPPORTS_CRIMSON_ROOTS));
        MAY_PLACE_ON.put(Blocks.WARPED_ROOTS, tag(BlockTag.SUPPORTS_WARPED_ROOTS));
        // NetherSproutsBlock, NetherWartBlock, WitherRoseBlock, MangrovePropaguleBlock
        MAY_PLACE_ON.put(Blocks.NETHER_SPROUTS, tag(BlockTag.SUPPORTS_NETHER_SPROUTS));
        MAY_PLACE_ON.put(Blocks.NETHER_WART, tag(BlockTag.SUPPORTS_NETHER_WART));
        MAY_PLACE_ON.put(Blocks.WITHER_ROSE, tag(BlockTag.SUPPORTS_WITHER_ROSE));
        MAY_PLACE_ON.put(Blocks.MANGROVE_PROPAGULE, tag(BlockTag.SUPPORTS_MANGROVE_PROPAGULE));
        // CropBlock and PitcherCropBlock
        MayPlaceOn crops = tag(BlockTag.SUPPORTS_CROPS);
        MAY_PLACE_ON.put(Blocks.WHEAT, crops);
        MAY_PLACE_ON.put(Blocks.CARROTS, crops);
        MAY_PLACE_ON.put(Blocks.POTATOES, crops);
        MAY_PLACE_ON.put(Blocks.BEETROOTS, crops);
        MAY_PLACE_ON.put(Blocks.TORCHFLOWER_CROP, crops);
        MAY_PLACE_ON.put(Blocks.PITCHER_CROP, crops);
        // MushroomBlock
        MAY_PLACE_ON.put(Blocks.BROWN_MUSHROOM, RulesMZPlants::mushroomMayPlaceOn);
        MAY_PLACE_ON.put(Blocks.RED_MUSHROOM, RulesMZPlants::mushroomMayPlaceOn);
        // SeaPickleBlock: anything with collision on top
        MAY_PLACE_ON.put(Blocks.SEA_PICKLE, (player, state, pos) ->
            InteractionTables.StateFlag.COLLISION_FACE_UP.test(state) || RulesMZWorld.isFaceSturdy(state, Direction.UP));
        // SeagrassBlock and TallSeagrassBlock
        MAY_PLACE_ON.put(Blocks.SEAGRASS, RulesMZPlants::seagrassMayPlaceOn);
        MAY_PLACE_ON.put(Blocks.TALL_SEAGRASS, RulesMZPlants::seagrassMayPlaceOn);
        // SmallDripleafBlock
        MAY_PLACE_ON.put(Blocks.SMALL_DRIPLEAF, (player, state, pos) -> is(player, state, BlockTag.SUPPORTS_SMALL_DRIPLEAF)
            || RulesMZWorld.hasWaterSource(player.blockAt(pos.add(0, 1, 0))) && VEGETATION.mayPlaceOn(player, state, pos));
    }

    private static MayPlaceOn tag(Tag<Block> tag) {
        return (player, state, pos) -> is(player, state, tag);
    }

    private static boolean mushroomMayPlaceOn(InteractionContext player, BlockState state, Vector3i pos) {
        return InteractionTables.StateFlag.SOLID_RENDER.test(state);
    }

    private static boolean seagrassMayPlaceOn(InteractionContext player, BlockState state, Vector3i pos) {
        return RulesMZWorld.isFaceSturdy(state, Direction.UP) && !is(player, state, BlockTag.CANNOT_SUPPORT_SEAGRASS);
    }

    /**
     * Mirrors {@code mayPlaceOn} of the given (vegetation) block.
     *
     * @param state the state below the plant
     * @param pos the position of that state
     */
    public static boolean mayPlaceOn(@NonNull InteractionContext player, @NonNull Block block, @NonNull BlockState state, @NonNull Vector3i pos) {
        Boolean otherRule = PlacementRulesAL.vegetationMayPlaceOn(player, block, state, pos);
        if (otherRule != null) {
            return otherRule;
        }
        return MAY_PLACE_ON.getOrDefault(block, VEGETATION).mayPlaceOn(player, state, pos);
    }

    /**
     * Mirrors {@code VegetationBlock#canSurvive}: {@code mayPlaceOn} of the block below. This is also what
     * {@code super.canSurvive} means for subclasses such as {@code CropBlock} and the lower half of a {@code DoublePlantBlock}.
     */
    public static boolean vegetationCanSurvive(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos) {
        Vector3i belowPos = pos.add(0, -1, 0);
        return mayPlaceOn(player, state.block(), player.blockAt(belowPos), belowPos);
    }

    /**
     * Mirrors the upper half part of {@code DoublePlantBlock#canSurvive}.
     */
    private static boolean upperHalfCanSurvive(InteractionContext player, BlockState state, Vector3i pos) {
        BlockState belowState = below(player, pos);
        return belowState.is(state.block()) && "lower".equals(belowState.getValue(Properties.DOUBLE_BLOCK_HALF));
    }

    private static boolean isUpperHalf(BlockState state) {
        return "upper".equals(state.getValue(Properties.DOUBLE_BLOCK_HALF));
    }

    /**
     * {@code MushroomBlock#canSurvive}. The light level ({@code getRawBrightness < 13}) is not known and assumed to be fine.
     */
    public static boolean mushroomCanSurvive(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos) {
        Vector3i belowPos = pos.add(0, -1, 0);
        BlockState belowState = player.blockAt(belowPos);
        return is(player, belowState, BlockTag.OVERRIDES_MUSHROOM_LIGHT_REQUIREMENT) || mayPlaceOn(player, state.block(), belowState, belowPos);
    }

    /**
     * {@code PitcherCropBlock#canSurvive}. The light level ({@code sufficientLight}) is not known and assumed to be fine.
     */
    public static boolean pitcherCropCanSurvive(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos) {
        return isUpperHalf(state) ? upperHalfCanSurvive(player, state, pos) : vegetationCanSurvive(player, state, pos);
    }

    /**
     * {@code MangrovePropaguleBlock#canSurvive}.
     */
    public static boolean mangrovePropaguleCanSurvive(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos) {
        if (state.getValue(Properties.HANGING, false)) {
            return is(player, RulesMZWorld.above(player, pos), BlockTag.SUPPORTS_HANGING_MANGROVE_PROPAGULE);
        }
        return vegetationCanSurvive(player, state, pos);
    }

    /**
     * {@code MangrovePropaguleBlock#getStateForPlacement}: a grown, standing propagule.
     */
    public static @NonNull BlockState mangrovePropaguleStateForPlacement(@NonNull PlacementContext context, @NonNull Block block) {
        return RulesMZWorld.withIfPresent(block.defaultBlockState(), Properties.AGE_4, 4);
    }

    /**
     * {@code SeaPickleBlock#canBeReplaced} and {@code TurtleEggBlock#canBeReplaced}: more can be added to a stack of fewer
     * than four.
     */
    public static boolean stackCanBeReplaced(@NonNull PlacementContext context, @NonNull BlockState state, boolean replacingClickedOnBlock,
                                             @NonNull IntegerProperty amount) {
        if (!context.isSecondaryUseActive() && context.itemInHand().is(state.block().asItem()) && state.getValue(amount, 4) < 4) {
            return true;
        }
        return Placements.BlockCanBeReplaced.DEFAULT.canBeReplaced(context, state, replacingClickedOnBlock);
    }

    /**
     * {@code SeaPickleBlock#getStateForPlacement} and {@code TurtleEggBlock#getStateForPlacement}: one more in the clicked
     * stack, or a new one.
     */
    public static @NonNull BlockState stackStateForPlacement(@NonNull PlacementContext context, @NonNull Block block, @NonNull IntegerProperty amount) {
        BlockState state = context.blockAt(context.clickedPos());
        if (state.is(block)) {
            return state.withValue(amount, Math.min(4, state.getValue(amount) + 1));
        }
        return block.defaultBlockState();
    }

    /**
     * Mirrors {@code SegmentableBlock#canBeReplaced(state, context, segment)}, e.g. for flower beds and leaf litter.
     * Callers fall back to {@link Placements.BlockCanBeReplaced#DEFAULT} if this is false.
     */
    public static boolean segmentCanBeReplaced(@NonNull PlacementContext context, @NonNull BlockState state, @NonNull IntegerProperty segment) {
        return !context.isSecondaryUseActive() && context.itemInHand().is(state.block().asItem()) && state.getValue(segment, 4) < 4;
    }

    /**
     * Mirrors {@code SegmentableBlock#getStateForPlacement(context, block, segment, facing)}, e.g. for flower beds and
     * leaf litter.
     */
    public static @NonNull BlockState segmentStateForPlacement(@NonNull PlacementContext context, @NonNull Block block, @NonNull IntegerProperty segment) {
        BlockState state = context.blockAt(context.clickedPos());
        if (state.is(block)) {
            return state.withValue(segment, Math.min(4, state.getValue(segment) + 1));
        }
        return RulesMZWorld.withFacing(block.defaultBlockState(), context.horizontalDirection().reversed());
    }

    /**
     * {@code SmallDripleafBlock#getStateForPlacement}: like a double plant.
     */
    public static @Nullable BlockState smallDripleafStateForPlacement(@NonNull PlacementContext context, @NonNull Block block) {
        BlockState state = PlacementRulesAL.doublePlantStateForPlacement(context, block);
        return state == null ? null : RulesMZWorld.withFacing(state, context.horizontalDirection().reversed());
    }

    /**
     * {@code SmallDripleafBlock#canSurvive}.
     */
    public static boolean smallDripleafCanSurvive(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos) {
        return isUpperHalf(state) ? upperHalfCanSurvive(player, state, pos) : vegetationCanSurvive(player, state, pos);
    }

    /**
     * {@code SeagrassBlock#getStateForPlacement}: only in full water.
     */
    public static @Nullable BlockState seagrassStateForPlacement(@NonNull PlacementContext context, @NonNull Block block) {
        return RulesMZWorld.hasFullWater(context.blockAt(context.clickedPos())) ? block.defaultBlockState() : null;
    }

    /**
     * {@code TallSeagrassBlock#getStateForPlacement}: like a double plant, with full water above.
     */
    public static @Nullable BlockState tallSeagrassStateForPlacement(@NonNull PlacementContext context, @NonNull Block block) {
        BlockState state = PlacementRulesAL.doublePlantStateForPlacement(context, block);
        if (state != null && RulesMZWorld.hasFullWater(context.blockAt(context.clickedPos().add(0, 1, 0)))) {
            return state;
        }
        return null;
    }

    /**
     * {@code TallSeagrassBlock#canSurvive}.
     */
    public static boolean tallSeagrassCanSurvive(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos) {
        if (isUpperHalf(state)) {
            return upperHalfCanSurvive(player, state, pos);
        }
        return vegetationCanSurvive(player, state, pos) && RulesMZWorld.hasFullWater(player.blockAt(pos));
    }

    /**
     * {@code SugarCaneBlock#canSurvive}: on sugar cane, or on a supporting block next to water. The fluid tag
     * {@code supports_sugar_cane_adjacently} is approximated as any water.
     */
    public static boolean sugarCaneCanSurvive(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos) {
        BlockState belowState = below(player, pos);
        if (belowState.is(state.block())) {
            return true;
        }
        if (!is(player, belowState, BlockTag.SUPPORTS_SUGAR_CANE)) {
            return false;
        }
        Vector3i belowPos = pos.add(0, -1, 0);
        for (Direction direction : Direction.HORIZONTAL) {
            BlockState neighbour = player.blockAt(RulesMZWorld.relative(belowPos, direction));
            if (RulesMZWorld.hasWater(neighbour) || is(player, neighbour, BlockTag.SUPPORTS_SUGAR_CANE_ADJACENTLY)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@code SporeBlossomBlock#canSurvive}: below the center of a block, outside water.
     */
    public static boolean sporeBlossomCanSurvive(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos) {
        return InteractionTables.StateFlag.CENTER_DOWN.test(RulesMZWorld.above(player, pos)) && !RulesMZWorld.hasWater(player.blockAt(pos));
    }

    /**
     * {@code MossyCarpetBlock#canSurvive}: a base carpet needs any block below, a side-only carpet a base carpet below.
     */
    public static boolean mossyCarpetCanSurvive(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Vector3i pos) {
        BlockState belowState = below(player, pos);
        if (state.getValue(Properties.BOTTOM, true)) {
            return !RulesMZWorld.isAir(belowState);
        }
        return belowState.is(state.block()) && belowState.getValue(Properties.BOTTOM, false);
    }

    private RulesMZPlants() {
    }
}
