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

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultState;
import org.geysermc.geyser.gametest.interaction.ScenarioHints;

/**
 * Scenario hints for {@code org.geysermc.geyser.interaction.block.BlockEntityBlockInteractions}.
 */
public final class BlockEntityBlockHints {
    private static final String[] SIGN_CLASSES = {"SignBlock", "CeilingHangingSignBlock", "WallHangingSignBlock"};
    private static final String[] GAME_MASTER_CLASSES = {"CommandBlock", "StructureBlock", "JigsawBlock", "TestBlock", "TestInstanceBlock"};

    public static void register(ScenarioHints hints) {
        for (String signClass : SIGN_CLASSES) {
            // Sign applicators (every SignApplicator class), hanging signs to chain, and something unrelated
            hints.blockItems(signClass, stacks(item("red_dye"), item("black_dye"), Items.INK_SAC, Items.GLOW_INK_SAC,
                Items.HONEYCOMB, Items.OAK_HANGING_SIGN, Items.BAMBOO_HANGING_SIGN, Items.OAK_SIGN, Items.STICK));
            hints.blockSetup(signClass, "waxed", (level, pos) -> sign(level, pos, true, false, false));
            hints.blockSetup(signClass, "waxed_front_command", (level, pos) -> sign(level, pos, true, true, false));
            hints.blockSetup(signClass, "waxed_back_command", (level, pos) -> sign(level, pos, true, false, true));
            hints.blockSetup(signClass, "waxed_both_commands", (level, pos) -> sign(level, pos, true, true, true));
            hints.blockSetup(signClass, "unwaxed_command", (level, pos) -> sign(level, pos, false, true, true));
        }

        hints.blockItems("ChiseledBookShelfBlock", stacks(Items.BOOK, Items.ENCHANTED_BOOK, Items.WRITABLE_BOOK,
            Items.WRITTEN_BOOK, Items.KNOWLEDGE_BOOK, Items.PAPER, Items.STICK));
        hints.blockSetup("ChiseledBookShelfBlock", "full", (level, pos) -> bookshelf(level, pos, 0, 1, 2, 3, 4, 5));
        hints.blockSetup("ChiseledBookShelfBlock", "checkered", (level, pos) -> bookshelf(level, pos, 0, 2, 4));
        hints.blockSetup("ChiseledBookShelfBlock", "checkered_inverse", (level, pos) -> bookshelf(level, pos, 1, 3, 5));

        hints.blockItems("ShelfBlock", stacks(Items.STICK, Items.DIAMOND, Items.BOOK));
        hints.blockSetup("ShelfBlock", "filled", (level, pos) -> {
            if (level.getBlockEntity(pos) instanceof ShelfBlockEntity shelf) {
                shelf.swapItemNoUpdate(0, new ItemStack(Items.DIAMOND));
                shelf.swapItemNoUpdate(2, new ItemStack(Items.BOOK));
                shelf.setChanged();
            }
        });

        // Whether the player may use game master blocks (op + creative) decides these; see the game mode scenarios
        for (String gameMasterClass : GAME_MASTER_CLASSES) {
            hints.blockItems(gameMasterClass, stacks(Items.STICK, Items.COMMAND_BLOCK));
        }
        hints.blockItems("LightBlock", stacks(Items.LIGHT, Items.STICK));

        ItemStack[] statueItems = stacks(Items.WOODEN_AXE, Items.IRON_AXE, Items.HONEYCOMB, Items.STICK, Items.COPPER_INGOT);
        hints.blockItems("CopperGolemStatueBlock", statueItems);
        hints.blockItems("WeatheringCopperGolemStatueBlock", statueItems);

        hints.blockItems("DecoratedPotBlock", stacks(Items.STICK, Items.DIAMOND, Items.BRICK));
        hints.blockItems("DecoratedPotBlock", new ItemStack(Items.DIAMOND, 64));
        hints.blockSetup("DecoratedPotBlock", "one_diamond", (level, pos) -> pot(level, pos, new ItemStack(Items.DIAMOND)));
        hints.blockSetup("DecoratedPotBlock", "full_diamonds", (level, pos) -> pot(level, pos, new ItemStack(Items.DIAMOND, 64)));

        hints.blockItems("JukeboxBlock", stacks(Items.MUSIC_DISC_CAT, Items.MUSIC_DISC_13, Items.STICK, Items.DIAMOND));
        hints.blockSetup("JukeboxBlock", "with_record", (level, pos) -> {
            if (level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
                // Also sets the has_record block state
                jukebox.setTheItem(new ItemStack(Items.MUSIC_DISC_CAT));
            }
        });

        hints.blockItems("LecternBlock", stacks(Items.BOOK, Items.WRITABLE_BOOK, Items.WRITTEN_BOOK, Items.ENCHANTED_BOOK, Items.STICK));
        hints.blockSetup("LecternBlock", "with_book", (level, pos) ->
            LecternBlock.tryPlaceBook(null, level, pos, level.getBlockState(pos), new ItemStack(Items.WRITABLE_BOOK)));

        hints.blockItems("VaultBlock", stacks(Items.TRIAL_KEY, Items.OMINOUS_TRIAL_KEY, Items.STICK));
        hints.blockSetup("VaultBlock", "active", (level, pos) -> vault(level, pos, VaultState.ACTIVE));
        hints.blockSetup("VaultBlock", "inactive", (level, pos) -> vault(level, pos, VaultState.INACTIVE));
        hints.blockSetup("VaultBlock", "unlocking", (level, pos) -> vault(level, pos, VaultState.UNLOCKING));
    }

    private static ItemStack[] stacks(net.minecraft.world.item.Item... items) {
        ItemStack[] stacks = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            stacks[i] = new ItemStack(items[i]);
        }
        return stacks;
    }

    private static void sign(ServerLevel level, BlockPos pos, boolean waxed, boolean frontCommand, boolean backCommand) {
        if (!(level.getBlockEntity(pos) instanceof SignBlockEntity sign)) {
            return;
        }
        if (frontCommand) {
            sign.setText(sign.getFrontText().setMessage(0, commandLine()), true);
        }
        if (backCommand) {
            sign.setText(sign.getBackText().setMessage(0, commandLine()), false);
        }
        sign.setWaxed(waxed);
    }

    private static Component commandLine() {
        return Component.literal("click").withStyle(style -> style.withClickEvent(new ClickEvent.RunCommand("say hi")));
    }

    private static void bookshelf(ServerLevel level, BlockPos pos, int... slots) {
        if (level.getBlockEntity(pos) instanceof ChiseledBookShelfBlockEntity bookshelf) {
            for (int slot : slots) {
                // Also sets the slot_x_occupied block state
                bookshelf.setItem(slot, new ItemStack(Items.BOOK));
            }
        }
    }

    private static void pot(ServerLevel level, BlockPos pos, ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof DecoratedPotBlockEntity pot) {
            pot.setTheItem(stack);
            pot.setChanged();
        }
    }

    private static void vault(ServerLevel level, BlockPos pos, VaultState state) {
        level.setBlock(pos, level.getBlockState(pos).setValue(VaultBlock.STATE, state), 3);
    }

    private BlockEntityBlockHints() {
    }

    private static net.minecraft.world.item.Item item(String path) {
        // 26.x groups coloured items into collections; look them up by identifier instead
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.withDefaultNamespace(path));
    }
}
