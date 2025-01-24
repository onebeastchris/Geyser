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

package org.geysermc.geyser.level.block.type.bonemealable;

import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.level.block.BlockStateValues;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.block.type.TallPlantBlock;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.session.cache.tags.BlockTag;
import org.geysermc.geyser.util.InteractionContext;

public class SmallDripleafBlock extends TallPlantBlock implements BoneMealableBlock {

    public SmallDripleafBlock(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    @Override
    public boolean bonemealEffective(InteractionContext context) {
        return true;
    }

    @Override
    protected boolean canPlaceOn(GeyserSession session, BlockState state, Vector3i position) {
        Vector3i above = position.up();
        BlockState aboveBlockState = session.getGeyser().getWorldManager().blockAt(session, above);
        return session.getTagCache().is(BlockTag.SMALL_DRIPLEAF_PLACEABLE, state.block())
            || BlockStateValues.getWaterHeight(aboveBlockState.javaId()) != -1 && super.canPlaceOn(session, state, position);
    }

    @Override
    public boolean canSurvive(InteractionContext context) {
        if ("upper".equals(context.state().getValue(Properties.DOUBLE_BLOCK_HALF))) {
            return super.canSurvive(context);
        } else {
            return this.canPlaceOn(context.session(), context.belowBlockState(), context.blockPosition().down());
        }
    }
}
