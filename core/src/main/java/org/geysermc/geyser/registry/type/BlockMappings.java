/*
 * Copyright (c) 2019-2022 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.registry.type;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import lombok.Builder;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.geysermc.geyser.api.block.custom.CustomBlockState;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.registry.java.BuiltInJavaRegistries;
import org.geysermc.paletter.Palette;
import org.geysermc.paletter.block.RegisteredBlockState;
import org.geysermc.paletter.vanilla.BedrockBlockState;
import org.geysermc.paletter.vanilla.BedrockBlockType;
import org.geysermc.paletter.vanilla.BedrockBlocks;
import org.geysermc.paletter.vanilla.BedrockStates;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Java block states to Bedrock blocks. Version independent: the tables are keys ({@link BedrockBlockState}) of the
 * newest Bedrock version. What a key is sent as, in a Bedrock version, is the palette's job: a session converts
 * through {@code session.palette()}. The only per-palette data is the cached runtime id table
 * ({@link #runtimeIds(Palette)}) and the identifier table ({@link #javaToBedrockIdentifiers(Palette)}) derived from the keys.
 */
@Builder
@Value
public class BlockMappings {
    // Version independent keys of the blocks Geyser sends by itself
    public static final BedrockBlockState AIR = BedrockBlocks.AIR.defaultState();
    public static final BedrockBlockState WATER = BedrockBlocks.WATER.state().with(BedrockStates.LIQUID_DEPTH, 0).build();
    public static final BedrockBlockState MOVING_BLOCK = BedrockBlocks.MOVING_BLOCK.defaultState();
    public static final BedrockBlockState COMMAND_BLOCK = BedrockBlocks.COMMAND_BLOCK.state()
        .with(BedrockStates.FACING_DIRECTION, 2).with(BedrockStates.CONDITIONAL_BIT, false).build();
    public static final BedrockBlockState MOB_SPAWNER = BedrockBlocks.MOB_SPAWNER.defaultState();
    /**
     * The entity BLOCK data piglins are sent with: Bedrock plays the melee attack animation with the first, the crossbow
     * animations with the second. Historically runtime ids 1 and 0 of the sequential palette, which are these states in
     * every version (sorted first by name hash); looked up by state, so hashed network ids send the same blocks.
     */
    public static final BedrockBlockState PIGLIN_MELEE = BedrockBlocks.HARD_PINK_STAINED_GLASS.defaultState();
    public static final BedrockBlockState PIGLIN_CROSSBOW = BedrockBlocks.CYAN_TERRACOTTA.defaultState();

    /**
     * Java state id to Bedrock block state key, with custom block overrides applied.
     */
    BedrockBlockState[] javaToBedrockStates;
    /**
     * Java state id to Bedrock block state key of the vanilla mapping, without custom block overrides.
     */
    BedrockBlockState[] javaToVanillaBedrockStates;

    /**
     * The Bedrock block of the first state of each vanilla Java block, by Java block id. See
     * {@link #javaToBedrockIdentifiers(Palette)}.
     */
    Int2ObjectMap<BedrockBlockState> vanillaBlocks;
    /**
     * The custom block of non-vanilla Java blocks, by Java block id.
     */
    Int2ObjectMap<BedrockBlockState> nonVanillaBlocks;

    IntArrayList collisionIgnoredBlocks;

    BedrockBlockState netherPortalState;
    Map<Block, BedrockBlockState> flowerPotStates;
    Set<BedrockBlockState> jigsawStates;
    Map<String, BedrockBlockState> structureBlockStates;
    Object2ObjectMap<CustomBlockState, BedrockBlockState> customBlockStates;

    /**
     * Runtime id tables per palette. There is one palette per Bedrock version, which live as long as Geyser does:
     * extending palettes at runtime would grow these caches.
     */
    @Builder.Default
    ConcurrentMap<Palette, int[]> runtimeIdCache = new ConcurrentHashMap<>();
    /**
     * Block tables per palette, like {@link #runtimeIdCache}.
     */
    @Builder.Default
    ConcurrentMap<Palette, RegisteredBlockState[]> blockCache = new ConcurrentHashMap<>();
    /**
     * Identifier tables per palette, like {@link #runtimeIdCache}.
     */
    @Builder.Default
    ConcurrentMap<Palette, Int2ObjectMap<String>> identifierCache = new ConcurrentHashMap<>();

    public @NonNull BedrockBlockState bedrockState(int javaState) {
        if (javaState < 0 || javaState >= this.javaToBedrockStates.length) {
            return AIR;
        }
        return this.javaToBedrockStates[javaState];
    }

    public @NonNull BedrockBlockState vanillaBedrockState(int javaState) {
        if (javaState < 0 || javaState >= this.javaToVanillaBedrockStates.length) {
            return AIR;
        }
        return this.javaToVanillaBedrockStates[javaState];
    }

    /**
     * @return Java state id to runtime id in the given palette; computed once per palette
     */
    public int @NonNull [] runtimeIds(@NonNull Palette palette) {
        return runtimeIdCache.computeIfAbsent(palette, key -> {
            BedrockBlockState[] states = this.javaToBedrockStates;
            int[] ids = new int[states.length];
            int air = palette.runtimeId(AIR);
            for (int i = 0; i < states.length; i++) {
                // A custom block the palette lacks is sent as air
                ids[i] = palette.runtimeIdOrDefault(states[i], air);
            }
            return ids;
        });
    }

    /**
     * @return Java state id to the block in the given palette, matching {@link #runtimeIds(Palette)}; computed once per palette
     */
    public RegisteredBlockState @NonNull [] blocks(@NonNull Palette palette) {
        return blockCache.computeIfAbsent(palette, key -> {
            int[] ids = runtimeIds(palette);
            RegisteredBlockState air = palette.blocks().air();
            RegisteredBlockState[] blocks = new RegisteredBlockState[ids.length];
            for (int i = 0; i < ids.length; i++) {
                RegisteredBlockState block = palette.blocks().byRuntimeId(ids[i]);
                blocks[i] = block != null ? block : air;
            }
            return blocks;
        });
    }

    /**
     * Java block ID -> Bedrock block ID (without minecraft:), IF they are different.
     * While Bedrock is progressing slowly through their flattening, some Bedrock identifiers may differ. What a block
     * is called also depends on the version (a version that lacks the block sends its fallback), so the identifier is
     * derived from the block's key in the given palette. Computed once per palette.
     */
    public @NonNull Int2ObjectMap<String> javaToBedrockIdentifiers(@NonNull Palette palette) {
        return identifierCache.computeIfAbsent(palette, key -> {
            Int2ObjectOpenHashMap<String> identifiers = new Int2ObjectOpenHashMap<>();
            for (Int2ObjectMap.Entry<BedrockBlockState> entry : this.vanillaBlocks.int2ObjectEntrySet()) {
                Block block = BuiltInJavaRegistries.BLOCK.get(entry.getIntKey()).orElse(null);
                if (block == null) {
                    continue;
                }
                String bedrockName = palette.block(entry.getValue()).identifier();
                if (!block.javaIdentifier().toString().equals(bedrockName)) {
                    // Without the minecraft namespace, but keeping any other (custom blocks)
                    String identifier = bedrockName.startsWith("minecraft:") ? bedrockName.substring("minecraft:".length()) : bedrockName;
                    identifiers.put(entry.getIntKey(), identifier.intern());
                }
            }
            for (Int2ObjectMap.Entry<BedrockBlockState> entry : this.nonVanillaBlocks.int2ObjectEntrySet()) {
                RegisteredBlockState block = palette.blockOrNull(entry.getValue());
                if (block != null) {
                    identifiers.put(entry.getIntKey(), block.identifier());
                }
            }
            identifiers.trim();
            return identifiers;
        });
    }

    /**
     * @return whether the block is an item frame or a glow item frame, by key
     */
    public static boolean isItemFrame(@NonNull Palette palette, @NonNull BlockDefinition definition) {
        if (definition instanceof RegisteredBlockState state) {
            BedrockBlockState key = palette.blocks().key(state);
            if (key != null) {
                BedrockBlockType type = key.type();
                return type.equals(BedrockBlocks.FRAME) || type.equals(BedrockBlocks.GLOW_FRAME);
            }
        }
        return false;
    }
}
