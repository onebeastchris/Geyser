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

package org.geysermc.geyser.interaction;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityFlag;
import org.cloudburstmc.protocol.bedrock.packet.AnimateEntityPacket;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.util.EntityUtils;
import org.geysermc.geyser.util.InteractionResult;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundSwingPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundUseItemOnPacket;

/**
 * Performs a right-click for a session the way a Java client would ({@code Minecraft#startUseItem}): it walks both
 * hands, asks {@link InteractionSimulator} what the Java client would predict, and sends the matching packets.
 */
public final class SessionInteractions {

    /**
     * Right-clicks a block.
     *
     * @param cursor the clicked location relative to the block, as sent to the Java server
     * @param useTouchRotation whether item use packets should use the rotation of the touch interaction
     * @return the outcome of the click
     */
    public static @NonNull Outcome useOnBlock(@NonNull GeyserSession session, @NonNull BlockHit hit, @NonNull Vector3f cursor, boolean useTouchRotation) {
        session.setLastBlockUseSimulationTick(session.getTicks());
        if (!canStartUsing(session)) {
            return Outcome.NOTHING;
        }

        InteractionContext player = session.getInteractionContext();
        Vector3i position = hit.position();
        for (Hand hand : EntityUtils.HANDS) {
            GeyserItemStack heldItem = player.itemInHand(hand);

            // MultiPlayerGameMode#useItemOn: nothing is sent for blocks outside the world border
            if (!session.getWorldBorder().isInsideBorderBoundaries(Vector3f.from(position.getX() + 0.5f, position.getY(), position.getZ() + 0.5f))) {
                return new Outcome(InteractionResult.FAIL, null);
            }

            InteractionResult result = InteractionSimulator.useItemOn(player, hand, hit);
            // Storing the block position allows inconsistencies in block place checking from post-1.19 - pre-1.20.5 to be resolved.
            int sequence = session.getWorldCache().nextPredictionSequence();
            session.getWorldCache().markPositionInSequence(hit.adjacentPosition());
            session.sendDownstreamGamePacket(new ServerboundUseItemOnPacket(
                position,
                hit.face().mcpl(),
                hand,
                cursor.getX(), cursor.getY(), cursor.getZ(),
                hit.inside(),
                false,
                sequence));

            if (result.consumesAction()) {
                if (result.shouldSwing()) {
                    swing(session, hand);
                }
                return new Outcome(result, null);
            }
            if (result == InteractionResult.FAIL) {
                return new Outcome(result, null);
            }

            if (!heldItem.isEmpty() && session.getGameMode() != GameMode.SPECTATOR) {
                result = useItem(session, hand, useTouchRotation);
                if (result.consumesAction()) {
                    return new Outcome(result, hand);
                }
            }
        }
        return Outcome.NOTHING;
    }

    /**
     * Right-clicks without targeting a block or entity.
     *
     * @return the outcome of the click
     */
    public static @NonNull Outcome useInAir(@NonNull GeyserSession session, boolean useTouchRotation) {
        if (!canStartUsing(session) || session.getGameMode() == GameMode.SPECTATOR) {
            // Nothing is sent for item use in spectator mode
            return Outcome.NOTHING;
        }

        InteractionContext player = session.getInteractionContext();
        for (Hand hand : EntityUtils.HANDS) {
            if (!player.itemInHand(hand).isEmpty()) {
                InteractionResult result = useItem(session, hand, useTouchRotation);
                if (result.consumesAction()) {
                    return new Outcome(result, hand);
                }
            }
        }
        return Outcome.NOTHING;
    }

    /**
     * Mirrors {@code MultiPlayerGameMode#useItem} followed by the swing handling of {@code Minecraft#startUseItem}.
     */
    private static @NonNull InteractionResult useItem(GeyserSession session, Hand hand, boolean useTouchRotation) {
        InteractionResult result = InteractionSimulator.useItem(session.getInteractionContext(), hand);
        session.useItem(hand, useTouchRotation);
        if (result.shouldSwing()) {
            swing(session, hand);
        }
        return result;
    }

    /**
     * The Java client only starts a right-click when its hands are free and it is not already using an item.
     */
    private static boolean canStartUsing(GeyserSession session) {
        return !session.isHandsBusy() && !session.getPlayerEntity().getFlag(EntityFlag.USING_ITEM);
    }

    /**
     * Swings the given arm, as a Java client would after an interaction with {@link InteractionResult#SUCCESS}.
     */
    public static void swing(@NonNull GeyserSession session, @NonNull Hand hand) {
        session.sendDownstreamGamePacket(new ServerboundSwingPacket(hand));
        if (hand == Hand.MAIN_HAND) {
            // The Bedrock client animates its own main hand; this also stops BedrockAnimateTranslator from sending another swing
            session.activateArmAnimationTicking();
        } else {
            // Bedrock cannot use its off hand; play the animation through the optional pack
            AnimateEntityPacket offHandPacket = new AnimateEntityPacket();
            offHandPacket.setAnimation("animation.player.attack.rotations.offhand");
            offHandPacket.setNextState("default");
            offHandPacket.setBlendOutTime(0.0f);
            offHandPacket.setStopExpression("query.any_animation_finished");
            offHandPacket.setController("__runtime_controller");
            offHandPacket.getRuntimeEntityIds().add(session.getPlayerEntity().geyserId());
            session.sendUpstreamPacket(offHandPacket);
        }
    }

    /**
     * @return whether the Bedrock client's follow-up actions to a block click (a "click air" transaction or an
     * arm swing) should be ignored, since the simulation already performed everything the Java client would have
     */
    public static boolean isFollowUpOfBlockUse(@NonNull GeyserSession session) {
        return session.getTicks() - session.getLastBlockUseSimulationTick() <= 1;
    }

    /**
     * @param result the result that ended the click, or {@link InteractionResult#PASS} if no action consumed it
     * @param itemUseHand the hand whose item use ({@code Item#use}) consumed the click, if any
     */
    public record Outcome(@NonNull InteractionResult result, @Nullable Hand itemUseHand) {
        public static final Outcome NOTHING = new Outcome(InteractionResult.PASS, null);
    }

    private SessionInteractions() {
    }
}
