/*
 * Copyright (c) 2024-2025 GeyserMC. http://geysermc.org
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

import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtList;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtType;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.level.block.BlockStateValues;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.level.block.entity.BedrockChunkWantsBlockEntityTag;
import org.geysermc.geyser.translator.level.block.entity.BlockEntityTranslator;
import org.geysermc.geyser.util.InteractionContext;
import org.geysermc.geyser.util.InteractionResult;

public abstract class AbstractCauldronBlock extends Block implements BedrockChunkWantsBlockEntityTag {

    public AbstractCauldronBlock(String javaIdentifier, Builder builder) {
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
    public InteractionResult interactWithItem(InteractionContext context) {
        GeyserItemStack itemStack = context.itemInHand();

        // Default handlers; regardless of fill level
        if (itemStack.is(Items.WATER_BUCKET)) {
            return InteractionResult.SUCCESS;
        }

        if (itemStack.is(Items.LAVA_BUCKET, Items.POWDER_SNOW_BUCKET)) {
            if (isWaterAbove(context.aboveBlockState())) {
                return InteractionResult.CONSUME;
            } else {
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.TRY_EMPTY_HAND;
    }

    private boolean isWaterAbove(BlockState state) {
        return BlockStateValues.getWaterHeight(state.javaId()) != -1;
    }

}
