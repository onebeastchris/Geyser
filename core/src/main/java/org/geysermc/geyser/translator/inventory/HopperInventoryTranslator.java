/*
 * Copyright (c) 2019-2022 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.translator.inventory;

import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerSlotType;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerType;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.ItemStackRequest;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.action.DropAction;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.action.ItemStackRequestAction;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.action.PlaceAction;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.action.SwapAction;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.action.TakeAction;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.response.ItemStackResponse;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.inventory.BedrockContainerSlot;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.inventory.Inventory;
import org.geysermc.geyser.inventory.updater.ContainerInventoryUpdater;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.geyser.session.GeyserSession;

/**
 * Implemented on top of any block that does not have special properties implemented
 */
public class HopperInventoryTranslator extends AbstractBlockInventoryTranslator {
    public HopperInventoryTranslator() {
        super(5, "minecraft:hopper[enabled=false,facing=down]", ContainerType.HOPPER, ContainerInventoryUpdater.INSTANCE);
    }

    @Override
    public BedrockContainerSlot javaSlotToBedrockContainer(int javaSlot) {
        if (javaSlot < this.size) {
            return new BedrockContainerSlot(ContainerSlotType.LEVEL_ENTITY, javaSlot);
        }
        return super.javaSlotToBedrockContainer(javaSlot);
    }

    @Override
    public boolean shouldHandleRequestFirst(ItemStackRequestAction action, Inventory inventory) {
        return inventory.getJavaId() == InventoryTranslator.ARMOUR_GUI_ID;
    }

    @Override
    public ItemStackResponse translateSpecialRequest(GeyserSession session, Inventory inventory, ItemStackRequest request) {
        ItemStackRequestAction[] requests = request.getActions();
        for (ItemStackRequestAction requestAction : requests) {
            switch (requestAction.getType()) {
                case TAKE: {
                    TakeAction takeAction = (TakeAction) requestAction;
                    GeyserImpl.getInstance().getLogger().info("HopperInventoryTranslator: translateSpecialRequest: TAKE");
                    int slot = takeAction.getSource().getSlot();
                    if (isAffectedSlot(slot)) {
                        if (!canBeRemoved(inventory.getItem(slot))) {
                            return InventoryTranslator.rejectRequest(request, false);
                        }
                        // todo: unequip the item
                    }
                    return super.translateRequest(session, inventory, request);
                }
                case PLACE: {
                    PlaceAction placeAction = (PlaceAction) requestAction;
                    GeyserImpl.getInstance().getLogger().info("HopperInventoryTranslator: translateSpecialRequest: PLACE");
                    int destinationSlot = placeAction.getDestination().getSlot();
                    int sourceSlot = placeAction.getSource().getSlot();

                    if (isAffectedSlot(destinationSlot)) {
                        if (isCorrectItemForSlot(session, inventory.getItem(sourceSlot), destinationSlot)) {
                            // todo: equip the item
                        } else {
                            return InventoryTranslator.rejectRequest(request, false);
                        }
                    }
                    return super.translateRequest(session, inventory, request);
                }
                case SWAP:
                    SwapAction swapAction = (SwapAction) requestAction;
                    GeyserImpl.getInstance().getLogger().info("HopperInventoryTranslator: translateSpecialRequest: SWAP");
                    int sourceSlot = swapAction.getSource().getSlot();
                    int destinationSlot = swapAction.getDestination().getSlot();
                    if (isAffectedSlot(sourceSlot) || isAffectedSlot(destinationSlot)) {

                        GeyserItemStack sourceItem = inventory.getItem(sourceSlot);
                        if (isAffectedSlot(sourceSlot) && !canBeRemoved(sourceItem)) {
                            return InventoryTranslator.rejectRequest(request, false);
                        }

                        GeyserItemStack destinationItem = inventory.getItem(destinationSlot);
                        if (isAffectedSlot(destinationSlot) && !isCorrectItemForSlot(session,  destinationItem, destinationSlot)) {
                            return InventoryTranslator.rejectRequest(request, false);
                        }
                        // todo: unequip or equip the item
                    }
                    return super.translateRequest(session, inventory, request);
                case DROP:
                    DropAction dropAction = (DropAction) requestAction;
                    GeyserImpl.getInstance().getLogger().info("HopperInventoryTranslator: translateSpecialRequest: DROP");
                    int slot = dropAction.getSource().getSlot();
                    if (isAffectedSlot(dropAction.getSource().getSlot())) {
                        if (!canBeRemoved(inventory.getItem(slot))) {
                            return InventoryTranslator.rejectRequest(request, false);
                        }
                        // todo: unequip the item
                    }
                    return super.translateRequest(session, inventory, request);
                default:
                    return super.translateRequest(session, inventory, request);
            }
        }
        return super.translateRequest(session, inventory, request);
    }

    private boolean isAffectedSlot(int slot) {
        return 0 <= slot && slot <= 4;
    }

    private boolean canBeRemoved(GeyserItemStack item) {
        if (item.getNbt() != null) {
            return !item.getNbt().contains("binding_curse");
        }
        return true;
    }

    private boolean isCorrectItemForSlot(GeyserSession session, GeyserItemStack itemStack, int slot) {
        Item item = itemStack.asItem();

        GeyserImpl.getInstance().getLogger().error("MAPPING for " + itemStack.asItem().javaIdentifier() + "towards slot " + slot +
                " : " + itemStack.getMapping(session).isWearable() + " " + itemStack.getMapping(session).getArmorType());

        // Only allow armor in the armor slots
        if (!itemStack.getMapping(session).isWearable() && slot != 4) {
            return false;
        }

        // TODO: offhand
        return switch (slot) {
            case 0 -> itemStack.getMapping(session).getArmorType().equals("helmet") || itemStack.asItem() == Items.PLAYER_HEAD || itemStack.asItem() == Items.CARVED_PUMPKIN;
            case 1 -> itemStack.getMapping(session).getArmorType().equals("chestplate") || itemStack.asItem() == Items.ELYTRA;
            case 2 -> itemStack.getMapping(session).getArmorType().equals("leggings");
            case 3 -> itemStack.getMapping(session).getArmorType().equals("boots");
            case 4 -> true; // TODO: is offhand restricted?
            default -> false;
        };
    }
}
