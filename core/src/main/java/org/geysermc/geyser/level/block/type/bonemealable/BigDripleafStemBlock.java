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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.session.cache.tags.BlockTag;
import org.geysermc.geyser.util.InteractionContext;

public class BigDripleafStemBlock extends Block implements BoneMealableBlock {
    public BigDripleafStemBlock(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    @Override
    public boolean bonemealEffective(InteractionContext context) {
        Vector3i topBlock = checkAbove(context);
        if (topBlock == null) {
            return false;
        } else {
            Vector3i aboveTop = topBlock.up();
            BlockState aboveTopBlock = context.getWorldManager().blockAt(context.session(), aboveTop);
            return context.session().getDimensionType().isInsideDimension(aboveTop.getY()) && BigDripleafBlock.canReplaceAbove(aboveTopBlock);
        }
    }

    @Override
    public boolean canSurvive(InteractionContext context) {
        BlockState below = context.belowBlockState();
        BlockState above = context.aboveBlockState();
        return below.is(this) || context.session().getTagCache().is(BlockTag.BIG_DRIPLEAF_PLACEABLE, below.block()) &&
            above.is(this) || context.session().getTagCache().is(BlockTag.BIG_DRIPLEAF_PLACEABLE, above.block());
    }

    private @Nullable Vector3i checkAbove(InteractionContext context) {
        Vector3i pos = context.blockPosition().clone();

        BlockState state;
        do {
            pos = pos.up();
            state = context.getWorldManager().blockAt(context.session(), pos);
        } while (state.is(context.block()));

        return state.is(Blocks.BIG_DRIPLEAF_STEM) ? pos : null;
    }
}
