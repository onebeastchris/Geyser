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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetherNetNativeInfoTest {

    @Test
    void knownHashesAreNamed() {
        assertTrue(NetherNetNativeInfo.identify("c6998048e2347a36dc93e188b435cc5d08be1aec648758f1b99be1fe0042a2cd")
            .startsWith(NetherNetNativeInfo.STOCK));
        assertEquals("unknown build, neither stock nor the T3-rtx fix build", NetherNetNativeInfo.identify("00"));
    }

    @Test
    void everyPlatformIsKnownForBothBuilds() {
        long stock = NetherNetNativeInfo.KNOWN.values().stream().filter(build -> build.startsWith(NetherNetNativeInfo.STOCK)).count();
        long fixed = NetherNetNativeInfo.KNOWN.values().stream().filter(build -> build.startsWith(NetherNetNativeInfo.FIXED)).count();
        assertEquals(8, stock);
        assertEquals(8, fixed);
    }
}
