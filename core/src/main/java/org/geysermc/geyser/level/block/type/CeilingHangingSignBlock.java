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

import org.geysermc.geyser.item.type.HangingSignItem;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.level.physics.SupportType;
import org.geysermc.geyser.util.InteractionContext;
import org.geysermc.geyser.util.InteractionResult;

public class CeilingHangingSignBlock extends SignBlock {

    public CeilingHangingSignBlock(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    @Override
    public InteractionResult interactWithItem(InteractionContext context) {
        // TODO check for sign block entity

        if (canPlaceAnotherSign(context)) {
            return InteractionResult.PASS;
        }

        return super.interactWithItem(context);
    }

    private boolean canPlaceAnotherSign(InteractionContext context) {
        return context.itemInHand().asItem() instanceof HangingSignItem
                && context.interactFace() == Direction.DOWN
                && !canExecuteClickCommands(context);
    }

    @Override
    public boolean canSurvive(InteractionContext context) {
        return context.aboveBlockState().isFaceSturdy(Direction.DOWN, SupportType.CENTER);
    }
}
