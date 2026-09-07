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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfilerTest {

    /** A manually-advanced nanosecond clock so timing assertions are exact and deterministic. */
    private static final class FakeClock implements LongSupplier {
        private long now;

        @Override
        public long getAsLong() {
            return now;
        }

        void advance(long nanos) {
            now += nanos;
        }
    }

    @Nested
    @DisplayName("NoOpProfiler")
    class NoOp {

        @Test
        @DisplayName("is inert and allocation-free but never throws")
        void inert() {
            Profiler p = NoOpProfiler.INSTANCE;
            assertFalse(p.isActive());

            // None of these should throw or record anything, even wildly unbalanced.
            p.push("a");
            p.pop();
            p.pop();
            p.popPush("b");
            p.incrementCounter("c", 5);
            p.recordQueueWait("q", 100);

            assertSame(ProfileResults.EMPTY, p.results());
            assertTrue(p.results().entries().isEmpty());
        }

        @Test
        @DisplayName("zone is reusable and safe in try-with-resources")
        void zone() {
            Profiler p = NoOpProfiler.INSTANCE;
            try (Profiler.Zone z = p.zone("x")) {
                assertNotNull(z);
            }
            assertTrue(p.results().entries().isEmpty());
        }
    }

    @Nested
    @DisplayName("ActiveProfiler timing")
    class Timing {

        @Test
        @DisplayName("records count, total and max for a single span")
        void singleSpan() {
            FakeClock clock = new FakeClock();
            ActiveProfiler p = new ActiveProfiler(clock);

            p.push("translate");
            clock.advance(40);
            p.pop();

            ProfileResults.Entry e = p.results().entry("translate");
            assertNotNull(e);
            assertEquals(1, e.count());
            assertEquals(40, e.totalNanos());
            assertEquals(40, e.selfNanos());
            assertEquals(40, e.maxNanos());
        }

        @Test
        @DisplayName("separates self time from child time in a nested tree")
        void nestedSelfVsTotal() {
            FakeClock clock = new FakeClock();
            ActiveProfiler p = new ActiveProfiler(clock);

            p.push("packet");          // t=0
            clock.advance(10);         // 10ns of packet's own work
            p.push("item_translate");  // t=10
            clock.advance(30);         // 30ns inside the child
            p.pop();                   // t=40, close child
            clock.advance(5);          // 5ns more of packet's own work
            p.pop();                   // t=45, close packet

            ProfileResults results = p.results();
            ProfileResults.Entry packet = results.entry("packet");
            ProfileResults.Entry child = results.entry("packet/item_translate");

            assertNotNull(packet);
            assertNotNull(child);
            // packet ran for 45ns total, of which 30ns was the child -> 15ns self.
            assertEquals(45, packet.totalNanos());
            assertEquals(15, packet.selfNanos());
            // child is a leaf: all of its time is self time.
            assertEquals(30, child.totalNanos());
            assertEquals(30, child.selfNanos());
        }

        @Test
        @DisplayName("aggregates repeated entries of the same path")
        void aggregatesRepeats() {
            FakeClock clock = new FakeClock();
            ActiveProfiler p = new ActiveProfiler(clock);

            p.push("op");
            clock.advance(10);
            p.pop();

            p.push("op");
            clock.advance(30);
            p.pop();

            ProfileResults.Entry e = p.results().entry("op");
            assertEquals(2, e.count());
            assertEquals(40, e.totalNanos());
            assertEquals(20, e.averageNanos());
            assertEquals(30, e.maxNanos(), "max should track the slowest single entry");
        }

        @Test
        @DisplayName("popPush closes one sibling and opens the next at the same depth")
        void popPushSiblings() {
            FakeClock clock = new FakeClock();
            ActiveProfiler p = new ActiveProfiler(clock);

            p.push("root");
            p.push("a");
            clock.advance(10);
            p.popPush("b");
            clock.advance(20);
            p.pop();  // close b
            p.pop();  // close root

            ProfileResults results = p.results();
            assertEquals(10, results.entry("root/a").totalNanos());
            assertEquals(20, results.entry("root/b").totalNanos());
            assertEquals(0, p.openDepth());
        }
    }

    @Nested
    @DisplayName("Exception safety")
    class Safety {

        @Test
        @DisplayName("zone still pops when the body throws")
        void zonePopsOnThrow() {
            FakeClock clock = new FakeClock();
            ActiveProfiler p = new ActiveProfiler(clock);

            assertThrows(RuntimeException.class, () -> {
                try (Profiler.Zone ignored = p.zone("risky")) {
                    clock.advance(12);
                    throw new RuntimeException("boom");
                }
            });

            assertEquals(0, p.openDepth(), "stack must be unwound after an exception");
            assertEquals(12, p.results().entry("risky").totalNanos());
        }

        @Test
        @DisplayName("a stray pop is recorded, not thrown")
        void strayPopIsSafe() {
            FakeClock clock = new FakeClock();
            ActiveProfiler p = new ActiveProfiler(clock);

            p.pop();
            p.pop();

            assertEquals(2, p.imbalancedPops());
            assertTrue(p.results().entries().isEmpty());
        }
    }

    @Nested
    @DisplayName("Counters and queue-wait")
    class CountersAndLatency {

        @Test
        @DisplayName("counters accumulate")
        void counters() {
            ActiveProfiler p = new ActiveProfiler(new FakeClock());
            p.incrementCounter("chunk_sections", 16);
            p.incrementCounter("chunk_sections", 8);
            p.incrementCounter("block_entities", 3);

            ProfileResults results = p.results();
            assertEquals(24, results.counter("chunk_sections"));
            assertEquals(3, results.counter("block_entities"));
            assertEquals(0, results.counter("absent"));
        }

        @Test
        @DisplayName("queue wait tracks count, average and max dwell time")
        void queueWait() {
            ActiveProfiler p = new ActiveProfiler(new FakeClock());
            p.recordQueueWait("inbound-handoff", 100);
            p.recordQueueWait("inbound-handoff", 300);
            p.recordQueueWait("inbound-handoff", 200);

            ProfileResults.QueueWait q = p.results().queueWait("inbound-handoff");
            assertNotNull(q);
            assertEquals(3, q.count());
            assertEquals(600, q.totalNanos());
            assertEquals(200, q.averageNanos());
            assertEquals(300, q.maxNanos());
        }
    }

    @Nested
    @DisplayName("ProfileResults reporting")
    class Reporting {

        @Test
        @DisplayName("hotSpots ranks by self time, descending")
        void hotSpots() {
            FakeClock clock = new FakeClock();
            ActiveProfiler p = new ActiveProfiler(clock);

            span(p, clock, "cheap", 5);
            span(p, clock, "expensive", 100);
            span(p, clock, "medium", 40);

            var top = p.results().hotSpots(2);
            assertEquals(2, top.size());
            assertEquals("expensive", top.get(0).path());
            assertEquals("medium", top.get(1).path());
        }

        @Test
        @DisplayName("dump renders a non-empty, readable report")
        void dumpSmoke() {
            FakeClock clock = new FakeClock();
            ActiveProfiler p = new ActiveProfiler(clock);

            p.push("ClientboundLevelChunk");
            clock.advance(100);
            p.push("block_palette");
            clock.advance(60);
            p.pop();
            p.pop();
            p.incrementCounter("sections", 24);
            p.recordQueueWait("inbound-handoff", 5_000);

            String dump = p.results().dump();
            assertTrue(dump.contains("ClientboundLevelChunk"));
            assertTrue(dump.contains("block_palette"));
            assertTrue(dump.contains("inbound-handoff"));
            assertTrue(dump.contains("sections"));
        }

        @Test
        @DisplayName("entries are ordered parents-before-children (pre-order tree)")
        void entriesPreOrder() {
            FakeClock clock = new FakeClock();
            ActiveProfiler p = new ActiveProfiler(clock);

            p.push("root");
            p.push("b_child");
            clock.advance(1);
            p.pop();
            p.push("a_child");
            clock.advance(1);
            p.pop();
            p.pop();

            var paths = p.results().entries().stream().map(ProfileResults.Entry::path).toList();
            assertEquals("root", paths.get(0), "parent must precede its children");
            assertTrue(paths.indexOf("root") < paths.indexOf("root/a_child"));
            assertTrue(paths.indexOf("root") < paths.indexOf("root/b_child"));
            assertTrue(paths.indexOf("root/a_child") < paths.indexOf("root/b_child"), "siblings sort by name");
        }

        @Test
        @DisplayName("unknown lookups return null/zero rather than throwing")
        void absentLookups() {
            ProfileResults empty = ProfileResults.EMPTY;
            assertNull(empty.entry("nope"));
            assertNull(empty.queueWait("nope"));
            assertEquals(0, empty.counter("nope"));
            assertTrue(empty.hotSpots(5).isEmpty());
        }

        private void span(ActiveProfiler p, FakeClock clock, String name, long nanos) {
            p.push(name);
            clock.advance(nanos);
            p.pop();
        }
    }
}
