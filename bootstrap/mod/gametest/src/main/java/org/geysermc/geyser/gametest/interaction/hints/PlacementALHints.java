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

package org.geysermc.geyser.gametest.interaction.hints;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.geysermc.geyser.gametest.interaction.ScenarioHints;

import java.util.Arrays;

/**
 * Scenarios for {@code org.geysermc.geyser.interaction.placement.PlacementRulesAL}: placing blocks of the vanilla block
 * classes A to L.
 */
public final class PlacementALHints {

    public static void register(ScenarioHints hints) {
        // Blocks that need a support (below, above, behind or a specific block), try several attachment directions, need
        // water, or whose collision depends on the placement state
        hints.itemVariants("BlockItem", stacks("amethyst_cluster", "anvil", "bamboo", "white_banner", "bell", "dead_tube_coral",
            "rail", "stone_pressure_plate", "cactus", "cake", "white_carpet", "iron_chain", "chest", "chorus_flower", "cocoa_beans",
            "wheat_seeds", "repeater", "dirt_path", "farmland", "end_rod", "lever", "stone_button", "grindstone", "oak_fence_gate",
            "iron_bars", "kelp", "weeping_vines", "twisting_vines", "pale_hanging_moss", "hanging_roots", "hopper", "ladder",
            "lantern", "leaf_litter", "lightning_rod", "big_dripleaf", "dead_bush", "azalea", "cactus_flower", "tall_grass"));
        // Jungle logs support cocoa, sand supports cactus and dry vegetation, ladders can't be placed against the back of
        // another ladder, chests next to chests become double chests, end rods against end rods flip, leaves don't
        // connect to fences and panes but support hanging moss
        hints.itemTargets("BlockItem", blocks("jungle_log", "sand", "ladder", "chest", "end_rod", "oak_leaves"));
    }

    private static ItemStack[] stacks(String... paths) {
        return Arrays.stream(paths)
            .map(path -> new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(path))))
            .toArray(ItemStack[]::new);
    }

    private static Block[] blocks(String... paths) {
        return Arrays.stream(paths)
            .map(path -> BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(path)))
            .toArray(Block[]::new);
    }

    private PlacementALHints() {
    }
}
