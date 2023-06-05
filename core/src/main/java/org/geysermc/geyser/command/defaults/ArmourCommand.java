/*
 * Copyright (c) 2019-2023 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.command.defaults;

import com.github.steveice10.mc.protocol.data.game.inventory.ContainerType;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.command.GeyserCommand;
import org.geysermc.geyser.command.GeyserCommandSource;
import org.geysermc.geyser.inventory.Inventory;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.inventory.InventoryTranslator;
import org.geysermc.geyser.util.InventoryUtils;

public class ArmourCommand extends GeyserCommand {

    public ArmourCommand(GeyserImpl geyser, String name, String description, String permission) {
        super(name, description, permission);
    }

    @Override
    public void execute(GeyserSession session, GeyserCommandSource sender, String[] args) {
        if (session == null) {
            return;
        }

        InventoryTranslator newTranslator = InventoryTranslator.inventoryTranslator(ContainerType.HOPPER);
        Inventory openInventory = session.getOpenInventory();

        String name = "Armour and Offhand"; //todo lang string

        assert newTranslator != null;
        Inventory newInventory = newTranslator.createInventory(name, InventoryTranslator.ARMOUR_GUI_ID, ContainerType.HOPPER, session.getPlayerInventory());

        if (openInventory != null) {
            // If the window type is the same, don't close.
            // In rare cases, inventories can do funny things where it keeps the same window type up but change the contents.
            if (openInventory.getContainerType() != newInventory.getContainerType()) {
                // Sometimes the server can double-open an inventory with the same ID - don't confirm in that instance.
                InventoryUtils.closeInventory(session, openInventory.getJavaId(), openInventory.getJavaId() != newInventory.getJavaId());
            }
        }

        session.setInventoryTranslator(newTranslator);

        // Armor slots
        newInventory.setItem(0, session.getPlayerInventory().getItem(5), session);
        newInventory.setItem(1, session.getPlayerInventory().getItem(6), session);
        newInventory.setItem(2, session.getPlayerInventory().getItem(7), session);
        newInventory.setItem(3, session.getPlayerInventory().getItem(8), session);

        // Offhand
        newInventory.setItem(4, session.getPlayerInventory().getItem(45), session);
        InventoryUtils.openInventory(session, newInventory);
    }

    @Override
    public boolean isExecutableOnConsole() {
        return false;
    }

    @Override
    public boolean isBedrockOnly() {
        return true;
    }
}
