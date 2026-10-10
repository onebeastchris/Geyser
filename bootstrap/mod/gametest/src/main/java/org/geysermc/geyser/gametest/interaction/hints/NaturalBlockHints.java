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

import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.geysermc.geyser.gametest.interaction.ScenarioHints;

import java.util.ArrayList;
import java.util.List;

/**
 * Scenarios for {@code org.geysermc.geyser.interaction.block.NaturalBlockInteractions}: cauldrons, cakes, candles,
 * campfires, composters, flower pots, berries, respawn anchors, beehives, pumpkins, redstone ore and TNT.
 */
public final class NaturalBlockHints {

    public static void register(ScenarioHints hints) {
        cauldrons(hints);

        hints.blockItems("BeehiveBlock", stack(Items.GLASS_BOTTLE), stack(Items.SHEARS), stack(Items.STICK));
        for (int honey : new int[] {4, 5}) {
            hints.blockSetup("BeehiveBlock", "honey_" + honey, with(BeehiveBlock.HONEY_LEVEL, honey));
        }

        // Candles on cakes (only on untouched cakes), eating with full/empty hands and hunger
        hints.blockItems("CakeBlock", stack(Items.CANDLE), stack(item("red_candle")), stack(Items.STICK), stack(Items.CAKE));
        for (int bites : new int[] {1, 6}) {
            hints.blockSetup("CakeBlock", "bites_" + bites, with(CakeBlock.BITES, bites));
        }
        hints.playerSetup("CakeBlock", "hungry", player -> player.getFoodData().setFoodLevel(10));

        // Raw food is in the campfire_input recipe property set; lit or not and free slots don't matter client-side
        hints.blockItems("CampfireBlock", stack(Items.PORKCHOP), stack(Items.BEEF), stack(Items.COOKED_BEEF), stack(Items.STICK));
        hints.blockSetup("CampfireBlock", "unlit", with(BlockStateProperties.LIT, false));
        hints.blockSetup("CampfireBlock", "full", (level, pos) -> {
            if (level.getBlockEntity(pos) instanceof CampfireBlockEntity campfire) {
                for (int i = 0; i < campfire.getItems().size(); i++) {
                    campfire.getItems().set(i, new ItemStack(Items.PORKCHOP));
                }
                campfire.setChanged();
            }
        });

        // Extinguishing needs an empty hand and the ability to build (generic scenarios cover hands and game modes)
        hints.blockItems("CandleBlock", stack(Items.STICK), stack(Items.CANDLE), stack(Items.FLINT_AND_STEEL));
        hints.blockSetup("CandleBlock", "lit", with(BlockStateProperties.LIT, true));

        // Flint and steel/fire charges pass to the item; extinguishing depends on the hit location being above y + 0.5
        hints.blockItems("CandleCakeBlock", stack(Items.FLINT_AND_STEEL), stack(Items.FIRE_CHARGE), stack(Items.STICK));
        hints.blockSetup("CandleCakeBlock", "lit", with(BlockStateProperties.LIT, true));
        hints.playerSetup("CandleCakeBlock", "hungry", player -> player.getFoodData().setFoodLevel(10));

        for (String caveVines : new String[] {"CaveVinesBlock", "CaveVinesPlantBlock"}) {
            hints.blockItems(caveVines, stack(Items.GLOW_BERRIES), stack(Items.BONE_MEAL), stack(Items.STICK));
            hints.blockSetup(caveVines, "berries", with(CaveVines.BERRIES, true));
        }

        composter(hints);

        // Pottable and non-pottable (block) items, on empty and filled pots
        hints.blockItems("FlowerPotBlock", stack(Items.POPPY), stack(Items.OAK_SAPLING), stack(Items.BAMBOO), stack(Items.CACTUS),
            stack(Items.DIRT), stack(Items.STICK));
        hints.blockSetup("FlowerPotBlock", "empty", (level, pos) -> level.setBlockAndUpdate(pos, Blocks.FLOWER_POT.defaultBlockState()));
        hints.blockSetup("FlowerPotBlock", "potted_poppy", (level, pos) -> level.setBlockAndUpdate(pos, Blocks.POTTED_POPPY.defaultBlockState()));

        hints.blockItems("PumpkinBlock", stack(Items.SHEARS), stack(Items.STICK));

        // Block items pass if they could be placed next to the ore
        hints.blockItems("RedStoneOreBlock", stack(Items.STONE), stack(Items.SNOW), stack(Items.SHORT_GRASS), stack(Items.STICK));
        hints.blockSetup("RedStoneOreBlock", "surrounded_by_stone", (level, pos) -> {
            for (Direction direction : Direction.values()) {
                level.setBlockAndUpdate(pos.relative(direction), Blocks.STONE.defaultBlockState());
            }
        });
        hints.blockSetup("RedStoneOreBlock", "snow_layers_above", (level, pos) ->
            level.setBlockAndUpdate(pos.above(), Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 2)));

        // Glowstone in either hand (an off hand glowstone makes the main hand pass)
        hints.blockItems("RespawnAnchorBlock", stack(Items.GLOWSTONE), stack(Items.STICK));
        for (int charges : new int[] {1, 4}) {
            hints.blockSetup("RespawnAnchorBlock", "charges_" + charges, with(RespawnAnchorBlock.CHARGE, charges));
        }

        hints.blockItems("SweetBerryBushBlock", stack(Items.BONE_MEAL), stack(Items.SWEET_BERRIES), stack(Items.STICK));
        for (int age = 1; age <= 3; age++) {
            hints.blockSetup("SweetBerryBushBlock", "age_" + age, with(SweetBerryBushBlock.AGE, age));
        }

        hints.blockItems("TntBlock", stack(Items.FLINT_AND_STEEL), stack(Items.FIRE_CHARGE), stack(Items.STICK));
    }

    private static void cauldrons(ScenarioHints hints) {
        ItemStack dyedHelmet = new ItemStack(Items.LEATHER_HELMET);
        dyedHelmet.set(DataComponents.DYED_COLOR, new DyedItemColor(0xFF0000));
        ItemStack dyedWolfArmor = new ItemStack(Items.WOLF_ARMOR);
        dyedWolfArmor.set(DataComponents.DYED_COLOR, new DyedItemColor(0x00FF00));

        // Banner patterns only need to be non-empty; a direct holder avoids needing registry access here
        ItemStack patternedBanner = new ItemStack(item("white_banner"));
        Holder<BannerPattern> stripe = Holder.direct(new BannerPattern(Identifier.withDefaultNamespace("stripe_bottom"), "block.minecraft.banner.stripe_bottom"));
        patternedBanner.set(DataComponents.BANNER_PATTERNS, new BannerPatternLayers(List.of(new BannerPatternLayers.Layer(stripe, DyeColor.RED))));

        hints.blockItems("AbstractCauldronBlock",
            stack(Items.BUCKET), stack(Items.WATER_BUCKET), stack(Items.LAVA_BUCKET), stack(Items.POWDER_SNOW_BUCKET),
            stack(Items.GLASS_BOTTLE),
            PotionContents.createItemStack(Items.POTION, Potions.WATER), PotionContents.createItemStack(Items.POTION, Potions.AWKWARD),
            PotionContents.createItemStack(Items.SPLASH_POTION, Potions.WATER),
            dyedHelmet, stack(Items.LEATHER_HELMET), dyedWolfArmor, stack(Items.LEATHER_HORSE_ARMOR),
            patternedBanner, stack(item("white_banner")),
            stack(item("red_shulker_box")), stack(Items.SHULKER_BOX),
            stack(Items.STICK));
        for (int fillLevel = 2; fillLevel <= 3; fillLevel++) {
            // Water and powder snow cauldrons
            hints.blockSetup("AbstractCauldronBlock", "level_" + fillLevel, with(LayeredCauldronBlock.LEVEL, fillLevel));
        }
        // Lava and powder snow buckets can't be emptied into a cauldron below water (including waterlogged blocks)
        hints.blockSetup("AbstractCauldronBlock", "water_above", (level, pos) -> level.setBlockAndUpdate(pos.above(), Blocks.WATER.defaultBlockState()));
        hints.blockSetup("AbstractCauldronBlock", "waterlogged_slab_above", (level, pos) -> level.setBlockAndUpdate(pos.above(),
            Blocks.STONE_SLAB.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true)));
    }

    private static void composter(ScenarioHints hints) {
        // Every compostable item, as Geyser hardcodes the list, plus a few that aren't
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemLike compostable : ComposterBlock.COMPOSTABLES.keySet()) {
            stacks.add(new ItemStack(compostable));
        }
        stacks.add(stack(Items.STICK));
        stacks.add(stack(Items.DIRT));
        stacks.add(stack(Items.BONE_MEAL));
        hints.blockItems("ComposterBlock", stacks.toArray(ItemStack[]::new));
        for (int fillLevel : new int[] {3, 7, 8}) {
            hints.blockSetup("ComposterBlock", "level_" + fillLevel, with(BlockStateProperties.LEVEL_COMPOSTER, fillLevel));
        }
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

    private static ItemStack stack(ItemLike item) {
        return new ItemStack(item);
    }

    /**
     * Looks up coloured variants by ID, which keeps this independent of how vanilla groups coloured items.
     */
    private static Item item(String path) {
        return BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(path));
    }

    private NaturalBlockHints() {
    }
}
