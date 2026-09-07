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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongSupplier;

/**
 * The aggregating {@link Profiler}: builds a tree of named spans with call counts and self/total time,
 * plus counters and thread hand-off dwell times. Feeds an internal report (the pie-chart-style
 * {@link ProfileResults#dump()}); the same push/pop boundaries are the natural place to later attach a
 * sampling backend's dynamic labels (async-profiler / Pyroscope).
 *
 * <p><b>Threading:</b> not thread-safe by design. All mutating calls must run on the owning session's
 * event loop thread. {@link #results()} copies its state so the returned snapshot can be read off-thread.
 *
 * <p>The clock is injectable so timing behaviour can be unit-tested deterministically.
 */
public final class ActiveProfiler implements Profiler {
    private final LongSupplier nanoClock;
    private final long startNanos;

    private final Deque<Frame> stack = new ArrayDeque<>();
    private final Map<String, PathData> paths = new LinkedHashMap<>();
    private final Map<String, long[]> counters = new LinkedHashMap<>();
    private final Map<String, WaitData> queueWaits = new LinkedHashMap<>();

    /** Reused across every {@link #zone(String)} - spans are strictly nested on one thread, so close() just pops. */
    private final Zone popZone = this::pop;

    private long imbalancedPops;

    public ActiveProfiler() {
        this(System::nanoTime);
    }

    public ActiveProfiler(LongSupplier nanoClock) {
        this.nanoClock = nanoClock;
        this.startNanos = nanoClock.getAsLong();
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void push(String name) {
        String parentPath = stack.isEmpty() ? null : stack.peek().path;
        String path = parentPath == null ? name : parentPath + '/' + name;
        stack.push(new Frame(path, nanoClock.getAsLong()));
    }

    @Override
    public void pop() {
        Frame frame = stack.poll();
        if (frame == null) {
            // A stray pop must never crash a packet handler; record it and move on.
            imbalancedPops++;
            return;
        }
        long elapsed = nanoClock.getAsLong() - frame.startNanos;
        if (elapsed < 0) {
            elapsed = 0;
        }

        PathData data = paths.computeIfAbsent(frame.path, PathData::new);
        data.count++;
        data.totalNanos += elapsed;
        data.childNanos += frame.childNanos;
        if (elapsed > data.maxNanos) {
            data.maxNanos = elapsed;
        }

        Frame parent = stack.peek();
        if (parent != null) {
            parent.childNanos += elapsed;
        }
    }

    @Override
    public void popPush(String name) {
        pop();
        push(name);
    }

    @Override
    public Zone zone(String name) {
        push(name);
        return popZone;
    }

    @Override
    public void incrementCounter(String name, long amount) {
        counters.computeIfAbsent(name, k -> new long[1])[0] += amount;
    }

    @Override
    public void recordQueueWait(String name, long waitNanos) {
        if (waitNanos < 0) {
            waitNanos = 0;
        }
        WaitData data = queueWaits.computeIfAbsent(name, WaitData::new);
        data.count++;
        data.totalNanos += waitNanos;
        if (waitNanos > data.maxNanos) {
            data.maxNanos = waitNanos;
        }
    }

    /** @return number of {@link #pop()} calls that had no matching {@link #push(String)} - should be 0 in balanced use. */
    public long imbalancedPops() {
        return imbalancedPops;
    }

    /** @return the depth of the currently open span stack (0 when nothing is open). */
    public int openDepth() {
        return stack.size();
    }

    @Override
    public ProfileResults results() {
        List<ProfileResults.Entry> entries = new ArrayList<>(paths.size());
        for (PathData data : paths.values()) {
            entries.add(new ProfileResults.Entry(
                data.path,
                data.count,
                data.totalNanos,
                Math.max(0, data.totalNanos - data.childNanos),
                data.maxNanos
            ));
        }
        // Order by full path so parents precede their children and siblings group together (pre-order tree).
        // '/' (0x2F) sorts before digits and letters, so a parent path always precedes its descendants.
        entries.sort(Comparator.comparing(ProfileResults.Entry::path));

        List<ProfileResults.Counter> counterList = new ArrayList<>(counters.size());
        counters.forEach((name, value) -> counterList.add(new ProfileResults.Counter(name, value[0])));

        List<ProfileResults.QueueWait> waitList = new ArrayList<>(queueWaits.size());
        for (WaitData data : queueWaits.values()) {
            waitList.add(new ProfileResults.QueueWait(data.name, data.count, data.totalNanos, data.maxNanos));
        }

        long wall = nanoClock.getAsLong() - startNanos;
        return new ProfileResults(entries, counterList, waitList, wall);
    }

    private static final class Frame {
        final String path;
        final long startNanos;
        long childNanos;

        Frame(String path, long startNanos) {
            this.path = path;
            this.startNanos = startNanos;
        }
    }

    private static final class PathData {
        final String path;
        long count;
        long totalNanos;
        long childNanos;
        long maxNanos;

        PathData(String path) {
            this.path = path;
        }
    }

    private static final class WaitData {
        final String name;
        long count;
        long totalNanos;
        long maxNanos;

        WaitData(String name) {
            this.name = name;
        }
    }
}
