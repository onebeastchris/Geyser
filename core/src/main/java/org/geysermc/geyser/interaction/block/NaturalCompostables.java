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
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.item.type.Item;

import java.util.Set;

/**
 * The items a composter accepts. Vanilla hardcodes these (with their chances) in {@code ComposterBlock#bootStrap};
 * nothing about them is sent to the client, so we have to hardcode them as well. Only membership matters for the
 * client-side interaction result. Verified against vanilla by the interaction game tests.
 */
final class NaturalCompostables {
    private static final Set<Item> COMPOSTABLES = Set.of(
        Items.JUNGLE_LEAVES, Items.OAK_LEAVES, Items.SPRUCE_LEAVES, Items.DARK_OAK_LEAVES, Items.PALE_OAK_LEAVES,
        Items.ACACIA_LEAVES, Items.CHERRY_LEAVES, Items.BIRCH_LEAVES, Items.AZALEA_LEAVES, Items.MANGROVE_LEAVES,
        Items.OAK_SAPLING, Items.SPRUCE_SAPLING, Items.BIRCH_SAPLING, Items.JUNGLE_SAPLING, Items.ACACIA_SAPLING,
        Items.CHERRY_SAPLING, Items.DARK_OAK_SAPLING, Items.PALE_OAK_SAPLING, Items.MANGROVE_PROPAGULE,
        Items.BEETROOT_SEEDS, Items.DRIED_KELP, Items.SHORT_GRASS, Items.KELP, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS,
        Items.SEAGRASS, Items.SWEET_BERRIES, Items.GLOW_BERRIES, Items.WHEAT_SEEDS, Items.MOSS_CARPET,
        Items.PALE_MOSS_CARPET, Items.PALE_HANGING_MOSS, Items.PINK_PETALS, Items.WILDFLOWERS, Items.LEAF_LITTER,
        Items.SMALL_DRIPLEAF, Items.HANGING_ROOTS, Items.MANGROVE_ROOTS, Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD,
        Items.FIREFLY_BUSH, Items.BUSH, Items.CACTUS_FLOWER, Items.SHORT_DRY_GRASS, Items.TALL_DRY_GRASS,
        Items.DRIED_KELP_BLOCK, Items.TALL_GRASS, Items.FLOWERING_AZALEA_LEAVES, Items.CACTUS, Items.SUGAR_CANE,
        Items.VINE, Items.NETHER_SPROUTS, Items.WEEPING_VINES, Items.TWISTING_VINES, Items.MELON_SLICE,
        Items.GLOW_LICHEN, Items.SEA_PICKLE, Items.LILY_PAD, Items.PUMPKIN, Items.CARVED_PUMPKIN, Items.MELON,
        Items.APPLE, Items.BEETROOT, Items.CARROT, Items.COCOA_BEANS, Items.POTATO, Items.WHEAT, Items.BROWN_MUSHROOM,
        Items.RED_MUSHROOM, Items.MUSHROOM_STEM, Items.CRIMSON_FUNGUS, Items.WARPED_FUNGUS, Items.NETHER_WART,
        Items.CRIMSON_ROOTS, Items.WARPED_ROOTS, Items.SHROOMLIGHT, Items.DANDELION, Items.POPPY, Items.BLUE_ORCHID,
        Items.ALLIUM, Items.AZURE_BLUET, Items.RED_TULIP, Items.ORANGE_TULIP, Items.WHITE_TULIP, Items.PINK_TULIP,
        Items.OXEYE_DAISY, Items.CORNFLOWER, Items.LILY_OF_THE_VALLEY, Items.WITHER_ROSE, Items.OPEN_EYEBLOSSOM,
        Items.CLOSED_EYEBLOSSOM, Items.FERN, Items.SUNFLOWER, Items.LILAC, Items.ROSE_BUSH, Items.PEONY,
        Items.LARGE_FERN, Items.SPORE_BLOSSOM, Items.AZALEA, Items.MOSS_BLOCK, Items.PALE_MOSS_BLOCK,
        Items.BIG_DRIPLEAF, Items.HAY_BLOCK, Items.BROWN_MUSHROOM_BLOCK, Items.RED_MUSHROOM_BLOCK,
        Items.NETHER_WART_BLOCK, Items.WARPED_WART_BLOCK, Items.FLOWERING_AZALEA, Items.BREAD, Items.BAKED_POTATO,
        Items.COOKIE, Items.TORCHFLOWER, Items.PITCHER_PLANT, Items.CAKE, Items.PUMPKIN_PIE
    );

    static boolean isCompostable(@NonNull Item item) {
        return COMPOSTABLES.contains(item);
    }

    private NaturalCompostables() {
    }
}
