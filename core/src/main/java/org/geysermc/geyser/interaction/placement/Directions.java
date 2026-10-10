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

package org.geysermc.geyser.interaction.placement;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.geyser.level.physics.Direction;

/**
 * Direction helpers mirroring {@code net.minecraft.core.Direction}.
 */
public final class Directions {
    private static final Direction[] BY_2D_DATA = {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};

    /**
     * Mirrors {@code Direction#fromYRot}.
     */
    public static @NonNull Direction fromYRot(double yaw) {
        return BY_2D_DATA[(int) Math.floor(yaw / 90.0 + 0.5) & 3];
    }

    /**
     * Mirrors {@code Direction#orderedByNearest}: all six directions, ordered by how directly the view vector of the
     * given rotation points at them.
     */
    public static @NonNull Direction @NonNull [] orderedByNearest(float yaw, float pitch) {
        float pitchRadians = pitch * (float) (Math.PI / 180.0);
        float yawRadians = -yaw * (float) (Math.PI / 180.0);
        float pitchSin = (float) Math.sin(pitchRadians);
        float pitchCos = (float) Math.cos(pitchRadians);
        float yawSin = (float) Math.sin(yawRadians);
        float yawCos = (float) Math.cos(yawRadians);
        boolean east = yawSin > 0.0F;
        boolean up = pitchSin < 0.0F;
        boolean south = yawCos > 0.0F;
        float x = east ? yawSin : -yawSin;
        float y = up ? -pitchSin : pitchSin;
        float z = south ? yawCos : -yawCos;
        float horizontalX = x * pitchCos;
        float horizontalZ = z * pitchCos;
        Direction xDirection = east ? Direction.EAST : Direction.WEST;
        Direction yDirection = up ? Direction.UP : Direction.DOWN;
        Direction zDirection = south ? Direction.SOUTH : Direction.NORTH;
        if (x > z) {
            if (y > horizontalX) {
                return makeDirectionArray(yDirection, xDirection, zDirection);
            }
            return horizontalZ > y ? makeDirectionArray(xDirection, zDirection, yDirection) : makeDirectionArray(xDirection, yDirection, zDirection);
        }
        if (y > horizontalZ) {
            return makeDirectionArray(yDirection, zDirection, xDirection);
        }
        return horizontalX > y ? makeDirectionArray(zDirection, xDirection, yDirection) : makeDirectionArray(zDirection, yDirection, xDirection);
    }

    private static Direction[] makeDirectionArray(Direction first, Direction second, Direction third) {
        return new Direction[] {first, second, third, third.reversed(), second.reversed(), first.reversed()};
    }

    private Directions() {
    }
}
