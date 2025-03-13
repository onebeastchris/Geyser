/*
 * Copyright (c) 2025 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.level.block.behavior;

import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.level.block.BlockStateValues;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.Fluid;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.block.type.MovingPistonBlock;
import org.geysermc.geyser.level.block.type.bonemealable.PitcherCropBlock;
import org.geysermc.geyser.level.block.type.bush.BushBlock;
import org.geysermc.geyser.level.block.type.bush.DoublePlantBlock;
import org.geysermc.geyser.level.block.type.bush.TallSeagrassBlock;
import org.geysermc.geyser.level.block.type.interactive.DoorBlock;
import org.geysermc.geyser.level.block.type.interactive.FenceGateBlock;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.level.physics.SupportType;
import org.geysermc.geyser.session.cache.tags.BlockTag;
import org.geysermc.geyser.util.BlockUtils;
import org.geysermc.geyser.util.InteractionContext;

import java.util.function.Predicate;

public final class CanSurvive {

    public static final Predicate<InteractionContext> BUSH_BLOCK = context -> {
        if (context.block() instanceof BushBlock bushBlock) {
            return bushBlock.canPlaceOn(new BushBlock.PlaceOnContext(context.session(), context.belowBlockState(), context.blockPosition().down()));
        }
        throw new IllegalStateException("Called BushBlock can survive check for block " + context.state().block().javaIdentifier());
    };
    public static final Predicate<InteractionContext> DOUBLE_PLANT_BLOCK = context -> {
        if (context.state().getValue(Properties.DOUBLE_BLOCK_HALF).equals("upper")) {
            BlockState below = context.belowBlockState();
            return below.block() instanceof DoublePlantBlock && below.getValue(Properties.DOUBLE_BLOCK_HALF).equals("lower");
        } else {
            return BUSH_BLOCK.test(context);
        }
    };

    public static final Predicate<InteractionContext> AMETHYST_CLUSTER_BLOCK = context -> {
        Direction direction = context.state().getValue(Properties.FACING);
        Vector3i relative = direction.reversed().relative(context.blockPosition());
        return context.getWorldManager().blockAt(context.session(), relative).isFaceSturdy(direction, SupportType.FULL);
    };
    public static final Predicate<InteractionContext> MANGROVE_PROPAGULE_BLOCK = context -> context.state().getValue(Properties.HANGING) ? context.aboveBlockState().is(Blocks.MANGROVE_LEAVES) : BUSH_BLOCK.test(context);
    public static final Predicate<InteractionContext> BASE_RAIL_BLOCK = context -> context.belowBlockState().isFaceSturdy(Direction.UP, SupportType.RIGID);
    public static final Predicate<InteractionContext> TALL_SEAGRASS_BLOCK = context -> {
        if ("upper".equals(context.state().getValue(Properties.DOUBLE_BLOCK_HALF))) {
            BlockState below = context.belowBlockState();
            return below.block() instanceof TallSeagrassBlock && "lower".equals(below.getValue(Properties.DOUBLE_BLOCK_HALF));
        } else {
            int waterLevel = BlockStateValues.getWaterLevel(context.state().javaId());
            return DOUBLE_PLANT_BLOCK.test(context) && waterLevel == 8;
        }
    };
    public static final Predicate<InteractionContext> PISTON_HEAD_BLOCK = context -> {
        // TODO
        throw new RuntimeException("not implemented!");
    };
    public static final Predicate<InteractionContext> MUSHROOM_BLOCK = context -> {
        // TODO
        throw new RuntimeException("not implemented!");
    };
    public static final Predicate<InteractionContext> BASE_TORCH_BLOCK = context -> BlockUtils.canSupportCenter(context.session(), context.belowBlockState(), Direction.UP);
    public static final Predicate<InteractionContext> WALL_TORCH_BLOCK = context -> {
        Direction direction = context.state().getValue(Properties.HORIZONTAL_FACING);
        BlockState relativeToDirection = context.blockStateAt(direction.reversed().relative(context.blockPosition()));
        return relativeToDirection.isFaceSturdy(direction, SupportType.FULL);
    };
    public static final Predicate<InteractionContext> FIRE_BLOCK = context -> {
        // TODO isValidFireLocation check
        return context.belowBlockState().isFaceSturdy(Direction.UP, SupportType.FULL);
    };
    public static final Predicate<InteractionContext> SOUL_FIRE_BLOCK = context -> context.isBlockTag(BlockTag.SOUL_FIRE_BASE_BLOCKS, context.belowBlockState().block());
    public static final Predicate<InteractionContext> RED_STONE_WIRE_BLOCK = context ->  {
        return context.belowBlockState().isFaceSturdy(Direction.UP, SupportType.FULL) || context.belowBlockState().is(Blocks.HOPPER);
    };
    public static final Predicate<InteractionContext> CROP_BLOCK = context -> {
        // TODO brightness check
        return BUSH_BLOCK.test(context);
    };
    public static final Predicate<InteractionContext> FARM_BLOCK = context -> {
        Block above = context.aboveBlockState().block();
        return !context.aboveBlockState().isSolid() || above instanceof FenceGateBlock || above instanceof MovingPistonBlock;
    };
    public static final Predicate<InteractionContext> STANDING_SIGN_BLOCK = context -> context.belowBlockState().isSolid();
    public static final Predicate<InteractionContext> DOOR_BLOCK = context -> {
        boolean lower = context.state().getValue(Properties.DOUBLE_BLOCK_HALF).equals("lower");
        return lower ? context.belowBlockState().isFaceSturdy(Direction.UP, SupportType.FULL) : context.belowBlockState().block() instanceof DoorBlock;
    };
    public static final Predicate<InteractionContext> LADDER_BLOCK = context -> {
        Direction facing = context.state().getValue(Properties.HORIZONTAL_FACING);
        Vector3i relative = facing.reversed().relative(context.blockPosition());
        return context.blockStateAt(relative).isFaceSturdy(facing, SupportType.FULL);
    };
    public static final Predicate<InteractionContext> WALL_SIGN_BLOCK = context -> {
        Direction facing = context.state().getValue(Properties.HORIZONTAL_FACING);
        Vector3i relative = facing.reversed().relative(context.blockPosition());
        return context.blockStateAt(relative).isSolid();
    };
    public static final Predicate<InteractionContext> CEILING_HANGING_SIGN_BLOCK = context -> {
        return context.aboveBlockState().isFaceSturdy(Direction.DOWN, SupportType.CENTER);
    };
    public static final Predicate<InteractionContext> FACE_ATTACHED_HORIZONTAL_DIRECTIONAL_BLOCK = context -> {
        Direction connectedDirection = switch (context.state().getValue(Properties.ATTACH_FACE)) {
            case "ceiling" -> Direction.DOWN;
            case "floor" -> Direction.UP;
            default -> context.state().getValue(Properties.HORIZONTAL_FACING);
        };
        Vector3i relativeBlockPos = connectedDirection.reversed().relative(context.blockPosition());
        return context.blockStateAt(relativeBlockPos).isFaceSturdy(connectedDirection, SupportType.FULL);
    };
    public static final Predicate<InteractionContext> BASE_PRESSURE_PLATE_BLOCK = context -> {
        return BlockUtils.canSupportRigidBlock(context.belowBlockState())
            || BlockUtils.canSupportCenter(context.session(), context.belowBlockState(), Direction.UP);
    };
    public static final Predicate<InteractionContext> REDSTONE_WALL_TORCH_BLOCK = WALL_TORCH_BLOCK;
    public static final Predicate<InteractionContext> SNOW_LAYER_BLOCK = context -> {
        BlockState below = context.belowBlockState();
        if (context.isBlockTag(BlockTag.SNOW_LAYER_CANNOT_SURVIVE_ON)) {
            return false;
        } else {
            if (context.isBlockTag(BlockTag.SNOW_LAYER_CAN_SURVIVE_ON)) {
                return true;
            } else {
                // TODO add "isFaceFull" check
                return below.is(Blocks.SNOW) && below.getValue(Properties.LAYERS) == 8;
            }
        }
    };
    public static final Predicate<InteractionContext> CACTUS_BLOCK = context -> {
        for (Direction dir : Direction.HORIZONTAL) {
            BlockState relative = context.getWorldManager().blockAt(context.session(), dir.relative(context.blockPosition()));
            if (relative.isSolid() || BlockStateValues.getLavaLevel(relative.javaId()) != -1) {
                return false;
            }
        }

        BlockState below = context.belowBlockState();
        return (below.is(Blocks.CACTUS) || context.isBlockTag(BlockTag.SAND, below.block())) &&
            BlockStateValues.getFluid(context.aboveBlockState().javaId()).equals(Fluid.EMPTY);
    };
    public static final Predicate<InteractionContext> SUGAR_CANE_BLOCK = context -> {
        BlockState below = context.belowBlockState();
        if (below.is(Blocks.SUGAR_CANE)) {
            return true;
        } else {
            if (context.isBlockTag(BlockTag.DIRT, below.block()) || context.isBlockTag(BlockTag.SAND, below.block())) {
                for (Direction dir : Direction.HORIZONTAL) {
                    BlockState relative = context.blockStateAt(dir.relative(context.blockPosition()));
                    if (relative.is(Blocks.FROSTED_ICE) || BlockStateValues.getWaterHeight(relative.javaId()) != -1) {
                        return true;
                    }
                }
            }

            return false;
        }
    };
    public static final Predicate<InteractionContext> CAKE_BLOCK = context -> {
        return context.belowBlockState().isSolid();
    };
    public static final Predicate<InteractionContext> DIODE_BLOCK = context -> {
        return context.belowBlockState().isFaceSturdy(Direction.UP, SupportType.RIGID);
    };
    public static final Predicate<InteractionContext> VINE_BLOCK = context -> {
        return BlockUtils.countVineFaces(context.state()) > 0;
    };
    public static final Predicate<InteractionContext> COCOA_BLOCK = context -> {
        Direction facing = context.state().getValue(Properties.HORIZONTAL_FACING);
        BlockState relative = context.blockStateAt(facing.relative(context.blockPosition()));
        return context.isBlockTag(BlockTag.JUNGLE_LOGS, relative.block());
    };
    public static final Predicate<InteractionContext> TRIP_WIRE_HOOK_BLOCK = context -> {
        Direction facing = context.state().getValue(Properties.HORIZONTAL_FACING);
        BlockState relative = context.blockStateAt(facing.reversed().relative(context.blockPosition()));
        return relative.isFaceSturdy(facing, SupportType.FULL);
    };
    public static final Predicate<InteractionContext> CARPET_BLOCK = context -> !context.belowBlockState().isAir();
    public static final Predicate<InteractionContext> BANNER_BLOCK = context -> context.belowBlockState().isSolid();
    public static final Predicate<InteractionContext> WALL_BANNER_BLOCK = context -> {
        Direction facing = context.state().getValue(Properties.HORIZONTAL_FACING);
        BlockState relative = context.blockStateAt(facing.reversed().relative(context.blockPosition()));
        return relative.isSolid();
    };
    public static final Predicate<InteractionContext> CHORUS_PLANT_BLOCK = context -> {
        BlockState below = context.belowBlockState();
        boolean notSurroundedByAir = !context.aboveBlockState().isAir() && !below.isAir();

        for (Direction direction : Direction.HORIZONTAL) {
            Vector3i relative = direction.relative(context.blockPosition());
            BlockState relativeState = context.getWorldManager().blockAt(context.session(), relative);
            if (relativeState.block().is(Blocks.CHORUS_PLANT)) {
                if (notSurroundedByAir) {
                    return false;
                }

                BlockState belowRelative = context.getWorldManager().blockAt(context.session(), relative.down());
                if (belowRelative.block().is(Blocks.CHORUS_PLANT) || belowRelative.is(Blocks.END_STONE)) {
                    return true;
                }
            }
        }

        return below.is(Blocks.CHORUS_PLANT) || below.is(Blocks.END_STONE);
    };
    public static final Predicate<InteractionContext> CHORUS_FLOWER_BLOCK = context -> {
        BlockState below = context.belowBlockState();
        if (!below.is(Blocks.CHORUS_PLANT) && !below.is(Blocks.END_STONE)) {
            if (!below.isAir()) {
                return false;
            } else {
                boolean foundOne = false;

                for (Direction direction : Direction.HORIZONTAL) {
                    BlockState relative = context.getWorldManager().blockAt(context.session(), direction.relative(context.blockPosition()));
                    if (relative.is(Blocks.CHORUS_PLANT)) {
                        if (foundOne) {
                            return false;
                        }
                        foundOne = true;
                    } else if (!relative.isAir()) {
                        return false;
                    }
                }

                return foundOne;
            }
        }

        return true;
    };
    public static final Predicate<InteractionContext> PITCHER_CROP_BLOCK = context -> {
        if (PitcherCropBlock.isLowerHalf(context.state())) {
            // TODO light check
        }

        return DOUBLE_PLANT_BLOCK.test(context);
    };
    public static final Predicate<InteractionContext> DIRT_PATH_BLOCK = context -> !context.aboveBlockState().isSolid() || context.aboveBlockState().block() instanceof FenceGateBlock;
    public static final Predicate<InteractionContext> GROWING_PLANT_BLOCK = context -> {
        // TODO
        throw new RuntimeException("not implemented!");
    };
    public static final Predicate<InteractionContext> BASE_CORAL_PLANT_TYPE_BLOCK = context -> context.belowBlockState().isFaceSturdy(Direction.UP, SupportType.FULL);
    public static final Predicate<InteractionContext> BASE_CORAL_WALL_FAN_BLOCK = context -> {
        Direction facing = context.state().getValue(Properties.HORIZONTAL_FACING);
        BlockState relative = context.blockStateAt(facing.reversed().relative(context.blockPosition()));
        return relative.isFaceSturdy(facing, SupportType.FULL);
    };
    public static final Predicate<InteractionContext> SEA_PICKLE_BLOCK = context -> CanPlaceOn.SEA_PICKLE_BLOCK.test(BushBlock.PlaceOnContext.of(context));
    public static final Predicate<InteractionContext> BAMBOO_SAPLING_BLOCK = context -> context.isBlockTag(BlockTag.BAMBOO_PLANTABLE_ON, context.belowBlockState().block());
    public static final Predicate<InteractionContext> BAMBOO_STALK_BLOCK = BAMBOO_SAPLING_BLOCK;
    public static final Predicate<InteractionContext> BUBBLE_COLUMN_BLOCK = context -> context.belowBlockState().is(Blocks.BUBBLE_COLUMN, Blocks.MAGMA_BLOCK, Blocks.SOUL_SAND);
    public static final Predicate<InteractionContext> SCAFFOLDING_BLOCK = context -> BlockUtils.getScaffoldingDistance(context.toBlockPlaceContext()) < 7;
    public static final Predicate<InteractionContext> BELL_BLOCK = context -> {
        // TODO
        throw new RuntimeException("not implemented!");
    };
    public static final Predicate<InteractionContext> LANTERN_BLOCK = context -> {
        // TODO
        throw new RuntimeException("not implemented!");
    };
    public static final Predicate<InteractionContext> CANDLE_BLOCK = context -> BlockUtils.canSupportCenter(context.session(), context.belowBlockState(), Direction.UP);
    public static final Predicate<InteractionContext> CANDLE_CAKE_BLOCK = context -> context.belowBlockState().isSolid();
    public static final Predicate<InteractionContext> POINTED_DRIPSTONE_BLOCK = context -> {
        // TODO
        throw new RuntimeException("not implemented!");
    };
    public static final Predicate<InteractionContext> SPORE_BLOSSOM_BLOCK = context -> {
        return BlockUtils.canSupportCenter(context.session(), context.aboveBlockState(), Direction.DOWN)
            && BlockStateValues.getWaterHeight(context.state().javaId()) == -1;
    };
    public static final Predicate<InteractionContext> DRIPLEAF_BASE_CHECK = context -> {
        return context.belowBlockState().is(Blocks.BIG_DRIPLEAF_STEM)
            || context.isBlockTag(BlockTag.BIG_DRIPLEAF_PLACEABLE, context.belowBlockState().block());
    };
    public static final Predicate<InteractionContext> BIG_DRIPLEAF_BLOCK = DRIPLEAF_BASE_CHECK.or(context -> context.belowBlockState().is(Blocks.BIG_DRIPLEAF));
    public static final Predicate<InteractionContext> BIG_DRIPLEAF_STEM_BLOCK = DRIPLEAF_BASE_CHECK.and(context -> {
        return context.aboveBlockState().is(Blocks.BIG_DRIPLEAF_STEM, Blocks.BIG_DRIPLEAF);
    });
    public static final Predicate<InteractionContext> SMALL_DRIPLEAF_BLOCK = context -> {
        if (context.state().getValue(Properties.DOUBLE_BLOCK_HALF).equals("upper")) {
            return DOUBLE_PLANT_BLOCK.test(context);
        } else {
            return CanPlaceOn.SMALL_DRIPLEAF_BLOCK.test(BushBlock.PlaceOnContext.of(context));
        }
    };
    public static final Predicate<InteractionContext> HANGING_ROOTS_BLOCK = context -> context.aboveBlockState().isFaceSturdy(Direction.DOWN, SupportType.FULL);
    public static final Predicate<InteractionContext> FROGSPAWN_BLOCK = context -> {
        // TODO fluids
        throw new RuntimeException("not implemented!");
    };
    public static final Predicate<InteractionContext> MOSSY_CARPET_BLOCK = context -> {
        if (context.state().getValue(Properties.BOTTOM)) {
            return !context.belowBlockState().isAir();
        } else {
            BlockState below = context.belowBlockState();
            return below.is(Blocks.PALE_MOSS_CARPET) && below.getValue(Properties.BOTTOM);
        }
    };
    public static final Predicate<InteractionContext> HANGING_MOSS_BLOCK = context -> {
        // TODO
        throw new RuntimeException("not implemented!");
    };
}
