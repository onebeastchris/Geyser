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

import lombok.RequiredArgsConstructor;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.SoundEvent;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerType;
import org.cloudburstmc.protocol.bedrock.packet.ContainerOpenPacket;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.geyser.level.WorldManager;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.session.cache.tags.Tag;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;

@RequiredArgsConstructor
public class InteractionContext {
    private final GeyserSession session;
    private final Vector3i blockPosition;
    private final Vector3f clickPosition;
    private final int blockFace;
    private final Hand hand;
    private final BlockState state;

    public GeyserSession session() {
        return session;
    }

    public BlockState state() {
        return state;
    }

    public Hand hand() {
        return hand;
    }

    public int blockFace() {
        return blockFace;
    }

    public Vector3f clickPosition() {
        return clickPosition;
    }

    public Vector3i blockPosition() {
        return blockPosition;
    }

    private Block block;
    private Direction direction;
    private BlockState below;
    private BlockState above;

    public static InteractionContext of(GeyserSession session, Vector3i blockPosition,
                                        Vector3f clickPosition, int clickFace, Hand hand) {
        BlockState state = session.getGeyser().getWorldManager().blockAt(session, blockPosition);
        return new InteractionContext(session, blockPosition, clickPosition, clickFace, hand, state);
    }

    public WorldManager getWorldManager() {
        return session.getGeyser().getWorldManager();
    }

    public BlockState aboveBlockState() {
        if (above == null) {
            return above = getWorldManager().blockAt(session, blockPosition.up());
        }
        return above;
    }

    public BlockState aboveBlockState(Vector3i position) {
        return getWorldManager().blockAt(session, position.up());
    }

    public BlockState belowBlockState() {
        if (below == null) {
            return session.getGeyser().getWorldManager().blockAt(session, blockPosition.down());
        }
        return below;
    }

    public BlockState belowBlockState(Vector3i position) {
        return getWorldManager().blockAt(session, position.down());
    }

    public GeyserItemStack itemInHand() {
        return session.getPlayerInventory().getItemInHand(hand);
    }

    public boolean isBlock(Tag<Block> tag) {
        return session.getTagCache().is(tag, block());
    }

    public boolean isBlock(Tag<Block> tag, Block block) {
        return session.getTagCache().is(tag, block);
    }

    public boolean isItem(Tag<Item> tag) {
        return session.getTagCache().is(tag, itemInHand());
    }

    public boolean isItem(Tag<Item> tag, Item item) {
        return session.getTagCache().is(tag, item);
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

    public Block block() {
        if (block == null) {
            return block = state.block();
        }
        return block;
    }

    public Direction interactFace() {
        if (direction == null) {
            return direction = Direction.values()[blockFace];
        }
        return direction;
    }

    public BlockState relativeBlockState() {
        return session.getGeyser().getWorldManager().blockAt(session, interactFace().relative(blockPosition));
    }

    public boolean intendsToUseShield() {
        return isMainHand() && offHand().is(Items.SHIELD) && !session.isSneaking();
    }

    public void openContainer(ContainerType containerType) {
        ContainerOpenPacket openPacket = new ContainerOpenPacket();
        openPacket.setBlockPosition(blockPosition);
        openPacket.setId((byte) 1);
        openPacket.setType(containerType);
        openPacket.setUniqueEntityId(-1);
        session.sendUpstreamPacket(openPacket);
    }
}
