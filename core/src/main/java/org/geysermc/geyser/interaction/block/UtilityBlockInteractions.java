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
import org.geysermc.geyser.interaction.InteractionRegistry;
import org.geysermc.geyser.interaction.Interactions;
import org.geysermc.geyser.interaction.UseOnContext;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.Axis;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.session.cache.tags.ItemTag;
import org.geysermc.geyser.util.InteractionResult;

/**
 * Containers, workstations and redstone components: blocks whose client-side interaction mostly depends on block state only.
 */
public final class UtilityBlockInteractions {
    /**
     * Vanilla classes whose {@code useWithoutItem} always succeeds on the client: menus are opened by the server
     * (chests, ender chests and shulker boxes check for blocked lids and sitting cats on the server only), or the
     * block changes unconditionally.
     */
    private static final String[] ALWAYS_SUCCESS = {
        "AbstractFurnaceBlock", "AnvilBlock", "BarrelBlock", "BeaconBlock", "BrewingStandBlock", "CartographyTableBlock",
        "ChestBlock", "CrafterBlock", "CraftingTableBlock", "DispenserBlock", "EnchantingTableBlock", "EnderChestBlock",
        "GrindstoneBlock", "HopperBlock", "LoomBlock", "ShulkerBoxBlock", "SmithingTableBlock", "StonecutterBlock",
        // Toggled without any condition
        "FenceGateBlock", "LeverBlock",
        // Tuned on the server
        "NoteBlock",
        // Only spawns particles on the client, even when no teleport destination is found
        "DragonEggBlock"
    };

    private static final Interactions.BlockUseWithoutItem SUCCESS = (context, state) -> InteractionResult.SUCCESS;
    private static final Interactions.BlockUseWithoutItem PASS = (context, state) -> InteractionResult.PASS;
    private static final Interactions.BlockUseWithoutItem SUCCESS_IF_MAY_BUILD =
        (context, state) -> context.player().mayBuild() ? InteractionResult.SUCCESS : InteractionResult.PASS;

    public static void register(InteractionRegistry.Registrar registrar) {
        for (String vanillaClass : ALWAYS_SUCCESS) {
            registrar.blockUseWithoutItem(vanillaClass, SUCCESS);
        }

        // Sleeping (and exploding) is handled by the server
        registrar.blockUseWithoutItem("BedBlock", (context, state) -> InteractionResult.SUCCESS_SERVER);
        registrar.blockUseWithoutItem("BellBlock", UtilityBlockInteractions::bellUseWithoutItem);
        // A pressed button consumes the click without swinging
        registrar.blockUseWithoutItem("ButtonBlock",
            (context, state) -> state.getValue(Properties.POWERED, false) ? InteractionResult.CONSUME : InteractionResult.SUCCESS);
        // Iron is the only block set type of doors and trapdoors that cannot be opened by hand
        registrar.blockUseWithoutItem("DoorBlock",
            (context, state) -> state.is(Blocks.IRON_DOOR) ? InteractionResult.PASS : InteractionResult.SUCCESS);
        registrar.blockUseWithoutItem("TrapDoorBlock",
            (context, state) -> state.is(Blocks.IRON_TRAPDOOR) ? InteractionResult.PASS : InteractionResult.SUCCESS);

        // Only players that may build can change these
        registrar.blockUseWithoutItem("ComparatorBlock", SUCCESS_IF_MAY_BUILD);
        registrar.blockUseWithoutItem("RepeaterBlock", SUCCESS_IF_MAY_BUILD);
        registrar.blockUseWithoutItem("DaylightDetectorBlock", SUCCESS_IF_MAY_BUILD);
        registrar.blockUseWithoutItem("RedStoneWireBlock", UtilityRedstoneWire::useWithoutItem);

        // Leashing mobs to a fence only happens on the server; the client moves on to the item (see LeadItem)
        registrar.blockUseWithoutItem("FenceBlock", PASS);
        // Removing a moving piston without a block entity only happens on the server
        registrar.blockUseWithoutItem("MovingPistonBlock", PASS);

        registrar.blockUseItemOn("NoteBlock", UtilityBlockInteractions::noteBlockUseItemOn);
    }

    /**
     * Mob heads used on top of a note block skip the note block, so that the head is placed instead.
     */
    private static @NonNull InteractionResult noteBlockUseItemOn(@NonNull UseOnContext context, @NonNull BlockState state) {
        if (context.clickedFace() == Direction.UP
            && context.player().isInTag(ItemTag.NOTEBLOCK_TOP_INSTRUMENTS, context.stack().asItem().javaId())) {
            return InteractionResult.PASS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    /**
     * The bell rings on the server, but the client checks whether the bell itself (not its frame) was hit.
     */
    private static @NonNull InteractionResult bellUseWithoutItem(@NonNull UseOnContext context, @NonNull BlockState state) {
        Direction face = context.clickedFace();
        double clickY = context.hit().location().getY() - context.clickedPos().getY();
        // Vanilla compares against a float constant
        if (face.getAxis() == Axis.Y || clickY > 0.8124F) {
            return InteractionResult.PASS;
        }

        Direction facing = state.getValue(Properties.HORIZONTAL_FACING);
        String attachment = state.getValue(Properties.BELL_ATTACHMENT);
        if (facing == null || attachment == null) {
            return InteractionResult.PASS;
        }
        boolean properHit = switch (attachment) {
            case "floor" -> facing.getAxis() == face.getAxis();
            case "single_wall", "double_wall" -> facing.getAxis() != face.getAxis();
            case "ceiling" -> true;
            default -> false;
        };
        return properHit ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private UtilityBlockInteractions() {
    }
}
