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

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.math.vector.Vector3i;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.inventory.item.StoredItemMappings;
import org.geysermc.geyser.level.physics.PistonBehavior;
import org.geysermc.geyser.registry.type.BlockMapping;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.util.InteractResult;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CandleCakeBlock extends BlockMapping {
    private final boolean lit;

    public CandleCakeBlock(String javaIdentifier, int javaBlockId, double hardness, boolean canBreakWithHand, int collisionIndex, @Nullable String pickItem, @Nonnull PistonBehavior pistonBehavior, boolean isBlockEntity, InteractResult defaultInteractResult) {
        super(javaIdentifier, javaBlockId, hardness, canBreakWithHand, collisionIndex, pickItem, pistonBehavior, isBlockEntity, defaultInteractResult);
        this.lit = parseBooleanProperty("lit");
    }

    @Override
    public InteractResult interactWith(GeyserSession session, Vector3i blockPosition, Vector3f clickPosition, int face, boolean isMainHand) {
        GeyserItemStack itemInHand = session.getPlayerInventory().getItemInHand(isMainHand);
        StoredItemMappings storedItems = session.getItemMappings().getStoredItems();
        if (itemInHand.getJavaId() != storedItems.flintAndSteel().getJavaId() && itemInHand.getJavaId() != storedItems.fireCharge().getJavaId()) {
            if ((lit && clickPosition.getY() > 0.5f && itemInHand.isEmpty()) || session.canEat(false)) {
                // Extinguishing the cake, or eating it.
                //TODO Bedrock can't currently extinguish candles on cakes.
                return InteractResult.SUCCESS;
            }
        }
        return InteractResult.PASS;
    }
}