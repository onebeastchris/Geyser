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

package org.geysermc.geyser.api.extension;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Properties;

/**
 * Represents an extension translation manager used by extensions to manage translations.
 * Extensions can supply locale properties to use via a {@code translations} folder in their resources folder, those will be loaded automatically.
 * Alternatively, adding translations can be done at any point during runtime using {@link #registerTranslation(String, String, String)}.
 * It is used to register translations, get translations, and load translations from a specific path.
 */
public interface ExtensionTranslationManager {

    /**
     * Gets a list containing all currently loaded Bedrock locale codes.
     * This list is immutable, and can change during runtime - for example, when a new Bedrock client connects.
     *
     * @return A list of all loaded locales.
     */
    @NonNull List<String> locales();

    /**
     * Gets the translation for a given key and locale.
     *
     * @param key The key of the translation.
     * @param locale The locale code.
     * @return The translation, or null if not found.
     */
    @Nullable String translation(String key, String locale);

    /**
     * Registers a new translation.
     *
     * @param key The key of the translation.
     * @param locale The locale code.
     * @param translation The translation text.
     * @return True if the registration was successful, false otherwise.
     */
    boolean registerTranslation(String key, String locale, String translation);

    /**
     * Loads translations from a specific path for a given locale.
     *
     * @param locale The locale code.
     * @param properties The properties to load from.
     * @return True if the loading was successful, false otherwise.
     */
    boolean loadTranslations(String locale, Properties properties);
}
