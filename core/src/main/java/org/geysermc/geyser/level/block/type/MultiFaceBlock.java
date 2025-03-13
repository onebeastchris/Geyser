/*
 * Copyright (c) 2024 GeyserMC. http://geysermc.org
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

import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.BooleanProperty;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.util.BlockPlaceContext;
import org.geysermc.geyser.util.InteractionContext;

import java.util.Arrays;

public abstract class MultiFaceBlock extends Block {

    public MultiFaceBlock(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    public boolean canReplace(BlockPlaceContext context) {
        return Arrays.stream(Direction.values()).anyMatch(direction -> !hasFace(direction, context.state()));
    }

    private boolean hasFace(Direction direction, BlockState state) {
        BooleanProperty property = switch (direction) {
            case DOWN -> Properties.DOWN;
            case UP -> Properties.UP;
            case NORTH -> Properties.NORTH;
            case SOUTH -> Properties.SOUTH;
            case WEST -> Properties.WEST;
            case EAST -> Properties.EAST;
        };

        return state.getValue(property, false);
    }

    protected boolean canSpreadInAnyDirection(InteractionContext context, Direction opposed) {
        return Arrays.stream(Direction.values()).anyMatch(dir -> getSpreadFromFaceTowardDirection(context, opposed, dir));
    }

    private boolean getSpreadFromFaceTowardDirection(InteractionContext context, Direction direction1, Direction direction2) {
        if (direction1.getAxis() == direction2.getAxis()) {
            return false;
        }

        BlockState state = context.state();
        Vector3i position = context.blockPosition();

        if (otherBlockValidSource(context.state()) || (hasFace(direction1, state) && !hasFace(direction2, state))) {

            // Same position & direction
            if (canSpreadInto(context, position, direction2)) {
                return true;
            }

            // Same plane
            if (canSpreadInto(context, direction2.relative(position), direction1)) {
                return true;
            }

            // Wrap around
            return canSpreadInto(context, direction2.relative(direction1.relative(position)), direction2.reversed());
        }

        return false;
    }

    private boolean canSpreadInto(InteractionContext context, Vector3i pos, Direction direction) {
        BlockState state = context.getWorldManager().blockAt(context.session(), pos);
        return replaceableState(state) && isValidStateForPlacement(state, direction, pos, context);
    }

    private boolean replaceableState(BlockState state) { // todo not lichen specific
        return state.isAir() || state.is(Blocks.GLOW_LICHEN) || state.is(Blocks.WATER) && state.getValue(Properties.LEVEL) == 15;
    }

    private boolean isValidStateForPlacement(BlockState state, Direction direction, Vector3i pos, InteractionContext context) {
        if ((!state.is(this)) || !hasFace(direction, state)) {
            Vector3i relative = direction.relative(pos);
            return canAttachTo();
        }
        return false;
    }

    private boolean canAttachTo() {
        return true; // TODO
    }

    protected abstract boolean otherBlockValidSource(BlockState state);
}
