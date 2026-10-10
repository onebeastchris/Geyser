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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.geysermc.geyser.gametest.interaction.ScenarioHints;
import org.geysermc.geyser.gametest.mixin.AxeItemAccessor;
import org.geysermc.geyser.gametest.mixin.FireBlockInvoker;
import org.geysermc.geyser.gametest.mixin.HoeItemAccessor;
import org.geysermc.geyser.gametest.mixin.ShovelItemAccessor;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static org.geysermc.geyser.gametest.interaction.hints.VanillaSources.blocks;
import static org.geysermc.geyser.gametest.interaction.hints.VanillaSources.concat;
import static org.geysermc.geyser.gametest.interaction.hints.VanillaSources.items;
import static org.geysermc.geyser.gametest.interaction.hints.VanillaSources.sample;
import static org.geysermc.geyser.gametest.interaction.hints.VanillaSources.stacks;

/**
 * Scenarios for {@code org.geysermc.geyser.interaction.item.ItemUseOnInteractions}: using items on blocks
 * ({@code Item#useOn}). Block placement is covered by {@link PlacementHints}.
 * <p>
 * Targets come from vanilla's own tables (axe, hoe and shovel conversions, waxables), tags and block classes, sampled
 * where there are many. Blocks are only listed explicitly where vanilla compares against them literally (e.g. end
 * crystals on obsidian/bedrock) or as negative cases ({@code stone}: a plain full block without any interaction).
 */
public final class ItemUseOnHints {
    private static final int SAMPLE = 8;

    public static void register(ScenarioHints hints) {
        hints.itemTargets("SolidBucketItem", Blocks.STONE, Blocks.SHORT_GRASS, Blocks.SNOW);
        hints.itemTargets("PlaceOnWaterBlockItem", Blocks.STONE, Blocks.WATER);

        // Needs a free 0.5x1.975 box where a block would be placed; never on the bottom face
        hints.itemTargets("ArmorStandItem", Blocks.STONE, Blocks.SHORT_GRASS, Blocks.OAK_FENCE, Blocks.OAK_SLAB, Blocks.SNOW);

        // Strippables (AxeItem.STRIPPABLES), scrapables (WeatheringCopper#getPrevious), wax-off (HoneycombItem.WAXABLES
        // values), and a block without any conversion
        hints.itemTargets("AxeItem", concat(sample(AxeItemAccessor.getStrippables().keySet(), SAMPLE),
            sample(blocks(block -> WeatheringCopper.getPrevious(block.defaultBlockState()).isPresent()), SAMPLE),
            sample(HoneycombItem.WAXABLES.get().values(), SAMPLE), List.of(Blocks.STONE)));
        // A shield in the off hand makes the main hand axe pass (unless sneaking)
        hints.playerSetup("AxeItem", "shield_in_off_hand", player -> player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.SHIELD)));

        // Every bonemealable block, at its default state and fully grown, and blocks that aren't bonemealable
        List<Block> bonemealable = blocks(block -> block instanceof BonemealableBlock);
        hints.itemTargets("BoneMealItem", concat(bonemealable, List.of(Blocks.STONE, Blocks.DIRT)));
        for (Block block : bonemealable) {
            if (ageProperty(block.defaultBlockState()) != null) {
                hints.itemTargetSetup("BoneMealItem", "max_age", block, ItemUseOnHints::maxAge);
            }
        }

        hints.itemTargets("BrushItem", concat(blocks(block -> block instanceof BrushableBlock), List.of(Blocks.STONE)));
        // Literal checks in vanilla: lodestones, obsidian/bedrock with free space above, end portal frames
        hints.itemTargets("CompassItem", Blocks.LODESTONE, Blocks.STONE);
        hints.itemTargets("DebugStickItem", Blocks.STONE, Blocks.OAK_STAIRS);
        hints.itemTargets("EndCrystalItem", Blocks.OBSIDIAN, Blocks.BEDROCK, Blocks.STONE);
        hints.itemTargets("EnderEyeItem", Blocks.END_PORTAL_FRAME, Blocks.STONE);
        hints.itemTargetSetup("EnderEyeItem", "with_eye", Blocks.END_PORTAL_FRAME, (level, pos) ->
            level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(EndPortalFrameBlock.HAS_EYE, true)));

        // Lighting (campfires, candles, candle cakes; waterlogged ones can't be lit) or placing fire: on soul fire base
        // blocks, next to flammable blocks (FireBlock#canBurn), on a sturdy top face (stone), or nowhere (glass sides,
        // replaceable grass)
        Block[] igniterTargets = concat(sample(blocks(block -> block instanceof CampfireBlock), 2),
            sample(blocks(block -> block instanceof CandleBlock), 2), sample(blocks(block -> block instanceof CandleCakeBlock), 1),
            sample(blocks(BlockTags.SOUL_FIRE_BASE_BLOCKS), 1),
            sample(blocks(block -> ((FireBlockInvoker) Blocks.FIRE).geyser$canBurn(block.defaultBlockState())), 2),
            List.of(Blocks.STONE, Blocks.GLASS, Blocks.SHORT_GRASS));
        hints.itemTargets("FlintAndSteelItem", igniterTargets);
        hints.itemTargets("FireChargeItem", igniterTargets);

        hints.itemTargets("FireworkRocketItem", Blocks.STONE);
        // Gliding makes using a rocket on a block pass
        hints.playerSetup("FireworkRocketItem", "fall_flying", player -> {
            player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.ELYTRA));
            player.startFallFlying();
        });

        // Item frames work on every face, paintings only on horizontal faces; support (solid, or a diode) and free space
        // decide success
        hints.itemTargets("HangingEntityItem", concat(sample(blocks(block -> block instanceof DiodeBlock), 2),
            List.of(Blocks.STONE, Blocks.OAK_FENCE, Blocks.SHORT_GRASS, Blocks.GLASS, Blocks.OAK_SLAB)));

        // Tillables (HoeItem.TILLABLES), with and without air above
        hints.itemTargets("HoeItem", concat(HoeItemAccessor.getTillables().keySet(), List.of(Blocks.STONE)));
        for (Block tillable : HoeItemAccessor.getTillables().keySet()) {
            hints.itemTargetSetup("HoeItem", "stone_above", tillable, ItemUseOnHints::stoneAbove);
        }
        // Waxables and already waxed blocks (HoneycombItem.WAXABLES)
        hints.itemTargets("HoneycombItem", concat(sample(HoneycombItem.WAXABLES.get().keySet(), SAMPLE),
            sample(HoneycombItem.WAXABLES.get().values(), 2), List.of(Blocks.STONE)));
        // Leashing to fences (BlockTags.FENCES) happens on the server only
        hints.itemTargets("LeadItem", concat(sample(blocks(BlockTags.FENCES), 2), List.of(Blocks.STONE)));
        // Filled maps on banners
        hints.itemTargets("MapItem", concat(sample(blocks(block -> block instanceof AbstractBannerBlock), 2), List.of(Blocks.STONE)));
        hints.itemTargets("MinecartItem", concat(blocks(BlockTags.RAILS), List.of(Blocks.STONE)));

        // Water bottles turn mud convertibles (BlockTags.CONVERTABLE_TO_MUD) into mud (not from below)
        hints.itemTargets("PotionItem", concat(blocks(BlockTags.CONVERTABLE_TO_MUD), List.of(Blocks.STONE)));
        hints.itemVariants("PotionItem", PotionContents.createItemStack(Items.POTION, Potions.WATER),
            PotionContents.createItemStack(Items.POTION, Potions.AWKWARD));

        // Growing plant heads (sheared below max age) and bodies, and cave vines
        hints.itemTargets("ShearsItem", concat(blocks(block -> block instanceof GrowingPlantHeadBlock),
            blocks(block -> block instanceof GrowingPlantBodyBlock), blocks(block -> block instanceof CaveVines), List.of(Blocks.STONE)));
        // Flattenables (ShovelItem.FLATTENABLES) with and without air above, and campfires
        hints.itemTargets("ShovelItem", concat(ShovelItemAccessor.getFlattenables().keySet(),
            sample(blocks(block -> block instanceof CampfireBlock), 2), List.of(Blocks.STONE)));
        for (Block flattenable : ShovelItemAccessor.getFlattenables().keySet()) {
            hints.itemTargetSetup("ShovelItem", "stone_above", flattenable, ItemUseOnHints::stoneAbove);
        }
        // Spawners (their block entity is a Spawner)
        hints.itemTargets("SpawnEggItem", concat(blocks(block -> block instanceof SpawnerBlock || block instanceof TrialSpawnerBlock),
            List.of(Blocks.STONE, Blocks.SHORT_GRASS)));

        // Block states of targets that are only reachable through the target's own block class
        hints.blockItems("CampfireBlock", stacks(sample(items(ItemTags.SHOVELS), 1)));
        hints.blockItems("CampfireBlock", stacks(Items.FLINT_AND_STEEL, Items.FIRE_CHARGE));
        hints.blockSetup("CampfireBlock", "unlit_waterlogged", (level, pos) -> {
            BlockState state = level.getBlockState(pos);
            if (state.hasProperty(CampfireBlock.LIT) && state.hasProperty(CampfireBlock.WATERLOGGED)) {
                level.setBlockAndUpdate(pos, state.setValue(CampfireBlock.LIT, false).setValue(CampfireBlock.WATERLOGGED, true));
            }
        });
        for (String caveVines : new String[] {"CaveVinesBlock", "CaveVinesPlantBlock"}) {
            hints.blockItems(caveVines, stacks(Items.SHEARS, Items.BONE_MEAL));
        }
    }

    /**
     * @return the age property of the state, if it has one
     */
    private static @Nullable IntegerProperty ageProperty(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty integer && integer.getName().equals("age")) {
                return integer;
            }
        }
        return null;
    }

    /**
     * Grows the target to its maximum age.
     */
    private static void maxAge(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        IntegerProperty age = ageProperty(state);
        if (age != null) {
            level.setBlockAndUpdate(pos, state.setValue(age, Collections.max(age.getPossibleValues())));
        }
    }

    private static void stoneAbove(ServerLevel level, BlockPos pos) {
        level.setBlockAndUpdate(pos.above(), Blocks.STONE.defaultBlockState());
    }

    private ItemUseOnHints() {
    }
}
