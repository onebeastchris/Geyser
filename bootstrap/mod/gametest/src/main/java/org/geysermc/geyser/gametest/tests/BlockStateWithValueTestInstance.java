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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.geysermc.geyser.GeyserImpl;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Checks Geyser's {@code BlockState#withValue} against vanilla's {@code BlockState#setValue}, for every block state,
 * every property and every value. Properties are matched by name and values by their serialized name. Also checks that
 * every state ID belongs to the same block in Geyser.
 */
public class BlockStateWithValueTestInstance extends GeyserTestInstance {
    public static final MapCodec<BlockStateWithValueTestInstance> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
        commonFields(instance).apply(instance, BlockStateWithValueTestInstance::new)
    );
    private static final int MAX_REPORTED = 50;

    private int mismatchCount;
    private final List<String> mismatches = new ArrayList<>();

    public BlockStateWithValueTestInstance(HolderGetter<TestEnvironmentDefinition<?>> testEnvironments, boolean required) {
        super(testEnvironments, required);
    }

    @Override
    public void run(GameTestHelper helper) {
        mismatchCount = 0;
        mismatches.clear();
        int stateCount = Block.BLOCK_STATE_REGISTRY.size();
        for (int id = 0; id < stateCount; id++) {
            BlockState state = Block.stateById(id);
            org.geysermc.geyser.level.block.type.BlockState geyserState = org.geysermc.geyser.level.block.type.BlockState.of(id);
            String vanillaBlock = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
            if (!geyserState.block().javaIdentifier().asString().equals(vanillaBlock)) {
                mismatch("state " + id + " is " + state + " in vanilla, but " + geyserState + " in Geyser");
                continue;
            }
            for (Property<?> property : state.getBlock().getStateDefinition().getProperties()) {
                check(state, geyserState, property);
            }
        }

        if (mismatchCount == 0) {
            helper.succeed();
            return;
        }
        GeyserImpl.getInstance().getLogger().error("[block_state_with_value] " + mismatchCount + " interaction mismatches:\n"
            + String.join("\n", mismatches));
        helper.fail(mismatchCount + " BlockState#withValue mismatches (see log), e.g. " + mismatches.getFirst());
    }

    private <T extends Comparable<T>> void check(BlockState state, org.geysermc.geyser.level.block.type.BlockState geyserState, Property<T> property) {
        org.geysermc.geyser.level.block.property.Property<?> geyserProperty = findProperty(geyserState, property.getName());
        if (geyserProperty == null) {
            mismatch(geyserState + " has no property " + property.getName() + " (vanilla " + state + ")");
            return;
        }
        for (T value : property.getPossibleValues()) {
            BlockState expected = state.setValue(property, value);
            String valueName = property.getName(value);
            String actual;
            try {
                org.geysermc.geyser.level.block.type.BlockState result = withValue(geyserState, geyserProperty, valueName);
                if (result == null) {
                    actual = "no value " + valueName + " for " + geyserProperty;
                } else if (result.javaId() == Block.getId(expected)) {
                    continue;
                } else {
                    actual = result.toString();
                }
            } catch (Throwable throwable) {
                actual = "threw " + throwable;
            }
            mismatch(geyserState + " with " + property.getName() + "=" + valueName + ": vanilla=" + expected + " geyser=" + actual);
        }
    }

    private static <G extends Comparable<G>> org.geysermc.geyser.level.block.type.@Nullable BlockState withValue(
            org.geysermc.geyser.level.block.type.BlockState state, org.geysermc.geyser.level.block.property.Property<G> property, String valueName) {
        Optional<G> value = property.valueOf(valueName);
        return value.map(v -> state.withValue(property, v)).orElse(null);
    }

    private static org.geysermc.geyser.level.block.property.@Nullable Property<?> findProperty(org.geysermc.geyser.level.block.type.BlockState state, String name) {
        org.geysermc.geyser.level.block.property.Property<?>[] keys = state.block().propertyKeys();
        if (keys == null) {
            return null;
        }
        for (org.geysermc.geyser.level.block.property.Property<?> key : keys) {
            if (key.name().equals(name)) {
                return key;
            }
        }
        return null;
    }

    private void mismatch(String message) {
        mismatchCount++;
        if (mismatches.size() < MAX_REPORTED) {
            mismatches.add(message);
        }
    }

    @Override
    public MapCodec<BlockStateWithValueTestInstance> codec() {
        return MAP_CODEC;
    }

    @Override
    protected MutableComponent typeDescription() {
        return Component.literal("Geyser BlockState#withValue Test");
    }
}
