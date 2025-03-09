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

package org.geysermc.geyser.inventory;

import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtMap;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.inventory.InventoryTranslator;
import org.geysermc.geyser.translator.protocol.java.inventory.JavaOpenBookTranslator;
import org.geysermc.geyser.util.InventoryUtils;
import org.geysermc.mcprotocollib.protocol.data.game.inventory.ContainerType;

import java.util.Objects;

@Getter
public class LecternContainer extends Container {

    /**
     * Unlike other fake inventories that rely on placing blocks in the world;
     * the virtual lectern workaround for books isn't triggered the same way.
     * Specifically, we don't get a window id - hence, we just use our own!
     */
    public final static int FAKE_LECTERN_WINDOW_ID = -69;

    @Setter
    private int currentBedrockPage = 0;
    @Setter
    private NbtMap blockEntityTag;
    @Setter
    private Vector3i position;

    private boolean isBookInPlayerInventory = false;

    public LecternContainer(String title, int id, int size, ContainerType containerType, PlayerInventory playerInventory) {
        super(title, id, size, containerType, playerInventory);
    }

    /**
     * When the Java server asks the client to open a book in their hotbar, we create a fake lectern to show it to the client.
     * We can't use the {@link #isUsingRealBlock()} check as we may also be dealing with a real virtual lectern (with its own inventory).
     */
    @Override
    public void setItem(int slot, @NonNull GeyserItemStack newItem, GeyserSession session) {
        if (isBookInPlayerInventory) {
            session.getPlayerInventory().setItem(slot, newItem, session);
        } else {
            super.setItem(slot, newItem, session);
        }
    }

    /**
     * This is used ONLY once to set the book of a fake lectern in {@link JavaOpenBookTranslator}.
     * See {@link LecternContainer#setItem(int, GeyserItemStack, GeyserSession)} as for why this is separate.
     */
    public void setFakeLecternBook(GeyserItemStack book, GeyserSession session) {
        this.isBookInPlayerInventory = true;
        super.setItem(0, book, session);
    }

    @Override
    public boolean shouldConfirmContainerClose() {
        return !isBookInPlayerInventory;
    }

    public static void openLecternInventory(GeyserSession session, GeyserItemStack stack) {
        Inventory openInventory = session.getOpenInventory();
        if (openInventory != null) {
            InventoryUtils.closeInventory(session, openInventory.getJavaId(), true);

            InventoryUtils.sendJavaContainerClose(session, openInventory);
        }

        InventoryTranslator translator = InventoryTranslator.inventoryTranslator(ContainerType.LECTERN);
        Objects.requireNonNull(translator, "could not find lectern inventory translator!");
        session.setInventoryTranslator(translator);

        // Should never be null
        Objects.requireNonNull(translator, "lectern translator must exist");
        Inventory inventory = translator.createInventory("", FAKE_LECTERN_WINDOW_ID, ContainerType.LECTERN, session.getPlayerInventory());
        ((LecternContainer) inventory).setFakeLecternBook(stack, session);
        InventoryUtils.openInventory(session, inventory);
    }
}
