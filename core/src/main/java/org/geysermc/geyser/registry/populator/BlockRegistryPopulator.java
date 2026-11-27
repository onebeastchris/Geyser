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

package org.geysermc.geyser.registry.populator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.extern.slf4j.Slf4j;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.api.block.custom.CustomBlockState;
import org.geysermc.geyser.api.block.custom.nonvanilla.JavaBlockState;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.block.type.FlowerPotBlock;
import org.geysermc.geyser.network.bedrock.GameProtocol;
import org.geysermc.geyser.registry.BedrockPalettes;
import org.geysermc.geyser.registry.BlockRegistries;
import org.geysermc.geyser.registry.java.BuiltInJavaRegistries;
import org.geysermc.geyser.registry.type.BlockMappings;
import org.geysermc.geyser.util.JsonUtils;
import dev.opencollab.paletter.PaletteLayer;
import dev.opencollab.paletter.Paletter;
import dev.opencollab.paletter.block.CustomBlock;
import dev.opencollab.paletter.data.DataVersion;
import dev.opencollab.paletter.vanilla.BedrockBlockState;
import dev.opencollab.paletter.vanilla.BedrockBlocks;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Populates the block registries.
 * <p>
 * Java block states are mapped to block states of the newest Bedrock version (from the mappings), and those to
 * Paletter handles ({@link BedrockBlockState}), once, as the one version independent {@link BlockMappings}. A session
 * resolves the handles against its palette; older versions get adapted states and fallbacks from Paletter.
 */
@Slf4j
public final class BlockRegistryPopulator {
    /**
     * The stage of population
     */
    public enum Stage {
        PRE_INIT,
        INIT_JAVA,
        INIT_BEDROCK,
        POST_INIT
    }

    public static void populate(Stage stage) {
        switch (stage) {
            case PRE_INIT, POST_INIT -> nullifyBlocksNbt();
            case INIT_JAVA -> registerJavaBlocks();
            case INIT_BEDROCK -> registerBedrockBlocks();
            default -> throw new IllegalArgumentException("Unknown stage: " + stage);
        }
    }

    private static final boolean ALLOW_MALFORMED_PALETTES = Boolean.getBoolean("Geyser.Debug.AllowMalformedPalettes");

    /**
     * Stores the Java to Bedrock block mappings until they are no longer needed.
     */
    private static JavaBlockMappingTable BLOCK_MAPPINGS;
    /**
     * The amount of vanilla Java block states
     */
    private static int VANILLA_JAVA_BLOCKS_SIZE = -1;
    public static int MIN_CUSTOM_RUNTIME_ID = -1;
    public static int JAVA_BLOCKS_SIZE = -1;

    private static void nullifyBlocksNbt() {
        BLOCK_MAPPINGS = null;
    }

    static final String BLOCK_LAYER = "geyser:blocks";

    private static void registerBedrockBlocks() {
        // The tables are keys, the same in every palette with the same layers: no palette is needed
        BlockRegistries.BLOCKS.set(buildMappings());
        GeyserImpl.getInstance().getLogger().debug("Added " + BlockRegistries.CUSTOM_BLOCK_STATE_KEYS.get().size() + " custom block states to the Bedrock palettes.");
    }

    /**
     * Builds Geyser's one Paletter: the custom block layer, then the item layer.
     *
     * @throws dev.opencollab.paletter.PaletteException if content of a layer cannot be compiled
     */
    static Paletter buildPaletter(PaletteLayer itemLayer) {
        // Paletter may bundle Bedrock versions older than the oldest one Geyser supports: no session asks for their palettes
        DataVersion oldest = Paletter.bundledVersion(GameProtocol.SUPPORTED_BEDROCK_PROTOCOLS.intStream().min().orElseThrow());

        Paletter.Builder paletter = Paletter.builder()
            .versions(version -> version.compareTo(oldest) >= 0)
            // What older Bedrock versions show for the newest content they lack
            .fallbacks(BedrockFallbacks.policy())
            // Geyser never reads behaviour values (friction, shapes, ...): their data is never loaded
            .behaviour(false)
            // Content a version has behind an experiment is sent with the experiment
            .experiments(BedrockFallbacks.EXPERIMENTS)
            // What Paletter adapted, replaced or dropped per version, in Geyser's log instead of the System.Logger
            .reports(report -> BedrockPalettes.logReport(report, GeyserImpl.getInstance().getLogger()));

        // Custom blocks are Paletter content: it generates their states, block properties and runtime ids
        PaletteLayer.Builder layer = PaletteLayer.builder(BLOCK_LAYER);
        for (CustomBlock customBlock : BlockRegistries.CUSTOM_PALETTER_BLOCKS.get().values()) {
            layer.block(customBlock);
        }
        paletter.layer(layer.build());
        paletter.layer(itemLayer);
        Paletter built = paletter.build();
        for (int protocol : GameProtocol.SUPPORTED_BEDROCK_PROTOCOLS) {
            if (!built.supports(protocol)) {
                throw new IllegalStateException("The bundled Paletter has no data for supported Bedrock protocol " + protocol);
            }
        }
        return built;
    }

    /**
     * Hands the Paletter to {@link BedrockPalettes}.
     *
     * @param blocksLeftOut whether custom blocks were left out while compiling, so the block mappings no longer have them
     */
    static void initializePalettes(Paletter paletter, boolean blocksLeftOut) {
        BedrockPalettes.initialize(paletter);
        if (blocksLeftOut) {
            BlockRegistries.BLOCKS.set(buildMappings());
        }
        populate(Stage.POST_INIT);
    }

    private static BlockMappings buildMappings() {
        Map<CustomBlockState, BedrockBlockState> customStateKeys = BlockRegistries.CUSTOM_BLOCK_STATE_KEYS.get();

        BedrockBlockState air = BedrockBlocks.AIR.defaultState();
        BedrockBlockState[] javaToBedrock = new BedrockBlockState[JAVA_BLOCKS_SIZE];
        BedrockBlockState[] javaToVanillaBedrock = new BedrockBlockState[JAVA_BLOCKS_SIZE];
        IntArrayList collisionIgnoredBlocks = new IntArrayList();
        List<BlockState> javaBlockStates = BlockRegistries.BLOCK_STATES.get();
        Set<String> erroredIds = new HashSet<>();

        int javaRuntimeId = -1;
        while (++javaRuntimeId < VANILLA_JAVA_BLOCKS_SIZE) {
            BlockState blockState = javaBlockStates.get(javaRuntimeId);
            String javaId = blockState.toString();

            BedrockBlockState vanillaBedrock;
            String problem;
            try {
                vanillaBedrock = BLOCK_MAPPINGS.bedrockState(blockState);
                problem = vanillaBedrock == null ? "Block tag:\n" + BLOCK_MAPPINGS.rawState(blockState) : null;
            } catch (IllegalArgumentException | IllegalStateException e) {
                // blocks.json has no value for this state
                vanillaBedrock = null;
                problem = e.getMessage();
            }
            if (vanillaBedrock == null) {
                if (!ALLOW_MALFORMED_PALETTES) {
                    throw new RuntimeException("""
                        Unable to find a Bedrock %s block state for %s! The Java block mappings must be regenerated against the bundled Paletter version. %s"""
                        .formatted(BedrockPalettes.newestBundledVersion(), javaId, problem));
                }
                String javaIdReplacement = javaId.contains("[") ? javaId.substring(0, javaId.indexOf('[')) : javaId;
                if (erroredIds.add(javaIdReplacement)) {
                    GeyserImpl.getInstance().getLogger().error("""
                        Unable to find a Bedrock %s block state for %s! This will be replaced with air. %s"""
                        .formatted(BedrockPalettes.newestBundledVersion(), javaId, problem));
                }
                vanillaBedrock = air;
            }

            BedrockBlockState bedrock = vanillaBedrock;
            CustomBlockState blockStateOverride = BlockRegistries.CUSTOM_BLOCK_STATE_OVERRIDES.get(javaRuntimeId);
            if (blockStateOverride != null) {
                bedrock = customStateKeys.get(blockStateOverride);
                if (bedrock == null) {
                    throw new RuntimeException("Unable to find " + javaId + " Bedrock runtime ID! Custom block override: \n" +
                        blockStateOverride);
                }
            }

            Block block = blockState.block();
            if (block == Blocks.BAMBOO || block == Blocks.POINTED_DRIPSTONE) {
                collisionIgnoredBlocks.add(javaRuntimeId);
            }

            boolean waterlogged = blockState.getValue(Properties.WATERLOGGED, false)
                || block == Blocks.BUBBLE_COLUMN || block == Blocks.KELP || block == Blocks.KELP_PLANT
                || block == Blocks.SEAGRASS || block == Blocks.TALL_SEAGRASS;
            if (waterlogged) {
                BlockRegistries.WATERLOGGED.get().set(javaRuntimeId);
            }

            javaToVanillaBedrock[javaRuntimeId] = vanillaBedrock;
            javaToBedrock[javaRuntimeId] = bedrock;
        }

        Map<JavaBlockState, CustomBlockState> nonVanillaStateOverrides = BlockRegistries.NON_VANILLA_BLOCK_STATE_OVERRIDES.get();
        if (!nonVanillaStateOverrides.isEmpty()) {
            // First ensure all non vanilla runtime IDs at minimum are air in case they aren't consecutive
            for (int i = MIN_CUSTOM_RUNTIME_ID; i < JAVA_BLOCKS_SIZE; i++) {
                javaToBedrock[i] = air;
                javaToVanillaBedrock[i] = air;
            }

            for (Map.Entry<JavaBlockState, CustomBlockState> entry : nonVanillaStateOverrides.entrySet()) {
                BedrockBlockState bedrock = customStateKeys.get(entry.getValue());
                if (bedrock == null) {
                    GeyserImpl.getInstance().getLogger().warning("Unable to find custom block for " + entry.getValue());
                    continue;
                }

                JavaBlockState javaState = entry.getKey();
                int stateRuntimeId = javaState.javaId();
                if (javaState.waterlogged()) {
                    BlockRegistries.WATERLOGGED.register(set -> set.set(stateRuntimeId));
                }

                javaToVanillaBedrock[stateRuntimeId] = bedrock; // TODO: Check this?
                javaToBedrock[stateRuntimeId] = bedrock;
            }
        }

        // Derived from the Java side and keys alone
        List<Block> javaPottable = BuiltInJavaRegistries.BLOCK.parallelStream()
            .flatMap(block -> {
                if (block instanceof FlowerPotBlock flowerPot && flowerPot.flower() != Blocks.AIR) {
                    return Stream.of(flowerPot.flower());
                }
                return null;
            })
            .toList();
        Map<Block, BedrockBlockState> flowerPotStates = new Object2ObjectOpenHashMap<>();
        Set<BedrockBlockState> jigsawStates = new ObjectOpenHashSet<>();
        Map<String, BedrockBlockState> structureBlockStates = new Object2ObjectOpenHashMap<>();
        Int2ObjectMap<BedrockBlockState> vanillaBlocks = new Int2ObjectOpenHashMap<>();
        BedrockBlockState netherPortalState = null;
        Block lastBlockSeen = null;
        for (int id = 0; id < VANILLA_JAVA_BLOCKS_SIZE; id++) {
            BlockState state = javaBlockStates.get(id);
            Block block = state.block();
            BedrockBlockState key = javaToBedrock[id];
            if (block != lastBlockSeen) {
                lastBlockSeen = block;
                // The identifier is taken from the first state, see BlockMappings#javaToBedrockIdentifiers
                vanillaBlocks.put(block.javaId(), key);
            }
            if (block == Blocks.JIGSAW) {
                jigsawStates.add(key);
            } else if (block == Blocks.STRUCTURE_BLOCK) {
                structureBlockStates.put(state.getValue(Properties.STRUCTUREBLOCK_MODE).toUpperCase(Locale.ROOT), key);
            } else if (block == Blocks.NETHER_PORTAL) {
                netherPortalState = key;
            }
            if (javaPottable.contains(block)) {
                // Specifically NOT putIfAbsent - mangrove propagule breaks otherwise
                flowerPotStates.put(block, key);
            }
        }
        if (netherPortalState == null) {
            throw new AssertionError("Unable to find nether portal block");
        }

        Int2ObjectMap<BedrockBlockState> nonVanillaBlocks = new Int2ObjectOpenHashMap<>();
        for (Map.Entry<JavaBlockState, CustomBlockState> entry : nonVanillaStateOverrides.entrySet()) {
            BedrockBlockState bedrock = customStateKeys.get(entry.getValue());
            if (bedrock != null) {
                nonVanillaBlocks.put(entry.getKey().stateGroupId(), bedrock);
            }
        }

        Object2ObjectMap<CustomBlockState, BedrockBlockState> customBlockStates = customStateKeys.isEmpty()
            ? Object2ObjectMaps.emptyMap() : new Object2ObjectOpenHashMap<>(customStateKeys);
        return BlockMappings.builder()
            .javaToBedrockStates(javaToBedrock)
            .javaToVanillaBedrockStates(javaToVanillaBedrock)
            .vanillaBlocks(vanillaBlocks)
            .nonVanillaBlocks(nonVanillaBlocks)
            .collisionIgnoredBlocks(collisionIgnoredBlocks)
            .netherPortalState(netherPortalState)
            .flowerPotStates(flowerPotStates)
            .jigsawStates(jigsawStates)
            .structureBlockStates(structureBlockStates)
            .customBlockStates(customBlockStates)
            .build();
    }

    private static void registerJavaBlocks() {
        JavaBlockMappingTable blockMappings;
        try (InputStream stream = GeyserImpl.getInstance().getBootstrap().getResourceOrThrow("mappings/blocks.json")) {
            blockMappings = JavaBlockMappingTable.of(JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject());
        } catch (Exception e) {
            throw new AssertionError("Unable to load Java block mappings", e);
        }

        int javaRuntimeId = -1;
        for (BlockState javaBlockState : BlockRegistries.BLOCK_STATES.get()) {
            javaRuntimeId++;
            String javaId = javaBlockState.toString().intern();

            BlockRegistries.JAVA_BLOCK_STATE_IDENTIFIER_TO_ID.register(javaId, javaRuntimeId);
        }

        BLOCK_MAPPINGS = blockMappings;
        // Only vanilla states are registered at this point; non-vanilla custom block states come later
        VANILLA_JAVA_BLOCKS_SIZE = BlockRegistries.BLOCK_STATES.get().size();
        JAVA_BLOCKS_SIZE = VANILLA_JAVA_BLOCKS_SIZE;

        JsonObject blockInteractionsJson;
        try (InputStream stream = GeyserImpl.getInstance().getBootstrap().getResourceOrThrow("mappings/interactions.json")) {
            blockInteractionsJson = JsonUtils.fromJson(stream);
        } catch (Exception e) {
            throw new AssertionError("Unable to load Java block interaction mappings", e);
        }

        BlockRegistries.INTERACTIVE.set(toBlockStateSet(blockInteractionsJson.getAsJsonArray("always_consumes")));
        BlockRegistries.INTERACTIVE_MAY_BUILD.set(toBlockStateSet(blockInteractionsJson.getAsJsonArray("requires_may_build")));
    }

    private static BitSet toBlockStateSet(JsonArray node) {
        BitSet blockStateSet = new BitSet(node.size());
        for (JsonElement javaIdentifier : node) {
            blockStateSet.set(BlockRegistries.JAVA_BLOCK_STATE_IDENTIFIER_TO_ID.get().getInt(javaIdentifier.getAsString()));
        }
        return blockStateSet;
    }
}
