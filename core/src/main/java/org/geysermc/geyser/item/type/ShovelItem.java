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

import org.cloudburstmc.protocol.bedrock.data.SoundEvent;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.block.type.CampfireBlock;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.util.InteractionContext;
import org.geysermc.geyser.util.InteractionResult;

public class ShovelItem extends Item {
    public ShovelItem(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    @Override
    public InteractionResult useOn(InteractionContext context) {
        if (context.interactFace() != Direction.DOWN) {
            boolean airAbove = context.aboveBlockState().isAir();

            if (airAbove && isFlattenable(context.state())) {
                context.sendLevelSoundEventPacket(SoundEvent.ITEM_USE_ON, Blocks.DIRT_PATH.javaId());
                return InteractionResult.SUCCESS;
            }

            if (isLitCampfire(context.state())) {
                // TODO particles CampfireBlock#dowse
                return InteractionResult.SUCCESS;
            }

        }
        return InteractionResult.PASS;
    }

    // TODO mappings
    private boolean isFlattenable(BlockState state) {
        return state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) ||
                state.is(Blocks.PODZOL) || state.is(Blocks.COARSE_DIRT) ||
                state.is(Blocks.MYCELIUM) || state.is(Blocks.ROOTED_DIRT);
    }

    private boolean isLitCampfire(BlockState state) {
        return state.block() instanceof CampfireBlock && state.getValue(Properties.LIT, false);
    }
}
