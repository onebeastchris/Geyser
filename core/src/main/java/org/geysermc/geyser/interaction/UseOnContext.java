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

package org.geysermc.geyser.interaction;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;

/**
 * Mirror of Java's {@code UseOnContext}: a click of a hand, holding an item, on a block.
 * Passed to both block and item interactions.
 *
 * @param player the interacting player
 * @param hand the hand used
 * @param stack the item in that hand at the time of the click. May be empty.
 * @param hit the block that was clicked
 */
public record UseOnContext(@NonNull InteractionContext player, @NonNull Hand hand, @NonNull GeyserItemStack stack, @NonNull BlockHit hit) {

    public Vector3i clickedPos() {
        return hit.position();
    }

    public Direction clickedFace() {
        return hit.face();
    }

    public BlockState clickedState() {
        return player.blockAt(hit.position());
    }

    public boolean isSecondaryUseActive() {
        return player.isSecondaryUseActive();
    }
}
