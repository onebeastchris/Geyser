/*
 * Copyright (c) 2019-2022 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.item.type;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.geyser.item.TooltipOptions;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.item.BedrockItemBuilder;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponents;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.LodestoneTracker;
import org.geysermc.paletter.vanilla.BedrockItemVariant;
import org.geysermc.paletter.vanilla.BedrockItems;

public class CompassItem extends Item {
    public CompassItem(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
    }

    /**
     * Compasses tracking a lodestone are a separate item on Bedrock.
     */
    @Override
    public @Nullable BedrockItemVariant bedrockVariant(@Nullable DataComponents components) {
        if (components != null && components.contains(DataComponentTypes.LODESTONE_TRACKER)) {
            return BedrockItems.LODESTONE_COMPASS.variant();
        }
        return null;
    }

    @Override
    public boolean isBedrockVariant(@NonNull BedrockItemVariant variant) {
        return variant.type() == BedrockItems.LODESTONE_COMPASS;
    }

    /**
     * Bedrock stacks of the lodestone compass get a tracker without a target: the target is only in the tracking
     * handle of the NBT.
     */
    @Override
    public void toJava(@NonNull BedrockItemVariant variant, @NonNull DataComponents components) {
        components.put(DataComponentTypes.LODESTONE_TRACKER, new LodestoneTracker(null, true));
    }

    @Override
    public void translateComponentsToBedrock(@NonNull GeyserSession session, @NonNull DataComponents components, @NonNull TooltipOptions tooltip, @NonNull BedrockItemBuilder builder) {
        super.translateComponentsToBedrock(session, components, tooltip, builder);

        LodestoneTracker tracker = components.get(DataComponentTypes.LODESTONE_TRACKER);
        if (tracker != null) {
            int trackId = session.getLodestoneCache().store(tracker);
            // Set the bedrock tracking id - will return 0 if invalid
            builder.putInt("trackingHandle", trackId);
        }
    }
}
