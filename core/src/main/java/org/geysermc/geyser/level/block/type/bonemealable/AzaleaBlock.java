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
import org.geysermc.geyser.level.block.BlockStateValues;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.Fluid;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.block.type.bush.BushBlock;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.util.InteractionContext;

public class AzaleaBlock extends BushBlock implements BoneMealableBlock {
    public AzaleaBlock(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    @Override
    public boolean bonemealEffective(InteractionContext context) {
        return isEmptyFluid(context.aboveBlockState());
    }

    private boolean isEmptyFluid(BlockState state) {
        return BlockStateValues.getFluid(state.javaId()).equals(Fluid.EMPTY);
    }

    @Override
    protected boolean canPlaceOn(GeyserSession session, BlockState state, Vector3i position) {
        return state.is(Blocks.CLAY) || super.canPlaceOn(session, state, position);
    }
}
