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

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.With;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.SoundEvent;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerType;
import org.cloudburstmc.protocol.bedrock.packet.ContainerOpenPacket;
import org.cloudburstmc.protocol.bedrock.packet.LevelSoundEventPacket;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.geyser.level.WorldManager;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.session.cache.tags.Tag;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;

@With
@AllArgsConstructor
@RequiredArgsConstructor
public class InteractionContext {
    private final GeyserSession session;
    private final Vector3i blockPosition;
    private final Vector3f clickPosition;
    private final int blockFace;
    private final Hand hand;
    private final boolean spectator;

    public GeyserSession session() {
        return session;
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

    public boolean isSpectator() {
        return spectator;
    }

    public boolean mayBuild() {
        return session.getGameMode() == GameMode.SURVIVAL || session.getGameMode() == GameMode.CREATIVE;
    }

    private Block block;
    private BlockState state;
    private Direction direction;
    private BlockState below;
    private BlockState above;

    public static InteractionContext of(GeyserSession session, Vector3i blockPosition,
                                        Vector3f clickPosition, int clickFace, Hand hand) {
        return new InteractionContext(session, blockPosition, clickPosition, clickFace, hand, session.getGameMode() == GameMode.SPECTATOR);
    }

    public WorldManager getWorldManager() {
        return session.getGeyser().getWorldManager();
    }

    public BlockState state() {
        if (state == null) {
            return state = session.getGeyser().getWorldManager().blockAt(session, blockPosition);
        }
        return state;
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

    public boolean isBlockTag(Tag<Block> tag) {
        return session.getTagCache().is(tag, block());
    }

    public boolean isBlockTag(Tag<Block> tag, Block block) {
        return session.getTagCache().is(tag, block);
    }

    public boolean isItemTag(Tag<Item> tag) {
        return session.getTagCache().is(tag, itemInHand());
    }

    public boolean isItemTag(Tag<Item> tag, Item item) {
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

    public boolean canPlaceOnBlockInAdventureMode(GeyserItemStack stack) {
        var predicate = stack.getComponent(DataComponentTypes.CAN_PLACE_ON);
        if (predicate != null) {
            // TODO implement
            return true;
        }
        return false;
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

    public boolean shouldUpdateClient() {
        return this.hand == Hand.OFF_HAND;
    }

    public boolean lookingAtBlock() {
        return this.blockPosition != Vector3i.ZERO;
    }

    public void updateHeldItem(GeyserItemStack stack) {
        // uhhhhhhhhhhhhhhhhhhhhhhhhhhhhh

    }

    public void openContainer(ContainerType containerType) {
        ContainerOpenPacket openPacket = new ContainerOpenPacket();
        openPacket.setBlockPosition(blockPosition);
        openPacket.setId((byte) 1);
        openPacket.setType(containerType);
        openPacket.setUniqueEntityId(-1);
        session.sendUpstreamPacket(openPacket);
    }

    public void sendLevelSoundEventPacket(SoundEvent soundEvent, int newJavaState) {
        int extraData = newJavaState == -1 ? -1 : session.getBlockMappings().getBedrockBlockId(newJavaState);

        LevelSoundEventPacket packet = new LevelSoundEventPacket();
        packet.setPosition(blockPosition.toFloat());
        packet.setBabySound(false);
        packet.setRelativeVolumeDisabled(false);
        packet.setIdentifier(":");
        packet.setSound(soundEvent);
        packet.setExtraData(extraData);
        session.sendUpstreamPacket(packet);
    }

    public void sendLevelSoundEventPacket(SoundEvent soundEvent) {
        sendLevelSoundEventPacket(soundEvent, -1);
    }

    public void updateBlock(BlockState state) {
        updateBlock(state, blockPosition);
    }

    public void updateBlock(BlockState state, Vector3i blockPosition) {
        ChunkUtils.updateBlock(session, state, blockPosition);
    }
}
