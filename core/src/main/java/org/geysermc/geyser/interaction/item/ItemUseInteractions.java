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
import org.cloudburstmc.nbt.NbtList;
import org.geysermc.geyser.interaction.InteractionContext;
import org.geysermc.geyser.interaction.InteractionRegistry;
import org.geysermc.geyser.interaction.Interactions;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.item.enchantment.EnchantmentComponent;
import org.geysermc.geyser.session.cache.tags.ItemTag;
import org.geysermc.geyser.util.InteractionResult;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.Consumable;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.Equippable;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.FoodProperties;

import java.util.List;

/**
 * Item interactions without a targeted block ({@code Item#use}). Most items are driven purely by their data components,
 * which the server sends us, see {@link #defaultUse}. Items that need to look at the world are in
 * {@link UseWorldInteractions}.
 * <p>
 * Only the client side of each {@code use} matters: projectiles, maps and so on are created by the server, and the
 * client just reports {@link InteractionResult#SUCCESS}, or starts using the item and reports
 * {@link InteractionResult#CONSUME}.
 */
public final class ItemUseInteractions {
    private static final Interactions.ItemUse SUCCESS = (player, hand, stack) -> InteractionResult.SUCCESS;

    public static void register(InteractionRegistry.Registrar registrar) {
        registrar.itemUse("Item", ItemUseInteractions::defaultUse);

        // Projectiles and fishing hooks are only spawned server side
        registrar.itemUse("EggItem", SUCCESS);
        registrar.itemUse("EnderpearlItem", SUCCESS);
        registrar.itemUse("ExperienceBottleItem", SUCCESS);
        registrar.itemUse("SnowballItem", SUCCESS);
        registrar.itemUse("ThrowablePotionItem", SUCCESS);
        registrar.itemUse("SplashPotionItem", SUCCESS);
        registrar.itemUse("LingeringPotionItem", SUCCESS);
        registrar.itemUse("WindChargeItem", SUCCESS);
        registrar.itemUse("FishingRodItem", SUCCESS);
        // The filled map is created server side
        registrar.itemUse("EmptyMapItem", SUCCESS);
        // Player#openItemGui
        registrar.itemUse("WritableBookItem", SUCCESS);
        registrar.itemUse("WrittenBookItem", SUCCESS);
        // Starts using the bundle
        registrar.itemUse("BundleItem", SUCCESS);
        // ItemUtils#startUsingInstantly
        registrar.itemUse("SpyglassItem", (player, hand, stack) -> InteractionResult.CONSUME);
        // Boosting the controlled vehicle only happens server side; the client always passes
        registrar.itemUse("FoodOnAStickItem", (player, hand, stack) -> InteractionResult.PASS);
        registrar.itemUse("FireworkRocketItem", (player, hand, stack) -> player.isFallFlying() ? InteractionResult.SUCCESS : InteractionResult.PASS);
        registrar.itemUse("InstrumentItem", ItemUseInteractions::useInstrument);
        registrar.itemUse("KnowledgeBookItem", ItemUseInteractions::useKnowledgeBook);
        registrar.itemUse("BowItem", ItemUseInteractions::useBow);
        registrar.itemUse("CrossbowItem", ItemUseInteractions::useCrossbow);
        registrar.itemUse("TridentItem", ItemUseInteractions::useTrident);

        registrar.itemUse("BoatItem", UseWorldInteractions::useBoat);
        registrar.itemUse("BottleItem", UseWorldInteractions::useBottle);
        registrar.itemUse("BucketItem", UseWorldInteractions::useBucket);
        registrar.itemUse("EnderEyeItem", UseWorldInteractions::useEnderEye);
        registrar.itemUse("PlaceOnWaterBlockItem", UseWorldInteractions::usePlaceOnWater);
        registrar.itemUse("SpawnEggItem", UseWorldInteractions::useSpawnEgg);
    }

    /**
     * Mirrors {@code Item#use}, which is entirely driven by data components.
     */
    public static @NonNull InteractionResult defaultUse(@NonNull InteractionContext player, @NonNull Hand hand, @NonNull GeyserItemStack stack) {
        Consumable consumable = stack.getComponent(DataComponentTypes.CONSUMABLE);
        if (consumable != null) {
            return startConsuming(player, stack);
        }

        Equippable equippable = stack.getComponent(DataComponentTypes.EQUIPPABLE);
        if (equippable != null && equippable.swappable()) {
            return EquipmentInteractions.swapWithEquipmentSlot(player, stack, equippable);
        }

        // Both start using the item
        if (stack.getComponent(DataComponentTypes.BLOCKS_ATTACKS) != null) {
            return InteractionResult.CONSUME;
        }

        if (stack.getComponent(DataComponentTypes.KINETIC_WEAPON) != null) {
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    /**
     * Mirrors {@code Consumable#startConsuming}. Both consuming over time and instant consumption result in
     * {@link InteractionResult#CONSUME} on the client.
     */
    public static @NonNull InteractionResult startConsuming(@NonNull InteractionContext player, @NonNull GeyserItemStack stack) {
        // Consumable#canConsume
        FoodProperties food = stack.getComponent(DataComponentTypes.FOOD);
        if (food != null && !player.canEat(food.isCanAlwaysEat())) {
            return InteractionResult.FAIL;
        }
        return InteractionResult.CONSUME;
    }

    /**
     * Mirrors {@code InstrumentItem#use}: starts playing if the item has an instrument. The cooldown it adds is
     * checked before {@code use} is called.
     */
    private static @NonNull InteractionResult useInstrument(@NonNull InteractionContext player, @NonNull Hand hand, @NonNull GeyserItemStack stack) {
        return stack.getComponent(DataComponentTypes.INSTRUMENT) != null ? InteractionResult.CONSUME : InteractionResult.FAIL;
    }

    /**
     * Mirrors {@code KnowledgeBookItem#use}: fails without recipes; recipes are awarded server side.
     */
    private static @NonNull InteractionResult useKnowledgeBook(@NonNull InteractionContext player, @NonNull Hand hand, @NonNull GeyserItemStack stack) {
        NbtList<?> recipes = stack.getComponent(DataComponentTypes.RECIPES);
        return recipes == null || recipes.isEmpty() ? InteractionResult.FAIL : InteractionResult.SUCCESS;
    }

    /**
     * Mirrors {@code BowItem#use}: starts drawing if the player has an arrow or infinite materials.
     */
    private static @NonNull InteractionResult useBow(@NonNull InteractionContext player, @NonNull Hand hand, @NonNull GeyserItemStack stack) {
        return hasProjectile(player, false) ? InteractionResult.CONSUME : InteractionResult.FAIL;
    }

    /**
     * Mirrors {@code CrossbowItem#use}: shoots if charged, otherwise starts charging if the player has a projectile.
     */
    private static @NonNull InteractionResult useCrossbow(@NonNull InteractionContext player, @NonNull Hand hand, @NonNull GeyserItemStack stack) {
        List<ItemStack> chargedProjectiles = stack.getComponent(DataComponentTypes.CHARGED_PROJECTILES);
        if (chargedProjectiles != null && !chargedProjectiles.isEmpty()) {
            return InteractionResult.CONSUME;
        }
        return hasProjectile(player, true) ? InteractionResult.CONSUME : InteractionResult.FAIL;
    }

    /**
     * Mirrors {@code Player#getProjectile} returning a non-empty stack for bows and crossbows: a projectile held in
     * either hand, an arrow anywhere in the inventory, or the arrow players with infinite materials always have.
     *
     * @param allowFireworks whether held firework rockets count (crossbows)
     */
    private static boolean hasProjectile(@NonNull InteractionContext player, boolean allowFireworks) {
        if (isHeldProjectile(player, player.itemInHand(Hand.OFF_HAND), allowFireworks)
            || isHeldProjectile(player, player.itemInHand(Hand.MAIN_HAND), allowFireworks)) {
            return true;
        }
        int size = player.inventorySize();
        for (int i = 0; i < size; i++) {
            if (isArrow(player, player.inventoryItem(i))) {
                return true;
            }
        }
        return player.hasInfiniteMaterials();
    }

    private static boolean isHeldProjectile(@NonNull InteractionContext player, @NonNull GeyserItemStack stack, boolean allowFireworks) {
        return isArrow(player, stack) || (allowFireworks && !stack.isEmpty() && stack.is(Items.FIREWORK_ROCKET));
    }

    private static boolean isArrow(@NonNull InteractionContext player, @NonNull GeyserItemStack stack) {
        return !stack.isEmpty() && player.isInTag(ItemTag.ARROWS, stack.getJavaId());
    }

    /**
     * Mirrors {@code TridentItem#use}: fails if the trident is about to break, or if it has riptide while the player
     * is neither in water nor in rain.
     */
    private static @NonNull InteractionResult useTrident(@NonNull InteractionContext player, @NonNull Hand hand, @NonNull GeyserItemStack stack) {
        if (stack.nextDamageWillBreak()) {
            return InteractionResult.FAIL;
        }
        // EnchantmentHelper#getTridentSpinAttackStrength > 0, approximated by the presence of the effect
        if (UseEnchantments.has(player, stack, EnchantmentComponent.TRIDENT_SPIN_ATTACK_STRENGTH) && !player.isInWaterOrRain()) {
            return InteractionResult.FAIL;
        }
        return InteractionResult.CONSUME;
    }

    private ItemUseInteractions() {
    }
}
