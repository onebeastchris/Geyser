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

import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.util.BlockPlaceContext;

import java.util.Objects;

public class SlabBlock extends Block {
    public SlabBlock(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    @Override
    public boolean canBeReplaced(BlockPlaceContext context) {
        String slabType = context.state().getValue(Properties.SLAB_TYPE);
        if (Objects.equals(slabType, "double") || !context.itemInHand().is(this.item)) {
            return false;
        } else if (context.replacedClicked()) {
            boolean upper = context.clickPosition().getY() - (double)context.clickPosition().getY() > 0.5;
            Direction direction = context.interactFace();
            return Objects.equals(slabType, "bottom")
                    ? direction == Direction.UP || upper && direction.getAxis().isHorizontal()
                    : direction == Direction.DOWN || !upper && direction.getAxis().isHorizontal();
        } else {
            return true;
        }
    }
}
