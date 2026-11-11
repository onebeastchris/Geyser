/*
 * Copyright (c) 2026 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.translator.level.block.entity;

import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.packet.BlockEntityDataPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateBlockPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateSubChunkBlocksPacket;
import org.geysermc.geyser.GeyserBootstrap;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.level.WorldManager;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.BoundingBox;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.registry.BlockRegistries;
import org.geysermc.geyser.registry.type.BlockMappings;
import org.geysermc.geyser.registry.type.GeyserBedrockBlock;
import org.geysermc.geyser.scoreboard.network.util.GeyserMockContext;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.session.cache.ChunkCache;
import org.geysermc.geyser.session.cache.PistonCache;
import org.geysermc.geyser.translator.protocol.java.level.JavaBlockEventTranslator;
import org.geysermc.geyser.translator.protocol.java.level.JavaSectionBlocksUpdateTranslator;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockChangeEntry;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.value.PistonValue;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.value.PistonValueType;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundBlockEventPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSectionBlocksUpdatePacket;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Checks the Bedrock packets sent for piston movements. The Java server is emulated with vanilla's packet order and timing:
 * the block event arrives first, then that tick's section updates, and moved blocks are finalised two ticks later.
 */
public class PistonPacketFlowTest {
    private static final Vector3i PISTON_POS = Vector3i.from(1, 64, 1);
    private static final Direction FACING = Direction.EAST;
    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    @ParameterizedTest(name = "sticky={0}, pushed blocks={1}")
    @CsvSource({
        // sticky, pushed blocks, extend packets, retract packets
        "false, 0, 3, 4",
        "false, 1, 5, 4",
        "false, 12, 16, 4",
        "true, 0, 3, 4",
        "true, 1, 5, 6",
        "true, 12, 16, 6"
    })
    void packetsPerCycle(boolean sticky, int pushedBlocks, int extendPackets, int retractPackets) {
        GeyserMockContext.mockContext(context -> {
            Cycle cycle = new Harness(context).runCycle(List.of(PISTON_POS), FACING, sticky, pushedBlocks, true);

            assertEquals(extendPackets, cycle.extend().size());
            assertEquals(retractPackets, cycle.retract().size());
            // One piston tag when the movement starts and one when it ends; the client animates in between
            assertEquals(List.of(1, 2), pistonStates(cycle.extend()));
            assertEquals(List.of(3, 0), pistonStates(cycle.retract()));
            for (List<BedrockPacket> packets : List.of(cycle.extend(), cycle.retract())) {
                assertTrue(packets.stream().noneMatch(p -> p instanceof UpdateBlockPacket update && update.getDataLayer() == 1),
                    "no water layer updates for blocks that aren't waterlogged");
            }
        });
    }

    @Test
    void fiftyPistonsAreGroupedPerSubChunk() {
        GeyserMockContext.mockContext(context -> {
            List<Vector3i> pistons = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                pistons.add(Vector3i.from(i % 6, 64, i / 6)); // sub-chunk (0, 64, 0)
            }
            for (int i = 0; i < 20; i++) {
                pistons.add(Vector3i.from(16 + i % 4, 64, i / 4)); // sub-chunk (16, 64, 0)
            }
            Cycle cycle = new Harness(context).runCycle(pistons, Direction.UP, false, 1, true);

            // Extend: per sub-chunk, one packet moving the blocks and one with the server's final blocks.
            // Per piston, one MovingBlock tag and two piston tags.
            assertEquals(4 + 50 * 3, cycle.extend().size());
            // Retract: per sub-chunk, one packet removing the heads from Geyser and one from the server.
            // Per piston, two piston tags.
            assertEquals(4 + 50 * 2, cycle.retract().size());

            UpdateSubChunkBlocksPacket first = (UpdateSubChunkBlocksPacket) cycle.extend().get(0);
            assertEquals(Vector3i.from(0, 64, 0), first.getPosition());
            assertEquals(30 * 2, first.getStandardBlocks().size(), "a moving block and air for each piston");
            assertTrue(cycle.extend().get(1) instanceof UpdateSubChunkBlocksPacket, "block changes are sent before block entity data");
        });
    }

    @Test
    void finalBlocksArePlacedWhenTheServerDidNotMoveThem() {
        GeyserMockContext.mockContext(context -> {
            // Geyser predicted a block would move, but the server never placed moving_piston there,
            // so Geyser has to replace the moving block it placed itself
            List<BedrockPacket> extend = new Harness(context).runCycle(List.of(PISTON_POS), FACING, false, 1, false).extend();

            List<String> movedBlock = blockChanges(extend, PISTON_POS.add(FACING.getUnitVector().mul(2)), 0);
            assertEquals("minecraft:moving_block", movedBlock.get(0));
            assertEquals("minecraft:air", movedBlock.get(movedBlock.size() - 1));
            List<String> head = blockChanges(extend, PISTON_POS.add(FACING.getUnitVector()), 0);
            assertEquals("minecraft:piston_head", head.get(head.size() - 1));
        });
    }

    @Test
    void pushingWaterloggedBlockClearsWaterLayer() {
        GeyserMockContext.mockContext(context -> {
            Harness harness = new Harness(context);
            BlockState slab = Blocks.OAK_SLAB.defaultBlockState().withValue(Properties.WATERLOGGED, true);
            // The harness doesn't load the waterlogged registry, so mark this one state
            BlockRegistries.WATERLOGGED.get().set(slab.javaId());
            try {
                Vector3i slabPos = PISTON_POS.add(FACING.getUnitVector());
                harness.world.put(PISTON_POS, piston(false, false, FACING));
                harness.world.put(slabPos, slab);
                harness.blockEvent(PISTON_POS, PistonValueType.PUSHING, FACING);
                harness.pistonCache.sendPendingPackets();

                assertEquals(List.of("minecraft:air"), blockChanges(harness.packets, slabPos, 1));
            } finally {
                BlockRegistries.WATERLOGGED.get().clear(slab.javaId());
            }
        });
    }

    @Test
    void cancelledMidPushSendsProgressEveryTick() {
        GeyserMockContext.mockContext(context -> {
            Harness harness = new Harness(context);
            harness.world.put(PISTON_POS, piston(false, false, FACING));
            harness.blockEvent(PISTON_POS, PistonValueType.PUSHING, FACING);
            harness.tick();
            harness.packets.clear();

            harness.blockEvent(PISTON_POS, PistonValueType.CANCELLED_MID_PUSH, FACING);
            harness.ticks(3);

            // Bedrock has no state for this, so the tag is sent when cancelled and on each of the 3 ticks
            assertEquals(List.of(2, 2, 2, 0), pistonStates(harness.packets));
        });
    }

    @Test
    void pistonThatIsNotMovingGetsStaticTag() {
        GeyserMockContext.mockContext(context -> {
            Harness harness = new Harness(context);
            harness.serverBlocks(Map.of(PISTON_POS, piston(true, true, FACING)));

            List<NbtMap> tags = pistonTags(harness.packets);
            assertEquals(1, tags.size());
            assertEquals(2, tags.get(0).getByte("State"));
            assertTrue(tags.get(0).getBoolean("Sticky"));
        });
    }

    private record Cycle(List<BedrockPacket> extend, List<BedrockPacket> retract) {
    }

    /**
     * A session with mocked world access that records the Bedrock packets it sends.
     */
    private static final class Harness {
        final Map<Vector3i, BlockState> world = new HashMap<>();
        final List<BedrockPacket> packets = new ArrayList<>();
        final List<Runnable> nextServerTick = new ArrayList<>();
        final GeyserSession session;
        final PistonCache pistonCache;

        Harness(GeyserMockContext context) {
            GeyserImpl geyser = context.mockOrSpy(GeyserImpl.class);
            GeyserBootstrap bootstrap = mock(GeyserBootstrap.class);
            when(geyser.getBootstrap()).thenReturn(bootstrap);
            when(bootstrap.getResourceOrThrow(anyString())).thenAnswer(invocation ->
                GeyserImpl.class.getClassLoader().getResourceAsStream(invocation.getArgument(0)));
            if (!BlockRegistries.COLLISIONS.loaded()) {
                BlockRegistries.COLLISIONS.load(); // Needed by PistonBlockEntity's static init
            }

            session = mock(GeyserSession.class, RETURNS_DEEP_STUBS);
            pistonCache = new PistonCache(session);
            when(session.getPistonCache()).thenReturn(pistonCache);
            when(session.getTickEventLoop().inEventLoop()).thenReturn(true);
            doAnswer(invocation -> {
                // Same as GeyserSession#sendUpstreamPacket
                pistonCache.beforeUpstreamPacket(invocation.getArgument(0));
                return packets.add(invocation.getArgument(0));
            }).when(session).sendUpstreamPacket(any());
            when(session.getGeyser()).thenReturn(geyser);

            WorldManager worldManager = mock(WorldManager.class, CALLS_REAL_METHODS);
            doAnswer(invocation -> blockAt(Vector3i.from((int) invocation.getArgument(1), (int) invocation.getArgument(2), (int) invocation.getArgument(3))).javaId())
                .when(worldManager).getBlockAt(any(), anyInt(), anyInt(), anyInt());
            when(geyser.getWorldManager()).thenReturn(worldManager);

            ChunkCache chunkCache = mock(ChunkCache.class);
            doAnswer(invocation -> world.put(Vector3i.from((int) invocation.getArgument(0), (int) invocation.getArgument(1), (int) invocation.getArgument(2)),
                BlockState.of(invocation.getArgument(3)))).when(chunkCache).updateBlock(anyInt(), anyInt(), anyInt(), anyInt());
            when(session.getChunkCache()).thenReturn(chunkCache);
            when(session.getItemFrameCache().get(any())).thenReturn(null);
            when(session.getBlockBreakHandler().getCurrentBlockPos()).thenReturn(null);

            BlockMappings mappings = mock(BlockMappings.class);
            when(mappings.getBedrockBlock(any(BlockState.class))).thenAnswer(invocation ->
                bedrockBlock(((BlockState) invocation.getArgument(0)).block().javaIdentifier().value()));
            when(mappings.getBedrockAir()).thenReturn(bedrockBlock("air"));
            when(mappings.getBedrockMovingBlock()).thenReturn(bedrockBlock("moving_block"));
            when(session.getBlockMappings()).thenReturn(mappings);

            // Keep the player away from the pistons
            BoundingBox playerBox = new BoundingBox(100.5, 64.9, 100.5, 0.6, 1.8, 0.6);
            when(session.getCollisionManager().getActiveBoundingBox()).thenReturn(playerBox);
            when(session.getCollisionManager().getPlayerBoundingBox()).thenReturn(playerBox);
            when(session.getPlayerEntity().getVehicle()).thenReturn(null);
        }

        BlockState blockAt(Vector3i pos) {
            return world.getOrDefault(pos, AIR);
        }

        void blockEvent(Vector3i pos, PistonValueType type, Direction facing) {
            new JavaBlockEventTranslator().translate(session, new ClientboundBlockEventPacket(pos, type.ordinal(), facing.ordinal(), type,
                new PistonValue(org.geysermc.mcprotocollib.protocol.data.game.entity.object.Direction.valueOf(facing.name())), Blocks.PISTON.javaId()));
        }

        /**
         * Sends one section update per sub-chunk, like the vanilla server does at the end of a tick
         */
        void serverBlocks(Map<Vector3i, BlockState> changes) {
            Map<Vector3i, List<BlockChangeEntry>> sections = new LinkedHashMap<>();
            changes.forEach((pos, state) -> sections.computeIfAbsent(Vector3i.from(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4),
                section -> new ArrayList<>()).add(new BlockChangeEntry(pos, state.javaId())));
            sections.forEach((section, entries) -> new JavaSectionBlocksUpdateTranslator().translate(session,
                new ClientboundSectionBlocksUpdatePacket(section.getX(), section.getY(), section.getZ(), entries.toArray(BlockChangeEntry[]::new))));
            // The chunk cache is mocked, so the translator doesn't update the world
            world.putAll(changes);
        }

        void tick() {
            pistonCache.tick();
        }

        /**
         * Runs server ticks, each followed by a Geyser tick
         */
        void ticks(int count) {
            for (int i = 0; i < count; i++) {
                List<Runnable> serverTick = new ArrayList<>(nextServerTick);
                nextServerTick.clear();
                serverTick.forEach(Runnable::run);
                tick();
            }
        }

        /**
         * Extends pistons that each push a line of stone, then retracts them.
         *
         * @param serverMovesBlocks whether the server moves the blocks Geyser expects to move
         */
        Cycle runCycle(List<Vector3i> pistons, Direction facing, boolean sticky, int pushedBlocks, boolean serverMovesBlocks) {
            Vector3i offset = facing.getUnitVector();
            for (Vector3i piston : pistons) {
                world.put(piston, piston(sticky, false, facing));
                for (int i = 1; i <= pushedBlocks; i++) {
                    world.put(piston.add(offset.mul(i)), STONE);
                }
            }

            pistons.forEach(piston -> blockEvent(piston, PistonValueType.PUSHING, facing));
            Map<Vector3i, BlockState> extendStart = new LinkedHashMap<>();
            Map<Vector3i, BlockState> extendEnd = new LinkedHashMap<>();
            for (Vector3i piston : pistons) {
                extendStart.put(piston, piston(sticky, true, facing));
                if (serverMovesBlocks) {
                    extendStart.put(piston.add(offset), movingPiston(facing));
                    extendEnd.put(piston.add(offset), pistonHead(sticky, facing));
                    for (int i = 1; i <= pushedBlocks; i++) {
                        extendStart.put(piston.add(offset.mul(i + 1)), movingPiston(facing));
                        extendEnd.put(piston.add(offset.mul(i + 1)), STONE);
                    }
                }
            }
            List<BedrockPacket> extend = runMovement(extendStart, extendEnd);

            boolean pullsBlock = sticky && pushedBlocks > 0;
            pistons.forEach(piston -> blockEvent(piston, PistonValueType.PULLING, facing));
            Map<Vector3i, BlockState> retractStart = new LinkedHashMap<>();
            Map<Vector3i, BlockState> retractEnd = new LinkedHashMap<>();
            for (Vector3i piston : pistons) {
                retractStart.put(piston, movingPiston(facing));
                retractStart.put(piston.add(offset), pullsBlock ? movingPiston(facing) : AIR);
                retractEnd.put(piston, piston(sticky, false, facing));
                if (pullsBlock) {
                    retractStart.put(piston.add(offset.mul(2)), AIR);
                    retractEnd.put(piston.add(offset), STONE);
                }
            }
            List<BedrockPacket> retract = runMovement(retractStart, retractEnd);
            return new Cycle(extend, retract);
        }

        /**
         * Sends the server's block changes for a movement and runs ticks until the pistons are removed from the cache
         */
        private List<BedrockPacket> runMovement(Map<Vector3i, BlockState> start, Map<Vector3i, BlockState> end) {
            serverBlocks(start);
            tick();
            ticks(1);
            nextServerTick.add(() -> serverBlocks(end));
            ticks(8);
            List<BedrockPacket> sent = List.copyOf(packets);
            packets.clear();
            return sent;
        }
    }

    private static GeyserBedrockBlock bedrockBlock(String name) {
        return new GeyserBedrockBlock(name.hashCode(), NbtMap.builder().putString("name", "minecraft:" + name).build());
    }

    private static BlockState piston(boolean sticky, boolean extended, Direction facing) {
        return (sticky ? Blocks.STICKY_PISTON : Blocks.PISTON).defaultBlockState()
            .withValue(Properties.FACING, facing)
            .withValue(Properties.EXTENDED, extended);
    }

    private static BlockState pistonHead(boolean sticky, Direction facing) {
        return Blocks.PISTON_HEAD.defaultBlockState()
            .withValue(Properties.FACING, facing)
            .withValue(Properties.SHORT, false)
            .withValue(Properties.PISTON_TYPE, sticky ? "sticky" : "normal");
    }

    private static BlockState movingPiston(Direction facing) {
        return Blocks.MOVING_PISTON.defaultBlockState().withValue(Properties.FACING, facing);
    }

    private static List<NbtMap> pistonTags(List<BedrockPacket> packets) {
        return packets.stream()
            .filter(packet -> packet instanceof BlockEntityDataPacket data && "PistonArm".equals(data.getData().getString("id")))
            .map(packet -> ((BlockEntityDataPacket) packet).getData())
            .toList();
    }

    private static List<Integer> pistonStates(List<BedrockPacket> packets) {
        return pistonTags(packets).stream().map(tag -> (int) tag.getByte("State")).toList();
    }

    /**
     * @return the names of the blocks sent for a position and layer, from both single and grouped block updates
     */
    private static List<String> blockChanges(List<BedrockPacket> packets, Vector3i pos, int layer) {
        List<String> names = new ArrayList<>();
        for (BedrockPacket packet : packets) {
            if (packet instanceof UpdateBlockPacket update && update.getBlockPosition().equals(pos) && update.getDataLayer() == layer) {
                names.add(name(update.getDefinition()));
            } else if (packet instanceof UpdateSubChunkBlocksPacket subChunk) {
                for (var entry : layer == 0 ? subChunk.getStandardBlocks() : subChunk.getExtraBlocks()) {
                    if (entry.getPosition().equals(pos)) {
                        names.add(name(entry.getDefinition()));
                    }
                }
            }
        }
        return names;
    }

    private static String name(BlockDefinition definition) {
        return ((GeyserBedrockBlock) definition).getState().getString("name");
    }
}
