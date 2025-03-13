/*
 * Copyright (c) 2024-2025 GeyserMC. http://geysermc.org
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

import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.block.type.bonemealable.BoneMealableBlock;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.util.InteractionContext;

public abstract class GrowingPlantHeadBlock extends GrowingPlantBlock implements BoneMealableBlock {

    public GrowingPlantHeadBlock(String javaIdentifier, Direction direction, Builder builder) {
        super(javaIdentifier, direction, builder);
    }

    @Override
    public boolean bonemealEffective(InteractionContext context) {
        return allowedToGrowIn(context.getWorldManager().blockAt(context.session(), growingDirection.relative(context.blockPosition())));
    }

    protected abstract boolean allowedToGrowIn(BlockState state);

    @Override
    public GrowingPlantHeadBlock getHeadBlock() {
        return this;
    }
}
