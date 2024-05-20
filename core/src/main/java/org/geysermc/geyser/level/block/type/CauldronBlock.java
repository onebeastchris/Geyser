/*
 * Copyright (c) 2024 GeyserMC. http://geysermc.org
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

import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtList;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtType;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.inventory.item.Potion;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.session.cache.tags.ItemTag;
import org.geysermc.geyser.translator.level.block.entity.BedrockChunkWantsBlockEntityTag;
import org.geysermc.geyser.translator.level.block.entity.BlockEntityTranslator;
import org.geysermc.geyser.util.InteractionResult;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.BannerPatternLayer;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentType;

import java.util.List;

public class CauldronBlock extends Block implements BedrockChunkWantsBlockEntityTag {

    public CauldronBlock(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    @Override
    public NbtMap createTag(GeyserSession session, Vector3i position, BlockState blockState) {
        // As of 1.18.30: this is required to make rendering not look weird on chunk load (lava and snow cauldrons look dim)
        return BlockEntityTranslator.getConstantBedrockTag("Cauldron", position.getX(), position.getY(), position.getZ())
                .putByte("isMovable", (byte) 0)
                .putShort("PotionId", (short) -1)
                .putShort("PotionType", (short) -1)
                .putList("Items", NbtType.END, NbtList.EMPTY)
                .build();
    }

    @Override
    public InteractionResult interactWith(GeyserSession session, Vector3i blockPosition, Vector3f clickPosition, int face, boolean isMainHand, BlockState state) {
        final GeyserItemStack stack = session.getPlayerInventory().getItemInHand(isMainHand);
        final Item itemInHand = stack.asItem();
        if (itemInHand.equals(Items.WATER_BUCKET) || itemInHand.equals(Items.LAVA_BUCKET) || itemInHand.equals(Items.POWDER_SNOW_BUCKET)) {
            // One of these buckets always overrides the contents of the cauldron, even if it's empty
            return InteractionResult.SUCCESS;
        }
        var level = state.getValueNullable(Properties.LEVEL_CAULDRON);
        if (((level != null && level == 3) || state.is(Blocks.LAVA_CAULDRON)) && itemInHand.equals(Items.BUCKET)) {
            // Emptying the cauldron contents into the bucket
            return InteractionResult.SUCCESS;
        }
        if (state.is(Blocks.CAULDRON) || (state.is(Blocks.WATER_CAULDRON) && level != null && level != 3)) {
            final Potion potion = Potion.getByJavaId(itemInHand.javaId());
            if (potion == Potion.WATER) {
                // Adding a level of water to the cauldron
                return InteractionResult.SUCCESS;
            } else if (potion != null) {
                return InteractionResult.PASS;
            }
        }

        if (state.block() == Blocks.WATER_CAULDRON) {
            if (itemInHand.equals(Items.GLASS_BOTTLE)) {
                // Adding from the cauldron to the bottle
                return InteractionResult.SUCCESS;
            }
            if (itemInHand.javaIdentifier().endsWith("_shulker_box")) {
                // A dyed shulker box being undyed
                return InteractionResult.SUCCESS;
            }

            if (itemInHand.javaIdentifier().endsWith("banner")) {
                final List<BannerPatternLayer> patterns = stack.getComponent(DataComponentType.BANNER_PATTERNS);
                if (patterns == null || patterns.isEmpty()) {
                    // No pattern to clear
                    return InteractionResult.PASS;
                }
                return InteractionResult.SUCCESS;
            }

            // remove dye from e.g. leather armour
            if (stack.getComponent(DataComponentType.DYED_COLOR) != null &&
                    session.getTagCache().is(ItemTag.DYEABLE, itemInHand)) {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

}
