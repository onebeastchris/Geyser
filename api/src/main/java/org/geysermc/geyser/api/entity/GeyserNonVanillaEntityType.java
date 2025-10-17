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

package org.geysermc.geyser.api.entity;

import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.util.Identifier;

/**
 * A class that represents entities
 */
public interface GeyserNonVanillaEntityType {

    @NonNull
    Identifier identifier();

    @Positive
    int javaId();

    boolean projectile();

    static Builder builder() {
        return GeyserApi.api().provider(GeyserNonVanillaEntityType.Builder.class);
    }

    interface Builder {

        /**
         * Sets the Java entity type identifier.
         * It is used for scoreboards and translations.
         *
         * @param identifier the entity type identifier
         * @return this builder
         */
        Builder identifier(@NonNull Identifier identifier);

        /**
         * Sets the Java network id for the entity.
         * This id cannot already be used for vanilla entities!
         *
         * @param javaId the network id
         * @return this builder
         */
        Builder javaId(int javaId);

        /**
         * Validates and creates this nonvanilla entity type representation using
         * the information provided in this builder.
         *
         * @return the entity type
         */
        GeyserNonVanillaEntityType build();
    }
}
