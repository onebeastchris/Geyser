/*
 * Copyright (c) 2019-2026 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.interaction.block;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.geyser.interaction.UseOnContext;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.inventory.item.Potion;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.item.type.BannerItem;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.geyser.item.type.ShulkerBoxItem;
import org.geysermc.geyser.level.block.BlockStateValues;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.Fluid;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.session.cache.tags.ItemTag;
import org.geysermc.geyser.util.InteractionResult;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.BannerPatternLayer;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.PotionContents;

import java.util.List;

/**
 * Client-side results of vanilla's cauldron interaction dispatchers ({@code CauldronInteractions}), one per cauldron
 * type, as used by {@code AbstractCauldronBlock#useItemOn}. Every interaction that changes the cauldron or the item
 * only does so on the server; the client just predicts {@link InteractionResult#SUCCESS}. Items without an interaction
 * result in {@link InteractionResult#TRY_WITH_EMPTY_HAND} ({@code CauldronInteraction.DEFAULT}).
 */
final class CauldronInteractions {
    private static final int MAX_FILL_LEVEL = 3;

    static @NonNull InteractionResult useItemOn(@NonNull UseOnContext context, @NonNull BlockState state) {
        Block block = state.block();
        if (block == Blocks.CAULDRON) {
            return empty(context);
        } else if (block == Blocks.WATER_CAULDRON) {
            return water(context, state);
        } else if (block == Blocks.LAVA_CAULDRON) {
            return lava(context);
        } else if (block == Blocks.POWDER_SNOW_CAULDRON) {
            return powderSnow(context, state);
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    private static InteractionResult empty(UseOnContext context) {
        GeyserItemStack stack = context.stack();
        if (stack.asItem() == Items.POTION) {
            // A water bottle fills an empty cauldron
            return isWaterPotion(stack) ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        return emptyBucket(context);
    }

    private static InteractionResult water(UseOnContext context, BlockState state) {
        GeyserItemStack stack = context.stack();
        Item item = stack.asItem();

        // The dispatcher checks its tags before its items
        if (context.player().isInTag(ItemTag.CAULDRON_CAN_REMOVE_DYE, item.javaId())) {
            // Washing off dye
            return stack.getComponent(DataComponentTypes.DYED_COLOR) != null ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (item instanceof BannerItem) {
            // Washing off the top banner pattern layer
            List<BannerPatternLayer> layers = stack.getComponent(DataComponentTypes.BANNER_PATTERNS);
            return layers != null && !layers.isEmpty() ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
        } else if (item instanceof ShulkerBoxItem && item != Items.SHULKER_BOX) {
            // Washing off the colour of a dyed shulker box
            return InteractionResult.SUCCESS;
        } else if (item == Items.BUCKET) {
            return isFull(state) ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
        } else if (item == Items.GLASS_BOTTLE) {
            // Every water cauldron holds at least one bottle
            return InteractionResult.SUCCESS;
        } else if (item == Items.POTION) {
            return !isFull(state) && isWaterPotion(stack) ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        return emptyBucket(context);
    }

    private static InteractionResult lava(UseOnContext context) {
        if (context.stack().asItem() == Items.BUCKET) {
            return InteractionResult.SUCCESS;
        }
        return emptyBucket(context);
    }

    private static InteractionResult powderSnow(UseOnContext context, BlockState state) {
        if (context.stack().asItem() == Items.BUCKET) {
            return isFull(state) ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        return emptyBucket(context);
    }

    /**
     * The interactions every cauldron type shares: emptying a water, lava or powder snow bucket into it, regardless of
     * its current contents. Lava and powder snow can't be poured into a cauldron below water.
     */
    private static InteractionResult emptyBucket(UseOnContext context) {
        Item item = context.stack().asItem();
        if (item == Items.WATER_BUCKET) {
            return InteractionResult.SUCCESS;
        } else if (item == Items.LAVA_BUCKET || item == Items.POWDER_SNOW_BUCKET) {
            return isUnderWater(context) ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    /**
     * @return whether the fluid above the cauldron is water, including waterlogged blocks
     */
    private static boolean isUnderWater(UseOnContext context) {
        BlockState above = context.player().blockAt(context.clickedPos().up());
        return BlockStateValues.getFluid(above.javaId()) == Fluid.WATER;
    }

    private static boolean isFull(BlockState state) {
        return state.getValue(Properties.LEVEL_CAULDRON, 0) == MAX_FILL_LEVEL;
    }

    private static boolean isWaterPotion(GeyserItemStack stack) {
        PotionContents contents = stack.getComponent(DataComponentTypes.POTION_CONTENTS);
        return contents != null && contents.getPotionId() == Potion.WATER.ordinal();
    }

    private CauldronInteractions() {
    }
}
