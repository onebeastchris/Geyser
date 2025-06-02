/*
 * Copyright (c) 2025 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.api.event.lifecycle;

import org.geysermc.event.Event;
import org.geysermc.geyser.api.item.creative.CreativeInventoryCategory;
import org.geysermc.geyser.api.item.creative.CreativeInventoryElement;
import org.geysermc.geyser.api.util.CreativeCategory;

/**
 * Defines the creative inventory layout for all connecting Bedrock players.
 * With this event, you can add custom item groups, modify vanilla groups, and move items freely.
 * Note: Bedrock uses the creative inventory items for recipes. If an item is not present in the creative inventory,
 * any crafting recipe with it as the output will not show up.
 */
public interface GeyserDefineCreativeInventoryEvent extends Event {

    /**
     * Provides the current layout and contents of a given creative category.
     *
     * @param category the creative category to query
     * @return the contents of the category
     */
    CreativeInventoryCategory creativeInventory(CreativeCategory category);

    /**
     * Unregisters a creative item element, regardless of the category it is in.
     *
     * @param item the item to unregister
     */
    CreativeInventoryElement unregister(CreativeInventoryElement item);
}
