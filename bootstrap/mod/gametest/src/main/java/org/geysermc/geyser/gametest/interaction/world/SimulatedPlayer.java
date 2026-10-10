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

package org.geysermc.geyser.gametest.interaction.world;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * A plain (non-server) player standing in for the Java client's local player. It lives in a {@link SimulatedClientLevel},
 * so that {@code player.level().isClientSide()} is true for vanilla code using the player's level.
 * <p>
 * Use {@link #setGameMode(GameType)} to change the game mode; it updates the abilities like the client does.
 * Sneaking is {@link #setShiftKeyDown(boolean)}, the food level {@code getFoodData().setFoodLevel(int)} and
 * cooldowns {@code getCooldowns().addCooldown(ItemStack, int)}.
 */
public final class SimulatedPlayer extends Player {
    private static final GameProfile PROFILE = new GameProfile(UUID.fromString("5f8eb73b-25be-4c5a-a50f-d27d65e30ca0"), "geyser-interaction");

    // Nullable as the super constructor may query the game mode before this class' fields are initialised
    private @Nullable GameType gameType = GameType.SURVIVAL;
    private boolean gameMaster;

    public SimulatedPlayer(Level level) {
        super(level, PROFILE);
        setGameMode(GameType.SURVIVAL);
    }

    public void setGameMode(GameType gameType) {
        this.gameType = gameType;
        gameType.updatePlayerAbilities(getAbilities());
    }

    /**
     * @param gameMaster whether the player has the permission level needed for game master blocks
     *                   ({@code Permissions.COMMANDS_GAMEMASTER}, i.e. op level 2)
     */
    public void setGameMaster(boolean gameMaster) {
        this.gameMaster = gameMaster;
    }

    public boolean isGameMaster() {
        return gameMaster;
    }

    @Override
    public GameType gameMode() {
        return gameType == null ? GameType.SURVIVAL : gameType;
    }

    @Override
    public boolean canUseGameMasterBlocks() {
        // Mirrors Player#canUseGameMasterBlocks, with the permission check replaced
        return getAbilities().instabuild && gameMaster;
    }

    @Override
    public boolean isLocalPlayer() {
        return true;
    }
}
