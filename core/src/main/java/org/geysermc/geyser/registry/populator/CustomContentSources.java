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

package org.geysermc.geyser.registry.populator;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.registry.mappings.BuiltInMappings;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Where custom content comes from, so warnings about it name the extension or mappings file to fix.
 */
final class CustomContentSources {

    private CustomContentSources() {
    }

    static String mappingsFile(Path file) {
        return "custom mappings file " + file.getFileName();
    }

    /**
     * @return the extension calling into a custom content event: the first one with a class on the stack
     */
    static String callingExtension() {
        if (BuiltInMappings.isRegistering()) {
            return "Geyser's built-in mappings";
        }
        Map<ClassLoader, String> extensions = new HashMap<>();
        for (Extension extension : GeyserImpl.getInstance().extensionManager().extensions()) {
            extensions.putIfAbsent(extension.getClass().getClassLoader(), extension.name());
        }
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames
                .map(frame -> extensions.get(frame.getDeclaringClass().getClassLoader()))
                .filter(Objects::nonNull)
                .findFirst())
            .map(name -> "extension " + name)
            .orElse("an extension or plugin");
    }

    /**
     * Logs custom content that Paletter rejects because Bedrock Dedicated Server would. It is left out; the rest is
     * registered as usual.
     */
    static void warnRejected(String kind, String identifier, @Nullable String source, RuntimeException exception) {
        GeyserImpl.getInstance().getLogger().warning("Not registering custom " + kind + " " + identifier + " from "
            + (source == null ? "an unknown source" : source) + ", as Bedrock would reject it: " + exception.getMessage());
    }
}
