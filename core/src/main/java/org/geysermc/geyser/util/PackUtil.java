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

package org.geysermc.geyser.util;

import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.pack.ResourcePack;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PackUtil {
    static Path packsPath = GeyserImpl.getInstance().getBootstrap().getConfigFolder().resolve("userPacks");

    public static HashMap<UUID, Map<String, ResourcePack>> individualPackMap = new HashMap<>();

    public static void addPack(UUID uuid, String pack) {
        if (individualPackMap.containsKey(uuid)) {
            individualPackMap.get(uuid).put(pack, ResourcePack.PACKS.get(pack));
        } else {
            HashMap<String, ResourcePack> packMap = new HashMap<>();
            packMap.put(pack, ResourcePack.PACKS.get(pack));
            individualPackMap.put(uuid, packMap);
        }
    }

    public static void removePacks(UUID uuid) {
        individualPackMap.remove(uuid);
    }

    public static void loadFromDisk() {
        if (!Files.exists(packsPath)) {
            try {
                Files.createDirectory(packsPath);
            } catch (IOException e) {
                GeyserImpl.getInstance().getLogger().error("Could not create packs directory", e);
            }

            // As we just created the directory it will be empty
            return;
        }

        try {
            Files.walk(packsPath).forEach(path -> {
                if (Files.isRegularFile(path)) {
                    try {
                        List<String> lines = Files.readAllLines(path);
                        for (String line : lines) {
                            if (ResourcePack.PACKS.containsKey(line)) {
                                GeyserImpl.getInstance().getLogger().info("Loaded pack " + line + " from disk");
                                addPack(UUID.fromString(path.getFileName().toString().replace(".txt", "")), line);
                            } else {
                                GeyserImpl.getInstance().getLogger().error("Could not find pack " + line + " from disk");

                            }

                        }

                    } catch (IOException e) {
                        GeyserImpl.getInstance().getLogger().error("Could not read pack file: " + path.getFileName(), e);
                    }
                }
            });
        } catch (IOException e) {
            GeyserImpl.getInstance().getLogger().error("Could not load packs from disk", e);
        }
    }

    private static void saveToDisk(UUID uuid) {
        if (!Files.exists(packsPath)) {
            try {
                Files.createDirectory(packsPath);
            } catch (IOException e) {
                GeyserImpl.getInstance().getLogger().error("Could not create packs directory", e);
            }
        }

        Path path = packsPath.resolve(uuid + ".txt");
        try {
            if (!Files.exists(path)) {
                Files.createFile(path);
            }

            Writer writer = new BufferedWriter(new FileWriter(path.toFile()));
            //writer.write(individualPackMap.get(uuid).getManifest().getHeader().getUuid().toString());
            writer.close();


        } catch (IOException e) {
            GeyserImpl.getInstance().getLogger().error("Could not save pack to disk", e);
        }
    }
}