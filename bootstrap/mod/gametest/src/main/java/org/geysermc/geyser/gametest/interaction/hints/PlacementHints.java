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
package org.geysermc.geyser.gametest.interaction.hints;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.geysermc.geyser.gametest.interaction.InteractionClassData;
import org.geysermc.geyser.gametest.interaction.InteractionClassData.Hook;
import org.geysermc.geyser.gametest.interaction.ScenarioHints;
import org.geysermc.geyser.gametest.interaction.world.ReflectionUtils;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * Scenarios for block placement ({@code org.geysermc.geyser.interaction.placement}), all derived from vanilla:
 * <ul>
 *     <li>one item for every vanilla class overriding a placement method ({@code canSurvive},
 *     {@code getStateForPlacement}, {@code canBeReplaced} or a {@code BlockItem} placement method), so every
 *     placement rule is exercised by the block item test;</li>
 *     <li>as targets, one block for every vanilla class overriding {@code canBeReplaced} (completing slabs, adding
 *     candles, ...) and one block of every {@code supports_*}/{@code overrides_*} block tag, the tags vanilla uses for
 *     placement supports.</li>
 * </ul>
 * Every block item is additionally placed on a fixed set of targets by {@code PlacementScenarios}.
 */
public final class PlacementHints {

    public static void register(ScenarioHints hints) {
        SortedMap<String, SortedMap<String, SortedSet<String>>> classes = InteractionClassData.compute();

        Set<Item> variants = new LinkedHashSet<>();
        for (Hook hook : List.of(Hook.BLOCK_CAN_SURVIVE, Hook.BLOCK_STATE_FOR_PLACEMENT, Hook.BLOCK_CAN_BE_REPLACED)) {
            for (SortedSet<String> blocks : classes.get(hook.key()).values()) {
                blocks.stream().map(PlacementHints::block).map(Block::asItem).filter(item -> item != Items.AIR).findFirst()
                    .ifPresent(variants::add);
            }
        }
        for (SortedSet<String> items : classes.get(Hook.ITEM_PLACEMENT.key()).values()) {
            items.stream().map(id -> BuiltInRegistries.ITEM.getValue(Identifier.parse(id))).findFirst().ifPresent(variants::add);
        }
        hints.itemVariants("BlockItem", VanillaSources.stacks(variants));

        Set<Block> targets = new LinkedHashSet<>();
        for (SortedSet<String> blocks : classes.get(Hook.BLOCK_CAN_BE_REPLACED.key()).values()) {
            targets.add(block(blocks.first()));
        }
        targets.addAll(supportBlocks());
        hints.itemTargets("BlockItem", targets.toArray(Block[]::new));

        hints.playerSetup("GameMasterBlockItem", "game_master", player -> ScenarioHints.setGameMaster(player, true));
    }

    /**
     * @return one block of each block tag vanilla uses for placement supports (see {@link #placementTags})
     */
    public static List<Block> supportBlocks() {
        // Tags don't change during game tests
        List<Block> cached = supportBlocks;
        if (cached == null) {
            Set<Block> blocks = new LinkedHashSet<>();
            for (TagKey<Block> tag : placementTags()) {
                VanillaSources.blocks(tag).stream().findFirst().ifPresent(blocks::add);
            }
            cached = List.copyOf(blocks);
            supportBlocks = cached;
        }
        return cached;
    }

    private static volatile @Nullable List<Block> supportBlocks;

    /**
     * @return the block tags vanilla uses for placement supports: the {@code BlockTags} constants named
     * {@code SUPPORTS_*} or {@code OVERRIDES_*}
     */
    @SuppressWarnings("unchecked")
    private static List<TagKey<Block>> placementTags() {
        return ReflectionUtils.staticFields(BlockTags.class, TagKey.class).stream()
            .filter(field -> field.getName().startsWith("SUPPORTS_") || field.getName().startsWith("OVERRIDES_"))
            .map(field -> (TagKey<Block>) ReflectionUtils.get(field, null))
            .toList();
    }

    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));
    }

    private PlacementHints() {
    }
}
