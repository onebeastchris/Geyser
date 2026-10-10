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
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.interaction.InteractionContext;
import org.geysermc.geyser.interaction.InteractionRegistry;
import org.geysermc.geyser.interaction.InteractionTables;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.registry.BlockRegistries;
import org.geysermc.geyser.session.cache.tags.Tag;

/**
 * World and block state queries used by {@link PlacementRulesAL}.
 */
final class RulesALWorld {

    static @NonNull BlockState relative(@NonNull InteractionContext player, @NonNull Vector3i pos, @NonNull Direction direction) {
        return player.blockAt(pos.add(direction.getUnitVector()));
    }

    static @NonNull BlockState below(@NonNull InteractionContext player, @NonNull Vector3i pos) {
        return player.blockAt(pos.down());
    }

    static @NonNull BlockState above(@NonNull InteractionContext player, @NonNull Vector3i pos) {
        return player.blockAt(pos.up());
    }

    static boolean is(@NonNull InteractionContext player, @NonNull BlockState state, @NonNull Tag<Block> tag) {
        return player.isInTag(tag, state.block().javaId());
    }

    /**
     * Mirrors {@code BlockState#isAir}.
     */
    static boolean isAir(@NonNull BlockState state) {
        Block block = state.block();
        return block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR;
    }

    /**
     * Mirrors {@code BlockState#isFaceSturdy} with the full support type.
     */
    static boolean isFaceSturdy(@NonNull BlockState state, @NonNull Direction face) {
        return InteractionTables.isFaceSturdy(state, face);
    }

    /**
     * Mirrors {@code Block#canSupportCenter} for the top face of the given state.
     */
    static boolean canSupportCenterUp(@NonNull BlockState state) {
        return InteractionTables.StateFlag.CENTER_UP.test(state);
    }

    /**
     * Mirrors {@code Block#canSupportCenter} for the bottom face of the given state.
     */
    static boolean canSupportCenterDown(@NonNull BlockState state) {
        return InteractionTables.StateFlag.CENTER_DOWN.test(state);
    }

    /**
     * Mirrors {@code Block#canSupportRigidBlock}.
     */
    static boolean canSupportRigidBlock(@NonNull BlockState state) {
        return InteractionTables.StateFlag.RIGID_UP.test(state);
    }

    /**
     * Mirrors the legacy {@code BlockState#isSolid}.
     */
    static boolean isSolid(@NonNull BlockState state) {
        return InteractionTables.StateFlag.SOLID.test(state);
    }

    /**
     * Mirrors {@code BlockState#liquid}: the water and lava blocks themselves, not waterlogged blocks.
     */
    static boolean isLiquidBlock(@NonNull BlockState state) {
        return state.is(Blocks.WATER) || state.is(Blocks.LAVA);
    }

    private static boolean isWaterlogged(@NonNull BlockState state) {
        // Includes the blocks that always contain water (kelp, seagrass, bubble columns)
        return BlockRegistries.WATERLOGGED.get().get(state.javaId());
    }

    /**
     * Mirrors {@code FluidState#isEmpty} of the state's fluid.
     */
    static boolean hasNoFluid(@NonNull BlockState state) {
        return !isLiquidBlock(state) && !isWaterlogged(state);
    }

    /**
     * Mirrors {@code fluidState.is(FluidTags.LAVA)}.
     */
    static boolean hasLava(@NonNull BlockState state) {
        return state.is(Blocks.LAVA);
    }

    /**
     * Mirrors {@code fluidState.is(Fluids.WATER)} (and {@code isSourceOfType(Fluids.WATER)}): a water source, including
     * waterlogged blocks.
     */
    static boolean hasWaterSource(@NonNull BlockState state) {
        if (state.is(Blocks.WATER)) {
            return state.getValue(Properties.LEVEL, 0) == 0;
        }
        return isWaterlogged(state);
    }

    /**
     * Mirrors {@code fluidState.is(FluidTags.WATER) && fluidState.isFull()}: a water source, falling water (level 8 and
     * above) or a waterlogged block.
     */
    static boolean hasFullWater(@NonNull BlockState state) {
        if (state.is(Blocks.WATER)) {
            int level = state.getValue(Properties.LEVEL, 0);
            return level == 0 || level >= 8;
        }
        return isWaterlogged(state);
    }

    /**
     * Mirrors {@code level.getBlockState(pos).canBeReplaced(context)} for a position other than the clicked one, e.g. the
     * upper half of a door.
     */
    static boolean canBeReplaced(@NonNull PlacementContext context, @NonNull Vector3i pos) {
        BlockState state = context.blockAt(pos);
        return InteractionRegistry.blockCanBeReplaced(state.block()).canBeReplaced(context, state, context.replacingClickedOnBlock());
    }

    private RulesALWorld() {
    }
}
