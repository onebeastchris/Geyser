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
                    if (isAffectedSlot(destinationSlot)) {
                        if (canBeAdded(inventory.getItem(destinationSlot), destinationSlot)) {
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
                        if (isAffectedSlot(destinationSlot) && !canBeAdded(destinationItem, destinationSlot)) {
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

    private boolean canBeAdded(GeyserItemStack itemStack, int slot) {
        Item item = itemStack.asItem();
        return switch (slot) {
            case 0 -> item == Items.LEATHER_HELMET
                    || item == Items.CHAINMAIL_HELMET
                    || item == Items.IRON_HELMET
                    || item == Items.GOLDEN_HELMET
                    || item == Items.DIAMOND_HELMET
                    || item == Items.NETHERITE_HELMET
                    || item == Items.TURTLE_HELMET
                    || item == Items.CARVED_PUMPKIN
                    || item == Items.PLAYER_HEAD;
            case 1 -> item == Items.LEATHER_CHESTPLATE
                    || item == Items.CHAINMAIL_CHESTPLATE
                    || item == Items.IRON_CHESTPLATE
                    || item == Items.GOLDEN_CHESTPLATE
                    || item == Items.DIAMOND_CHESTPLATE
                    || item == Items.NETHERITE_CHESTPLATE
                    || item == Items.ELYTRA;
            case 2 -> item == Items.LEATHER_LEGGINGS
                    || item == Items.CHAINMAIL_LEGGINGS
                    || item == Items.IRON_LEGGINGS
                    || item == Items.GOLDEN_LEGGINGS
                    || item == Items.DIAMOND_LEGGINGS
                    || item == Items.NETHERITE_LEGGINGS;
            case 3 -> item == Items.LEATHER_BOOTS
                    || item == Items.CHAINMAIL_BOOTS
                    || item == Items.IRON_BOOTS
                    || item == Items.GOLDEN_BOOTS
                    || item == Items.DIAMOND_BOOTS
                    || item == Items.NETHERITE_BOOTS;
            case 4 -> true; // no limits on offhand... right?
            default -> false;
        };
    }
}
