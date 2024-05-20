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

import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtMap;
import org.geysermc.erosion.util.LecternUtils;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.level.WorldManager;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.util.BlockEntityUtils;
import org.geysermc.geyser.util.InteractionResult;

public class LecternBlock extends Block {
    public LecternBlock(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    @Override
    protected void handleLecternBlockUpdate(GeyserSession session, BlockState state, Vector3i position) {
        WorldManager worldManager = session.getGeyser().getWorldManager();
        if (worldManager.shouldExpectLecternHandled(session)) {
            worldManager.sendLecternData(session, position.getX(), position.getY(), position.getZ());
            return;
        }

        boolean currentHasBook = state.getValue(Properties.HAS_BOOK);
        Boolean previousHasBook = worldManager.blockAt(session, position).getValue(Properties.HAS_BOOK); // Can be null if not a lectern, watch out
        if (currentHasBook != previousHasBook) {
            if (currentHasBook) {
                worldManager.sendLecternData(session, position.getX(), position.getY(), position.getZ());
            } else {
                session.getLecternCache().remove(position);
                NbtMap newLecternTag = LecternUtils.getBaseLecternTag(position.getX(), position.getY(), position.getZ(), 0).build();
                BlockEntityUtils.updateBlockEntity(session, newLecternTag, position);
            }
        }
    }

    @Override
    public InteractionResult interactWith(GeyserSession session, Vector3i blockPosition, Vector3f clickPosition, int face, boolean isMainHand, BlockState state) {
        if (state.getValue(Properties.HAS_BOOK)) {
            return isMainHand ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        GeyserItemStack itemInHand = session.getPlayerInventory().getItemInHand(isMainHand);
        if (itemInHand.asItem().equals(Items.WRITTEN_BOOK) || itemInHand.asItem().equals(Items.WRITABLE_BOOK)) {
            return InteractionResult.SUCCESS;
        }

        return itemInHand.isEmpty() && isMainHand ?
                InteractionResult.PASS : InteractionResult.CONSUME;
    }
}
