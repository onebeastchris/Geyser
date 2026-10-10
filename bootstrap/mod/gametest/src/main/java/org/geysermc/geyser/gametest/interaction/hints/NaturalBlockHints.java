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
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.geysermc.geyser.gametest.interaction.ScenarioHints;
import org.geysermc.geyser.gametest.mixin.CauldronDispatcherAccessor;
import org.geysermc.geyser.gametest.mixin.FlowerPotBlockAccessor;

import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.geysermc.geyser.gametest.interaction.hints.VanillaSources.item;
import static org.geysermc.geyser.gametest.interaction.hints.VanillaSources.items;
import static org.geysermc.geyser.gametest.interaction.hints.VanillaSources.sample;
import static org.geysermc.geyser.gametest.interaction.hints.VanillaSources.stacks;

/**
 * Scenarios for {@code org.geysermc.geyser.interaction.block.NaturalBlockInteractions}: cauldrons, cakes, candles,
 * campfires, composters, flower pots, berries, respawn anchors, beehives, pumpkins, redstone ore and TNT.
 */
public final class NaturalBlockHints {

    public static void register(ScenarioHints hints) {
        // Every block state property value is tested generically (see InteractionScenarios), so setups here only
        // cover world changes and block entity data. Items checked by literal comparisons in vanilla code
        // (e.g. ItemStack#is(Items.SHEARS)) can't be derived and are listed as such.
        cauldrons(hints);

        // BeehiveBlock#useItemOn: literal shears and glass bottle checks
        hints.blockItems("BeehiveBlock", stacks(Items.GLASS_BOTTLE, Items.SHEARS, Items.STICK));

        // Candles on cakes (only on untouched cakes): CakeBlock#useItemOn checks ItemTags.CANDLES
        hints.blockItems("CakeBlock", stacks(sample(items(ItemTags.CANDLES), 3)));
        hints.blockItems("CakeBlock", stacks(Items.STICK));
        hints.playerSetup("CakeBlock", "hungry", player -> player.getFoodData().setFoodLevel(10));

        // Campfire inputs come from the server's recipes (the campfire_input recipe property set), plus a non-input
        RecipePropertySet campfireInput = hints.level().recipeAccess().propertySet(RecipePropertySet.CAMPFIRE_INPUT);
        hints.blockItems("CampfireBlock", stacks(sample(items(item -> campfireInput.test(new ItemStack(item))), 3)));
        hints.blockItems("CampfireBlock", stacks(Items.STICK));
        hints.blockSetup("CampfireBlock", "full", (level, pos) -> {
            if (level.getBlockEntity(pos) instanceof CampfireBlockEntity campfire) {
                ItemStack input = items(item -> campfireInput.test(new ItemStack(item))).stream().findFirst()
                    .map(ItemStack::new).orElse(ItemStack.EMPTY);
                for (int i = 0; i < campfire.getItems().size(); i++) {
                    campfire.getItems().set(i, input.copy());
                }
                campfire.setChanged();
            }
        });

        // Extinguishing needs an empty hand and the ability to build (generic scenarios cover hands and game modes)
        hints.blockItems("CandleBlock", stacks(Items.STICK, Items.FLINT_AND_STEEL));

        // CandleCakeBlock#useItemOn: literal flint and steel and fire charge checks pass to the item
        hints.blockItems("CandleCakeBlock", stacks(Items.FLINT_AND_STEEL, Items.FIRE_CHARGE, Items.STICK));
        hints.playerSetup("CandleCakeBlock", "hungry", player -> player.getFoodData().setFoodLevel(10));

        for (String caveVines : new String[] {"CaveVinesBlock", "CaveVinesPlantBlock"}) {
            hints.blockItems(caveVines, stacks(Items.BONE_MEAL, Items.STICK));
        }

        composter(hints);

        // Every pottable block's item (FlowerPotBlock.POTTED_BY_CONTENT), plus non-pottable items; the empty pot and
        // every potted block are tested as blocks of the class
        hints.blockItems("FlowerPotBlock", stacks(FlowerPotBlockAccessor.getPottedByContent().keySet().stream()
            .map(Block::asItem).filter(item -> item != Items.AIR).toList()));
        hints.blockItems("FlowerPotBlock", stacks(Items.DIRT, Items.STICK));

        // PumpkinBlock#useItemOn: literal shears check
        hints.blockItems("PumpkinBlock", stacks(Items.SHEARS, Items.STICK));

        // Block items pass if they could be placed next to the ore
        hints.blockItems("RedStoneOreBlock", stacks(Items.STONE, Items.SNOW, Items.SHORT_GRASS, Items.STICK));
        hints.blockSetup("RedStoneOreBlock", "surrounded_by_stone", (level, pos) -> {
            for (Direction direction : Direction.values()) {
                level.setBlockAndUpdate(pos.relative(direction), Blocks.STONE.defaultBlockState());
            }
        });
        hints.blockSetup("RedStoneOreBlock", "snow_layers_above", (level, pos) ->
            level.setBlockAndUpdate(pos.above(), Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 2)));

        // RespawnAnchorBlock#isRespawnFuel: literal glowstone check, in either hand (an off hand glowstone makes the
        // main hand pass)
        hints.blockItems("RespawnAnchorBlock", stacks(Items.GLOWSTONE, Items.STICK));

        hints.blockItems("SweetBerryBushBlock", stacks(Items.BONE_MEAL, Items.STICK));

        // TntBlock#useItemOn: literal flint and steel and fire charge checks
        hints.blockItems("TntBlock", stacks(Items.FLINT_AND_STEEL, Items.FIRE_CHARGE, Items.STICK));
    }

    private static void cauldrons(ScenarioHints hints) {
        // One item per distinct interaction of every cauldron type, from vanilla's interaction dispatchers (item tags
        // first, then items; e.g. all shulker boxes share one interaction)
        Set<Item> cauldronItems = new LinkedHashSet<>();
        for (CauldronInteraction.Dispatcher dispatcher : List.of(CauldronInteractions.EMPTY, CauldronInteractions.WATER,
                CauldronInteractions.LAVA, CauldronInteractions.POWDER_SNOW)) {
            CauldronDispatcherAccessor accessor = (CauldronDispatcherAccessor) dispatcher;
            Set<CauldronInteraction> seen = Collections.newSetFromMap(new IdentityHashMap<>());
            // Sorted, so the same items are picked in every run
            accessor.geyser$tags().entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().toString()))
                .forEach(entry -> {
                    if (seen.add(entry.getValue())) {
                        items(entry.getKey()).stream().findFirst().ifPresent(cauldronItems::add);
                    }
                });
            Map<Item, CauldronInteraction> interactions = accessor.geyser$items();
            interactions.keySet().stream().sorted(Comparator.comparingInt(BuiltInRegistries.ITEM::getId)).forEach(item -> {
                if (seen.add(interactions.get(item))) {
                    cauldronItems.add(item);
                }
            });
        }
        hints.blockItems("AbstractCauldronBlock", stacks(cauldronItems));

        // Interactions that also depend on components: dyed items, banners with patterns, potion contents
        ItemStack dyedHelmet = new ItemStack(Items.LEATHER_HELMET);
        dyedHelmet.set(DataComponents.DYED_COLOR, new DyedItemColor(0xFF0000));
        ItemStack dyedWolfArmor = new ItemStack(Items.WOLF_ARMOR);
        dyedWolfArmor.set(DataComponents.DYED_COLOR, new DyedItemColor(0x00FF00));
        // Banner patterns only need to be non-empty; a direct holder avoids needing registry access here
        ItemStack patternedBanner = new ItemStack(item("white_banner"));
        Holder<BannerPattern> stripe = Holder.direct(new BannerPattern(Identifier.withDefaultNamespace("stripe_bottom"), "block.minecraft.banner.stripe_bottom"));
        patternedBanner.set(DataComponents.BANNER_PATTERNS, new BannerPatternLayers(List.of(new BannerPatternLayers.Layer(stripe, DyeColor.RED))));
        hints.blockItems("AbstractCauldronBlock", dyedHelmet, dyedWolfArmor, patternedBanner,
            PotionContents.createItemStack(Items.POTION, Potions.WATER), PotionContents.createItemStack(Items.POTION, Potions.AWKWARD),
            PotionContents.createItemStack(Items.SPLASH_POTION, Potions.WATER), new ItemStack(Items.STICK));

        // Lava and powder snow buckets can't be emptied into a cauldron below water (including waterlogged blocks)
        hints.blockSetup("AbstractCauldronBlock", "water_above", (level, pos) -> level.setBlockAndUpdate(pos.above(), Blocks.WATER.defaultBlockState()));
        hints.blockSetup("AbstractCauldronBlock", "waterlogged_slab_above", (level, pos) -> level.setBlockAndUpdate(pos.above(),
            Blocks.STONE_SLAB.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true)));
    }

    private static void composter(ScenarioHints hints) {
        // Every compostable item (ComposterBlock.COMPOSTABLES), plus a few that aren't
        hints.blockItems("ComposterBlock", stacks(ComposterBlock.COMPOSTABLES.keySet().stream().map(ItemLike::asItem).toList()));
        hints.blockItems("ComposterBlock", stacks(Items.STICK, Items.DIRT, Items.BONE_MEAL));
    }

    private NaturalBlockHints() {
    }
}
