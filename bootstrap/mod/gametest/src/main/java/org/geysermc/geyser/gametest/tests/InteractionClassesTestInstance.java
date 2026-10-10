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
import net.minecraft.core.HolderGetter;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.gametest.interaction.InteractionClassData;
import org.geysermc.geyser.interaction.InteractionRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates and validates {@link InteractionRegistry#DATA_FILE}, and checks that Geyser has a handler for every vanilla
 * class in it.
 */
public class InteractionClassesTestInstance extends GeneratedDataTestInstance {
    public static final MapCodec<InteractionClassesTestInstance> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
        commonFields(instance).apply(instance, InteractionClassesTestInstance::new)
    );

    public InteractionClassesTestInstance(HolderGetter<TestEnvironmentDefinition<?>> testEnvironments, boolean required) {
        super(testEnvironments, required);
    }

    @Override
    public void run(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        if (!compareWithResource(InteractionRegistry.DATA_FILE, InteractionClassData.toJson(InteractionClassData.compute()))) {
            failures.add(InteractionRegistry.DATA_FILE + " is outdated, see the log for the expected content");
        }

        Map<String, Set<String>> unhandled = InteractionRegistry.unhandledClasses();
        if (!unhandled.isEmpty()) {
            GeyserImpl.getInstance().getLogger().error("Vanilla interaction classes without a Geyser handler: " + unhandled);
            failures.add("vanilla interaction classes without a Geyser handler: " + unhandled);
        }
        failOrSucceed(helper, failures.isEmpty() ? null : String.join("; ", failures));
    }

    @Override
    public MapCodec<InteractionClassesTestInstance> codec() {
        return MAP_CODEC;
    }

    @Override
    protected MutableComponent typeDescription() {
        return Component.literal("Geyser Interaction Classes Data Test");
    }
}
