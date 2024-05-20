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
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.physics.Axis;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.util.InteractionResult;

public class BellBlock extends Block {

    public BellBlock(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    @Override
    public InteractionResult interactWith(GeyserSession session, Vector3i blockPosition, Vector3f clickPosition, int face, boolean isMainHand, BlockState blockState) {
        if (!isMainHand) {
            return InteractionResult.PASS; // Only main hand can ring bell
        }
        Direction interactFace = Direction.VALUES[face];
        if (interactFace.getAxis() == Axis.Y) {
            // Java does not allow you to ring a bell up or down. Huh.
            return InteractionResult.PASS;
        }
        if (clickPosition.getY() <= 0.8124f) { // Too high? Nah. TODO reset Bedrock since it thinks it goes through and rings the bell
            Direction direction = blockState.getValue(Properties.HORIZONTAL_FACING);
            switch (blockState.getValue(Properties.BELL_ATTACHMENT)) {
                case "floor" -> {
                    if (interactFace.getAxis() == direction.getAxis()) {
                        return InteractionResult.SUCCESS;
                    }
                }
                case "single_wall", "double_wall" -> {
                    if (interactFace.getAxis() != direction.getAxis()) {
                        return InteractionResult.SUCCESS;
                    }
                }
                case "ceiling" -> {
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }
}
