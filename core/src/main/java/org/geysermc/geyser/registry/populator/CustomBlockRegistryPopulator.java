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
import org.cloudburstmc.nbt.NbtMap;
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
import org.geysermc.geyser.api.block.custom.NonVanillaCustomBlockData;
import org.geysermc.geyser.api.block.custom.property.CustomBlockProperty;
import org.geysermc.geyser.api.block.custom.property.PropertyType;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomBlocksEvent;
import org.geysermc.geyser.api.util.CreativeCategory;
import org.geysermc.geyser.item.exception.InvalidItemComponentsException;
import org.geysermc.geyser.item.type.BlockItem;
import org.geysermc.geyser.item.type.Item;
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
import dev.opencollab.paletter.Molang;
import dev.opencollab.paletter.block.BlockProperty;
import dev.opencollab.paletter.block.CustomBlock;
import dev.opencollab.paletter.block.component.BlockComponent;
import dev.opencollab.paletter.block.component.CollisionBox;
import dev.opencollab.paletter.block.component.DestructibleByMining;
import dev.opencollab.paletter.block.component.BlockDisplayName;
import dev.opencollab.paletter.block.component.Friction;
import dev.opencollab.paletter.block.component.Geometry;
import dev.opencollab.paletter.block.component.LightDampening;
import dev.opencollab.paletter.block.component.LightEmission;
import dev.opencollab.paletter.block.component.MaterialInstances;
import dev.opencollab.paletter.block.component.BlockMenuCategory;
import dev.opencollab.paletter.block.component.OnPlayerPlacing;
import dev.opencollab.paletter.block.component.PlacementFilter;
import dev.opencollab.paletter.block.component.RawBlockComponent;
import dev.opencollab.paletter.block.component.SelectionBox;
import dev.opencollab.paletter.block.component.Transformation;
import dev.opencollab.paletter.item.component.ItemComponent;
import dev.opencollab.paletter.vanilla.BedrockBlockState;
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
        BlockRegistries.CUSTOM_PALETTER_BLOCKS.set(paletterBlocks);
        BlockRegistries.CUSTOM_BLOCK_STATE_KEYS.set(stateKeys);
    }

    /**
     * Leaves out a custom block that Paletter rejected while compiling the palettes: its overrides keep their vanilla
     * block and item.
     *
     * @return whether there was a custom block of that identifier
     */
    static boolean leaveOut(String identifier, RuntimeException exception) {
        CustomBlockData block = null;
        for (CustomBlockData data : BlockRegistries.CUSTOM_PALETTER_BLOCKS.get().keySet()) {
            if (data.identifier().equals(identifier)) {
                block = data;
                break;
            }
        }
        if (block == null) {
            return false;
        }
        CustomBlockData rejected = block;
        CustomContentSources.warnRejected("block", identifier, CUSTOM_BLOCK_SOURCES == null ? null : CUSTOM_BLOCK_SOURCES.get(rejected), exception);
        BlockRegistries.CUSTOM_PALETTER_BLOCKS.get().remove(rejected);
        BlockRegistries.CUSTOM_BLOCKS.set(Arrays.stream(BlockRegistries.CUSTOM_BLOCKS.get()).filter(data -> data != rejected).toArray(CustomBlockData[]::new));
        BlockRegistries.CUSTOM_BLOCK_STATE_KEYS.get().keySet().removeIf(state -> state.block().equals(rejected));
        BlockRegistries.CUSTOM_BLOCK_STATE_OVERRIDES.get().values().removeIf(state -> state.block().equals(rejected));
        BlockRegistries.NON_VANILLA_BLOCK_STATE_OVERRIDES.get().values().removeIf(state -> state.block().equals(rejected));
        BlockRegistries.CUSTOM_BLOCK_ITEM_OVERRIDES.get().values().removeIf(rejected::equals);
        return true;
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
        Set<CustomBlockData> skullBlocks = new ObjectOpenHashSet<>();
        for (CustomSkull skull : BlockRegistries.CUSTOM_SKULLS.get().values()) {
            skullBlocks.add(skull.getCustomBlockData());
        }
        // Custom blocks taking over the item of a vanilla block
        Set<CustomBlockData> itemOverrides = new ObjectOpenHashSet<>();
        if (!BlockRegistries.CUSTOM_BLOCK_ITEM_OVERRIDES.get().isEmpty()) {
            for (Item item : BuiltInJavaRegistries.ITEM) {
                CustomBlockData override = BlockRegistries.CUSTOM_BLOCK_ITEM_OVERRIDES.get(item.javaKey().asString());
                if (override != null && item instanceof BlockItem) {
                    itemOverrides.add(override);
                }
            }
        }
        Map<CustomBlockData, CustomBlock> paletterBlocks = new Object2ObjectLinkedOpenHashMap<>();
        for (CustomBlockData data : blocks) {
            try {
                paletterBlocks.put(data, customBlock(data, blockItem(data, skullBlocks, itemOverrides)));
            } catch (IllegalArgumentException exception) {
                CustomContentSources.warnRejected("block", data.identifier(), sources.get(data), exception);
            }
        }
        return paletterBlocks;
    }


    /**
     * The item a custom block owns:
     * <ul>
     *     <li>none for non-vanilla custom blocks, whose items are non-vanilla custom items;</li>
     *     <li>an item of its own replacing the block item when the block takes over a vanilla block item in the creative
     *     inventory, or for custom skulls: Bedrock only renders the icon of a block item once it has loaded that block,
     *     which leaves custom skulls showing as the vanilla player head until then; an item of their own carries their
     *     own icon;</li>
     *     <li>else the plain block item.</li>
     * </ul>
     */
    private static CustomBlock.BlockItem blockItem(CustomBlockData customBlock, Set<CustomBlockData> skullBlocks, Set<CustomBlockData> itemOverrides) {
        if (customBlock instanceof NonVanillaCustomBlockData) {
            return CustomBlock.BlockItem.NONE;
        }
        boolean creative = customBlock.includedInCreativeInventory();
        if (skullBlocks.contains(customBlock)) {
            try {
                List<ItemComponent> components = ItemRegistryPopulator.skullItemComponents(customBlock.identifier());
                return CustomBlock.BlockItem.custom(item -> CustomItemRegistryPopulator.menuCategory(item.components(components),
                    creative ? customBlock.creativeCategory() : null, customBlock.creativeGroup()));
            } catch (InvalidItemComponentsException exception) {
                GeyserImpl.getInstance().getLogger().error("Failed to register the item form of custom skull " + customBlock.identifier() + "!", exception);
            }
        } else if (creative && itemOverrides.contains(customBlock)) {
            // It takes the place of the vanilla item in the creative inventory (see ItemRegistryPopulator)
            return CustomBlock.BlockItem.custom(item -> CustomItemRegistryPopulator.menuCategory(item,
                customBlock.creativeCategory(), customBlock.creativeGroup()));
        }
        return CustomBlock.BlockItem.SIMPLE;
    }

    /**
     * Converts a custom block to its Paletter form. Paletter generates the states, the block property data (including
     * its {@code block_id}), palette ordering and runtime ids, and registers the item the block owns.
     *
     * @return the Paletter custom block
     */
    @SuppressWarnings("unchecked")
    private static CustomBlock customBlock(CustomBlockData customBlock, CustomBlock.BlockItem blockItem) {
        CustomBlock.Builder builder = CustomBlock.builder(customBlock.identifier())
            .components(blockComponents(customBlock.identifier(), customBlock.components()))
            .item(blockItem);
        if (customBlock.components() != null) {
            // Sorted: the API hands them over as an unordered set, and the order would change between starts
            customBlock.components().tags().stream().sorted().forEach(builder::tag);
        }
        for (CustomBlockPermutation permutation : customBlock.permutations()) {
            List<BlockComponent> components = blockComponents(customBlock.identifier(), permutation.components());
            if (permutation.components() != null) {
                // A permutation has no blockTags field: its tags are tag:<name> components, as Geyser always sent them
                permutation.components().tags().stream().sorted().forEach(tag -> {
                    if (tag.contains(".")) {
                        GeyserImpl.getInstance().getLogger().warning("Ignoring tag '" + tag + "' of a permutation of custom block "
                            + customBlock.identifier() + ": permutation tags cannot contain '.'");
                    } else {
                        components.add(RawBlockComponent.of("tag:" + tag, NbtMap.EMPTY));
                    }
                });
            }
            builder.permutation(CustomBlock.Permutation.of(permutation.condition(), components));
        }

        // The order that properties are defined influences the order that block states are generated
        for (CustomBlockProperty<?> property : customBlock.properties().values()) {
            if (property.type() == PropertyType.booleanProp()) {
                builder.property(BlockProperty.bool(property.name()));
            } else if (property.type() == PropertyType.integerProp()) {
                builder.property(BlockProperty.ints(property.name(), (List<Integer>) property.values()));
            } else if (property.type() == PropertyType.stringProp()) {
                builder.property(BlockProperty.strings(property.name(), (List<String>) property.values()));
            }
        }

        // A menu category is required or the client will crash; "none" without one. It also decides whether the plain
        // block item is in the creative inventory
        CreativeCategory creativeCategory = blockItem == CustomBlock.BlockItem.SIMPLE && !customBlock.includedInCreativeInventory()
            ? null : customBlock.creativeCategory();
        String creativeGroup = customBlock.creativeGroup() != null ? customBlock.creativeGroup() : "";
        // Without a creative category, the category is written as "none"
        builder.menuCategory(BlockMenuCategory.builder().category(CustomItemRegistryPopulator.creativeCategory(creativeCategory))
            .group(creativeGroup).build());
        return builder.build();
    }

    /**
     * Translates custom block components, without their tags (see {@link #customBlock}).
     */
    private static List<BlockComponent> blockComponents(String block, @Nullable CustomBlockComponents components) {
        List<BlockComponent> result = new ArrayList<>();
        if (components == null) {
            return result;
        }
        if (components.displayName() != null) {
            result.add(BlockDisplayName.of(components.displayName()));
        }

        // An empty box (selection_box or collision_box false in the mappings) turns the box off
        BoxComponent selectionBox = components.selectionBox();
        if (selectionBox != null && selectionBox.isEmpty()) {
            result.add(SelectionBox.of(false));
        } else if (selectionBox != null) {
            result.add(SelectionBox.builder()
                .origin(selectionBox.originX(), selectionBox.originY(), selectionBox.originZ())
                .size(selectionBox.sizeX(), selectionBox.sizeY(), selectionBox.sizeZ())
                .build());
        }

        List<BoxComponent> collisionBoxes = components.collisionBoxes().stream().filter(box -> !box.isEmpty()).toList();
        if (!components.collisionBoxes().isEmpty() && collisionBoxes.isEmpty()) {
            result.add(CollisionBox.of(false));
        } else if (!collisionBoxes.isEmpty()) {
            result.add(CollisionBox.builder().boxes(collisionBoxes.stream()
                .map(box -> CollisionBox.Entry.builder()
                    .origin(box.originX(), box.originY(), box.originZ())
                    .size(box.sizeX(), box.sizeY(), box.sizeZ())
                    .build())
                .toList()).build());
        }

        GeometryComponent geometryComponent = components.geometry();
        if (geometryComponent != null) {
            Geometry.Builder geometry = Geometry.builder().identifier(geometryComponent.identifier());
            if (geometryComponent.boneVisibility() != null) {
                // Molang expressions are kept as strings, like Bedrock Dedicated Server does ("true" and "false" are constants)
                Map<String, Molang> boneVisibility = new LinkedHashMap<>();
                geometryComponent.boneVisibility().forEach((bone, visibility) -> boneVisibility.put(bone, Molang.of(visibility)));
                geometry.boneVisibility(boneVisibility);
            }
            result.add(geometry.build());
        }

        if (!components.materialInstances().isEmpty()) {
            MaterialInstances.Builder materials = MaterialInstances.builder();
            // Instances with other names, which the geometry's bones refer to
            Map<String, MaterialInstances.InstanceOrAlias> otherInstances = new LinkedHashMap<>();
            for (Map.Entry<String, MaterialInstance> entry : components.materialInstances().entrySet()) {
                MaterialInstance instance = entry.getValue();
                // The texture can be unspecified when blocks.json is used in the resource pack
                MaterialInstances.Instance.Builder material = MaterialInstances.Instance.builder()
                    .ambientOcclusion(instance.ambientOcclusionExponent())
                    .faceDimming(instance.faceDimming())
                    .isotropic(instance.isotropic())
                    .renderMethod(renderMethod(instance.renderMethod(), block))
                    .tintMethod(instance.tintMethod());
                if (instance.texture() != null) {
                    material.texture(instance.texture());
                }
                MaterialInstances.InstanceOrAlias built = MaterialInstances.InstanceOrAlias.define(material.build());
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
                    .allowedFaces(condition.allowedFaces().stream().map(face -> PlacementFilter.AllowedFaces.of(face.name().toLowerCase(Locale.ROOT))).toList());
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
        return result;
    }

    private static MaterialInstances.@Nullable RenderMethod renderMethod(@Nullable String name, String block) {
        if (name == null) {
            return null;
        }
        MaterialInstances.RenderMethod renderMethod = MaterialInstances.RenderMethod.of(name.toLowerCase(Locale.ROOT));
        if (!renderMethod.isKnown()) {
            GeyserImpl.getInstance().getLogger().warning("Unknown render method '" + name + "' of custom block " + block + ", ignoring it");
            return null;
        }
        return renderMethod;
    }
}
