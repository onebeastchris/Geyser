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

import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.geyser.interaction.BlockHit;
import org.geysermc.geyser.interaction.InteractionContext;
import org.geysermc.geyser.interaction.InteractionRegistry;
import org.geysermc.geyser.interaction.UseOnContext;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.item.type.BlockItem;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.block.type.FlowerPotBlock;
import org.geysermc.geyser.registry.BlockRegistries;
import org.geysermc.geyser.session.cache.tags.BlockTag;
import org.geysermc.geyser.session.cache.tags.ItemTag;
import org.geysermc.geyser.util.InteractionResult;
import org.geysermc.geyser.util.MinecraftKey;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;

import java.util.HashSet;
import java.util.Set;

/**
 * Food, plant, fluid and fuel related blocks (cake, cauldrons, composter, berries, candles, campfire, ...).
 * <p>
 * Only the client-side branches of vanilla are relevant: anything that changes the world (eating, harvesting,
 * charging, ...) happens on the server, the client merely predicts the result.
 */
public final class NaturalBlockInteractions {
    private static final Key CAMPFIRE_INPUT = MinecraftKey.key("campfire_input");
    private static final int MAX_BEEHIVE_HONEY_LEVEL = 5;
    private static final int MAX_COMPOSTER_LEVEL = 8;
    private static final int MAX_RESPAWN_ANCHOR_CHARGES = 4;
    private static final int MAX_SWEET_BERRY_BUSH_AGE = 3;

    public static void register(InteractionRegistry.Registrar registrar) {
        registrar.blockUseItemOn("AbstractCauldronBlock", CauldronInteractions::useItemOn);
        registrar.blockUseItemOn("BeehiveBlock", NaturalBlockInteractions::beehiveUseItemOn);
        registrar.blockUseItemOn("CakeBlock", NaturalBlockInteractions::cakeUseItemOn);
        registrar.blockUseWithoutItem("CakeBlock", NaturalBlockInteractions::cakeUseWithoutItem);
        registrar.blockUseItemOn("CampfireBlock", NaturalBlockInteractions::campfireUseItemOn);
        registrar.blockUseItemOn("CandleBlock", NaturalBlockInteractions::candleUseItemOn);
        registrar.blockUseItemOn("CandleCakeBlock", NaturalBlockInteractions::candleCakeUseItemOn);
        registrar.blockUseWithoutItem("CandleCakeBlock", NaturalBlockInteractions::candleCakeUseWithoutItem);
        registrar.blockUseWithoutItem("CaveVinesBlock", NaturalBlockInteractions::caveVinesUseWithoutItem);
        registrar.blockUseWithoutItem("CaveVinesPlantBlock", NaturalBlockInteractions::caveVinesUseWithoutItem);
        registrar.blockUseItemOn("ComposterBlock", NaturalBlockInteractions::composterUseItemOn);
        registrar.blockUseWithoutItem("ComposterBlock", NaturalBlockInteractions::composterUseWithoutItem);
        registrar.blockUseItemOn("FlowerPotBlock", NaturalBlockInteractions::flowerPotUseItemOn);
        registrar.blockUseWithoutItem("FlowerPotBlock", NaturalBlockInteractions::flowerPotUseWithoutItem);
        registrar.blockUseItemOn("PumpkinBlock", NaturalBlockInteractions::pumpkinUseItemOn);
        registrar.blockUseItemOn("RedStoneOreBlock", NaturalBlockInteractions::redstoneOreUseItemOn);
        registrar.blockUseItemOn("RespawnAnchorBlock", NaturalBlockInteractions::respawnAnchorUseItemOn);
        registrar.blockUseWithoutItem("RespawnAnchorBlock", NaturalBlockInteractions::respawnAnchorUseWithoutItem);
        registrar.blockUseItemOn("SweetBerryBushBlock", NaturalBlockInteractions::sweetBerryBushUseItemOn);
        registrar.blockUseWithoutItem("SweetBerryBushBlock", NaturalBlockInteractions::sweetBerryBushUseWithoutItem);
        registrar.blockUseItemOn("TntBlock", NaturalBlockInteractions::tntUseItemOn);
    }

    /**
     * Harvesting honey with a glass bottle. Shearing a hive is only handled on the server
     * ({@code level instanceof ServerLevel}), so the client falls through to the default for shears.
     */
    private static InteractionResult beehiveUseItemOn(UseOnContext context, BlockState state) {
        if (state.getValue(Properties.LEVEL_HONEY) >= MAX_BEEHIVE_HONEY_LEVEL && context.stack().asItem() == Items.GLASS_BOTTLE) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    /**
     * Putting a candle on an untouched cake.
     */
    private static InteractionResult cakeUseItemOn(UseOnContext context, BlockState state) {
        // Vanilla additionally checks that the item places a CandleBlock, which is true for every item of the tag
        if (state.getValue(Properties.BITES) == 0
            && context.player().isInTag(ItemTag.CANDLES, context.stack().asItem().javaId())) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    /**
     * Eating a slice of cake.
     */
    private static InteractionResult cakeUseWithoutItem(UseOnContext context, BlockState state) {
        InteractionContext player = context.player();
        if (player.canEat(false)) {
            return InteractionResult.SUCCESS;
        }
        // The client consumes the click if it can't eat with an empty hand
        return player.itemInHand(Hand.MAIN_HAND).isEmpty() ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    /**
     * Placing food on a campfire. Whether there is space is only checked on the server; lit or not doesn't matter.
     */
    private static InteractionResult campfireUseItemOn(UseOnContext context, BlockState state) {
        if (context.player().isInRecipePropertySet(CAMPFIRE_INPUT, context.stack().asItem().javaId())) {
            return InteractionResult.CONSUME;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    /**
     * Extinguishing candles by hand.
     */
    private static InteractionResult candleUseItemOn(UseOnContext context, BlockState state) {
        if (context.stack().isEmpty() && context.player().mayBuild() && state.getValue(Properties.LIT)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    private static InteractionResult candleCakeUseItemOn(UseOnContext context, BlockState state) {
        Item item = context.stack().asItem();
        if (item == Items.FLINT_AND_STEEL || item == Items.FIRE_CHARGE) {
            // Lighting is up to the item
            return InteractionResult.PASS;
        }
        if (isCandleHit(context.hit()) && context.stack().isEmpty() && state.getValue(Properties.LIT)) {
            // Extinguishing the candle
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    /**
     * @return whether the click was on the candle of a candle cake, i.e. above the cake itself
     */
    private static boolean isCandleHit(BlockHit hit) {
        return hit.location().getY() - hit.position().getY() > 0.5;
    }

    /**
     * Eating a slice of a candle cake, which drops the candle. Unlike a plain cake, nothing special happens with an empty hand.
     */
    private static InteractionResult candleCakeUseWithoutItem(UseOnContext context, BlockState state) {
        return context.player().canEat(false) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    /**
     * Picking glow berries ({@code CaveVines#use}).
     */
    private static InteractionResult caveVinesUseWithoutItem(UseOnContext context, BlockState state) {
        // TODO verify against vanilla: CaveVines#use was not part of the decompiled sources; reconstructed from 1.21.x
        return state.getValue(Properties.BERRIES) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    /**
     * Adding a compostable item. The client succeeds even for a full (level 7, "ready") composter; it is the server
     * that ignores the item then.
     */
    private static InteractionResult composterUseItemOn(UseOnContext context, BlockState state) {
        if (state.getValue(Properties.LEVEL_COMPOSTER) < MAX_COMPOSTER_LEVEL && NaturalCompostables.isCompostable(context.stack().asItem())) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    /**
     * Taking out bone meal.
     */
    private static InteractionResult composterUseWithoutItem(UseOnContext context, BlockState state) {
        return state.getValue(Properties.LEVEL_COMPOSTER) == MAX_COMPOSTER_LEVEL ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    /**
     * Potting a plant. Clicking a pot that already holds a plant with a pottable item does nothing, but consumes the click.
     */
    private static InteractionResult flowerPotUseItemOn(UseOnContext context, BlockState state) {
        if (!PottablePlants.isPottable(context.stack().asItem())) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        return isEmptyPot(state) ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    /**
     * Taking the plant out of a pot.
     */
    private static InteractionResult flowerPotUseWithoutItem(UseOnContext context, BlockState state) {
        return isEmptyPot(state) ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
    }

    private static boolean isEmptyPot(BlockState state) {
        return !(state.block() instanceof FlowerPotBlock pot) || pot.flower() == Blocks.AIR;
    }

    /**
     * Carving a pumpkin with shears; the carving itself happens on the server.
     */
    private static InteractionResult pumpkinUseItemOn(UseOnContext context, BlockState state) {
        return context.stack().asItem() == Items.SHEARS ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    /**
     * Redstone ore lights up when clicked. The click is consumed unless a block could be placed against the ore.
     */
    private static InteractionResult redstoneOreUseItemOn(UseOnContext context, BlockState state) {
        if (context.stack().asItem() instanceof BlockItem && canPlaceAgainst(context)) {
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Approximates {@code new BlockPlaceContext(player, hand, stack, hit).canPlace()} for a clicked block that can't be
     * replaced itself (the ore): whether the block next to the clicked face can be replaced by the held item.
     * Covers the default {@code BlockBehaviour#canBeReplaced} and snow layers; other blocks with their own
     * replacement rules (candles, sea pickles, slabs, ...) are treated as never replaceable.
     */
    // TODO use the shared BlockPlaceContext logic of the BlockItem simulation once it exists
    private static boolean canPlaceAgainst(UseOnContext context) {
        BlockState target = context.player().blockAt(context.hit().adjacentPosition());
        Item item = context.stack().asItem();
        if (target.is(Blocks.SNOW)) {
            int layers = target.getValue(Properties.LAYERS);
            return (item == Items.SNOW && layers < 8) || layers == 1;
        }
        Block block = target.block();
        return context.player().isInTag(BlockTag.REPLACEABLE, block.javaId()) && item != block.asItem();
    }

    /**
     * Charging an anchor with glowstone. If the glowstone is in the off hand, the main hand passes so that the
     * off hand can charge it.
     */
    private static InteractionResult respawnAnchorUseItemOn(UseOnContext context, BlockState state) {
        boolean canBeCharged = state.getValue(Properties.RESPAWN_ANCHOR_CHARGES) < MAX_RESPAWN_ANCHOR_CHARGES;
        if (canBeCharged && context.stack().asItem() == Items.GLOWSTONE) {
            return InteractionResult.SUCCESS;
        }
        if (canBeCharged && context.hand() == Hand.MAIN_HAND
            && context.player().itemInHand(Hand.OFF_HAND).asItem() == Items.GLOWSTONE) {
            return InteractionResult.PASS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    /**
     * Setting the spawn point or exploding; both are decided by the server.
     */
    private static InteractionResult respawnAnchorUseWithoutItem(UseOnContext context, BlockState state) {
        return state.getValue(Properties.RESPAWN_ANCHOR_CHARGES) == 0 ? InteractionResult.PASS : InteractionResult.CONSUME;
    }

    /**
     * Bone meal on a bush that can still grow is left to the bone meal item.
     */
    private static InteractionResult sweetBerryBushUseItemOn(UseOnContext context, BlockState state) {
        if (state.getValue(Properties.AGE_3) != MAX_SWEET_BERRY_BUSH_AGE && context.stack().asItem() == Items.BONE_MEAL) {
            return InteractionResult.PASS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    /**
     * Picking sweet berries; the drops are handled by the server.
     */
    private static InteractionResult sweetBerryBushUseWithoutItem(UseOnContext context, BlockState state) {
        return state.getValue(Properties.AGE_3) > 1 ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    /**
     * Igniting TNT. Priming only happens on the server, which also checks the {@code tnt_explodes} game rule;
     * the client always succeeds.
     */
    private static InteractionResult tntUseItemOn(UseOnContext context, BlockState state) {
        Item item = context.stack().asItem();
        if (item == Items.FLINT_AND_STEEL || item == Items.FIRE_CHARGE) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    /**
     * The block items that can be put into a flower pot: those placing a block that has a potted variant
     * ({@code FlowerPotBlock#POTTED_BY_CONTENT}). Derived from the potted blocks once on first use.
     */
    private static final class PottablePlants {
        private static final Set<Item> ITEMS = collect();

        static boolean isPottable(@NonNull Item item) {
            return item instanceof BlockItem && ITEMS.contains(item);
        }

        private static Set<Item> collect() {
            Set<Item> items = new HashSet<>();
            for (Block block : BlockRegistries.JAVA_BLOCKS.get()) {
                if (block instanceof FlowerPotBlock pot && pot.flower() != Blocks.AIR) {
                    Item item = pot.flower().asItem();
                    if (item != Items.AIR) {
                        items.add(item);
                    }
                }
            }
            return Set.copyOf(items);
        }
    }

    private NaturalBlockInteractions() {
    }
}
