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

import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.SoundEvent;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerType;
import org.cloudburstmc.protocol.bedrock.packet.ContainerOpenPacket;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.geyser.level.WorldManager;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.session.cache.tags.Tag;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;

public record InteractionContext(
        GeyserSession session,
        Vector3i blockPosition,
        Vector3f clickPosition,
        int blockFace,
        Hand hand,
        BlockState state
) {
    public static InteractionContext of(GeyserSession session, Vector3i blockPosition,
                                        Vector3f clickPosition, int clickFace, Hand hand) {
        BlockState state = session.getGeyser().getWorldManager().blockAt(session, blockPosition);
        return new InteractionContext(session, blockPosition, clickPosition, clickFace, hand, state);
    }

    public WorldManager getWorldManager() {
        return session.getGeyser().getWorldManager();
    }

    public BlockState aboveBlockState() {
        return getWorldManager().blockAt(session, blockPosition().up());
    }

    public BlockState belowBlockState() {
        return session.getGeyser().getWorldManager().blockAt(session, blockPosition.down());
    }

    public GeyserItemStack itemInHand() {
        return session.getPlayerInventory().getItemInHand(hand);
    }

    public boolean isBlock(Tag<Block> tag) {
        return session.getTagCache().is(tag, state.block());
    }

    public boolean is(Tag<Item> tag) {
        return session.getTagCache().is(tag, itemInHand());
    }

    public void playSound(SoundEvent event) {
        session.playSound(event, blockPosition.toFloat());
    }

    public boolean isMainHand() {
        return hand == Hand.MAIN_HAND;
    }

    public GeyserItemStack offHand() {
        return session.getPlayerInventory().getItemInHand(Hand.OFF_HAND);
    }

    public GeyserItemStack mainHand() {
        return session.getPlayerInventory().getItemInHand(Hand.MAIN_HAND);
    }

    public boolean isSneaking() {
        return session.isSneaking();
    }

    public Block block() {
        return state.block();
    }

    public Direction interactFace() {
        return Direction.values()[blockFace];
    }

    public BlockState relativeBlockState() {
        return session.getGeyser().getWorldManager().blockAt(session, interactFace().relative(blockPosition));
    }

    // TODO is this still needed?
    public void openContainer(ContainerType containerType) {
        ContainerOpenPacket openPacket = new ContainerOpenPacket();
        openPacket.setBlockPosition(blockPosition);
        openPacket.setId((byte) 1);
        openPacket.setType(containerType);
        openPacket.setUniqueEntityId(-1);
        session.sendUpstreamPacket(openPacket);
    }
}
