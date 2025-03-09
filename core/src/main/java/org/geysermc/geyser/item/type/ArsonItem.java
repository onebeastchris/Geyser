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

package org.geysermc.geyser.item.type;

import org.cloudburstmc.protocol.bedrock.data.SoundEvent;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.session.cache.tags.BlockTag;
import org.geysermc.geyser.util.ChunkUtils;
import org.geysermc.geyser.util.InteractionContext;
import org.geysermc.geyser.util.InteractionResult;

// FlintAndSteel and FireCharges have (almost) the same behavior.
public class ArsonItem extends Item {

    public ArsonItem(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    @Override
    public InteractionResult useOn(InteractionContext context) {

        boolean success = false;
        if (canBeLit(context)) {
            playSound(context);
            if (!context.isMainHand()) {
                ChunkUtils.updateBlock(context.session(), context.state().withValue(Properties.LIT, true), context.blockPosition());
            }
            success = true;
        } else { // Not a special block that can be lit, check if fire survives
            BlockState state = context.relativeBlockState();
//            if (!state.isAir()) {
//                Vector3i underRelativeBlockPos = context.interactFace().relative(context.blockPosition()).down();
//                BlockState belowRelativeState = context.getWorldManager().blockAt(context.session(), underRelativeBlockPos);
//
//            }
            success = state.isAir() && !state.is(Blocks.WATER); // TODO proper checks, e.g. whether fire can actually survive
        }

        return success ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    static boolean canBeLit(InteractionContext context) {
        // Check for campfire
        if (context.isBlockTag(BlockTag.CAMPFIRES) &&
            context.session().getCampfireCache().contains(context.blockPosition()) &&
            !context.state().getValue(Properties.LIT, false) &&
            !context.state().getValue(Properties.WATERLOGGED, false)) {
            return true;
        }

        // Check for candles
        if (context.isBlockTag(BlockTag.CANDLES) &&
            !context.state().getValue(Properties.LIT) &&
            !context.state().getValue(Properties.WATERLOGGED)
        ) {
            return true;
        }

        // Check for candle cakes
        return context.isBlockTag(BlockTag.CANDLE_CAKES) && !context.state().getValue(Properties.LIT);
    }

    private void playSound(InteractionContext context) {
        if (context.itemInHand().is(Items.FLINT_AND_STEEL)) {
            context.playSound(SoundEvent.IGNITE);
        } else {
            // TODO
        }
    }

}
