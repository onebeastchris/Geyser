/*
 * Copyright (c) 2019-2023 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.api.util;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

/**
 * The translation manager is used by Geyser to manage translations. You can get an instance of this class from the GeyserAPI.
 * It is used to register translations (and overrides) and to get translations - for both Java and Geyser translation strings.
 */
public interface TranslationManager {

    /**
     * Gets a list containing all currently loaded Bedrock locale codes.
     * This list is immutable, and can change during runtime - for example, when a new Bedrock client connects.
     *
     * @return A list of all loaded locales.
     */
    @NonNull List<String> locales();

    /**
     * Gets the default locale code.
     * Is en_us by default.
     *
     * @return The default locale code.
     */
    @NonNull String defaultLocale();

    /**
     * Gets the Java translation for a given key and locale.
     *
     * @param key The key of the translation.
     * @param locale The locale code.
     * @return The translation, or null if not found.
     */
    @Nullable String javaTranslation(String key, String locale);

    /**
     * Gets the Geyser translation for a given key and locale.
     *
     * @param key The key of the translation.
     * @param locale The locale code.
     * @return The translation, or null if not found.
     */
    @Nullable String geyserTranslation(String key, String locale);

    /**
     * Registers a new Java translation. This will not be registered if a translation for the key already exists.
     *
     * @param key The key of the translation.
     * @param locale The locale code.
     * @param translation The translation text.
     * @return True if the registration was successful, false otherwise.
     */
    boolean registerJavaTranslation(String key, String locale, String translation);

    /**
     * Registers a new Java translation, overriding any existing translation.
     *
     * @param key The key of the translation.
     * @param locale The locale code.
     * @param translation The translation text.
     * @return True if the registration was successful, false otherwise.
     */
    boolean registerJavaTranslationOverride(String key, String locale, String translation);

    /**
     * Overrides a Geyser translation, if it exists.
     *
     * @param key The key of the translation.
     * @param locale The locale code.
     * @param translation The translation text.
     * @return True if the override was successful, false otherwise.
     */
    boolean registerGeyserTranslationOverride(String key, String locale, String translation);
}