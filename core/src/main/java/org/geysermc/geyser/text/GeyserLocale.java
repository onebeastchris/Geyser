/*
 * Copyright (c) 2019-2022 GeyserMC. http://geysermc.org
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

import com.fasterxml.jackson.databind.JsonNode;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.geyser.GeyserBootstrap;
import org.geysermc.geyser.GeyserImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

public class GeyserLocale {

    /**
     * If we determine the default locale that the user wishes to use, use that locale
     */
    private static Locale DEFAULT_LOCALE;
    /**
     * Whether the system locale cannot be loaded by Geyser.
     */
    private static boolean SYSTEM_LOCALE_INVALID;

    private static final Map<Locale, Properties> LOCALE_MAPPINGS = new HashMap<>();

    /**
     * A set of all available locales. These include user-provided locales.
     */
    private static Set<Locale> AVAILABLE_LOCALES = new HashSet<>();

    /**
     * A map of all user provided locales/overrides for specific locales.
     */
    private static Map<Locale, Path> LOCALE_OVERRIDES = new HashMap<>();

    /**
     * Loads the initial locale(s) with the help of the bootstrap.
     */
    public static void init(GeyserBootstrap bootstrap) {
        loadAvailableLocales(bootstrap);
        Locale loadedLocale = loadGeyserLocale(Locale.getDefault(), bootstrap);
        if (loadedLocale != null) {
            DEFAULT_LOCALE = loadedLocale;
            // Load English as a backup in case something goes really wrong
            if (!Locale.US.equals(loadedLocale)) {
                loadGeyserLocale(Locale.US, bootstrap);
            }
            SYSTEM_LOCALE_INVALID = false;
        } else {
            // Unable to load default locale - probably doesn'
            DEFAULT_LOCALE = loadGeyserLocale(Locale.US, bootstrap);
            if (DEFAULT_LOCALE == null) {
                // en_US can't be loaded?
                throw new IllegalStateException("English locale not found in Geyser. Did you clone the submodules? (git submodule update --init)");
            }
            SYSTEM_LOCALE_INVALID = true;
        }
    }

    /**
     * Finalize the default locale, now that we know what the default locale should be.
     */
    public static void finalizeDefaultLocale(GeyserImpl geyser) {
        String newDefaultLocale = geyser.getConfig().getDefaultLocale();
        if (newDefaultLocale == null) {
            // We want to use the system locale which is already loaded
            return;
        }
        Locale newLocale = formatLocale(newDefaultLocale);
        Locale loadedNewLocale = loadGeyserLocale(newLocale, geyser.getBootstrap());
        if (loadedNewLocale != null) {
            // The config's locale is valid
            DEFAULT_LOCALE = loadedNewLocale;
        } else if (SYSTEM_LOCALE_INVALID) {
            geyser.getLogger().warning(Locale.getDefault().toString() + " is not a valid Bedrock language.");
        }
    }

    public static Locale getDefaultLocale() {
        return DEFAULT_LOCALE;
    }

    public static String getDefaultLocaleString() {
        return DEFAULT_LOCALE.getLanguage() + "_" + DEFAULT_LOCALE.getCountry();
    }

    public static Locale findClosestLocale(Locale initial) {
        if (AVAILABLE_LOCALES.contains(initial)) {
            return initial;
        }

        List<Locale.LanguageRange> preferences = new ArrayList<>();

        // First priority: same language
        preferences.add(new Locale.LanguageRange(initial.toLanguageTag(), 1.0));

        // Second: Same language
        preferences.add(new Locale.LanguageRange(initial.getLanguage(), 0.9));

        // Second priority: same country code
        if (!initial.getCountry().isEmpty()) {
            preferences.add(new Locale.LanguageRange("und-" + initial.getCountry(), 0.8));
        }

        // Third priority: default to English
        preferences.add(new Locale.LanguageRange("en", 0.5));

        Locale closest = Locale.lookup(preferences, AVAILABLE_LOCALES);
        GeyserImpl.getInstance().getLogger().info("Matched locale %s to %s".formatted(initial, closest));

        return closest;
    }

    /**
     * Loads a Geyser locale from resources, if the file doesn't exist it just logs a warning
     *
     * @param locale Locale to load
     */
    public static void loadGeyserLocale(Locale locale) {
        GeyserImpl geyser = GeyserImpl.getInstance();
        if (geyser == null) {
            throw new IllegalStateException("Geyser instance cannot be null when loading a locale!");
        }
        loadGeyserLocale(findClosestLocale(locale), geyser.getBootstrap());
    }

    /**
     * Loads a Geyser locale from resources, if the file doesn't exist it just logs a warning
     *
     * @param localeString Locale to load
     */
    public static void loadGeyserLocale(String localeString) {
        GeyserImpl geyser = GeyserImpl.getInstance();
        if (geyser == null) {
            throw new IllegalStateException("Geyser instance cannot be null when loading a locale!");
        }

        Locale locale = formatLocale(localeString);
        loadGeyserLocale(findClosestLocale(locale), geyser.getBootstrap());
    }

    private static @Nullable Locale loadGeyserLocale(Locale locale, GeyserBootstrap bootstrap) {
        // Don't load the locale if it's already loaded.
        if (LOCALE_MAPPINGS.containsKey(locale)) {
            return locale;
        }

        Properties localeProp = new Properties();

        File localLanguage;
        Path localFolder = bootstrap.getConfigFolder().resolve("languages");
        if (Files.exists(localFolder)) {
            localLanguage = localFolder.resolve(locale + ".properties").toFile();
        } else {
            localLanguage = null;
        }
        boolean validLocalLanguage = localLanguage != null && localLanguage.exists();

        InputStream localeStream = bootstrap.getResourceOrNull("languages/texts/" + locale + ".properties");

        // Load the locale
        if (localeStream != null) {
            try {
                try (InputStreamReader reader = new InputStreamReader(localeStream, StandardCharsets.UTF_8)) {
                    localeProp.load(reader);
                } catch (Exception e) {
                    throw new AssertionError(getLocaleStringLog("geyser.language.load_failed", locale), e);
                }

                // Insert the locale into the mappings
                LOCALE_MAPPINGS.put(locale, localeProp);
            } finally {
                try {
                    localeStream.close();
                } catch (IOException ignored) {}
            }
        } else {
            if (!validLocalLanguage) {
                // Don't warn on missing locales if a local file has been found
                bootstrap.getGeyserLogger().debug("Missing locale: " + locale);
            }
        }

        // Load any language overrides that exist after, to override any strings that we just added
        // By loading both, we ensure that if a language string doesn't exist in the custom properties folder,
        // it's loaded from our jar
        if (validLocalLanguage) {
            try (InputStream stream = new FileInputStream(localLanguage)) {
                localeProp.load(stream);
            } catch (IOException e) {
                bootstrap.getGeyserLogger().error("Unable to load custom language override!", e);
            }

            LOCALE_MAPPINGS.putIfAbsent(locale, localeProp);
        }
        return localeProp.isEmpty() ? null : locale;
    }

    /**
     * Get a formatted language string with the default locale for Geyser
     *
     * @param key Language string to translate
     * @return Translated string or the original message if it was not found in the given locale
     */
    public static String getLocaleStringLog(String key) {
        return getLocaleStringLog(key, ObjectArrays.EMPTY_ARRAY);
    }

    /**
     * Get a formatted language string with the default locale for Geyser
     *
     * @param key Language string to translate
     * @param values Values to put into the string
     * @return Translated string or the original message if it was not found in the given locale
     */
    public static String getLocaleStringLog(String key, Object... values) {
        return getPlayerLocaleString(key, getDefaultLocale(), values);
    }

    /**
     * Get a formatted language string with the given locale for Geyser
     *
     * @param key Language string to translate
     * @param locale Locale to translate to
     * @return Translated string or the original message if it was not found in the given locale
     */
    public static String getPlayerLocaleString(String key, Locale locale) {
        return getPlayerLocaleString(key, locale, ObjectArrays.EMPTY_ARRAY);
    }

    /**
     * Get a formatted language string with the given locale for Geyser
     *
     * @param key Language string to translate
     * @param locale Locale to translate to
     * @param values Values to put into the string
     * @return Translated string or the original message if it was not found in the given locale
     */
    public static String getPlayerLocaleString(String key, String locale, Object... values) {
        return getPlayerLocaleString(key, formatLocale(locale), values);
    }

    /**
     * Get a formatted language string with the given locale for Geyser
     *
     * @param key Language string to translate
     * @param locale Locale to translate to
     * @param values Values to put into the string
     * @return Translated string or the original message if it was not found in the given locale
     */
    public static String getPlayerLocaleString(String key, Locale locale, Object... values) {
        Properties properties = LOCALE_MAPPINGS.get(locale);
        String formatString = null;

        if (properties != null) {
            formatString = properties.getProperty(key);
        }

        // Try and get the key from the default locale
        if (formatString == null) {
            properties = LOCALE_MAPPINGS.get(getDefaultLocale());
            formatString = properties.getProperty(key);

            // Try and get the key from en_US (this should only ever happen in development)
            if (formatString == null) {
                properties = LOCALE_MAPPINGS.get(Locale.US);
                formatString = properties.getProperty(key);

                // Final fallback
                if (formatString == null) {
                    return key;
                }
            }
        }

        String message = formatString.replace("&", "\u00a7");
        if (values == null || values.length == 0) {
            // Nothing to replace
            return message;
        }

        return MessageFormat.format(message.replace("'", "''"), values);
    }

    /**
     * Cleans up and formats a locale string
     *
     * @param localeString The locale to format
     * @return The formatted locale
     */
    private static @Nullable Locale formatLocale(String localeString) {
        String[] parts = localeString.split("_", -1);
        String language = (parts.length > 0) ? parts[0] : "";
        String country = (parts.length > 1) ? parts[1] : "";
        String variant = (parts.length > 2) ? parts[2] : "";
        GeyserImpl.getInstance().getLogger().info(language + " " + country + " " + variant + " " + localeString);
        try {
            return new Locale(language, country, variant);
        } catch (NullPointerException e) {
            return null;
        }
    }

    private static void loadAvailableLocales(GeyserBootstrap bootstrap) {
        Set<Locale> validLocales = new HashSet<>();

        // First: figure out which locales we ship - but only do that initially.
        if (!AVAILABLE_LOCALES.isEmpty()) {
            InputStream localeStream = bootstrap.getResourceOrThrow("languages/languages.json");
            try {
                JsonNode node = GeyserImpl.JSON_MAPPER.readTree(localeStream);
                JsonNode languageNode = node.get("languages");
                languageNode.elements().forEachRemaining(jsonNode -> validLocales.add(formatLocale(jsonNode.asText())));
            } catch (IOException e) {
                throw new RuntimeException("Unable to find built-in locales. Did you clone the submodules? (git submodule update --init)", e);
            }
        }

        Map<Locale, Path> localeOverrides = new HashMap<>();

        // Now: find all user-added locales
        Path localFolder = bootstrap.getConfigFolder().resolve("languages");
        if (Files.exists(localFolder)) {
            try (Stream<Path> stream = Files.walk(localFolder)) {
                stream.forEachOrdered(path -> {
                    String fileName = path.getFileName().toString();
                    if (fileName.endsWith(".properties")) {
                        String localeString = fileName.replace(".properties", "");
                        Locale locale = formatLocale(localeString);
                        if (locale != null) {
                            validLocales.add(locale);
                            localeOverrides.put(locale, path);
                        } else {
                            bootstrap.getGeyserLogger().warning("Could not match locale for %s!".formatted(localeString));
                        }
                    }
                });
            } catch (IOException e) {
                bootstrap.getGeyserLogger().warning("Unable to load custom locales! " + e.getMessage());
            }
        }

        LOCALE_OVERRIDES = localeOverrides;
        AVAILABLE_LOCALES = validLocales;
    }
}
