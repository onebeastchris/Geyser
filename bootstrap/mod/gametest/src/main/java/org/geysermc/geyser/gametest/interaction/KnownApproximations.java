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

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Placements where Geyser intentionally predicts success because it lacks the information vanilla uses, and a wrong
 * failure prediction would stop the player from placing the block at all. Only mismatches where vanilla fails and
 * Geyser succeeds are tolerated for these; they are logged, but don't fail the test.
 */
public final class KnownApproximations {
    private static final Map<Block, String> OPTIMISTIC_PLACEMENTS = Map.of(
        Blocks.BROWN_MUSHROOM, "light level (MushroomBlock#canSurvive)",
        Blocks.RED_MUSHROOM, "light level (MushroomBlock#canSurvive)"
    );

    /**
     * @return why placing the block of the stack may be predicted as successful when vanilla fails, or null if it
     * must not
     */
    public static @Nullable String optimisticPlacement(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem ? OPTIMISTIC_PLACEMENTS.get(blockItem.getBlock()) : null;
    }

    private KnownApproximations() {
    }
}
