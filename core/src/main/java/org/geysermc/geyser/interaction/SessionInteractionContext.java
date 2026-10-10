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

package org.geysermc.geyser.interaction;

import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.math.GenericMath;
import org.cloudburstmc.math.vector.Vector3d;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.AttributeData;
import org.geysermc.geyser.entity.attribute.GeyserAttributeType;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.inventory.PlayerInventory;
import org.geysermc.geyser.level.JavaDimension;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.registry.BlockRegistries;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.session.cache.registry.JavaRegistryKey;
import org.geysermc.geyser.session.cache.registry.JavaRegistryProvider;
import org.geysermc.geyser.session.cache.tags.Tag;
import org.geysermc.geyser.translator.collision.BlockCollision;
import org.geysermc.geyser.util.BlockUtils;
import org.geysermc.mcprotocollib.protocol.data.game.entity.EquipmentSlot;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.AdventureModePredicate;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.HolderSet;

/**
 * {@link InteractionContext} backed by a live session. Created once per session; holds no state of its own.
 */
public final class SessionInteractionContext implements InteractionContext {
    private final GeyserSession session;

    public SessionInteractionContext(GeyserSession session) {
        this.session = session;
    }

    @Override
    public @NonNull GameMode gameMode() {
        return session.getGameMode();
    }

    @Override
    public boolean isSecondaryUseActive() {
        return session.isSneaking();
    }

    @Override
    public @NonNull GeyserItemStack itemInHand(@NonNull Hand hand) {
        return session.getPlayerInventory().getItemInHand(hand);
    }

    @Override
    public @NonNull GeyserItemStack itemBySlot(@NonNull EquipmentSlot slot) {
        PlayerInventory inventory = session.getPlayerInventory();
        // Java inventory menu slots: 5-8 armor (head to feet), 45 offhand
        return switch (slot) {
            case MAIN_HAND -> inventory.getItemInHand();
            case OFF_HAND -> inventory.getOffhand();
            case HELMET -> inventory.getItem(5);
            case CHESTPLATE -> inventory.getItem(6);
            case LEGGINGS -> inventory.getItem(7);
            case BOOTS -> inventory.getItem(8);
            case BODY, SADDLE -> GeyserItemStack.EMPTY;
        };
    }

    @Override
    public int inventorySize() {
        // 36 inventory slots, 4 armor slots and the offhand
        return 41;
    }

    @Override
    public @NonNull GeyserItemStack inventoryItem(int index) {
        PlayerInventory inventory = session.getPlayerInventory();
        if (index >= 0 && index < 9) {
            return inventory.getItem(36 + index); // Hotbar
        } else if (index < 36) {
            return inventory.getItem(index); // Main inventory: same slot numbers in the menu
        } else if (index < 40) {
            return inventory.getItem(8 - (index - 36)); // Armor, feet first; menu slots are head first
        } else if (index == 40) {
            return inventory.getOffhand();
        }
        return GeyserItemStack.EMPTY;
    }

    @Override
    public boolean hasInfiniteMaterials() {
        return session.isInstabuild();
    }

    @Override
    public boolean isInvulnerable() {
        return session.isInvulnerable();
    }

    @Override
    public boolean canUseGameMasterBlocks() {
        return session.canUseCommandBlocks();
    }

    @Override
    public int foodLevel() {
        AttributeData hunger = session.getPlayerEntity().getAttributes().get(GeyserAttributeType.HUNGER);
        return hunger == null ? 20 : (int) hunger.getValue();
    }

    @Override
    public boolean isOnCooldown(@NonNull GeyserItemStack stack) {
        return session.getWorldCache().hasCooldown(stack);
    }

    @Override
    public boolean canPlaceOnInAdventureMode(@NonNull GeyserItemStack stack, @NonNull Vector3i position) {
        AdventureModePredicate canPlaceOn = stack.getComponent(DataComponentTypes.CAN_PLACE_ON);
        if (canPlaceOn == null) {
            return false;
        }
        BlockState state = blockAt(position);
        for (AdventureModePredicate.BlockPredicate predicate : canPlaceOn.getPredicates()) {
            if (BlockUtils.blockMatchesPredicate(session, state, predicate)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @NonNull BlockState blockAt(@NonNull Vector3i position) {
        return session.getGeyser().getWorldManager().blockAt(session, position);
    }

    @Override
    public boolean isInTag(@NonNull Tag<?> tag, int javaId) {
        return session.getTagCache().is(tag, javaId);
    }

    @Override
    public boolean isInHolderSet(@Nullable HolderSet holderSet, @NonNull JavaRegistryKey<?> registry, int javaId) {
        return session.getTagCache().is(holderSet, registry, javaId);
    }

    @Override
    public boolean isInRecipePropertySet(@NonNull Key propertySet, int itemId) {
        return session.getWorldCache().isInRecipePropertySet(propertySet, itemId);
    }

    @Override
    public @NonNull Vector3d position() {
        Vector3f position = session.getPlayerEntity().position();
        return Vector3d.from(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public float yaw() {
        return session.getPlayerEntity().getJavaYaw();
    }

    @Override
    public float pitch() {
        return session.getPlayerEntity().getPitch();
    }

    @Override
    public double eyeHeight() {
        return session.getEyeHeight();
    }

    @Override
    public double blockInteractionRange() {
        return session.getPlayerEntity().getBlockInteractionRange();
    }

    @Override
    public boolean isFallFlying() {
        return session.getPlayerEntity().isGliding();
    }

    @Override
    public boolean isInWaterOrRain() {
        // Approximation: only the block at the player's feet is checked for water
        if (session.getCollisionManager().isPlayerTouchingWater()) {
            return true;
        }
        if (!session.isRaining()) {
            return false;
        }
        JavaDimension dimension = session.getDimensionType();
        if (dimension == null) {
            return false;
        }

        // Mirrors Level#isRainingAt at the top of the player's bounding box: it's raining there if there is no motion
        // blocking block above it. Approximations: the biome's precipitation is not known, and a block blocks motion
        // if it has a collision box or contains a fluid.
        Vector3f position = session.getPlayerEntity().position();
        int x = GenericMath.floor(position.getX());
        int z = GenericMath.floor(position.getZ());
        int maxY = dimension.minY() + dimension.height();
        for (int y = GenericMath.floor(position.getY() + session.getPlayerEntity().getBoundingBoxHeight()); y < maxY; y++) {
            BlockState state = session.getGeyser().getWorldManager().blockAt(session, x, y, z);
            if (state.is(Blocks.WATER) || state.is(Blocks.LAVA) || BlockRegistries.WATERLOGGED.get().get(state.javaId())) {
                return false;
            }
            BlockCollision collision = BlockUtils.getCollision(state.javaId());
            if (collision != null && collision.getBoundingBoxes().length > 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @Nullable BlockEntityInteractionData blockEntityData(@NonNull Vector3i position) {
        return session.getWorldCache().getBlockEntityInteractionData(position);
    }

    @Override
    public @NonNull JavaRegistryProvider registries() {
        return session.getRegistryCache();
    }
}
