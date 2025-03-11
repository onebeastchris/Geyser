/*
 * Copyright (c) 2024-2025 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.level.block.type.interactive;

import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.SoundEvent;
import org.cloudburstmc.protocol.bedrock.packet.LevelSoundEventPacket;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.level.physics.SupportType;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.util.ChunkUtils;
import org.geysermc.geyser.util.InteractionContext;
import org.geysermc.geyser.util.InteractionResult;

public class DoorBlock extends Block {
    public DoorBlock(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    @Override
    public void updateBlock(GeyserSession session, BlockState state, Vector3i position) {
        // Needed to check whether we must force the client to update the door state.
        String doubleBlockHalf = state.getValue(Properties.DOUBLE_BLOCK_HALF);

        if (!session.getGeyser().getWorldManager().hasOwnChunkCache() && doubleBlockHalf.equals("lower")) {
            BlockState oldBlockState = session.getGeyser().getWorldManager().blockAt(session, position);
            // If these are the same, it means that we already updated the lower door block (manually in the workaround below),
            // and we do not need to update the block in the cache/on the client side using the super.updateBlock() method again.
            // Otherwise, we send the door updates twice which will cause visual glitches on the client side
            if (oldBlockState == state) {
                return;
            }
        }

        super.updateBlock(session, state, position);

        if (doubleBlockHalf.equals("upper")) {
            // Update the lower door block as Bedrock client doesn't like door to be closed from the top
            // See https://github.com/GeyserMC/Geyser/issues/4358
            Vector3i belowDoorPosition = position.sub(0, 1, 0);
            BlockState belowDoorBlockState = session.getGeyser().getWorldManager().blockAt(session, belowDoorPosition.getX(), belowDoorPosition.getY(), belowDoorPosition.getZ());
            ChunkUtils.updateBlock(session, belowDoorBlockState, belowDoorPosition);
        }
    }

    @Override
    public InteractionResult interact(InteractionContext context) {
        if (context.state().is(Blocks.IRON_DOOR)) {
            // We can't just open the door, and our offhand is weak
            return InteractionResult.PASS;
        }

        boolean open = context.state().getValue(Properties.OPEN);
        SoundEvent event = open ? SoundEvent.DOOR_CLOSE : SoundEvent.DOOR_OPEN;
        BlockState newState = context.state().withValue(Properties.OPEN, !open);
        context.updateBlock(newState);

        LevelSoundEventPacket levelSoundEventPacket = new LevelSoundEventPacket();
        levelSoundEventPacket.setPosition(context.blockPosition().add(0.5, 0.5, 0.5).toFloat());
        levelSoundEventPacket.setBabySound(false);
        levelSoundEventPacket.setRelativeVolumeDisabled(false);
        levelSoundEventPacket.setIdentifier(":");
        levelSoundEventPacket.setSound(event);
        levelSoundEventPacket.setExtraData(context.session().getBlockMappings().getBedrockBlock(newState).getRuntimeId());
        context.session().sendUpstreamPacket(levelSoundEventPacket);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canSurvive(InteractionContext context) {
        return context.state().getValue(Properties.DOUBLE_BLOCK_HALF).equals("lower") ?
            context.belowBlockState().isFaceSturdy(Direction.UP, SupportType.FULL) :
            context.belowBlockState().block().is(this);
    }
}
