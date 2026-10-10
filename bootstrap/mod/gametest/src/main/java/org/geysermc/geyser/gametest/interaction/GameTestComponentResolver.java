/*
 * Copyright (c) 2019-2026 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.gametest.interaction;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.geysermc.geyser.item.components.resolvable.ResolvableComponent;
import org.geysermc.geyser.item.components.resolvable.ResolvableComponentGetter;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.geyser.session.cache.registry.JavaRegistryProvider;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponents;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the registry-dependent base components of items from the game test's registries, like the session's
 * {@code ComponentCache} does from the registries a server sent. Items are resolved lazily and cached.
 */
public final class GameTestComponentResolver implements ResolvableComponentGetter {
    private final JavaRegistryProvider registries;
    private final Map<Item, DataComponents> resolved = new HashMap<>();

    public GameTestComponentResolver(JavaRegistryProvider registries) {
        this.registries = registries;
    }

    @Override
    public @Nullable DataComponents getResolvedComponents(Item item) {
        List<ResolvableComponent<?>> toResolve = item.resolvableComponents();
        if (toResolve.isEmpty()) {
            return null;
        }
        return resolved.computeIfAbsent(item, $ -> {
            DataComponents components = new DataComponents(new Object2ObjectOpenHashMap<>());
            for (ResolvableComponent<?> component : toResolve) {
                component.resolve(registries, components);
            }
            return components;
        });
    }
}
