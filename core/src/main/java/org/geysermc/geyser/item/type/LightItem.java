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

package org.geysermc.geyser.item.type;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.BlockStateProperties;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponents;
import org.geysermc.paletter.vanilla.BedrockItemType;
import org.geysermc.paletter.vanilla.BedrockItemVariant;
import org.geysermc.paletter.vanilla.BedrockItems;

import java.util.List;
import java.util.Map;

public class LightItem extends BlockItem {
    /**
     * The Bedrock item of each light level; Bedrock has one item per level where Java has one with a level state
     */
    private static final List<BedrockItemType> LIGHT_BLOCKS = List.of(BedrockItems.LIGHT_BLOCK_0, BedrockItems.LIGHT_BLOCK_1,
        BedrockItems.LIGHT_BLOCK_2, BedrockItems.LIGHT_BLOCK_3, BedrockItems.LIGHT_BLOCK_4, BedrockItems.LIGHT_BLOCK_5,
        BedrockItems.LIGHT_BLOCK_6, BedrockItems.LIGHT_BLOCK_7, BedrockItems.LIGHT_BLOCK_8, BedrockItems.LIGHT_BLOCK_9,
        BedrockItems.LIGHT_BLOCK_10, BedrockItems.LIGHT_BLOCK_11, BedrockItems.LIGHT_BLOCK_12, BedrockItems.LIGHT_BLOCK_13,
        BedrockItems.LIGHT_BLOCK_14, BedrockItems.LIGHT_BLOCK_15);
    private static final int DEFAULT_LEVEL = 15;

    public LightItem(Builder builder, Block block, Block... otherBlocks) {
        super(builder, block, otherBlocks);
    }

    /**
     * Light items are always sent as the item of their level
     */
    @Override
    public @NonNull BedrockItemVariant bedrockVariant(@Nullable DataComponents components) {
        return LIGHT_BLOCKS.get(level(components)).variant();
    }

    @Override
    public boolean isBedrockVariant(@NonNull BedrockItemVariant variant) {
        return LIGHT_BLOCKS.contains(variant.type());
    }

    @Override
    public void toJava(@NonNull BedrockItemVariant variant, @NonNull DataComponents components) {
        String level = String.valueOf(LIGHT_BLOCKS.indexOf(variant.type()));
        components.put(DataComponentTypes.BLOCK_STATE, new BlockStateProperties(Map.of(Properties.LEVEL.name(), level)));
    }

    private static int level(@Nullable DataComponents components) {
        BlockStateProperties state = components == null ? null : components.get(DataComponentTypes.BLOCK_STATE);
        String level = state == null ? null : state.getProperties().get(Properties.LEVEL.name());
        if (level == null) {
            return DEFAULT_LEVEL;
        }
        try {
            int parsed = Integer.parseInt(level);
            return parsed >= 0 && parsed < LIGHT_BLOCKS.size() ? parsed : DEFAULT_LEVEL;
        } catch (NumberFormatException e) {
            return DEFAULT_LEVEL;
        }
    }
}
