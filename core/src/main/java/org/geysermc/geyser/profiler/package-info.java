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

/**
 * A prototype hierarchical performance profiler for Geyser (Mojang-{@code ProfilerFiller}-style push/pop,
 * adapted to the proxy's shared-event-loop threading model).
 *
 * <h2>What's here</h2>
 * <ul>
 *     <li>{@link org.geysermc.geyser.profiler.Profiler} - the interface (push/pop, {@code zone}, counters,
 *     {@code recordQueueWait}).</li>
 *     <li>{@link org.geysermc.geyser.profiler.NoOpProfiler} - the free default; every session starts with it.</li>
 *     <li>{@link org.geysermc.geyser.profiler.ActiveProfiler} - aggregating tree implementation with an
 *     injectable clock.</li>
 *     <li>{@link org.geysermc.geyser.profiler.ProfileResults} - immutable snapshot + {@code dump()} report.</li>
 * </ul>
 *
 * <h2>Where it's wired (prototype)</h2>
 * <ul>
 *     <li>{@code GeyserSession#profiler()} holds one profiler per session (default {@code NoOpProfiler}).</li>
 *     <li>{@code PacketTranslatorRegistry#translate0} opens a root span per packet, and records queue-wait
 *     at the RakNet&rarr;tick-loop hand-off.</li>
 *     <li>{@code GeyserItemStack#getItemData} opens a nested {@code item_translate} span - an example of
 *     instrumenting a hot sub-operation.</li>
 * </ul>
 *
 * <h2>Enabling it</h2>
 * Swap the session's profiler on its event loop, snapshot, then restore the no-op:
 * <pre>{@code
 * session.execute(() -> session.profiler(new ActiveProfiler()));
 * // ... let traffic flow ...
 * session.execute(() -> {
 *     System.out.println(session.profiler().results().dump());
 *     session.profiler(NoOpProfiler.INSTANCE);
 * });
 * }</pre>
 * A {@code /geyser profile <player> <seconds>} command is the intended real trigger; it is out of scope
 * for this prototype.
 *
 * <h2>Not yet done (by design)</h2>
 * Outbound (tick-loop&rarr;RakNet-flush) queue-wait, a command, config/gating, and a sampling backend
 * (async-profiler / Pyroscope) hung off the same push/pop boundaries via dynamic labels.
 */
package org.geysermc.geyser.profiler;
