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

package org.geysermc.geyser.gametest.interaction;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The area around the tested position. Scenario setups may place blocks around the tested block; the area is
 * restored to its original block states after every scenario.
 */
public final class WorldArea {
    /**
     * How far around the tested position setups may change blocks.
     */
    public static final int RADIUS = 2;

    private final ServerLevel level;
    private final BlockPos center;
    private final Map<BlockPos, BlockState> original = new LinkedHashMap<>();

    public WorldArea(ServerLevel level, BlockPos center) {
        this.level = level;
        this.center = center.immutable();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RADIUS, -RADIUS, -RADIUS), center.offset(RADIUS, RADIUS, RADIUS))) {
            original.put(pos.immutable(), level.getBlockState(pos));
        }
    }

    public BlockPos center() {
        return center;
    }

    /**
     * @return whether everything in the area was air when the test started
     */
    public boolean wasEmpty() {
        return original.values().stream().allMatch(BlockState::isAir);
    }

    /**
     * Restores the area, then places the given state at the center.
     */
    public void place(BlockState state) {
        restore();
        level.setBlock(center, state, InteractionComparison.PLACE_FLAGS);
    }

    public void restore() {
        original.forEach((pos, state) -> {
            if (level.getBlockState(pos) != state) {
                level.setBlock(pos, state, InteractionComparison.PLACE_FLAGS);
            }
        });
    }
}
