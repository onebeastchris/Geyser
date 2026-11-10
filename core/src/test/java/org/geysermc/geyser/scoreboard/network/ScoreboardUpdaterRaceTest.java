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

package org.geysermc.geyser.scoreboard.network;

import static org.geysermc.geyser.scoreboard.network.util.GeyserMockContext.mockContext;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import io.netty.channel.EventLoop;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import net.kyori.adventure.text.Component;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.entity.VanillaEntities;
import org.geysermc.geyser.entity.spawn.EntitySpawnContext;
import org.geysermc.geyser.entity.type.player.PlayerEntity;
import org.geysermc.geyser.entity.type.player.SessionPlayerEntity;
import org.geysermc.geyser.scoreboard.Scoreboard;
import org.geysermc.geyser.scoreboard.ScoreboardUpdater;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.session.cache.EntityCache;
import org.geysermc.geyser.session.cache.WorldCache;
import org.geysermc.geyser.session.cache.waypoint.WaypointCache;
import org.geysermc.geyser.translator.protocol.java.entity.JavaSetEntityDataTranslator;
import org.geysermc.geyser.translator.protocol.java.scoreboard.JavaSetDisplayObjectiveTranslator;
import org.geysermc.geyser.translator.protocol.java.scoreboard.JavaSetObjectiveTranslator;
import org.geysermc.geyser.translator.protocol.java.scoreboard.JavaSetScoreTranslator;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.EntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.MetadataTypes;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.IntEntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.scoreboard.ObjectiveAction;
import org.geysermc.mcprotocollib.protocol.data.game.scoreboard.ScoreType;
import org.geysermc.mcprotocollib.protocol.data.game.scoreboard.ScoreboardPosition;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundSetEntityDataPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.scoreboard.ClientboundSetDisplayObjectivePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.scoreboard.ClientboundSetObjectivePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.scoreboard.ClientboundSetScorePacket;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Reproduces the race between the global {@link ScoreboardUpdater} thread and a session's own thread on an
 * entity's metadata (see GeyserMC/Geyser#6893).
 * <p>
 * Once a session receives more scoreboard packets per second than the configured threshold, the score translators
 * stop calling {@link Scoreboard#onUpdate()} themselves and leave it to {@link ScoreboardUpdater}, which calls it
 * from its own thread without handing over to the session's thread. With a below-name objective that update writes
 * the player's {@code SCORE} entity data and calls {@code updateBedrockMetadata()}, while the session's thread is
 * writing the same entity's data for regular entity data packets. The pending entity data is held in a map that is
 * not thread-safe, so the two threads leave it in a state it cannot reach otherwise, which shows up as:
 * <pre>
 * java.lang.ArrayIndexOutOfBoundsException: Index -1 out of bounds for length 33
 *     at it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap.insert
 *     at it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap.put
 *     at org.geysermc.geyser.entity.GeyserEntityDataManager.put
 * </pre>
 * {@link #belowNameUpdateFromUpdaterThreadRacesSessionThread()} fails with that exception for as long as
 * {@link Scoreboard#onUpdate()} can run next to the session's thread. The thread it starts stands in for
 * {@link ScoreboardUpdater} and calls the same method that {@link ScoreboardUpdater#run()} calls, without the waiting
 * in between; a fix that changes how the updater hands its work over has to be reflected in that thread.
 * {@link #belowNameUpdateOnSessionThreadIsSafe()} runs the same workload with the update done on the session's
 * thread, and passes.
 * <p>
 * As these depend on thread timing they are opt-in, to keep the regular test run deterministic. The environment
 * variable is not an input of the test task, hence the {@code --rerun}:
 * <pre>
 * GEYSER_RACE_REPRO=true ./gradlew :core:test --rerun --tests '*ScoreboardUpdaterRaceTest*'
 * </pre>
 */
@EnabledIfEnvironmentVariable(named = "GEYSER_RACE_REPRO", matches = "true")
public class ScoreboardUpdaterRaceTest {
    /**
     * How often the two threads are started on a fresh session before giving up. How soon they collide depends on
     * the machine; this ends at the first collision, which usually is within the first attempt.
     */
    private static final int RACE_ATTEMPTS = 20;
    private static final int CONTROL_ATTEMPTS = 4;
    private static final int SCORES_PER_ATTEMPT = 50_000;

    private static final int PLAYER_ID = 2;
    private static final String PLAYER_NAME = "player1";
    private static final String OBJECTIVE = "objective";

    @Test
    @Timeout(value = 120, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void belowNameUpdateFromUpdaterThreadRacesSessionThread() {
        assertNoCorruption(false, RACE_ATTEMPTS);
    }

    @Test
    @Timeout(value = 120, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void belowNameUpdateOnSessionThreadIsSafe() {
        assertNoCorruption(true, CONTROL_ATTEMPTS);
    }

    private static void assertNoCorruption(boolean updateOnSessionThread, int attempts) {
        for (int attempt = 1; attempt <= attempts; attempt++) {
            List<Throwable> failures = runAttempt(updateOnSessionThread);
            if (!failures.isEmpty()) {
                fail("Entity data was corrupted while the scoreboard was updated "
                    + (updateOnSessionThread ? "on the session's thread" : "next to the session's thread")
                    + " (attempt " + attempt + " of " + attempts + ", " + failures.size() + " failure(s))",
                    failures.get(0));
            }
        }
    }

    /**
     * @param updateOnSessionThread whether the other thread only asks for the scoreboard update, which the session's
     *                              thread then does itself, instead of doing the update
     */
    private static List<Throwable> runAttempt(boolean updateOnSessionThread) {
        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());

        mockContext(() -> {
            GeyserSession session = createSession();
            Scoreboard scoreboard = session.getWorldCache().getScoreboard();

            PlayerEntity player = createPlayer(session);
            session.getEntityCache().addPlayerEntity(player);
            session.getEntityCache().spawnEntity(player);

            new JavaSetObjectiveTranslator().translate(session, new ClientboundSetObjectivePacket(
                OBJECTIVE,
                ObjectiveAction.ADD,
                Component.text(OBJECTIVE),
                ScoreType.INTEGER,
                null
            ));
            new JavaSetDisplayObjectiveTranslator().translate(session,
                new ClientboundSetDisplayObjectivePacket(ScoreboardPosition.BELOW_NAME, OBJECTIVE));

            AtomicBoolean stop = new AtomicBoolean();
            AtomicBoolean updateRequested = new AtomicBoolean();
            AtomicInteger scoresSent = new AtomicInteger();

            // What ScoreboardUpdater#run does for every session above the threshold. It waits for a new score
            // instead of for a quarter of a second, so that every update has something to do
            Thread updater = new Thread(() -> {
                int scoresSeen = 0;
                while (!stop.get()) {
                    int sent = scoresSent.get();
                    if (sent == scoresSeen) {
                        Thread.onSpinWait();
                        continue;
                    }
                    scoresSeen = sent;

                    if (updateOnSessionThread) {
                        updateRequested.set(true);
                        continue;
                    }

                    try {
                        scoreboard.onUpdate();
                    } catch (Throwable throwable) {
                        failures.add(throwable);
                        return;
                    }
                }
            }, "Scoreboard Updater (test)");
            updater.setDaemon(true);
            updater.start();

            // What the session's thread does: a new score for the player, and entity data for that same player.
            // The score translator updates the scoreboard itself until the session has received more scoreboard
            // packets than the threshold, which is the case after the first few hundred, and from then on leaves
            // it to ScoreboardUpdater
            var setScoreTranslator = new JavaSetScoreTranslator();
            var setEntityDataTranslator = new JavaSetEntityDataTranslator();
            try {
                for (int i = 0; failures.isEmpty() && i < SCORES_PER_ATTEMPT; i++) {
                    setScoreTranslator.translate(session, new ClientboundSetScorePacket(PLAYER_NAME, OBJECTIVE, i));
                    scoresSent.incrementAndGet();

                    setEntityDataTranslator.translate(session, new ClientboundSetEntityDataPacket(PLAYER_ID,
                        new EntityMetadata[]{
                            // air
                            new IntEntityMetadata(1, MetadataTypes.INT, i % 300),
                            // ticks frozen
                            new IntEntityMetadata(7, MetadataTypes.INT, i % 140)
                        }
                    ));

                    if (updateRequested.compareAndSet(true, false)) {
                        scoreboard.onUpdate();
                    }
                }
            } catch (Throwable throwable) {
                failures.add(throwable);
            }

            stop.set(true);
            try {
                updater.join(TimeUnit.SECONDS.toMillis(5));
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
            if (updater.isAlive()) {
                // The corrupted map can also leave a thread iterating it forever
                var stuck = new IllegalStateException("The scoreboard updater thread did not finish");
                stuck.setStackTrace(updater.getStackTrace());
                failures.add(stuck);
            }
        });

        return failures;
    }

    /**
     * A session like the one of GeyserMockContextScoreboard, but with a world cache that counts its scoreboard
     * packets, and with mocks that do not keep a record of what is called on them: here that is a lot, from two
     * threads.
     */
    private static GeyserSession createSession() {
        // Resolved first: it is mocked too, and can therefore not be called while stubbing something else
        GeyserImpl geyser = GeyserImpl.getInstance();

        GeyserSession session = mock(GeyserSession.class, withSettings().stubOnly());
        when(session.getGeyser()).thenReturn(geyser);
        when(session.locale()).thenReturn("en_US");

        // As in the other scoreboard tests, everything counts as the session's thread
        EventLoop eventLoop = mock(EventLoop.class, withSettings().stubOnly());
        when(eventLoop.inEventLoop()).thenReturn(true);
        when(session.getTickEventLoop()).thenReturn(eventLoop);

        SessionPlayerEntity self = mock(SessionPlayerEntity.class, withSettings().stubOnly());
        when(self.geyserId()).thenReturn(1L);
        when(self.getUsername()).thenReturn("Tim203");
        when(session.getPlayerEntity()).thenReturn(self);

        EntityCache entityCache = new EntityCache(session);
        when(session.getEntityCache()).thenReturn(entityCache);

        WorldCache worldCache = new WorldCache(session);
        when(session.getWorldCache()).thenReturn(worldCache);

        WaypointCache waypointCache = mock(WaypointCache.class, withSettings().stubOnly());
        when(session.getWaypointCache()).thenReturn(waypointCache);

        return session;
    }

    private static PlayerEntity createPlayer(GeyserSession session) {
        EntitySpawnContext context = EntitySpawnContext.DUMMY_CONTEXT.apply(session, UUID.randomUUID(), VanillaEntities.PLAYER);
        context.geyserId((long) PLAYER_ID);
        context.javaId(PLAYER_ID);
        return new PlayerEntity(context, PLAYER_NAME, null);
    }
}
