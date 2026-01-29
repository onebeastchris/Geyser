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

package org.geysermc.geyser.util.diagnostics;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.geyser.GeyserImpl;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DiagnosticUtils {

    private static final Path DIAGNOSTICS_FOLDER = GeyserImpl.getInstance().configDirectory().resolve("diagnostics");

    public static @Nullable Path createHeapDump() {
        String name = "heap-dump-" + DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss").format(LocalDateTime.now());
        GeyserImpl.getInstance().getLogger().info("Writing heap dump...");

        try {
            Files.createDirectories(DIAGNOSTICS_FOLDER);
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            Path file;
            try {
                Class<?> clazz = Class.forName("openj9.lang.management.OpenJ9DiagnosticsMXBean");
                Object openj9Mbean = ManagementFactory.newPlatformMXBeanProxy(server, "openj9.lang.management:type=OpenJ9Diagnostics", clazz);
                Method m = clazz.getMethod("triggerDumpToFile", String.class, String.class);
                file = DIAGNOSTICS_FOLDER.resolve(name + ".phd");
                m.invoke(openj9Mbean, "heap", file.toString());
            } catch (ClassNotFoundException e) {
                Class<?> clazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
                Object hotspotMBean = ManagementFactory.newPlatformMXBeanProxy(server, "com.sun.management:type=HotSpotDiagnostic", clazz);
                Method m = clazz.getMethod("dumpHeap", String.class, boolean.class);
                file = DIAGNOSTICS_FOLDER.resolve(name + ".hprof");
                m.invoke(hotspotMBean, file.toString(), true);
            }

            Path path = GeyserImpl.getInstance().configDirectory().relativize(file);
            GeyserImpl.getInstance().getLogger().info("Heap dump written to %s!".formatted(path));
            return path;
        } catch (Throwable t) {
            GeyserImpl.getInstance().getLogger().error("Failed to create heap dump", t);
            return null;
        }
    }
}
