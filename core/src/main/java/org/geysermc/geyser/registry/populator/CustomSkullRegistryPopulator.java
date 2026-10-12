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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.geyser.GeyserBootstrap;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomSkullsEvent;
import org.geysermc.geyser.configuration.GeyserCustomSkullConfiguration;
import org.geysermc.geyser.extension.GeyserExtensionClassLoader;
import org.geysermc.geyser.pack.SkullResourcePackManager;
import org.geysermc.geyser.registry.BlockRegistries;
import org.geysermc.geyser.registry.mappings.MappingsConfigReader;
import org.geysermc.geyser.registry.mappings.MappingsType;
import org.geysermc.geyser.registry.type.CustomSkull;
import org.geysermc.geyser.skin.SkinProvider;
import org.geysermc.geyser.text.GeyserLocale;
import org.geysermc.geyser.util.FileUtils;
import org.geysermc.geyser.util.JsonUtils;
import org.geysermc.mcprotocollib.auth.texture.Texture;
import org.geysermc.mcprotocollib.auth.texture.TextureType;
import org.geysermc.mcprotocollib.auth.util.TextureUrlChecker;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class CustomSkullRegistryPopulator {

    private static final Pattern SKULL_HASH_PATTERN = Pattern.compile("^[a-fA-F0-9]+$");

    public static void populate() {
        SkullResourcePackManager.SKULL_SKINS.clear(); // Remove skins after reloading
        BlockRegistries.CUSTOM_SKULLS.set(Object2ObjectMaps.emptyMap());

        if (!GeyserImpl.getInstance().config().gameplay().enableCustomContent()) {
            return;
        }

        // Try to migrate the legacy custom-skulls.yml file
        try {
            GeyserBootstrap bootstrap = GeyserImpl.getInstance().getBootstrap();
            File skullConfigFile = bootstrap.getConfigFolder().resolve("custom-skulls.yml").toFile();
            if (skullConfigFile.exists()) {
                GeyserCustomSkullConfiguration skullConfig = FileUtils.loadConfig(skullConfigFile, GeyserCustomSkullConfiguration.class);
                tryMigrateLegacyConfigToMappingsFile(skullConfig, skullConfigFile);
            }
        } catch (IOException exception) {
            GeyserImpl.getInstance().getLogger().severe(GeyserLocale.getLocaleStringLog("geyser.config.failed"), exception);
        }

        BlockRegistries.CUSTOM_SKULLS.set(new Object2ObjectOpenHashMap<>());

        SkullRegistrations registrations = new SkullRegistrations();

        MappingsConfigReader.getCustomMappingsDirectoryAndEnsureItExists().ifPresent(mappingsDirectory -> {
            for (Path mappingsFile : MappingsConfigReader.getCustomMappingsFiles(mappingsDirectory)) {
                String source = "mappings file " + mappingsDirectory.getParent().relativize(mappingsFile);
                MappingsConfigReader.readCustomMappings(MappingsType.SKULLS, mappingsFile,
                    (type, textures) -> textures.forEach(texture -> registrations.register(texture, type, source)));
            }
        });

        GeyserImpl.getInstance().getEventBus().fire(registrations);

        Map<String, String> skinHashes = registrations.skinHashes;

        registrations.usernames.forEach((username, source) -> {
            String profile = getProfileFromUsername(username);
            if (profile != null) {
                String skinHash = getSkinHash(profile, source);
                if (skinHash != null) {
                    skinHashes.putIfAbsent(skinHash, source);
                }
            }
        });

        registrations.uuids.forEach((uuid, source) -> {
            String profile = getProfileFromUuid(uuid);
            if (profile != null) {
                String skinHash = getSkinHash(profile, source);
                if (skinHash != null) {
                    skinHashes.putIfAbsent(skinHash, source);
                }
            }
        });

        registrations.profiles.forEach((profile, source) -> {
            String skinHash = getSkinHash(profile, source);
            if (skinHash != null) {
                skinHashes.putIfAbsent(skinHash, source);
            }
        });

        skinHashes.forEach((skinHash, source) -> {
            if (!SKULL_HASH_PATTERN.matcher(skinHash).matches()) {
                GeyserImpl.getInstance().getLogger().error("Skin hash " + skinHash + " (registered by " + source + ") does not match required format ^[a-fA-F0-9]+$ and will not be added as a custom block.");
                return;
            }

            try {
                SkullResourcePackManager.cacheSkullSkin(skinHash);
                BlockRegistries.CUSTOM_SKULLS.register(skinHash, new CustomSkull(skinHash));
            } catch (IOException e) {
                GeyserImpl.getInstance().getLogger().error("Failed to cache skin for skull texture " + skinHash + " This skull will not be added as a custom block.", e);
            }
        });

        if (!BlockRegistries.CUSTOM_SKULLS.get().isEmpty()) {
            GeyserImpl.getInstance().getLogger().info("Registered " + BlockRegistries.CUSTOM_SKULLS.get().size() + " custom skulls as custom blocks.");
        }
    }

    /**
     * Gets the skin hash from a base64 encoded profile
     * @param profile the base64 encoded profile
     * @param source who registered the skull, used for logging
     * @return the skin hash or null if the profile is invalid
     */
    private static @Nullable String getSkinHash(String profile, String source) {
        String hash = loadHashFromJson(profile);
        if (hash == null) {
            GeyserImpl.getInstance().getLogger().warning("Skull texture " + profile + " (registered by " + source + ") contained no valid skins and will not be added as a custom block.");
            return null;
        }
        return hash;
    }

    /**
     * Gets the base64 encoded profile from a player's username
     * @param username the player username
     * @return the base64 encoded profile or null if the request failed
     */
    private static @Nullable String getProfileFromUsername(String username) {
        try {
            return SkinProvider.requestTexturesFromUsername(username).get();
        } catch (InterruptedException | ExecutionException e) {
            GeyserImpl.getInstance().getLogger().error("Unable to request skull textures for " + username + " This skull will not be added as a custom block.", e);
            return null;
        }
    }

    /**
     * Gets the base64 encoded profile from a player's UUID
     * @param uuid the player UUID
     * @return the base64 encoded profile or null if the request failed
     */
    private static @Nullable String getProfileFromUuid(String uuid) {
        try {
            UUID parsed = UUID.fromString(uuid);
            return SkinProvider.requestTexturesFromUUID(parsed).get();
        } catch (InterruptedException | ExecutionException e) {
            GeyserImpl.getInstance().getLogger().error("Unable to request skull textures for " + uuid + " This skull will not be added as a custom block.", e);
            return null;
        } catch (IllegalArgumentException e) {
            GeyserImpl.getInstance().getLogger().error("Invalid skull uuid " + uuid + " This skull will not be added as a custom block.");
            return null;
        }
    }

    /**
     * Gets the skin hash from a profile
     */
    public static @Nullable String loadHashFromJson(String encodedJson) {
        JsonObject skinObject;
        try {
            skinObject = JsonUtils.parseJson(new String(Base64.getDecoder().decode(encodedJson), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            GeyserImpl.getInstance().getLogger().warning("Invalid base64 encoded profile!");
            return null;
        }

        MinimalTexturesPayload result;
        try {
            result = GeyserImpl.GSON.fromJson(skinObject, MinimalTexturesPayload.class);
        } catch (Exception e) {
            GeyserImpl.getInstance().getLogger().error("Could not decode texture payload!", e);
            return null;
        }

        if (result != null && result.textures != null) {
            for (Texture texture : result.textures.values()) {
                if (TextureUrlChecker.isAllowedTextureDomain(texture.getURL())) {
                    continue;
                }

                GeyserImpl.getInstance().getLogger().warning("Textures payload has been tampered with! (non-whitelisted domain)!");
                return null;
            }

            Texture skin = result.textures.get(TextureType.SKIN);
            if (skin == null) {
                GeyserImpl.getInstance().getLogger().warning("Textures payload contains no skin!");
                return null;
            }

            return skin.getHash();
        }
        return null;
    }

    private static void tryMigrateLegacyConfigToMappingsFile(GeyserCustomSkullConfiguration configuration, File legacyPath) {
        if (configuration.isEmpty()) {
            GeyserImpl.getInstance().getLogger().info("Not migrating legacy custom-skulls.yml file because it was empty");
            if (!legacyPath.delete()) {
                GeyserImpl.getInstance().getLogger().error("Failed to delete legacy custom-skulls.yml file!");
            }
            return;
        }

        JsonObject mappingsFile = new JsonObject();
        mappingsFile.addProperty("format_version", 1);
        JsonObject skulls = new JsonObject();
        skulls.add("username", stringsToArray(configuration.getPlayerUsernames()));
        skulls.add("uuid", stringsToArray(configuration.getPlayerUUIDs()));
        skulls.add("profile", stringsToArray(configuration.getPlayerProfiles()));
        skulls.add("skin_hash", stringsToArray(configuration.getPlayerSkinHashes()));
        mappingsFile.add("skulls", skulls);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        MappingsConfigReader.getCustomMappingsDirectoryAndEnsureItExists().ifPresentOrElse(mappingsDirectory -> {
            Path destination = mappingsDirectory.resolve("migrated-custom-skulls.json");
            if (Files.exists(destination)) {
                GeyserImpl.getInstance().getLogger().warning("Not migrating legacy custom-skulls.yml file because the destination (\"custom_mappings/migrated-custom-skulls.json\") already exists!");
                return;
            } else {
                GeyserImpl.getInstance().getLogger().info("Migrating legacy custom-skulls.yml file to \"custom_mappings/migrated-custom-skulls.json\"...");
            }
            try {
                Files.writeString(destination, gson.toJson(mappingsFile));
            } catch (IOException exception) {
                GeyserImpl.getInstance().getLogger().error("Failed to migrate legacy custom-skulls.yml file!", exception);
                return;
            }
            if (!legacyPath.delete()) {
                GeyserImpl.getInstance().getLogger().error("Failed to delete legacy custom-skulls.yml file!");
            }
        }, () -> GeyserImpl.getInstance().getLogger().error("Not migrating legacy custom-skulls.yml file because the mappings directory does not exist"));
    }

    private static JsonArray stringsToArray(List<String> strings) {
        JsonArray array = new JsonArray();
        strings.forEach(array::add);
        return array;
    }

    /*
     * see mcpl GameProfile's MinecraftTexturesPayload for the full impl
     * We only need the textures, and e.g. profileId throws due to missing uuid conversion
     */
    private static class MinimalTexturesPayload {
        public Map<TextureType, Texture> textures;
    }

    /**
     * Collects registered skull textures, keeping track of who registered each one so that
     * warnings about duplicate or invalid textures can point to their source.
     */
    private static class SkullRegistrations extends GeyserDefineCustomSkullsEvent {
        private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

        // Texture -> source
        private final Map<String, String> usernames = new LinkedHashMap<>();
        private final Map<String, String> uuids = new LinkedHashMap<>();
        private final Map<String, String> profiles = new LinkedHashMap<>();
        private final Map<String, String> skinHashes = new LinkedHashMap<>();

        @Override
        public void register(@NonNull String texture, @NonNull SkullTextureType type) {
            register(texture, type, findCaller());
        }

        void register(@NonNull String texture, @NonNull SkullTextureType type, @NonNull String source) {
            Map<String, String> textures = switch (type) {
                case USERNAME -> usernames;
                case UUID -> uuids;
                case PROFILE -> profiles;
                case SKIN_HASH -> skinHashes;
            };
            String existing = textures.putIfAbsent(texture, source);
            if (existing != null) {
                GeyserImpl.getInstance().getLogger().warning("Not adding texture " + texture + " for skull texture type " + type
                    + " twice! It was registered by " + source + ", but was already registered by " + existing + ".");
            }
        }

        /**
         * Finds who called {@link #register(String, SkullTextureType)} through the event, preferring
         * the extension it was called from, and falling back to the calling class otherwise.
         */
        private static String findCaller() {
            return STACK_WALKER.walk(frames -> {
                String fallback = null;
                for (StackWalker.StackFrame frame : (Iterable<StackWalker.StackFrame>) frames::iterator) {
                    Class<?> clazz = frame.getDeclaringClass();
                    if (clazz.getClassLoader() instanceof GeyserExtensionClassLoader classLoader) {
                        return "extension " + classLoader.description().id();
                    }
                    if (fallback == null && clazz != SkullRegistrations.class && !isInternalClass(clazz.getName())) {
                        fallback = "class " + clazz.getName();
                    }
                }
                return fallback == null ? "unknown source" : fallback;
            });
        }

        private static boolean isInternalClass(String name) {
            return name.startsWith("org.geysermc.event.") || name.startsWith("java.") || name.startsWith("jdk.") || name.startsWith("sun.");
        }
    }
}
