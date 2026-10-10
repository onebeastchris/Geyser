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
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import org.geysermc.geyser.gametest.interaction.world.SimulatedPlayer;

import java.util.function.Consumer;

/**
 * Lets interaction handler areas tell the generated interaction game tests which situations are worth testing,
 * on top of the generic scenarios (empty hands, the block's own item, sneaking, every game mode, ...).
 * <p>
 * Vanilla class names are simple class names. A hint applies to a test if its class name is the tested vanilla class,
 * any class in the hierarchy of a tested block or item, up to and including {@code Block}/{@code Item} (so
 * {@code "Item"} applies to every item test), or any interface they implement (e.g. {@code GameMasterBlock}).
 * <p>
 * Hints should derive their inputs from vanilla (tags, vanilla's own tables, block and item classes) where vanilla
 * has them, and only list items or blocks explicitly where vanilla code compares against them literally. The tested classes are the names used in {@code java/interaction_classes.json}.
 */
public interface ScenarioHints {

    /**
     * Items worth holding (in either hand) when clicking blocks backed by the given vanilla class.
     */
    void blockItems(String vanillaClass, ItemStack... stacks);

    /**
     * Prepares the world after the block was placed, e.g. to fill block entity data. Runs once for every tested block
     * of the class (guard with {@code state.hasProperty(...)} or {@code instanceof} checks where needed), and may
     * replace the block or change neighbouring blocks; the test restores the area afterwards.
     * Each setup creates additional scenarios; the plain placed block is always tested as well.
     */
    void blockSetup(String vanillaClass, String name, BlockSetup setup);

    /**
     * Blocks worth clicking (and looking at, for {@code Item#use}) with items backed by the given vanilla class.
     * {@code Blocks.AIR} means looking at nothing in reach.
     */
    void itemTargets(String vanillaClass, Block... blocks);

    /**
     * Like {@link #itemTargets}, with a world setup that runs after the target block was placed
     * (e.g. an end portal frame with an eye, a block above grass, mature crops).
     */
    void itemTargetSetup(String vanillaClass, String name, Block block, BlockSetup setup);

    /**
     * Additional stacks (e.g. enchanted, damaged or with special components) to test like the items backed by the given
     * vanilla class: held in the main hand or the off hand, used in the air and on the targets.
     */
    void itemVariants(String vanillaClass, ItemStack... stacks);

    /**
     * Additional player setups (e.g. hunger, abilities, equipment, other items in the inventory) worth testing for
     * blocks and items backed by the given vanilla class. The setup runs after the game mode was set and the hand
     * items were given to the player, which is a {@link SimulatedPlayer} in a client-side level.
     */
    void playerSetup(String vanillaClass, String name, Consumer<Player> setup);

    /**
     * @return the registries of the game test server, e.g. to create enchanted stacks for {@link #itemVariants}
     */
    HolderLookup.Provider registries();

    /**
     * @return the game test level, e.g. to derive scenarios from the server's recipes
     */
    ServerLevel level();

    /**
     * Changes the game mode (and the abilities that come with it) of a scenario player.
     */
    static void setGameMode(Player player, GameType gameType) {
        if (player instanceof SimulatedPlayer simulated) {
            simulated.setGameMode(gameType);
        } else if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.setGameMode(gameType);
        }
    }

    /**
     * Gives a scenario player the permission level needed for game master blocks (op level 2).
     */
    static void setGameMaster(Player player, boolean gameMaster) {
        if (player instanceof SimulatedPlayer simulated) {
            simulated.setGameMaster(gameMaster);
        }
    }

    @FunctionalInterface
    interface BlockSetup {
        void setup(ServerLevel level, BlockPos pos);
    }
}
