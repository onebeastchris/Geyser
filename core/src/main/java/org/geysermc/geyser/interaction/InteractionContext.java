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
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.BoundingBox;
import org.geysermc.geyser.session.cache.registry.JavaRegistryKey;
import org.geysermc.geyser.session.cache.registry.JavaRegistryProvider;
import org.geysermc.geyser.session.cache.tags.Tag;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.HolderSet;

/**
 * Everything the interaction simulation may know about the interacting player and the world they are in.
 * <p>
 * Implemented by {@link SessionInteractionContext} for a live session, and by the game test module where the
 * world and the player are backed by the vanilla server. Implementations should be cheap to query; most methods
 * are called at most a couple of times per click.
 */
public interface InteractionContext {

    @NonNull GameMode gameMode();

    /**
     * Mirrors {@code Player#isSecondaryUseActive}, which is the sneak key on Java Edition.
     */
    boolean isSecondaryUseActive();

    @NonNull GeyserItemStack itemInHand(@NonNull Hand hand);

    /**
     * Mirrors {@code Abilities#mayBuild}: false in adventure and spectator mode.
     */
    default boolean mayBuild() {
        return gameMode() != GameMode.ADVENTURE && gameMode() != GameMode.SPECTATOR;
    }

    /**
     * Mirrors {@code Abilities#instabuild}, as sent by the server in the player abilities packet.
     */
    boolean hasInfiniteMaterials();

    /**
     * Mirrors {@code Abilities#invulnerable}, as sent by the server in the player abilities packet.
     */
    boolean isInvulnerable();

    /**
     * Mirrors {@code Player#canUseGameMasterBlocks}: instabuild and at least gamemaster permissions.
     */
    boolean canUseGameMasterBlocks();

    int foodLevel();

    /**
     * Mirrors {@code Player#canEat}.
     */
    default boolean canEat(boolean canAlwaysEat) {
        return isInvulnerable() || canAlwaysEat || foodLevel() < 20;
    }

    boolean isOnCooldown(@NonNull GeyserItemStack stack);

    /**
     * Mirrors {@code ItemStack#canPlaceOnBlockInAdventureMode}: whether the stack's {@code can_place_on} component
     * matches the block at the given position.
     */
    boolean canPlaceOnInAdventureMode(@NonNull GeyserItemStack stack, @NonNull Vector3i position);

    @NonNull BlockState blockAt(@NonNull Vector3i position);

    /**
     * @param tag the tag to check
     * @param javaId the network ID of the block/item/etc. in the registry the tag belongs to
     */
    boolean isInTag(@NonNull Tag<?> tag, int javaId);

    /**
     * @param holderSet the holder set to check; null matches nothing
     * @param registry the registry the holder set refers to
     * @param javaId the network ID of the object in that registry
     */
    boolean isInHolderSet(@Nullable HolderSet holderSet, @NonNull JavaRegistryKey<?> registry, int javaId);

    /**
     * @return whether the given item is part of the recipe property set (for example {@code minecraft:campfire_input})
     * the server sent with the recipe update
     */
    boolean isInRecipePropertySet(@NonNull Key propertySet, int itemId);

    /**
     * @return the position of the player's feet
     */
    @NonNull Vector3d position();

    /**
     * @return Java-style yaw of the player
     */
    float yaw();

    float pitch();

    /**
     * Mirrors {@code LivingEntity#isFallFlying}: whether the player is gliding with an elytra.
     */
    boolean isFallFlying();

    /**
     * Mirrors {@code LevelHeightAccessor#getMinY} of the dimension the player is in.
     */
    int minY();

    /**
     * Mirrors {@code LevelHeightAccessor#getMaxY} of the dimension the player is in: the highest block position
     * that is still inside the world (inclusive).
     */
    int maxY();

    /**
     * Whether any entity intersects the given box. The player themselves counts as an entity, too.
     * <ul>
     *     <li>With {@code buildingBlockersOnly == false}, mirrors {@code !Level#getEntities(null, box).isEmpty()}:
     *     every entity that is not a spectator.</li>
     *     <li>With {@code buildingBlockersOnly == true}, mirrors the entity part of {@code Level#isUnobstructed}:
     *     only entities that block building ({@code Entity#blocksBuilding}: living entities, boats, minecarts,
     *     end crystals, falling blocks and primed TNT).</li>
     * </ul>
     * Like {@code AABB#intersects}, boxes that merely touch do not intersect.
     *
     * @param box the box in world coordinates
     * @param buildingBlockersOnly whether to only consider entities that block block placement
     */
    boolean hasEntityInBox(@NonNull BoundingBox box, boolean buildingBlockersOnly);

    /**
     * Minimal block entity knowledge required by the simulation; see {@link BlockEntityInteractionData}.
     *
     * @return the data known for the block entity at this position, or null if none is tracked
     */
    @Nullable BlockEntityInteractionData blockEntityData(@NonNull Vector3i position);

    @NonNull JavaRegistryProvider registries();
}
