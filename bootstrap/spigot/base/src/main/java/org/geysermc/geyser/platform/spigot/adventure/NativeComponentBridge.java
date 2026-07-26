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
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.geyser.GeyserImpl;

/**
 * Used when the server provides the same Adventure major version Geyser is compiled against: Geyser's component
 * classes ARE the server's component classes (loaded via parent delegation), so everything is a plain pass-through.
 * <p>
 * This is the only class allowed to reference server API methods with Adventure types in their signatures;
 * it must never be linked in any other mode.
 */
final class NativeComponentBridge implements ComponentBridge {

    @Override
    public boolean supportsServerComponents() {
        return true;
    }

    @Override
    public Object toServerComponent(Component component) {
        return component;
    }

    @Override
    public @Nullable String serializeServerComponent(Object serverComponent) {
        try {
            return GsonComponentSerializer.gson().serialize((Component) serverComponent);
        } catch (Throwable throwable) {
            GeyserImpl.getInstance().getLogger().error("Failed to serialize server component", throwable);
            return null;
        }
    }

    @Override
    public void sendMessage(CommandSender sender, Component component) {
        sender.sendMessage(component);
    }
}
