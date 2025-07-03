/*
 * Copyright (c) 2025 GeyserMC. http://geysermc.org
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

package org.geysermc.geyser.command.defaults;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.api.skin.SkinData;
import org.geysermc.geyser.api.util.TriState;
import org.geysermc.geyser.command.GeyserCommand;
import org.geysermc.geyser.command.GeyserCommandSource;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.skin.SkinManager;
import org.geysermc.geyser.skin.SkinProvider;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.io.IOException;

import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class TestCommand extends GeyserCommand {

    public TestCommand(@NonNull String name, @NonNull String description) {
        super(name, description, "", TriState.TRUE);
    }

    @Override
    public void register(CommandManager<GeyserCommandSource> manager) {
        manager.command(baseBuilder(manager)
            .required("username", stringParser())
            .handler(this::execute));
    }

    @Override
    public void execute(CommandContext<GeyserCommandSource> context) {
        GeyserSession session = GeyserImpl.getInstance().getSessionManager().getAllSessions().get(0);
        SkinProvider.requestTexturesFromUsername(context.get("username")).whenCompleteAsync((encodedJson, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                return;
            }
            try {
                SkinManager.GameProfileData gameProfileData = SkinManager.GameProfileData.loadFromJson(encodedJson);
                if (gameProfileData == null) {
                    return;
                }
                SkinData skinData = SkinProvider.reqSkinDataFromProfile(session, gameProfileData).join();
                SkinManager.sendSkinPacket(session, session.getPlayerEntity(), skinData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
