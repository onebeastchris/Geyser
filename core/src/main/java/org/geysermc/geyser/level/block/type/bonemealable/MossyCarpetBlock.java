/*
 * Copyright (c) 2025 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.level.block.type.bonemealable;

import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.level.WorldManager;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.BasicEnumProperty;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.util.InteractionContext;

import java.util.List;
import java.util.Objects;

// TODO test

public class MossyCarpetBlock extends Block implements BoneMealableBlock {

    private static final List<BasicEnumProperty> WALL_PROPERTIES = List.of(
        Properties.NORTH_WALL, Properties.EAST_WALL, Properties.SOUTH_WALL, Properties.WEST_WALL
    );

    public MossyCarpetBlock(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    @Override
    protected BlockState setDefaultState(BlockState firstState) {
        return firstState.withValue(Properties.BOTTOM, true)
            .withValue(Properties.NORTH_WALL, "none")
            .withValue(Properties.EAST_WALL, "none")
            .withValue(Properties.SOUTH_WALL, "none")
            .withValue(Properties.WEST_WALL, "none");
    }

    @Override
    public boolean canSurvive(InteractionContext context) {
        BlockState below = context.belowBlockState();
        return context.state().getValue(Properties.BOTTOM) ?
            !below.isAir() : below.is(this) && below.getValue(Properties.BOTTOM);
    }

    @Override
    public boolean bonemealEffective(InteractionContext context) {
        return context.state().getValue(Properties.BOTTOM) && canCreateMossySides(context);
    }

    private static boolean canCreateMossySides(InteractionContext context) {
        BlockState blockState = context.aboveBlockState();
        boolean isPaleMossCarpet = blockState.is(Blocks.PALE_MOSS_CARPET);
        if ((!isPaleMossCarpet || !(Boolean)blockState.getValue(Properties.BOTTOM)) && (isPaleMossCarpet || blockState.block().canBeReplaced())) {
            BlockState defaultState = Blocks.PALE_MOSS_CARPET.defaultBlockState().withValue(Properties.BOTTOM, false);
            BlockState updatedState = getNewBlockState(defaultState, context.getWorldManager(), context.blockPosition().up(), context.session());

            return hasFaces(updatedState) && updatedState != blockState;
        } else {
            return false;
        }
    }

    private static BlockState getNewBlockState(BlockState blockState, WorldManager manager, Vector3i position, GeyserSession session) {
        BlockState lowSideState = null;
        BlockState blockState3 = null;

        for (Direction direction : Direction.HORIZONTAL) {
            BasicEnumProperty enumProperty = getPropertyForFace(direction);
            String wallSide = canSupportAtFace(direction) ? "low" : "none";
            if (wallSide.equals("low")) {
                if (lowSideState == null) {
                    lowSideState = manager.blockAt(session, position.up());
                }

                if (lowSideState.is(Blocks.PALE_MOSS_CARPET) && !Objects.equals(lowSideState.getValue(enumProperty), "none") && !(Boolean)lowSideState.getValue(Properties.BOTTOM)) {
                    wallSide = "tall";
                }

                if (!(Boolean)blockState.getValue(Properties.BOTTOM)) {
                    if (blockState3 == null) {
                        blockState3 = manager.blockAt(session, position.down());
                    }

                    if (blockState3.is(Blocks.PALE_MOSS_CARPET) && "none".equals(blockState3.getValue(enumProperty))) {
                        wallSide = "none";
                    }
                }
            }

            blockState = blockState.withValue(enumProperty, wallSide);
        }

        return blockState;
    }

    private static boolean hasFaces(BlockState blockState) {
        if (blockState.getValue(Properties.BOTTOM)) {
            return true;
        } else {
            for (BasicEnumProperty enumProperty : WALL_PROPERTIES) {
                if (!Objects.equals(blockState.getValue(enumProperty), "none")) {
                    return true;
                }
            }

            return false;
        }
    }

    public static BasicEnumProperty getPropertyForFace(Direction direction) {
        return switch (direction) {
            case NORTH -> Properties.NORTH_WALL;
            case SOUTH -> Properties.SOUTH_WALL;
            case WEST -> Properties.WEST_WALL;
            case EAST -> Properties.EAST_WALL;
            default -> throw new IllegalStateException("Invalid direction: " + direction);
        };
    }

    private static boolean canSupportAtFace(Direction direction) {
        return direction == Direction.UP ? false : true; // TODO MultifaceBlock#canAttactTo
    }
}
