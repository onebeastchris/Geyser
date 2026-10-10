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
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.EnumProperty;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.property.Property;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.registry.BlockRegistries;
import org.geysermc.geyser.session.cache.tags.Tag;

/**
 * World and block state helpers shared by the placement rules. Public so that other rule classes can use them, too.
 */
public final class RulesMZWorld {

    /**
     * Mirrors {@code BlockState#isAir}.
     */
    public static boolean isAir(@NonNull BlockState state) {
        Block block = state.block();
        return block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR;
    }

    public static boolean is(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Tag<Block> tag) {
        return player.isInTag(tag, state.block().javaId());
    }

    public static @NonNull Vector3i relative(@NonNull Vector3i pos, @NonNull Direction direction) {
        return pos.add(direction.getUnitVector());
    }

    public static @NonNull BlockState below(@NonNull InteractionContext player, @NonNull Vector3i pos) {
        return player.blockAt(pos.add(0, -1, 0));
    }

    public static @NonNull BlockState above(@NonNull InteractionContext player, @NonNull Vector3i pos) {
        return player.blockAt(pos.add(0, 1, 0));
    }

    /**
     * Mirrors {@code BlockState#isFaceSturdy} with the full support type.
     */
    public static boolean isFaceSturdy(@NonNull BlockState state, @NonNull Direction face) {
        return InteractionTables.isFaceSturdy(state, face);
    }

    /**
     * Mirrors {@code getFluidState().is(FluidTags.WATER)} (also {@code Level#isWaterAt}): water of any level, or a
     * waterlogged (or always water filled, like seagrass) state.
     */
    public static boolean hasWater(@NonNull BlockState state) {
        return state.is(Blocks.WATER) || BlockRegistries.WATERLOGGED.get().get(state.javaId());
    }

    /**
     * Mirrors {@code getFluidState().is(Fluids.WATER)} and {@code isSourceOfType(Fluids.WATER)}: a water source.
     * Waterlogged states always hold a source.
     */
    public static boolean hasWaterSource(@NonNull BlockState state) {
        if (state.is(Blocks.WATER)) {
            return state.getValue(Properties.LEVEL, 0) == 0;
        }
        return BlockRegistries.WATERLOGGED.get().get(state.javaId());
    }

    /**
     * Mirrors {@code fluidState.is(FluidTags.WATER) && fluidState.isFull()}: a water source or falling water.
     */
    public static boolean hasFullWater(@NonNull BlockState state) {
        if (state.is(Blocks.WATER)) {
            int level = state.getValue(Properties.LEVEL, 0);
            return level == 0 || level >= 8;
        }
        return BlockRegistries.WATERLOGGED.get().get(state.javaId());
    }

    /**
     * Mirrors {@code FluidState#isEmpty}.
     */
    public static boolean hasNoFluid(@NonNull BlockState state) {
        return !state.is(Blocks.WATER) && !state.is(Blocks.LAVA) && !BlockRegistries.WATERLOGGED.get().get(state.javaId());
    }

    /**
     * Mirrors {@code BlockState#canBeReplaced(BlockPlaceContext)} for a block other than the clicked one, after the
     * context was created.
     */
    public static boolean canBeReplaced(@NonNull PlacementContext context, @NonNull BlockState state) {
        return InteractionRegistry.blockCanBeReplaced(state.block()).canBeReplaced(context, state, context.replacingClickedOnBlock());
    }

    /**
     * {@code BlockPlaceContext#getClickedPos} as seen from a {@link Placements.BlockCanBeReplaced} handler. Those also run
     * while the context is created, when {@link PlacementContext#clickedPos()} does not know yet whether the clicked block
     * gets replaced.
     */
    public static @NonNull Vector3i clickedPos(@NonNull PlacementContext context, boolean replacingClickedOnBlock) {
        return replacingClickedOnBlock ? context.hit().position() : context.hit().adjacentPosition();
    }

    /**
     * Mirrors {@code context.getClickLocation().y - context.getClickedPos().getY() > 0.5}: whether the upper half of the
     * placement position was clicked.
     */
    public static boolean clickedUpperHalf(@NonNull PlacementContext context, @NonNull Vector3i clickedPos) {
        return context.clickLocation().getY() - clickedPos.getY() > 0.5;
    }

    /**
     * @return the facing property ({@code facing}, any set of directions) of the block, or null if it has none
     */
    @SuppressWarnings("unchecked")
    public static @Nullable EnumProperty<Direction> facingProperty(@NonNull Block block) {
        Property<?>[] keys = block.propertyKeys();
        if (keys == null) {
            return null;
        }
        for (Property<?> key : keys) {
            if (key == Properties.HORIZONTAL_FACING || key == Properties.FACING || key == Properties.FACING_HOPPER) {
                return (EnumProperty<Direction>) key;
            }
        }
        return null;
    }

    /**
     * Sets the facing property of the state, if the state has one.
     */
    public static @NonNull BlockState withFacing(@NonNull BlockState state, @NonNull Direction facing) {
        EnumProperty<Direction> property = facingProperty(state.block());
        return property == null ? state : state.withValue(property, facing);
    }

    /**
     * @return the facing of the state, or null if it has no facing property
     */
    public static @Nullable Direction facing(@NonNull BlockState state) {
        EnumProperty<Direction> property = facingProperty(state.block());
        return property == null ? null : state.getValue(property);
    }

    /**
     * Sets a property if the block of the state has it; vanilla blocks of one class all share their properties, but
     * this keeps the rules safe for blocks Geyser models with fewer properties.
     */
    public static <T extends Comparable<T>> @NonNull BlockState withIfPresent(@NonNull BlockState state, @NonNull Property<T> property, @NonNull T value) {
        return state.getValue(property) == null ? state : state.withValue(property, value);
    }

    /**
     * @return the boolean property of a block with six (or five) face properties, like vines and multiface blocks
     */
    public static @NonNull Property<Boolean> faceProperty(@NonNull Direction direction) {
        return switch (direction) {
            case DOWN -> Properties.DOWN;
            case UP -> Properties.UP;
            case NORTH -> Properties.NORTH;
            case SOUTH -> Properties.SOUTH;
            case WEST -> Properties.WEST;
            case EAST -> Properties.EAST;
        };
    }

    private RulesMZWorld() {
    }
}
