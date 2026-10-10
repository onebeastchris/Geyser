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

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import org.geysermc.geyser.gametest.interaction.ScenarioHints;

import java.util.List;

/**
 * Scenario hints for {@code Item#use} handlers ({@code org.geysermc.geyser.interaction.item.ItemUseInteractions}).
 * <p>
 * Item variants (e.g. a riptide trident) can't be hinted per item class, so they are put in the offhand: the main hand
 * item of the same class is used first, and the offhand variant is used when the main hand doesn't succeed.
 */
public final class ItemUseHints {

    public static void register(ScenarioHints hints) {
        registerDefaultUse(hints);
        registerProjectileWeapons(hints);
        registerRaytraceItems(hints);

        hints.playerSetup("FireworkRocketItem", "fall_flying", player -> {
            player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.ELYTRA));
            player.startFallFlying();
        });

        hints.playerSetup("InstrumentItem", "goat_horn_without_instrument_offhand", player -> {
            ItemStack horn = new ItemStack(Items.GOAT_HORN);
            horn.remove(DataComponents.INSTRUMENT);
            player.setItemInHand(InteractionHand.OFF_HAND, horn);
        });

        hints.playerSetup("KnowledgeBookItem", "knowledge_book_with_recipe_offhand", player -> {
            ItemStack book = new ItemStack(Items.KNOWLEDGE_BOOK);
            book.set(DataComponents.RECIPES, List.of(ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath("minecraft", "stick"))));
            player.setItemInHand(InteractionHand.OFF_HAND, book);
        });
    }

    /**
     * {@code Item#use}: consumables (hunger), equippables (occupied slot, curse of binding, creative) and the rest.
     */
    private static void registerDefaultUse(ScenarioHints hints) {
        hints.playerSetup("Item", "starving", player -> player.getFoodData().setFoodLevel(0));
        hints.playerSetup("Item", "full_hunger", player -> player.getFoodData().setFoodLevel(20));
        hints.playerSetup("Item", "full_hunger_invulnerable", player -> {
            player.getFoodData().setFoodLevel(20);
            player.getAbilities().invulnerable = true;
            player.onUpdateAbilities();
        });
        hints.playerSetup("Item", "full_hunger_food_offhand", player -> {
            player.getFoodData().setFoodLevel(20);
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.GOLDEN_APPLE));
        });

        hints.playerSetup("Item", "wearing_armor", ItemUseHints::wearArmor);
        hints.playerSetup("Item", "wearing_armor_creative", player -> {
            wearArmor(player);
            setGameMode(player, GameType.CREATIVE);
        });
        hints.playerSetup("Item", "wearing_cursed_armor", player -> {
            wearArmor(player);
            curse(player);
        });
        hints.playerSetup("Item", "wearing_cursed_armor_creative", player -> {
            wearArmor(player);
            curse(player);
            setGameMode(player, GameType.CREATIVE);
        });
        hints.playerSetup("Item", "wearing_elytra", player -> player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.ELYTRA)));
        hints.playerSetup("Item", "shield_offhand", player -> player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.SHIELD)));
        hints.playerSetup("Item", "helmet_offhand", player -> player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.DIAMOND_HELMET)));
    }

    /**
     * Bows and crossbows: arrows anywhere in the inventory, held projectiles, infinite materials. Tridents: riptide,
     * water, nearly broken.
     */
    private static void registerProjectileWeapons(ScenarioHints hints) {
        for (String weapon : List.of("BowItem", "CrossbowItem")) {
            hints.playerSetup(weapon, "arrow_in_inventory", player -> player.getInventory().setItem(20, new ItemStack(Items.ARROW)));
            hints.playerSetup(weapon, "spectral_arrow_in_hotbar", player -> player.getInventory().setItem(8, new ItemStack(Items.SPECTRAL_ARROW)));
            hints.playerSetup(weapon, "arrow_offhand", player -> player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.ARROW)));
            hints.playerSetup(weapon, "firework_offhand", player -> player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.FIREWORK_ROCKET)));
            hints.playerSetup(weapon, "firework_in_inventory", player -> player.getInventory().setItem(20, new ItemStack(Items.FIREWORK_ROCKET)));
            hints.playerSetup(weapon, "arrow_as_boots", player -> player.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.ARROW)));
            hints.playerSetup(weapon, "instabuild", player -> {
                player.getAbilities().instabuild = true;
                player.onUpdateAbilities();
            });
        }

        hints.playerSetup("TridentItem", "riptide_trident_offhand", player ->
            player.setItemInHand(InteractionHand.OFF_HAND, riptideTrident(player)));
        hints.playerSetup("TridentItem", "riptide_trident_offhand_in_water", player -> {
            player.setItemInHand(InteractionHand.OFF_HAND, riptideTrident(player));
            player.level().setBlockAndUpdate(player.blockPosition(), Blocks.WATER.defaultBlockState());
        });
        hints.playerSetup("TridentItem", "nearly_broken_trident_offhand", player -> {
            ItemStack trident = new ItemStack(Items.TRIDENT);
            trident.setDamageValue(trident.getMaxDamage() - 1);
            player.setItemInHand(InteractionHand.OFF_HAND, trident);
        });
    }

    /**
     * Items that raytrace from the player's eyes, with or without fluids.
     */
    private static void registerRaytraceItems(ScenarioHints hints) {
        hints.itemTargets("BucketItem", Blocks.AIR, Blocks.WATER, Blocks.LAVA, Blocks.STONE, Blocks.POWDER_SNOW, Blocks.BUBBLE_COLUMN,
            Blocks.KELP, Blocks.OAK_SLAB, Blocks.SHORT_GRASS, Blocks.OAK_SIGN, Blocks.TORCH, Blocks.OAK_FENCE, Blocks.CHEST);
        hints.playerSetup("BucketItem", "sneaking", player -> player.setShiftKeyDown(true));
        hints.playerSetup("BucketItem", "adventure", player -> setGameMode(player, GameType.ADVENTURE));
        hints.playerSetup("BucketItem", "empty_bucket_offhand", player -> player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.BUCKET)));

        hints.itemTargets("BottleItem", Blocks.AIR, Blocks.WATER, Blocks.LAVA, Blocks.STONE, Blocks.KELP, Blocks.BUBBLE_COLUMN);

        hints.itemTargets("BoatItem", Blocks.AIR, Blocks.WATER, Blocks.LAVA, Blocks.STONE, Blocks.SHORT_GRASS, Blocks.OAK_FENCE,
            Blocks.COBBLESTONE_WALL, Blocks.OAK_SLAB);

        hints.itemTargets("EnderEyeItem", Blocks.AIR, Blocks.STONE, Blocks.END_PORTAL_FRAME, Blocks.WATER);

        hints.itemTargets("SpawnEggItem", Blocks.AIR, Blocks.WATER, Blocks.LAVA, Blocks.STONE, Blocks.SHORT_GRASS);
        hints.playerSetup("SpawnEggItem", "spawn_egg_without_entity_offhand", player -> {
            ItemStack egg = new ItemStack(Items.PIG_SPAWN_EGG);
            egg.remove(DataComponents.ENTITY_DATA);
            player.setItemInHand(InteractionHand.OFF_HAND, egg);
        });

        hints.itemTargets("PlaceOnWaterBlockItem", Blocks.AIR, Blocks.WATER, Blocks.LAVA, Blocks.ICE, Blocks.STONE, Blocks.KELP);
    }

    private static void wearArmor(Player player) {
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS));
    }

    private static void curse(Player player) {
        Holder<Enchantment> binding = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.BINDING_CURSE);
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            player.getItemBySlot(slot).enchant(binding, 1);
        }
    }

    private static ItemStack riptideTrident(Player player) {
        ItemStack trident = new ItemStack(Items.TRIDENT);
        trident.enchant(player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.RIPTIDE), 3);
        return trident;
    }

    private static void setGameMode(Player player, GameType gameType) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.setGameMode(gameType);
        }
    }

    private ItemUseHints() {
    }
}
