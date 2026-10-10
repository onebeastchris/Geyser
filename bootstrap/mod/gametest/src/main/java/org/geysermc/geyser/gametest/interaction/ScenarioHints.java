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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

/**
 * Lets interaction handler areas tell the generated interaction game tests which situations are worth testing,
 * on top of the generic scenarios (empty hands, the block's own item, sneaking, every game mode, ...).
 * Vanilla class names are the same names as used in {@code java/interaction_classes.json}.
 */
public interface ScenarioHints {

    /**
     * Items worth holding (in either hand) when clicking blocks backed by the given vanilla class.
     */
    void blockItems(String vanillaClass, ItemStack... stacks);

    /**
     * Prepares the world after the block was placed, e.g. to fill block entity data.
     * Each setup creates additional scenarios; the plain placed block is always tested as well.
     */
    void blockSetup(String vanillaClass, String name, BlockSetup setup);

    /**
     * Blocks worth clicking with items backed by the given vanilla class.
     */
    void itemTargets(String vanillaClass, Block... blocks);

    /**
     * Additional player setups (e.g. hunger, abilities) worth testing for items backed by the given vanilla class.
     */
    void playerSetup(String vanillaClass, String name, Consumer<Player> setup);

    @FunctionalInterface
    interface BlockSetup {
        void setup(ServerLevel level, BlockPos pos);
    }
}
