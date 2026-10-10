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
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.GameMasterBlockItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SignApplicator;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.entity.vault.VaultState;
import org.geysermc.geyser.gametest.interaction.ScenarioHints;

import java.util.List;

import static org.geysermc.geyser.gametest.interaction.hints.VanillaSources.items;
import static org.geysermc.geyser.gametest.interaction.hints.VanillaSources.sample;
import static org.geysermc.geyser.gametest.interaction.hints.VanillaSources.stacks;

/**
 * Scenario hints for {@code org.geysermc.geyser.interaction.block.BlockEntityBlockInteractions}.
 */
public final class BlockEntityBlockHints {

    public static void register(ScenarioHints hints) {
        // Every sign class extends SignBlock: sign applicators (SignApplicator items), hanging signs to chain
        // (HangingSignItem) and something unrelated
        hints.blockItems("SignBlock", stacks(items(item -> item instanceof SignApplicator)));
        hints.blockItems("SignBlock", stacks(sample(items(item -> item instanceof HangingSignItem), 2)));
        hints.blockItems("SignBlock", stacks(Items.OAK_SIGN, Items.STICK));
        hints.blockSetup("SignBlock", "waxed", (level, pos) -> sign(level, pos, true, false, false));
        hints.blockSetup("SignBlock", "waxed_front_command", (level, pos) -> sign(level, pos, true, true, false));
        hints.blockSetup("SignBlock", "waxed_back_command", (level, pos) -> sign(level, pos, true, false, true));
        hints.blockSetup("SignBlock", "waxed_both_commands", (level, pos) -> sign(level, pos, true, true, true));
        hints.blockSetup("SignBlock", "unwaxed_command", (level, pos) -> sign(level, pos, false, true, true));

        // ChiseledBookShelfBlock: ItemTags.BOOKSHELF_BOOKS, and items that aren't
        hints.blockItems("ChiseledBookShelfBlock", stacks(items(ItemTags.BOOKSHELF_BOOKS)));
        hints.blockItems("ChiseledBookShelfBlock", stacks(Items.PAPER, Items.STICK));
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
        hints.blockItems("GameMasterBlock", stacks(sample(items(item -> item instanceof GameMasterBlockItem), 1)));
        hints.blockItems("GameMasterBlock", stacks(Items.STICK));
        hints.blockItems("LightBlock", stacks(Items.STICK));

        // Copper golem statues: ItemTags.AXES scrape, plus literal honeycomb checks
        hints.blockItems("CopperGolemStatueBlock", stacks(sample(items(ItemTags.AXES), 2)));
        hints.blockItems("CopperGolemStatueBlock", stacks(Items.HONEYCOMB, Items.STICK, Items.COPPER_INGOT));

        // Stack sizes matter for decorated pots (inserting into a full pot)
        hints.blockItems("DecoratedPotBlock", stacks(Items.STICK, Items.DIAMOND, Items.BRICK));
        hints.blockItems("DecoratedPotBlock", new ItemStack(Items.DIAMOND, 64));
        hints.blockSetup("DecoratedPotBlock", "one_diamond", (level, pos) -> pot(level, pos, new ItemStack(Items.DIAMOND)));
        hints.blockSetup("DecoratedPotBlock", "full_diamonds", (level, pos) -> pot(level, pos, new ItemStack(Items.DIAMOND, 64)));

        // JukeboxBlock: items with the jukebox_playable component, and items without
        List<Item> playable = items(item -> new ItemStack(item).has(DataComponents.JUKEBOX_PLAYABLE));
        hints.blockItems("JukeboxBlock", stacks(sample(playable, 2)));
        hints.blockItems("JukeboxBlock", stacks(Items.STICK, Items.DIAMOND));
        hints.blockSetup("JukeboxBlock", "with_record", (level, pos) -> {
            if (level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox && !playable.isEmpty()) {
                // Also sets the has_record block state
                jukebox.setTheItem(new ItemStack(playable.getFirst()));
            }
        });

        // LecternBlock: ItemTags.LECTERN_BOOKS, and items that aren't
        List<Item> lecternBooks = items(ItemTags.LECTERN_BOOKS);
        hints.blockItems("LecternBlock", stacks(lecternBooks));
        hints.blockItems("LecternBlock", stacks(Items.BOOK, Items.ENCHANTED_BOOK, Items.STICK));
        hints.blockSetup("LecternBlock", "with_book", (level, pos) -> {
            if (!lecternBooks.isEmpty()) {
                LecternBlock.tryPlaceBook(null, level, pos, level.getBlockState(pos), new ItemStack(lecternBooks.getFirst()));
            }
        });

        // VaultBlock: the default key item, and the ominous key that ominous vaults are configured with in structures
        hints.blockItems("VaultBlock", VaultConfig.DEFAULT.keyItem().copy(), new ItemStack(Items.OMINOUS_TRIAL_KEY), new ItemStack(Items.STICK));
        hints.blockSetup("VaultBlock", "active", (level, pos) -> vault(level, pos, VaultState.ACTIVE));
        hints.blockSetup("VaultBlock", "inactive", (level, pos) -> vault(level, pos, VaultState.INACTIVE));
        hints.blockSetup("VaultBlock", "unlocking", (level, pos) -> vault(level, pos, VaultState.UNLOCKING));
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
}
