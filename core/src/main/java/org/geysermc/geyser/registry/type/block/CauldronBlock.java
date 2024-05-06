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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.inventory.item.Potion;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.geyser.level.physics.PistonBehavior;
import org.geysermc.geyser.registry.type.BlockMapping;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.session.cache.tags.ItemTag;
import org.geysermc.geyser.util.InteractResult;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.BannerPatternLayer;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CauldronBlock extends BlockMapping {
    private final CauldronType type;
    private final int level;

    public CauldronBlock(String javaIdentifier, int javaBlockId, float hardness, boolean canBreakWithHand, int collisionIndex,
                         @Nullable String pickItem, @NonNull PistonBehavior pistonBehavior, boolean isBlockEntity, InteractResult defaultInteractResult) {
        super(javaIdentifier, javaBlockId, hardness, canBreakWithHand, collisionIndex, pickItem, pistonBehavior, isBlockEntity, defaultInteractResult);

        if (javaIdentifier.contains("lava")) {
            this.type = CauldronType.LAVA;
        } else if (javaIdentifier.contains("water")) {
            this.type = CauldronType.WATER;
        } else if (javaIdentifier.contains("snow")) {
            this.type = CauldronType.POWDERED_SNOW;
        } else {
            this.type = CauldronType.EMPTY;
        }

        if (type.hasLevel) {
            this.level = parseIntProperty("level");
        } else {
            this.level = -1;
        }
    }

    @Override
    public InteractResult interactWith(GeyserSession session, Vector3i blockPosition, Vector3f clickPosition, int face, boolean isMainHand) {
        final GeyserItemStack stack = session.getPlayerInventory().getItemInHand(isMainHand);
        final Item itemInHand = stack.asItem();
        if (itemInHand.equals(Items.WATER_BUCKET) || itemInHand.equals(Items.LAVA_BUCKET) || itemInHand.equals(Items.POWDER_SNOW_BUCKET)) {
            // One of these buckets always overrides the contents of the cauldron, even if it's empty
            return InteractResult.SUCCESS;
        }
        if (((type.hasLevel && level == 3) || type == CauldronType.LAVA) && itemInHand.equals(Items.BUCKET)) {
            // Emptying the cauldron contents into the bucket
            return InteractResult.SUCCESS;
        }
        if (type == CauldronType.EMPTY || (type == CauldronType.WATER && level != 3)) {
            final Potion potion = Potion.getByJavaId(itemInHand.javaId());
            if (potion == Potion.WATER) {
                // Adding a level of water to the cauldron
                return InteractResult.SUCCESS;
            } else if (potion != null) {
                return InteractResult.PASS;
            }
        }
        if (type == CauldronType.WATER) {
            if (itemInHand.equals(Items.GLASS_BOTTLE)) {
                // Adding from the cauldron to the bottle
                return InteractResult.SUCCESS;
            }
            if (itemInHand.javaIdentifier().endsWith("_shulker_box")) {
                // A dyed shulker box being undyed
                return InteractResult.SUCCESS;
            }

            if (itemInHand.javaIdentifier().endsWith("banner")) {
                final List<BannerPatternLayer> patterns = stack.getComponent(DataComponentType.BANNER_PATTERNS);
                if (patterns == null || patterns.isEmpty()) {
                    // No pattern to clear
                    return InteractResult.PASS;
                }
                return InteractResult.SUCCESS;
            }

            // remove dye from e.g. leather armour
            if (stack.getComponent(DataComponentType.DYED_COLOR) != null &&
                    session.getTagCache().is(ItemTag.DYEABLE, itemInHand)) {
                return InteractResult.SUCCESS;
            }
        }
        return InteractResult.PASS;
    }

    enum CauldronType {
        EMPTY(false),
        WATER(true),
        LAVA(false),
        POWDERED_SNOW(true);

        private final boolean hasLevel;

        CauldronType(boolean hasLevel) {
            this.hasLevel = hasLevel;
        }
    }
}
