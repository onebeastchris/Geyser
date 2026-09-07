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
 * A lightweight, hierarchical performance profiler, inspired by Mojang's {@code ProfilerFiller}
 * (the source of the F3 debug pie chart) but adapted to Geyser's proxy architecture.
 *
 * <h2>Design</h2>
 * <ul>
 *     <li><b>Session-scoped, not thread-scoped.</b> Geyser pins many sessions onto a small,
 *     shared pool of "Geyser player thread" event loops, so a {@link ThreadLocal} profiler would
 *     conflate sessions that happen to share a thread. Each {@code GeyserSession} therefore owns
 *     one {@code Profiler}, which is only ever touched from that session's tick event loop.</li>
 *
 *     <li><b>Zero-cost when disabled.</b> The default implementation is {@link NoOpProfiler},
 *     a singleton whose methods JIT away to nothing. Call sites on hot paths must still avoid
 *     building span names eagerly - guard with {@link #isActive()} or pass constant names.</li>
 *
 *     <li><b>Exception-safe scopes.</b> Translators throw and the dispatcher swallows, so raw
 *     {@link #push}/{@link #pop} pairs are easy to unbalance. Prefer {@link #zone(String)} with
 *     try-with-resources, or a {@code try/finally} around {@code push}/{@code pop}.</li>
 *
 *     <li><b>Queue latency, not just CPU.</b> Much of Geyser's real latency is time spent waiting
 *     in a queue during a thread hand-off (RakNet thread &rarr; tick loop, tick loop &rarr; RakNet
 *     flush), which a pure method timer never sees. {@link #recordQueueWait(String, long)} captures
 *     that dwell time so "where did this packet spend its milliseconds" can be answered honestly.</li>
 * </ul>
 *
 * <p>Implementations are <b>not</b> thread-safe: all mutating calls must happen on the owning
 * session's event loop thread. {@link #results()} takes a snapshot that is safe to read elsewhere.
 */
public interface Profiler {

    /**
     * @return {@code true} if this profiler is collecting. When {@code false}, callers should skip
     *         any work needed only to build span names or counters (e.g. string concatenation).
     */
    boolean isActive();

    /**
     * Opens a new named span as a child of the currently open span (or a root span if none is open).
     * Must be balanced by a matching {@link #pop()}.
     *
     * @param name a stable, low-cardinality name (e.g. a packet or operation type - never a per-entity id)
     */
    void push(String name);

    /**
     * Closes the most recently opened span, accumulating its elapsed time.
     * A {@code pop()} with no open span is recorded as an imbalance rather than throwing, so a stray
     * pop can never crash a packet handler.
     */
    void pop();

    /**
     * Convenience for {@code pop()} immediately followed by {@code push(name)} - closes the current
     * sibling span and opens the next at the same depth.
     */
    void popPush(String name);

    /**
     * Opens a span for use with try-with-resources; {@link Zone#close()} calls {@link #pop()}.
     * Because spans are strictly nested on a single thread, the returned handle need not be unique -
     * implementations may return a shared, allocation-free closer.
     *
     * <pre>{@code
     * try (Profiler.Zone ignored = session.profiler().zone("item_translate")) {
     *     // ... work ...
     * }
     * }</pre>
     */
    Zone zone(String name);

    /**
     * Adds to a named counter (bytes serialized, sub-operations performed, ...), attributed to the
     * currently open span. Counters are cheap, cumulative, and reset with the profiler.
     */
    void incrementCounter(String name, long amount);

    /**
     * Records the time a task spent waiting in a queue before running, for a named hand-off point.
     * Call this on the <i>consuming</i> thread, passing {@code System.nanoTime() - enqueuedAt}.
     * This is how cross-thread dwell time (the dominant, otherwise-invisible latency source) is captured.
     */
    void recordQueueWait(String name, long waitNanos);

    /**
     * @return an immutable snapshot of everything collected so far, safe to read off-thread.
     */
    ProfileResults results();

    /**
     * An open span that closes via {@link #pop()}. Intended purely for try-with-resources; do not
     * store, share across threads, or close out of order.
     */
    interface Zone extends AutoCloseable {
        @Override
        void close();
    }
}
