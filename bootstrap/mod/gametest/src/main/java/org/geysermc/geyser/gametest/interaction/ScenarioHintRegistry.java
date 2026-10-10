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

import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.geysermc.geyser.gametest.interaction.hints.BlockEntityBlockHints;
import org.geysermc.geyser.gametest.interaction.hints.ItemUseHints;
import org.geysermc.geyser.gametest.interaction.hints.ItemUseOnHints;
import org.geysermc.geyser.gametest.interaction.hints.NaturalBlockHints;
import org.geysermc.geyser.gametest.interaction.hints.PlacementALHints;
import org.geysermc.geyser.gametest.interaction.hints.UtilityBlockHints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Collects the {@link ScenarioHints} of all handler areas.
 */
public final class ScenarioHintRegistry implements ScenarioHints {
    /**
     * Every class registering scenario hints. Add new hint classes here.
     */
    private static final List<Consumer<ScenarioHints>> SOURCES = List.of(
        UtilityBlockHints::register,
        NaturalBlockHints::register,
        BlockEntityBlockHints::register,
        ItemUseHints::register,
        ItemUseOnHints::register,
        PlacementALHints::register
    );

    public record NamedBlockSetup(String name, BlockSetup setup) {
    }

    public record TargetSetup(String name, Block block, BlockSetup setup) {
    }

    public record NamedPlayerSetup(String name, Consumer<Player> setup) {
    }

    private final HolderLookup.Provider registries;
    private final Map<String, List<ItemStack>> blockItems = new HashMap<>();
    private final Map<String, List<NamedBlockSetup>> blockSetups = new HashMap<>();
    private final Map<String, List<Block>> itemTargets = new HashMap<>();
    private final Map<String, List<TargetSetup>> itemTargetSetups = new HashMap<>();
    private final Map<String, List<ItemStack>> itemVariants = new HashMap<>();
    private final Map<String, List<NamedPlayerSetup>> playerSetups = new HashMap<>();

    private ScenarioHintRegistry(HolderLookup.Provider registries) {
        this.registries = registries;
    }

    public static ScenarioHintRegistry load(HolderLookup.Provider registries) {
        ScenarioHintRegistry registry = new ScenarioHintRegistry(registries);
        for (Consumer<ScenarioHints> source : SOURCES) {
            source.accept(registry);
        }
        return registry;
    }

    @Override
    public void blockItems(String vanillaClass, ItemStack... stacks) {
        add(blockItems, vanillaClass, List.of(stacks));
    }

    @Override
    public void blockSetup(String vanillaClass, String name, BlockSetup setup) {
        add(blockSetups, vanillaClass, List.of(new NamedBlockSetup(name, setup)));
    }

    @Override
    public void itemTargets(String vanillaClass, Block... blocks) {
        add(itemTargets, vanillaClass, List.of(blocks));
    }

    @Override
    public void itemTargetSetup(String vanillaClass, String name, Block block, BlockSetup setup) {
        add(itemTargetSetups, vanillaClass, List.of(new TargetSetup(name, block, setup)));
    }

    @Override
    public void itemVariants(String vanillaClass, ItemStack... stacks) {
        add(itemVariants, vanillaClass, List.of(stacks));
    }

    @Override
    public void playerSetup(String vanillaClass, String name, Consumer<Player> setup) {
        add(playerSetups, vanillaClass, List.of(new NamedPlayerSetup(name, setup)));
    }

    @Override
    public HolderLookup.Provider registries() {
        return registries;
    }

    public List<ItemStack> blockItems(Collection<String> classes) {
        return collect(blockItems, classes, ItemStack::copy);
    }

    public List<NamedBlockSetup> blockSetups(Collection<String> classes) {
        return collect(blockSetups, classes, Function.identity());
    }

    public List<Block> itemTargets(Collection<String> classes) {
        return collect(itemTargets, classes, Function.identity());
    }

    public List<TargetSetup> itemTargetSetups(Collection<String> classes) {
        return collect(itemTargetSetups, classes, Function.identity());
    }

    public List<ItemStack> itemVariants(Collection<String> classes) {
        return collect(itemVariants, classes, ItemStack::copy);
    }

    public List<NamedPlayerSetup> playerSetups(Collection<String> classes) {
        return collect(playerSetups, classes, Function.identity());
    }

    private static <T> void add(Map<String, List<T>> map, String vanillaClass, List<T> values) {
        map.computeIfAbsent(vanillaClass, $ -> new ArrayList<>()).addAll(values);
    }

    /**
     * @return the hints registered for any of the classes, without duplicates (by identity), in registration order per class
     */
    private static <T> List<T> collect(Map<String, List<T>> map, Collection<String> classes, Function<T, T> copy) {
        Set<T> result = new LinkedHashSet<>();
        for (String vanillaClass : classes) {
            result.addAll(map.getOrDefault(vanillaClass, List.of()));
        }
        return result.stream().map(copy).toList();
    }
}
