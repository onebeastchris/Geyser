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
import org.geysermc.geyser.level.WorldManager;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.session.cache.tags.BlockTag;
import org.geysermc.geyser.util.InteractionContext;

public class BambooBlock extends Block implements BoneMealableBlock {
    public BambooBlock(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    @Override
    public boolean bonemealEffective(InteractionContext context) {
        WorldManager manager = context.session().getGeyser().getWorldManager();
        int above = getHeightAbove(context.session(), context.blockPosition());
        int below = getHeightBelow(context.session(), context.blockPosition());
        return above + below < 16 && manager.blockAt(context.session(), context.blockPosition().up(above)).getValue(Properties.STAGE) != 1;
    }

    @Override
    public boolean canSurvive(InteractionContext context) {
        return context.session().getTagCache().is(BlockTag.BAMBOO_PLANTABLE_ON, context.belowBlockState().block());
    }

    int getHeightAbove(GeyserSession session, Vector3i position) {
        WorldManager manager = session.getGeyser().getWorldManager();

        int i = 0;
        while (i < 16 && manager.blockAt(session, position.up()).is(Blocks.BAMBOO)) {
            i++;
        }

        return i;
    }

    int getHeightBelow(GeyserSession session, Vector3i position) {
        WorldManager manager = session.getGeyser().getWorldManager();

        int i = 0;
        while (i < 16 && manager.blockAt(session, position.down()).is(Blocks.BAMBOO)) {
            i++;
        }

        return i;
    }
}
