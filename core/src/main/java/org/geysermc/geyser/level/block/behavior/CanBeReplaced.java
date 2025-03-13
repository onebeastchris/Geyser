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

import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.MultiFaceBlock;
import org.geysermc.geyser.level.block.type.bonemealable.growingheadblocks.GrowingPlantBodyBlock;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.util.BlockPlaceContext;
import org.geysermc.geyser.util.BlockUtils;

import java.util.Objects;
import java.util.function.Predicate;

public final class CanBeReplaced {
    public static final Predicate<BlockPlaceContext> DEFAULT = context -> {
        return context.block().canBeReplaced() && (context.itemInHand().isEmpty() || !context.itemInHand().is(context.block().asItem()));
    };
    public static final Predicate<BlockPlaceContext> SNOW_LAYER_BLOCK = context -> {
        int i = context.state().getValue(Properties.LAYERS);
        if (!context.itemInHand().is(context.block().asItem()) || i >= 8) {
            return i == 1;
        } else {
            return !context.willReplaceClickedBlock() || context.interactFace() == Direction.UP;
        }
    };
    public static final Predicate<BlockPlaceContext> VINE_BLOCK = context -> {
        if (context.state().is(Blocks.VINE)) {
            return BlockUtils.countVineFaces(context.state()) < context.block().propertyKeys().length;
        } else {
            return DEFAULT.test(context);
        }
    };
    public static final Predicate<BlockPlaceContext> MULTIFACE_BLOCK = context -> {
        if (!(context.block() instanceof MultiFaceBlock multiFaceBlock)) {
            throw new IllegalArgumentException("Expected a MultiFaceBlock, got " + context.block().javaIdentifier());
        }
        return !context.itemInHand().is(context.block().asItem()) || multiFaceBlock.canReplace(context);
    };
    public static final Predicate<BlockPlaceContext> SEA_PICKLE_BLOCK = context -> {
        return !context.isSecondaryActive() && context.itemInHand().is(context.block().asItem())
            && context.state().getValue(Properties.PICKLES) < 4 || DEFAULT.test(context);
    };
    public static final Predicate<BlockPlaceContext> SLAB_BLOCK = context -> {
        String slabType = context.state().getValue(Properties.SLAB_TYPE);
        if (Objects.equals(slabType, "double") || !context.itemInHand().is(context.block().asItem())) {
            return false;
        } else if (context.willReplaceClickedBlock()) {
            boolean upper = context.clickPosition().getY() - (double)context.blockPosition().getY() > 0.5;
            Direction direction = context.interactFace();
            return Objects.equals(slabType, "bottom")
                ? direction == Direction.UP || upper && direction.getAxis().isHorizontal()
                : direction == Direction.DOWN || !upper && direction.getAxis().isHorizontal();
        } else {
            return true;
        }
    };
    public static final Predicate<BlockPlaceContext> PITCHER_CROP_BLOCK = $ -> false;
    public static final Predicate<BlockPlaceContext> GROWING_PLANT_BODY_BLOCK = context -> {
        if (context.block() instanceof GrowingPlantBodyBlock bodyBlock) {
            boolean defaultBehavior = DEFAULT.test(context);
            return (!defaultBehavior || !context.itemInHand().is(bodyBlock.getHeadBlock().asItem())) && defaultBehavior;
        }
        throw new IllegalStateException("Attempted to query GrowingPlantBody canBeReplaced behavior for " + context.block().javaIdentifier());
    };
    public static final Predicate<BlockPlaceContext> TURTLE_EGG_BLOCK = context -> {
        return !context.isSecondaryActive() && context.itemInHand().is(context.block().asItem()) &&
            context.state().getValue(Properties.EGGS) < 4 || DEFAULT.test(context);
    };
    public static final Predicate<BlockPlaceContext> CANDLE_BLOCK = context -> {
        return !context.isSecondaryActive() &&
            context.itemInHand().is(context.block().asItem()) &&
            context.state().getValue(Properties.CANDLES) < 4 || DEFAULT.test(context);
    };
    public static final Predicate<BlockPlaceContext> SCAFFOLDING_BLOCK = context -> {
        return context.itemInHand().is(context.block().asItem());
    };
    public static final Predicate<BlockPlaceContext> PINK_PETALS_BLOCK = context -> {
        return !context.isSecondaryActive() && context.itemInHand().is(context.block().asItem()) &&
            context.state().getValue(Properties.FLOWER_AMOUNT) < 4 || DEFAULT.test(context);
    };
}
