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

package org.geysermc.geyser.gametest.interaction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.gametest.interaction.InteractionComparison.Click;
import org.geysermc.geyser.gametest.interaction.InteractionComparison.PlayerState;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Generates the scenarios of the interaction tests and runs them through {@link InteractionComparison}.
 * <p>
 * Block tests, for every block of the tested class:
 * <ul>
 *     <li>the default state is clicked on its front face (or top) with the full matrix: empty hands, the block's own
 *     item, {@link #COMMON_ITEMS} and hinted items, in either hand, in every game mode, sneaking and as op;</li>
 *     <li>for representative blocks (the first block, and the first block of each runtime class): every hinted block
 *     setup, clicked on the top face, on the front face at a 3x2 grid of locations and on the other side faces above
 *     and below the middle (the player always stands in front of the clicked face), with a reduced matrix;</li>
 *     <li>for representative blocks: every value of every block state property, clicked on the top face and below and
 *     above the middle of the front face, with the reduced matrix. Hinted setups are therefore only needed for world
 *     changes, block entity data and combinations of properties.</li>
 * </ul>
 * Item tests, for every item of the tested class (at most {@link #MAX_ITEMS_PER_CLASS}) and every hinted variant:
 * using it in the air in every game mode, hungry and on cooldown, and clicking and looking at {@link #DEFAULT_TARGETS}
 * and hinted targets, on the top and a side face.
 * <p>
 * Hinted player setups are added to the player states of every matrix.
 */
public final class InteractionScenarios {
    /**
     * Items commonly relevant for block interactions, held in the full block matrix.
     */
    private static final List<Item> COMMON_ITEMS = List.of(Items.STICK, Items.STONE, Items.BREAD, Items.FLINT_AND_STEEL,
        Items.BONE_MEAL, Items.WATER_BUCKET, Items.GLASS_BOTTLE, Items.HONEYCOMB, Items.WOODEN_AXE, Items.SHEARS);
    private static final List<Block> DEFAULT_TARGETS = List.of(Blocks.STONE, Blocks.GRASS_BLOCK, Blocks.OAK_LOG);
    private static final int MAX_ITEMS_PER_CLASS = 24;
    private static final int MAX_STATE_VARIANTS = 48;

    private static final List<PlayerState> FULL_EMPTY_HANDS = List.of(PlayerState.SURVIVAL, PlayerState.SNEAKING, PlayerState.CREATIVE,
        PlayerState.CREATIVE_OP, PlayerState.ADVENTURE, PlayerState.SPECTATOR);
    private static final List<PlayerState> FULL_MAIN_HAND = List.of(PlayerState.SURVIVAL, PlayerState.SNEAKING, PlayerState.CREATIVE,
        PlayerState.CREATIVE_OP, PlayerState.ADVENTURE);
    private static final List<PlayerState> FULL_OFF_HAND = List.of(PlayerState.SURVIVAL, PlayerState.SNEAKING);
    private static final List<PlayerState> REDUCED_EMPTY_HANDS = List.of(PlayerState.SURVIVAL, PlayerState.SNEAKING, PlayerState.CREATIVE_OP);
    private static final List<PlayerState> REDUCED_MAIN_HAND = List.of(PlayerState.SURVIVAL, PlayerState.SNEAKING);
    private static final List<PlayerState> REDUCED_OFF_HAND = List.of(PlayerState.SURVIVAL);
    private static final List<PlayerState> ITEM_AIR = List.of(PlayerState.SURVIVAL, PlayerState.SNEAKING, PlayerState.CREATIVE,
        PlayerState.ADVENTURE, PlayerState.SPECTATOR, PlayerState.HUNGRY, PlayerState.ON_COOLDOWN);
    private static final List<PlayerState> ITEM_AIR_OFF_HAND = List.of(PlayerState.SURVIVAL, PlayerState.HUNGRY);
    private static final List<PlayerState> ITEM_TARGET = List.of(PlayerState.SURVIVAL, PlayerState.SNEAKING, PlayerState.CREATIVE,
        PlayerState.ADVENTURE);

    /**
     * @return the position scenarios are run at: above the test structure, so setups can change the blocks around it
     */
    public static BlockPos testPosition(GameTestHelper helper) {
        return helper.absolutePos(BlockPos.ZERO).above(WorldArea.RADIUS + 1);
    }

    public static void runBlockTest(GameTestHelper helper, String testName, String vanillaClass) {
        InteractionComparison comparison = new InteractionComparison(helper, testName);
        List<Block> blocks = InteractionClassData.blocksOf(vanillaClass);
        if (blocks.isEmpty()) {
            GeyserImpl.getInstance().getLogger().warning("[" + testName + "] no blocks use " + vanillaClass + "; re-run the :gametest:runDatagen task");
            helper.succeed();
            return;
        }
        ScenarioHintRegistry hints = ScenarioHintRegistry.load(helper.getLevel());
        WorldArea area = new WorldArea(helper.getLevel(), testPosition(helper));
        warnIfNotEmpty(area, testName);
        BlockPos pos = area.center();

        Set<Class<?>> representedClasses = new HashSet<>();
        for (Block block : blocks) {
            boolean representative = representedClasses.add(block.getClass());
            List<String> classes = new ArrayList<>(InteractionClassData.hierarchyNames(block.getClass(), Block.class));
            classes.add(vanillaClass);

            List<ItemStack> hintItems = hints.blockItems(classes);
            List<PlayerState> hintPlayers = hints.playerSetups(classes).stream().map(PlayerState::hint).toList();
            List<ItemStack> reducedItems = new ArrayList<>();
            if (block.asItem() != Items.AIR) {
                reducedItems.add(new ItemStack(block.asItem()));
            }
            reducedItems.addAll(hintItems);
            List<ItemStack> fullItems = new ArrayList<>(reducedItems);
            COMMON_ITEMS.forEach(item -> fullItems.add(new ItemStack(item)));

            BlockState defaultState = block.defaultBlockState();
            Direction primaryFace = primaryFace(defaultState);

            comparison.situation(InteractionComparison.describe(defaultState));
            area.place(defaultState);
            blockMatrix(comparison, fullItems, hintPlayers, Click.center(pos, primaryFace), true);

            if (!representative) {
                continue;
            }
            // null: the plain block
            List<ScenarioHintRegistry.@Nullable NamedBlockSetup> setups = new ArrayList<>();
            setups.add(null);
            setups.addAll(hints.blockSetups(classes));
            for (ScenarioHintRegistry.NamedBlockSetup setup : setups) {
                for (Click click : representativeClicks(pos, primaryFace)) {
                    area.place(defaultState);
                    String situation = InteractionComparison.describe(defaultState);
                    if (setup != null) {
                        situation += " with setup " + setup.name();
                        try {
                            setup.setup().setup(helper.getLevel(), pos);
                        } catch (Throwable throwable) {
                            comparison.mismatch(situation + ": block setup threw " + throwable);
                            break;
                        }
                        situation += " -> " + InteractionComparison.describe(helper.getLevel().getBlockState(pos));
                    }
                    comparison.situation(situation);
                    blockMatrix(comparison, reducedItems, hintPlayers, click, false);
                }
            }

            for (BlockState variant : stateVariants(defaultState)) {
                for (Click click : variantClicks(pos, primaryFace(variant))) {
                    area.place(variant);
                    comparison.situation(InteractionComparison.describe(variant));
                    blockMatrix(comparison, reducedItems, hintPlayers, click, false);
                }
            }
        }
        area.restore();
        comparison.finish(helper);
    }

    private static void blockMatrix(InteractionComparison comparison, List<ItemStack> items, List<PlayerState> hintPlayers, Click click, boolean full) {
        List<PlayerState> emptyHands = concat(full ? FULL_EMPTY_HANDS : REDUCED_EMPTY_HANDS, hintPlayers);
        List<PlayerState> mainHand = concat(full ? FULL_MAIN_HAND : REDUCED_MAIN_HAND, hintPlayers);
        List<PlayerState> offHand = full ? FULL_OFF_HAND : REDUCED_OFF_HAND;

        for (PlayerState state : emptyHands) {
            bothHands(comparison, ItemStack.EMPTY, ItemStack.EMPTY, state, click);
        }
        for (ItemStack item : items) {
            for (PlayerState state : mainHand) {
                bothHands(comparison, item, ItemStack.EMPTY, state, click);
            }
            for (PlayerState state : offHand) {
                bothHands(comparison, ItemStack.EMPTY, item, state, click);
            }
        }
    }

    private static void bothHands(InteractionComparison comparison, ItemStack main, ItemStack off, PlayerState state, Click click) {
        comparison.compareUseItemOn(main, off, state, click, InteractionHand.MAIN_HAND);
        comparison.compareUseItemOn(main, off, state, click, InteractionHand.OFF_HAND);
    }

    /**
     * @param vanillaClass the item class to test, or null to only test the given items
     * @param explicitItems items to test in addition to the class' items
     */
    public static void runItemTest(GameTestHelper helper, String testName, @Nullable String vanillaClass, List<Item> explicitItems) {
        InteractionComparison comparison = new InteractionComparison(helper, testName);
        ScenarioHintRegistry hints = ScenarioHintRegistry.load(helper.getLevel());
        WorldArea area = new WorldArea(helper.getLevel(), testPosition(helper));
        warnIfNotEmpty(area, testName);
        BlockPos pos = area.center();

        List<ItemStack> stacks = new ArrayList<>();
        if (vanillaClass != null) {
            List<Item> items = InteractionClassData.itemsOf(vanillaClass);
            if (items.isEmpty() && explicitItems.isEmpty()) {
                GeyserImpl.getInstance().getLogger().warning("[" + testName + "] no items use " + vanillaClass + "; re-run the :gametest:runDatagen task");
            }
            sample(items, MAX_ITEMS_PER_CLASS).forEach(item -> stacks.add(new ItemStack(item)));
            stacks.addAll(hints.itemVariants(List.of(vanillaClass)));
        }
        explicitItems.forEach(item -> stacks.add(new ItemStack(item)));

        for (ItemStack stack : stacks) {
            List<String> classes = new ArrayList<>(InteractionClassData.hierarchyNames(stack.getItem().getClass(), Item.class));
            if (vanillaClass != null) {
                classes.add(vanillaClass);
            }
            List<PlayerState> hintPlayers = hints.playerSetups(classes).stream().map(PlayerState::hint).toList();

            // Using the item in the air
            area.restore();
            comparison.situation("air");
            Click air = Click.air(pos);
            for (PlayerState state : concat(ITEM_AIR, hintPlayers)) {
                comparison.compareUseItem(stack, ItemStack.EMPTY, state, air, InteractionHand.MAIN_HAND);
            }
            for (PlayerState state : ITEM_AIR_OFF_HAND) {
                comparison.compareUseItem(ItemStack.EMPTY, stack, state, air, InteractionHand.OFF_HAND);
            }

            // Using the item on (or while looking at) targets
            List<ScenarioHintRegistry.TargetSetup> targets = new ArrayList<>();
            Set<Block> targetBlocks = new LinkedHashSet<>(DEFAULT_TARGETS);
            targetBlocks.addAll(hints.itemTargets(classes));
            targetBlocks.forEach(block -> targets.add(new ScenarioHintRegistry.TargetSetup("", block, (level, blockPos) -> { })));
            targets.addAll(hints.itemTargetSetups(classes));
            for (ScenarioHintRegistry.TargetSetup target : targets) {
                for (Direction face : List.of(Direction.UP, Direction.SOUTH)) {
                    BlockState state = target.block().defaultBlockState();
                    area.place(state);
                    String situation = InteractionComparison.describe(state);
                    if (!target.name().isEmpty()) {
                        situation += " with setup " + target.name();
                        try {
                            target.setup().setup(helper.getLevel(), pos);
                        } catch (Throwable throwable) {
                            comparison.mismatch(situation + ": target setup threw " + throwable);
                            break;
                        }
                        state = helper.getLevel().getBlockState(pos);
                        situation += " -> " + InteractionComparison.describe(state);
                    }
                    comparison.situation(situation);
                    // Blocks without an outline (air, fluids) can't be clicked by the Java client, only looked through
                    boolean clickable = !state.getShape(helper.getLevel(), pos).isEmpty();
                    Click click = Click.center(pos, face);
                    List<PlayerState> states = face == Direction.UP ? concat(ITEM_TARGET, hintPlayers) : ITEM_TARGET;
                    for (PlayerState playerState : states) {
                        if (clickable) {
                            comparison.compareUseItemOn(stack, ItemStack.EMPTY, playerState, click, InteractionHand.MAIN_HAND);
                            comparison.compareUseItemOn(stack, ItemStack.EMPTY, playerState, click, InteractionHand.OFF_HAND);
                        }
                        comparison.compareUseItem(stack, ItemStack.EMPTY, playerState, click, InteractionHand.MAIN_HAND);
                    }
                    if (clickable) {
                        comparison.compareUseItemOn(ItemStack.EMPTY, stack, PlayerState.SURVIVAL, click, InteractionHand.MAIN_HAND);
                        comparison.compareUseItemOn(ItemStack.EMPTY, stack, PlayerState.SURVIVAL, click, InteractionHand.OFF_HAND);
                    }
                    comparison.compareUseItem(ItemStack.EMPTY, stack, PlayerState.SURVIVAL, click, InteractionHand.OFF_HAND);
                }
            }
        }
        area.restore();
        comparison.finish(helper);
    }

    /**
     * @return the face a player would usually click: the direction the block faces, or the top face
     */
    private static Direction primaryFace(BlockState state) {
        for (Property<?> property : state.getBlock().getStateDefinition().getProperties()) {
            if (state.getValue(property) instanceof Direction direction && direction != Direction.DOWN) {
                return direction;
            }
        }
        return Direction.UP;
    }

    /**
     * Clicks covering the faces of a block: the top face; a 3x2 grid on the primary face; and the other side faces
     * below and above the middle.
     */
    private static List<Click> representativeClicks(BlockPos pos, Direction primaryFace) {
        List<Click> clicks = new ArrayList<>();
        clicks.add(Click.center(pos, Direction.UP));
        for (Direction face : Direction.Plane.HORIZONTAL) {
            if (face == primaryFace) {
                for (double v : new double[] {0.25, 0.75}) {
                    for (double u : new double[] {0.2, 0.5, 0.8}) {
                        clicks.add(Click.on(pos, face, u, v));
                    }
                }
            } else {
                clicks.add(Click.on(pos, face, 0.5, 0.25));
                clicks.add(Click.on(pos, face, 0.5, 0.75));
            }
        }
        return clicks;
    }

    /**
     * Clicks for every state variant: the top, and the lower and upper half of the primary face, as interactions often
     * depend on whether the hit is above the middle of the block (e.g. extinguishing candle cakes, slab halves).
     */
    private static List<Click> variantClicks(BlockPos pos, Direction primaryFace) {
        Direction side = primaryFace.getAxis().isHorizontal() ? primaryFace : Direction.SOUTH;
        return List.of(Click.center(pos, Direction.UP), Click.on(pos, side, 0.5, 0.25), Click.on(pos, side, 0.5, 0.75));
    }

    /**
     * @return the default state with each value of each property, except the default state itself
     */
    private static List<BlockState> stateVariants(BlockState defaultState) {
        Set<BlockState> variants = new LinkedHashSet<>();
        for (Property<?> property : defaultState.getBlock().getStateDefinition().getProperties()) {
            addVariants(variants, defaultState, property);
        }
        variants.remove(defaultState);
        return variants.stream().limit(MAX_STATE_VARIANTS).toList();
    }

    private static <T extends Comparable<T>> void addVariants(Set<BlockState> variants, BlockState state, Property<T> property) {
        for (T value : property.getPossibleValues()) {
            variants.add(state.setValue(property, value));
        }
    }

    /**
     * @return at most {@code max} evenly spread elements of the list, always including the first
     */
    private static <T> List<T> sample(List<T> list, int max) {
        if (list.size() <= max) {
            return list;
        }
        List<T> result = new ArrayList<>(max);
        for (int i = 0; i < max; i++) {
            result.add(list.get(i * list.size() / max));
        }
        return result;
    }

    private static <T> List<T> concat(List<T> first, List<T> second) {
        if (second.isEmpty()) {
            return first;
        }
        List<T> result = new ArrayList<>(first);
        result.addAll(second);
        return result;
    }

    private static void warnIfNotEmpty(WorldArea area, String testName) {
        if (!area.wasEmpty()) {
            GeyserImpl.getInstance().getLogger().warning("[" + testName + "] the area around " + area.center()
                + " is not empty; scenarios may be affected by the blocks around it");
        }
    }

    /**
     * @return a readable identifier of an item, for test names
     */
    public static String id(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
    }

    private InteractionScenarios() {
    }
}
