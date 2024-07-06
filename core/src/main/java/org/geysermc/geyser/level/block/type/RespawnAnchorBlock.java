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

package org.geysermc.geyser.level.block.type;

import org.cloudburstmc.protocol.bedrock.data.SoundEvent;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.util.InteractionContext;
import org.geysermc.geyser.util.InteractionResult;

public class RespawnAnchorBlock extends Block {

    public RespawnAnchorBlock(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    @Override
    public InteractionResult interactWith(InteractionContext context) {
        int charges = context.state().getValue(Properties.RESPAWN_ANCHOR_CHARGES);
        if (charges < 4) {
            if (isGlowstone(context.session(), context.mainHand())) {
                context.playSound(SoundEvent.RESPAWN_ANCHOR_CHARGE);
                return InteractionResult.SUCCESS;
            } else if (isGlowstone(context.session(), false)) {
                return InteractionResult.PASS; // will be charged by offhand
            }
        }

        if (charges != 0 && context.mainHand()) {
            if (!context.session().getDimensionType().respawn_anchor_works()) {
                return InteractionResult.SUCCESS; // boom!
            } else {
                return InteractionResult.CONSUME; // spawn setting
            }
        }

        return InteractionResult.PASS;
    }

    private boolean isGlowstone(GeyserSession session, boolean isMainHand) {
        return session.getPlayerInventory().getItemInHand(isMainHand).is(Items.GLOWSTONE);
    }
}