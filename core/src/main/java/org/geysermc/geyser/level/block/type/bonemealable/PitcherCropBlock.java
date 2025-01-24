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
import org.geysermc.geyser.level.block.type.TallPlantBlock;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.util.BlockPlaceContext;
import org.geysermc.geyser.util.InteractionContext;

public class PitcherCropBlock extends TallPlantBlock implements BoneMealableBlock {
    public PitcherCropBlock(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    @Override
    public boolean bonemealEffective(InteractionContext context) {
        return false; // TODO
    }

    @Override
    public boolean canBeReplaced(BlockPlaceContext context) {
        return false;
    }

    @Override
    public boolean canSurvive(InteractionContext context) {
        throw new IllegalStateException("pitchercrop#cansurvive is not implemented!");
        //return isLowerHalf(state) && !sufficientLight() ? false : super.canSurvive(session, state, position);
    }

    @Override
    protected boolean canPlaceOn(GeyserSession session, BlockState state, Vector3i position) {
        return state.is(Blocks.FARMLAND);
    }

    private boolean isLowerHalf(BlockState state) {
        return state.is(Blocks.PITCHER_CROP) && "lower".equals(state.getValue(Properties.DOUBLE_BLOCK_HALF));
    }
}
