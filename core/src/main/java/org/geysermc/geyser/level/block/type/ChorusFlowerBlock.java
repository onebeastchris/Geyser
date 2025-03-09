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

package org.geysermc.geyser.level.block.type;

import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.util.InteractionContext;

public class ChorusFlowerBlock extends Block {
    public ChorusFlowerBlock(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    @Override
    public boolean canSurvive(InteractionContext context) {
        BlockState below = context.belowBlockState();
        if (!below.is(Blocks.CHORUS_PLANT) && !below.is(Blocks.END_STONE)) {
            if (!below.isAir()) {
                return false;
            } else {
                boolean foundOne = false;

                for (Direction direction : Direction.HORIZONTAL) {
                    BlockState relative = context.getWorldManager().blockAt(context.session(), direction.relative(context.blockPosition()));
                    if (relative.is(Blocks.CHORUS_PLANT)) {
                        if (foundOne) {
                            return false;
                        }
                        foundOne = true;
                    } else if (!relative.isAir()) {
                        return false;
                    }
                }

                return foundOne;
            }
        }

        return true;
    }
}
