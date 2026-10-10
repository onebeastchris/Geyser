/*
 * Copyright (c) 2019-2026 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.gametest.tests;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.HolderGetter;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.gametest.GeyserGameTestPlatform;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Base for tests validating a data file of Geyser that is generated from the vanilla registries: the expected content
 * is computed from the live registries and compared with the file Geyser loaded. On a mismatch, the complete expected
 * content is logged so it can be pasted into the file.
 */
public abstract class GeneratedDataTestInstance extends GeyserTestInstance {

    protected GeneratedDataTestInstance(HolderGetter<TestEnvironmentDefinition<?>> testEnvironments, boolean required) {
        super(testEnvironments, required);
    }

    /**
     * @return whether the resource matches; if not, the expected content was logged
     */
    protected static boolean compareWithResource(String resource, JsonObject expected) {
        JsonElement actual = readResource(resource);
        if (expected.equals(actual)) {
            return true;
        }
        String json = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(expected);
        GeyserImpl.getInstance().getLogger().error(resource + " is outdated. Replace its content with the following:\n"
            + "----- BEGIN " + resource + " -----\n" + json + "\n----- END " + resource + " -----");
        return false;
    }

    private static @Nullable JsonElement readResource(String resource) {
        try (InputStream stream = new GeyserGameTestPlatform().resolveResource(resource)) {
            if (stream == null) {
                return null;
            }
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Unable to read " + resource, e);
        }
    }

    protected static void failOrSucceed(GameTestHelper helper, @Nullable String failure) {
        if (failure == null) {
            helper.succeed();
        } else {
            helper.fail(failure);
        }
    }
}
