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
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.geysermc.geyser.gametest.interaction.InteractionScenarios;

/**
 * Compares the Java client's and Geyser's predicted results of clicking every block whose {@code useItemOn} or
 * {@code useWithoutItem} is implemented by the given vanilla class.
 */
public class BlockInteractionTestInstance extends GeyserTestInstance {
    public static final MapCodec<BlockInteractionTestInstance> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
        commonFields(instance)
            .and(Codec.STRING.fieldOf("vanilla_class").forGetter(test -> test.vanillaClass))
            .apply(instance, BlockInteractionTestInstance::new)
    );
    private final String vanillaClass;

    public BlockInteractionTestInstance(HolderGetter<TestEnvironmentDefinition<?>> testEnvironments, boolean required, String vanillaClass) {
        super(testEnvironments, required);
        this.vanillaClass = vanillaClass;
    }

    @Override
    public void run(GameTestHelper helper) {
        InteractionScenarios.runBlockTest(helper, "block interaction " + vanillaClass, vanillaClass);
    }

    @Override
    public MapCodec<BlockInteractionTestInstance> codec() {
        return MAP_CODEC;
    }

    @Override
    protected MutableComponent typeDescription() {
        return Component.literal("Geyser Block Interaction Test for " + vanillaClass);
    }
}
