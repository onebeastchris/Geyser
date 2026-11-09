/*
 * Copyright (c) 2026 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.network.bedrock.nethernet;

import org.checkerframework.checker.nullness.qual.Nullable;
import tel.schich.libdatachannel.LibDataChannel;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

/**
 * Which libdatachannel native this server loads, identified by the SHA-256 of the library file, so a dump shows
 * beyond doubt whether a test ran with the usrsctp T3-rtx fix or without it. Hashing reads the same file
 * libdatachannel-java loads: the {@code libdatachannel.native.datachannel-java.path} or {@code .classpath} property
 * when set, otherwise the arch-detect bundle's entry for this platform.
 *
 * @param platform libdatachannel-java's name for this platform, such as linux-glibc-x86_64
 * @param source where the library is read from
 * @param sha256 the library's SHA-256, or null if it could not be read
 * @param build what that hash is known as, or why it is unknown
 */
public record NetherNetNativeInfo(String platform, String source, @Nullable String sha256, String build) {
    static final String STOCK = "stock opencollab 0.24.6.0-20261029.091642-10, without the T3-rtx fix";
    static final String FIXED = "0.24.6.1-fork-2bcaf08689, with the usrsctp T3-rtx fix";

    /** SHA-256 of every native in the builds Geyser has shipped for #6854 testing */
    static final Map<String, String> KNOWN = Map.ofEntries(
        Map.entry("c4ffdd4f9a01b69e064bbd4fd5ede597cac75a38720e383f7dd2376361072345", STOCK + " (linux-glibc-aarch64)"),
        Map.entry("c6998048e2347a36dc93e188b435cc5d08be1aec648758f1b99be1fe0042a2cd", STOCK + " (linux-glibc-x86_64)"),
        Map.entry("551c98922f925ee60ded4a4c2402d04dd85585dbcebc98fc3a48c5df09007bf3", STOCK + " (linux-musl-aarch64)"),
        Map.entry("5482aa79703b2c94e6f44dd80c6f86c55a01b403888b06e944afe3e43ecc3846", STOCK + " (linux-musl-x86_64)"),
        Map.entry("b6ea094a3e10554bf9938b6a1da85e44d0a1c3908cd00485f6733cf2a94a8381", STOCK + " (macos-arm64)"),
        Map.entry("0c7fc952d3af04c9396b81a36827d68e7509ac98cc9d04171eb8ad8ca204e98c", STOCK + " (macos-x86_64)"),
        Map.entry("300b5a71fe4fdabb5aa647e72df1ca188cb33d71c39ebca8c81c6115421293ce", STOCK + " (windows-aarch64)"),
        Map.entry("1fb73d13cf50f9540b85dce4b223b29b4a21dedaa978012cac5e39f1eb832a48", STOCK + " (windows-x86_64)"),
        Map.entry("945928112d54e6cfc5b2bc61ed79530e5b7e42983cb1f5883770fcd8bc82eb4a", FIXED + " (linux-glibc-aarch64)"),
        Map.entry("c2b35318369590033128053133d2ed86d1700662ae88212be2d2d6d1eaf395dc", FIXED + " (linux-glibc-x86_64)"),
        Map.entry("e8ec3a805b788437531e34d0d5ec2a6f8f6463a6d1dedbf1f21923dd8aaa3001", FIXED + " (linux-musl-aarch64)"),
        Map.entry("519cb89005b22e5df567249d5d7c50104b4bfc566a37db6e7224eac7664f9dfa", FIXED + " (linux-musl-x86_64)"),
        Map.entry("ba9ad2ea0398801a0f90b7a70d2e5edb529fa9df48e2216324c5816a4fc858c5", FIXED + " (macos-arm64)"),
        Map.entry("81adb056b5c2dd9cb848e2c10f24f9d21830a871642b52952018dfd09c1a6be7", FIXED + " (macos-x86_64)"),
        Map.entry("8660ee83f8a9720ad5f0db6a7f43f9169786eca0514b5284f9b0424cfaf2965e", FIXED + " (windows-aarch64)"),
        Map.entry("9fdca6af3155e54347e96700ef1f80634c80238e63c9fc4c60618f2baad7323b", FIXED + " (windows-x86_64)")
    );

    private static volatile @Nullable NetherNetNativeInfo current;

    /**
     * @return this server's native, hashed once
     */
    public static NetherNetNativeInfo current() {
        NetherNetNativeInfo info = current;
        if (info == null) {
            info = read();
            current = info;
        }
        return info;
    }

    private static NetherNetNativeInfo read() {
        // Linux lists the library the JVM actually mapped, wherever it came from
        Path mapped = mappedLibrary();
        if (mapped != null) {
            try (InputStream in = Files.newInputStream(mapped)) {
                return hashed(detectPlatform(), "mapped " + mapped, in);
            } catch (IOException | NoSuchAlgorithmException e) {
                return new NetherNetNativeInfo(detectPlatform(), "mapped " + mapped, null, "could not be read: " + e);
            }
        }

        String platform = detectPlatform();
        String prefix = "libdatachannel.native." + LibDataChannel.LIB_NAME.toLowerCase();
        String fsPath = System.getProperty(prefix + ".path");
        String classPath = System.getProperty(prefix + ".classpath");
        String source = fsPath != null ? fsPath : classPath != null ? classPath : "/" + platform + "/native/" + libraryFile();
        try (InputStream in = fsPath != null ? Files.newInputStream(Path.of(fsPath)) : LibDataChannel.class.getResourceAsStream(source)) {
            if (in == null) {
                return new NetherNetNativeInfo(platform, source, null, "not bundled for this platform");
            }
            return hashed(platform, source, in);
        } catch (IOException | NoSuchAlgorithmException e) {
            return new NetherNetNativeInfo(platform, source, null, "could not be read: " + e);
        }
    }

    private static NetherNetNativeInfo hashed(String platform, String source, InputStream in) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[64 << 10];
        int read;
        while ((read = in.read(buffer)) > 0) {
            digest.update(buffer, 0, read);
        }
        String sha256 = HexFormat.of().formatHex(digest.digest());
        return new NetherNetNativeInfo(platform, source, sha256, identify(sha256));
    }

    /**
     * @return the libdatachannel-java library this process has mapped, or null if not on Linux or not loaded yet
     */
    static @Nullable Path mappedLibrary() {
        Path maps = Path.of("/proc/self/maps");
        if (!Files.isReadable(maps)) {
            return null;
        }
        try {
            for (String line : Files.readAllLines(maps, StandardCharsets.ISO_8859_1)) {
                int start = line.indexOf('/');
                if (start >= 0 && line.endsWith(libraryFile())) {
                    Path path = Path.of(line.substring(start));
                    if (Files.isReadable(path)) {
                        return path;
                    }
                }
            }
        } catch (IOException | RuntimeException ignored) {
        }
        return null;
    }

    /** libdatachannel-java's platform name, from its package-private Platform class */
    private static String detectPlatform() {
        try {
            Method detect = Class.forName("tel.schich.libdatachannel.Platform").getDeclaredMethod("detectArch");
            detect.setAccessible(true);
            return (String) detect.invoke(null);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            return "unknown (" + e + ")";
        }
    }

    private static String libraryFile() {
        String os = System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT);
        String name = "lib" + LibDataChannel.LIB_NAME;
        return os.contains("win") ? name + ".dll" : os.contains("mac") ? name + ".dylib" : name + ".so";
    }

    static String identify(String sha256) {
        return KNOWN.getOrDefault(sha256, "unknown build, neither stock nor the T3-rtx fix build");
    }
}
