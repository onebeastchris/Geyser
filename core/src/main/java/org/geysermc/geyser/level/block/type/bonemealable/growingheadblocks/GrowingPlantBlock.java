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

package org.geysermc.geyser.level.block.type.bonemealable.growingheadblocks;

import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.level.physics.SupportType;
import org.geysermc.geyser.util.InteractionContext;

public abstract class GrowingPlantBlock extends Block {
    protected Direction growingDirection;

    public GrowingPlantBlock(String javaIdentifier, Direction direction, Builder builder) {
        super(javaIdentifier, builder);
        this.growingDirection = direction;
    }

    @Override
    public boolean canSurvive(InteractionContext context) {
        Vector3i opposite = growingDirection.reversed().relative(context.blockPosition());
        BlockState oppositeBlock = context.getWorldManager().blockAt(context.session(), opposite);
        return canAttachTo(oppositeBlock) && (oppositeBlock.is(getHeadBlock())
            || oppositeBlock.is(getBodyBlock())
            || oppositeBlock.isFaceSturdy(growingDirection, SupportType.FULL));
    }

    protected boolean canAttachTo(BlockState blockState) {
        return true;
    }

    protected abstract GrowingPlantHeadBlock getHeadBlock();

    protected abstract Block getBodyBlock();
}
