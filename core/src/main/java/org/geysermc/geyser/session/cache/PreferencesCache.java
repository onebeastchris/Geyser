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

package org.geysermc.geyser.session.cache;

import lombok.Getter;
import lombok.Setter;
import org.geysermc.geyser.api.packs.ResourcePack;
import org.geysermc.geyser.configuration.GeyserConfiguration;
import org.geysermc.geyser.pack.ResourcePackUtil;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.util.CooldownUtils;

import java.util.Map;

@Getter
public class PreferencesCache {
    private final GeyserSession session;

    /**
     * True if the client prefers being shown their coordinates, regardless if they're being shown or not.
     * This will be true everytime the client joins the server because neither the client nor server store the preference permanently.
     */
    @Setter
    private boolean prefersShowCoordinates = true;

    /**
     * If the client's preference will be ignored, this will return false.
     */
    private boolean allowShowCoordinates;

    /**
     * If the session wants custom skulls to be shown.
     */
    @Setter
    private boolean prefersCustomSkulls;

    /**
     * Which CooldownType the client prefers. Initially set to {@link CooldownUtils#getDefaultShowCooldown()}.
     */
    @Setter
    private CooldownUtils.CooldownType cooldownPreference = CooldownUtils.getDefaultShowCooldown();

    /**
     * The resource packs to load for the client; includes the global packs from Geysers packs folder.
     */
    @Setter
    @Getter
    public Map<String, ResourcePack> PACKS;

    public PreferencesCache(GeyserSession session) {
        this.session = session;

        prefersCustomSkulls = session.getGeyser().getConfig().isAllowCustomSkulls();
        PACKS = ResourcePackUtil.PACKS;
    }

    /**
     * Tell the client to hide or show the coordinates.
     *
     * If {@link #prefersShowCoordinates} is true, coordinates will be shown, unless either of the following conditions apply: <br>
     * <br>
     * {@link GeyserSession#reducedDebugInfo} is enabled
     * {@link GeyserConfiguration#isShowCoordinates()} is disabled
     */
    public void updateShowCoordinates() {
        allowShowCoordinates = !session.isReducedDebugInfo() && session.getGeyser().getConfig().isShowCoordinates();
        session.sendGameRule("showcoordinates", allowShowCoordinates && prefersShowCoordinates);
    }

    /**
     * @return true if the session prefers custom skulls, and the config allows them.
     */
    public boolean showCustomSkulls() {
        return prefersCustomSkulls && session.getGeyser().getConfig().isAllowCustomSkulls();
    }

    /**
     * @param packs The resource packs to load for the client; includes the global packs from Geysers packs folder.
     */
    public void addPacks(Map<String, ResourcePack> packs) {
        PACKS.putAll(packs);
    }
}
