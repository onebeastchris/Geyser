/*
 * Copyright (c) 2019-2024 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.registry.type.block;

import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.level.physics.PistonBehavior;
import org.geysermc.geyser.registry.type.BlockMapping;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.session.cache.tags.ItemTag;
import org.geysermc.geyser.util.InteractResult;

public class ChiseledBookshelfBlock extends BlockMapping {

    // TODO: interactions with chiseled bookshelves
    private final int[] slots = new int[6];
    private final int facing;

    public ChiseledBookshelfBlock(String javaIdentifier, int javaBlockId, float hardness, boolean canBreakWithHand, int collisionIndex, String pickItem, PistonBehavior pistonBehavior, boolean isBlockEntity, InteractResult defaultInteractResult) {
        super(javaIdentifier, javaBlockId, hardness, canBreakWithHand, collisionIndex, pickItem, pistonBehavior, isBlockEntity, defaultInteractResult);

        for (int i = 0; i < slots.length; i++) {
            slots[i] = parseBooleanProperty("slot_" + i + "_occupied") ? 1 : 0;
        }

        facing = switch (parseStringProperty("facing")) {
            case "east" -> 0;
            case "south" -> 1;
            case "west" -> 2;
            case "north" -> 3;
            default -> throw new RuntimeException("Could not assign state for chiseled bookshelf! " + parseStringProperty("facing"))
        };
    }

    @Override
    public InteractResult interactWith(GeyserSession session, Vector3i blockPosition, Vector3f clickPosition, int face, boolean isMainHand) {
        GeyserItemStack itemStack = session.getPlayerInventory().getItemInHand(isMainHand);
        if (session.getTagCache().is(ItemTag.BOOKSHELF_BOOKS, itemStack)) {
            // TODO!
        }

        return InteractResult.PASS;
    }

    private int getTargetSlot(GeyserSession session, Vector3f clickPosition, int face) {
        Direction direction = Direction.VALUES[face];
        if (direction.reversed().ordinal() != facing) {
            return -1;
        }

        // TODO test, verify - need to find out if our slot is empty or not
        return 1;
    }
}
