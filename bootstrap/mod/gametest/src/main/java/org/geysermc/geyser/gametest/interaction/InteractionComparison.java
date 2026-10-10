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

package org.geysermc.geyser.gametest.interaction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.gametest.interaction.world.SimulatedClientLevel;
import org.geysermc.geyser.gametest.interaction.world.SimulatedPlayer;
import org.geysermc.geyser.gametest.registries.GameTestJavaRegistryProvider;
import org.geysermc.geyser.interaction.BlockHit;
import org.geysermc.geyser.interaction.InteractionSimulator;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Runs single clicks through vanilla's client logic and Geyser's simulation, and collects every difference.
 * <p>
 * Every simulated call gets a fresh {@link SimulatedPlayer} and a reset {@link SimulatedClientLevel}, as vanilla
 * mutates both. Geyser's simulation runs first, as it reads the player's state.
 */
public final class InteractionComparison {
    /**
     * Placement flags for scenario blocks: {@code Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE}, so placing a block
     * neither updates neighbours nor makes the block check whether it survives.
     */
    public static final int PLACE_FLAGS = 2 | 16;
    private static final int MAX_LOGGED_MISMATCHES = 1000;
    private static final int MAX_LOGGED_THROWABLES = 10;
    /**
     * Distance from the clicked location to the player's eyes.
     */
    private static final double EYE_DISTANCE = 2.5;

    /**
     * A player state: game mode, sneaking, op, and an optional additional setup.
     */
    public record PlayerState(String name, GameType gameType, boolean sneaking, boolean gameMaster, @Nullable Consumer<Player> setup) {
        public static final PlayerState SURVIVAL = new PlayerState("survival", GameType.SURVIVAL, false, false, null);
        public static final PlayerState SNEAKING = new PlayerState("survival+sneaking", GameType.SURVIVAL, true, false, null);
        public static final PlayerState CREATIVE = new PlayerState("creative", GameType.CREATIVE, false, false, null);
        public static final PlayerState CREATIVE_OP = new PlayerState("creative+op", GameType.CREATIVE, false, true, null);
        public static final PlayerState ADVENTURE = new PlayerState("adventure", GameType.ADVENTURE, false, false, null);
        public static final PlayerState SPECTATOR = new PlayerState("spectator", GameType.SPECTATOR, false, false, null);
        public static final PlayerState HUNGRY = new PlayerState("survival+hungry", GameType.SURVIVAL, false, false,
            player -> player.getFoodData().setFoodLevel(6));
        public static final PlayerState ON_COOLDOWN = new PlayerState("survival+cooldown", GameType.SURVIVAL, false, false, player -> {
            player.getCooldowns().addCooldown(player.getMainHandItem(), 100);
            player.getCooldowns().addCooldown(player.getOffhandItem(), 100);
        });

        public static PlayerState hint(ScenarioHintRegistry.NamedPlayerSetup setup) {
            return new PlayerState("survival+" + setup.name(), GameType.SURVIVAL, false, false, setup.setup());
        }
    }

    /**
     * A click on a block, or a look at it (for {@code Item#use}).
     *
     * @param pos the clicked block
     * @param face the clicked face
     * @param location the clicked location
     * @param air whether the player looks at nothing (only for {@code Item#use})
     */
    public record Click(BlockPos pos, Direction face, Vec3 location, boolean air) {

        /**
         * @param u horizontal position on the face, 0 to 1 (ignored for the top face)
         * @param v vertical position on the face, 0 to 1 (ignored for the top face)
         */
        public static Click on(BlockPos pos, Direction face, double u, double v) {
            Vec3 location = switch (face) {
                case UP -> new Vec3(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
                case DOWN -> new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                case NORTH -> new Vec3(pos.getX() + u, pos.getY() + v, pos.getZ());
                case SOUTH -> new Vec3(pos.getX() + u, pos.getY() + v, pos.getZ() + 1);
                case WEST -> new Vec3(pos.getX(), pos.getY() + v, pos.getZ() + u);
                case EAST -> new Vec3(pos.getX() + 1, pos.getY() + v, pos.getZ() + u);
            };
            return new Click(pos, face, location, false);
        }

        public static Click center(BlockPos pos, Direction face) {
            return on(pos, face, 0.5, 0.5);
        }

        /**
         * Looking straight up from inside the given (empty) block, with nothing in reach.
         */
        public static Click air(BlockPos pos) {
            return new Click(pos, Direction.UP, new Vec3(pos.getX() + 0.5, pos.getY() + 100, pos.getZ() + 0.5), true);
        }

        @Override
        public String toString() {
            if (air) {
                return "air";
            }
            Vec3 relative = location.subtract(pos.getX(), pos.getY(), pos.getZ());
            return face.name().toLowerCase(Locale.ROOT) + String.format(Locale.ROOT, "@(%.2f,%.2f,%.2f)", relative.x, relative.y, relative.z);
        }
    }

    private final String testName;
    private final ServerLevel level;
    private final SimulatedClientLevel clientLevel;
    private final GameTestJavaRegistryProvider registries;
    private final GameTestComponentResolver componentResolver;
    private final Set<String> mismatches = new LinkedHashSet<>();
    private int mismatchCount;
    private int simulations;
    private int loggedThrowables;
    private final long startTime = System.nanoTime();
    private String situation = "";

    public InteractionComparison(GameTestHelper helper, String testName) {
        this.testName = testName;
        this.level = helper.getLevel();
        this.clientLevel = SimulatedClientLevel.create(level);
        this.registries = new GameTestJavaRegistryProvider(level.registryAccess());
        this.componentResolver = new GameTestComponentResolver(registries);
    }

    public ServerLevel level() {
        return level;
    }

    /**
     * Sets a description of the current world setup (block state, setup name), prefixed to every mismatch.
     */
    public void situation(String situation) {
        this.situation = situation;
    }

    /**
     * Compares {@code useItemOn} for the given hand.
     */
    public void compareUseItemOn(ItemStack main, ItemStack off, PlayerState state, Click click, InteractionHand hand) {
        SimulatedPlayer player = preparePlayer(main, off, state, click);
        if (player == null) {
            return;
        }
        String description = describe(main, off, state, click, "useItemOn(" + hand + ")");
        GameTestInteractionContext context = new GameTestInteractionContext(clientLevel, player, registries, componentResolver);
        BlockHit hit = new BlockHit(VanillaConversions.toGeyser(click.pos()), VanillaConversions.toGeyser(click.face()),
            VanillaConversions.toGeyser(click.location()), false);
        compare(description,
            () -> InteractionSimulator.useItemOn(context, VanillaConversions.toGeyser(hand), hit),
            () -> VanillaInteractions.useItemOn(player, clientLevel.level(), hand, new BlockHitResult(click.location(), click.face(), click.pos(), false)));
    }

    /**
     * Compares {@code useItem} for the given hand, with the player looking at the clicked location.
     */
    public void compareUseItem(ItemStack main, ItemStack off, PlayerState state, Click click, InteractionHand hand) {
        SimulatedPlayer player = preparePlayer(main, off, state, click);
        if (player == null) {
            return;
        }
        String description = describe(main, off, state, click, "useItem(" + hand + ")");
        GameTestInteractionContext context = new GameTestInteractionContext(clientLevel, player, registries, componentResolver);
        compare(description,
            () -> InteractionSimulator.useItem(context, VanillaConversions.toGeyser(hand)),
            () -> VanillaInteractions.useItem(player, clientLevel.level(), hand));
    }

    private void compare(String description, Supplier<org.geysermc.geyser.util.InteractionResult> geyser,
                         Supplier<net.minecraft.world.InteractionResult> vanilla) {
        simulations++;
        String geyserResult;
        String vanillaResult;
        try {
            geyserResult = geyser.get().name();
        } catch (Throwable throwable) {
            geyserResult = "threw " + throwable;
            logThrowable("Geyser threw for " + description, throwable);
        }
        try {
            vanillaResult = VanillaConversions.toGeyser(vanilla.get()).name();
        } catch (Throwable throwable) {
            vanillaResult = "threw " + throwable;
            logThrowable("Vanilla threw for " + description, throwable);
        }
        if (!geyserResult.equals(vanillaResult)) {
            mismatch(description + ": vanilla=" + vanillaResult + " geyser=" + geyserResult);
        }
    }

    private void logThrowable(String message, Throwable throwable) {
        // Only the first stack traces; the rest is in the mismatch list
        if (loggedThrowables++ < MAX_LOGGED_THROWABLES) {
            GeyserImpl.getInstance().getLogger().error("[" + testName + "] " + message, throwable);
        }
    }

    public void mismatch(String message) {
        mismatchCount++;
        if (mismatches.size() < MAX_LOGGED_MISMATCHES) {
            mismatches.add(message);
        }
    }

    private @Nullable SimulatedPlayer preparePlayer(ItemStack main, ItemStack off, PlayerState state, Click click) {
        clientLevel.reset();
        SimulatedPlayer player = new SimulatedPlayer(clientLevel.level());
        clientLevel.setPlayer(player);
        player.setGameMode(state.gameType());
        player.setGameMaster(state.gameMaster());
        player.setShiftKeyDown(state.sneaking());
        player.setItemInHand(InteractionHand.MAIN_HAND, main.copy());
        player.setItemInHand(InteractionHand.OFF_HAND, off.copy());

        // Stand in front of the clicked face, looking at the clicked location
        Vec3 eyes;
        if (click.air()) {
            eyes = Vec3.atCenterOf(click.pos());
        } else {
            Direction face = click.face();
            eyes = click.location().add(face.getStepX() * EYE_DISTANCE, face.getStepY() * EYE_DISTANCE, face.getStepZ() * EYE_DISTANCE);
        }
        Vec3 look = click.location().subtract(eyes);
        double horizontal = Math.sqrt(look.x * look.x + look.z * look.z);
        float yRot = (float) (Math.toDegrees(Math.atan2(look.z, look.x)) - 90.0);
        float xRot = (float) -Math.toDegrees(Math.atan2(look.y, horizontal));
        player.snapTo(eyes.x, eyes.y - player.getEyeHeight(), eyes.z, yRot, xRot);

        if (state.setup() != null) {
            try {
                state.setup().accept(player);
            } catch (Throwable throwable) {
                mismatch(describe(main, off, state, click, "player setup") + " threw " + throwable);
                return null;
            }
        }
        return player;
    }

    private String describe(ItemStack main, ItemStack off, PlayerState state, Click click, String call) {
        return situation + " | " + click + " | main=" + describe(main) + " off=" + describe(off) + " | " + state.name() + " | " + call;
    }

    public static String describe(ItemStack stack) {
        if (stack.isEmpty()) {
            return "empty";
        }
        String description = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (stack.getCount() != 1) {
            description = stack.getCount() + "x" + description;
        }
        if (!stack.getComponentsPatch().isEmpty()) {
            description += stack.getComponentsPatch();
        }
        return description;
    }

    public static String describe(BlockState state) {
        return state.toString();
    }

    /**
     * Logs the result and fails the test if there were mismatches.
     */
    public void finish(GameTestHelper helper) {
        long millis = (System.nanoTime() - startTime) / 1_000_000;
        GeyserImpl.getInstance().getLogger().info("[" + testName + "] " + simulations + " simulated clicks in " + millis + " ms, "
            + mismatchCount + " mismatches");
        if (mismatchCount == 0) {
            helper.succeed();
            return;
        }
        List<String> lines = new ArrayList<>(mismatches);
        GeyserImpl.getInstance().getLogger().error("[" + testName + "] " + mismatchCount + " interaction mismatches"
            + (mismatchCount > lines.size() ? " (" + lines.size() + " unique shown)" : "") + ":\n" + String.join("\n", lines));
        helper.fail(mismatchCount + " interaction mismatches (see log), e.g. " + String.join(" || ", lines.subList(0, Math.min(3, lines.size()))));
    }
}
