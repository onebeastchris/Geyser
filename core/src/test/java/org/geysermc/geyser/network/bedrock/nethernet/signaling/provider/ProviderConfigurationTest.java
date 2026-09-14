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

package org.geysermc.geyser.network.bedrock.nethernet.signaling.provider;

import com.google.gson.JsonNull;
import org.cloudburstmc.netty.signaling.provider.ProviderRuntimeConfiguration;
import org.geysermc.geyser.configuration.GeyserConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.configurate.interfaces.InterfaceDefaultOptions;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * What Geyser's configuration binds to and hands the resolver. How the resolver treats those values
 * is covered by the transport library, which owns it.
 */
class ProviderConfigurationTest {

    private static GeyserConfig.SignalingConfig config(String yaml) throws IOException {
        return YamlConfigurationLoader.builder()
            .source(() -> new BufferedReader(new StringReader(yaml)))
            .defaultOptions(InterfaceDefaultOptions::addTo)
            .build().load().get(GeyserConfig.SignalingConfig.class);
    }

    private static ProviderRuntimeConfiguration resolve(GeyserConfig.SignalingConfig config, Path dir)
        throws IOException {
        var nxs = config.nxs();
        return ProviderRuntimeConfiguration.resolve(
            new ProviderRuntimeConfiguration.Settings(nxs.endpoint(), nxs.token(), nxs.advertiseAddresses(), nxs.data()),
            dir, "::", 20000, 40, "Geyser");
    }

    @Test
    void signalingIsBuiltinAndPointsAtWardenUntilConfigured(@TempDir Path dir) throws Exception {
        var config = config("{}");

        assertEquals(GeyserConfig.SignalingConfig.Mode.BUILTIN, config.mode());
        assertEquals("https://agent.warden.cloud", resolve(config, dir).origin().toString());
    }

    @Test
    void bindsEveryConfiguredNxsSetting(@TempDir Path dir) throws Exception {
        var result = resolve(config("""
            nxs:
              endpoint: https://signal.example.net
              token: yaml-secret
              data: {region: EU, pool: proxy, location: london}
              advertise-addresses: ['1.1.1.1:29133']
            """), dir);

        assertEquals("https://signal.example.net", result.origin().toString());
        assertEquals("yaml-secret", result.authorizationToken());
        assertEquals("EU", result.region());
        assertEquals("proxy", result.pool());
        assertEquals(Map.of("location", "london"), result.tags());
        assertEquals(1, result.advertisedEndpoints().size());
        assertEquals("Geyser", result.label());
    }

    @Test
    void emitsOptionalHostLocation() throws Exception {
        var config = config("""
            nxs:
              location: {country: nl, city: Amsterdam, latitude: 52.37, longitude: 4.89}
            """);
        var extension = WardenLocationAdapter.extensions(config.nxs().location())
            .getAsJsonObject("cloud.warden.location");

        assertFalse(extension.get("critical").getAsBoolean());
        assertEquals(1, extension.get("version").getAsInt());
        var location = extension.getAsJsonObject("data").getAsJsonObject("location");
        assertEquals("NL", location.get("country").getAsString());
        assertEquals("Amsterdam", location.get("city").getAsString());
        assertEquals(52.37, location.get("latitude").getAsDouble());
        assertEquals(4.89, location.get("longitude").getAsDouble());
    }

    @Test
    void emptyHostLocationClearsAnEarlierOverride() throws Exception {
        var extension = WardenLocationAdapter.extensions(config("{}").nxs().location())
            .getAsJsonObject("cloud.warden.location");

        assertEquals(JsonNull.INSTANCE, extension.getAsJsonObject("data").get("location"));
    }

    @Test
    void refusesInvalidHostLocation() {
        for (String location : List.of("{country: ZZ}", "{city: London}", "{latitude: 0}",
            "{latitude: 91, longitude: 0}", "{latitude: 0, longitude: 181}",
            "{latitude: NaN, longitude: 0}", "{country: NL, extra: value}")) {
            assertThrows(IOException.class, () -> WardenLocationAdapter.extensions(
                config("nxs:\n  location: " + location + "\n").nxs().location()));
        }
        for (String city : List.of("Lon\ndon", String.valueOf((char) 0xD800))) {
            assertThrows(IOException.class, () -> WardenLocationAdapter.extensions(Map.of("country", "GB", "city", city)));
        }
    }

    @Test
    void refusesAModeItCannotServe() {
        assertThrows(IOException.class, () -> config("mode: invalid\n"));
    }

    @Test
    void carriesTheListenerItWasGiven(@TempDir Path dir) throws Exception {
        var result = resolve(config("nxs:\n  endpoint: https://signal.example.net\n"), dir);

        assertEquals("::", result.bindAddress());
        assertEquals(20000, result.udpPort());
        assertEquals(List.of(), result.advertisedEndpoints());
    }
}
