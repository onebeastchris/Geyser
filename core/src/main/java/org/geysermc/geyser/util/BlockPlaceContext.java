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

package org.geysermc.geyser.util;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.Direction;

@Accessors(fluent = true)
public class BlockPlaceContext {
    // lazy-init
    private BlockState relativeBlock;

    @Getter
    private final Vector3i relativeBlockPosition;

    // Whether the block we clicked can be replaced
    @Getter
    private boolean replacedClicked = true;
    private InteractionContext context;

    public BlockPlaceContext(InteractionContext context) {
        this.relativeBlockPosition = context.interactFace().relative(context.blockPosition());
        this.replacedClicked = context.state().block().canBeReplaced(this);
    }

    public static BlockPlaceContext of(InteractionContext context) {
        return new BlockPlaceContext(context);
    }

    public boolean isSecondaryActive() {
        return context.session().isSneaking();
    }

    private BlockState relativeBlockState() {
        if (relativeBlock == null) {
            return relativeBlock = context.blockStateAt(relativeBlockPosition);
        }
        return relativeBlock;
    }

    public BlockState state() {
        return replacedClicked ? context.state() : relativeBlockState(); // if only used below, maybe state suffices?
    }

    public Item asItem() {
        return state().block().asItem();
    }

    public GeyserItemStack itemInHand() {
        return context.itemInHand();
    }

    public Block block() {
        return state().block();
    }

    public Vector3i blockPosition() {
        return replacedClicked ? context.blockPosition(): relativeBlockPosition;
    }

    public boolean canPlace() {
        return replacedClicked || relativeBlock.block().canBeReplaced(this);
    }

    public Direction interactFace() {
        return context.interactFace();
    }

    public BlockState blockStateAt(Vector3i blockPosition) {
        return context.blockStateAt(blockPosition);
    }
}
