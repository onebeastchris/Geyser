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

import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.BoneMealableBlock;
import org.geysermc.geyser.util.InteractionContext;
import org.geysermc.geyser.util.InteractionResult;

public class BoneMealItem extends Item {
    public BoneMealItem(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    @Override
    public InteractionResult useOn(InteractionContext context) {
        if (context.block() instanceof BoneMealableBlock boneMealableBlock
                && boneMealableBlock.bonemealEffective(context.state())) {
            return InteractionResult.SUCCESS;
        }

        // TODO "sturdiness block face checking"
        var level = context.state().getValueNullable(Properties.LEVEL);
        if (context.block().equals(Blocks.WATER) && level != null && level == 8) {
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
