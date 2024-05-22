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
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.session.GeyserSession;

@Accessors(fluent = true)
@Getter
public class BlockPlaceContext {
    private final GeyserSession session;
    private final BlockState state;
    private final GeyserItemStack itemInHand;
    private final BlockState relativeBlock;
    private final boolean isSecondaryActive;
    private boolean replacedClicked = true;

    public BlockPlaceContext(InteractionContext context) {
        this.session = context.session();
        this.state = context.state();
        this.itemInHand = context.itemInHand();
        this.relativeBlock = context.session().getGeyser().getWorldManager().blockAt(context.session(),
                context.interactFace().relative(context.blockPosition()));
        this.isSecondaryActive = context.session().isSneaking();
        this.replacedClicked = relativeBlock.block().canBeReplaced(this);
    }

    private BlockState getState() {
        return replacedClicked ? state : relativeBlock; // if only used below, maybe state suffices?
    }

    public boolean canPlace() {
        return replacedClicked || getState().block().canBeReplaced(this);
    }

    public static BlockPlaceContext of(InteractionContext context) {
        return new BlockPlaceContext(context);
    }
}
