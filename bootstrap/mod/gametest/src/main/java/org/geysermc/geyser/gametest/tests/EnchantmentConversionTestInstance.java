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
package org.geysermc.geyser.gametest.tests;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kyori.adventure.key.Key;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.gametest.interaction.GameTestComponentResolver;
import org.geysermc.geyser.gametest.interaction.VanillaConversions;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.item.enchantment.Enchantment;
import org.geysermc.geyser.item.enchantment.EnchantmentComponent;
import org.geysermc.geyser.session.cache.registry.JavaRegistries;
import org.geysermc.geyser.session.cache.registry.JavaRegistry;
import org.geysermc.geyser.session.cache.registry.JavaRegistryProvider;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks that enchantments survive the conversion into Geyser's registries and item stacks, which item interactions
 * rely on (binding curse for equipping, riptide for tridents): the tracked enchantment effects must match vanilla, and
 * an enchanted stack must reference the enchantment by the same network ID in Geyser.
 */
public class EnchantmentConversionTestInstance extends GeyserTestInstance {
    public static final MapCodec<EnchantmentConversionTestInstance> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
        commonFields(instance).apply(instance, EnchantmentConversionTestInstance::new)
    );

    public EnchantmentConversionTestInstance(HolderGetter<TestEnvironmentDefinition<?>> testEnvironments, boolean required) {
        super(testEnvironments, required);
    }

    @Override
    public void run(GameTestHelper helper) {
        JavaRegistryProvider registries = createRegistryProvider(helper);
        JavaRegistry<Enchantment> geyserRegistry = registries.registry(JavaRegistries.ENCHANTMENT);
        GameTestComponentResolver componentResolver = new GameTestComponentResolver(registries);
        Registry<net.minecraft.world.item.enchantment.Enchantment> registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        List<String> mismatches = new ArrayList<>();

        for (Holder.Reference<net.minecraft.world.item.enchantment.Enchantment> holder : registry.listElements().toList()) {
            String name = holder.key().identifier().toString();
            //noinspection PatternValidation
            Enchantment geyser = geyserRegistry.byKey(Key.key(name));
            if (geyser == null) {
                mismatches.add(name + ": missing from the Geyser registry");
                continue;
            }
            check(mismatches, name, "prevent_armor_change",
                holder.value().effects().has(EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE),
                geyser.effects().contains(EnchantmentComponent.PREVENT_ARMOR_CHANGE));
            check(mismatches, name, "trident_spin_attack_strength",
                holder.value().effects().has(EnchantmentEffectComponents.TRIDENT_SPIN_ATTACK_STRENGTH),
                geyser.effects().contains(EnchantmentComponent.TRIDENT_SPIN_ATTACK_STRENGTH));

            ItemStack stack = new ItemStack(Items.IRON_HELMET);
            stack.enchant(holder, 1);
            GeyserItemStack converted = VanillaConversions.toGeyser(stack, helper.getLevel().registryAccess(), componentResolver);
            ItemEnchantments enchantments = converted.getComponent(DataComponentTypes.ENCHANTMENTS);
            int expectedId = registry.getIdOrThrow(holder.value());
            if (enchantments == null || !enchantments.getEnchantments().containsKey(expectedId)) {
                mismatches.add(name + ": enchanted stack converted to " + (enchantments == null ? "no enchantments" : enchantments.getEnchantments())
                    + ", expected ID " + expectedId + " (Geyser registry ID " + geyserRegistry.byValue(geyser) + ")");
            }
        }

        if (mismatches.isEmpty()) {
            helper.succeed();
            return;
        }
        GeyserImpl.getInstance().getLogger().error("[enchantment_conversion] " + mismatches.size() + " mismatches:\n" + String.join("\n", mismatches));
        helper.fail(mismatches.size() + " enchantment conversion mismatches (see log), e.g. " + mismatches.getFirst());
    }

    private static void check(List<String> mismatches, String name, String effect, boolean vanilla, boolean geyser) {
        if (vanilla != geyser) {
            mismatches.add(name + ": " + effect + " vanilla=" + vanilla + " geyser=" + geyser);
        }
    }

    @Override
    public MapCodec<EnchantmentConversionTestInstance> codec() {
        return MAP_CODEC;
    }

    @Override
    protected MutableComponent typeDescription() {
        return Component.literal("Geyser Enchantment Conversion Test");
    }
}
