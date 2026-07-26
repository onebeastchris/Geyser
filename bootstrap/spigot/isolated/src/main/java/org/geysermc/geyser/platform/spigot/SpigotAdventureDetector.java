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

package org.geysermc.geyser.platform.spigot;

import org.bukkit.command.CommandSender;
import org.geysermc.floodgate.isolation.library.Library;
import org.geysermc.floodgate.isolation.library.LibraryManager;
import org.geysermc.floodgate.isolation.library.Repository;

/**
 * Determines whether the server provides an Adventure version Geyser can use directly, and loads the
 * bundled Adventure library when it does not. Runs in the isolated loader, before the platform classloader
 * exists, so an accidental {@code net.kyori} reference in platform code cannot influence (or be broken by)
 * the outcome. The platform side merely observes the result: see {@code SpigotAdventure} in the base module.
 */
final class SpigotAdventureDetector {
    private static final String ADVENTURE_LIBRARY_ID = "adventure";

    private SpigotAdventureDetector() {
    }

    /**
     * @param serverLoader the plugin's classloader, which delegates to the server
     * @return whether the server provides the Adventure major version Geyser is compiled against;
     * if false, the bundled Adventure library must be loaded
     */
    static boolean serverProvidesUsableAdventure(ClassLoader serverLoader) {
        Class<?> component = findClass(serverLoader, "net.kyori.adventure.text.Component");
        if (component == null) {
            return false;
        }

        // Distinguishes Paper (native component support in the Bukkit API) from Spigot, even if
        // some other plugin leaked an unrelocated Adventure onto the server classpath
        try {
            CommandSender.class.getMethod("sendMessage", component);
        } catch (NoSuchMethodException e) {
            return false;
        }

        // BookLike exists since Adventure 5 - the major version Geyser is compiled against.
        // The serializer check guards against a server that bundles the API without the gson serializer.
        return findClass(serverLoader, "net.kyori.adventure.inventory.BookLike") != null
                && findClass(serverLoader, "net.kyori.adventure.text.serializer.gson.GsonComponentSerializer") != null;
    }

    private static Class<?> findClass(ClassLoader loader, String name) {
        try {
            return Class.forName(name, false, loader);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Loads the Adventure library shipped as {@code bundled/adventure.jar}. Must happen before the platform
     * is loaded - once a {@code net.kyori} class has been resolved in the platform classloader, that
     * binding is permanent.
     */
    static void load(LibraryManager manager) {
        manager.addLibrary(Library.builder()
                .id(ADVENTURE_LIBRARY_ID)
                .repository(Repository.BUNDLED)
                .artifactId(ADVENTURE_LIBRARY_ID)
                .forceOverride(true)
                .build())
            .apply();
    }
}
