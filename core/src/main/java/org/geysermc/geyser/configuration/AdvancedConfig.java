/*
 * Copyright (c) 2024 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.configuration;

import org.geysermc.geyser.Constants;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultBoolean;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultNumeric;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultString;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.UUID;

@ConfigSerializable
public interface AdvancedConfig {
    // Cannot be type File yet because we may want to hide it in plugin instances.
    @Comment("""
        Floodgate uses encryption to ensure use from authorized sources.
        This should point to the public key generated by Floodgate (BungeeCord, Spigot or Velocity)
        You can ignore this when not using Floodgate.
        If you're using a plugin version of Floodgate on the same server, the key will automatically be picked up from Floodgate.""")
    @DefaultString("key.pem")
    String floodgateKeyFile();

    @Comment("""
            The maximum number of custom skulls to be displayed per player. Increasing this may decrease performance on weaker devices.
            Setting this to -1 will cause all custom skulls to be displayed regardless of distance or number.""")
    @DefaultNumeric(128)
    int maxVisibleCustomSkulls();

    @Comment("The radius in blocks around the player in which custom skulls are displayed.")
    @DefaultNumeric(32)
    int customSkullRenderDistance();

    @Comment("""
            Specify how many days images will be cached to disk to save downloading them from the internet.
            A value of 0 is disabled. (Default: 0)""")
    int cacheImages();

    @Comment("""
            Which item to use to mark unavailable slots in a Bedrock player inventory. Examples of this are the 2x2 crafting grid while in creative,
            or custom inventory menus with sizes different from the usual 3x9. A barrier block is the default item.""")
    @DefaultString("minecraft:barrier")
    String unusableSpaceBlock();

    @Comment("""
            Geyser updates the Scoreboard after every Scoreboard packet, but when Geyser tries to handle
            a lot of scoreboard packets per second can cause serious lag.
            This option allows you to specify after how many Scoreboard packets per seconds
            the Scoreboard updates will be limited to four updates per second.""")
    @DefaultNumeric(20)
    int scoreboardPacketThreshold();

    @Comment("""
            The internet supports a maximum MTU of 1492 but could cause issues with packet fragmentation.
            1400 is the default.""")
    @DefaultNumeric(1400)
    int mtu();

    @Comment("""
        Whether to connect directly into the Java server without creating a TCP connection.
        This should only be disabled if a plugin that interfaces with packets or the network does not work correctly with Geyser.
        If enabled, the remote address and port sections are ignored
        If disabled, expect performance decrease and latency increase
        """)
    @DefaultBoolean(true)
    @PlatformTypeSpecific
    boolean useDirectConnection();

    @Comment("""
        Whether Geyser should attempt to disable compression for Bedrock players. This should be a benefit as there is no need to compress data
        when Java packets aren't being handled over the network.
        This requires use-direct-connection to be true.
        """)
    @DefaultBoolean(true)
    @PlatformTypeSpecific
    boolean disableCompression();

    @Comment("Do not touch!")
    default UUID metricsUuid() {
        return UUID.randomUUID();
    }

    @Comment("Do not touch!")
    default int version() {
        return Constants.ADVANCED_CONFIG_VERSION;
    }
}
