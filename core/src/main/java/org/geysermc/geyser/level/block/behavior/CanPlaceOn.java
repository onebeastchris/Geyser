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

import org.geysermc.geyser.level.block.BlockStateValues;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.Fluid;
import org.geysermc.geyser.level.block.type.IceBlock;
import org.geysermc.geyser.level.block.type.bush.BushBlock;
import org.geysermc.geyser.level.block.type.bush.DeadBushBlock;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.level.physics.SupportType;
import org.geysermc.geyser.session.cache.tags.BlockTag;

import java.util.function.Predicate;

public final class CanPlaceOn {

    public static final Predicate<BushBlock.PlaceOnContext> FARMLAND_CHECK = context -> context.state().is(Blocks.FARMLAND);
    public static final Predicate<BushBlock.PlaceOnContext> DEFAULT = FARMLAND_CHECK.or(context -> context.is(BlockTag.DIRT));
    public static final Predicate<BushBlock.PlaceOnContext> CLAY_OR_DEFAULT = DEFAULT.or(context -> context.state().is(Blocks.CLAY));
    public static final Predicate<BushBlock.PlaceOnContext> NYLIUM_SOULSOIL_DEFAULT = DEFAULT.or(context -> context.is(BlockTag.NYLIUM) || context.state().is(Blocks.SOUL_SOIL));
    public static final Predicate<BushBlock.PlaceOnContext> UP_STURY_NOT_MAGMA = context -> context.state().isFaceSturdy(Direction.UP, SupportType.FULL) && !context.state().is(Blocks.MAGMA_BLOCK);

    public static final Predicate<BushBlock.PlaceOnContext> ATTACHED_STEM_BLOCK = FARMLAND_CHECK;
    public static final Predicate<BushBlock.PlaceOnContext> AZALEA_BLOCK = CLAY_OR_DEFAULT;
    public static final Predicate<BushBlock.PlaceOnContext> CROP_BLOCK = FARMLAND_CHECK;
    public static final Predicate<BushBlock.PlaceOnContext> DEAD_BUSH_BLOCK = context -> context.is(BlockTag.DEAD_BUSH_MAY_PLACE_ON);
    public static final Predicate<BushBlock.PlaceOnContext> FUNGUS_BLOCK = NYLIUM_SOULSOIL_DEFAULT.or(context -> context.state().is(Blocks.MYCELIUM));
    public static final Predicate<BushBlock.PlaceOnContext> MANGROVE_PROPAGULE_BLOCK = CLAY_OR_DEFAULT;
    public static final Predicate<BushBlock.PlaceOnContext> MUSHROOM_BLOCK = context -> context.state().block().isSolidRender();
    public static final Predicate<BushBlock.PlaceOnContext> NETHER_SPROUTS_BLOCK = NYLIUM_SOULSOIL_DEFAULT;
    public static final Predicate<BushBlock.PlaceOnContext> NETHER_WART_BLOCK = context -> context.state().is(Blocks.SOUL_SAND);
    public static final Predicate<BushBlock.PlaceOnContext> PITCHER_CROP_BLOCK = FARMLAND_CHECK;
    public static final Predicate<BushBlock.PlaceOnContext> ROOTS_BLOCK = NYLIUM_SOULSOIL_DEFAULT;
    public static final Predicate<BushBlock.PlaceOnContext> SEA_PICKLE_BLOCK = context -> {
        return context.state().isFaceSturdy(Direction.UP, SupportType.FULL); // TODO collision shape check
    };
    public static final Predicate<BushBlock.PlaceOnContext> SEAGRASS_BLOCK = UP_STURY_NOT_MAGMA;
    public static final Predicate<BushBlock.PlaceOnContext> SMALL_DRIPLEAF_BLOCK = context -> context.is(BlockTag.SMALL_DRIPLEAF_PLACEABLE) || context.isWaterSourceAbove() && DEFAULT.test(context);
    public static final Predicate<BushBlock.PlaceOnContext> STEM_BLOCK = FARMLAND_CHECK;
    public static final Predicate<BushBlock.PlaceOnContext> TALL_SEAGRASS_BLOCK = UP_STURY_NOT_MAGMA;
    public static final Predicate<BushBlock.PlaceOnContext> WATERLILY_BLOCK = context -> {
        Fluid fluid = BlockStateValues.getFluid(context.state().javaId());
        Fluid above = BlockStateValues.getFluid(context.aboveBlockState());
        return (fluid == Fluid.WATER || context.state().block() instanceof IceBlock) && above == Fluid.EMPTY;
    };
    public static final Predicate<BushBlock.PlaceOnContext> WITHER_ROSE_BLOCK = DEFAULT.or(context -> {
        return context.state().is(Blocks.NETHERRACK, Blocks.SOUL_SAND, Blocks.SOUL_SOIL);
    });

}
