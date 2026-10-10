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

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import org.geysermc.geyser.gametest.interaction.InteractionScenarios;

import java.util.List;
import java.util.Optional;

/**
 * Compares the Java client's and Geyser's predicted results of using items: either every item whose {@code use} or
 * {@code useOn} is implemented by the given vanilla class, or an explicit list of items (for items driven by data
 * components only, such as food or armor).
 */
public class ItemInteractionTestInstance extends GeyserTestInstance {
    public static final MapCodec<ItemInteractionTestInstance> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
        commonFields(instance)
            .and(Codec.STRING.optionalFieldOf("vanilla_class").forGetter(test -> test.vanillaClass))
            .and(BuiltInRegistries.ITEM.byNameCodec().listOf().optionalFieldOf("items", List.of()).forGetter(test -> test.items))
            .apply(instance, ItemInteractionTestInstance::new)
    );
    private final Optional<String> vanillaClass;
    private final List<Item> items;

    public ItemInteractionTestInstance(HolderGetter<TestEnvironmentDefinition<?>> testEnvironments, boolean required,
                                       Optional<String> vanillaClass, List<Item> items) {
        super(testEnvironments, required);
        this.vanillaClass = vanillaClass;
        this.items = items;
    }

    @Override
    public void run(GameTestHelper helper) {
        InteractionScenarios.runItemTest(helper, "item interaction " + name(), vanillaClass.orElse(null), items);
    }

    private String name() {
        return vanillaClass.orElseGet(() -> items.stream().map(InteractionScenarios::id).toList().toString());
    }

    @Override
    public MapCodec<ItemInteractionTestInstance> codec() {
        return MAP_CODEC;
    }

    @Override
    protected MutableComponent typeDescription() {
        return Component.literal("Geyser Item Interaction Test for " + name());
    }
}
