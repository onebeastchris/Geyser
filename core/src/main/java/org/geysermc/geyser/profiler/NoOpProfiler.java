/*
 * Copyright (c) 2019-2024 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.profiler;

/**
 * The default {@link Profiler}: does nothing, allocates nothing, and is safe to call from any thread.
 *
 * <p>Every method is trivially inlinable so that, when profiling is off (the normal case), the
 * instrumentation scattered across hot paths compiles down to effectively nothing. {@link #zone(String)}
 * returns a shared no-op closer, so even try-with-resources instrumentation is allocation-free.
 */
public final class NoOpProfiler implements Profiler {
    public static final NoOpProfiler INSTANCE = new NoOpProfiler();

    private static final Zone NOOP_ZONE = () -> { };

    private NoOpProfiler() {
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void push(String name) {
    }

    @Override
    public void pop() {
    }

    @Override
    public void popPush(String name) {
    }

    @Override
    public Zone zone(String name) {
        return NOOP_ZONE;
    }

    @Override
    public void incrementCounter(String name, long amount) {
    }

    @Override
    public void recordQueueWait(String name, long waitNanos) {
    }

    @Override
    public ProfileResults results() {
        return ProfileResults.EMPTY;
    }
}
