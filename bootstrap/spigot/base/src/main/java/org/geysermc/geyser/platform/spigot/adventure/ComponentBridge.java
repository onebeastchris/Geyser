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

package org.geysermc.geyser.platform.spigot.adventure;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Converts between the Adventure components Geyser uses internally and whatever component representation
 * the server understands. Obtain the implementation matching the running server via {@link SpigotAdventure#bridge()}.
 * <p>
 * Server components are typed as {@link Object}: in {@link PaperComponentBridge} mode they belong to a different
 * Adventure version loaded by the server classloader, so they must never appear in method signatures as
 * {@code Component} — doing so would bind the name to the wrong class or fail linking entirely.
 */
public interface ComponentBridge {

    /**
     * Whether the server API accepts Adventure components (Paper). If false (Spigot), messages are
     * sent as BungeeCord chat components and {@link #toServerComponent(Component)} is unavailable.
     */
    boolean supportsServerComponents();

    /**
     * Converts one of Geyser's components into an instance of the server's component class.
     *
     * @return the server component, or null when unsupported or conversion failed
     */
    @Nullable Object toServerComponent(Component component);

    /**
     * Serializes an instance of the server's component class into JSON.
     *
     * @param serverComponent a component instance of the server's Adventure, e.g. read from a ping event
     * @return the JSON representation, or null when unsupported or serialization failed
     */
    @Nullable String serializeServerComponent(Object serverComponent);

    /**
     * Sends one of Geyser's components to a command sender, converting as needed.
     */
    void sendMessage(CommandSender sender, Component component);
}
