/*
 * Copyright (c) 2024 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.level.block.type.bonemealable;

import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.block.type.bush.DoublePlantBlock;
import org.geysermc.geyser.util.InteractionContext;

public class PitcherCropBlock extends DoublePlantBlock implements BoneMealableBlock {
    public PitcherCropBlock(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    @Override
    public boolean bonemealEffective(InteractionContext context) {
        BlockState state;
        Vector3i pos = context.blockPosition();

        if (!isLowerHalf(context.state())) {
            if (!isLowerHalf(context.belowBlockState())) {
                return false;
            }
            state = context.belowBlockState();
        } else {
            state = context.state();
        }

        BlockState above = context.getWorldManager().blockAt(context.session(), pos.up());
        return canGrow(state, above, state.getValue(Properties.AGE_4) + 1);
    }

    public static boolean isLowerHalf(BlockState state) {
        return state.is(Blocks.PITCHER_CROP) && "lower".equals(state.getValue(Properties.DOUBLE_BLOCK_HALF));
    }

    private boolean canGrow(BlockState state, BlockState above, int newAge) {
        // TODO sufficient light check
        return state.getValue(Properties.AGE_4) < 4 && (!isDouble(newAge) || canGrowInto(above));
    }

    private static boolean isDouble(int i) {
        return i >= 3;
    }

    private boolean canGrowInto(BlockState state) {
        return state.isAir() || state.is(Blocks.PITCHER_CROP);
    }
}
