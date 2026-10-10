/*
 * Copyright (c) 2026 GeyserMC. http://geysermc.org
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
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import org.geysermc.geyser.item.enchantment.Enchantment;
import org.geysermc.geyser.item.enchantment.EnchantmentComponent;
import org.geysermc.geyser.session.cache.registry.JavaRegistries;
import org.geysermc.geyser.session.cache.registry.JavaRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks that the enchantment effects Geyser tracks are read from the synced enchantment registry like vanilla has
 * them. Unit effects (such as {@code prevent_armor_change}) are encoded as empty compounds, which must survive the
 * conversion.
 */
public class EnchantmentEffectsTestInstance extends GeyserTestInstance {
    public static final MapCodec<EnchantmentEffectsTestInstance> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
        commonFields(instance).apply(instance, EnchantmentEffectsTestInstance::new)
    );

    public EnchantmentEffectsTestInstance(HolderGetter<TestEnvironmentDefinition<?>> testEnvironments, boolean required) {
        super(testEnvironments, required);
    }

    @Override
    public void run(GameTestHelper helper) {
        JavaRegistry<Enchantment> geyserRegistry = createRegistryProvider(helper).registry(JavaRegistries.ENCHANTMENT);
        List<String> mismatches = new ArrayList<>();
        for (Holder.Reference<net.minecraft.world.item.enchantment.Enchantment> holder
                : helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).listElements().toList()) {
            String name = holder.key().identifier().toString();
            //noinspection PatternValidation
            Enchantment geyser = geyserRegistry.byKey(Key.key(name));
            if (geyser == null) {
                mismatches.add(name + ": missing from the Geyser registry");
                continue;
            }
            boolean vanilla = holder.value().effects().has(EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE);
            if (vanilla != geyser.effects().contains(EnchantmentComponent.PREVENT_ARMOR_CHANGE)) {
                mismatches.add(name + ": prevent_armor_change vanilla=" + vanilla + " geyser=" + !vanilla);
            }
        }

        if (mismatches.isEmpty()) {
            helper.succeed();
        } else {
            helper.fail(mismatches.size() + " enchantment effect mismatches: " + String.join(", ", mismatches));
        }
    }

    @Override
    public MapCodec<EnchantmentEffectsTestInstance> codec() {
        return MAP_CODEC;
    }

    @Override
    protected MutableComponent typeDescription() {
        return Component.literal("Geyser Enchantment Effects Test");
    }
}
