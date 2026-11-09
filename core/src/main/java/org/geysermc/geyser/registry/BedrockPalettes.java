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

package org.geysermc.geyser.registry;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.geyser.GeyserLogger;
import org.geysermc.paletter.Palette;
import org.geysermc.paletter.PaletteReport;
import org.geysermc.paletter.Paletter;
import org.geysermc.paletter.data.DataVersion;

import java.util.List;

/**
 * Geyser's Bedrock palettes. Everything Bedrock (vanilla data per version, runtime ids, custom item and block
 * definitions, the creative inventory) is Paletter's job. Geyser contributes content layers and a fallback policy to
 * the one {@link Paletter} it {@link #initialize hands over}:
 * <ol>
 *     <li>{@code geyser:blocks}: custom blocks (API and custom mappings);</li>
 *     <li>{@code geyser:items}: custom items, block items of custom blocks, vanilla item overrides and creative edits;</li>
 *     <li>the fallbacks (what older versions show for the newest content they lack), which are Paletter's policy
 *     and not a layer.</li>
 * </ol>
 * All layers are built first; each Bedrock data version then compiles exactly one palette (no intermediate palettes
 * with some of the layers), shared by every session on it.
 */
public final class BedrockPalettes {
    private static volatile Paletter paletter;

    private BedrockPalettes() {
    }

    /**
     * Sets the Paletter; only possible once, before any palette is requested.
     */
    public static synchronized void initialize(@NonNull Paletter paletter) {
        if (BedrockPalettes.paletter != null) {
            throw new IllegalStateException("The Bedrock palettes are already initialized");
        }
        BedrockPalettes.paletter = paletter;
    }

    public static @NonNull Paletter paletter() {
        Paletter paletter = BedrockPalettes.paletter;
        if (paletter == null) {
            throw new IllegalStateException("The Bedrock palettes are not initialized yet");
        }
        return paletter;
    }

    /**
     * @return the Bedrock data versions Geyser has palettes for, oldest first
     */
    public static @NonNull List<DataVersion> versions() {
        return paletter().versions();
    }

    /**
     * @return the palette of a protocol version, with all layers
     */
    public static @NonNull Palette palette(int protocolVersion) {
        return paletter().palette(protocolVersion);
    }

    /**
     * Logs what Paletter adapted, replaced or dropped while compiling the palette of a version, once per version:
     * errors as errors, warnings (likely a mistake in a layer, e.g. a dropped component or a creative placement that
     * matched nothing) as warnings, the expected adaptations for older versions at debug level.
     */
    public static void logReport(@NonNull DataVersion version, @NonNull Palette palette, @NonNull GeyserLogger logger) {
        for (PaletteReport.Entry entry : palette.report().entries()) {
            if (entry.level() == PaletteReport.Level.ERROR) {
                logger.error("Bedrock " + version.name() + " palette: " + entry.source() + ": " + entry.message());
            } else if (entry.level() == PaletteReport.Level.WARNING) {
                logger.warning("Bedrock " + version.name() + " palette: " + entry.source() + ": " + entry.message());
            } else {
                logger.debug("Bedrock " + version.name() + " palette: " + entry.source() + ": " + entry.message());
            }
        }
    }
}
