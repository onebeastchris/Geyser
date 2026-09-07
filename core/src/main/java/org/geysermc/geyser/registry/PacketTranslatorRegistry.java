/*
 * Copyright (c) 2019-2022 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.registry;

import org.cloudburstmc.protocol.bedrock.packet.ServerboundDiagnosticsPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundDelimiterPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundTabListPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundChunkBatchStartPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundLightUpdatePacket;
import io.netty.channel.EventLoop;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.erosion.ErosionCancellationException;
import org.geysermc.geyser.profiler.Profiler;
import org.geysermc.geyser.registry.loader.RegistryLoaders;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.text.GeyserLocale;
import org.geysermc.geyser.translator.protocol.PacketTranslator;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class PacketTranslatorRegistry<T> extends AbstractMappedRegistry<Class<? extends T>, PacketTranslator<? extends T>, IdentityHashMap<Class<? extends T>, PacketTranslator<? extends T>>> {
    private static final Set<Class<?>> IGNORED_PACKETS = Collections.newSetFromMap(new IdentityHashMap<>());

    /** Name of the thread hand-off where inbound packets wait to be picked up by the session's tick loop. */
    private static final String INBOUND_HANDOFF = "inbound-handoff";

    /**
     * Caches a stable, low-cardinality span name per packet class so the hot path never builds a
     * name string per packet. {@link ClassValue} is thread-safe and lock-free on read.
     */
    private static final ClassValue<String> PACKET_NAMES = new ClassValue<>() {
        @Override
        protected String computeValue(Class<?> type) {
            return type.getSimpleName();
        }
    };

    static {
        IGNORED_PACKETS.add(ClientboundChunkBatchStartPacket.class); // we don't track chunk batch sizes/periods
        IGNORED_PACKETS.add(ClientboundDelimiterPacket.class); // Not implemented, spams logs
        IGNORED_PACKETS.add(ClientboundLightUpdatePacket.class); // Light is handled on Bedrock for us
        IGNORED_PACKETS.add(ClientboundTabListPacket.class); // Cant be implemented in Bedrock
        IGNORED_PACKETS.add(ServerboundDiagnosticsPacket.class); // spammy
    }

    protected PacketTranslatorRegistry() {
        super(null, RegistryLoaders.empty(IdentityHashMap::new));
    }

    @SuppressWarnings("unchecked")
    public <P extends T> boolean translate(Class<? extends P> clazz, P packet, GeyserSession session, boolean canRunImmediately) {
        if (session.getUpstream().isClosed() || session.isClosed()) {
            return false;
        }

        PacketTranslator<P> translator = (PacketTranslator<P>) this.mappings.get(clazz);
        if (translator != null) {
            EventLoop eventLoop = session.getTickEventLoop();
            if (canRunImmediately || !translator.shouldExecuteInEventLoop() || eventLoop.inEventLoop()) {
                translate0(session, translator, packet);
            } else {
                // This packet is being handed from its inbound thread (e.g. the RakNet thread) to the
                // session's tick loop. Capture the dwell time it spends queued - a latency source a pure
                // method profiler never sees. The isActive() read may race with a toggle; that is benign
                // (worst case a single sample is taken or skipped).
                boolean profiling = session.profiler().isActive();
                long enqueuedAt = profiling ? System.nanoTime() : 0L;
                eventLoop.execute(() -> {
                    if (profiling) {
                        session.profiler().recordQueueWait(INBOUND_HANDOFF, System.nanoTime() - enqueuedAt);
                    }
                    translate0(session, translator, packet);
                });
            }
            return true;
        } else {
            if (GeyserImpl.getInstance().config().debugMode()) {
                if (!IGNORED_PACKETS.contains(clazz)) {
                    GeyserImpl.getInstance().getLogger().debug("Could not find packet for " + (packet.toString().length() > 25 ? packet.getClass().getSimpleName() : packet));
                }
            }

            return false;
        }
    }

    private <P extends T> void translate0(GeyserSession session, PacketTranslator<P> translator, P packet) {
        if (session.isClosed()) {
            return;
        }

        // Time the whole packet translation as a root span. Guarded by isActive() so that, when
        // profiling is off (the norm), neither the name lookup nor the push/pop runs.
        Profiler profiler = session.profiler();
        boolean profiling = profiler.isActive();
        if (profiling) {
            profiler.push(PACKET_NAMES.get(packet.getClass()));
        }
        try {
            translator.translate(session, packet);
        } catch (ErosionCancellationException ex) {
            GeyserImpl.getInstance().getLogger().debug("Caught ErosionCancellationException");
        } catch (Throwable ex) {
            GeyserImpl.getInstance().getLogger().error(GeyserLocale.getLocaleStringLog("geyser.network.translator.packet.failed", packet.getClass().getSimpleName()), ex);
            ex.printStackTrace();
        } finally {
            if (profiling) {
                profiler.pop();
            }
        }
    }

    public static <T> PacketTranslatorRegistry<T> create() {
        return new PacketTranslatorRegistry<>();
    }
}
