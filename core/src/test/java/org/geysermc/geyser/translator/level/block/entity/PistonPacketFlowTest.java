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

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.packet.BlockEntityDataPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateBlockPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateSubChunkBlocksPacket;
import org.geysermc.geyser.GeyserBootstrap;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.level.WorldManager;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.Block;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
 * Records the Bedrock packets sent for a full piston cycle, emulating the vanilla Java server's
 * packet order and timing (block event first, then section block updates in the same tick,
 * moving blocks finalised two ticks later).
 */
public class PistonPacketFlowTest {
    private static final Vector3i P = Vector3i.from(1, 64, 1);
    private static final Direction FACING = Direction.EAST;
    private static final Vector3i D = FACING.getUnitVector();

    static final class Harness {
        final Map<Vector3i, BlockState> world = new HashMap<>();
        final List<BedrockPacket> packets = new ArrayList<>();
        final GeyserSession session;
        PistonCache pistonCache;
        final JavaBlockEventTranslator blockEvent = new JavaBlockEventTranslator();
        final JavaSectionBlocksUpdateTranslator sectionUpdate = new JavaSectionBlocksUpdateTranslator();
        final List<Runnable> pendingServerTick = new ArrayList<>();

        Harness(GeyserMockContext context) {
            GeyserImpl geyser = context.mockOrSpy(GeyserImpl.class);
            GeyserBootstrap bootstrap = mock(GeyserBootstrap.class);
            when(geyser.getBootstrap()).thenReturn(bootstrap);
            when(bootstrap.getResourceOrThrow(anyString())).thenAnswer(inv ->
                GeyserImpl.class.getClassLoader().getResourceAsStream(inv.getArgument(0)));
            Blocks.PISTON.javaId();
            if (!BlockRegistries.COLLISIONS.loaded()) {
                BlockRegistries.COLLISIONS.load();
            }

            session = mock(GeyserSession.class, RETURNS_DEEP_STUBS);
            doAnswer(inv -> {
                // Same hook as GeyserSession#sendUpstreamPacket
                this.pistonCache.beforeUpstreamPacket(inv.getArgument(0));
                return packets.add(inv.getArgument(0));
            }).when(session).sendUpstreamPacket(any());
            when(session.getGeyser()).thenReturn(geyser);

            WorldManager worldManager = mock(WorldManager.class, CALLS_REAL_METHODS);
            doAnswer(inv -> get(Vector3i.from((int) inv.getArgument(1), (int) inv.getArgument(2), (int) inv.getArgument(3))).javaId())
                .when(worldManager).getBlockAt(any(), anyInt(), anyInt(), anyInt());
            when(worldManager.hasOwnChunkCache()).thenReturn(false);
            when(geyser.getWorldManager()).thenReturn(worldManager);
            when(session.getErosionHandler().isActive()).thenReturn(false);

            ChunkCache chunkCache = mock(ChunkCache.class);
            doAnswer(inv -> world.put(Vector3i.from((int) inv.getArgument(0), (int) inv.getArgument(1), (int) inv.getArgument(2)),
                BlockState.of(inv.getArgument(3)))).when(chunkCache).updateBlock(anyInt(), anyInt(), anyInt(), anyInt());
            when(chunkCache.isCache()).thenReturn(false);
            when(session.getChunkCache()).thenReturn(chunkCache);
            when(session.getItemFrameCache().get(any())).thenReturn(null);
            when(session.getBlockBreakHandler().getCurrentBlockPos()).thenReturn(null);

            BlockMappings mappings = mock(BlockMappings.class);
            when(mappings.getBedrockBlock(anyInt())).thenAnswer(inv -> def(BlockState.of(inv.getArgument(0)).block().javaIdentifier().value()));
            when(mappings.getBedrockBlock(any(BlockState.class))).thenAnswer(inv -> def(((BlockState) inv.getArgument(0)).block().javaIdentifier().value()));
            GeyserBedrockBlock air = def("air");
            GeyserBedrockBlock water = def("water");
            GeyserBedrockBlock movingBlock = def("moving_block");
            when(mappings.getBedrockAir()).thenReturn(air);
            when(mappings.getBedrockWater()).thenReturn(water);
            when(mappings.getBedrockMovingBlock()).thenReturn(movingBlock);
            when(session.getBlockMappings()).thenReturn(mappings);

            // Player far away from the pistons
            BoundingBox playerBox = new BoundingBox(100.5, 64.9, 100.5, 0.6, 1.8, 0.6);
            when(session.getCollisionManager().getActiveBoundingBox()).thenReturn(playerBox);
            when(session.getCollisionManager().getPlayerBoundingBox()).thenReturn(playerBox);
            when(session.getPlayerEntity().getVehicle()).thenReturn(null);

            pistonCache = new PistonCache(session);
            when(session.getPistonCache()).thenReturn(pistonCache);
        }

        static GeyserBedrockBlock def(String name) {
            return new GeyserBedrockBlock(name.hashCode(), NbtMap.builder().putString("name", "minecraft:" + name).build());
        }

        BlockState get(Vector3i pos) {
            return world.getOrDefault(pos, Blocks.AIR.defaultBlockState());
        }

        void blockEvent(Vector3i pos, PistonValueType type) {
            blockEvent(pos, type, FACING);
        }

        void blockEvent(Vector3i pos, PistonValueType type, Direction facing) {
            blockEvent.translate(session, new ClientboundBlockEventPacket(pos, type.ordinal(), facing.ordinal(), type, new PistonValue(
                org.geysermc.mcprotocollib.protocol.data.game.entity.object.Direction.valueOf(facing.name())), Blocks.PISTON.javaId()));
        }

        /** Sends a section update like the vanilla server does at the end of a tick. */
        /** Sends section updates like the vanilla server does at the end of a tick. */
        void serverBlocks(Map<Vector3i, BlockState> changes) {
            Map<Vector3i, List<BlockChangeEntry>> sections = new LinkedHashMap<>();
            changes.forEach((pos, state) -> sections.computeIfAbsent(Vector3i.from(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4),
                k -> new ArrayList<>()).add(new BlockChangeEntry(pos, state.javaId())));
            sections.forEach((section, entries) -> sectionUpdate.translate(session, new ClientboundSectionBlocksUpdatePacket(
                section.getX(), section.getY(), section.getZ(), entries.toArray(BlockChangeEntry[]::new))));
            // The chunk cache palette is disabled in this harness, so keep the world in sync manually
            world.putAll(changes);
        }

        void geyserTick() {
            pistonCache.tick();
        }

        /** Runs n server ticks, each followed by one Geyser session tick. */
        void ticks(int n) {
            for (int i = 0; i < n; i++) {
                List<Runnable> run = new ArrayList<>(pendingServerTick);
                pendingServerTick.clear();
                run.forEach(Runnable::run);
                geyserTick();
            }
        }
    }

    static BlockState piston(boolean sticky, boolean extended) {
        return piston(sticky, extended, FACING);
    }

    static BlockState piston(boolean sticky, boolean extended, Direction facing) {
        return (sticky ? Blocks.STICKY_PISTON : Blocks.PISTON).defaultBlockState()
            .withValue(Properties.FACING, facing).withValue(Properties.EXTENDED, extended);
    }

    static BlockState head(boolean sticky) {
        return head(sticky, FACING);
    }

    static BlockState head(boolean sticky, Direction facing) {
        return Blocks.PISTON_HEAD.defaultBlockState().withValue(Properties.FACING, facing)
            .withValue(Properties.SHORT, false)
            .withValue(Properties.PISTON_TYPE, sticky ? "sticky" : "normal");
    }

    static BlockState moving() {
        return moving(FACING);
    }

    static BlockState moving(Direction facing) {
        return Blocks.MOVING_PISTON.defaultBlockState().withValue(Properties.FACING, facing);
    }

    /**
     * Extends a piston pushing {@code count} stone blocks in a line, then retracts it.
     *
     * @param serverMovesBlocks whether the server agrees with Geyser on which blocks move
     * @return the Bedrock packets sent for the extension and the retraction
     */
    private static List<List<BedrockPacket>> runCycle(Harness h, boolean sticky, int count, boolean serverMovesBlocks) {
        BlockState stone = Blocks.STONE.defaultBlockState();
        h.world.put(P, piston(sticky, false));
        for (int i = 1; i <= count; i++) {
            h.world.put(P.add(D.mul(i)), stone);
        }

        // ---- Extend ----
        h.blockEvent(P, PistonValueType.PUSHING);
        Map<Vector3i, BlockState> t0 = new LinkedHashMap<>();
        t0.put(P, piston(sticky, true));
        if (serverMovesBlocks) {
            t0.put(P.add(D), moving());
            for (int i = 1; i <= count; i++) {
                t0.put(P.add(D.mul(i + 1)), moving());
            }
        }
        h.serverBlocks(t0);
        h.geyserTick();
        h.ticks(1); // server tick 1: nothing changes
        Map<Vector3i, BlockState> t2 = new LinkedHashMap<>();
        if (serverMovesBlocks) {
            t2.put(P.add(D), head(sticky));
            for (int i = 1; i <= count; i++) {
                t2.put(P.add(D.mul(i + 1)), stone);
            }
        }
        h.pendingServerTick.add(() -> h.serverBlocks(t2));
        h.ticks(8); // finalise + piston REMOVAL_DELAY
        List<BedrockPacket> extend = List.copyOf(h.packets);
        h.packets.clear();

        // ---- Retract ----
        h.blockEvent(P, PistonValueType.PULLING);
        Map<Vector3i, BlockState> r0 = new LinkedHashMap<>();
        r0.put(P, moving());
        if (sticky && count > 0) {
            r0.put(P.add(D), moving());
            r0.put(P.add(D.mul(2)), Blocks.AIR.defaultBlockState());
        } else {
            r0.put(P.add(D), Blocks.AIR.defaultBlockState());
        }
        h.serverBlocks(r0);
        h.geyserTick();
        h.ticks(1);
        Map<Vector3i, BlockState> r2 = new LinkedHashMap<>();
        r2.put(P, piston(sticky, false));
        if (sticky && count > 0) {
            r2.put(P.add(D), stone);
        }
        h.pendingServerTick.add(() -> h.serverBlocks(r2));
        h.ticks(8);
        List<BedrockPacket> retract = List.copyOf(h.packets);
        h.packets.clear();
        return List.of(extend, retract);
    }

    static List<NbtMap> pistonArmTags(List<BedrockPacket> packets) {
        return packets.stream()
            .filter(p -> p instanceof BlockEntityDataPacket b && "PistonArm".equals(b.getData().getString("id")))
            .map(p -> ((BlockEntityDataPacket) p).getData())
            .toList();
    }

    /**
     * @return the names of the blocks sent for a position and layer, from both single and grouped block updates
     */
    static List<String> blockChanges(List<BedrockPacket> packets, Vector3i pos, int layer) {
        List<String> names = new ArrayList<>();
        for (BedrockPacket p : packets) {
            if (p instanceof UpdateBlockPacket u && u.getBlockPosition().equals(pos) && u.getDataLayer() == layer) {
                names.add(name(u.getDefinition()));
            } else if (p instanceof UpdateSubChunkBlocksPacket s) {
                for (var entry : layer == 0 ? s.getStandardBlocks() : s.getExtraBlocks()) {
                    if (entry.getPosition().equals(pos)) {
                        names.add(name(entry.getDefinition()));
                    }
                }
            }
        }
        return names;
    }

    static List<UpdateBlockPacket> updateBlocks(List<BedrockPacket> packets, Vector3i pos) {
        return packets.stream()
            .filter(p -> p instanceof UpdateBlockPacket u && u.getBlockPosition().equals(pos))
            .map(p -> (UpdateBlockPacket) p)
            .toList();
    }

    static String summarise(List<BedrockPacket> packets) {
        Object2IntOpenHashMap<String> counts = new Object2IntOpenHashMap<>();
        StringBuilder seq = new StringBuilder();
        for (BedrockPacket p : packets) {
            String key;
            if (p instanceof UpdateBlockPacket u) {
                key = "UpdateBlock[L" + u.getDataLayer() + " " + ((GeyserBedrockBlock) u.getDefinition()).getState().getString("name").substring(10) + "]";
            } else if (p instanceof BlockEntityDataPacket b) {
                NbtMap d = b.getData();
                key = "BlockEntityData[" + d.getString("id") + ("PistonArm".equals(d.getString("id")) ? " State=" + d.getByte("State") + " P=" + d.getFloat("Progress") : "") + "]";
            } else if (p instanceof UpdateSubChunkBlocksPacket s) {
                key = "UpdateSubChunkBlocks[" + s.getStandardBlocks().size() + "+" + s.getExtraBlocks().size() + "]";
            } else {
                key = p.getClass().getSimpleName();
            }
            counts.addTo(key, 1);
            String pos = p instanceof UpdateBlockPacket u ? " @" + u.getBlockPosition() : p instanceof BlockEntityDataPacket b ? " @" + b.getBlockPosition() : "";
            seq.append("\n      ").append(key).append(pos);
        }
        return "total=" + packets.size() + " " + new TreeMap<>(counts) + seq;
    }

    @Test
    void packetsPerCycle() {
        // sticky, attached blocks, expected extend packets, expected retract packets
        int[][] cases = {
            {0, 0, 3, 4},
            {0, 1, 5, 4},
            {0, 12, 16, 4},
            {1, 0, 3, 4},
            {1, 1, 5, 6},
            {1, 12, 16, 6}
        };
        GeyserMockContext.mockContext(context -> {
            for (int[] c : cases) {
                boolean sticky = c[0] == 1;
                List<List<BedrockPacket>> cycle = runCycle(new Harness(context), sticky, c[1], true);
                String name = (sticky ? "sticky" : "normal") + " piston, " + c[1] + " block(s)";
                System.out.println("=== " + name + " ===\n  extend: " + summarise(cycle.get(0)) + "\n  retract: " + summarise(cycle.get(1)));

                assertEquals(c[2], cycle.get(0).size(), name + " extend");
                assertEquals(c[3], cycle.get(1).size(), name + " retract");
                for (List<BedrockPacket> stroke : cycle) {
                    // Like BDS: one PistonArm tag when the movement starts and one when it ends
                    assertEquals(2, pistonArmTags(stroke).size(), name);
                    // No separate water layer updates for blocks that aren't waterlogged
                    assertTrue(stroke.stream().noneMatch(p -> p instanceof UpdateBlockPacket u && u.getDataLayer() == 1), name);
                }
                List<NbtMap> extendTags = pistonArmTags(cycle.get(0));
                assertEquals(1, extendTags.get(0).getByte("State"));
                assertEquals(2, extendTags.get(1).getByte("State"));
                List<NbtMap> retractTags = pistonArmTags(cycle.get(1));
                assertEquals(3, retractTags.get(0).getByte("State"));
                assertEquals(0, retractTags.get(1).getByte("State"));
            }
        });
    }

    @Test
    void finalBlocksPlacedWhenServerDidNotMoveThem() {
        GeyserMockContext.mockContext(context -> {
            // Geyser predicted a block moving, but the server never placed moving_piston there.
            // Geyser must then correct the moving_block it placed itself.
            List<BedrockPacket> extend = runCycle(new Harness(context), false, 1, false).get(0);
            Vector3i target = P.add(D.mul(2));
            List<String> updates = blockChanges(extend, target, 0);
            assertEquals("minecraft:moving_block", updates.get(0));
            assertEquals("minecraft:air", updates.get(updates.size() - 1));
            List<String> head = blockChanges(extend, P.add(D), 0);
            assertEquals("minecraft:piston_head", head.get(head.size() - 1));
        });
    }

    @Test
    void waterloggedBlockClearsWaterLayer() {
        GeyserMockContext.mockContext(context -> {
            Harness h = new Harness(context);
            BlockState slab = Blocks.OAK_SLAB.defaultBlockState().withValue(Properties.WATERLOGGED, true);
            BlockRegistries.WATERLOGGED.get().set(slab.javaId());
            try {
                h.world.put(P, piston(false, false));
                h.world.put(P.add(D), slab);
                h.blockEvent(P, PistonValueType.PUSHING);
                h.pistonCache.sendPendingPackets();
                assertEquals(List.of("minecraft:air"), blockChanges(h.packets, P.add(D), 1));
            } finally {
                BlockRegistries.WATERLOGGED.get().clear(slab.javaId());
            }
        });
    }

    @Test
    void cancelledMidPushKeepsSendingProgress() {
        GeyserMockContext.mockContext(context -> {
            Harness h = new Harness(context);
            h.world.put(P, piston(false, false));
            h.blockEvent(P, PistonValueType.PUSHING);
            h.geyserTick();
            h.packets.clear();
            h.blockEvent(P, PistonValueType.CANCELLED_MID_PUSH);
            h.ticks(3);
            List<NbtMap> tags = pistonArmTags(h.packets);
            assertEquals(4, tags.size());
            assertEquals(0, tags.get(tags.size() - 1).getByte("State"));
        });
    }

    @Test
    void staticPistonTagSentWhenNotMoving() {
        GeyserMockContext.mockContext(context -> {
            Harness h = new Harness(context);
            h.serverBlocks(Map.of(P, piston(true, true)));
            List<NbtMap> tags = pistonArmTags(h.packets);
            assertEquals(1, tags.size());
            assertEquals(2, tags.get(0).getByte("State"));
            assertTrue(tags.get(0).getBoolean("Sticky"));
            assertEquals(2, updateBlocks(h.packets, P).size());
        });
    }

    @Test
    void fiftyPistonsInTwoSections() {
        GeyserMockContext.mockContext(context -> {
            Harness h = new Harness(context);
            Direction up = Direction.UP;
            BlockState stone = Blocks.STONE.defaultBlockState();
            List<Vector3i> pistons = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                pistons.add(Vector3i.from(i % 6, 64, i / 6)); // section (0, 4, 0)
            }
            for (int i = 0; i < 20; i++) {
                pistons.add(Vector3i.from(16 + i % 4, 64, i / 4)); // section (1, 4, 0)
            }
            for (Vector3i pos : pistons) {
                h.world.put(pos, piston(false, false, up));
                h.world.put(pos.up(), stone);
            }

            // ---- Extend: block events for every piston, then the server's section updates ----
            pistons.forEach(pos -> h.blockEvent(pos, PistonValueType.PUSHING, up));
            Map<Vector3i, BlockState> t0 = new LinkedHashMap<>();
            pistons.forEach(pos -> {
                t0.put(pos, piston(false, true, up));
                t0.put(pos.up(), moving(up));
                t0.put(pos.up(2), moving(up));
            });
            h.serverBlocks(t0);
            h.geyserTick();
            h.ticks(1);
            Map<Vector3i, BlockState> t2 = new LinkedHashMap<>();
            pistons.forEach(pos -> {
                t2.put(pos.up(), head(false, up));
                t2.put(pos.up(2), stone);
            });
            h.pendingServerTick.add(() -> h.serverBlocks(t2));
            h.ticks(8);
            List<BedrockPacket> extend = List.copyOf(h.packets);
            h.packets.clear();

            // ---- Retract ----
            pistons.forEach(pos -> h.blockEvent(pos, PistonValueType.PULLING, up));
            Map<Vector3i, BlockState> r0 = new LinkedHashMap<>();
            pistons.forEach(pos -> {
                r0.put(pos, moving(up));
                r0.put(pos.up(), Blocks.AIR.defaultBlockState());
            });
            h.serverBlocks(r0);
            h.geyserTick();
            h.ticks(1);
            Map<Vector3i, BlockState> r2 = new LinkedHashMap<>();
            pistons.forEach(pos -> r2.put(pos, piston(false, false, up)));
            h.pendingServerTick.add(() -> h.serverBlocks(r2));
            h.ticks(8);
            List<BedrockPacket> retract = List.copyOf(h.packets);

            System.out.println("=== 50 pistons ===\n  extend: " + counts(extend) + "\n  retract: " + counts(retract)
                + "\n  per piston: " + (extend.size() + retract.size()) / 50.0);
            // Per section: one grouped packet when the blocks start moving and one when they settle
            assertEquals(2, extend.stream().filter(p -> p instanceof UpdateSubChunkBlocksPacket).count() / 2);
            assertEquals(100 + 50, pistonArmTags(extend).size() + movingBlockTags(extend));
            assertEquals(0, extend.stream().filter(p -> p instanceof UpdateBlockPacket).count());
            assertEquals(154, extend.size());
            assertEquals(104, retract.size());
            // The grouped packet places each moving block before its block entity data is sent
            UpdateSubChunkBlocksPacket first = (UpdateSubChunkBlocksPacket) extend.get(0);
            assertEquals(Vector3i.from(0, 64, 0), first.getPosition());
            assertEquals(60, first.getStandardBlocks().size());
        });
    }

    static long movingBlockTags(List<BedrockPacket> packets) {
        return packets.stream()
            .filter(p -> p instanceof BlockEntityDataPacket b && "MovingBlock".equals(b.getData().getString("id")))
            .count();
    }

    static String counts(List<BedrockPacket> packets) {
        String summary = summarise(packets);
        return summary.substring(0, summary.indexOf('\n') < 0 ? summary.length() : summary.indexOf('\n'));
    }

    static String name(UpdateBlockPacket packet) {
        return name(packet.getDefinition());
    }

    static String name(org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition definition) {
        return ((GeyserBedrockBlock) definition).getState().getString("name");
    }
}
