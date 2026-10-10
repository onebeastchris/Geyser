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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import org.geysermc.geyser.gametest.interaction.InteractionClassData;
import org.geysermc.geyser.gametest.interaction.ScenarioHints;

import java.util.List;
import java.util.function.Predicate;

import static org.geysermc.geyser.gametest.interaction.hints.VanillaSources.blocks;
import static org.geysermc.geyser.gametest.interaction.hints.VanillaSources.concat;
import static org.geysermc.geyser.gametest.interaction.hints.VanillaSources.items;
import static org.geysermc.geyser.gametest.interaction.hints.VanillaSources.sample;

/**
 * Scenario hints for {@code Item#use} handlers ({@code org.geysermc.geyser.interaction.item.ItemUseInteractions}).
 * <p>
 * Item variants (e.g. a riptide trident) are tested through {@link ScenarioHints#itemVariants}.
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

        ItemStack hornWithoutInstrument = new ItemStack(Items.GOAT_HORN);
        hornWithoutInstrument.remove(DataComponents.INSTRUMENT);
        hints.itemVariants("InstrumentItem", hornWithoutInstrument);

        ItemStack bookWithRecipe = new ItemStack(Items.KNOWLEDGE_BOOK);
        bookWithRecipe.set(DataComponents.RECIPES, List.of(ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath("minecraft", "stick"))));
        hints.itemVariants("KnowledgeBookItem", bookWithRecipe);
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
     * Bows and crossbows: their supported projectiles ({@code ProjectileWeaponItem#getAllSupportedProjectiles} and
     * {@code #getSupportedHeldProjectiles}) anywhere in the inventory, held, or in an armor slot, and infinite
     * materials. Tridents: riptide, water, nearly broken.
     */
    private static void registerProjectileWeapons(ScenarioHints hints) {
        for (String weapon : List.of("BowItem", "CrossbowItem")) {
            List<Item> weaponItems = InteractionClassData.itemsOf(weapon);
            if (weaponItems.isEmpty() || !(weaponItems.getFirst() instanceof ProjectileWeaponItem projectileWeapon)) {
                continue;
            }
            Predicate<ItemStack> anywhere = projectileWeapon.getAllSupportedProjectiles();
            Predicate<ItemStack> held = projectileWeapon.getSupportedHeldProjectiles();
            List<Item> projectiles = sample(items(item -> anywhere.test(new ItemStack(item))), 2);
            // Projectiles that only count while held (e.g. fireworks for crossbows), which don't count in the inventory
            List<Item> heldOnly = sample(items(item -> held.test(new ItemStack(item)) && !anywhere.test(new ItemStack(item))), 1);
            for (Item projectile : projectiles) {
                String name = BuiltInRegistries.ITEM.getKey(projectile).getPath();
                hints.playerSetup(weapon, name + "_in_inventory", player -> player.getInventory().setItem(20, new ItemStack(projectile)));
                hints.playerSetup(weapon, name + "_in_hotbar", player -> player.getInventory().setItem(8, new ItemStack(projectile)));
                hints.playerSetup(weapon, name + "_offhand", player -> player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(projectile)));
                // Armor slots don't count as inventory
                hints.playerSetup(weapon, name + "_as_boots", player -> player.setItemSlot(EquipmentSlot.FEET, new ItemStack(projectile)));
            }
            for (Item projectile : heldOnly) {
                String name = BuiltInRegistries.ITEM.getKey(projectile).getPath();
                hints.playerSetup(weapon, name + "_offhand", player -> player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(projectile)));
                hints.playerSetup(weapon, name + "_in_inventory", player -> player.getInventory().setItem(20, new ItemStack(projectile)));
            }
            hints.playerSetup(weapon, "instabuild", player -> {
                player.getAbilities().instabuild = true;
                player.onUpdateAbilities();
            });
        }

        // Charged crossbows shoot instead of starting to charge
        ItemStack chargedWithArrow = new ItemStack(Items.CROSSBOW);
        chargedWithArrow.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.ofNonEmpty(List.of(new ItemStack(Items.ARROW))));
        ItemStack chargedWithFirework = new ItemStack(Items.CROSSBOW);
        chargedWithFirework.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.ofNonEmpty(List.of(new ItemStack(Items.FIREWORK_ROCKET))));
        hints.itemVariants("CrossbowItem", chargedWithArrow, chargedWithFirework);

        ItemStack nearlyBrokenTrident = new ItemStack(Items.TRIDENT);
        nearlyBrokenTrident.setDamageValue(nearlyBrokenTrident.getMaxDamage() - 1);
        hints.itemVariants("TridentItem", riptideTrident(hints), nearlyBrokenTrident);
        hints.playerSetup("TridentItem", "in_water", player ->
            player.level().setBlockAndUpdate(player.blockPosition(), Blocks.WATER.defaultBlockState()));
    }

    /**
     * Items that raytrace from the player's eyes, with or without fluids: looking at nothing, at fluid sources and
     * fluid pickups ({@code BucketPickup}), fluid containers ({@code LiquidBlockContainer}), and plain or partial blocks.
     */
    private static void registerRaytraceItems(ScenarioHints hints) {
        Block[] fluidTargets = concat(List.of(Blocks.AIR), sample(blocks(block -> block instanceof BucketPickup), 6),
            sample(blocks(block -> block instanceof LiquidBlockContainer), 3), List.of(Blocks.STONE, Blocks.OAK_SLAB, Blocks.SHORT_GRASS));

        hints.itemTargets("BucketItem", fluidTargets);
        // Bubble columns aren't pickups but are fluids; blocks with interactions of their own
        hints.itemTargets("BucketItem", Blocks.WATER, Blocks.LAVA, Blocks.BUBBLE_COLUMN, Blocks.OAK_SIGN, Blocks.TORCH,
            Blocks.OAK_FENCE, Blocks.CHEST);
        hints.playerSetup("BucketItem", "sneaking", player -> player.setShiftKeyDown(true));
        hints.playerSetup("BucketItem", "adventure", player -> setGameMode(player, GameType.ADVENTURE));
        hints.playerSetup("BucketItem", "empty_bucket_offhand", player -> player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.BUCKET)));

        hints.itemTargets("BottleItem", fluidTargets);
        hints.itemTargets("BottleItem", Blocks.WATER, Blocks.LAVA, Blocks.BUBBLE_COLUMN);

        hints.itemTargets("BoatItem", fluidTargets);
        hints.itemTargets("BoatItem", Blocks.WATER, Blocks.LAVA, Blocks.OAK_FENCE, Blocks.COBBLESTONE_WALL);

        hints.itemTargets("EnderEyeItem", Blocks.AIR, Blocks.STONE, Blocks.END_PORTAL_FRAME, Blocks.WATER);

        hints.itemTargets("SpawnEggItem", fluidTargets);
        hints.itemTargets("SpawnEggItem", Blocks.WATER, Blocks.LAVA);
        ItemStack eggWithoutEntity = new ItemStack(Items.PIG_SPAWN_EGG);
        eggWithoutEntity.remove(DataComponents.ENTITY_DATA);
        hints.itemVariants("SpawnEggItem", eggWithoutEntity);

        hints.itemTargets("PlaceOnWaterBlockItem", fluidTargets);
        hints.itemTargets("PlaceOnWaterBlockItem", Blocks.WATER, Blocks.LAVA, Blocks.ICE);
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

    private static ItemStack riptideTrident(ScenarioHints hints) {
        ItemStack trident = new ItemStack(Items.TRIDENT);
        trident.enchant(hints.registries().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.RIPTIDE), 3);
        return trident;
    }

    private static void setGameMode(Player player, GameType gameType) {
        ScenarioHints.setGameMode(player, gameType);
    }

    private ItemUseHints() {
    }
}
