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

package org.geysermc.geyser.text;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.geyser.api.util.TranslationManager;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class GeyserTranslationManager implements TranslationManager {

    @Override
    public @NonNull List<String> locales() {
        return MinecraftLocale.LOCALE_MAPPINGS.keySet().stream().toList();
    }

    @Override
    public @NonNull String defaultLocale() {
        return GeyserLocale.getDefaultLocale();
    }

    @Override
    public @Nullable String javaTranslation(String key, String locale) {
        return MinecraftLocale.getLocaleStringIfPresent(key, locale);
    }

    @Override
    public @Nullable String geyserTranslation(String key, String locale) {
        return GeyserLocale.getPlayerLocaleString(key, locale);
    }

    @Override
    public boolean registerJavaTranslation(String key, String locale, String translation) {
        Map<String, String> localeMap = MinecraftLocale.LOCALE_MAPPINGS.get(locale);
        if (localeMap == null) {
            return false;
        }

        String old = localeMap.get(key);
        if (old != null) {
            return false;
        }

        localeMap.put(key, translation);
        return true;
    }

    @Override
    public boolean registerJavaTranslationOverride(String key, String locale, String translation) {
        Map<String, String> localeMap = MinecraftLocale.LOCALE_MAPPINGS.get(locale);
        if (localeMap == null) {
            return false;
        }

        localeMap.put(key, translation);
        return true;
    }

    @Override
    public boolean registerGeyserTranslationOverride(String key, String locale, String translation) {
        Properties translations = GeyserLocale.LOCALE_MAPPINGS.get(locale);
        if (translations == null) {
            return false;
        }
        translations.put(key, translation);
        return true;
    }
}
