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
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.session.cache.tags.BlockTag;
import org.geysermc.geyser.session.cache.tags.Tag;

import java.util.IdentityHashMap;
import java.util.Map;

import static org.geysermc.geyser.interaction.item.UseOnWorld.above;
import static org.geysermc.geyser.interaction.item.UseOnWorld.below;
import static org.geysermc.geyser.interaction.item.UseOnWorld.isAir;
import static org.geysermc.geyser.interaction.item.UseOnWorld.isInTag;

/**
 * Mirrors {@code BonemealableBlock#isValidBonemealTarget} for every vanilla bonemealable block, which is all the client
 * checks before predicting a successful bone meal use ({@code BoneMealItem#growCrop}).
 * <p>
 * Which blocks are bonemealable at all comes from the generated interaction tables
 * ({@link InteractionTables.BlockFlag#BONEMEALABLE}); this class adds the state dependent part per block. A bonemealable
 * block without a check here is always a valid target: saplings, mushrooms, tall flowers, flower beds, small dripleaf,
 * torchflower crops and glow lichen, and any bonemealable block added in a newer version.
 * <p>
 * Most checks only depend on the block state and its direct neighbours and are exact. Known approximations:
 * <ul>
 *     <li>pitcher crops ignore the light level ({@code CropBlock#hasSufficientLight})</li>
 *     <li>glow lichen is assumed to always be able to spread</li>
 *     <li>the pale moss carpet topper check is simplified to "bottom carpet with a side, room above"</li>
 * </ul>
 */
final class UseOnBonemealTargets {

    @FunctionalInterface
    private interface Target {
        boolean isValid(InteractionContext player, Vector3i pos, BlockState state);
    }

    private static final Map<Block, Target> TARGETS = new IdentityHashMap<>();

    static {
        Target airAbove = (player, pos, state) -> isAir(above(player, pos));
        Target airBelow = (player, pos, state) -> isAir(below(player, pos));

        // CropBlock: !isMaxAge(state)
        put((player, pos, state) -> state.getValue(Properties.AGE_7) < 7, Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES);
        put((player, pos, state) -> state.getValue(Properties.AGE_3) < 3, Blocks.BEETROOTS);
        // TorchflowerCropBlock: max age is 2, but the block only has ages 0 and 1, so it is always valid (no entry)
        put(UseOnBonemealTargets::isValidPitcherCrop, Blocks.PITCHER_CROP);
        // StemBlock
        put((player, pos, state) -> state.getValue(Properties.AGE_7) != 7, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
        // CocoaBlock, SweetBerryBushBlock
        put((player, pos, state) -> state.getValue(Properties.AGE_2) < 2, Blocks.COCOA);
        put((player, pos, state) -> state.getValue(Properties.AGE_3) < 3, Blocks.SWEET_BERRY_BUSH);
        // CaveVinesBlock, CaveVinesPlantBlock
        put((player, pos, state) -> !state.getValue(Properties.BERRIES), Blocks.CAVE_VINES, Blocks.CAVE_VINES_PLANT);

        // SaplingBlock, AzaleaBlock: tree growers are only known to the server; never a valid target on the client
        put((player, pos, state) -> false, Blocks.AZALEA, Blocks.FLOWERING_AZALEA, Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING,
            Blocks.BIRCH_SAPLING, Blocks.JUNGLE_SAPLING, Blocks.ACACIA_SAPLING, Blocks.CHERRY_SAPLING, Blocks.DARK_OAK_SAPLING,
            Blocks.PALE_OAK_SAPLING);
        // Always valid, so no entry: MushroomBlock, TallFlowerBlock, FlowerBedBlock, SmallDripleafBlock,
        // and glow lichen (MultifaceSpreadeableBlock, approximated, see class documentation)
        // MangrovePropaguleBlock: !isHanging(state) || !isFullyGrown(state)
        put((player, pos, state) -> !state.getValue(Properties.HANGING) || state.getValue(Properties.AGE_4) < 4, Blocks.MANGROVE_PROPAGULE);
        // GrassBlock, NyliumBlock, BonemealableFeaturePlacerBlock (moss blocks), BambooSaplingBlock
        put(airAbove, Blocks.GRASS_BLOCK, Blocks.CRIMSON_NYLIUM, Blocks.WARPED_NYLIUM, Blocks.MOSS_BLOCK, Blocks.PALE_MOSS_BLOCK,
            Blocks.BAMBOO_SAPLING);
        // RootedDirtBlock, MangroveLeavesBlock
        put(airBelow, Blocks.ROOTED_DIRT, Blocks.MANGROVE_LEAVES);
        // TallGrassBlock: the grown double plant must survive here and there must be room above
        put((player, pos, state) -> isInTag(player, below(player, pos), BlockTag.SUPPORTS_VEGETATION) && isAir(above(player, pos)),
            Blocks.SHORT_GRASS, Blocks.FERN);
        // BushBlock, FireflyBushBlock, ShortDryGrassBlock, TallDryGrassBlock: BonemealableBlock#hasSpreadableNeighbourPos
        put((player, pos, state) -> hasSpreadableNeighbour(player, pos, BlockTag.SUPPORTS_VEGETATION), Blocks.BUSH, Blocks.FIREFLY_BUSH);
        put((player, pos, state) -> hasSpreadableNeighbour(player, pos, BlockTag.SUPPORTS_DRY_VEGETATION),
            Blocks.SHORT_DRY_GRASS, Blocks.TALL_DRY_GRASS);
        // FungusBlock: must stand on its nylium
        put((player, pos, state) -> below(player, pos).is(Blocks.CRIMSON_NYLIUM), Blocks.CRIMSON_FUNGUS);
        put((player, pos, state) -> below(player, pos).is(Blocks.WARPED_NYLIUM), Blocks.WARPED_FUNGUS);

        // GrowingPlantHeadBlock: canGrowInto(state in growth direction)
        put((player, pos, state) -> above(player, pos).is(Blocks.WATER), Blocks.KELP);
        put((player, pos, state) -> isAir(below(player, pos)), Blocks.WEEPING_VINES);
        put(airAbove, Blocks.TWISTING_VINES);
        // GrowingPlantBodyBlock: the head this body belongs to must be able to grow
        put((player, pos, state) -> headCanGrow(player, pos, Blocks.KELP_PLANT, Blocks.KELP, Direction.UP), Blocks.KELP_PLANT);
        put((player, pos, state) -> headCanGrow(player, pos, Blocks.WEEPING_VINES_PLANT, Blocks.WEEPING_VINES, Direction.DOWN),
            Blocks.WEEPING_VINES_PLANT);
        put((player, pos, state) -> headCanGrow(player, pos, Blocks.TWISTING_VINES_PLANT, Blocks.TWISTING_VINES, Direction.UP),
            Blocks.TWISTING_VINES_PLANT);

        // BigDripleafBlock, BigDripleafStemBlock
        put((player, pos, state) -> canBigDripleafGrowInto(player, pos.add(0, 1, 0)), Blocks.BIG_DRIPLEAF);
        put((player, pos, state) -> {
            Vector3i head = findConnectedEnd(player, pos, Blocks.BIG_DRIPLEAF_STEM, Blocks.BIG_DRIPLEAF, Direction.UP);
            return head != null && canBigDripleafGrowInto(player, head.add(0, 1, 0));
        }, Blocks.BIG_DRIPLEAF_STEM);

        // SeaPickleBlock: alive (waterlogged) and on a coral block
        put((player, pos, state) -> state.getValue(Properties.WATERLOGGED) && isInTag(player, below(player, pos), BlockTag.CORAL_BLOCKS),
            Blocks.SEA_PICKLE);
        // SeagrassBlock: water source above
        put((player, pos, state) -> UseOnWorld.isWaterSource(above(player, pos)), Blocks.SEAGRASS);

        // BambooStalkBlock: room to grow and the top not fully grown
        put(UseOnBonemealTargets::isValidBamboo, Blocks.BAMBOO);
        // HangingMossBlock: the block below the tip must be air
        put((player, pos, state) -> {
            Vector3i tip = findConnectedEnd(player, pos, Blocks.PALE_HANGING_MOSS, null, Direction.DOWN);
            return tip != null && isAir(player.blockAt(tip));
        }, Blocks.PALE_HANGING_MOSS);
        // MossyCarpetBlock: approximated, see class documentation
        put(UseOnBonemealTargets::isValidPaleMossCarpet, Blocks.PALE_MOSS_CARPET);
    }

    private static void put(Target target, Block... blocks) {
        for (Block block : blocks) {
            TARGETS.put(block, target);
        }
    }

    /**
     * Mirrors {@code state.getBlock() instanceof BonemealableBlock block && block.isValidBonemealTarget(level, pos, state)}.
     */
    static boolean isValidBonemealTarget(@NonNull InteractionContext player, @NonNull Vector3i pos, @NonNull BlockState state) {
        if (!InteractionTables.BlockFlag.BONEMEALABLE.test(state)) {
            return false;
        }
        Target target = TARGETS.get(state.block());
        return target == null || target.isValid(player, pos, state);
    }

    /**
     * Mirrors {@code BlockUtil#getTopConnectedBlock}: moves from {@code pos} in the given direction as long as the
     * blocks are {@code body}.
     *
     * @param end the block that must end the chain, or null to return the first position that is not {@code body}
     * @return the position of the end block, or null if the chain does not end with it
     */
    private static Vector3i findConnectedEnd(InteractionContext player, Vector3i pos, Block body, Block end, Direction direction) {
        Vector3i step = direction.getUnitVector();
        Vector3i current = pos;
        BlockState state;
        do {
            current = current.add(step);
            if (UseOnWorld.isOutsideBuildHeight(player, current.getY())) {
                // Outside the world there is only void air
                return end == null ? current : null;
            }
            state = player.blockAt(current);
        } while (state.is(body));
        return end == null || state.is(end) ? current : null;
    }

    private static boolean headCanGrow(InteractionContext player, Vector3i pos, Block body, Block head, Direction direction) {
        Vector3i headPos = findConnectedEnd(player, pos, body, head, direction);
        if (headPos == null) {
            return false;
        }
        BlockState growInto = player.blockAt(headPos.add(direction.getUnitVector()));
        return head == Blocks.KELP ? growInto.is(Blocks.WATER) : isAir(growInto);
    }

    /**
     * Mirrors {@code BigDripleafBlock#canPlaceAt}.
     */
    private static boolean canBigDripleafGrowInto(InteractionContext player, Vector3i pos) {
        if (UseOnWorld.isOutsideBuildHeight(player, pos.getY())) {
            return false;
        }
        BlockState state = player.blockAt(pos);
        return isAir(state) || state.is(Blocks.WATER);
    }

    /**
     * Mirrors {@code BonemealableBlock#hasSpreadableNeighbourPos}: an empty horizontal neighbour the plant could survive on.
     */
    private static boolean hasSpreadableNeighbour(InteractionContext player, Vector3i pos, Tag<Block> supportingBlocks) {
        for (Direction direction : Direction.HORIZONTAL) {
            Vector3i neighbour = pos.add(direction.getUnitVector());
            if (isAir(player.blockAt(neighbour)) && isInTag(player, below(player, neighbour), supportingBlocks)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Mirrors {@code PitcherCropBlock#isValidBonemealTarget}, ignoring the light level.
     */
    private static boolean isValidPitcherCrop(InteractionContext player, Vector3i pos, BlockState state) {
        Vector3i lowerPos = pos;
        BlockState lower = state;
        if ("upper".equals(state.getValue(Properties.DOUBLE_BLOCK_HALF))) {
            lowerPos = pos.add(0, -1, 0);
            lower = player.blockAt(lowerPos);
            if (!lower.is(Blocks.PITCHER_CROP) || !"lower".equals(lower.getValue(Properties.DOUBLE_BLOCK_HALF))) {
                return false;
            }
        }
        int age = lower.getValue(Properties.AGE_4);
        if (age >= 4) {
            return false;
        }
        // Growing to age 3 and beyond makes the crop two blocks tall
        if (age + 1 < 3) {
            return true;
        }
        BlockState above = above(player, lowerPos);
        return isAir(above) || above.is(Blocks.PITCHER_CROP);
    }

    /**
     * Mirrors {@code BambooStalkBlock#isValidBonemealTarget}.
     */
    private static boolean isValidBamboo(InteractionContext player, Vector3i pos, BlockState state) {
        int heightAbove = 0;
        while (heightAbove < 16 && player.blockAt(pos.add(0, heightAbove + 1, 0)).is(Blocks.BAMBOO)) {
            heightAbove++;
        }
        int heightBelow = 0;
        while (heightBelow < 16 && player.blockAt(pos.add(0, -(heightBelow + 1), 0)).is(Blocks.BAMBOO)) {
            heightBelow++;
        }
        Vector3i growthPos = pos.add(0, heightAbove + 1, 0);
        return heightAbove + heightBelow + 1 < 16
            && player.blockAt(pos.add(0, heightAbove, 0)).getValue(Properties.STAGE, 0) != 1
            && !UseOnWorld.isOutsideBuildHeight(player, growthPos.getY())
            && isAir(player.blockAt(growthPos));
    }

    /**
     * Approximates {@code MossyCarpetBlock#isValidBonemealTarget}: a bottom carpet with at least one side can grow a topper
     * into an empty block above.
     */
    private static boolean isValidPaleMossCarpet(InteractionContext player, Vector3i pos, BlockState state) {
        if (!state.getValue(Properties.BOTTOM)) {
            return false;
        }
        BlockState above = above(player, pos);
        if (!isAir(above) && !(above.is(Blocks.PALE_MOSS_CARPET) && !above.getValue(Properties.BOTTOM))) {
            return false;
        }
        return !"none".equals(state.getValue(Properties.NORTH_WALL)) || !"none".equals(state.getValue(Properties.EAST_WALL))
            || !"none".equals(state.getValue(Properties.SOUTH_WALL)) || !"none".equals(state.getValue(Properties.WEST_WALL));
    }

    private UseOnBonemealTargets() {
    }
}
