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

package org.geysermc.geyser.extension;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.api.extension.ExtensionTranslationManager;
import org.geysermc.geyser.text.GeyserLocale;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class GeyserExtensionTranslationManager implements ExtensionTranslationManager {

    private Map<String, Properties> translations;

    @Override
    public @NonNull List<String> locales() {
        return translations.keySet().stream().toList();
    }

    @Override
    public @Nullable String translation(String key, String locale) {
        Properties properties = translations.get(locale);
        if (properties == null) return null;
        return properties.getProperty(key);
    }

    @Override
    public boolean registerTranslation(String key, String locale, String translation) {
        Properties properties = translations.get(locale);
        if (properties == null) {
            properties = new Properties();
            translations.put(locale, properties);
        }
        return properties.setProperty(key, translation) == null;
    }

    @Override
    public boolean loadTranslations(String locale, Properties properties) {
        return translations.put(locale, properties) == null;
    }

    protected GeyserExtensionTranslationManager loadFromPath(Path path) {
        if (path != null) {
            try {
                Files.walk(path).filter(Files::isRegularFile).forEach(file -> {
                    String locale = file.getFileName().toString().split("\\.")[0];
                    Properties properties = new Properties();
                    try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file.toFile()), StandardCharsets.UTF_8)) {
                        properties.load(reader);
                        loadTranslations(locale, properties);
                    } catch (IOException e) {
                        GeyserImpl.getInstance().getLogger().error(GeyserLocale.getLocaleStringLog("geyser.extensions.load.translation.invalid", locale), e);
                    }
                });
            } catch (IOException e) {
                GeyserImpl.getInstance().getLogger().error(GeyserLocale.getLocaleStringLog("geyser.extensions.load.translation.failed", path), e);
            }
        }
        return this;
    }
}
