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
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.mcprotocollib.protocol.data.DefaultComponentSerializer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

/**
 * Used on Paper servers whose bundled Adventure is older than the one Geyser uses: Geyser runs on its own
 * (bundled) Adventure, and components cross the API boundary by being serialized to JSON on one side and
 * deserialized by the other side's gson serializer.
 * <p>
 * All lookups explicitly target the server classloader: looking classes up by name from this class would
 * find Geyser's own Adventure, which is exactly the wrong side of the boundary.
 * <p>
 * Reflection code mostly taken from <a href="https://github.com/KyoriPowered/adventure-platform/blob/94d5821f2e755170f42bd8a5fe1d5bf6f66d04ad/platform-bukkit/src/main/java/net/kyori/adventure/platform/bukkit/PaperFacet.java#L46">adventure-platform</a>.
 */
final class PaperComponentBridge implements ComponentBridge {
    /**
     * {@code String -> Component} of the server's gson serializer, bound to its {@code gson()} instance
     */
    private final MethodHandle deserialize;
    /**
     * {@code Component -> String} of the server's gson serializer, bound to its {@code gson()} instance
     */
    private final MethodHandle serialize;
    /**
     * {@code CommandSender#sendMessage(Component)} with the server's component class
     */
    private final Method sendMessage;

    PaperComponentBridge(ClassLoader serverLoader) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();

            Class<?> componentClass = Class.forName("net.kyori.adventure.text.Component", false, serverLoader);
            Class<?> serializerClass = Class.forName("net.kyori.adventure.text.serializer.gson.GsonComponentSerializer", true, serverLoader);
            Class<?> serializerImplClass = Class.forName("net.kyori.adventure.text.serializer.gson.GsonComponentSerializerImpl", true, serverLoader);

            Object serializer = lookup.findStatic(serializerClass, "gson", MethodType.methodType(serializerClass)).invoke();

            // The interface methods only have erased signatures; the impl class declares the concrete ones
            Method deserializeMethod = serializerImplClass.getDeclaredMethod("deserialize", String.class);
            deserializeMethod.setAccessible(true);
            this.deserialize = lookup.unreflect(deserializeMethod).bindTo(serializer);

            Method serializeMethod = serializerImplClass.getDeclaredMethod("serialize", componentClass);
            serializeMethod.setAccessible(true);
            this.serialize = lookup.unreflect(serializeMethod).bindTo(serializer);

            this.sendMessage = CommandSender.class.getMethod("sendMessage", componentClass);
        } catch (Throwable throwable) {
            throw new IllegalStateException("Could not hook into the server's Adventure library", throwable);
        }
    }

    @Override
    public boolean supportsServerComponents() {
        return true;
    }

    @Override
    public @Nullable Object toServerComponent(Component component) {
        try {
            return deserialize.invoke(DefaultComponentSerializer.get().serialize(component));
        } catch (Throwable throwable) {
            GeyserImpl.getInstance().getLogger().error("Failed to convert component for the server", throwable);
            return null;
        }
    }

    @Override
    public @Nullable String serializeServerComponent(Object serverComponent) {
        try {
            return (String) serialize.invoke(serverComponent);
        } catch (Throwable throwable) {
            GeyserImpl.getInstance().getLogger().error("Failed to serialize server component", throwable);
            return null;
        }
    }

    @Override
    public void sendMessage(CommandSender sender, Component component) {
        Object serverComponent = toServerComponent(component);
        if (serverComponent == null) {
            return;
        }

        try {
            sendMessage.invoke(sender, serverComponent);
        } catch (ReflectiveOperationException e) {
            GeyserImpl.getInstance().getLogger().error("Failed to send component message", e);
        }
    }
}
