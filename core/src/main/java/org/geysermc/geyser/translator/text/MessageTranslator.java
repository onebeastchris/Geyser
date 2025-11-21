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

package org.geysermc.geyser.translator.text;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.renderer.TranslatableComponentRenderer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.CharacterAndFormat;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.protocol.bedrock.packet.TextPacket;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.session.cache.registry.JavaRegistries;
import org.geysermc.geyser.text.ChatColor;
import org.geysermc.geyser.text.ChatDecoration;
import org.geysermc.geyser.text.DummyLegacyHoverEventSerializer;
import org.geysermc.geyser.text.GeyserLocale;
import org.geysermc.geyser.text.MinecraftTranslationRegistry;
import org.geysermc.mcprotocollib.protocol.data.DefaultComponentSerializer;
import org.geysermc.mcprotocollib.protocol.data.game.Holder;
import org.geysermc.mcprotocollib.protocol.data.game.chat.ChatType;
import org.geysermc.mcprotocollib.protocol.data.game.chat.ChatTypeDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.geysermc.geyser.translator.text.HexColorMap.HEX_COLOR_EMOJI_MAP;

public class MessageTranslator {
    // These are used for handling the translations of the messages
    // Custom instead of TranslatableComponentRenderer#usingTranslationSource so we don't need to worry about finding a Locale class
    private static final TranslatableComponentRenderer<String> RENDERER = new MinecraftTranslationRegistry();

    // Possible TODO: replace the legacy hover event serializer with an empty one since we have no use for hover events
    private static final GsonComponentSerializer GSON_SERIALIZER;

    private static final LegacyComponentSerializer BEDROCK_SERIALIZER;
    private static final String BEDROCK_COLORS;

    // Legacy formatting character
    private static final String BASE = "\u00a7";

    // Reset character
    private static final String RESET = BASE + "r";
    private static final Pattern RESET_PATTERN = Pattern.compile("(" + RESET + "){2,}");
    private static final Pattern LOCALIZATION_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?s");

    private static final Cache<String, String> COLOR_MATCH_CACHE = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    static {
        GSON_SERIALIZER = DefaultComponentSerializer.get()
                .toBuilder()
                // Use a custom legacy hover event deserializer since we don't use any of this data anyway, and
                // fixes issues where legacy hover events throw deserialization errors
                .legacyHoverEventSerializer(new DummyLegacyHoverEventSerializer())
                .build();
        // Tell MCProtocolLib to use this serializer, too.
        DefaultComponentSerializer.set(GSON_SERIALIZER);

        // Customize the formatting characters of our legacy serializer for bedrock edition
        List<CharacterAndFormat> formats = new ArrayList<>(CharacterAndFormat.defaults());
        // The following two do not yet exist on Bedrock - https://bugs.mojang.com/browse/MCPE-41729
        formats.remove(CharacterAndFormat.STRIKETHROUGH);
        formats.remove(CharacterAndFormat.UNDERLINED);

        formats.add(CharacterAndFormat.characterAndFormat('g', TextColor.color(221, 214, 5))); // Minecoin Gold
        // Add the new characters implemented in 1.19.80
        formats.add(CharacterAndFormat.characterAndFormat('h', TextColor.color(227, 212, 209))); // Quartz
        formats.add(CharacterAndFormat.characterAndFormat('i', TextColor.color(206, 202, 202))); // Iron
        formats.add(CharacterAndFormat.characterAndFormat('j', TextColor.color(68, 58, 59))); // Netherite
        formats.add(CharacterAndFormat.characterAndFormat('m', TextColor.color(151, 22, 7))); // Redstone
        formats.add(CharacterAndFormat.characterAndFormat('n', TextColor.color(180, 104, 77))); // Copper
        formats.add(CharacterAndFormat.characterAndFormat('p', TextColor.color(222, 177, 45))); // Gold
        formats.add(CharacterAndFormat.characterAndFormat('q', TextColor.color(17, 160, 54))); // Emerald
        formats.add(CharacterAndFormat.characterAndFormat('s', TextColor.color(44, 186, 168))); // Diamond
        formats.add(CharacterAndFormat.characterAndFormat('t', TextColor.color(33, 73, 123))); // Lapis
        formats.add(CharacterAndFormat.characterAndFormat('u', TextColor.color(154, 92, 198))); // Amethyst

        ComponentFlattener flattener = ComponentFlattener.basic().toBuilder()
            .nestingLimit(30)
            .complexMapper(TranslatableComponent.class, (translatable, consumer) -> {
                final String translated = translatable.key();
                final Matcher matcher = LOCALIZATION_PATTERN.matcher(translated);
                final List<TranslationArgument> args = translatable.arguments();
                int argPosition = 0;
                int lastIdx = 0;
                while (matcher.find()) {
                    // append prior
                    if (lastIdx < matcher.start()) {
                        consumer.accept(Component.text(translated.substring(lastIdx, matcher.start())));
                    }
                    lastIdx = matcher.end();

                    final @Nullable String argIdx = matcher.group(1);
                    // calculate argument position
                    if (argIdx != null) {
                        try {
                            final int idx = Integer.parseInt(argIdx) - 1;
                            if (idx < args.size()) {
                                consumer.accept(args.get(idx).asComponent());
                            }
                        } catch (final NumberFormatException ex) {
                            // ignore, drop the format placeholder
                        }
                    } else {
                        final int idx = argPosition++;
                        if (idx < args.size()) {
                            consumer.accept(args.get(idx).asComponent());
                        }
                    }
                }

                // append tail
                if (lastIdx < translated.length()) {
                    consumer.accept(Component.text(translated.substring(lastIdx)));
                }
            })
            .build();

        BEDROCK_SERIALIZER = LegacyComponentSerializer.legacySection().toBuilder()
                .formats(formats)
                .flattener(flattener)
                .build();

        // cache all the legacy character codes
        StringBuilder colorBuilder = new StringBuilder();
        for (CharacterAndFormat format : formats) {
            if (format.format() instanceof TextColor) {
                colorBuilder.append(format.character());
            }
        }
        BEDROCK_COLORS = colorBuilder.toString();
    }


    private static String findNearestColor(String targetColor, java.util.Set<String> availableColors) {
        if (targetColor == null || availableColors == null || availableColors.isEmpty()) {
            return null;
        }

        String cacheKey = targetColor + ":" + availableColors.hashCode();

        String cachedResult = COLOR_MATCH_CACHE.getIfPresent(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        int targetRgb = parseHexColor(targetColor);
        if (targetRgb == -1) {
            return null;
        }

        double[] targetLab = rgbToLab(targetRgb);

        String nearestColor = null;
        double nearestDistance = Double.MAX_VALUE;

        for (String availableColor : availableColors) {
            int availableRgb = parseHexColor(availableColor);
            if (availableRgb == -1) {
                continue;
            }

            double[] availableLab = rgbToLab(availableRgb);
            double distance = ciede2000(targetLab, availableLab);

            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestColor = availableColor;
            }
        }

        if (nearestColor != null) {
            COLOR_MATCH_CACHE.put(cacheKey, nearestColor);
        }

        return nearestColor;
    }

    private static double[] rgbToLab(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        double[] xyz = rgbToXyz(r, g, b);

        return xyzToLab(xyz[0], xyz[1], xyz[2]);
    }

    private static double[] rgbToXyz(int r, int g, int b) {
        double rLinear = gammaCorrection(r / 255.0);
        double gLinear = gammaCorrection(g / 255.0);
        double bLinear = gammaCorrection(b / 255.0);

        double x = rLinear * 0.4124564 + gLinear * 0.3575761 + bLinear * 0.1804375;
        double y = rLinear * 0.2126729 + gLinear * 0.7151522 + bLinear * 0.0721750;
        double z = rLinear * 0.0193339 + gLinear * 0.1191920 + bLinear * 0.9503041;

        return new double[]{x * 100, y * 100, z * 100};
    }

    private static double gammaCorrection(double value) {
        return value > 0.04045 ? Math.pow((value + 0.055) / 1.055, 2.4) : value / 12.92;
    }

    private static double[] xyzToLab(double x, double y, double z) {
        double xn = 95.047;
        double yn = 100.000;
        double zn = 108.883;

        double fx = labFunction(x / xn);
        double fy = labFunction(y / yn);
        double fz = labFunction(z / zn);

        double l = 116 * fy - 16;
        double a = 500 * (fx - fy);
        double b = 200 * (fy - fz);

        return new double[]{l, a, b};
    }

    private static double labFunction(double t) {
        double delta = 6.0 / 29.0;
        return t > delta * delta * delta ? Math.cbrt(t) : (t / (3 * delta * delta) + 4.0 / 29.0);
    }

    private static double ciede2000(double[] lab1, double[] lab2) {
        double l1 = lab1[0], a1 = lab1[1], b1 = lab1[2];
        double l2 = lab2[0], a2 = lab2[1], b2 = lab2[2];

        double c1 = Math.sqrt(a1 * a1 + b1 * b1);
        double c2 = Math.sqrt(a2 * a2 + b2 * b2);
        double cBar = (c1 + c2) / 2.0;

        double g = 0.5 * (1 - Math.sqrt(Math.pow(cBar, 7) / (Math.pow(cBar, 7) + Math.pow(25, 7))));

        double a1Prime = (1 + g) * a1;
        double a2Prime = (1 + g) * a2;

        double c1Prime = Math.sqrt(a1Prime * a1Prime + b1 * b1);
        double c2Prime = Math.sqrt(a2Prime * a2Prime + b2 * b2);

        double h1Prime = Math.atan2(b1, a1Prime) * 180 / Math.PI;
        if (h1Prime < 0) h1Prime += 360;

        double h2Prime = Math.atan2(b2, a2Prime) * 180 / Math.PI;
        if (h2Prime < 0) h2Prime += 360;

        double deltaL = l2 - l1;
        double deltaC = c2Prime - c1Prime;

        double deltaH;
        if (c1Prime * c2Prime == 0) {
            deltaH = 0;
        } else if (Math.abs(h2Prime - h1Prime) <= 180) {
            deltaH = h2Prime - h1Prime;
        } else if (h2Prime - h1Prime > 180) {
            deltaH = h2Prime - h1Prime - 360;
        } else {
            deltaH = h2Prime - h1Prime + 360;
        }

        double deltaHPrime = 2 * Math.sqrt(c1Prime * c2Prime) * Math.sin(Math.toRadians(deltaH / 2));

        double lBar = (l1 + l2) / 2;
        double cPrimeBar = (c1Prime + c2Prime) / 2;

        double hPrimeBar;
        if (c1Prime * c2Prime == 0) {
            hPrimeBar = h1Prime + h2Prime;
        } else if (Math.abs(h1Prime - h2Prime) <= 180) {
            hPrimeBar = (h1Prime + h2Prime) / 2;
        } else if (Math.abs(h1Prime - h2Prime) > 180 && (h1Prime + h2Prime) < 360) {
            hPrimeBar = (h1Prime + h2Prime + 360) / 2;
        } else {
            hPrimeBar = (h1Prime + h2Prime - 360) / 2;
        }

        double t = 1 - 0.17 * Math.cos(Math.toRadians(hPrimeBar - 30)) +
            0.24 * Math.cos(Math.toRadians(2 * hPrimeBar)) +
            0.32 * Math.cos(Math.toRadians(3 * hPrimeBar + 6)) -
            0.20 * Math.cos(Math.toRadians(4 * hPrimeBar - 63));

        double deltaTheta = 30 * Math.exp(-Math.pow((hPrimeBar - 275) / 25, 2));

        double rc = 2 * Math.sqrt(Math.pow(cPrimeBar, 7) / (Math.pow(cPrimeBar, 7) + Math.pow(25, 7)));

        double sl = 1 + (0.015 * Math.pow(lBar - 50, 2)) / Math.sqrt(20 + Math.pow(lBar - 50, 2));
        double sc = 1 + 0.045 * cPrimeBar;
        double sh = 1 + 0.015 * cPrimeBar * t;

        double rt = -Math.sin(2 * Math.toRadians(deltaTheta)) * rc;

        double kL = 1.0;
        double kC = 1.0;
        double kH = 1.0;

        double deltaE = Math.sqrt(
            Math.pow(deltaL / (kL * sl), 2) +
                Math.pow(deltaC / (kC * sc), 2) +
                Math.pow(deltaHPrime / (kH * sh), 2) +
                rt * (deltaC / (kC * sc)) * (deltaHPrime / (kH * sh))
        );

        return deltaE;
    }

    private static int parseHexColor(String hexColor) {
        if (hexColor == null || !hexColor.startsWith("#") || hexColor.length() != 7) {
            return -1;
        }

        try {
            return Integer.parseInt(hexColor.substring(1), 16);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static Component processHexColorEmojis(Component component) {
        Component processedComponent = component;

        if (component instanceof TextComponent textComponent) {
            String content = textComponent.content();
            Style style = textComponent.style();
            TextColor color = style.color();

            if (color != null && !content.isEmpty()) {
                if (color instanceof NamedTextColor) {
                    return component;
                }

                String hexColor = String.format("#%06x", color.value());
                StringBuilder newContent = new StringBuilder();
                boolean hasEmojiReplacements = false;

                for (int i = 0; i < content.length(); i++) {
                    char ch = content.charAt(i);
                    String charStr = String.valueOf(ch);

                    Map<String, String> letterColorMap = HEX_COLOR_EMOJI_MAP.get(charStr);

                    if (letterColorMap != null) {
                        String emoji = letterColorMap.get(hexColor);
                        if (emoji == null) {
                            String nearestColor = findNearestColor(hexColor, letterColorMap.keySet());
                            if (nearestColor != null) {
                                emoji = letterColorMap.get(nearestColor);
                            }
                        }

                        if (emoji != null) {
                            if (style.hasDecoration(TextDecoration.OBFUSCATED)) {
                                hasEmojiReplacements = true;
                                continue;
                            }

                            newContent.append(emoji);
                            hasEmojiReplacements = true;
                            continue;
                        }
                    }
                    newContent.append(ch);
                }

                if (hasEmojiReplacements) {
                    processedComponent = Component.text(newContent.toString()).style(style.color(null));
                }
            }
        }

        List<Component> newChildren = new ArrayList<>();
        boolean hasChildChanges = false;

        for (Component child : component.children()) {
            Component processed = processHexColorEmojis(child);
            newChildren.add(processed);
            if (processed != child) {
                hasChildChanges = true;
            }
        }

        if (hasChildChanges || processedComponent != component) {
            return processedComponent.children(newChildren);
        }

        return component;
    }

    /**
     * Convert a Java message to the legacy format ready for bedrock. Unlike
     * {@link #convertMessageRaw(Component, String)} this adds a leading color reset. In Bedrock
     * some places have build-in colors.
     *
     * @param message Java message
     * @param locale Locale to use for translation strings
     * @return Parsed and formatted message for bedrock
     */
    public static String convertMessage(Component message, String locale) {
        return convertMessage(message, locale, true);
    }

    /**
     * Convert a Java message to the legacy format ready for bedrock, for use in item tooltips
     * (a gray color is applied).
     *
     * @param message Java message
     * @param locale Locale to use for translation strings
     * @return Parsed and formatted message for bedrock, in gray color
     */
    public static String convertMessageForTooltip(Component message, String locale) {
        return RESET + ChatColor.GRAY + convertMessageRaw(message, locale);
    }

    /**
     * Convert a Java message to the legacy format ready for bedrock. Unlike {@link #convertMessage(Component, String)}
     * this version does not add a leading color reset. In Bedrock some places have build-in colors.
     *
     * @param message Java message
     * @param locale Locale to use for translation strings
     * @return Parsed and formatted message for bedrock
     */
    public static String convertMessageRaw(Component message, String locale) {
        return convertMessage(message, locale, false);
    }

    private static String convertMessage(Component message, String locale, boolean addLeadingResetFormat) {
        try {
            // Translate any components that require it
            message = RENDERER.render(message, locale);

            if (locale.toLowerCase().contains("en_")) {
                message = processHexColorEmojis(message);
            }

            String legacy = BEDROCK_SERIALIZER.serialize(message);

            StringBuilder finalLegacy = new StringBuilder();
            char[] legacyChars = legacy.toCharArray();
            boolean lastFormatReset = !addLeadingResetFormat;
            for (int i = 0; i < legacyChars.length; i++) {
                char legacyChar = legacyChars[i];
                if (legacyChar != ChatColor.ESCAPE || i >= legacyChars.length - 1) {
                    // No special formatting for Bedrock needed
                    // Or, we're at the end of the string
                    finalLegacy.append(legacyChar);
                    lastFormatReset = false;
                    continue;
                }

                char next = legacyChars[++i];
                if (BEDROCK_COLORS.indexOf(next) != -1) {
                    // Unlike Java Edition, the ChatFormatting is not reset when a ChatColor is added
                    if (!lastFormatReset) {
                        finalLegacy.append(RESET);
                    }
                }
                finalLegacy.append(BASE).append(next);
                lastFormatReset = next == 'r';
            }

            String finalLegacyString = finalLegacy.toString();

            // Remove duplicate resets and trailing resets
            finalLegacyString = RESET_PATTERN.matcher(finalLegacyString).replaceAll(RESET);
            if (finalLegacyString.endsWith(RESET)) {
                finalLegacyString = finalLegacyString.substring(0, finalLegacyString.length() - 2);
            }

            // If the message contains \n then go through and re-set the color after each by caching the last color
            // Bedrock is dumb and resets the color after a newline
            if (finalLegacyString.contains("\n")) {
                StringBuilder output = new StringBuilder();

                StringBuilder lastColors = new StringBuilder();
                for (int i = 0; i < finalLegacyString.length(); i++) {
                    char c = finalLegacyString.charAt(i);

                    output.append(c);

                    if (c == ChatColor.ESCAPE) {
                        // If the string ends with a formatting character, remove and skip
                        if (i >= finalLegacyString.length() - 1) {
                            output = output.deleteCharAt(output.length() - 1);
                            continue;
                        }

                        char newColor = finalLegacyString.charAt(i + 1);
                        if (newColor == 'r') {
                            lastColors = new StringBuilder();
                        } else {
                            lastColors.append(ChatColor.ESCAPE).append(newColor);
                        }
                    } else if (c == '\n' && !lastColors.isEmpty()) {
                        output.append(lastColors);
                    }
                }

                return output.toString();
            } else {
                return finalLegacyString;
            }
        } catch (Exception e) {
            GeyserImpl.getInstance().getLogger().debug(GSON_SERIALIZER.serialize(message));
            GeyserImpl.getInstance().getLogger().error("Failed to parse message", e);

            return "";
        }
    }

    public static String convertJsonMessage(String message, String locale) {
        return convertMessage(GSON_SERIALIZER.deserialize(message), locale);
    }

    /**
     * Convenience method for locale getting.
     */
    public static String convertMessage(GeyserSession session, Component message) {
        return convertMessage(message, session.locale());
    }

    /**
     * DO NOT USE THIS METHOD unless where you're calling from does not have a (reliable) way of getting the
     * context's locale.
     */
    public static String convertMessage(Component message) {
        return convertMessage(message, GeyserLocale.getDefaultLocale());
    }

    /**
     * Verifies the message is valid JSON in case it's plaintext. Works around GsonComponentSerializer not using lenient mode.
     * See <a href="https://wiki.vg/Chat">here</a> for messages sent in lenient mode, and for a description on leniency.
     *
     * @param message Potentially lenient JSON message
     * @param locale Locale to use for translation strings
     * @return Bedrock formatted message
     */
    public static String convertMessageLenient(String message, String locale) {
        if (message == null) {
            return "";
        }
        if (message.isBlank()) {
            return message;
        }

        try {
            return convertJsonMessage(message, locale);
        } catch (Exception ignored) {
            // Use the default legacy serializer since message is java-legacy
            String convertedMessage = convertMessage(LegacyComponentSerializer.legacySection().deserialize(message), locale);

            // We have to do this since Adventure strips the starting reset character
            if (message.startsWith(RESET) && !convertedMessage.startsWith(RESET)) {
                convertedMessage = RESET + convertedMessage;
            }

            return convertedMessage;
        }
    }

    public static String convertMessageLenient(String message) {
        return convertMessageLenient(message, GeyserLocale.getDefaultLocale());
    }

    /**
     * Convert a Java message to plain text
     *
     * @param message Message to convert
     * @param locale Locale to use for translation strings
     * @return The plain text of the message
     */
    public static String convertToPlainText(Component message, String locale) {
        if (message == null) {
            return "";
        }
        return PlainTextComponentSerializer.plainText().serialize(RENDERER.render(message, locale));
    }

    /**
     * Convert legacy format message to plain text
     *
     * @param message Message to convert
     * @return The plain text of the message
     */
    public static String convertToPlainText(String message) {
        char[] input = message.toCharArray();
        char[] output = new char[input.length];
        int outputSize = 0;
        for (int i = 0, inputLength = input.length; i < inputLength; i++) {
            char c = input[i];
            if (c == ChatColor.ESCAPE) {
                i++;
            } else {
                output[outputSize++] = c;
            }
        }
        return new String(output, 0, outputSize);
    }

    /**
     * Convert JSON and legacy format message to plain text
     *
     * @param message Message to convert
     * @param locale Locale to use for translation strings
     * @return The plain text of the message
     */
    public static String convertToPlainTextLenient(String message, String locale) {
        if (message == null) {
            return "";
        }
        Component messageComponent = null;
        if (message.startsWith("{") && message.endsWith("}")) {
            // Message is a JSON object
            try {
                messageComponent = GSON_SERIALIZER.deserialize(message);
                // Translate any components that require it
                messageComponent = RENDERER.render(messageComponent, locale);
            } catch (Exception ignored) {
            }
        }
        if (messageComponent == null) {
            messageComponent = LegacyComponentSerializer.legacySection().deserialize(message);
        }
        return PlainTextComponentSerializer.plainText().serialize(messageComponent);
    }

    public static void handleChatPacket(GeyserSession session, Component message, Holder<ChatType> chatTypeHolder, Component targetName, Component sender, @Nullable UUID senderUuid) {
        TextPacket textPacket = new TextPacket();
        textPacket.setPlatformChatId("");
        textPacket.setSourceName("");

        if (senderUuid == null) {
            textPacket.setXuid(session.getAuthData().xuid());
        } else {
            String xuid = "";
            GeyserSession playerSession = GeyserImpl.getInstance().connectionByUuid(senderUuid);

            // Prefer looking up xuid using the session to catch linked players
            if (playerSession != null) {
                xuid = playerSession.getAuthData().xuid();
            } else if (senderUuid.version() == 0) {
                xuid = Long.toString(senderUuid.getLeastSignificantBits());
            }
            textPacket.setXuid(xuid);
        }
        textPacket.setType(TextPacket.Type.CHAT);

        textPacket.setNeedsTranslation(false);

        ChatType chatType = chatTypeHolder.getOrCompute(session.getRegistryCache().registry(JavaRegistries.CHAT_TYPE)::byId);
        if (chatType != null && chatType.chat() != null) {
            var chat = chatType.chat();
            // As of 1.19 - do this to apply all the styling for signed messages
            // Though, Bedrock cannot care about the signed stuff.
            TranslatableComponent.Builder withDecoration = Component.translatable()
                    .key(chat.translationKey())
                    .style(ChatDecoration.getStyle(chat));
            List<ChatTypeDecoration.Parameter> parameters = chat.parameters();
            List<Component> args = new ArrayList<>(3);
            if (parameters.contains(ChatDecoration.Parameter.TARGET)) {
                args.add(targetName);
            }
            if (parameters.contains(ChatDecoration.Parameter.SENDER)) {
                args.add(sender);
            }
            if (parameters.contains(ChatDecoration.Parameter.CONTENT)) {
                args.add(message);
            }
            withDecoration.arguments(args);
            textPacket.setMessage(MessageTranslator.convertMessage(withDecoration.build(), session.locale()));
        } else {
            session.getGeyser().getLogger().debug("Likely illegal chat type detection found.");
            if (session.getGeyser().config().debugMode()) {
                Thread.dumpStack();
            }
            textPacket.setMessage(MessageTranslator.convertMessage(message, session.locale()));
        }

        session.sendUpstreamPacket(textPacket);
    }

    /**
     * Checks if the given message is over 256 characters (Java edition server chat limit) and sends a message to the user if it is
     *
     * @param message Message to check
     * @param session {@link GeyserSession} for the user
     * @return True if the message is too long, false if not
     */
    public static boolean isTooLong(String message, GeyserSession session) {
        if (message.length() > 256) {
            session.sendMessage(GeyserLocale.getPlayerLocaleString("geyser.chat.too_long", session.locale(), message.length()));
            return true;
        }

        return false;
    }

    /**
     * Normalizes whitespaces - a thing a vanilla client apparently does with commands and chat messages.
     */
    public static String normalizeSpace(String string) {
        if (string == null || string.isEmpty()) {
            return string;
        }
        final int size = string.length();
        final char[] newChars = new char[size];
        int count = 0;
        int whitespacesCount = 0;
        boolean startWhitespaces = true;
        for (int i = 0; i < size; i++) {
            final char actualChar = string.charAt(i);
            final boolean isWhitespace = Character.isWhitespace(actualChar);
            if (isWhitespace) {
                if (whitespacesCount == 0 && !startWhitespaces) {
                    newChars[count++] = ' ';
                }
                whitespacesCount++;
            } else {
                startWhitespaces = false;
                // Replace non-breaking spaces with regular spaces for normalization
                newChars[count++] = (actualChar == '\u00A0' ? ' ' : actualChar);
                whitespacesCount = 0;
            }
        }
        if (startWhitespaces) {
            return "";
        }
        return new String(newChars, 0, count - (whitespacesCount > 0 ? 1 : 0)).trim();
    }

    /**
     * Deserialize an NbtMap with a description text component (usually provided from a registry) into a Bedrock-formatted string.
     */
    public static String deserializeDescription(GeyserSession session, NbtMap tag) {
        Object description = tag.get("description");
        Component parsed = componentFromNbtTag(description);
        return convertMessage(session, parsed);
    }

    /**
     * Deserialize an NbtMap with a description text component (usually provided from a registry) into a Bedrock-formatted string.
     */
    public static String deserializeDescriptionForTooltip(GeyserSession session, NbtMap tag) {
        Object description = tag.get("description");
        Component parsed = componentFromNbtTag(description);
        return convertMessageForTooltip(parsed, session.locale());
    }

    /**
     * Should only be used by {@link org.geysermc.geyser.session.cache.RegistryCache.RegistryReader}s, as these do not always have a {@link GeyserSession} available.
     */
    public static @Nullable String convertFromNullableNbtTag(Optional<GeyserSession> session, @Nullable Object nbtTag) {
        if (nbtTag == null) {
            return null;
        }
        return session.map(present -> convertMessage(present, componentFromNbtTag(nbtTag)))
            .orElse("MISSING GEYSER SESSION");
    }

    public static Component componentFromNbtTag(Object nbtTag) {
        return componentFromNbtTag(nbtTag, Style.empty());
    }

    public static List<String> signTextFromNbtTag(GeyserSession session, List<?> nbtTag) {
        var components = componentsFromNbtList(nbtTag, Style.empty());
        List<String> messages = new ArrayList<>();
        for (Component component : components) {
            messages.add(convertMessageRaw(component, session.locale()));
        }
        return messages;
    }

    private static Component componentFromNbtTag(Object nbtTag, Style style) {
        if (nbtTag instanceof String literal) {
            return Component.text(literal).style(style);
        } else if (nbtTag instanceof List<?> list) {
            return Component.join(JoinConfiguration.noSeparators(), componentsFromNbtList(list, style));
        } else if (nbtTag instanceof NbtMap map) {
            Component component = null;
            String text = map.getString("text", map.getString("", null));
            if (text != null) {
                component = Component.text(text);
            } else {
                String translateKey = map.getString("translate", null);
                if (translateKey != null) {
                    String fallback = map.getString("fallback", null);
                    List<Component> args = new ArrayList<>();

                    Object with = map.get("with");
                    if (with instanceof List<?> list) {
                        args = componentsFromNbtList(list, style);
                    } else if (with != null) {
                        args.add(componentFromNbtTag(with, style));
                    }
                    component = Component.translatable(translateKey, fallback, args);
                }
            }

            if (component != null) {
                Style newStyle = getStyleFromNbtMap(map, style);
                component = component.style(newStyle);

                Object extra = map.get("extra");
                if (extra != null) {
                    component = component.append(componentFromNbtTag(extra, newStyle));
                }

                return component;
            }
        }

        GeyserImpl.getInstance().getLogger().error("Expected tag to be a literal string, a list of components, or a component object with a text/translate key: " + nbtTag);
        return Component.empty();
    }

    private static List<Component> componentsFromNbtList(List<?> list, Style style) {
        List<Component> components = new ArrayList<>();
        for (Object entry : list) {
            components.add(componentFromNbtTag(entry, style));
        }
        return components;
    }

    public static Style getStyleFromNbtMap(NbtMap map) {
        Style.Builder style = Style.style();

        String colorString = map.getString("color", null);
        if (colorString != null) {
            if (colorString.startsWith(TextColor.HEX_PREFIX)) {
                style.color(TextColor.fromHexString(colorString));
            } else {
                style.color(NamedTextColor.NAMES.value(colorString));
            }
        }

        map.listenForBoolean("bold", value -> style.decoration(TextDecoration.BOLD, value));
        map.listenForBoolean("italic", value -> style.decoration(TextDecoration.ITALIC, value));
        map.listenForBoolean("underlined", value -> style.decoration(TextDecoration.UNDERLINED, value));
        map.listenForBoolean("strikethrough", value -> style.decoration(TextDecoration.STRIKETHROUGH, value));
        map.listenForBoolean("obfuscated", value -> style.decoration(TextDecoration.OBFUSCATED, value));

        return style.build();
    }

    public static Style getStyleFromNbtMap(NbtMap map, Style base) {
        return base.merge(getStyleFromNbtMap(map));
    }

    public static void init() {
        // no-op
    }
}
