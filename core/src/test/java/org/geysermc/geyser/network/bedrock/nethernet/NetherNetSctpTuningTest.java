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

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NetherNetSctpTuningTest {

    @Test
    void nothingSetKeepsNetworkSettings() {
        assertNull(NetherNetSctpTuning.fromProperties(key -> null));
    }

    @Test
    void overridesAreListedInOrder() {
        Map<String, String> properties = Map.of(
            "Geyser.NetherNetSctp.MinRto", "1000",
            "Geyser.NetherNetSctp.CongestionControl", " 2 ");
        NetherNetSctpTuning.Tuning tuning = NetherNetSctpTuning.fromProperties(properties::get);
        assertEquals("MinRto=1000, CongestionControl=2", tuning.description());
        assertEquals("SctpSettings{recvBufferSize=0, sendBufferSize=0, maxChunksOnQueue=0, initialCongestionWindow=0, maxBurst=0, "
            + "congestionControlModule=2, delayedSackTimeMs=0, minRetransmitTimeoutMs=1000, maxRetransmitTimeoutMs=0, "
            + "initialRetransmitTimeoutMs=0, maxRetransmitAttempts=0, heartbeatIntervalMs=0}", tuning.settings().toString());
    }

    @Test
    void otherOverridesKeepNetworksMinimumRto() {
        NetherNetSctpTuning.Tuning tuning = NetherNetSctpTuning.fromProperties(
            Map.of("Geyser.NetherNetSctp.InitialCwnd", "4")::get);
        assertEquals("SctpSettings{recvBufferSize=0, sendBufferSize=0, maxChunksOnQueue=0, initialCongestionWindow=4, maxBurst=0, "
            + "congestionControlModule=-1, delayedSackTimeMs=0, minRetransmitTimeoutMs=400, maxRetransmitTimeoutMs=0, "
            + "initialRetransmitTimeoutMs=0, maxRetransmitAttempts=0, heartbeatIntervalMs=0}", tuning.settings().toString());
    }

    @Test
    void notANumberIsRefused() {
        assertThrows(IllegalArgumentException.class,
            () -> NetherNetSctpTuning.fromProperties(Map.of("Geyser.NetherNetSctp.MinRto", "1s")::get));
    }
}
