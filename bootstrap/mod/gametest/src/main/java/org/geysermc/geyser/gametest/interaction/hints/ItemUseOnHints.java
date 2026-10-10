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

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.geysermc.geyser.gametest.interaction.ScenarioHints;

import java.util.Arrays;

/**
 * Scenarios for {@code org.geysermc.geyser.interaction.item.ItemUseOnInteractions}: using items on blocks
 * ({@code Item#useOn}), including block placement.
 * <p>
 * Blocks are looked up by ID, which keeps this independent of how vanilla groups (coloured) block constants.
 */
public final class ItemUseOnHints {

    public static void register(ScenarioHints hints) {
        // Placement: replaceable blocks, blocks that can be extended with their own item, consumable block items'
        // supports (grass for sweet berries, farmland for carrots/potatoes, a sturdy bottom face for glow berries),
        // double blocks (doors, tall flowers) and beds that need a second free position
        hints.itemTargets("BlockItem", blocks("stone", "short_grass", "snow", "oak_slab", "candle", "sea_pickle", "turtle_egg",
            "pink_petals", "leaf_litter", "vine", "glow_lichen", "scaffolding", "grass_block", "farmland", "dirt", "oak_fence",
            "glass", "water", "oak_door", "white_bed"));
        // Block items with special placement rules, tested in addition to a sample of all block items
        hints.itemVariants("BlockItem", stacks("sweet_berries", "glow_berries", "carrot", "potato", "oak_slab", "snow", "candle",
            "sea_pickle", "turtle_egg", "pink_petals", "scaffolding", "oak_door", "white_bed", "command_block", "torch"));
        hints.itemTargets("SolidBucketItem", blocks("stone", "short_grass", "snow"));
        hints.itemTargets("PlaceOnWaterBlockItem", blocks("stone", "water"));

        // Needs a free 0.5x1.975 box where a block would be placed; never on the bottom face
        hints.itemTargets("ArmorStandItem", blocks("stone", "short_grass", "oak_fence", "oak_slab", "snow"));

        // Strippables, scrapables (all weathering stages), wax-off, and blocks without any conversion
        hints.itemTargets("AxeItem", blocks("oak_log", "oak_wood", "stripped_oak_log", "crimson_stem", "warped_hyphae", "bamboo_block",
            "copper_block", "exposed_copper", "weathered_cut_copper", "oxidized_cut_copper_stairs", "exposed_copper_door",
            "waxed_copper_block", "waxed_oxidized_copper_bulb", "lightning_rod", "waxed_lightning_rod", "copper_torch", "stone"));
        // A shield in the off hand makes the main hand axe pass (unless sneaking)
        hints.playerSetup("AxeItem", "shield_in_off_hand", player -> player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.SHIELD)));

        // Every kind of bonemealable block, plus blocks next to water (water plant growth) and non-bonemealable blocks
        hints.itemTargets("BoneMealItem", blocks("wheat", "carrots", "beetroots", "torchflower_crop", "pitcher_crop", "melon_stem",
            "pumpkin_stem", "oak_sapling", "cherry_sapling", "mangrove_propagule", "azalea", "flowering_azalea", "grass_block",
            "crimson_nylium", "warped_nylium", "moss_block", "pale_moss_block", "pale_moss_carpet", "pale_hanging_moss", "short_grass",
            "fern", "tall_grass", "sunflower", "pink_petals", "wildflowers", "bush", "firefly_bush", "short_dry_grass", "tall_dry_grass",
            "brown_mushroom", "crimson_fungus", "warped_fungus", "cocoa", "sweet_berry_bush", "cave_vines", "cave_vines_plant", "kelp",
            "kelp_plant", "weeping_vines", "weeping_vines_plant", "twisting_vines", "twisting_vines_plant", "big_dripleaf",
            "big_dripleaf_stem", "small_dripleaf", "sea_pickle", "seagrass", "rooted_dirt", "mangrove_leaves", "glow_lichen", "bamboo",
            "bamboo_sapling", "dirt", "stone", "sand", "dead_bush"));
        hints.itemTargetSetup("BoneMealItem", "mature", block("wheat"), with(BlockStateProperties.AGE_7, 7));
        hints.itemTargetSetup("BoneMealItem", "mature", block("sweet_berry_bush"), with(BlockStateProperties.AGE_3, 3));

        hints.itemTargets("BrushItem", blocks("suspicious_sand", "stone"));
        hints.itemTargets("CompassItem", blocks("lodestone", "stone"));
        hints.itemTargets("DebugStickItem", blocks("stone", "oak_stairs"));
        // Obsidian/bedrock with free space above
        hints.itemTargets("EndCrystalItem", blocks("obsidian", "bedrock", "stone"));
        // End portal frames with and without an eye
        hints.itemTargets("EnderEyeItem", blocks("end_portal_frame", "stone"));
        hints.itemTargetSetup("EnderEyeItem", "with_eye", block("end_portal_frame"), with(BlockStateProperties.EYE, true));

        // Lighting (unlit campfires/candles/candle cakes; waterlogged ones can't be lit) or placing fire: on a sturdy top face,
        // on soul fire base blocks, next to flammable blocks, or nowhere (against the side of a non-flammable block)
        String[] igniterTargets = {"campfire", "soul_campfire", "candle", "white_candle", "candle_cake", "stone", "glass", "oak_planks",
            "soul_sand", "oak_fence", "short_grass", "obsidian"};
        hints.itemTargets("FlintAndSteelItem", blocks(igniterTargets));
        hints.itemTargets("FireChargeItem", blocks(igniterTargets));

        hints.itemTargets("FireworkRocketItem", blocks("stone"));
        // Gliding makes using a rocket on a block pass
        hints.playerSetup("FireworkRocketItem", "fall_flying", player -> {
            player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.ELYTRA));
            player.startFallFlying();
        });

        // Item frames work on every face, paintings only on horizontal faces; support and free space decide success
        hints.itemTargets("HangingEntityItem", blocks("stone", "oak_fence", "repeater", "short_grass", "glass", "oak_slab"));

        // Tillables with and without air above (rooted dirt doesn't need air above)
        hints.itemTargets("HoeItem", blocks("grass_block", "dirt", "dirt_path", "coarse_dirt", "rooted_dirt", "podzol", "stone"));
        hints.itemTargetSetup("HoeItem", "stone_above", block("grass_block"), ItemUseOnHints::stoneAbove);
        hints.itemTargetSetup("HoeItem", "stone_above", block("rooted_dirt"), ItemUseOnHints::stoneAbove);
        hints.itemTargets("HoneycombItem", blocks("copper_block", "exposed_cut_copper_slab", "oxidized_copper_door", "copper_bars",
            "copper_chain", "copper_lantern", "lightning_rod", "waxed_copper_block", "copper_torch", "stone"));
        // Leashing happens on the server only
        hints.itemTargets("LeadItem", blocks("oak_fence", "stone"));
        // Filled maps on banners
        hints.itemTargets("MapItem", blocks("white_banner", "white_wall_banner", "stone"));
        hints.itemTargets("MinecartItem", blocks("rail", "powered_rail", "activator_rail", "stone"));

        // Water bottles turn mud convertibles into mud (not from below)
        hints.itemTargets("PotionItem", blocks("dirt", "coarse_dirt", "rooted_dirt", "stone"));
        hints.itemVariants("PotionItem", PotionContents.createItemStack(Items.POTION, Potions.WATER),
            PotionContents.createItemStack(Items.POTION, Potions.AWKWARD));

        // Growing plant heads below and at max age, and their bodies
        hints.itemTargets("ShearsItem", blocks("kelp", "kelp_plant", "weeping_vines", "twisting_vines", "cave_vines", "cave_vines_plant",
            "stone"));
        // Flattenables with and without air above, lit and unlit campfires
        hints.itemTargets("ShovelItem", blocks("grass_block", "dirt", "podzol", "coarse_dirt", "mycelium", "rooted_dirt", "dirt_path",
            "campfire", "soul_campfire", "stone"));
        hints.itemTargetSetup("ShovelItem", "stone_above", block("grass_block"), ItemUseOnHints::stoneAbove);
        hints.itemTargets("SpawnEggItem", blocks("spawner", "trial_spawner", "stone", "short_grass"));

        // Block states of targets that are only reachable through the target's own block class
        hints.blockItems("CampfireBlock", new ItemStack(Items.WOODEN_SHOVEL), new ItemStack(Items.FLINT_AND_STEEL),
            new ItemStack(Items.FIRE_CHARGE));
        hints.blockSetup("CampfireBlock", "unlit_waterlogged", (level, pos) -> {
            BlockState state = level.getBlockState(pos);
            if (state.hasProperty(BlockStateProperties.LIT) && state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, false).setValue(BlockStateProperties.WATERLOGGED, true));
            }
        });
        hints.blockSetup("CandleBlock", "waterlogged", with(BlockStateProperties.WATERLOGGED, true));
        for (String caveVines : new String[] {"CaveVinesBlock", "CaveVinesPlantBlock"}) {
            hints.blockItems(caveVines, new ItemStack(Items.SHEARS), new ItemStack(Items.BONE_MEAL));
        }
        hints.blockSetup("CaveVinesBlock", "max_age", with(BlockStateProperties.AGE_25, 25));
    }

    private static Block block(String path) {
        return BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(path));
    }

    private static ItemStack[] stacks(String... paths) {
        return Arrays.stream(paths)
            .map(path -> new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(path))))
            .toArray(ItemStack[]::new);
    }

    private static void stoneAbove(ServerLevel level, BlockPos pos) {
        level.setBlockAndUpdate(pos.above(), Blocks.STONE.defaultBlockState());
    }

    private static Block[] blocks(String... paths) {
        return Arrays.stream(paths)
            .map(path -> BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(path)))
            .toArray(Block[]::new);
    }

    /**
     * Sets a property of the placed block, if it has that property.
     */
    private static <T extends Comparable<T>> ScenarioHints.BlockSetup with(Property<T> property, T value) {
        return (level, pos) -> {
            BlockState state = level.getBlockState(pos);
            if (state.hasProperty(property)) {
                level.setBlockAndUpdate(pos, state.setValue(property, value));
            }
        };
    }

    private ItemUseOnHints() {
    }
}
