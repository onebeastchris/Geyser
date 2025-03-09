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

package org.geysermc.geyser.item.type;

import org.cloudburstmc.protocol.bedrock.data.SoundEvent;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.level.block.BlockStateValues;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.Fluid;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.registry.BlockRegistries;
import org.geysermc.geyser.util.InteractionContext;
import org.geysermc.geyser.util.InteractionResult;

public class BucketItem extends Item {

    private final Fluid fluid;

    public BucketItem(String javaIdentifier, Fluid fluid, Builder builder) {
        super(javaIdentifier, builder);
        this.fluid = fluid;
    }

    @Override
    public InteractionResult use(InteractionContext context) {
        // We're not looking at a block; pass
        if (!context.lookingAtBlock()) {
            return InteractionResult.PASS;
        }

        if (!context.session().canUseItemAt(context.blockPosition(), context.interactFace(), context.itemInHand())) {
            return InteractionResult.FAIL;
        }

        // We have an empty bucket in our hands
        if (context.itemInHand().is(Items.BUCKET)) {
            int stateId = context.state().javaId();
            if (context.block().is(Blocks.POWDER_SNOW)) {
                if (context.shouldUpdateClient()) {
                    // TODO this is gonna be... fun
                }
                context.sendLevelSoundEventPacket(SoundEvent.BUCKET_FILL_POWDER_SNOW);
                return InteractionResult.SUCCESS;
            }

            if (context.block().is(Blocks.LAVA) && BlockStateValues.getLavaLevel(stateId) == 15) {
                if (context.shouldUpdateClient()) {
                    // TODO this is gonna be... fun
                }
                context.sendLevelSoundEventPacket(SoundEvent.BUCKET_FILL_LAVA);
                return InteractionResult.SUCCESS;
            }

            if (context.block().is(Blocks.WATER)) {
                boolean canPickUp = false;
                if (BlockRegistries.WATERLOGGED.get().get(stateId)) {
                    if (context.shouldUpdateClient()) {
                        context.updateBlock(BlockState.of(stateId).withValue(Properties.WATERLOGGED, false));
                    }
                    canPickUp = true;
                }

                if (!canPickUp && BlockStateValues.getWaterLevel(stateId) != 15) {
                    return InteractionResult.FAIL;
                }

                if (context.shouldUpdateClient()) {
                    // item change
                    // TODO this is gonna be... fun
                }

                context.sendLevelSoundEventPacket(SoundEvent.BUCKET_FILL_WATER);
                return InteractionResult.SUCCESS;
            }

        }

        // TODO implement bucket placing

        return InteractionResult.SUCCESS;
    }
}
