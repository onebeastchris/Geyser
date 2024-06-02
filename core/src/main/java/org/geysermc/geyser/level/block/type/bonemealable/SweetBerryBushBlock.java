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

package org.geysermc.geyser.level.block.type.bonemealable;

import org.cloudburstmc.protocol.bedrock.data.SoundEvent;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.util.InteractionContext;
import org.geysermc.geyser.util.InteractionResult;

public class SweetBerryBushBlock extends Block implements BoneMealableBlock {

    public SweetBerryBushBlock(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    @Override
    public InteractionResult interactWith(InteractionContext context) {
        int age = context.state().getValue(Properties.AGE_3);
        if (age != 3 && context.itemInHand().is(Items.BONE_MEAL)) {
            // Bone meal should be run instead
            return InteractionResult.PASS;
        } else if (age > 1 && context.mainHand()) {
            // Picking off berries
            // todo sound?
            context.playSound(SoundEvent.SWEET_BERRY_BUSH_PICK);
            return InteractionResult.SUCCESS;
        } else {
            return super.interactWith(context);
        }
    }


    @Override
    public boolean bonemealEffective(InteractionContext context) {
        return context.state().getValue(Properties.AGE_3) < 3;
    }
}
