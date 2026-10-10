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
import org.cloudburstmc.math.vector.Vector3d;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.level.physics.Direction;

/**
 * Mirror of Java's {@code BlockHitResult} for a block that was clicked.
 *
 * @param position the clicked block
 * @param face the clicked face of the block
 * @param location the exact clicked location in world coordinates
 * @param inside whether the player's eyes are inside the clicked block
 */
public record BlockHit(@NonNull Vector3i position, @NonNull Direction face, @NonNull Vector3d location, boolean inside) {

    /**
     * @return the clicked location relative to the block's origin, each axis usually within [0, 1]
     */
    public Vector3d relativeLocation() {
        return location.sub(position.getX(), position.getY(), position.getZ());
    }

    /**
     * @return the position next to the clicked block in the direction of the clicked face
     */
    public Vector3i adjacentPosition() {
        return position.add(face.getUnitVector());
    }
}
