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

package org.geysermc.geyser.level.block.type.cauldrons;

import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.inventory.item.Potion;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.item.type.BannerItem;
import org.geysermc.geyser.item.type.DyeableArmorItem;
import org.geysermc.geyser.item.type.ShulkerBoxItem;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.session.cache.tags.ItemTag;
import org.geysermc.geyser.util.InteractionContext;
import org.geysermc.geyser.util.InteractionResult;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.BannerPatternLayer;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentType;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.PotionContents;

import java.util.List;

public class WaterCauldronBlock extends AbstractCauldronBlock {

    public WaterCauldronBlock(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);

        interactionHandlers.put(Items.BUCKET, ctx -> {
            if (ctx.state().getValue(Properties.LEVEL_CAULDRON) == 3) {
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.TRY_EMPTY_HAND;
            }
        });

        interactionHandlers.put(Items.GLASS_BOTTLE, ctx -> InteractionResult.SUCCESS);
        interactionHandlers.put(Items.POTION, ctx -> {
            if (ctx.state().getValue(Properties.LEVEL_CAULDRON) == 3) {
                return InteractionResult.TRY_EMPTY_HAND;
            }

            GeyserItemStack stack = ctx.itemInHand();
            PotionContents contents = stack.getComponent(DataComponentType.POTION_CONTENTS);
            if (contents != null && Potion.WATER.equals(Potion.getByJavaId(stack.getJavaId()))) {
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.TRY_EMPTY_HAND;
            }
        });
    }

    @Override
    public InteractionResult interactWithItem(InteractionContext context) {
        GeyserItemStack currentItem = context.itemInHand();

        if (currentItem.asItem() instanceof DyeableArmorItem || currentItem.asItem().equals(Items.WOLF_ARMOR)) {
            if (!context.isItem(ItemTag.DYEABLE)) {
                return InteractionResult.TRY_EMPTY_HAND;
            }

            if (currentItem.getComponent(DataComponentType.DYED_COLOR) != null) {
                return InteractionResult.TRY_EMPTY_HAND;
            }

            return InteractionResult.SUCCESS;
        }

        if (currentItem.asItem() instanceof BannerItem) {
            List<BannerPatternLayer> layer = currentItem.getComponent(DataComponentType.BANNER_PATTERNS);
            if (layer == null || layer.isEmpty()) {
                return InteractionResult.TRY_EMPTY_HAND;
            } else {
                return InteractionResult.SUCCESS;
            }
        }

        if (currentItem.asItem() instanceof ShulkerBoxItem) {
            return InteractionResult.SUCCESS;
        }

        return super.interactWithItem(context);
    }
}
