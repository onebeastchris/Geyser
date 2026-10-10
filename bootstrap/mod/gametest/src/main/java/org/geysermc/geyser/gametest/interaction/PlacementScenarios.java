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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.geysermc.geyser.gametest.interaction.hints.PlacementHints;
import org.geysermc.geyser.gametest.interaction.InteractionComparison.Click;
import org.geysermc.geyser.gametest.interaction.InteractionComparison.PlayerState;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Block placement comparison: every item placed through {@code BlockItem#useOn} (every block item that doesn't override
 * {@code useOn}) is used on a fixed set of target situations, in survival with and without sneaking, comparing
 * {@code useItemOn} of the main hand. Mismatches are grouped by the vanilla class implementing the placed block's
 * {@code canSurvive} (or {@code getStateForPlacement}, or the item's placement methods), which is the class of the
 * rule that is most likely wrong.
 */
public final class PlacementScenarios {
    private static final List<PlayerState> PLAYER_STATES = List.of(PlayerState.SURVIVAL, PlayerState.SNEAKING);

    /**
     * @param name description of the situation
     * @param block the target block, or null for the placed item's own block
     * @param face the clicked face of the target
     */
    private record Target(String name, @Nullable Block block, Direction face) {
    }

    /**
     * Situations every block item is tested in: the faces of a full block, a block without sturdy faces, and the item's
     * own block (completing slabs, adding candles, ...). Supports are added per item by {@link #supportTargets}.
     */
    private static final List<Target> TARGETS = List.of(
        new Target("stone top", Blocks.STONE, Direction.UP),
        // Places into mid-air next to the stone, with nothing below
        new Target("stone side", Blocks.STONE, Direction.SOUTH),
        new Target("stone bottom", Blocks.STONE, Direction.DOWN),
        new Target("glass top", Blocks.GLASS, Direction.UP),
        new Target("own block top", null, Direction.UP),
        new Target("own block side", null, Direction.SOUTH)
    );
    private static final List<Direction> SUPPORT_FACES = List.of(Direction.UP, Direction.SOUTH);
    private static final int MAX_SUPPORT_TARGETS = 3;

    /**
     * @param letters the first letters of the item identifiers (paths) to test
     */
    public static void runPlacementTest(GameTestHelper helper, String testName, String letters) {
        InteractionComparison comparison = new InteractionComparison(helper, testName);
        WorldArea area = new WorldArea(helper.getLevel(), InteractionScenarios.testPosition(helper));
        BlockPos pos = area.center();

        for (Item item : placementItems(letters)) {
            Block placed = ((BlockItem) item).getBlock();
            comparison.group(ruleClass(item, placed));
            ItemStack stack = new ItemStack(item);
            List<Target> targets = new ArrayList<>(TARGETS);
            targets.addAll(supportTargets(helper, area, placed));
            for (Target target : targets) {
                BlockState state = (target.block() == null ? placed : target.block()).defaultBlockState();
                area.place(state);
                if (helper.getLevel().getBlockState(pos) != state || state.getShape(helper.getLevel(), pos).isEmpty()) {
                    // Couldn't be placed, or can't be clicked by the Java client
                    continue;
                }
                comparison.situation(target.name() + " " + InteractionComparison.describe(state));
                Click click = Click.center(pos, target.face());
                for (PlayerState playerState : PLAYER_STATES) {
                    comparison.compareUseItemOn(stack, ItemStack.EMPTY, playerState, click, InteractionHand.MAIN_HAND);
                }
            }
        }
        comparison.group(null);
        area.restore();
        comparison.finish(helper);
    }

    /**
     * Finds the supports the placed block needs, from vanilla: one block of each {@code supports_*}/{@code overrides_*}
     * block tag ({@link PlacementHints#supportBlocks}) on whose top or side vanilla's {@code canSurvive} accepts the
     * block (in any facing), where it doesn't accept stone. This covers e.g. crops on farmland, cactus on sand and cocoa
     * on jungle logs without listing them.
     */
    private static List<Target> supportTargets(GameTestHelper helper, WorldArea area, Block placed) {
        BlockPos pos = area.center();
        List<BlockState> orientations = orientations(placed.defaultBlockState());
        List<Direction> faces = new ArrayList<>();
        area.place(Blocks.STONE.defaultBlockState());
        for (Direction face : SUPPORT_FACES) {
            if (!survives(helper, orientations, pos.relative(face))) {
                faces.add(face);
            }
        }
        List<Target> targets = new ArrayList<>();
        for (Block support : PlacementHints.supportBlocks()) {
            if (targets.size() >= MAX_SUPPORT_TARGETS || faces.isEmpty()) {
                break;
            }
            area.place(support.defaultBlockState());
            for (Direction face : faces) {
                if (survives(helper, orientations, pos.relative(face))) {
                    targets.add(new Target(BuiltInRegistries.BLOCK.getKey(support).getPath() + " " + face.getSerializedName(), support, face));
                    break;
                }
            }
        }
        area.restore();
        return targets;
    }

    private static boolean survives(GameTestHelper helper, List<BlockState> orientations, BlockPos pos) {
        for (BlockState state : orientations) {
            try {
                if (state.canSurvive(helper.getLevel(), pos)) {
                    return true;
                }
            } catch (RuntimeException ignored) {
                // Not a support worth testing then
            }
        }
        return false;
    }

    /**
     * @return the state, and the state with each other value of its direction properties
     */
    private static List<BlockState> orientations(BlockState state) {
        List<BlockState> states = new ArrayList<>();
        states.add(state);
        for (Property<?> property : state.getProperties()) {
            if (property.getValueClass() == Direction.class) {
                addValues(states, state, property);
            }
        }
        return states;
    }

    private static <T extends Comparable<T>> void addValues(List<BlockState> states, BlockState state, Property<T> property) {
        for (T value : property.getPossibleValues()) {
            BlockState other = state.setValue(property, value);
            if (!states.contains(other)) {
                states.add(other);
            }
        }
    }

    /**
     * @return every block item using {@code BlockItem#useOn} whose identifier starts with one of the letters, sorted
     */
    public static List<Item> placementItems(String letters) {
        List<Item> items = new ArrayList<>();
        for (Item item : InteractionClassData.itemsOf("BlockItem")) {
            String path = BuiltInRegistries.ITEM.getKey(item).getPath();
            Class<?> useOn = InteractionClassData.Hook.ITEM_USE_ON.declaringClass(item.getClass());
            if (item instanceof BlockItem && useOn == BlockItem.class && !path.isEmpty()
                && letters.indexOf(path.toLowerCase(Locale.ROOT).charAt(0)) >= 0) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * @return the simple name of the vanilla class whose placement rule applies: the block's {@code canSurvive},
     * else its {@code getStateForPlacement}, else the item's placement methods
     */
    private static String ruleClass(Item item, Block block) {
        Class<?> clazz = InteractionClassData.Hook.BLOCK_CAN_SURVIVE.declaringClass(block.getClass());
        if (clazz == null) {
            clazz = InteractionClassData.Hook.BLOCK_STATE_FOR_PLACEMENT.declaringClass(block.getClass());
        }
        if (clazz == null) {
            clazz = InteractionClassData.Hook.ITEM_PLACEMENT.declaringClass(item.getClass());
        }
        return clazz == null ? "Block" : clazz.getSimpleName();
    }

    private PlacementScenarios() {
    }
}
