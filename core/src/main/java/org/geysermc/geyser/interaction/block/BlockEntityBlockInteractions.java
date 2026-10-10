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

package org.geysermc.geyser.interaction.block;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.cloudburstmc.math.vector.Vector3d;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.interaction.BlockEntityInteractionData;
import org.geysermc.geyser.interaction.BlockHit;
import org.geysermc.geyser.interaction.InteractionContext;
import org.geysermc.geyser.interaction.InteractionRegistry;
import org.geysermc.geyser.interaction.UseOnContext;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.item.type.DyeItem;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.BooleanProperty;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.session.cache.tags.ItemTag;
import org.geysermc.geyser.util.InteractionResult;
import org.geysermc.geyser.util.MathUtils;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityType;

import java.util.Set;

/**
 * Blocks whose client-side interaction depends on block entity data or game master permissions (signs, shelves, pots, command blocks, ...).
 * <p>
 * Vanilla checks that the block entity exists before doing anything; we assume that it does, since the server always
 * sends the block entities of these blocks.
 */
public final class BlockEntityBlockInteractions {
    private static final BooleanProperty[] BOOKSHELF_SLOTS = {
        Properties.SLOT_0_OCCUPIED, Properties.SLOT_1_OCCUPIED, Properties.SLOT_2_OCCUPIED,
        Properties.SLOT_3_OCCUPIED, Properties.SLOT_4_OCCUPIED, Properties.SLOT_5_OCCUPIED
    };
    private static final int BOOKSHELF_ROWS = 2;
    private static final int BOOKSHELF_COLUMNS = 3;
    private static final int SHELF_ROWS = 1;
    private static final int SHELF_COLUMNS = 3;

    /**
     * Items of the vanilla {@code HangingSignItem} class.
     */
    private static final Set<Item> HANGING_SIGN_ITEMS = Set.of(
        Items.OAK_HANGING_SIGN, Items.SPRUCE_HANGING_SIGN, Items.BIRCH_HANGING_SIGN, Items.JUNGLE_HANGING_SIGN,
        Items.ACACIA_HANGING_SIGN, Items.CHERRY_HANGING_SIGN, Items.DARK_OAK_HANGING_SIGN, Items.PALE_OAK_HANGING_SIGN,
        Items.MANGROVE_HANGING_SIGN, Items.BAMBOO_HANGING_SIGN, Items.CRIMSON_HANGING_SIGN, Items.WARPED_HANGING_SIGN
    );

    /**
     * Horizontal offset of a wall sign's hitbox center from the block center, towards the block it is attached to.
     */
    private static final double WALL_SIGN_CENTER_OFFSET = 7.0 / 16.0;

    public static void register(InteractionRegistry.Registrar registrar) {
        // Signs. Client-side, using an item on a sign always consumes the click, so useWithoutItem is not normally reached
        registrar.blockUseItemOn("SignBlock", BlockEntityBlockInteractions::signUseItemOn);
        registrar.blockUseWithoutItem("SignBlock", (context, state) -> InteractionResult.CONSUME);
        registrar.blockUseItemOn("CeilingHangingSignBlock", (context, state) ->
            context.clickedFace() == Direction.DOWN && shouldChainHangingSign(context, state)
                ? InteractionResult.PASS : signUseItemOn(context, state));
        registrar.blockUseItemOn("WallHangingSignBlock", (context, state) ->
            // Clicking a side of the sign that does not hold text
            context.clickedFace().getAxis() != state.getValue(Properties.HORIZONTAL_FACING).getAxis() && shouldChainHangingSign(context, state)
                ? InteractionResult.PASS : signUseItemOn(context, state));

        registrar.blockUseItemOn("ChiseledBookShelfBlock", BlockEntityBlockInteractions::chiseledBookshelfUseItemOn);
        registrar.blockUseWithoutItem("ChiseledBookShelfBlock", BlockEntityBlockInteractions::chiseledBookshelfUseWithoutItem);
        registrar.blockUseItemOn("ShelfBlock", BlockEntityBlockInteractions::shelfUseItemOn);

        // Game master blocks open their screen client-side, if the player is allowed to
        registrar.blockUseWithoutItem("CommandBlock", BlockEntityBlockInteractions::gameMasterBlockUseWithoutItem);
        registrar.blockUseWithoutItem("StructureBlock", BlockEntityBlockInteractions::gameMasterBlockUseWithoutItem);
        registrar.blockUseWithoutItem("JigsawBlock", BlockEntityBlockInteractions::gameMasterBlockUseWithoutItem);
        registrar.blockUseWithoutItem("TestBlock", BlockEntityBlockInteractions::gameMasterBlockUseWithoutItem);
        registrar.blockUseWithoutItem("TestInstanceBlock", BlockEntityBlockInteractions::gameMasterBlockUseWithoutItem);
        // Only the server cycles the light level
        registrar.blockUseWithoutItem("LightBlock", (context, state) -> InteractionResult.CONSUME);

        registrar.blockUseItemOn("CopperGolemStatueBlock", (context, state) ->
            isAxe(context.player(), context.stack()) ? InteractionResult.PASS : InteractionResult.SUCCESS);
        registrar.blockUseItemOn("WeatheringCopperGolemStatueBlock", BlockEntityBlockInteractions::weatheringCopperGolemStatueUseItemOn);

        // The client always predicts success; the server decides whether the item fits
        registrar.blockUseItemOn("DecoratedPotBlock", (context, state) -> InteractionResult.SUCCESS);
        registrar.blockUseWithoutItem("DecoratedPotBlock", (context, state) -> InteractionResult.SUCCESS);

        registrar.blockUseItemOn("JukeboxBlock", BlockEntityBlockInteractions::jukeboxUseItemOn);
        registrar.blockUseWithoutItem("JukeboxBlock", (context, state) ->
            state.getValue(Properties.HAS_RECORD) ? InteractionResult.SUCCESS : InteractionResult.PASS);

        registrar.blockUseItemOn("LecternBlock", BlockEntityBlockInteractions::lecternUseItemOn);
        registrar.blockUseWithoutItem("LecternBlock", (context, state) ->
            state.getValue(Properties.HAS_BOOK) ? InteractionResult.SUCCESS : InteractionResult.CONSUME);

        registrar.blockUseItemOn("VaultBlock", (context, state) ->
            !context.stack().isEmpty() && "active".equals(state.getValue(Properties.VAULT_STATE))
                ? InteractionResult.SUCCESS_SERVER : InteractionResult.TRY_WITH_EMPTY_HAND);
    }

    private static @NonNull InteractionResult signUseItemOn(@NonNull UseOnContext context, @NonNull BlockState state) {
        boolean hasApplicatorToUse = isSignApplicator(context.stack()) && context.player().mayBuild();
        if (!hasApplicatorToUse && !isWaxed(context.player(), context.clickedPos())) {
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Mirrors the vanilla {@code SignApplicator} interface: dyes, (glow) ink sacs and honeycomb.
     */
    private static boolean isSignApplicator(@NonNull GeyserItemStack stack) {
        Item item = stack.asItem();
        return item instanceof DyeItem || item == Items.INK_SAC || item == Items.GLOW_INK_SAC || item == Items.HONEYCOMB;
    }

    private static boolean isWaxed(@NonNull InteractionContext player, @NonNull Vector3i position) {
        BlockEntityInteractionData data = player.blockEntityData(position);
        return data != null && data.waxed();
    }

    /**
     * Both hanging sign classes: whether the player holds a hanging sign to place another one onto this sign.
     * The clicked face is checked by the caller.
     */
    private static boolean shouldChainHangingSign(@NonNull UseOnContext context, @NonNull BlockState state) {
        return HANGING_SIGN_ITEMS.contains(context.stack().asItem()) && !canExecuteClickCommands(context.player(), context.clickedPos(), state);
    }

    /**
     * Mirrors {@code SignBlockEntity#canExecuteClickCommands} for the side the player is facing.
     */
    private static boolean canExecuteClickCommands(@NonNull InteractionContext player, @NonNull Vector3i position, @NonNull BlockState state) {
        BlockEntityInteractionData data = player.blockEntityData(position);
        if (data == null || !data.waxed() || !(data.frontClickCommand() || data.backClickCommand())) {
            return false;
        }
        return isFacingFrontText(player, position, state) ? data.frontClickCommand() : data.backClickCommand();
    }

    /**
     * Mirrors {@code SignBlockEntity#isFacingFrontText}.
     */
    private static boolean isFacingFrontText(@NonNull InteractionContext player, @NonNull Vector3i position, @NonNull BlockState state) {
        double centerX = 0.5;
        double centerZ = 0.5;
        float signYRot;
        Integer rotation = state.getValueNullable(Properties.ROTATION_16);
        if (rotation != null) {
            // Standing and ceiling hanging signs
            signYRot = rotation * 22.5F;
        } else {
            Direction facing = state.getValue(Properties.HORIZONTAL_FACING);
            signYRot = yRot(facing);
            if (state.block().blockEntityType() == BlockEntityType.SIGN) {
                // Wall signs (unlike wall hanging signs) sit at the edge of the block
                centerX -= facing.getUnitVector().getX() * WALL_SIGN_CENTER_OFFSET;
                centerZ -= facing.getUnitVector().getZ() * WALL_SIGN_CENTER_OFFSET;
            }
        }
        Vector3d playerPosition = player.position();
        double xd = playerPosition.getX() - (position.getX() + centerX);
        double zd = playerPosition.getZ() - (position.getZ() + centerZ);
        // Vanilla uses a table-based approximation of atan2 here; the difference only matters right at the edge
        float playerYRot = (float) (Math.atan2(zd, xd) * 180.0F / (float) Math.PI) - 90.0F;
        return Math.abs(MathUtils.wrapDegrees(playerYRot - signYRot)) <= 90.0F;
    }

    /**
     * Mirrors {@code Direction#toYRot} for horizontal directions.
     */
    private static float yRot(@NonNull Direction direction) {
        return switch (direction) {
            case WEST -> 90.0F;
            case NORTH -> 180.0F;
            case EAST -> 270.0F;
            default -> 0.0F;
        };
    }

    private static @NonNull InteractionResult chiseledBookshelfUseItemOn(@NonNull UseOnContext context, @NonNull BlockState state) {
        if (!context.player().isInTag(ItemTag.BOOKSHELF_BOOKS, context.stack().asItem().javaId())) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        int slot = hitSlot(context.hit(), state.getValue(Properties.HORIZONTAL_FACING), BOOKSHELF_ROWS, BOOKSHELF_COLUMNS);
        if (slot < 0) {
            return InteractionResult.PASS;
        }
        // Inserting into an occupied slot falls back to taking the book out
        return state.getValue(BOOKSHELF_SLOTS[slot]) ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.SUCCESS;
    }

    private static @NonNull InteractionResult chiseledBookshelfUseWithoutItem(@NonNull UseOnContext context, @NonNull BlockState state) {
        int slot = hitSlot(context.hit(), state.getValue(Properties.HORIZONTAL_FACING), BOOKSHELF_ROWS, BOOKSHELF_COLUMNS);
        if (slot < 0) {
            return InteractionResult.PASS;
        }
        return state.getValue(BOOKSHELF_SLOTS[slot]) ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    private static @NonNull InteractionResult shelfUseItemOn(@NonNull UseOnContext context, @NonNull BlockState state) {
        if (context.hand() == Hand.OFF_HAND) {
            return InteractionResult.PASS;
        }
        if (hitSlot(context.hit(), state.getValue(Properties.HORIZONTAL_FACING), SHELF_ROWS, SHELF_COLUMNS) < 0) {
            return InteractionResult.PASS;
        }
        // Client-side, only the selected hotbar item matters
        return context.player().itemInHand(Hand.MAIN_HAND).isEmpty() ? InteractionResult.PASS : InteractionResult.SUCCESS;
    }

    /**
     * Mirrors {@code SelectableSlotContainer#getHitSlot}: slots are numbered left to right, then top to bottom,
     * as seen when looking at the front of the block.
     *
     * @return the hit slot, or -1 if the front of the block was not hit
     */
    private static int hitSlot(@NonNull BlockHit hit, @NonNull Direction facing, int rows, int columns) {
        Direction face = hit.face();
        if (face != facing) {
            return -1;
        }
        // Vanilla takes the location relative to the adjacent block; the axes used here are the same for both
        double relativeX = hit.location().getX() - hit.position().getX();
        double relativeY = hit.location().getY() - hit.position().getY();
        double relativeZ = hit.location().getZ() - hit.position().getZ();
        float horizontal;
        switch (face) {
            case NORTH -> horizontal = (float) (1.0 - relativeX);
            case SOUTH -> horizontal = (float) relativeX;
            case WEST -> horizontal = (float) relativeZ;
            case EAST -> horizontal = (float) (1.0 - relativeZ);
            default -> {
                return -1;
            }
        }
        int row = section(1.0F - (float) relativeY, rows);
        int column = section(horizontal, columns);
        return column + row * columns;
    }

    private static int section(float relativeCoordinate, int sections) {
        float scaledCoordinate = relativeCoordinate * 16.0F;
        float sectionSize = 16.0F / sections;
        return Math.clamp((long) Math.floor(scaledCoordinate / sectionSize), 0, sections - 1);
    }

    private static @NonNull InteractionResult gameMasterBlockUseWithoutItem(@NonNull UseOnContext context, @NonNull BlockState state) {
        return context.player().canUseGameMasterBlocks() ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private static boolean isAxe(@NonNull InteractionContext player, @NonNull GeyserItemStack stack) {
        return player.isInTag(ItemTag.AXES, stack.asItem().javaId());
    }

    private static @NonNull InteractionResult weatheringCopperGolemStatueUseItemOn(@NonNull UseOnContext context, @NonNull BlockState state) {
        if (!isAxe(context.player(), context.stack())) {
            // Honeycomb is left to HoneycombItem#useOn, which waxes the statue
            return context.stack().is(Items.HONEYCOMB) ? InteractionResult.PASS : InteractionResult.SUCCESS;
        }
        // An axe turns an unweathered statue back into a copper golem, which the client predicts as well
        // (CopperGolemStatueBlockEntity#removeStatue also creates the golem on the client)
        return state.is(Blocks.COPPER_GOLEM_STATUE) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private static @NonNull InteractionResult jukeboxUseItemOn(@NonNull UseOnContext context, @NonNull BlockState state) {
        if (state.getValue(Properties.HAS_RECORD)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        // JukeboxPlayable#tryInsertIntoJukebox
        GeyserItemStack stack = context.stack();
        if (!stack.isEmpty() && stack.getComponent(DataComponentTypes.JUKEBOX_PLAYABLE) != null && state.is(Blocks.JUKEBOX)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    private static @NonNull InteractionResult lecternUseItemOn(@NonNull UseOnContext context, @NonNull BlockState state) {
        if (state.getValue(Properties.HAS_BOOK)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        GeyserItemStack stack = context.stack();
        if (context.player().isInTag(ItemTag.LECTERN_BOOKS, stack.asItem().javaId())) {
            // LecternBlock#tryPlaceBook succeeds whenever there is no book yet
            return InteractionResult.SUCCESS;
        }
        return stack.isEmpty() && context.hand() == Hand.MAIN_HAND ? InteractionResult.PASS : InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    private BlockEntityBlockInteractions() {
    }
}
