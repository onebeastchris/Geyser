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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Picks the {@link ComponentBridge} matching how Adventure was set up for this server.
 * <p>
 * Whether Geyser runs on the server's Adventure (modern Paper) or on the bundled Adventure library is decided
 * by the isolated loader <i>before</i> this classloader existed (see {@code SpigotAdventureDetector} in the
 * isolated module) - by the time any code here runs, every {@code net.kyori} lookup already resolves to the
 * right place. This class only observes the outcome:
 * <ul>
 *     <li>Geyser's {@link Component} is the same class as the server's: components can be passed to the
 *     Paper API directly.</li>
 *     <li>The server has a component API but a different (older) Adventure: components are converted at the
 *     API boundary by serializing to JSON on one side and deserializing on the other.</li>
 *     <li>The server has no component API (Spigot): messages are sent as BungeeCord chat components.</li>
 * </ul>
 */
public final class SpigotAdventure {

    private SpigotAdventure() {
    }

    public static ComponentBridge bridge() {
        return Holder.BRIDGE;
    }

    private static final class Holder {
        private static final ComponentBridge BRIDGE = createBridge();

        private static ComponentBridge createBridge() {
            // The parent of Geyser's classloader is the plugin classloader, which delegates to the server
            ClassLoader serverLoader = SpigotAdventure.class.getClassLoader().getParent();

            Class<?> serverComponent = findClass(serverLoader, "net.kyori.adventure.text.Component");
            // Distinguishes Paper (native component support in the Bukkit API) from Spigot, even if
            // some other plugin leaked an unrelocated Adventure onto the server classpath
            boolean serverHasComponentApi = serverComponent != null
                    && hasMethod(CommandSender.class, "sendMessage", serverComponent);

            if (!serverHasComponentApi) {
                return new BungeeComponentBridge();
            }

            if (serverComponent == Component.class) {
                // Our lookup delegated to the server: no bundled Adventure was loaded
                return new NativeComponentBridge();
            }

            try {
                return new PaperComponentBridge(serverLoader);
            } catch (Throwable throwable) {
                // Geyser's logger may not exist yet at first use
                Logger.getLogger("Geyser").log(Level.SEVERE, "Failed to hook into the server's Adventure " +
                        "library! Falling back to BungeeCord chat components.", throwable);
                return new BungeeComponentBridge();
            }
        }
    }

    private static @Nullable Class<?> findClass(ClassLoader loader, String name) {
        try {
            return Class.forName(name, false, loader);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static boolean hasMethod(Class<?> owner, String name, Class<?>... parameters) {
        try {
            owner.getMethod(name, parameters);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
