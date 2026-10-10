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
import org.cloudburstmc.math.vector.Vector3d;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.AttributeData;
import org.geysermc.geyser.entity.attribute.GeyserAttributeType;
import org.geysermc.geyser.entity.type.BoatEntity;
import org.geysermc.geyser.entity.type.EnderCrystalEntity;
import org.geysermc.geyser.entity.type.Entity;
import org.geysermc.geyser.entity.type.FallingBlockEntity;
import org.geysermc.geyser.entity.type.LivingEntity;
import org.geysermc.geyser.entity.type.MinecartEntity;
import org.geysermc.geyser.entity.type.TNTEntity;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.level.JavaDimension;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.BoundingBox;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.session.cache.registry.JavaRegistryKey;
import org.geysermc.geyser.session.cache.registry.JavaRegistryProvider;
import org.geysermc.geyser.session.cache.tags.Tag;
import org.geysermc.geyser.util.BlockUtils;
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
    public boolean isFallFlying() {
        return session.getPlayerEntity().isGliding();
    }

    @Override
    public int minY() {
        return session.getDimensionType().minY();
    }

    @Override
    public int maxY() {
        JavaDimension dimension = session.getDimensionType();
        return dimension.minY() + dimension.height() - 1;
    }

    @Override
    public boolean hasEntityInBox(@NonNull BoundingBox box, boolean buildingBlockersOnly) {
        // The player is a building blocker as well; use the bounding box also used for collisions
        if (box.checkIntersection(session.getCollisionManager().getPlayerBoundingBox())) {
            return true;
        }

        // Only called on the session's event loop, so the unsafe view is fine
        Entity self = session.getPlayerEntity();
        for (Entity entity : session.getEntityCache().getEntitiesUnsafe().values()) {
            if (entity == self || !entity.isValid()) {
                continue;
            }
            if (buildingBlockersOnly && !blocksBuilding(entity)) {
                continue;
            }
            Vector3f position = entity.position();
            double width = entity.getBoundingBoxWidth();
            double height = entity.getBoundingBoxHeight();
            // Same as BoundingBox#checkIntersection, without allocating a box per entity
            if (Math.abs(position.getX() - box.getMiddleX()) * 2 < width + box.getSizeX()
                && Math.abs(position.getY() + height / 2 - box.getMiddleY()) * 2 < height + box.getSizeY()
                && Math.abs(position.getZ() - box.getMiddleZ()) * 2 < width + box.getSizeZ()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Mirrors {@code Entity#blocksBuilding}.
     */
    private static boolean blocksBuilding(Entity entity) {
        return entity instanceof LivingEntity || entity instanceof BoatEntity || entity instanceof MinecartEntity
            || entity instanceof EnderCrystalEntity || entity instanceof FallingBlockEntity || entity instanceof TNTEntity;
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
