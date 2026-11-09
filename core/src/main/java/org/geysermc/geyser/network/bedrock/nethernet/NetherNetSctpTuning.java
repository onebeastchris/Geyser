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

package org.geysermc.geyser.network.bedrock.nethernet;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.netty.channel.nethernet.NetherNetNative;
import org.geysermc.geyser.GeyserLogger;
import tel.schich.libdatachannel.LibDataChannel;
import tel.schich.libdatachannel.SctpSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Debug-only SCTP overrides, so testers can compare settings without a new build, for example
 * {@code -DGeyser.NetherNetSctp.MinRto=1000}. Anything not set keeps what Cloudburst Network uses (a 400 ms minimum
 * RTO, libdatachannel's defaults otherwise). The settings are process-wide and only reach connections made afterwards.
 */
public final class NetherNetSctpTuning {
    static final String PREFIX = "Geyser.NetherNetSctp.";
    /** Network's own setting in NetherNetNative, the base everything else is applied to */
    static final int NETWORK_MIN_RTO_MS = 400;

    private static volatile String active = "network defaults";

    private NetherNetSctpTuning() {
    }

    /**
     * Loads the native library with Network's settings first, so a later bind can't reset ours, then applies any overrides.
     */
    public static void apply(GeyserLogger logger) {
        NetherNetNative.initialize();
        Tuning tuning = fromProperties(System::getProperty);
        if (tuning == null) {
            return;
        }
        LibDataChannel.setSctpSettings(tuning.settings());
        active = tuning.description();
        logger.warning("[NetherNet] Overriding SCTP settings for testing: " + active);
    }

    /**
     * @return the overrides in effect, or "network defaults"
     */
    public static String active() {
        return active;
    }

    record Tuning(SctpSettings settings, String description) {
    }

    /**
     * @return the settings with every override applied, or null if no property is set
     * @throws IllegalArgumentException if a property is not a number
     */
    static @Nullable Tuning fromProperties(Function<String, @Nullable String> properties) {
        SctpSettings.Builder builder = SctpSettings.builder().minRetransmitTimeoutMs(NETWORK_MIN_RTO_MS);
        List<String> set = new ArrayList<>();
        Integer value;
        if ((value = read(properties, "MinRto", set)) != null) builder.minRetransmitTimeoutMs(value);
        if ((value = read(properties, "MaxRto", set)) != null) builder.maxRetransmitTimeoutMs(value);
        if ((value = read(properties, "InitialRto", set)) != null) builder.initialRetransmitTimeoutMs(value);
        if ((value = read(properties, "InitialCwnd", set)) != null) builder.initialCongestionWindow(value);
        if ((value = read(properties, "MaxBurst", set)) != null) builder.maxBurst(value);
        if ((value = read(properties, "CongestionControl", set)) != null) builder.congestionControlModule(value);
        if ((value = read(properties, "DelayedSack", set)) != null) builder.delayedSackTimeMs(value);
        if ((value = read(properties, "SendBuffer", set)) != null) builder.sendBufferSize(value);
        if ((value = read(properties, "RecvBuffer", set)) != null) builder.recvBufferSize(value);
        if ((value = read(properties, "MaxRetransmits", set)) != null) builder.maxRetransmitAttempts(value);
        if ((value = read(properties, "Heartbeat", set)) != null) builder.heartbeatIntervalMs(value);
        return set.isEmpty() ? null : new Tuning(builder.build(), String.join(", ", set));
    }

    private static @Nullable Integer read(Function<String, @Nullable String> properties, String name, List<String> set) {
        String raw = properties.apply(PREFIX + name);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            int value = Integer.parseInt(raw.trim());
            set.add(name + "=" + value);
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(PREFIX + name + " must be a whole number, not \"" + raw + "\"", e);
        }
    }
}
