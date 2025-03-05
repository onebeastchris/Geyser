/*
 * Copyright (c) 2019-2022 GeyserMC. http://geysermc.org
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

import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.util.BlockPlaceContext;
import org.geysermc.geyser.util.InteractionContext;
import org.geysermc.geyser.util.InteractionResult;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;

public class BlockItem extends Item {
    // If item is instanceof ItemNameBlockItem
    private final boolean treatLikeBlock;

    public BlockItem(Builder builder, Block block, Block... otherBlocks) {
        super(block.javaIdentifier().value(), builder);

        // Ensure this item can be looked up by its block(s)
        registerBlock(block, this);
        for (Block otherBlock : otherBlocks) {
            registerBlock(otherBlock, this);
        }
        treatLikeBlock = true;
    }

    // Use this constructor if the item name is not the same as its primary block
    public BlockItem(String javaIdentifier, Builder builder, Block block, Block... otherBlocks) {
        super(javaIdentifier, builder);

        registerBlock(block, this);
        for (Block otherBlock : otherBlocks) {
            registerBlock(otherBlock, this);
        }
        treatLikeBlock = false;
    }

    @Override
    public String translationKey() {
        if (!treatLikeBlock) {
            return super.translationKey();
        }
        return "block." + this.javaIdentifier.namespace() + "." + this.javaIdentifier.value();
    }

    @Override
    public InteractionResult useOn(InteractionContext context) {
        InteractionResult result = tryPlace(BlockPlaceContext.of(context));
        if (!result.consumesAction() && context.itemInHand().getComponent(DataComponentTypes.CONSUMABLE) != null) {
            return super.useOn(context); // TODO check partial result usage??
        }

        return super.useOn(context);
    }

    private InteractionResult tryPlace(BlockPlaceContext context) {
        if (!context.canPlace()) {
            return InteractionResult.FAIL;
        }

        // TODO

        if (context.itemInHand().is(Items.SCAFFOLDING)) {
            // TODO: checks here whether we can place scaffolding (max length of 7) :))
            return InteractionResult.FAIL; // if too far away
        }

        // TODO final placement checks.. not going to bother with these unless we really have to

        return InteractionResult.SUCCESS;
    }
}
