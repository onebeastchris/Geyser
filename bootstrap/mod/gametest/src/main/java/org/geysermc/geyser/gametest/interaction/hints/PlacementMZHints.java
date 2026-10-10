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
 * Scenarios for {@code org.geysermc.geyser.interaction.placement.PlacementRulesMZ} and {@code ItemPlacementRules}:
 * placing blocks of the vanilla block classes M to Z, and the {@code BlockItem} subclasses with placement overrides.
 */
public final class PlacementMZHints {

    public static void register(ScenarioHints hints) {
        // Blocks that need a support, choose between a standing and a wall variant, stack, need water, or whose collision
        // depends on the placement state
        hints.itemVariants("BlockItem", stacks("torch", "redstone_torch", "oak_sign", "oak_hanging_sign", "skeleton_skull",
            "tripwire_hook", "vine", "glow_lichen", "sea_pickle", "turtle_egg", "snow", "scaffolding", "oak_slab", "oak_stairs",
            "cobblestone_wall", "oak_trapdoor", "pointed_dripstone", "redstone", "sugar_cane", "small_dripleaf", "spore_blossom",
            "brown_mushroom", "nether_wart", "crimson_fungus", "pumpkin_seeds", "mangrove_propagule", "pale_moss_carpet",
            "seagrass", "piston", "observer", "shulker_box", "wither_rose", "pitcher_pod", "oak_sapling", "command_block"));
        // Slabs and snow get completed, scaffolding extends, vines/lichen/pickles/eggs grow, water holds seagrass, soul sand
        // and mycelium support nether plants and mushrooms, fences hold torches on top but not on their sides
        hints.itemTargets("BlockItem", blocks("oak_slab", "snow", "scaffolding", "vine", "glow_lichen", "sea_pickle",
            "turtle_egg", "water", "farmland", "soul_sand", "mycelium", "oak_fence", "oak_wall_hanging_sign"));
        hints.itemTargets("ScaffoldingBlockItem", blocks("scaffolding"));
        hints.playerSetup("GameMasterBlockItem", "game_master", player -> ScenarioHints.setGameMaster(player, true));
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

    private PlacementMZHints() {
    }
}
