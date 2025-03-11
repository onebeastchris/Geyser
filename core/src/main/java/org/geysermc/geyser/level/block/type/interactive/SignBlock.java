/*
 * Copyright (c) 2019-2025 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.level.block.type.interactive;

import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.item.type.DyeItem;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.util.InteractionContext;
import org.geysermc.geyser.util.InteractionResult;


public class SignBlock extends Block {
    public SignBlock(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    @Override
    public InteractionResult interactWithItem(InteractionContext context) {
        // TODO check for block entity
        boolean isWaxed = context.session().getWaxedSignCache().contains(context.blockPosition());
        Item item = context.itemInHand().asItem();
        boolean itemInteractsWithSign = item.equals(Items.INK_SAC) || item.equals(Items.GLOW_INK_SAC) ||
                item.equals(Items.HONEYCOMB) || item instanceof DyeItem;
        return !(itemInteractsWithSign && context.session().canBuildForGamemode()) && !isWaxed ?
                InteractionResult.CONSUME : InteractionResult.SUCCESS;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean canExecuteClickCommands(InteractionContext context) {
        boolean isWaxed = context.session().getWaxedSignCache().contains(context.blockPosition());
        boolean isFrontFacing = context.state().getValue(Properties.HORIZONTAL_FACING).reversed() == context.interactFace(); // TODO test
        boolean hasClickCommand = isFrontFacing ? context.session().getFrontRunCommandSignCache().contains(context.blockPosition())
                : context.session().getBackRunCommandSignCache().contains(context.blockPosition());

        return isWaxed && hasClickCommand;
    }
}
