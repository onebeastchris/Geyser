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
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.block.type.BushBlock;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.session.cache.tags.BlockTag;
import org.geysermc.geyser.util.BlockPlaceContext;
import org.geysermc.geyser.util.InteractionContext;

public class SeaPickleBlock extends BushBlock implements BoneMealableBlock {
    public SeaPickleBlock(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    @Override
    public boolean canBeReplaced(BlockPlaceContext context) {
        return !context.isSecondaryActive() && context.itemInHand().is(this.item)
            && context.state().getValue(Properties.PICKLES) < 4 || super.canBeReplaced(context);
    }

    @Override
    public boolean canSurvive(InteractionContext context) {
        return this.canPlaceOn(context.session(), context.belowBlockState(), context.blockPosition().down());
    }

    @Override
    protected boolean canPlaceOn(GeyserSession session, BlockState state, Vector3i position) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public boolean bonemealEffective(InteractionContext context) {
        return !context.state().getValue(Properties.WATERLOGGED)
            && context.session().getTagCache().is(BlockTag.CORAL_BLOCKS, context.belowBlockState().block());
    }
}
