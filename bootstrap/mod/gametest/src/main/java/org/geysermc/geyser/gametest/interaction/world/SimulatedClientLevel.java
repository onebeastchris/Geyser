/*
 * Copyright (c) 2019-2026 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.gametest.interaction.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A {@link Level} that behaves like the Java client's level for the purpose of predicting interactions: it reports
 * {@code isClientSide() == true}, is not a {@code ServerLevel}, reads blocks, fluids and block entities from the game
 * test's server level, and never writes to it.
 * <p>
 * Entity queries return the server level's entities plus the simulated player (see {@link #setPlayer(Entity)}).
 * <p>
 * Writes (blocks set by client-side prediction) go to an overlay that is visible to subsequent reads until
 * {@link #reset()}, like the client's predicted block states. Sounds, particles, game events, entity additions,
 * neighbour updates and the like are dropped.
 * <p>
 * The class extending {@code Level} is generated at runtime by {@link ProxyClassGenerator}, so that it does not depend
 * on the exact signatures of {@code Level}'s abstract methods: abstract public methods are forwarded to the server level
 * (registries, recipes, chunk source, ...), and everything listed in {@link #NO_OP_METHODS} is dropped by name.
 * Instances are created without a constructor; their {@code Level} fields are copied from the server level.
 */
public final class SimulatedClientLevel {
    /**
     * Methods (by name, any signature) that would have side effects on the world or the network; they do nothing and
     * return {@code false}/{@code 0}/{@code null}. Writes that clients predict are implemented in {@link Behaviour}.
     */
    static final Set<String> NO_OP_METHODS = Set.of(
        // Writes not handled by the behaviour (fallbacks in case a signature changes)
        "setBlock", "removeBlock", "destroyBlock", "setBlockAndUpdate",
        "setBlockEntity", "removeBlockEntity", "blockEntityChanged", "onBlockEntityAdded", "addBlockEntityTicker", "tickBlockEntities",
        "addFreshEntity", "addFreshEntityWithPassengers", "addEntity", "removeEntity",
        // Neighbour and shape updates, scheduled ticks
        "sendBlockUpdated", "setBlocksDirty", "updatePOIOnBlockStateChange", "updateNeighborsAt", "updateNeighborsAtExceptFromFacing",
        "neighborChanged", "neighborShapeChanged", "updateNeighbourForOutputSignal", "blockUpdated", "scheduleTick", "blockEvent",
        // Effects and events
        "playSound", "playSeededSound", "playLocalSound", "playPlayerSound", "addParticle", "addAlwaysVisibleParticle",
        "addDestroyBlockEffect", "addBreakingBlockEffect", "explode", "levelEvent", "globalLevelEvent", "gameEvent",
        "destroyBlockProgress", "broadcastEntityEvent", "broadcastDamageEvent", "createFireworks",
        // World state
        "setRespawnData", "setThunderLevel", "setRainLevel", "setSkyFlashTime", "setSpawnSettings", "updateSkyBrightness",
        "setDayTime", "setMapData", "overrideMapData", "sendPacketToServer", "disconnect", "close"
    );
    /**
     * Public, non-abstract methods that are forwarded to the server level, because the inherited implementation relies
     * on {@code Level#getEntities()}, which is protected and can't be forwarded.
     */
    static final Set<String> FORWARDED_METHODS = Set.of(
        "getEntities", "getEntity", "hasEntities", "getPushableEntities", "getEntityInAnyDimension", "getPlayerInAnyDimension"
    );

    private static final String GENERATED_CLASS_NAME = SimulatedClientLevel.class.getPackageName() + ".GeneratedSimulatedClientLevel";
    private static @Nullable Generated generated;

    private final ServerLevel serverLevel;
    private final Level level;
    private final Behaviour behaviour;

    private SimulatedClientLevel(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
        Generated generated = generated();
        this.level = ReflectionUtils.allocate(generated.clazz());
        ReflectionUtils.copyFields(Level.class, serverLevel, level);
        Field isClientSide = ReflectionUtils.findField(Level.class, "isClientSide", boolean.class);
        if (isClientSide != null) {
            // Only read by Level internals; the isClientSide() method itself is overridden
            ReflectionUtils.set(isClientSide, level, true);
        }
        this.behaviour = new Behaviour(serverLevel, level);
        ReflectionUtils.set(generated.behaviourField(), level, behaviour);
        ReflectionUtils.set(generated.delegateField(), level, serverLevel);
    }

    public static SimulatedClientLevel create(ServerLevel serverLevel) {
        return new SimulatedClientLevel(serverLevel);
    }

    /**
     * @return the level to pass to vanilla code
     */
    public Level level() {
        return level;
    }

    public ServerLevel serverLevel() {
        return serverLevel;
    }

    /**
     * Forgets everything written to this level and the client copies of block entities. Call before every simulated click.
     */
    public void reset() {
        behaviour.overlay.clear();
        behaviour.blockEntities.clear();
        behaviour.player = null;
    }

    /**
     * Sets the player that lives in this level, which is returned by entity queries of this level (the server
     * level doesn't know about it).
     */
    public void setPlayer(@Nullable Entity player) {
        behaviour.player = player;
    }

    /**
     * @return a description of how every {@code Level} method is overridden, for debugging
     */
    public static String describeGeneratedMethods() {
        return generated().plan().stream().map(Object::toString).sorted().collect(Collectors.joining("\n"));
    }

    private static synchronized Generated generated() {
        if (generated == null) {
            List<ProxyClassGenerator.PlannedMethod> plan = ProxyClassGenerator.plan(Level.class, Behaviour.class, NO_OP_METHODS, FORWARDED_METHODS);
            byte[] bytes = ProxyClassGenerator.generate(GENERATED_CLASS_NAME, Level.class, Behaviour.class, plan);
            ProxyClassGenerator.GeneratedClassLoader loader = new ProxyClassGenerator.GeneratedClassLoader(SimulatedClientLevel.class.getClassLoader());
            Class<? extends Level> clazz = loader.define(GENERATED_CLASS_NAME, bytes).asSubclass(Level.class);
            try {
                generated = new Generated(clazz, clazz.getField(ProxyClassGenerator.BEHAVIOUR_FIELD),
                    clazz.getField(ProxyClassGenerator.DELEGATE_FIELD), plan);
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException(e);
            }
        }
        return generated;
    }

    private record Generated(Class<? extends Level> clazz, Field behaviourField, Field delegateField, List<ProxyClassGenerator.PlannedMethod> plan) {
    }

    /**
     * The overridden {@code Level} methods. Every public method here whose name and parameter types match a method
     * of {@code Level} (or its interfaces) replaces it; signatures are copied from {@code Level} (26.2).
     */
    public static final class Behaviour {
        private static final @Nullable Field BLOCK_ENTITY_LEVEL =ReflectionUtils.findField(BlockEntity.class, Level.class);

        private final ServerLevel serverLevel;
        private final Level level;
        final Map<BlockPos, BlockState> overlay = new HashMap<>();
        final Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();
        @Nullable Entity player;

        Behaviour(ServerLevel serverLevel, Level level) {
            this.serverLevel = serverLevel;
            this.level = level;
        }

        public boolean isClientSide() {
            return true;
        }

        public @Nullable MinecraftServer getServer() {
            return null;
        }

        public BlockState getBlockState(BlockPos pos) {
            BlockState state = overlay.get(pos);
            return state == null ? serverLevel.getBlockState(pos) : state;
        }

        public FluidState getFluidState(BlockPos pos) {
            BlockState state = overlay.get(pos);
            return state == null ? serverLevel.getFluidState(pos) : state.getFluidState();
        }

        /**
         * Returns a copy of the server's block entity that lives in this level, so that client-side checks in block
         * entity code ({@code level.isClientSide()}) behave like on the client.
         */
        public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
            BlockState overlayState = overlay.get(pos);
            if (overlayState != null && !overlayState.is(serverLevel.getBlockState(pos).getBlock())) {
                // The client predicted another block here; its block entity would be a new, empty one
                return null;
            }
            BlockEntity existing = blockEntities.get(pos);
            if (existing != null) {
                return existing;
            }
            BlockEntity server = serverLevel.getBlockEntity(pos);
            if (server == null) {
                return null;
            }
            BlockEntity copy = ReflectionUtils.allocate(server.getClass());
            ReflectionUtils.copyFields(server.getClass(), server, copy);
            if (BLOCK_ENTITY_LEVEL != null) {
                ReflectionUtils.set(BLOCK_ENTITY_LEVEL, copy, level);
            }
            blockEntities.put(pos.immutable(), copy);
            return copy;
        }

        public List<Entity> getEntities(@Nullable Entity except, AABB bb, Predicate<? super Entity> selector) {
            List<Entity> entities = serverLevel.getEntities(except, bb, selector);
            if (player != null && player != except && player.getBoundingBox().intersects(bb) && selector.test(player)) {
                entities = new ArrayList<>(entities);
                entities.add(player);
            }
            return entities;
        }

        /**
         * Used by e.g. {@code EntityGetter#isUnobstructed}; must include the player like the other overloads.
         */
        public List<Entity> getEntities(@Nullable Entity except, AABB bb) {
            return getEntities(except, bb, entity -> !entity.isSpectator());
        }

        public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> type, AABB bb, Predicate<? super T> selector) {
            List<T> entities = serverLevel.getEntities(type, bb, selector);
            if (player != null && player.getBoundingBox().intersects(bb)) {
                T cast = type.tryCast(player);
                if (cast != null && selector.test(cast)) {
                    entities = new ArrayList<>(entities);
                    entities.add(cast);
                }
            }
            return entities;
        }

        public boolean setBlock(BlockPos pos, BlockState state, int updateFlags) {
            return setBlock(pos, state, updateFlags, 512);
        }

        public boolean setBlock(BlockPos pos, BlockState state, int updateFlags, int updateLimit) {
            if (serverLevel.isOutsideBuildHeight(pos)) {
                return false;
            }
            BlockState old = getBlockState(pos);
            if (old == state) {
                return false;
            }
            overlay.put(pos.immutable(), state);
            return true;
        }

        public boolean setBlockAndUpdate(BlockPos pos, BlockState state) {
            return setBlock(pos, state, 3);
        }

        public boolean removeBlock(BlockPos pos, boolean movedByPiston) {
            return setBlock(pos, getFluidState(pos).createLegacyBlock(), 3);
        }

        public boolean destroyBlock(BlockPos pos, boolean dropResources, @Nullable Entity breaker, int updateLimit) {
            if (getBlockState(pos).isAir()) {
                return false;
            }
            return setBlock(pos, getFluidState(pos).createLegacyBlock(), 3);
        }
    }
}
