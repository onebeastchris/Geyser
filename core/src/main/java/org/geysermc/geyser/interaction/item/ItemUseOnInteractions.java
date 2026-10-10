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

package org.geysermc.geyser.interaction.item;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.interaction.InteractionContext;
import org.geysermc.geyser.interaction.InteractionRegistry;
import org.geysermc.geyser.interaction.InteractionTables;
import org.geysermc.geyser.interaction.UseOnContext;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.inventory.item.Potion;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.BoundingBox;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.session.cache.tags.BlockTag;
import org.geysermc.geyser.util.InteractionResult;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.PotionContents;

import static org.geysermc.geyser.interaction.item.UseOnWorld.isAir;
import static org.geysermc.geyser.interaction.item.UseOnWorld.isInTag;

/**
 * Item interactions with blocks ({@code Item#useOn}), including block placement (see {@link PlacementPrediction}).
 * Called after the clicked block did not consume the click. Only the client-side branches of vanilla matter here:
 * many items only act on the server ({@code level.isClientSide()} / {@code instanceof ServerLevel} checks), and the
 * client then just reports {@link InteractionResult#SUCCESS}.
 */
public final class ItemUseOnInteractions {
    // EntityType#ARMOR_STAND dimensions
    private static final double ARMOR_STAND_WIDTH = 0.5;
    private static final double ARMOR_STAND_HEIGHT = 1.975;
    // HangingEntity: distance of the entity's centre from the block centre, and its depth
    private static final double HANGING_ENTITY_OFFSET = 0.46875;
    private static final double HANGING_ENTITY_DEPTH = 0.0625;
    private static final double ITEM_FRAME_SIZE = 0.75;

    public static void register(InteractionRegistry.Registrar registrar) {
        registrar.itemUseOn("BlockItem", PlacementPrediction::useOn);
        // Places like a block item; the bucket being emptied does not change the result
        registrar.itemUseOn("SolidBucketItem", PlacementPrediction::useOn);
        // Lily pads and frogspawn are placed through Item#use instead
        registrar.itemUseOn("PlaceOnWaterBlockItem", context -> InteractionResult.PASS);

        registrar.itemUseOn("ArmorStandItem", ItemUseOnInteractions::useArmorStand);
        registrar.itemUseOn("AxeItem", ItemUseOnInteractions::useAxe);
        registrar.itemUseOn("BoneMealItem", ItemUseOnInteractions::useBoneMeal);
        // Starts brushing if a block is targeted, but always consumes the click
        registrar.itemUseOn("BrushItem", context -> InteractionResult.CONSUME);
        registrar.itemUseOn("CompassItem", context -> result(context.clickedState().is(Blocks.LODESTONE)));
        // Cycling block states is done by the server only (the player is not a ServerPlayer on the client)
        registrar.itemUseOn("DebugStickItem", context -> InteractionResult.SUCCESS);
        registrar.itemUseOn("EndCrystalItem", ItemUseOnInteractions::useEndCrystal);
        registrar.itemUseOn("EnderEyeItem", context -> {
            BlockState state = context.clickedState();
            return result(state.is(Blocks.END_PORTAL_FRAME) && !state.getValue(Properties.EYE));
        });
        registrar.itemUseOn("FireChargeItem", ItemUseOnInteractions::useIgniter);
        registrar.itemUseOn("FlintAndSteelItem", ItemUseOnInteractions::useIgniter);
        registrar.itemUseOn("FireworkRocketItem", context -> context.player().isFallFlying() ? InteractionResult.PASS : InteractionResult.SUCCESS);
        registrar.itemUseOn("HangingEntityItem", ItemUseOnInteractions::useHangingEntity);
        registrar.itemUseOn("HoeItem", ItemUseOnInteractions::useHoe);
        registrar.itemUseOn("HoneycombItem", context -> result(InteractionTables.BlockFlag.HONEYCOMB_WAXABLE.test(context.clickedState())));
        // Leashing mobs to a fence is only done on the server; the client always passes
        registrar.itemUseOn("LeadItem", context -> InteractionResult.PASS);
        // Filled maps toggle banner markers
        registrar.itemUseOn("MapItem", context -> result(isInTag(context.player(), context.clickedState(), BlockTag.BANNERS)));
        registrar.itemUseOn("MinecartItem", ItemUseOnInteractions::useMinecart);
        registrar.itemUseOn("PotionItem", ItemUseOnInteractions::usePotion);
        registrar.itemUseOn("ShearsItem", ItemUseOnInteractions::useShears);
        registrar.itemUseOn("ShovelItem", ItemUseOnInteractions::useShovel);
        registrar.itemUseOn("SpawnEggItem", ItemUseOnInteractions::useSpawnEgg);
    }

    /**
     * {@link InteractionResult#SUCCESS} if the item did something, otherwise {@link InteractionResult#PASS}
     * (the {@code Item#useOn} default).
     */
    private static InteractionResult result(boolean success) {
        return success ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    /**
     * Mirrors {@code ArmorStandItem#useOn}: the armor stand needs room at the position a block would be placed at.
     */
    private static @NonNull InteractionResult useArmorStand(@NonNull UseOnContext context) {
        if (context.clickedFace() == Direction.DOWN) {
            return InteractionResult.FAIL;
        }
        InteractionContext player = context.player();
        Vector3i pos = PlacementPrediction.canBeReplaced(context, context.clickedState(), context.clickedPos(), true)
            ? context.clickedPos() : context.hit().adjacentPosition();
        BoundingBox box = new BoundingBox(pos.getX() + 0.5, pos.getY() + ARMOR_STAND_HEIGHT / 2, pos.getZ() + 0.5,
            ARMOR_STAND_WIDTH, ARMOR_STAND_HEIGHT, ARMOR_STAND_WIDTH);
        if (UseOnWorld.hasBlockCollision(player, box) || player.hasEntityInBox(box, false)) {
            return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Mirrors {@code AxeItem#useOn}: stripping, scraping or removing wax, unless the player intends to block with
     * the item in their off hand.
     */
    private static @NonNull InteractionResult useAxe(@NonNull UseOnContext context) {
        InteractionContext player = context.player();
        if (context.hand() == Hand.MAIN_HAND && !player.isSecondaryUseActive()
            && player.itemInHand(Hand.OFF_HAND).getComponent(DataComponentTypes.BLOCKS_ATTACKS) != null) {
            // AxeItem#playerHasBlockingItemUseIntent
            return InteractionResult.PASS;
        }
        Block block = context.clickedState().block();
        return result(InteractionTables.BlockFlag.AXE_STRIPPABLE.test(block)
            || InteractionTables.BlockFlag.AXE_SCRAPABLE.test(block)
            || InteractionTables.BlockFlag.AXE_WAX_OFF.test(block));
    }

    /**
     * Mirrors {@code BoneMealItem#useOn}: growing the clicked block, or growing water plants in front of a sturdy face.
     */
    private static @NonNull InteractionResult useBoneMeal(@NonNull UseOnContext context) {
        InteractionContext player = context.player();
        BlockState state = context.clickedState();
        if (UseOnBonemealTargets.isValidBonemealTarget(player, context.clickedPos(), state)) {
            return InteractionResult.SUCCESS;
        }
        // BoneMealItem#growWaterPlant only needs full water on the client
        if (UseOnWorld.isFaceSturdy(state, context.clickedFace())
            && UseOnWorld.isFullWater(player.blockAt(context.hit().adjacentPosition()))) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /**
     * Mirrors {@code EndCrystalItem#useOn}.
     */
    private static @NonNull InteractionResult useEndCrystal(@NonNull UseOnContext context) {
        InteractionContext player = context.player();
        BlockState state = context.clickedState();
        if (!state.is(Blocks.OBSIDIAN) && !state.is(Blocks.BEDROCK)) {
            return InteractionResult.FAIL;
        }
        Vector3i above = context.clickedPos().add(0, 1, 0);
        if (!isAir(player.blockAt(above))) {
            return InteractionResult.FAIL;
        }
        // Any entity in the two blocks above
        BoundingBox box = new BoundingBox(above.getX() + 0.5, above.getY() + 1, above.getZ() + 0.5, 1, 2, 1);
        return player.hasEntityInBox(box, false) ? InteractionResult.FAIL : InteractionResult.SUCCESS;
    }

    /**
     * Mirrors {@code FlintAndSteelItem#useOn} and {@code FireChargeItem#useOn}: lighting a campfire, candle or candle cake,
     * or placing fire next to the clicked block.
     */
    private static @NonNull InteractionResult useIgniter(@NonNull UseOnContext context) {
        InteractionContext player = context.player();
        if (canLight(player, context.clickedState())) {
            return InteractionResult.SUCCESS;
        }
        return UseOnWorld.canPlaceFireAt(player, context.hit().adjacentPosition()) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    /**
     * Mirrors {@code CampfireBlock#canLight}, {@code CandleBlock#canLight} and {@code CandleCakeBlock#canLight}.
     */
    private static boolean canLight(InteractionContext player, BlockState state) {
        Boolean lit = state.getValue(Properties.LIT);
        if (lit == null || lit) {
            return false;
        }
        if (isInTag(player, state, BlockTag.CAMPFIRES) || isInTag(player, state, BlockTag.CANDLES)) {
            Boolean waterlogged = state.getValue(Properties.WATERLOGGED);
            return waterlogged != null && !waterlogged;
        }
        return isInTag(player, state, BlockTag.CANDLE_CAKES);
    }

    /**
     * Mirrors {@code HangingEntityItem#useOn} (and {@code ItemFrameItem#mayPlace}).
     * <p>
     * Whether the entity survives decides between {@link InteractionResult#SUCCESS} and {@link InteractionResult#CONSUME},
     * so only the arm swing depends on it. It is approximated: the clicked block must have collision (for
     * {@code BlockState#isSolid}) or be a diode, and the 1x1 entity box must be free of block collisions.
     * Overlapping hanging entities and larger paintings are not considered.
     */
    private static @NonNull InteractionResult useHangingEntity(@NonNull UseOnContext context) {
        InteractionContext player = context.player();
        GeyserItemStack stack = context.stack();
        Direction face = context.clickedFace();
        Vector3i pos = context.hit().adjacentPosition();
        boolean itemFrame = stack.is(Items.ITEM_FRAME) || stack.is(Items.GLOW_ITEM_FRAME);
        // Player#mayUseItemAt is the same check InteractionSimulator#itemUseOn already did for the clicked block
        if (itemFrame ? UseOnWorld.isOutsideBuildHeight(player, pos.getY()) : face.isVertical()) {
            return InteractionResult.FAIL;
        }
        if (!itemFrame && !stack.is(Items.PAINTING)) {
            return InteractionResult.SUCCESS;
        }

        BlockState wall = context.clickedState();
        boolean supported = UseOnWorld.hasCollision(wall) || face.isHorizontal() && (wall.is(Blocks.REPEATER) || wall.is(Blocks.COMPARATOR));
        if (!supported) {
            return InteractionResult.CONSUME;
        }
        Vector3i normal = face.getUnitVector();
        double size = itemFrame ? ITEM_FRAME_SIZE : 1;
        BoundingBox box = new BoundingBox(
            pos.getX() + 0.5 - normal.getX() * HANGING_ENTITY_OFFSET,
            pos.getY() + 0.5 - normal.getY() * HANGING_ENTITY_OFFSET,
            pos.getZ() + 0.5 - normal.getZ() * HANGING_ENTITY_OFFSET,
            normal.getX() != 0 ? HANGING_ENTITY_DEPTH : size,
            normal.getY() != 0 ? HANGING_ENTITY_DEPTH : size,
            normal.getZ() != 0 ? HANGING_ENTITY_DEPTH : size);
        return UseOnWorld.hasBlockCollision(player, box) ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
    }

    /**
     * Mirrors {@code HoeItem#useOn}: rooted dirt is always tilled, the other tillables need air above
     * ({@code HoeItem#onlyIfAirAbove}).
     */
    private static @NonNull InteractionResult useHoe(@NonNull UseOnContext context) {
        BlockState state = context.clickedState();
        if (!InteractionTables.BlockFlag.HOE_TILLABLE.test(state)) {
            return InteractionResult.PASS;
        }
        if (state.is(Blocks.ROOTED_DIRT)) {
            return InteractionResult.SUCCESS;
        }
        return result(context.clickedFace() != Direction.DOWN && isAir(UseOnWorld.above(context.player(), context.clickedPos())));
    }

    /**
     * Mirrors {@code MinecartItem#useOn}. The check for other minecarts with the experimental minecart movement is not modelled.
     */
    private static @NonNull InteractionResult useMinecart(@NonNull UseOnContext context) {
        return isInTag(context.player(), context.clickedState(), BlockTag.RAILS) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    /**
     * Mirrors {@code PotionItem#useOn}: water bottles turn dirt into mud.
     */
    private static @NonNull InteractionResult usePotion(@NonNull UseOnContext context) {
        PotionContents contents = context.stack().getComponent(DataComponentTypes.POTION_CONTENTS);
        boolean water = contents != null && Potion.getByJavaId(contents.getPotionId()) == Potion.WATER;
        return result(water && context.clickedFace() != Direction.DOWN
            && isInTag(context.player(), context.clickedState(), BlockTag.CONVERTABLE_TO_MUD));
    }

    /**
     * Mirrors {@code ShearsItem#useOn}: stopping the growth of a growing plant head ({@code GrowingPlantHeadBlock}).
     */
    private static @NonNull InteractionResult useShears(@NonNull UseOnContext context) {
        BlockState state = context.clickedState();
        Block block = state.block();
        boolean plantHead = block == Blocks.KELP || block == Blocks.WEEPING_VINES || block == Blocks.TWISTING_VINES
            || block == Blocks.CAVE_VINES;
        return result(plantHead && state.getValue(Properties.AGE_25) < 25);
    }

    /**
     * Mirrors {@code ShovelItem#useOn}: flattening into a path with air above, or dousing a lit campfire.
     */
    private static @NonNull InteractionResult useShovel(@NonNull UseOnContext context) {
        if (context.clickedFace() == Direction.DOWN) {
            return InteractionResult.PASS;
        }
        BlockState state = context.clickedState();
        if (InteractionTables.BlockFlag.SHOVEL_FLATTENABLE.test(state) && isAir(UseOnWorld.above(context.player(), context.clickedPos()))) {
            return InteractionResult.SUCCESS;
        }
        // CampfireBlock
        return result((state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)) && state.getValue(Properties.LIT));
    }

    /**
     * Mirrors {@code SpawnEggItem#useOn}: everything but the entity type check happens on the server.
     * <p>
     * {@code EntityType#canSpawn(Level)} is not modelled yet (its source is missing); it is assumed to be true.
     */
    private static @NonNull InteractionResult useSpawnEgg(@NonNull UseOnContext context) {
        return context.stack().getComponent(DataComponentTypes.ENTITY_DATA) == null ? InteractionResult.FAIL : InteractionResult.SUCCESS;
    }

    private ItemUseOnInteractions() {
    }
}
