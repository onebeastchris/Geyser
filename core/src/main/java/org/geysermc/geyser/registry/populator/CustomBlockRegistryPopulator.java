/*
 * Copyright (c) 2019-2025 GeyserMC. http://geysermc.org
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

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.api.block.custom.CustomBlockData;
import org.geysermc.geyser.api.block.custom.CustomBlockPermutation;
import org.geysermc.geyser.api.block.custom.CustomBlockState;
import org.geysermc.geyser.api.block.custom.component.BoxComponent;
import org.geysermc.geyser.api.block.custom.component.CustomBlockComponents;
import org.geysermc.geyser.api.block.custom.component.GeometryComponent;
import org.geysermc.geyser.api.block.custom.component.MaterialInstance;
import org.geysermc.geyser.api.block.custom.component.PlacementConditions;
import org.geysermc.geyser.api.block.custom.component.TransformationComponent;
import org.geysermc.geyser.api.block.custom.nonvanilla.JavaBlockState;
import org.geysermc.geyser.api.block.custom.property.CustomBlockProperty;
import org.geysermc.geyser.api.block.custom.property.PropertyType;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomBlocksEvent;
import org.geysermc.geyser.api.util.CreativeCategory;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.GeyserCustomBlockState;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.BoundingBox;
import org.geysermc.geyser.level.physics.PistonBehavior;
import org.geysermc.geyser.registry.BlockRegistries;
import org.geysermc.geyser.registry.java.BuiltInJavaRegistries;
import org.geysermc.geyser.registry.mappings.MappingsConfigReader;
import org.geysermc.geyser.registry.mappings.MappingsType;
import org.geysermc.geyser.registry.type.CustomSkull;
import org.geysermc.geyser.translator.collision.OtherCollision;
import org.geysermc.geyser.util.BlockUtils;
import org.geysermc.geyser.util.MathUtils;
import org.geysermc.paletter.block.BlockProperty;
import org.geysermc.paletter.block.CustomBlock;
import org.geysermc.paletter.block.component.BlockComponent;
import org.geysermc.paletter.block.component.CollisionBox;
import org.geysermc.paletter.block.component.DestructibleByMining;
import org.geysermc.paletter.block.component.DisplayName;
import org.geysermc.paletter.block.component.Friction;
import org.geysermc.paletter.block.component.Geometry;
import org.geysermc.paletter.block.component.LightDampening;
import org.geysermc.paletter.block.component.LightEmission;
import org.geysermc.paletter.block.component.MaterialInstances;
import org.geysermc.paletter.block.component.MenuCategory;
import org.geysermc.paletter.block.component.OnPlayerPlacing;
import org.geysermc.paletter.block.component.PlacementFilter;
import org.geysermc.paletter.block.component.SelectionBox;
import org.geysermc.paletter.block.component.Transformation;
import org.geysermc.paletter.vanilla.BedrockBlockState;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.geysermc.geyser.registry.populator.BlockRegistryPopulator.JAVA_BLOCKS_SIZE;
import static org.geysermc.geyser.registry.populator.BlockRegistryPopulator.MIN_CUSTOM_RUNTIME_ID;

public class CustomBlockRegistryPopulator {

    /**
     * The stage of population
     */
    public enum Stage {
        DEFINITION,
        VANILLA_REGISTRATION,
        NON_VANILLA_REGISTRATION,
        CUSTOM_REGISTRATION
    }

    /**
     * Populates the custom block registries by stage
     *
     * @param stage the stage to populate
     */
    public static void populate(Stage stage) {
        if (!GeyserImpl.getInstance().config().gameplay().enableCustomContent()) {
            return;
        }

        switch (stage) {
            case DEFINITION -> populateBedrock();
            case VANILLA_REGISTRATION -> populateVanilla();
            case NON_VANILLA_REGISTRATION -> populateNonVanilla();
            case CUSTOM_REGISTRATION -> registration();
            default -> throw new IllegalArgumentException("Unknown stage: " + stage);
        }
    }

    private static Set<CustomBlockData> CUSTOM_BLOCKS;
    private static Map<String, CustomBlockData> CUSTOM_BLOCK_ITEM_OVERRIDES;
    private static Map<JavaBlockState, CustomBlockState> NON_VANILLA_BLOCK_STATE_OVERRIDES;
    private static Map<String, CustomBlockState> BLOCK_STATE_OVERRIDES_QUEUE;
    /**
     * The extension or mappings file each custom block comes from
     */
    private static Map<CustomBlockData, String> CUSTOM_BLOCK_SOURCES;

    /**
     * Initializes custom blocks defined by API
     */
    private static void populateBedrock() {
        CUSTOM_BLOCKS = new ObjectLinkedOpenHashSet<>();
        CUSTOM_BLOCK_ITEM_OVERRIDES = new HashMap<>();
        NON_VANILLA_BLOCK_STATE_OVERRIDES = new HashMap<>();
        BLOCK_STATE_OVERRIDES_QUEUE = new HashMap<>();
        CUSTOM_BLOCK_SOURCES = new HashMap<>();

        Set<String> customBlockIdentifiers = new ObjectOpenHashSet<>();
        GeyserImpl.getInstance().getEventBus().fire(new GeyserDefineCustomBlocksEvent() {
            @Override
            public void register(@NonNull CustomBlockData customBlockData) {
                if (customBlockData.name().isEmpty()) {
                    throw new IllegalArgumentException("Custom block name must have at least 1 character.");
                }
                if (!customBlockIdentifiers.add(customBlockData.identifier())) {
                    throw new IllegalArgumentException("Another custom block was already registered under the identifier: " + customBlockData.identifier());
                }
                if (Character.isDigit(customBlockData.name().charAt(0))) {
                    throw new IllegalArgumentException("Custom block can not start with a digit. Name: " + customBlockData.name());
                }
                CUSTOM_BLOCKS.add(customBlockData);
                CUSTOM_BLOCK_SOURCES.put(customBlockData, CustomContentSources.callingExtension());
            }

            @Override
            public void registerOverride(@NonNull String javaIdentifier, @NonNull CustomBlockState customBlockState) {
                if (!CUSTOM_BLOCKS.contains(customBlockState.block())) {
                    throw new IllegalArgumentException("Custom block is unregistered. Name: " + customBlockState.name());
                }
                // We can't register these yet as we don't have the java block id registry populated
                BLOCK_STATE_OVERRIDES_QUEUE.put(javaIdentifier, customBlockState);
            }

            @Override
            public void registerItemOverride(@NonNull String javaIdentifier, @NonNull CustomBlockData customBlockData) {
                if (!CUSTOM_BLOCKS.contains(customBlockData)) {
                    throw new IllegalArgumentException("Custom block is unregistered. Name: " + customBlockData.name());
                }
                CUSTOM_BLOCK_ITEM_OVERRIDES.put(javaIdentifier, customBlockData);
            }

            @Override
            public void registerOverride(@NonNull JavaBlockState javaBlockState, @NonNull CustomBlockState customBlockState) {
                if (!CUSTOM_BLOCKS.contains(customBlockState.block())) {
                    throw new IllegalArgumentException("Custom block is unregistered. Name: " + customBlockState.name());
                }
                NON_VANILLA_BLOCK_STATE_OVERRIDES.put(javaBlockState, customBlockState);
            }
        });
    }

    /**
     * Registers all vanilla custom blocks and skulls defined by API and mappings
     */
    private static void populateVanilla() {
        Int2ObjectMap<CustomBlockState> blockStateOverrides = new Int2ObjectOpenHashMap<>();

        for (CustomSkull customSkull : BlockRegistries.CUSTOM_SKULLS.get().values()) {
            CUSTOM_BLOCKS.add(customSkull.getCustomBlockData());
            CUSTOM_BLOCK_SOURCES.put(customSkull.getCustomBlockData(), "custom skulls");
        }

        for(Map.Entry<String, CustomBlockState> entry : BLOCK_STATE_OVERRIDES_QUEUE.entrySet()) {
            int id = BlockRegistries.JAVA_BLOCK_STATE_IDENTIFIER_TO_ID.getOrDefault(entry.getKey(), -1);
            if (id == -1) {
                GeyserImpl.getInstance().getLogger().warning("Custom block state override for Java Identifier: " +
                        entry.getKey() + " could not be registered as it is not a valid block state.");
                continue;
            }

            CustomBlockState oldBlockState = blockStateOverrides.put(id, entry.getValue());
            if (oldBlockState != null) {
                GeyserImpl.getInstance().getLogger().warning("Duplicate block state override for Java Identifier: " +
                        entry.getKey() + " Old override: " + oldBlockState.name() + " New override: " + entry.getValue().name());
            }
        }
        BLOCK_STATE_OVERRIDES_QUEUE = null;

        MappingsConfigReader.loadCustomMappingsPerFile(MappingsType.BLOCKS, file -> (key, block) -> {
            CUSTOM_BLOCKS.add(block.data());
            CUSTOM_BLOCK_SOURCES.put(block.data(), CustomContentSources.mappingsFile(file));
            if (block.overrideItem()) {
                CUSTOM_BLOCK_ITEM_OVERRIDES.put(block.javaIdentifier(), block.data());
            }
            block.states().forEach((javaIdentifier, customBlockState) -> {
                int id = BlockRegistries.JAVA_BLOCK_STATE_IDENTIFIER_TO_ID.getOrDefault(javaIdentifier, -1);
                blockStateOverrides.put(id, customBlockState);
            });
        });

        BlockRegistries.CUSTOM_BLOCK_STATE_OVERRIDES.set(blockStateOverrides);
        if (!blockStateOverrides.isEmpty()) {
            GeyserImpl.getInstance().getLogger().info("Registered " + blockStateOverrides.size() + " custom block overrides.");
        }

        BlockRegistries.CUSTOM_BLOCK_ITEM_OVERRIDES.set(CUSTOM_BLOCK_ITEM_OVERRIDES);
        if (!CUSTOM_BLOCK_ITEM_OVERRIDES.isEmpty()) {
            GeyserImpl.getInstance().getLogger().info("Registered " + CUSTOM_BLOCK_ITEM_OVERRIDES.size() + " custom block item overrides.");
        }
    }

    /**
     * Registers all non-vanilla custom blocks defined by API
     */
    private static void populateNonVanilla() {
        BlockRegistries.NON_VANILLA_BLOCK_STATE_OVERRIDES.set(NON_VANILLA_BLOCK_STATE_OVERRIDES);

        if (NON_VANILLA_BLOCK_STATE_OVERRIDES.isEmpty()) {
            // Nothing left to register, freeze block state registry
            BlockRegistries.BLOCK_STATES.freeze();
            return;
        }

        MIN_CUSTOM_RUNTIME_ID = BlockRegistries.NON_VANILLA_BLOCK_STATE_OVERRIDES.get().keySet().stream().min(Comparator.comparing(JavaBlockState::javaId)).orElseThrow().javaId();
        int maxCustomRuntimeID = BlockRegistries.NON_VANILLA_BLOCK_STATE_OVERRIDES.get().keySet().stream().max(Comparator.comparing(JavaBlockState::javaId)).orElseThrow().javaId();

        if (MIN_CUSTOM_RUNTIME_ID < BlockRegistries.BLOCK_STATES.get().size()) {
            throw new RuntimeException("Non vanilla custom block state overrides runtime ID must start after the last vanilla block state (" + JAVA_BLOCKS_SIZE + ")");
        }

        JAVA_BLOCKS_SIZE = maxCustomRuntimeID + 1; // Runtime ids start at 0, so we need to add 1

        // Now: Vanilla blocks are already loaded and registered; let's load non-vanilla properly too
        IntSet usedNonVanillaRuntimeIDs = new IntOpenHashSet();

        for (JavaBlockState javaBlockState : BlockRegistries.NON_VANILLA_BLOCK_STATE_OVERRIDES.get().keySet()) {
            if (!usedNonVanillaRuntimeIDs.add(javaBlockState.javaId())) {
                throw new RuntimeException("Duplicate runtime ID " + javaBlockState.javaId() + " for non vanilla Java block state " + javaBlockState.identifier());
            }

            String javaId = javaBlockState.identifier();
            int stateRuntimeId = javaBlockState.javaId();
            String pistonBehavior = javaBlockState.pistonBehavior();

            Block.Builder builder = Block.builder()
                .javaId(stateRuntimeId)
                .destroyTime(javaBlockState.blockHardness())
                    .pushReaction(pistonBehavior == null ? PistonBehavior.PUSH_PULL : PistonBehavior.getByName(pistonBehavior));
            if (!javaBlockState.canBreakWithHand()) {
                builder.requiresCorrectToolForDrops();
            }
            String cleanJavaIdentifier = BlockUtils.getCleanIdentifier(javaBlockState.identifier());

            // Only register new block if it's not in the BLOCK registry yet
            Optional<Block> existing = BuiltInJavaRegistries.BLOCK.get(javaBlockState.stateGroupId());
            if (existing.isEmpty()) {
                Block block = new Block(cleanJavaIdentifier, builder);
                block.setJavaId(javaBlockState.stateGroupId());
                BuiltInJavaRegistries.BLOCK.register(javaBlockState.stateGroupId(), block.javaIdentifier(), block);
            } else {
                // Building the block registered only its first state; every other state needs one too,
                // or BlockState.of() returns air for it and block updates turn it into air on Bedrock
                BlockRegistries.BLOCK_STATES.registerWithAnyIndex(stateRuntimeId,
                    new BlockState(existing.get(), stateRuntimeId), Blocks.AIR.defaultBlockState());
            }

            BlockRegistries.JAVA_BLOCK_STATE_IDENTIFIER_TO_ID.register(javaId, stateRuntimeId);
            BlockRegistries.NON_VANILLA_BLOCK_IDS.register(set -> set.set(stateRuntimeId));

            // TODO register different collision types?
            BoundingBox[] geyserCollisions = Arrays.stream(javaBlockState.collision())
                .map(box -> new BoundingBox(box.middleX(), box.middleY(), box.middleZ(),
                    box.sizeX(), box.sizeY(), box.sizeZ()))
                .toArray(BoundingBox[]::new);
            OtherCollision collision = new OtherCollision(geyserCollisions);
            BlockRegistries.COLLISIONS.registerWithAnyIndex(javaBlockState.javaId(), collision, collision);
        }

        BlockRegistries.BLOCK_STATES.freeze();

        if (!NON_VANILLA_BLOCK_STATE_OVERRIDES.isEmpty()) {
            GeyserImpl.getInstance().getLogger().info("Registered " + NON_VANILLA_BLOCK_STATE_OVERRIDES.size() + " non-vanilla block overrides.");
        }
    }

    /**
     * Registers all bedrock custom blocks defined in previous stages
     */
    private static void registration() {
        Map<CustomBlockData, CustomBlock> paletterBlocks = paletterBlocks(CUSTOM_BLOCKS, CUSTOM_BLOCK_SOURCES);
        CUSTOM_BLOCK_SOURCES = null;
        if (paletterBlocks.size() < CUSTOM_BLOCKS.size()) {
            // Overrides of rejected blocks keep their vanilla block and item
            BlockRegistries.CUSTOM_BLOCK_STATE_OVERRIDES.get().values().removeIf(state -> !paletterBlocks.containsKey(state.block()));
            CUSTOM_BLOCK_ITEM_OVERRIDES.values().removeIf(block -> !paletterBlocks.containsKey(block));
        }

        // Paletter owns the order of custom blocks, their block items and block properties
        BlockRegistries.CUSTOM_BLOCKS.set(paletterBlocks.keySet().toArray(new CustomBlockData[0]));
        if (!paletterBlocks.isEmpty()) {
            GeyserImpl.getInstance().getLogger().info("Registered " + paletterBlocks.size() + " custom blocks.");
        }

        // Paletter generates the states of a custom block; they are mapped to their keys once, here
        Map<CustomBlockState, BedrockBlockState> stateKeys = new Object2ObjectLinkedOpenHashMap<>();
        paletterBlocks.forEach((data, customBlock) -> {
            for (BedrockBlockState key : customBlock.type().states()) {
                stateKeys.put(new GeyserCustomBlockState(data, key.toNbt().getCompound("states")), key);
            }
        });
        BlockRegistries.CUSTOM_PALETTER_BLOCKS.set(new ArrayList<>(paletterBlocks.values()));
        BlockRegistries.CUSTOM_BLOCK_STATE_KEYS.set(stateKeys);
    }

    /**
     * Converts custom blocks to their Paletter form, leaving out (with a warning) those Paletter rejects because
     * Bedrock Dedicated Server would.
     *
     * @param sources the extension or mappings file each block comes from
     * @return the accepted blocks, in their order
     */
    @VisibleForTesting
    static Map<CustomBlockData, CustomBlock> paletterBlocks(Collection<CustomBlockData> blocks, Map<CustomBlockData, String> sources) {
        Map<CustomBlockData, CustomBlock> paletterBlocks = new Object2ObjectLinkedOpenHashMap<>();
        for (CustomBlockData data : blocks) {
            try {
                paletterBlocks.put(data, customBlock(data));
            } catch (IllegalArgumentException exception) {
                CustomContentSources.warnRejected("block", data.identifier(), sources.get(data), exception);
            }
        }
        return paletterBlocks;
    }


    /**
     * Converts a custom block to its Paletter form. Paletter generates the states, the block property data (including
     * its {@code block_id}), palette ordering and runtime ids. The block item is registered separately, together with
     * the other custom items (see {@code ItemRegistryPopulator}).
     *
     * @return the Paletter custom block, without block item
     */
    @SuppressWarnings("unchecked")
    private static CustomBlock customBlock(CustomBlockData customBlock) {
        CustomBlock.Builder builder = CustomBlock.builder(customBlock.identifier())
            .components(blockComponents(customBlock.identifier(), customBlock.components()))
            .item(CustomBlock.BlockItem.NONE);
        if (customBlock.components() != null) {
            // Tags belong to the block; the tags of permutations cannot be sent
            // Sorted: the API hands them over as an unordered set, and the order would change between starts
            customBlock.components().tags().stream().sorted().forEach(builder::tag);
        }
        for (CustomBlockPermutation permutation : customBlock.permutations()) {
            builder.permutation(permutation.condition(), blockComponents(customBlock.identifier(), permutation.components()));
        }

        // The order that properties are defined influences the order that block states are generated
        for (CustomBlockProperty<?> property : customBlock.properties().values()) {
            if (property.type() == PropertyType.booleanProp()) {
                builder.property(BlockProperty.bool(property.name()));
            } else if (property.type() == PropertyType.integerProp()) {
                builder.property(new BlockProperty<>(property.name(), BlockProperty.Type.INTEGER, (List<Integer>) property.values()));
            } else if (property.type() == PropertyType.stringProp()) {
                builder.property(new BlockProperty<>(property.name(), BlockProperty.Type.STRING, (List<String>) property.values()));
            }
        }

        // A menu category is required or the client will crash; "none" without one
        CreativeCategory creativeCategory = customBlock.creativeCategory();
        MenuCategory.Category menuCategory = creativeCategory == null ? MenuCategory.Category.NONE : switch (creativeCategory) {
            case CONSTRUCTION -> MenuCategory.Category.CONSTRUCTION;
            case NATURE -> MenuCategory.Category.NATURE;
            case EQUIPMENT -> MenuCategory.Category.EQUIPMENT;
            case ITEMS -> MenuCategory.Category.ITEMS;
            default -> MenuCategory.Category.NONE;
        };
        String creativeGroup = customBlock.creativeGroup() != null ? customBlock.creativeGroup() : "";
        builder.menuCategory(MenuCategory.builder().category(menuCategory).group(creativeGroup).build());
        return builder.build();
    }

    /**
     * Translates custom block components, without their tags (see {@link #customBlock}).
     */
    private static BlockComponent[] blockComponents(String block, @Nullable CustomBlockComponents components) {
        List<BlockComponent> result = new ArrayList<>();
        if (components == null) {
            return new BlockComponent[0];
        }
        if (components.displayName() != null) {
            result.add(DisplayName.of(components.displayName()));
        }

        BoxComponent selectionBox = components.selectionBox();
        if (selectionBox != null) {
            result.add(SelectionBox.builder()
                .origin(selectionBox.originX(), selectionBox.originY(), selectionBox.originZ())
                .size(selectionBox.sizeX(), selectionBox.sizeY(), selectionBox.sizeZ())
                .build());
        }

        if (!components.collisionBoxes().isEmpty()) {
            result.add(CollisionBox.builder().boxes(components.collisionBoxes().stream()
                .map(box -> CollisionBox.Entry.builder()
                    .origin(box.originX(), box.originY(), box.originZ())
                    .size(box.sizeX(), box.sizeY(), box.sizeZ())
                    .build())
                .toArray(CollisionBox.Entry[]::new)).build());
        }

        GeometryComponent geometryComponent = components.geometry();
        if (geometryComponent != null) {
            Geometry.Builder geometry = Geometry.builder().identifier(geometryComponent.identifier());
            if (geometryComponent.boneVisibility() != null) {
                // Molang expressions are kept as strings, like Bedrock Dedicated Server does ("true" and "false" are constants)
                geometry.boneVisibility(geometryComponent.boneVisibility());
            }
            result.add(geometry.build());
        }

        if (!components.materialInstances().isEmpty()) {
            MaterialInstances.Builder materials = MaterialInstances.builder();
            // Instances with other names, which the geometry's bones refer to
            Map<String, MaterialInstances.Instance> otherInstances = new LinkedHashMap<>();
            for (Map.Entry<String, MaterialInstance> entry : components.materialInstances().entrySet()) {
                MaterialInstance instance = entry.getValue();
                // The texture can be unspecified when blocks.json is used in the resource pack
                MaterialInstances.Instance.Builder material = MaterialInstances.Instance.builder()
                    .ambientOcclusion(instance.ambientOcclusionExponent())
                    .faceDimming(instance.faceDimming())
                    .isotropic(instance.isotropic())
                    .renderMethod(enumValue(MaterialInstances.RenderMethod.class, instance.renderMethod(), block, "render method"))
                    .tintMethod(instance.tintMethod());
                if (instance.texture() != null) {
                    material.texture(instance.texture());
                }
                MaterialInstances.Instance built = material.build();
                switch (entry.getKey()) {
                    case "*" -> materials.all(built);
                    case "up" -> materials.up(built);
                    case "down" -> materials.down(built);
                    case "north" -> materials.north(built);
                    case "south" -> materials.south(built);
                    case "east" -> materials.east(built);
                    case "west" -> materials.west(built);
                    default -> otherInstances.put(entry.getKey(), built);
                }
            }
            materials.instances(otherInstances);
            result.add(materials.build());
        }

        List<PlacementConditions> placementFilter = components.placementFilter();
        if (placementFilter != null) {
            List<PlacementFilter.Conditions> filters = new ArrayList<>(placementFilter.size());
            for (PlacementConditions condition : placementFilter) {
                PlacementFilter.Conditions.Builder filter = PlacementFilter.Conditions.builder()
                    .allowedFaces(condition.allowedFaces().stream().map(face -> face.name().toLowerCase(Locale.ROOT)).toArray(String[]::new));
                List<PlacementFilter.BlockFilter> blockFilters = new ArrayList<>();
                for (Map.Entry<String, PlacementConditions.BlockFilterType> entry : condition.blockFilters().entrySet()) {
                    blockFilters.add(switch (entry.getValue()) {
                        case BLOCK -> PlacementFilter.BlockFilter.builder().name(entry.getKey()).build();
                        case TAG -> PlacementFilter.BlockFilter.builder().tags(entry.getKey()).build();
                    });
                }
                filter.blockFilter(blockFilters);
                filters.add(filter.build());
            }
            result.add(PlacementFilter.builder().conditions(filters).build());
        }

        if (components.destructibleByMining() != null) {
            result.add(DestructibleByMining.builder().secondsToDestroy(components.destructibleByMining()).build());
        }
        if (components.friction() != null) {
            result.add(Friction.of(components.friction()));
        }
        if (components.lightEmission() != null) {
            result.add(LightEmission.of(components.lightEmission()));
        }
        if (components.lightDampening() != null) {
            result.add(LightDampening.of(components.lightDampening()));
        }

        TransformationComponent transformation = components.transformation();
        if (transformation != null) {
            result.add(Transformation.builder()
                // Bedrock only rotates in steps of 90 degrees
                .rotation(MathUtils.unwrapDegreesToInt(transformation.rx()) / 90 * 90,
                    MathUtils.unwrapDegreesToInt(transformation.ry()) / 90 * 90,
                    MathUtils.unwrapDegreesToInt(transformation.rz()) / 90 * 90)
                .scale(transformation.sx(), transformation.sy(), transformation.sz())
                .translation(transformation.tx(), transformation.ty(), transformation.tz())
                .build());
        }

        // place_air is not an actual component: we just apply a dummy event to prevent the client from trying to place a
        // block. This mitigates the issue with the client sometimes double placing blocks.
        if (components.placeAir()) {
            result.add(OnPlayerPlacing.of("geyser:place_event"));
        }
        return result.toArray(BlockComponent[]::new);
    }

    private static <E extends Enum<E>> @Nullable E enumValue(Class<E> type, @Nullable String name, String block, String what) {
        if (name == null) {
            return null;
        }
        try {
            return Enum.valueOf(type, name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            GeyserImpl.getInstance().getLogger().warning("Unknown " + what + " '" + name + "' of custom block " + block + ", ignoring it");
            return null;
        }
    }
}
