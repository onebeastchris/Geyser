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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.block.type.bonemealable.BoneMealableBlock;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.util.BlockPlaceContext;
import org.geysermc.geyser.util.InteractionContext;

public abstract class GrowingPlantBodyBlock extends GrowingPlantBlock implements BoneMealableBlock {

    public GrowingPlantBodyBlock(String javaIdentifier, Direction direction, Builder builder) {
        super(javaIdentifier, direction, builder);
    }

    @Override
    public boolean canBeReplaced(BlockPlaceContext context) {
        // todo test whether item matches
        boolean superReplaced = super.canBeReplaced(context);
        return (!superReplaced || !context.itemInHand().is(this.item)) && superReplaced;
    }

    private @Nullable Vector3i headPosition(GeyserSession session, BlockState state, Vector3i position) {
        Vector3i clone = position.clone();

        BlockState newState;
        do {
            clone = growingDirection.relative(clone);
            newState = session.getGeyser().getWorldManager().blockAt(session, clone);
        } while (newState.is(state.block()));

        return newState.is(getHeadBlock()) ? clone : null;
    }

    @Override
    public boolean bonemealEffective(InteractionContext context) {
        Vector3i position = headPosition(context.session(), context.state(), context.blockPosition());
        if (position != null) {
            Vector3i next = growingDirection.relative(position);
            return getHeadBlock().allowedToGrowIn(context.getWorldManager().blockAt(context.session(), next));
        }

        return false;
    }

    @Override
    protected Block getBodyBlock() {
        return this;
    }
}
