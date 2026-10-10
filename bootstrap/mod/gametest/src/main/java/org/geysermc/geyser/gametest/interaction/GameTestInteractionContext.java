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

package org.geysermc.geyser.gametest.interaction;

import it.unimi.dsi.fastutil.ints.IntList;
import net.kyori.adventure.key.Key;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.AABB;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.cloudburstmc.math.vector.Vector3d;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.gametest.interaction.world.ReflectionUtils;
import org.geysermc.geyser.gametest.interaction.world.SimulatedClientLevel;
import org.geysermc.geyser.gametest.interaction.world.SimulatedPlayer;
import org.geysermc.geyser.interaction.BlockEntityInteractionData;
import org.geysermc.geyser.gametest.mixin.EntityAccessor;
import org.geysermc.geyser.interaction.InteractionContext;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.BoundingBox;
import org.geysermc.geyser.session.cache.registry.JavaRegistryKey;
import org.geysermc.geyser.session.cache.registry.JavaRegistryProvider;
import org.geysermc.geyser.session.cache.tags.Tag;
import org.geysermc.mcprotocollib.protocol.data.game.entity.EquipmentSlot;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.HolderSet;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * {@link InteractionContext} backed by the game test world and a {@link SimulatedPlayer}: what Geyser's session would
 * know about the player and the world, taken straight from vanilla.
 * <p>
 * Create one per simulated click, before running the vanilla logic (which may mutate the player); Geyser's simulation
 * must run before vanilla's. Blocks are read from the simulated client level, so blocks a scenario placed through
 * the player's level are visible.
 */
public final class GameTestInteractionContext implements InteractionContext {
    private static final Map<String, ResourceKey<RecipePropertySet>> RECIPE_PROPERTY_SETS = findRecipePropertySets();

    private final SimulatedClientLevel level;
    private final SimulatedPlayer player;
    private final RegistryAccess registryAccess;
    private final JavaRegistryProvider registries;
    private final GameTestComponentResolver componentResolver;
    private final GeyserItemStack mainHand;
    private final GeyserItemStack offHand;
    /**
     * The vanilla stacks Geyser stacks were converted from, to answer questions about Geyser stacks with vanilla code.
     */
    private final Map<GeyserItemStack, ItemStack> vanillaStacks = new IdentityHashMap<>();
    private final Map<Integer, GeyserItemStack> inventory = new HashMap<>();

    public GameTestInteractionContext(SimulatedClientLevel level, SimulatedPlayer player, JavaRegistryProvider registries,
                                      GameTestComponentResolver componentResolver) {
        this.level = level;
        this.player = player;
        this.registryAccess = level.serverLevel().registryAccess();
        this.registries = registries;
        this.componentResolver = componentResolver;
        this.mainHand = convert(player.getItemInHand(InteractionHand.MAIN_HAND));
        this.offHand = convert(player.getItemInHand(InteractionHand.OFF_HAND));
    }

    private GeyserItemStack convert(ItemStack stack) {
        ItemStack copy = stack.copy();
        GeyserItemStack converted = VanillaConversions.toGeyser(copy, registryAccess, componentResolver);
        vanillaStacks.put(converted, copy);
        return converted;
    }

    private @Nullable ItemStack vanillaStack(GeyserItemStack stack) {
        return vanillaStacks.get(stack);
    }

    @Override
    public @NonNull GameMode gameMode() {
        return VanillaConversions.toGeyser(player.gameMode());
    }

    @Override
    public boolean isSecondaryUseActive() {
        return player.isSecondaryUseActive();
    }

    @Override
    public @NonNull GeyserItemStack itemInHand(@NonNull Hand hand) {
        return hand == Hand.MAIN_HAND ? mainHand : offHand;
    }

    @Override
    public @NonNull GeyserItemStack itemBySlot(@NonNull EquipmentSlot slot) {
        return switch (slot) {
            case MAIN_HAND -> mainHand;
            case OFF_HAND -> offHand;
            case HELMET -> convert(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD));
            case CHESTPLATE -> convert(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST));
            case LEGGINGS -> convert(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS));
            case BOOTS -> convert(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET));
            case BODY, SADDLE -> GeyserItemStack.EMPTY;
        };
    }

    @Override
    public int inventorySize() {
        return player.getInventory().getContainerSize();
    }

    @Override
    public @NonNull GeyserItemStack inventoryItem(int index) {
        if (index < 0 || index >= inventorySize()) {
            return GeyserItemStack.EMPTY;
        }
        return inventory.computeIfAbsent(index, $ -> convert(player.getInventory().getItem(index)));
    }

    @Override
    public boolean mayBuild() {
        return player.mayBuild();
    }

    @Override
    public boolean hasInfiniteMaterials() {
        return player.hasInfiniteMaterials();
    }

    @Override
    public boolean isInvulnerable() {
        return player.getAbilities().invulnerable;
    }

    @Override
    public boolean canUseGameMasterBlocks() {
        return player.canUseGameMasterBlocks();
    }

    @Override
    public int foodLevel() {
        return player.getFoodData().getFoodLevel();
    }

    @Override
    public boolean isOnCooldown(@NonNull GeyserItemStack stack) {
        ItemStack vanilla = vanillaStack(stack);
        return vanilla != null && player.getCooldowns().isOnCooldown(vanilla);
    }

    @Override
    public boolean canPlaceOnInAdventureMode(@NonNull GeyserItemStack stack, @NonNull Vector3i position) {
        ItemStack vanilla = vanillaStack(stack);
        return vanilla != null && vanilla.canPlaceOnBlockInAdventureMode(new BlockInWorld(level.level(), VanillaConversions.toVanilla(position), false));
    }

    @Override
    public @NonNull BlockState blockAt(@NonNull Vector3i position) {
        return VanillaConversions.toGeyser(level.level().getBlockState(VanillaConversions.toVanilla(position)));
    }

    @Override
    public boolean isInTag(@NonNull Tag<?> tag, int javaId) {
        return isInTag(tag.registry(), tag.tag(), javaId);
    }

    private boolean isInTag(JavaRegistryKey<?> registry, Key tag, int javaId) {
        Registry<?> vanillaRegistry = registryAccess.lookupOrThrow(ResourceKey.createRegistryKey(identifier(registry.registryKey())));
        return isInTag(vanillaRegistry, identifier(tag), javaId);
    }

    private static <T> boolean isInTag(Registry<T> registry, Identifier tag, int javaId) {
        Holder<T> holder = registry.asHolderIdMap().byId(javaId);
        return holder != null && holder.is(TagKey.create(registry.key(), tag));
    }

    @Override
    public boolean isInHolderSet(@Nullable HolderSet holderSet, @NonNull JavaRegistryKey<?> registry, int javaId) {
        if (holderSet == null) {
            return false;
        }
        IntList holders = holderSet.getHolders();
        if (holders != null) {
            return holders.contains(javaId);
        }
        Key tag = holderSet.getLocation();
        if (tag == null) {
            return false;
        }
        if (tag.value().startsWith("#")) {
            //noinspection PatternValidation
            tag = Key.key(tag.namespace(), tag.value().substring(1));
        }
        return isInTag(registry, tag, javaId);
    }

    @Override
    public boolean isInRecipePropertySet(@NonNull Key propertySet, int itemId) {
        ResourceKey<RecipePropertySet> key = RECIPE_PROPERTY_SETS.get(propertySet.asString());
        if (key == null) {
            throw new IllegalArgumentException("Unknown recipe property set " + propertySet);
        }
        // The client checks the property sets the server sent, which are the server's recipe manager's
        return level.serverLevel().recipeAccess().propertySet(key).test(new ItemStack(BuiltInRegistries.ITEM.byId(itemId)));
    }

    @Override
    public @NonNull Vector3d position() {
        return VanillaConversions.toGeyser(player.position());
    }

    @Override
    public float yaw() {
        return player.getYRot();
    }

    @Override
    public float pitch() {
        return player.getXRot();
    }

    @Override
    public double eyeHeight() {
        return player.getEyeHeight();
    }

    @Override
    public double blockInteractionRange() {
        return player.blockInteractionRange();
    }

    @Override
    public boolean isFallFlying() {
        return player.isFallFlying();
    }

    @Override
    public boolean isInWaterOrRain() {
        return player.isInWaterOrRain();
    }

    @Override
    public int minY() {
        return level.serverLevel().getMinY();
    }

    @Override
    public int maxY() {
        return level.serverLevel().getMaxY();
    }

    @Override
    public boolean hasEntityInBox(@NonNull BoundingBox box, boolean buildingBlockersOnly) {
        Vector3d min = box.getMin();
        Vector3d max = box.getMax();
        AABB aabb = new AABB(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
        // Queried through the simulated level, which includes the simulated player
        return !level.level().getEntities((Entity) null, aabb, entity -> !entity.isSpectator()
            && (!buildingBlockersOnly || (!entity.isRemoved() && blocksBuilding(entity)))).isEmpty();
    }

    private static boolean blocksBuilding(Entity entity) {
        return ((EntityAccessor) entity).geyser$blocksBuilding();
    }

    @Override
    public @Nullable BlockEntityInteractionData blockEntityData(@NonNull Vector3i position) {
        BlockEntity blockEntity = level.serverLevel().getBlockEntity(VanillaConversions.toVanilla(position));
        if (blockEntity instanceof SignBlockEntity sign) {
            BlockEntityInteractionData data = BlockEntityInteractionData.sign(sign.isWaxed(),
                sign.getFrontText().hasAnyClickCommands(player), sign.getBackText().hasAnyClickCommands(player));
            // Like the session, only non-default data is tracked
            return data.equals(BlockEntityInteractionData.NONE) ? null : data;
        }
        return null;
    }

    @Override
    public @NonNull JavaRegistryProvider registries() {
        return registries;
    }

    private static Identifier identifier(Key key) {
        return Identifier.fromNamespaceAndPath(key.namespace(), key.value());
    }

    /**
     * Maps the identifiers of the recipe property sets to their keys, from the constants in {@link RecipePropertySet}.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, ResourceKey<RecipePropertySet>> findRecipePropertySets() {
        Map<String, ResourceKey<RecipePropertySet>> result = new HashMap<>();
        for (Field field : ReflectionUtils.staticFields(RecipePropertySet.class, ResourceKey.class)) {
            ResourceKey<?> key = (ResourceKey<?>) ReflectionUtils.get(field, null);
            if (key != null) {
                result.put(key.identifier().toString(), (ResourceKey<RecipePropertySet>) key);
            }
        }
        return result;
    }

    /**
     * @return the level vanilla code should use for the player of this context
     */
    public Level vanillaLevel() {
        return level.level();
    }
}
