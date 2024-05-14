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

package org.geysermc.geyser.level.block.type;

import org.cloudburstmc.math.vector.Vector3f;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.session.cache.tags.ItemTag;
import org.geysermc.geyser.util.InteractionContext;
import org.geysermc.geyser.util.InteractionResult;

public class ChiseledBookshelfBlock extends Block {
    public ChiseledBookshelfBlock(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    // TODO: interactions with chiseled bookshelves

    @Override
    public InteractionResult interactWith(InteractionContext context) {
        if (context.is(ItemTag.BOOKSHELF_BOOKS)) {
            // TODO!
            // if invalid hit result: pass
            // full slot: try to remove (ONLY when main hand; useWithoutItem)
            // empty slot: addbook & success
        }

        if (context.mainHand()) {
            // todo: useWithoutItem
            // invalid slot: pass
            // not occupied: consume
            // occupied: remove & success
        }

        return InteractionResult.PASS;
    }

    private int getTargetSlot(GeyserSession session, Vector3f clickPosition, int face) {
        Direction direction = Direction.VALUES[face];
//        if (direction.reversed().ordinal() != facing) {
//            return -1;
//        }

        // TODO test, verify - need to find out if our slot is empty or not
        return 1;
    }
}
