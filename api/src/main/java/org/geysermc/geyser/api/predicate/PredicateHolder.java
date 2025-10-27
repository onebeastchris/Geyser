/*
 * Copyright (c) 2025 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.api.predicate;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.geyser.api.predicate.context.MinecraftPredicateContext;

import java.util.List;

public interface PredicateHolder<C extends MinecraftPredicateContext> extends MinecraftPredicate<C> {

    @NonNull
    List<MinecraftPredicate<? super C>> predicates();

    @NonNull PredicateStrategy predicateStrategy();

    // TODO predicate caching like (was) done in CustomItemTranslator?
    @Override
    default boolean test(C context) {
        boolean needsOnlyOneMatch = predicateStrategy() == PredicateStrategy.OR;
        boolean allMatch = true;

        for (MinecraftPredicate<? super C> predicate : predicates()) {
            if (predicate.test(context)) {
                if (needsOnlyOneMatch) {
                    return true;
                }
            } else {
                allMatch = false;
                // If we need everything to match, that is no longer possible, so break
                if (!needsOnlyOneMatch) {
                    break;
                }
            }
        }
        return allMatch;
    }
}
