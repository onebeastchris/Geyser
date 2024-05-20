/*
 * Copyright (c) 2019-2024 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.item.type;

import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.util.InteractionResult;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;

public class ShovelItem extends Item {
    public ShovelItem(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    @Override
    public InteractionResult useOn(GeyserSession session, Vector3i blockPosition, Vector3f clickPosition, int blockFace, Hand hand) {
        Direction direction = Direction.VALUES[blockFace];
        if (direction != Direction.DOWN) {
            BlockState state = session.getGeyser().getWorldManager().blockAt(session, blockPosition);
            boolean airAbove = session.getGeyser().getWorldManager().blockAt(session, blockPosition.clone().add(Vector3i.UNIT_Y)).is(Blocks.AIR);

            if ((airAbove && isFlattenable(state)) || isLitCampfire(state)) {
                // todo extinguish particles for campfire, or path make sounds?
                return InteractionResult.SUCCESS;
            }

        }
        return InteractionResult.PASS;
    }

    private boolean isFlattenable(BlockState state) {
        return state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) ||
                state.is(Blocks.PODZOL) || state.is(Blocks.COARSE_DIRT) ||
                state.is(Blocks.MYCELIUM) || state.is(Blocks.ROOTED_DIRT);
    }

    private boolean isLitCampfire(BlockState state) {
        boolean campfire = state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE);
        return campfire && state.getValue(Properties.LIT, false);
    }
}
