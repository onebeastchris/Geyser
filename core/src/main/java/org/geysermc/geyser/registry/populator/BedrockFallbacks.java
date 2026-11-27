/*
 * Copyright (c) 2026 GeyserMC. http://geysermc.org
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


package org.geysermc.geyser.registry.populator;

import dev.opencollab.paletter.Fallbacks;
import dev.opencollab.paletter.block.BlockFallback;
import dev.opencollab.paletter.vanilla.BedrockBlocks;
import dev.opencollab.paletter.vanilla.BedrockItems;

/**
 * Geyser's fallback policy: what Bedrock versions show for content they lack. Paletter applies an entry only to the
 * versions that need it. It bundles no fallbacks, content without one is {@code minecraft:unknown}. Content a version only
 * has behind an experiment is present, as Geyser enables the experiment (see {@link #EXPERIMENTS}).
 * <p>
 * An entry is the content and its replacement. A block fallback to a state instead of a type sends the replacement in
 * exactly that state (instead of carrying matching state values over).
 */
final class BedrockFallbacks {
    /**
     * The experiment toggles Geyser enables: 26.40 has poplar wood, cushions and the straw bed behind
     * {@code y_2026_drop_3}, and its clients are sent the experiment and the real content, so they need no fallbacks.
     */
    static final String[] EXPERIMENTS = {"y_2026_drop_3"};

    private BedrockFallbacks() {
    }

    static Fallbacks policy() {
        return Fallbacks.builder()
            .blockAndItem(BedrockBlocks.BLACK_CONCRETE_DOUBLE_SLAB, BedrockBlocks.BLACKSTONE_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.BLACK_CONCRETE_SLAB, BedrockBlocks.BLACKSTONE_SLAB)
            .blockAndItem(BedrockBlocks.BLACK_CONCRETE_STAIRS, BedrockBlocks.BLACKSTONE_STAIRS)
            .blockAndItem(BedrockBlocks.BLACK_WOOL_DOUBLE_SLAB, BedrockBlocks.BLACKSTONE_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.BLACK_WOOL_SLAB, BedrockBlocks.BLACKSTONE_SLAB)
            .blockAndItem(BedrockBlocks.BLACK_WOOL_STAIRS, BedrockBlocks.BLACKSTONE_STAIRS)
            .blockAndItem(BedrockBlocks.BLUE_CONCRETE_DOUBLE_SLAB, BedrockBlocks.PRISMARINE_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.BLUE_CONCRETE_SLAB, BedrockBlocks.PRISMARINE_SLAB)
            .blockAndItem(BedrockBlocks.BLUE_CONCRETE_STAIRS, BedrockBlocks.PRISMARINE_STAIRS)
            .blockAndItem(BedrockBlocks.BLUE_WOOL_DOUBLE_SLAB, BedrockBlocks.PRISMARINE_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.BLUE_WOOL_SLAB, BedrockBlocks.PRISMARINE_SLAB)
            .blockAndItem(BedrockBlocks.BLUE_WOOL_STAIRS, BedrockBlocks.PRISMARINE_STAIRS)
            .blockAndItem(BedrockBlocks.BROWN_CONCRETE_DOUBLE_SLAB, BedrockBlocks.CINNABAR_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.BROWN_CONCRETE_SLAB, BedrockBlocks.CINNABAR_SLAB)
            .blockAndItem(BedrockBlocks.BROWN_CONCRETE_STAIRS, BedrockBlocks.CINNABAR_STAIRS)
            .blockAndItem(BedrockBlocks.BROWN_WOOL_DOUBLE_SLAB, BedrockBlocks.CINNABAR_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.BROWN_WOOL_SLAB, BedrockBlocks.CINNABAR_SLAB)
            .blockAndItem(BedrockBlocks.BROWN_WOOL_STAIRS, BedrockBlocks.CINNABAR_STAIRS)
            .blockAndItem(BedrockBlocks.CYAN_CONCRETE_DOUBLE_SLAB, BedrockBlocks.WARPED_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.CYAN_CONCRETE_SLAB, BedrockBlocks.WARPED_SLAB)
            .blockAndItem(BedrockBlocks.CYAN_CONCRETE_STAIRS, BedrockBlocks.WARPED_STAIRS)
            .blockAndItem(BedrockBlocks.CYAN_WOOL_DOUBLE_SLAB, BedrockBlocks.WARPED_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.CYAN_WOOL_SLAB, BedrockBlocks.WARPED_SLAB)
            .blockAndItem(BedrockBlocks.CYAN_WOOL_STAIRS, BedrockBlocks.WARPED_STAIRS)
            .blockAndItem(BedrockBlocks.GRAY_CONCRETE_DOUBLE_SLAB, BedrockBlocks.ANDESITE_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.GRAY_CONCRETE_SLAB, BedrockBlocks.ANDESITE_SLAB)
            .blockAndItem(BedrockBlocks.GRAY_CONCRETE_STAIRS, BedrockBlocks.ANDESITE_STAIRS)
            .blockAndItem(BedrockBlocks.GRAY_WOOL_DOUBLE_SLAB, BedrockBlocks.ANDESITE_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.GRAY_WOOL_SLAB, BedrockBlocks.ANDESITE_SLAB)
            .blockAndItem(BedrockBlocks.GRAY_WOOL_STAIRS, BedrockBlocks.ANDESITE_STAIRS)
            .blockAndItem(BedrockBlocks.GREEN_CONCRETE_DOUBLE_SLAB, BedrockBlocks.DARK_PRISMARINE_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.GREEN_CONCRETE_SLAB, BedrockBlocks.DARK_PRISMARINE_SLAB)
            .blockAndItem(BedrockBlocks.GREEN_CONCRETE_STAIRS, BedrockBlocks.DARK_PRISMARINE_STAIRS)
            .blockAndItem(BedrockBlocks.GREEN_WOOL_DOUBLE_SLAB, BedrockBlocks.DARK_PRISMARINE_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.GREEN_WOOL_SLAB, BedrockBlocks.DARK_PRISMARINE_SLAB)
            .blockAndItem(BedrockBlocks.GREEN_WOOL_STAIRS, BedrockBlocks.DARK_PRISMARINE_STAIRS)
            .blockAndItem(BedrockBlocks.LIGHT_BLUE_CONCRETE_DOUBLE_SLAB, BedrockBlocks.PRISMARINE_BRICK_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.LIGHT_BLUE_CONCRETE_SLAB, BedrockBlocks.PRISMARINE_BRICK_SLAB)
            .blockAndItem(BedrockBlocks.LIGHT_BLUE_CONCRETE_STAIRS, BedrockBlocks.PRISMARINE_BRICKS_STAIRS)
            .blockAndItem(BedrockBlocks.LIGHT_BLUE_WOOL_DOUBLE_SLAB, BedrockBlocks.PRISMARINE_BRICK_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.LIGHT_BLUE_WOOL_SLAB, BedrockBlocks.PRISMARINE_BRICK_SLAB)
            .blockAndItem(BedrockBlocks.LIGHT_BLUE_WOOL_STAIRS, BedrockBlocks.PRISMARINE_BRICKS_STAIRS)
            .blockAndItem(BedrockBlocks.LIGHT_GRAY_CONCRETE_DOUBLE_SLAB, BedrockBlocks.POLISHED_ANDESITE_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.LIGHT_GRAY_CONCRETE_SLAB, BedrockBlocks.POLISHED_ANDESITE_SLAB)
            .blockAndItem(BedrockBlocks.LIGHT_GRAY_CONCRETE_STAIRS, BedrockBlocks.POLISHED_ANDESITE_STAIRS)
            .blockAndItem(BedrockBlocks.LIGHT_GRAY_WOOL_DOUBLE_SLAB, BedrockBlocks.POLISHED_ANDESITE_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.LIGHT_GRAY_WOOL_SLAB, BedrockBlocks.POLISHED_ANDESITE_SLAB)
            .blockAndItem(BedrockBlocks.LIGHT_GRAY_WOOL_STAIRS, BedrockBlocks.POLISHED_ANDESITE_STAIRS)
            .blockAndItem(BedrockBlocks.LIME_CONCRETE_DOUBLE_SLAB, BedrockBlocks.PRISMARINE_BRICK_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.LIME_CONCRETE_SLAB, BedrockBlocks.PRISMARINE_BRICK_SLAB)
            .blockAndItem(BedrockBlocks.LIME_CONCRETE_STAIRS, BedrockBlocks.PRISMARINE_BRICKS_STAIRS)
            .blockAndItem(BedrockBlocks.LIME_WOOL_DOUBLE_SLAB, BedrockBlocks.PRISMARINE_BRICK_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.LIME_WOOL_SLAB, BedrockBlocks.PRISMARINE_BRICK_SLAB)
            .blockAndItem(BedrockBlocks.LIME_WOOL_STAIRS, BedrockBlocks.PRISMARINE_BRICKS_STAIRS)
            .blockAndItem(BedrockBlocks.MAGENTA_CONCRETE_DOUBLE_SLAB, BedrockBlocks.POLISHED_GRANITE_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.MAGENTA_CONCRETE_SLAB, BedrockBlocks.POLISHED_GRANITE_SLAB)
            .blockAndItem(BedrockBlocks.MAGENTA_CONCRETE_STAIRS, BedrockBlocks.POLISHED_GRANITE_STAIRS)
            .blockAndItem(BedrockBlocks.MAGENTA_WOOL_DOUBLE_SLAB, BedrockBlocks.POLISHED_GRANITE_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.MAGENTA_WOOL_SLAB, BedrockBlocks.POLISHED_GRANITE_SLAB)
            .blockAndItem(BedrockBlocks.MAGENTA_WOOL_STAIRS, BedrockBlocks.POLISHED_GRANITE_STAIRS)
            .blockAndItem(BedrockBlocks.ORANGE_CONCRETE_DOUBLE_SLAB, BedrockBlocks.RED_SANDSTONE_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.ORANGE_CONCRETE_SLAB, BedrockBlocks.RED_SANDSTONE_SLAB)
            .blockAndItem(BedrockBlocks.ORANGE_CONCRETE_STAIRS, BedrockBlocks.RED_SANDSTONE_STAIRS)
            .blockAndItem(BedrockBlocks.ORANGE_WOOL_DOUBLE_SLAB, BedrockBlocks.RED_SANDSTONE_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.ORANGE_WOOL_SLAB, BedrockBlocks.RED_SANDSTONE_SLAB)
            .blockAndItem(BedrockBlocks.ORANGE_WOOL_STAIRS, BedrockBlocks.RED_SANDSTONE_STAIRS)
            .blockAndItem(BedrockBlocks.PINK_CONCRETE_DOUBLE_SLAB, BedrockBlocks.GRANITE_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.PINK_CONCRETE_SLAB, BedrockBlocks.GRANITE_SLAB)
            .blockAndItem(BedrockBlocks.PINK_CONCRETE_STAIRS, BedrockBlocks.GRANITE_STAIRS)
            .blockAndItem(BedrockBlocks.PINK_WOOL_DOUBLE_SLAB, BedrockBlocks.GRANITE_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.PINK_WOOL_SLAB, BedrockBlocks.GRANITE_SLAB)
            .blockAndItem(BedrockBlocks.PINK_WOOL_STAIRS, BedrockBlocks.GRANITE_STAIRS)
            .blockAndItem(BedrockBlocks.PURPLE_CONCRETE_DOUBLE_SLAB, BedrockBlocks.PURPUR_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.PURPLE_CONCRETE_SLAB, BedrockBlocks.PURPUR_SLAB)
            .blockAndItem(BedrockBlocks.PURPLE_CONCRETE_STAIRS, BedrockBlocks.PURPUR_STAIRS)
            .blockAndItem(BedrockBlocks.PURPLE_WOOL_DOUBLE_SLAB, BedrockBlocks.PURPUR_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.PURPLE_WOOL_SLAB, BedrockBlocks.PURPUR_SLAB)
            .blockAndItem(BedrockBlocks.PURPLE_WOOL_STAIRS, BedrockBlocks.PURPUR_STAIRS)
            .blockAndItem(BedrockBlocks.RED_CONCRETE_DOUBLE_SLAB, BedrockBlocks.CRIMSON_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.RED_CONCRETE_SLAB, BedrockBlocks.CRIMSON_SLAB)
            .blockAndItem(BedrockBlocks.RED_CONCRETE_STAIRS, BedrockBlocks.CRIMSON_STAIRS)
            .blockAndItem(BedrockBlocks.RED_SHRUB, BedrockBlocks.BUSH)
            .blockAndItem(BedrockBlocks.RED_WOOL_DOUBLE_SLAB, BedrockBlocks.CRIMSON_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.RED_WOOL_SLAB, BedrockBlocks.CRIMSON_SLAB)
            .blockAndItem(BedrockBlocks.RED_WOOL_STAIRS, BedrockBlocks.CRIMSON_STAIRS)
            .blockAndItem(BedrockBlocks.WHITE_CONCRETE_DOUBLE_SLAB, BedrockBlocks.SMOOTH_QUARTZ_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.WHITE_CONCRETE_SLAB, BedrockBlocks.SMOOTH_QUARTZ_SLAB)
            .blockAndItem(BedrockBlocks.WHITE_CONCRETE_STAIRS, BedrockBlocks.SMOOTH_QUARTZ_STAIRS)
            .blockAndItem(BedrockBlocks.WHITE_WOOL_DOUBLE_SLAB, BedrockBlocks.SMOOTH_QUARTZ_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.WHITE_WOOL_SLAB, BedrockBlocks.SMOOTH_QUARTZ_SLAB)
            .blockAndItem(BedrockBlocks.WHITE_WOOL_STAIRS, BedrockBlocks.SMOOTH_QUARTZ_STAIRS)
            .blockAndItem(BedrockBlocks.YELLOW_CONCRETE_DOUBLE_SLAB, BedrockBlocks.BAMBOO_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.YELLOW_CONCRETE_SLAB, BedrockBlocks.BAMBOO_SLAB)
            .blockAndItem(BedrockBlocks.YELLOW_CONCRETE_STAIRS, BedrockBlocks.BAMBOO_STAIRS)
            .blockAndItem(BedrockBlocks.YELLOW_WOOL_DOUBLE_SLAB, BedrockBlocks.BAMBOO_DOUBLE_SLAB)
            .blockAndItem(BedrockBlocks.YELLOW_WOOL_SLAB, BedrockBlocks.BAMBOO_SLAB)
            .blockAndItem(BedrockBlocks.YELLOW_WOOL_STAIRS, BedrockBlocks.BAMBOO_STAIRS)
            .itemType(BedrockItems.SHELF_MUSHROOM, BedrockItems.OAK_PLANKS)
            .block(BedrockBlocks.SHELF_MUSHROOM, BlockFallback.of(BedrockBlocks.OAK_PLANKS.defaultState()))
            .build();
    }
}
