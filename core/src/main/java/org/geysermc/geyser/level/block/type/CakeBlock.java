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
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.session.cache.tags.ItemTag;
import org.geysermc.geyser.util.InteractionContext;
import org.geysermc.geyser.util.InteractionResult;

public class CakeBlock extends Block {

    public CakeBlock(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    @Override
    public InteractionResult interactWithItem(InteractionContext context) {
        if (context.is(ItemTag.CANDLES) && context.state().getValue(Properties.BITES) == 0) {
            context.playSound(SoundEvent.CAKE_ADD_CANDLE);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_EMPTY_HAND;
    }

    @Override
    public InteractionResult interact(InteractionContext context) {
        if (eat(context).consumesAction()) {
            return InteractionResult.SUCCESS;
        }

        if (context.mainHand().isEmpty()) {
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    private InteractionResult eat(InteractionContext context) {
        if (context.session().canEat(false)) {
            return InteractionResult.PASS;
        } else {
            return InteractionResult.SUCCESS;
        }
    }
}
