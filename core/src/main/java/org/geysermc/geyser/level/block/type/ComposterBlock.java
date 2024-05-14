/*
 * Copyright (c) 2019-2024 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.level.block.type;

import com.fasterxml.jackson.databind.JsonNode;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.cloudburstmc.protocol.bedrock.data.SoundEvent;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.util.InteractionContext;
import org.geysermc.geyser.util.InteractionResult;

import java.io.IOException;
import java.io.InputStream;

public class ComposterBlock extends Block {
    private static final IntSet VALID_ITEMS = new IntArraySet();

    public ComposterBlock(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    @Override
    public InteractionResult interactWith(InteractionContext context) {
        int level = context.state().getValue(Properties.LEVEL_COMPOSTER);
        if ((level == 8 && context.mainHand()) || (level < 8 && VALID_ITEMS.contains(context.itemInHand().getJavaId()))) {
            // Adding an item into the composter, or retrieving the contents of the composter at level 8.
            if (level == 8) {
                context.playSound(SoundEvent.COMPOSTER_EMPTY);
            } else {
                context.playSound(SoundEvent.COMPOSTER_FILL_LAYER);
            }
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }

    static {
        try (InputStream stream = GeyserImpl.getInstance().getBootstrap().getResourceOrThrow("mappings/compostables.json")) {
            JsonNode node = GeyserImpl.JSON_MAPPER.readTree(stream);
            for (JsonNode item : node) {
                VALID_ITEMS.add(item.asInt());
            }
        } catch (IOException e) {
            throw new AssertionError("Unable to load composter information from mappings!", e);
        }

    }
}
