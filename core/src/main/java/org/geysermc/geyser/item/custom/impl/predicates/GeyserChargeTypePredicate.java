package org.geysermc.geyser.item.custom.impl.predicates;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.geyser.api.predicate.MinecraftPredicate;
import org.geysermc.geyser.api.predicate.context.item.JavaChargedProjectile;
import org.geysermc.geyser.api.predicate.context.item.GeyserItemPredicateContext;
import org.geysermc.geyser.api.predicate.item.ChargeTypePredicate;
import org.geysermc.geyser.impl.GeyserCoreProvided;

import java.util.Objects;

public record GeyserChargeTypePredicate(JavaChargedProjectile.ChargeType type, boolean negated) implements ChargeTypePredicate, GeyserCoreProvided {

    public GeyserChargeTypePredicate {
        Objects.requireNonNull(type, "charge type cannot be null");
    }

    @Override
    public boolean test(GeyserItemPredicateContext context) {
        return negated != context.chargedProjectiles().stream().anyMatch(projectile -> projectile.type() == this.type);
    }

    @Override
    public @NonNull MinecraftPredicate<GeyserItemPredicateContext> negate() {
        return new GeyserChargeTypePredicate(type, !negated);
    }
}
