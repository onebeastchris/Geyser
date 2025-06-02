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

package org.geysermc.geyser.api.item.creative;

import java.util.Comparator;
import java.util.List;

public interface CreativeInventoryGroup extends CreativeInventoryElement {

    String groupIdentifier();

    CreativeInventoryItem icon();

    List<CreativeInventoryItem> items();

    default int size() {
        return items().size();
    }

    @Override
    default boolean isGroup() {
        return true;
    }

    @Override
    default boolean isItem() {
        return false;
    }

    default void add(CreativeInventoryItem element) {
        add(element, size());
    }

    void add(CreativeInventoryItem element, int index);

    void addAfter(CreativeInventoryItem add, CreativeInventoryItem afterElement);

    void addBefore(CreativeInventoryItem add, CreativeInventoryItem previousElement);

    void changeIndex(CreativeInventoryItem element, int index);

    void sort(Comparator<CreativeInventoryItem> comparator);

    CreativeInventoryItem remove(String identifier);

    interface Builder {

        Builder groupIdentifier(String identifier);

        Builder icon(CreativeInventoryItem icon);

        Builder items(List<CreativeInventoryItem> items);

        CreativeInventoryGroup build();
    }
}
