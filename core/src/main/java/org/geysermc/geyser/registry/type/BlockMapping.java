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

package org.geysermc.geyser.registry.type;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.level.physics.PistonBehavior;
import org.geysermc.geyser.registry.type.block.BlockSupplier;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.util.BlockUtils;
import org.geysermc.geyser.util.InteractionResult;

@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class BlockMapping {
    public static BlockMapping DEFAULT = BlockMapping.builder()
            .javaIdentifier("minecraft:air")
            .pistonBehavior(PistonBehavior.NORMAL)
            .build(BlockMapping::new);
    public BlockMapping(String javaIdentifier, int javaBlockId, float hardness,
                        boolean canBreakWithHand, int collisionIndex, String pickItem,
                        PistonBehavior pistonBehavior, boolean isBlockEntity, InteractionResult defaultInteractResult) {
        this(javaIdentifier, javaBlockId, hardness, canBreakWithHand, collisionIndex, pickItem,
                pistonBehavior, isBlockEntity, false, defaultInteractResult);
    }

    String javaIdentifier;
    /**
     * The block ID shared between all different block states of this block.
     * NOT the runtime ID!
     */
    int javaBlockId;

    float hardness;
    boolean canBreakWithHand;
    /**
     * The index of this collision in collision.json
     */
    int collisionIndex;
    @Nullable String pickItem;

    @NonNull PistonBehavior pistonBehavior;
    boolean isBlockEntity;
    boolean isNonVanilla;

    /**
     * The default interact result when interacting with this block.
     */
    @Getter(AccessLevel.NONE)
    InteractionResult defaultInteractResult;

    /**
     * @return the identifier without the additional block states
     */
    public String getCleanJavaIdentifier() {
        return BlockUtils.getCleanIdentifier(javaIdentifier);
    }

    /**
     * @return the corresponding Java identifier for this item
     */
    public String getItemIdentifier() {
        if (pickItem != null && !pickItem.equals("minecraft:air")) {
            // Spawners can have air as their pick item which we are not interested in.
            return pickItem;
        }

        return getCleanJavaIdentifier();
    }

    /**
     * Get the item a Java client would receive when pressing
     * the Pick Block key on a specific Java block state.
     *
     * @return The Java identifier of the item
     */
    public String getPickItem() {
        if (pickItem != null) {
            return pickItem;
        }

        return getCleanJavaIdentifier();
    }

    /**
     * Determines the behavior, if any, upon interacting with this block. Used to then trigger item behavior if it "fails".
     *
     * @param clickPosition where the player clicked, NOT including the existing block position
     * @param isMainHand whether this interaction test comes from the main hand or off hand
     */
    public InteractionResult interactWith(GeyserSession session, Vector3i blockPosition, Vector3f clickPosition, int face, boolean isMainHand) {
        return defaultInteractResult;
    }

    // TODO chris
    // signs.. cache!!!!!!!!!!!!!!!
    // chiseled bookshelves

    protected boolean parseBooleanProperty(String property) {
        return Boolean.parseBoolean(parseStringProperty(property));
    }

    protected int parseIntProperty(String property) {
        return Integer.parseInt(parseStringProperty(property));
    }

    protected String parseStringProperty(String property) {
        try {
            int propertyIndex = javaIdentifier.indexOf(property + "=");
            // Substring the position of the property name, plus the length of the property name, plus =
            String split = javaIdentifier.substring(propertyIndex + property.length() + 1);
            int commaIndex = split.indexOf(',');
            if (commaIndex != -1) {
                return split.substring(0, commaIndex);
            } else {
                // End of properties list, remove ']'
                return split.substring(0, split.length() - 1);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to find property " + property + " for " + javaIdentifier, e);
        }
    }

    public static BlockMappingBuilder builder() {
        return new BlockMappingBuilder();
    }

    public static final class BlockMappingBuilder {
        private String javaIdentifier;
        private int javaBlockId;
        private float hardness;
        private boolean canBreakWithHand;
        private int collisionIndex;
        private String pickItem;
        private PistonBehavior pistonBehavior;
        private boolean isBlockEntity;
        private boolean isNonVanilla;
        private InteractionResult defaultInteractResult;

        private BlockMappingBuilder() {
        }

        public BlockMappingBuilder javaIdentifier(String javaIdentifier) {
            this.javaIdentifier = javaIdentifier;
            return this;
        }

        public BlockMappingBuilder javaBlockId(int javaBlockId) {
            this.javaBlockId = javaBlockId;
            return this;
        }

        public BlockMappingBuilder hardness(float hardness) {
            this.hardness = hardness;
            return this;
        }

        public BlockMappingBuilder canBreakWithHand(boolean canBreakWithHand) {
            this.canBreakWithHand = canBreakWithHand;
            return this;
        }

        public BlockMappingBuilder collisionIndex(int collisionIndex) {
            this.collisionIndex = collisionIndex;
            return this;
        }

        public BlockMappingBuilder pickItem(String pickItem) {
            this.pickItem = pickItem;
            return this;
        }

        public BlockMappingBuilder pistonBehavior(PistonBehavior pistonBehavior) {
            this.pistonBehavior = pistonBehavior;
            return this;
        }

        public BlockMappingBuilder isBlockEntity(boolean blockEntity) {
            isBlockEntity = blockEntity;
            return this;
        }

        public BlockMappingBuilder isNonVanilla(boolean isNonVanilla) {
            this.isNonVanilla = isNonVanilla;
            return this;
        }

        public BlockMappingBuilder defaultInteractResult(InteractionResult result) {
            defaultInteractResult = result;
            return this;
        }

        public BlockMapping build(BlockSupplier type) {
            if (isNonVanilla) { // Custom blocks, avoids having the BlockSupplier having a isNonVanilla field
                return new BlockMapping(javaIdentifier, javaBlockId, hardness, canBreakWithHand, collisionIndex,
                        pickItem, pistonBehavior, isBlockEntity, true, defaultInteractResult);
            } else {
                return type.create(javaIdentifier, javaBlockId, hardness, canBreakWithHand, collisionIndex, pickItem,
                        pistonBehavior, isBlockEntity, defaultInteractResult);
            }
        }
    }
}
