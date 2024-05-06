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

package org.geysermc.geyser.registry.type.block;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.level.physics.Axis;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.level.physics.PistonBehavior;
import org.geysermc.geyser.registry.type.BlockMapping;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.util.InteractResult;

import java.util.Locale;

public class BellBlock extends BlockMapping {
    private final AttachmentType attachmentType;
    private final Direction direction;

    public BellBlock(String javaIdentifier, int javaBlockId, float hardness, boolean canBreakWithHand, int collisionIndex, @Nullable String pickItem, @NonNull PistonBehavior pistonBehavior, boolean isBlockEntity, InteractResult defaultInteractResult) {
        super(javaIdentifier, javaBlockId, hardness, canBreakWithHand, collisionIndex, pickItem, pistonBehavior, isBlockEntity, defaultInteractResult);
        this.attachmentType = AttachmentType.valueOf(parseStringProperty("attachment").toUpperCase(Locale.ROOT));
        this.direction = Direction.valueOf(parseStringProperty("facing").toUpperCase(Locale.ROOT));
    }

    @Override
    public InteractResult interactWith(GeyserSession session, Vector3i blockPosition, Vector3f clickPosition, int face, boolean isMainHand) {
        Direction interactFace = Direction.VALUES[face];
        if (interactFace.getAxis() == Axis.Y) {
            // Java does not allow you to ring a bell up or down. Huh.
            return InteractResult.PASS;
        }
        if (clickPosition.getY() <= 0.8124f) { // Too high? Nah. TODO reset Bedrock since it thinks it goes through and rings the bell
            switch (attachmentType) {
                case FLOOR -> {
                    if (interactFace.getAxis() == direction.getAxis()) {
                        return InteractResult.SUCCESS;
                    }
                }
                case SINGLE_WALL, DOUBLE_WALL -> {
                    if (interactFace.getAxis() != direction.getAxis()) {
                        return InteractResult.SUCCESS;
                    }
                }
                case CEILING -> {
                    return InteractResult.SUCCESS;
                }
            }
        }
        return InteractResult.PASS;
    }

    enum AttachmentType {
        CEILING,
        DOUBLE_WALL,
        FLOOR,
        SINGLE_WALL
    }
}
