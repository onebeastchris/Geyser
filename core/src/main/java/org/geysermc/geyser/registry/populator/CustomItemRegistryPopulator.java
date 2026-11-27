/*
 * Copyright (c) 2019-2025 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.registry.populator;

import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.nbt.NbtMap;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.api.item.custom.CustomRenderOffsets;
import org.geysermc.geyser.api.item.custom.v2.CustomItemBedrockOptions;
import org.geysermc.geyser.api.item.custom.v2.CustomItemDefinition;
import org.geysermc.geyser.api.item.custom.v2.CustomItemDefinitionRegisterException;
import org.geysermc.geyser.api.item.custom.v2.NonVanillaCustomItemDefinition;
import org.geysermc.geyser.api.item.custom.v2.component.geyser.GeyserBlockPlacer;
import org.geysermc.geyser.api.item.custom.v2.component.geyser.GeyserChargeable;
import org.geysermc.geyser.api.item.custom.v2.component.geyser.GeyserItemDataComponents;
import org.geysermc.geyser.api.item.custom.v2.component.geyser.GeyserThrowableComponent;
import org.geysermc.geyser.api.predicate.MinecraftPredicate;
import org.geysermc.geyser.api.predicate.context.item.ItemPredicateContext;
import org.geysermc.geyser.api.predicate.item.ItemConditionPredicate;
import org.geysermc.geyser.api.util.CreativeCategory;
import org.geysermc.geyser.api.util.Identifier;
import org.geysermc.geyser.api.util.Unit;
import org.geysermc.geyser.event.type.GeyserDefineCustomItemsEventImpl;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.item.custom.GeyserCustomItemBedrockOptions;
import org.geysermc.geyser.item.custom.GeyserCustomItemDefinition;
import org.geysermc.geyser.item.exception.InvalidItemComponentsException;
import org.geysermc.geyser.item.type.BlockItem;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.geyser.registry.java.BuiltInJavaRegistries;
import org.geysermc.geyser.registry.mappings.BuiltInMappings;
import org.geysermc.geyser.registry.mappings.MappingsConfigReader;
import org.geysermc.geyser.registry.mappings.MappingsType;
import org.geysermc.geyser.registry.populator.custom.CustomItemContext;
import org.geysermc.geyser.registry.type.NonVanillaItemRegistration;
import org.geysermc.geyser.util.MinecraftKey;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.AttackRange;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.Consumable;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponents;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.Equippable;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.FoodProperties;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.SwingAnimation;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.ToolData;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.UseCooldown;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.UseEffects;
import dev.opencollab.paletter.item.CustomItem;
import dev.opencollab.paletter.item.component.AllowOffHand;
import dev.opencollab.paletter.item.component.BlockPlacer;
import dev.opencollab.paletter.item.component.CanDestroyInCreative;
import dev.opencollab.paletter.item.component.Compostable;
import dev.opencollab.paletter.item.component.Cooldown;
import dev.opencollab.paletter.item.component.Damage;
import dev.opencollab.paletter.item.component.Digger;
import dev.opencollab.paletter.item.component.ItemDisplayName;
import dev.opencollab.paletter.item.component.Durability;
import dev.opencollab.paletter.item.component.Dyeable;
import dev.opencollab.paletter.item.component.Enchantable;
import dev.opencollab.paletter.item.component.EntityPlacer;
import dev.opencollab.paletter.item.component.Food;
import dev.opencollab.paletter.item.component.FrameCount;
import dev.opencollab.paletter.item.component.Fuel;
import dev.opencollab.paletter.item.component.Glint;
import dev.opencollab.paletter.item.component.HandEquipped;
import dev.opencollab.paletter.item.component.Icon;
import dev.opencollab.paletter.item.component.ItemComponent;
import dev.opencollab.paletter.item.component.ItemsOrTags;
import dev.opencollab.paletter.item.component.KineticWeapon;
import dev.opencollab.paletter.item.component.MaxStackSize;
import dev.opencollab.paletter.item.component.MiningSpeed;
import dev.opencollab.paletter.item.component.PiercingWeapon;
import dev.opencollab.paletter.item.component.Projectile;
import dev.opencollab.paletter.item.component.RawItemComponent;
import dev.opencollab.paletter.item.component.RenderOffsets;
import dev.opencollab.paletter.Vec3;
import dev.opencollab.paletter.item.component.Repairable;
import dev.opencollab.paletter.item.component.Shooter;
import dev.opencollab.paletter.item.component.SwingDuration;
import dev.opencollab.paletter.item.component.Tags;
import dev.opencollab.paletter.item.component.ThrowableItem;
import dev.opencollab.paletter.item.component.UseAnimation;
import dev.opencollab.paletter.item.component.UseModifiers;
import dev.opencollab.paletter.item.component.Wearable;
import dev.opencollab.paletter.vanilla.BedrockItemType;
import dev.opencollab.paletter.vanilla.BedrockItems;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomItemRegistryPopulator {
    /**
     * Added to every custom item, for Molang queries; it is not a recipe group.
     */
    public static final String CUSTOM_ITEM_MARKER_TAG = "geyser:is_custom";

    // default to high use duration to slow down the player
    private static final float DEFAULT_ITEM_USE_DURATION = 1000.0F;

    private static final AttackRange DEFAULT_ATTACK_RANGE = new AttackRange(0.0F, 3.0F, 0.0F, 5.0F, 0.3F, 1.0F);
    private static final UseEffects DEFAULT_USE_EFFECTS = new UseEffects(false, true, 0.2F);

    /**
     * Bedrock items that are thrown by holding down use
     */
    private static final Set<String> THROWN_ITEMS = Stream.of(BedrockItems.EXPERIENCE_BOTTLE, BedrockItems.EGG,
        BedrockItems.ENDER_PEARL, BedrockItems.ENDER_EYE, BedrockItems.LINGERING_POTION, BedrockItems.SNOWBALL,
        BedrockItems.SPLASH_POTION).map(BedrockItemType::identifier).collect(Collectors.toUnmodifiableSet());

    /**
     * @return the extension or mappings file each custom item comes from
     */
    public static Map<CustomItemDefinition, String> populate(List<Item> items, Multimap<Identifier, CustomItemDefinition> customItems,
                                                             Multimap<Identifier, NonVanillaCustomItemDefinition> nonVanillaCustomItems) {
        Map<CustomItemDefinition, String> sources = new Reference2ObjectOpenHashMap<>();
        if (!GeyserImpl.getInstance().config().gameplay().enableCustomContent()) {
            return sources;
        }

        // Load custom items from mappings files
        MappingsConfigReader.loadCustomMappingsPerFile(MappingsType.ITEMS, file -> (identifier, item) -> {
            try {
                validateVanillaOverride(identifier, item, customItems);
                customItems.get(identifier).add(item);
                sources.put(item, CustomContentSources.mappingsFile(file));
            } catch (CustomItemDefinitionRegisterException exception) {
                GeyserImpl.getInstance().getLogger().error("Not registering custom item definition (bedrock identifier=" + item.bedrockIdentifier() + "): " + exception.getMessage());
            }
        });

        GeyserImpl.getInstance().eventBus().fire(new GeyserDefineCustomItemsEventImpl(customItems, nonVanillaCustomItems) {

            @Override
            public void register(@NonNull Identifier identifier, @NonNull CustomItemDefinition definition) {
                try {
                    validateVanillaOverride(identifier, definition, customItems);
                    customItems.get(identifier).add(definition);
                    sources.put(definition, CustomContentSources.callingExtension());
                } catch (CustomItemDefinitionRegisterException registerException) {
                    throw new CustomItemDefinitionRegisterException("Not registering custom item definition (bedrock identifier=" + definition.bedrockIdentifier() + "): " + registerException.getMessage());
                }
            }

            @Override
            public void register(@NonNull NonVanillaCustomItemDefinition definition) {
                if (definition.identifier().vanilla()) {
                    throw new CustomItemDefinitionRegisterException("Non-vanilla custom item definition (identifier=" + definition.identifier() + ") is attempting to masquerade as a vanilla Minecraft item!");
                } else if (definition.bedrockIdentifier().vanilla()) {
                    throw new CustomItemDefinitionRegisterException("Non-vanilla custom item definition (identifier=" + definition.identifier() + ")' bedrock identifier's namespace is minecraft!");
                } else if (definition.javaId() < items.size()) {
                    throw new CustomItemDefinitionRegisterException("Non-vanilla custom item definition (identifier=" + definition.identifier() + ") is attempting to overwrite a vanilla Minecraft item! (item network ID taken)");
                }

                for (NonVanillaCustomItemDefinition existing : nonVanillaCustomItems.values()) {
                    if (existing.identifier().equals(definition.identifier()) || existing.javaId() == definition.javaId()) {
                        // Until predicates are a thing, then predicate conflict detection should be used like with vanilla items
                        throw new CustomItemDefinitionRegisterException("A non-vanilla custom item definition (identifier=" + definition.identifier() + ", network ID=" + definition.javaId() + ") is already registered!");
                    }
                }
                nonVanillaCustomItems.put(definition.identifier(), definition);
                sources.put(definition, CustomContentSources.callingExtension());
            }
        });

        int customItemCount = customItems.size() + nonVanillaCustomItems.size();
        if (customItemCount > 0) {
            GeyserImpl.getInstance().getLogger().info("Registered " + customItemCount + " custom items");
        }
        return sources;
    }

    /**
     * Validates a custom item for a vanilla Java item and translates it into Bedrock components.
     */
    public static List<ItemComponent> registerCustomItem(Item javaItem, CustomItemDefinition customItem,
                                                    boolean firstMappingsPass) throws InvalidItemComponentsException {
        CustomItemContext context = CustomItemContext.createVanillaAndValidateComponents(javaItem, customItem, firstMappingsPass);
        return customItemComponents(javaItem.javaKey(), context);
    }

    /**
     * Validates a non-vanilla custom item and translates it into Bedrock components, without registering its Java item.
     */
    public static List<ItemComponent> nonVanillaItemComponents(NonVanillaCustomItemDefinition customItem) throws InvalidItemComponentsException {
        CustomItemContext context = CustomItemContext.createNonVanillaAndValidateComponents(customItem, true);
        return customItemComponents(MinecraftKey.identifierToKey(customItem.identifier()), context);
    }

    /**
     * Registers the Java item of a non-vanilla custom item, once its Bedrock item is in the palettes (see
     * {@link #nonVanillaItemComponents}), so an item Paletter rejects is not left behind as a Java item without mapping.
     */
    public static NonVanillaItemRegistration registerCustomItem(NonVanillaCustomItemDefinition customItem) throws InvalidItemComponentsException {
        CustomItemContext context = CustomItemContext.createNonVanillaAndValidateComponents(customItem, false);
        List<ItemComponent> components = customItemComponents(MinecraftKey.identifierToKey(customItem.identifier()), context);
        Item javaItem = new Item(customItem.identifier().toString(), Item.builder()
            .components(context.components())
            .resolvableComponents(context.resolvableComponents()));
        Items.register(javaItem, customItem.javaId());
        return new NonVanillaItemRegistration(javaItem, components, customItem.translationString());
    }

    private static void validateVanillaOverride(Identifier vanillaIdentifier, CustomItemDefinition item, Multimap<Identifier, CustomItemDefinition> registered) throws CustomItemDefinitionRegisterException {
        if (BuiltInJavaRegistries.ITEM.get(MinecraftKey.identifierToKey(vanillaIdentifier)).filter(vanilla -> vanilla.bedrockType() != null).isEmpty()) {
            throw new CustomItemDefinitionRegisterException("unknown Java item " + vanillaIdentifier);
        }
        Identifier bedrockIdentifier = item.bedrockIdentifier();
        if (bedrockIdentifier.vanilla()) {
            throw new CustomItemDefinitionRegisterException("custom item bedrock identifier namespace can't be minecraft");
        } else if (item.model().equals(vanillaIdentifier) && item.predicates().isEmpty() && !BuiltInMappings.isRegistering()) {
            GeyserImpl.getInstance().getLogger().warning("Custom item " + bedrockIdentifier + " overrides the vanilla item model " + vanillaIdentifier + " without additional predicates!");
        }

        for (Map.Entry<Identifier, CustomItemDefinition> entry : registered.entries()) {
            if (entry.getValue().bedrockIdentifier().equals(item.bedrockIdentifier())) {
                throw new CustomItemDefinitionRegisterException("conflicts with another custom item definition with the same bedrock identifier");
            }
            try {
                checkPredicate(entry, vanillaIdentifier, item);
            } catch (CustomItemDefinitionRegisterException exception) {
                throw new CustomItemDefinitionRegisterException("conflicts with custom item definition (bedrock identifier=" + entry.getValue().bedrockIdentifier() + "): " + exception.getMessage());
            }
        }
    }

    private static void checkPredicate(Map.Entry<Identifier, CustomItemDefinition> existing, Identifier vanillaIdentifier,
                                       CustomItemDefinition newItem) throws CustomItemDefinitionRegisterException {
        // If the definitions are for different Java items or models then it doesn't matter
        if (!vanillaIdentifier.equals(existing.getKey()) || !newItem.model().equals(existing.getValue().model())) {
            return;
        }
        // If they both don't have predicates they conflict
        if (existing.getValue().predicates().isEmpty() && newItem.predicates().isEmpty()) {
            throw new CustomItemDefinitionRegisterException("both entries don't have predicates, one must have a predicate");
        }

        // If their amount of predicates is equal, and the new definition contains all the existing predicates, then they also conflict
        if (existing.getValue().predicates().size() == newItem.predicates().size()) {
            boolean equal = true;

            for (MinecraftPredicate<?> predicate : existing.getValue().predicates()) {
                // This .contains() only works for common predicates that are backed using record classes in the API module!! This is because those properly work with .equals()
                // Custom predicates defined by API users (not by JSON mappings, those only use common predicates) will not work with conflict detection here!
                if (!newItem.predicates().contains(predicate)) {
                    equal = false;
                    break;
                }
            }
            if (equal) {
                throw new CustomItemDefinitionRegisterException("both entries have the same predicates");
            }
        }
    }

    /**
     * Translates a custom item (vanilla based or non-vanilla) and its Java components into Bedrock components.
     *
     * @param itemIdentifier the Java item, also the default cooldown group
     */
    private static List<ItemComponent> customItemComponents(Key itemIdentifier, CustomItemContext context) {
        CustomItemDefinition definition = context.definition();
        DataComponents components = context.components();
        CustomItemBedrockOptions options = definition.bedrockOptions();
        List<ItemComponent> result = new ArrayList<>();

        // Don't send an icon if the item has a block placer component, and is set to use its block as icon
        // This makes bedrock use a 3D render of the block this item places as icon
        GeyserBlockPlacer definedBlockPlacer = definition.components().get(GeyserItemDataComponents.BLOCK_PLACER);
        if (definedBlockPlacer == null || !definedBlockPlacer.useBlockIcon()) {
            Map<String, String> textures = new LinkedHashMap<>();
            textures.put("default", definition.icon());
            if (options.icons().dyedIcon() != null) {
                textures.put("dyed", options.icons().dyedIcon());
            }
            result.add(Icon.builder().textures(textures).build());
        }

        result.add(ItemDisplayName.builder().value(definition.displayName()).build());

        // A Geyser tag allows Molang queries; then the tags of the definition
        // Sorted: the API hands them over as an unordered set, and the order would change between starts
        List<String> tags = new ArrayList<>();
        tags.add(CUSTOM_ITEM_MARKER_TAG);
        options.tags().stream().map(Identifier::toString).sorted().forEach(tags::add);
        result.add(Tags.builder().tags(tags).build());

        result.add(AllowOffHand.of(options.allowOffhand()));
        result.add(HandEquipped.of(options.displayHandheld()));

        // Java requires the stack size to be 1 when max damage is above 0 (checked in CustomItemContext#checkComponents).
        // Missing if a non-vanilla item didn't specify one, or a component patch removed it; vanilla then defaults to 1
        int stackSize = components.getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 1);
        result.add(MaxStackSize.of(Math.min(stackSize, Item.BEDROCK_MAX_STACK_SIZE)));

        // Ignore durability if the item's predicates require it to be unbreakable
        int maxDamage = components.getOrDefault(DataComponentTypes.MAX_DAMAGE, 0);
        if (maxDamage > 0 && !isUnbreakableItem(definition)) {
            result.add(Durability.builder().maxDurability(maxDamage).build());
        }

        // The client can't destroy any block with the item: Molang '1' matches every block, at the lowest speed.
        // Break speed is calculated server side, in BedrockActionTranslator
        result.add(Digger.builder()
            .destroySpeeds(Digger.DestroySpeeds.builder().block(Digger.Block.builder().tags("1").build()).speed(1).build())
            .useEfficiency(false)
            .build());
        result.add(MiningSpeed.of(1.0F));

        Integer attackDamage = definition.components().get(GeyserItemDataComponents.ATTACK_DAMAGE);
        if (attackDamage != null) {
            result.add(Damage.of(attackDamage));
        }

        ToolData toolData = components.get(DataComponentTypes.TOOL);
        result.add(CanDestroyInCreative.of(toolData == null || toolData.isCanDestroyBlocksInCreative()));

        if (components.get(DataComponentTypes.REPAIRABLE) != null) {
            // TODO: only allow repair by the same Java item (not Bedrock item) and repair materials (needs mapping item tags).
            //  Molang '1' matches every item.
            result.add(Repairable.builder().repairItems(Repairable.RepairItems.builder().items(ItemsOrTags.tagQuery("1")).repairAmount(1.0F).build()).build());
        }

        // The client's built-in render offsets of armor converted from v1 custom mappings (a bare string, see RenderOffsets)
        RenderOffsets.Preset armorPreset = null;
        Equippable equippable = components.get(DataComponentTypes.EQUIPPABLE);
        if (equippable != null) {
            int protectionValue = options.protectionValue();
            if (options instanceof GeyserCustomItemBedrockOptions geyserOptions) {
                // Fallback to vanilla item's protection value if no protection value is set
                protectionValue = geyserOptions.protectionValue(context);
            }
            Wearable.Slot slot = switch (equippable.slot()) {
                case BOOTS -> Wearable.Slot.SLOT_ARMOR_FEET;
                case LEGGINGS -> Wearable.Slot.SLOT_ARMOR_LEGS;
                case CHESTPLATE -> Wearable.Slot.SLOT_ARMOR_CHEST;
                case HELMET -> Wearable.Slot.SLOT_ARMOR_HEAD;
                case BODY -> Wearable.Slot.SLOT_ARMOR_BODY;
                default -> null;
            };
            if (slot != null) {
                result.add(Wearable.builder().slot(slot).protection(protectionValue).build());
                if (definition instanceof GeyserCustomItemDefinition geyserDefinition && geyserDefinition.isOldConvertedItem()) {
                    armorPreset = switch (equippable.slot()) {
                        case BOOTS -> RenderOffsets.Preset.BOOTS;
                        case LEGGINGS -> RenderOffsets.Preset.LEGGINGS;
                        case CHESTPLATE -> RenderOffsets.Preset.CHESTPLATES;
                        case HELMET -> RenderOffsets.Preset.HELMETS;
                        default -> null;
                    };
                }
            }
        }

        Integer enchantmentValue = components.get(DataComponentTypes.ENCHANTABLE);
        if (enchantmentValue != null && enchantmentValue > 0) {
            result.add(Enchantable.builder().slot("all").value(enchantmentValue).build());
        }

        Boolean glint = components.get(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        if (glint != null) {
            result.add(Glint.of(glint));
        }

        Integer dyeable = options.defaultDyeableColor();
        if (dyeable != null) {
            // Not included by default for items that override vanilla dyeable items, like leather armour: on Java, dyeable
            // items are defined through recipes, so we don't know whether the vanilla item actually is dyeable (it could've
            // been removed from the recipe, or a vanilla item made dyeable). Users have to set it in their mappings.
            result.add(Dyeable.builder().defaultColor((dyeable >> 16) & 0xFF, (dyeable >> 8) & 0xFF, dyeable & 0xFF).build());
        }

        AttackRange attackRange = components.getOrDefault(DataComponentTypes.ATTACK_RANGE, DEFAULT_ATTACK_RANGE);
        org.geysermc.mcprotocollib.protocol.data.game.item.component.KineticWeapon kineticWeapon = components.get(DataComponentTypes.KINETIC_WEAPON);
        if (kineticWeapon != null) {
            KineticWeapon.Builder weapon = KineticWeapon.builder()
                .delay(kineticWeapon.delayTicks())
                .damageModifier(0.0F)
                .damageMultiplier(1.0F)
                .reach(KineticWeapon.Reach.builder().min(attackRange.minReach()).max(attackRange.maxReach()).build())
                .creativeReach(KineticWeapon.CreativeReach.builder().min(attackRange.minCreativeReach()).max(attackRange.maxCreativeReach()).build())
                .hitboxMargin(attackRange.hitboxMargin()); // TODO is this 1-to-1 with Java?
            var dismount = kineticWeapon.dismountConditions();
            if (dismount != null) {
                weapon.dismountConditions(KineticWeapon.Conditions.builder()
                    .maxDuration(dismount.maxDurationTicks()).minSpeed(dismount.minSpeed()).minRelativeSpeed(dismount.minRelativeSpeed()).build());
            }
            result.add(weapon.build());
        }

        if (components.get(DataComponentTypes.PIERCING_WEAPON) != null) {
            result.add(PiercingWeapon.builder()
                .reach(PiercingWeapon.Reach.builder().min(attackRange.minReach()).max(attackRange.maxReach()).build())
                .creativeReach(PiercingWeapon.CreativeReach.builder().min(attackRange.minCreativeReach()).max(attackRange.maxCreativeReach()).build())
                .hitboxMargin(attackRange.hitboxMargin())
                .build());
        }

        // Technically present on all items in vanilla Minecraft, but Bedrock has its own defaults when it isn't sent
        SwingAnimation attackAnimation = components.get(DataComponentTypes.ATTACK_ANIMATION);
        if (attackAnimation != null) {
            result.add(SwingDuration.builder().value(attackAnimation.duration() / 20.0F).build()); // Java is in ticks, bedrock is in seconds
        }

        Optional<Consumable> consumableComponent = Optional.ofNullable(components.get(DataComponentTypes.CONSUMABLE))
            .or(() -> context.vanillaItem().flatMap(vanilla -> {
                // If there is no consumable component, and the vanilla item is a trident, manually add a consumable component
                // with a spear animation and a really high consume duration, to get the trident animation in 3rd-person working
                if (vanilla.bedrockType().identifier().equals(BedrockItems.TRIDENT.identifier())) {
                    return Optional.of(new Consumable(DEFAULT_ITEM_USE_DURATION, Consumable.ItemUseAnimation.TRIDENT, null, false, List.of()));
                }
                return Optional.empty();
            }));

        consumableComponent.ifPresent(consumable -> {
            UseAnimation.Value animation = useAnimation(consumable.animation());
            if (animation != null) {
                result.add(UseAnimation.of(animation));
            }
            FoodProperties food = components.get(DataComponentTypes.FOOD);
            result.add(Food.builder()
                .nutrition(food == null ? 0 : food.getNutrition())
                .saturationModifier(food == null ? 0.0F : food.getSaturationModifier())
                .canAlwaysEat(food == null || food.isCanAlwaysEat())
                .build());
        });

        UseCooldown useCooldown = components.get(DataComponentTypes.USE_COOLDOWN);
        if (useCooldown != null) {
            Key group = useCooldown.cooldownGroup() == null ? itemIdentifier : useCooldown.cooldownGroup();
            result.add(Cooldown.builder().category(group.asString()).duration(useCooldown.seconds()).build());
        }

        // A block placer specified on the definition wins over the one derived from the vanilla mapping
        GeyserBlockPlacer blockPlacer = definedBlockPlacer;
        if (blockPlacer == null) {
            blockPlacer = context.vanillaItem().map(vanilla -> {
                String bedrockIdentifier = vanilla.bedrockType().identifier();
                if (bedrockIdentifier.equals(BedrockItems.FIRE_CHARGE.identifier()) || bedrockIdentifier.equals(BedrockItems.FLINT_AND_STEEL.identifier())) {
                    return GeyserBlockPlacer.builder().block(Identifier.of("fire")).build();
                } else if (vanilla instanceof BlockItem) {
                    return GeyserBlockPlacer.builder().block(Identifier.of(bedrockIdentifier)).build();
                }
                return null;
            }).orElse(null);
        }
        if (blockPlacer != null) {
            // All block items get this component, to prevent double placement. Bedrock Dedicated Server always sends
            // canUseBlockAsIcon as true; the block is the icon only if the definition asks for it
            result.add(BlockPlacer.builder().block(blockPlacer.block().toString()).build());
            result.add(RawItemComponent.of(BlockPlacer.NAME, NbtMap.builder().putBoolean("canUseBlockAsIcon", blockPlacer.useBlockIcon()).build()));
        }

        GeyserChargeable chargeable = context.vanillaItem().map(vanilla -> vanilla.bedrockType().identifier()).map(identifier -> {
            if (identifier.equals(BedrockItems.BOW.identifier())) {
                return GeyserChargeable.builder().maxDrawDuration(1.0F).ammunition(Identifier.of("arrow")).build();
            } else if (identifier.equals(BedrockItems.CROSSBOW.identifier())) {
                return GeyserChargeable.builder().chargeOnDraw(true).ammunition(Identifier.of("arrow")).build();
            }
            return null;
        }).orElse(definition.components().get(GeyserItemDataComponents.CHARGEABLE));
        if (chargeable != null) {
            result.add(FrameCount.of(chargeable.chargeOnDraw() ? 10 : 3));
            Shooter.Builder shooter = Shooter.builder()
                .chargeOnDraw(chargeable.chargeOnDraw())
                .maxDrawDuration(chargeable.maxDrawDuration())
                .scalePowerByDrawDuration(true);
            for (Identifier ammunition : chargeable.ammunition()) {
                shooter.ammunition(Shooter.Ammunition.builder().item(ammunition.toString())
                    .useOffhand(true).useInCreative(false).searchInventory(true).build());
            }
            result.add(shooter.build());
        }

        GeyserThrowableComponent throwable = context.vanillaItem().map(vanilla -> vanilla.bedrockType().identifier()).map(identifier -> THROWN_ITEMS.contains(identifier) ? GeyserThrowableComponent.of(true) : null).orElse(definition.components().get(GeyserItemDataComponents.THROWABLE));
        if (throwable != null) {
            // Allows the item to be thrown when holding down right click; the projectile is what the swing needs
            result.add(ThrowableItem.builder().doSwingAnimation(throwable.doSwingAnimation()).build());
            result.add(Projectile.builder().projectileEntity("minecraft:snowball").build());
        } else if (definition.components().get(GeyserItemDataComponents.PROJECTILE) != null) {
            // The projectile does not have to match the actual one, since we control what entity actually spawns
            result.add(Projectile.builder().projectileEntity("minecraft:snowball").build());
        }

        // Bedrock doesn't really use these otherwise
        if (throwable != null || chargeable != null || consumableComponent.isPresent()) {
            UseEffects effects = components.getOrDefault(DataComponentTypes.USE_EFFECTS, DEFAULT_USE_EFFECTS);
            float useDuration = consumableComponent.map(Consumable::consumeSeconds).orElse(DEFAULT_ITEM_USE_DURATION);
            // TODO: test if the movement modifier is 1-to-1 with Java, it probably isn't
            result.add(UseModifiers.builder().useDuration(useDuration).movementModifier(effects.speedMultiplier()).build());
        }

        Unit entityPlacer = context.vanillaItem()
            .filter(Item::entityPlacer)
            .map(vanilla -> Unit.INSTANCE)
            .orElse(definition.components().get(GeyserItemDataComponents.ENTITY_PLACER));
        if (entityPlacer != null) {
            // Prevents double placement; the entity does not have to match the actual one, since we control what spawns
            result.add(EntityPlacer.builder().entity("minecraft:minecart").build());
        }

        if (components.get(DataComponentTypes.COMPOSTABLE) != null) {
            result.add(Compostable.builder().compostingChance(1).build());
        }

        // The client only lets an item into furnace fuel slots when it has this component.
        if (components.get(DataComponentTypes.COOKING_FUEL) != null) {
            result.add(Fuel.of(1.0F));
        }

        // this is not ideal, remove once v1 is removed!
        RenderOffsets.Hand mainHand = null;
        RenderOffsets.Hand offHand = null;
        if (definition instanceof GeyserCustomItemDefinition geyserDefinition && geyserDefinition.isOldConvertedItem()) {
            CustomRenderOffsets renderOffsets = geyserDefinition.getRenderOffsets();
            if (renderOffsets != null) {
                mainHand = hand(renderOffsets.mainHand());
                offHand = hand(renderOffsets.offhand());
            } else if (geyserDefinition.getTextureSize() != 16) {
                float scale1 = (float) (0.075 / (geyserDefinition.getTextureSize() / 16f));
                float scale2 = (float) (0.125 / (geyserDefinition.getTextureSize() / 16f));
                float scale3 = (float) (0.075 / (geyserDefinition.getTextureSize() / 16f * 2.4f));
                mainHand = hand(RenderOffsets.Offset.scale(scale3, scale3, scale3), RenderOffsets.Offset.scale(scale1, scale2, scale1));
                offHand = hand(RenderOffsets.Offset.scale(scale1, scale2, scale1), RenderOffsets.Offset.scale(scale1, scale2, scale1));
            }
        }
        if (mainHand != null || offHand != null || armorPreset != null) {
            result.add(RenderOffsets.builder().mainHand(mainHand).offHand(offHand).preset(armorPreset).build());
        }

        return result;
    }

    private static UseAnimation.@Nullable Value useAnimation(Consumable.ItemUseAnimation animation) {
        return switch (animation) {
            case NONE -> UseAnimation.Value.NONE;
            case EAT -> UseAnimation.Value.EAT;
            case DRINK -> UseAnimation.Value.DRINK;
            case BLOCK -> UseAnimation.Value.BLOCK;
            case BOW -> UseAnimation.Value.BOW;
            case SPEAR -> UseAnimation.Value.SPEAR;
            case CROSSBOW -> UseAnimation.Value.CROSSBOW;
            case SPYGLASS -> UseAnimation.Value.SPYGLASS;
            case BRUSH -> UseAnimation.Value.BRUSH;
            // No Bedrock counterpart (the trident gets its third person animation from the use duration alone)
            default -> null;
        };
    }

    private static RenderOffsets.Hand hand(RenderOffsets.@Nullable Offset firstPerson, RenderOffsets.@Nullable Offset thirdPerson) {
        return RenderOffsets.Hand.builder().firstPerson(firstPerson).thirdPerson(thirdPerson).build();
    }

    // A hand, view or vector the mapping leaves out is left out on the wire too, so the client keeps its own default for it
    // instead of an identity offset (a scale of 1 draws the item far larger than the client's default)

    private static RenderOffsets.@Nullable Hand hand(CustomRenderOffsets.@Nullable Hand hand) {
        if (hand == null) {
            return null;
        }
        return hand(offset(hand.firstPerson()), offset(hand.thirdPerson()));
    }

    private static RenderOffsets.@Nullable Offset offset(CustomRenderOffsets.@Nullable Offset offset) {
        if (offset == null) {
            return null;
        }
        return RenderOffsets.Offset.builder()
            .position(vec(offset.position()))
            .rotation(vec(offset.rotation()))
            .scale(vec(offset.scale()))
            .build();
    }

    private static @Nullable Vec3 vec(CustomRenderOffsets.@Nullable OffsetXYZ xyz) {
        return xyz == null ? null : Vec3.of(xyz.x(), xyz.y(), xyz.z());
    }

    private static boolean isUnbreakableItem(CustomItemDefinition definition) {
        for (MinecraftPredicate<? super ItemPredicateContext> predicate : definition.predicates()) {
            if (predicate.equals(ItemConditionPredicate.UNBREAKABLE)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Puts custom content in the creative inventory, at the end of its group (created if needed) or of its category:
     * as in Bedrock Dedicated Server, the menu category alone decides that, and Paletter places it.
     *
     * @return the builder, without a menu category (so not in the creative inventory) for categories without a creative tab
     */
    static CustomItem.Builder menuCategory(CustomItem.Builder item, @Nullable CreativeCategory category, @Nullable String group) {
        dev.opencollab.paletter.creative.CreativeCategory paletterCategory = creativeCategory(category);
        if (paletterCategory == null) {
            return item;
        }
        return group == null || group.isEmpty() ? item.menuCategory(paletterCategory) : item.menuCategory(paletterCategory, group);
    }

    /**
     * @return the Paletter creative category, or null for categories without a creative tab. ALL and ITEM_COMMAND_ONLY
     * are protocol tabs, not menu categories: the client's menu categories are construction, nature, equipment, items
     * and none (Paletter capture/WIRE-NOTES.md: ids 1 to 5, where 5, the id of ITEM_COMMAND_ONLY, is none)
     */
    static dev.opencollab.paletter.creative.@Nullable CreativeCategory creativeCategory(@Nullable CreativeCategory category) {
        if (category == null) {
            return null;
        }
        return switch (category) {
            case CONSTRUCTION -> dev.opencollab.paletter.creative.CreativeCategory.CONSTRUCTION;
            case NATURE -> dev.opencollab.paletter.creative.CreativeCategory.NATURE;
            case EQUIPMENT -> dev.opencollab.paletter.creative.CreativeCategory.EQUIPMENT;
            case ITEMS -> dev.opencollab.paletter.creative.CreativeCategory.ITEMS;
            default -> null;
        };
    }
}
