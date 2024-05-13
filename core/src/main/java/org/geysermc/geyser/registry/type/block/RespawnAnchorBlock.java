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

package org.geysermc.geyser.registry.type.block;

import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.SoundEvent;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.level.physics.PistonBehavior;
import org.geysermc.geyser.registry.type.BlockMapping;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.util.InteractResult;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class RespawnAnchorBlock extends BlockMapping {
    private final boolean canBeCharged;
    private final boolean isCharged;

    public RespawnAnchorBlock(String javaIdentifier, int javaBlockId, float hardness, boolean canBreakWithHand, int collisionIndex, @Nullable String pickItem, @Nonnull PistonBehavior pistonBehavior, boolean isBlockEntity, InteractResult defaultInteractResult) {
        super(javaIdentifier, javaBlockId, hardness, canBreakWithHand, collisionIndex, pickItem, pistonBehavior, isBlockEntity, defaultInteractResult);
        int charges = parseIntProperty("charges");
        this.canBeCharged = charges < 4;
        this.isCharged = charges != 0;
    }

    @Override
    public InteractResult interactWith(GeyserSession session, Vector3i blockPosition, Vector3f clickPosition, int face, boolean isMainHand) {
        if (canBeCharged) {
            if (isGlowstone(session, isMainHand)) {
                session.playSound(SoundEvent.RESPAWN_ANCHOR_CHARGE, blockPosition.toFloat());
                return InteractResult.SUCCESS;
            } else if (isGlowstone(session, false)) {
                return InteractResult.PASS; // will be charged by offhand
            }
        }

        if (isCharged && isMainHand) {
            if (!session.getDimensionType().respawn_anchor_works()) {
                return InteractResult.SUCCESS; // boom!
            } else {
                return InteractResult.CONSUME; // spawn setting
            }
        }

        return InteractResult.PASS;
    }

    private boolean isGlowstone(GeyserSession session, boolean isMainHand) {
        return session.getPlayerInventory().getItemInHand(isMainHand).asItem().equals(Items.GLOWSTONE);
    }
}
