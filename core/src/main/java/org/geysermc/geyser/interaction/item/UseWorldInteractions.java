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

package org.geysermc.geyser.interaction.item;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.math.GenericMath;
import org.cloudburstmc.math.vector.Vector3d;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.interaction.BlockHit;
import org.geysermc.geyser.interaction.InteractionContext;
import org.geysermc.geyser.interaction.InteractionRegistry;
import org.geysermc.geyser.interaction.InteractionTables;
import org.geysermc.geyser.interaction.UseOnContext;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.BoundingBox;
import org.geysermc.geyser.translator.collision.BlockCollision;
import org.geysermc.geyser.util.BlockUtils;
import org.geysermc.geyser.util.InteractionResult;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;

/**
 * {@code Item#use} implementations that look at the world through {@link UseRaytrace}. Only whether and what the
 * client-side raytrace hits decides their result.
 */
final class UseWorldInteractions {
    /**
     * Size of every boat and raft entity type.
     */
    private static final double BOAT_WIDTH = 1.375;
    private static final double BOAT_HEIGHT = 0.5625;

    /**
     * Mirrors {@code BoatItem#use}: places a boat where the player looks at a block or fluid, if the boat fits there.
     * <p>
     * Approximations: the player's eyes being inside a pickable entity (PASS) is not checked, and only block
     * collisions are checked for the new boat; entities and the world border are ignored.
     */
    static @NonNull InteractionResult useBoat(@NonNull InteractionContext player, @NonNull Hand hand, @NonNull GeyserItemStack stack) {
        BlockHit hit = UseRaytrace.clip(player, UseRaytrace.FluidMode.ANY);
        if (hit == null) {
            return InteractionResult.PASS;
        }
        return boatFits(player, hit.location()) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    /**
     * Mirrors {@code CollisionGetter#noCollision} for the block collisions of a boat placed at the given location.
     */
    private static boolean boatFits(@NonNull InteractionContext player, @NonNull Vector3d location) {
        double halfWidth = BOAT_WIDTH / 2;
        BoundingBox boat = new BoundingBox(location.getX(), location.getY() + BOAT_HEIGHT / 2, location.getZ(), BOAT_WIDTH, BOAT_HEIGHT, BOAT_WIDTH);
        // Like BlockCollisions, also check neighbouring blocks, since their collision may extend outside their block (fences, walls)
        int minX = GenericMath.floor(location.getX() - halfWidth - 1.0E-7) - 1;
        int maxX = GenericMath.floor(location.getX() + halfWidth + 1.0E-7) + 1;
        int minY = GenericMath.floor(location.getY() - 1.0E-7) - 1;
        int maxY = GenericMath.floor(location.getY() + BOAT_HEIGHT + 1.0E-7) + 1;
        int minZ = GenericMath.floor(location.getZ() - halfWidth - 1.0E-7) - 1;
        int maxZ = GenericMath.floor(location.getZ() + halfWidth + 1.0E-7) + 1;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockState state = player.blockAt(Vector3i.from(x, y, z));
                    BlockCollision collision = BlockUtils.getCollision(state.javaId());
                    if (collision != null && collision.checkIntersection(x, y, z, boat)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Mirrors {@code BottleItem#use}: fills the bottle if the player looks at water.
     * <p>
     * Approximation: filling it with dragon's breath from a nearby area effect cloud of the ender dragon is not checked.
     */
    static @NonNull InteractionResult useBottle(@NonNull InteractionContext player, @NonNull Hand hand, @NonNull GeyserItemStack stack) {
        BlockHit hit = UseRaytrace.clip(player, UseRaytrace.FluidMode.SOURCE_ONLY);
        if (hit == null) {
            return InteractionResult.PASS;
        }
        // Level#mayInteract is always true client side
        return UseRaytrace.fluidType(player.blockAt(hit.position())) == UseRaytrace.WATER ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    /**
     * Mirrors {@code EnderEyeItem#use}: does nothing when looking at an end portal frame (that's handled by
     * {@code useOn}), otherwise starts using the eye; it's thrown server side.
     */
    static @NonNull InteractionResult useEnderEye(@NonNull InteractionContext player, @NonNull Hand hand, @NonNull GeyserItemStack stack) {
        BlockHit hit = UseRaytrace.clip(player, UseRaytrace.FluidMode.NONE);
        if (hit != null && player.blockAt(hit.position()).is(Blocks.END_PORTAL_FRAME)) {
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    /**
     * Mirrors {@code SpawnEggItem#use}: succeeds client side when looking at a block or a fluid source.
     * <p>
     * Approximation: {@code EntityType#canSpawn} (e.g. disabled features or difficulty) is not checked.
     */
    static @NonNull InteractionResult useSpawnEgg(@NonNull InteractionContext player, @NonNull Hand hand, @NonNull GeyserItemStack stack) {
        BlockHit hit = UseRaytrace.clip(player, UseRaytrace.FluidMode.SOURCE_ONLY);
        if (hit == null) {
            return InteractionResult.PASS;
        }
        // SpawnEggItem#getType
        if (stack.getComponent(DataComponentTypes.ENTITY_DATA) == null) {
            return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Mirrors {@code PlaceOnWaterBlockItem#use}: places the block (lily pad, frogspawn) above the targeted fluid source
     * or block through {@code BlockItem#useOn}. A miss is used as well; it is then positioned at the end of the ray.
     */
    static @NonNull InteractionResult usePlaceOnWater(@NonNull InteractionContext player, @NonNull Hand hand, @NonNull GeyserItemStack stack) {
        BlockHit hit = UseRaytrace.clip(player, UseRaytrace.FluidMode.SOURCE_ONLY);
        if (hit == null) {
            hit = UseRaytrace.miss(player);
        }
        // BlockHitResult#withPosition
        BlockHit above = new BlockHit(hit.position().up(), hit.face(), hit.location(), hit.inside());
        // Calls BlockItem#useOn directly, skipping the adventure mode check of ItemStack#useOn
        return InteractionRegistry.itemUseOn("BlockItem").useOn(new UseOnContext(player, hand, stack, above));
    }

    /**
     * Mirrors {@code BucketItem#use}: empty buckets pick up fluid sources (and powder snow), filled buckets place their
     * fluid at the targeted block.
     */
    static @NonNull InteractionResult useBucket(@NonNull InteractionContext player, @NonNull Hand hand, @NonNull GeyserItemStack stack) {
        int content = bucketContent(stack);
        BlockHit hit = UseRaytrace.clip(player, content == UseRaytrace.NO_FLUID ? UseRaytrace.FluidMode.SOURCE_ONLY : UseRaytrace.FluidMode.NONE);
        if (hit == null) {
            return InteractionResult.PASS;
        }

        // Level#mayInteract is always true client side. Player#mayUseItemAt checks the clicked block in adventure mode.
        if (!player.mayBuild() && !player.canPlaceOnInAdventureMode(stack, hit.position())) {
            return InteractionResult.FAIL;
        }

        BlockState clicked = player.blockAt(hit.position());
        if (content == UseRaytrace.NO_FLUID) {
            return canPickUp(clicked) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }

        Vector3i placePos = content == UseRaytrace.WATER && isLiquidContainer(clicked) ? hit.position() : hit.adjacentPosition();
        return canEmpty(player, content, placePos, hit) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    /**
     * @return the fluid placed by the bucket; mob buckets contain water
     */
    private static int bucketContent(@NonNull GeyserItemStack stack) {
        if (stack.is(Items.BUCKET)) {
            return UseRaytrace.NO_FLUID;
        }
        return stack.is(Items.LAVA_BUCKET) ? UseRaytrace.LAVA : UseRaytrace.WATER;
    }

    /**
     * Mirrors {@code BucketPickup#pickupBlock} returning a non-empty stack.
     */
    private static boolean canPickUp(@NonNull BlockState state) {
        if (state.is(Blocks.WATER) || state.is(Blocks.LAVA)) {
            // LiquidBlock: only sources
            return state.getValue(Properties.LEVEL) == 0;
        }
        // SimpleWaterloggedBlock: only if waterlogged
        return Boolean.TRUE.equals(state.getValueNullable(Properties.WATERLOGGED))
            || state.is(Blocks.BUBBLE_COLUMN) || state.is(Blocks.POWDER_SNOW);
    }

    /**
     * Mirrors {@code BucketItem#emptyContents} returning true.
     * <p>
     * Once the fluid may be placed, the only client-side failure is {@code Level#setBlock} failing for a non-source
     * block (outside the build height); that is not checked. Evaporating water in the nether also counts as success.
     *
     * @param hit the original hit, or null when retrying next to the clicked block
     */
    private static boolean canEmpty(@NonNull InteractionContext player, int content, @NonNull Vector3i pos, @Nullable BlockHit hit) {
        BlockState state = player.blockAt(pos);
        boolean mayReplace = canBeReplacedByFluid(player, state);
        boolean placeLiquid = mayReplace || (isLiquidContainer(state) && canPlaceLiquid(state, content));
        boolean canPlace = isAir(state) || (placeLiquid && (!player.isSecondaryUseActive() || hit == null));
        if (!canPlace) {
            return hit != null && canEmpty(player, content, hit.adjacentPosition(), null);
        }
        return true;
    }

    /**
     * Whether the block implements {@code LiquidBlockContainer}: waterloggable blocks and underwater plants.
     */
    private static boolean isLiquidContainer(@NonNull BlockState state) {
        return state.getValueNullable(Properties.WATERLOGGED) != null
            || state.is(Blocks.KELP) || state.is(Blocks.KELP_PLANT) || state.is(Blocks.SEAGRASS) || state.is(Blocks.TALL_SEAGRASS);
    }

    /**
     * Mirrors {@code LiquidBlockContainer#canPlaceLiquid}: waterloggable blocks accept water, underwater plants nothing.
     */
    private static boolean canPlaceLiquid(@NonNull BlockState state, int content) {
        return content == UseRaytrace.WATER && state.getValueNullable(Properties.WATERLOGGED) != null;
    }

    /**
     * Mirrors {@code BlockBehaviour#canBeReplaced(BlockState, Fluid)}: replaceable or not solid.
     */
    private static boolean canBeReplacedByFluid(@NonNull InteractionContext player, @NonNull BlockState state) {
        return InteractionTables.StateFlag.REPLACEABLE.test(state) || !InteractionTables.StateFlag.SOLID.test(state);
    }

    private static boolean isAir(@NonNull BlockState state) {
        return state.is(Blocks.AIR) || state.is(Blocks.CAVE_AIR) || state.is(Blocks.VOID_AIR);
    }

    private UseWorldInteractions() {
    }
}
