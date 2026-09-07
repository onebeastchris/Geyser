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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * An immutable snapshot of profiler data: per-path timing spans, counters, and queue-wait statistics.
 * Safe to read from any thread once produced by {@link Profiler#results()}.
 */
public final class ProfileResults {
    public static final ProfileResults EMPTY =
        new ProfileResults(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), 0L);

    private final List<Entry> entries;
    private final List<Counter> counters;
    private final List<QueueWait> queueWaits;
    private final long wallNanos;

    ProfileResults(List<Entry> entries, List<Counter> counters, List<QueueWait> queueWaits, long wallNanos) {
        this.entries = entries;
        this.counters = counters;
        this.queueWaits = queueWaits;
        this.wallNanos = wallNanos;
    }

    /** @return every recorded span, ordered by full path (parents before children). */
    public List<Entry> entries() {
        return entries;
    }

    /** @return the span for a given full path (e.g. {@code "ClientboundLevelChunk/item_translate"}), or {@code null}. */
    public Entry entry(String path) {
        for (Entry e : entries) {
            if (e.path().equals(path)) {
                return e;
            }
        }
        return null;
    }

    /** @return up to {@code limit} spans with the highest <i>self</i> time (time in the span itself, excluding children). */
    public List<Entry> hotSpots(int limit) {
        List<Entry> copy = new ArrayList<>(entries);
        copy.sort(Comparator.comparingLong(Entry::selfNanos).reversed());
        return copy.subList(0, Math.min(limit, copy.size()));
    }

    public List<Counter> counters() {
        return counters;
    }

    public long counter(String name) {
        for (Counter c : counters) {
            if (c.name().equals(name)) {
                return c.value();
            }
        }
        return 0L;
    }

    public List<QueueWait> queueWaits() {
        return queueWaits;
    }

    public QueueWait queueWait(String name) {
        for (QueueWait q : queueWaits) {
            if (q.name().equals(name)) {
                return q;
            }
        }
        return null;
    }

    /** @return total wall-clock time the profiler has been collecting, in nanoseconds. */
    public long wallNanos() {
        return wallNanos;
    }

    /**
     * Renders a human-readable, pie-chart-style report: an indented span tree (each line showing its
     * share of its parent and of the whole), the top self-time hot spots, queue-wait stats, and counters.
     * This is what a {@code /geyser profile} command would print.
     */
    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Geyser profiler results (").append(formatNanos(wallNanos)).append(" wall) ===\n");

        sb.append("\n-- Span tree (self%, of parent) --\n");
        for (Entry e : entries) {
            int depth = 0;
            for (int i = 0; i < e.path().length(); i++) {
                if (e.path().charAt(i) == '/') {
                    depth++;
                }
            }
            long parentTotal = parentTotalNanos(e);
            double ofParent = parentTotal > 0 ? (100.0 * e.totalNanos() / parentTotal) : 100.0;
            sb.append("  ".repeat(depth + 1))
                .append(leaf(e.path()))
                .append(" x").append(e.count())
                .append(" total=").append(formatNanos(e.totalNanos()))
                .append(" self=").append(formatNanos(e.selfNanos()))
                .append(String.format(" (%.1f%% of parent)", ofParent))
                .append('\n');
        }

        sb.append("\n-- Top self-time hot spots --\n");
        for (Entry e : hotSpots(10)) {
            sb.append("  ").append(e.path())
                .append("  self=").append(formatNanos(e.selfNanos()))
                .append(" avg=").append(formatNanos(e.count() == 0 ? 0 : e.selfNanos() / e.count()))
                .append(" max=").append(formatNanos(e.maxNanos()))
                .append('\n');
        }

        if (!queueWaits.isEmpty()) {
            sb.append("\n-- Queue wait (thread hand-off dwell time) --\n");
            for (QueueWait q : queueWaits) {
                sb.append("  ").append(q.name())
                    .append(" x").append(q.count())
                    .append(" avg=").append(formatNanos(q.averageNanos()))
                    .append(" max=").append(formatNanos(q.maxNanos()))
                    .append('\n');
            }
        }

        if (!counters.isEmpty()) {
            sb.append("\n-- Counters --\n");
            for (Counter c : counters) {
                sb.append("  ").append(c.name()).append(" = ").append(c.value()).append('\n');
            }
        }
        return sb.toString();
    }

    private long parentTotalNanos(Entry e) {
        int slash = e.path().lastIndexOf('/');
        if (slash < 0) {
            return wallNanos;
        }
        Entry parent = entry(e.path().substring(0, slash));
        return parent != null ? parent.totalNanos() : wallNanos;
    }

    private static String leaf(String path) {
        int slash = path.lastIndexOf('/');
        return slash < 0 ? path : path.substring(slash + 1);
    }

    static String formatNanos(long nanos) {
        if (nanos < 1_000L) {
            return nanos + "ns";
        }
        if (nanos < 1_000_000L) {
            return String.format("%.1fus", nanos / 1_000.0);
        }
        if (nanos < 1_000_000_000L) {
            return String.format("%.2fms", nanos / 1_000_000.0);
        }
        return String.format("%.2fs", nanos / 1_000_000_000.0);
    }

    /**
     * A single named span, aggregated over every time it was entered.
     *
     * @param path       full slash-delimited path from the root span
     * @param count      number of times the span was entered
     * @param totalNanos wall time spent inside the span, including children
     * @param selfNanos  wall time spent in the span itself, excluding children
     * @param maxNanos   the single slowest entry
     */
    public record Entry(String path, long count, long totalNanos, long selfNanos, long maxNanos) {
        public long averageNanos() {
            return count == 0 ? 0 : totalNanos / count;
        }
    }

    public record Counter(String name, long value) {
    }

    /**
     * Aggregated dwell time for one thread hand-off point.
     *
     * @param name       hand-off identifier (e.g. {@code "inbound-handoff"})
     * @param count      number of hand-offs observed
     * @param totalNanos summed wait time
     * @param maxNanos   worst observed wait
     */
    public record QueueWait(String name, long count, long totalNanos, long maxNanos) {
        public long averageNanos() {
            return count == 0 ? 0 : totalNanos / count;
        }
    }
}
