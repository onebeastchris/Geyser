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

package org.geysermc.geyser.translator.protocol.bedrock.entity.player.input;

import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.cloudburstmc.protocol.bedrock.packet.AnimateEntityPacket;
import org.cloudburstmc.protocol.bedrock.packet.AnimatePacket;
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket;
import org.geysermc.geyser.entity.EntityDefinitions;
import org.geysermc.geyser.entity.type.Entity;
import org.geysermc.geyser.entity.type.ItemFrameEntity;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.item.type.BlockItem;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.util.BlockUtils;
import org.geysermc.geyser.util.InteractionContext;
import org.geysermc.geyser.util.InteractionResult;
import org.geysermc.mcprotocollib.protocol.data.game.entity.object.Direction;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundSwingPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundUseItemOnPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundUseItemPacket;

import static org.geysermc.geyser.translator.protocol.bedrock.BedrockInventoryTransactionTranslator.*;

public class ItemUseHandler {

    public static void simulateHitResult(GeyserSession session, InventoryTransactionPacket packet) {

        if (session.isHandsBusy()) { // TODO add block breaking check here
            return;
        }

        final Vector3i packetBlockPosition = packet.getBlockPosition();
        if (packetBlockPosition.equals(Vector3i.ZERO)) {
            // We are not looking at a block - so we skip block interactions entirely and attempt to use items.
            InteractionContext context = InteractionContext.of(session, Vector3i.ZERO, packet.getClickPosition(), packet.getBlockFace(), Hand.MAIN_HAND);
            for (Hand hand : Hand.values()) {
                if (hand == Hand.OFF_HAND) {
                    context = context.withHand(Hand.OFF_HAND);
                }

                if (useItem(context)) {
                    return;
                }
            }
            return;
        }

        Vector3i blockPos = BlockUtils.getBlockPosition(packetBlockPosition, packet.getBlockFace());

        // TODO move to BlockItem?
        if (session.getGeyser().getConfig().isDisableBedrockScaffolding()) {
            float yaw = session.getPlayerEntity().getYaw();
            boolean isGodBridging = switch (packet.getBlockFace()) {
                case 2 -> yaw <= -135f || yaw > 135f;
                case 3 -> yaw <= 45f && yaw > -45f;
                case 4 -> yaw > 45f && yaw <= 135f;
                case 5 -> yaw <= -45f && yaw > -135f;
                default -> false;
            };
            if (isGodBridging) {
                restoreCorrectBlock(session, blockPos);
                return;
            }
        }

        // Check if this is a double placement due to an extended collision block
        if (!session.getBlockMappings().getExtendedCollisionBoxes().isEmpty()) {
            Vector3i belowBlockPos = null;
            switch (packet.getBlockFace()) {
                case 1 -> belowBlockPos = blockPos.add(0, -2, 0);
                case 2 -> belowBlockPos = blockPos.add(0, -1, 1);
                case 3 -> belowBlockPos = blockPos.add(0, -1, -1);
                case 4 -> belowBlockPos = blockPos.add(1, -1, 0);
                case 5 -> belowBlockPos = blockPos.add(-1, -1, 0);
            }

            if (belowBlockPos != null) {
                int belowBlock = session.getGeyser().getWorldManager().getBlockAt(session, belowBlockPos);
                BlockDefinition extendedCollisionDefinition = session.getBlockMappings().getExtendedCollisionBoxes().get(belowBlock);
                if (extendedCollisionDefinition != null && (System.currentTimeMillis() - session.getLastInteractionTime()) < 200) {
                    restoreCorrectBlock(session, blockPos);
                    return;
                }
            }
        }

        // Check to make sure the client isn't spamming interaction
        // Based on Nukkit 1.0, with changes to ensure holding down still works
        boolean hasAlreadyClicked = System.currentTimeMillis() - session.getLastInteractionTime() < 110.0 &&
            packetBlockPosition.distanceSquared(session.getLastInteractionBlockPosition()) < 0.00001;
        session.setLastInteractionBlockPosition(packetBlockPosition);
        session.setLastInteractionPlayerPosition(session.getPlayerEntity().getPosition());
        if (hasAlreadyClicked) {
            return;
        } else {
            // Only update the interaction time if it's valid - that way holding down still works.
            session.setLastInteractionTime(System.currentTimeMillis());
        }

        // TODO when does this happen? what do we reset?
        if (isIncorrectHeldItem(session, packet)) {
            restoreCorrectBlock(session, blockPos);
            return;
        }

        // Bedrock sends block interact code for a Java entity, so we send entity code back to Java
        if (session.getBlockMappings().isItemFrame(packet.getBlockDefinition())) {
            Entity itemFrameEntity = ItemFrameEntity.getItemFrameEntity(session, packet.getBlockPosition());
            if (itemFrameEntity != null) {
                processEntityInteraction(session, packet, itemFrameEntity);
                return;
            }
        }

        /*
        Checks to ensure that the range will be accepted by the server.
        "Not in range" doesn't refer to how far a vanilla client goes (that's a whole other mess),
        but how much a server will accept from the client maximum
         */
        // Blocks cannot be placed or destroyed outside of the world border
        if (!session.getWorldBorder().isInsideBorderBoundaries()) {
            restoreCorrectBlock(session, blockPos);
            return;
        }

        // As of 1.21, Paper does not have any additional range checks that would inconvenience normal players.
        Vector3f playerPosition = session.getPlayerEntity().getPosition();
        playerPosition = playerPosition.down(EntityDefinitions.PLAYER.offset() - session.getEyeHeight());

        if (!canInteractWithBlock(session, playerPosition, packetBlockPosition)) {
            restoreCorrectBlock(session, blockPos);
            return;
        }

        double clickPositionFullX = (double) packetBlockPosition.getX() + (double) packet.getClickPosition().getX();
        double clickPositionFullY = (double) packetBlockPosition.getY() + (double) packet.getClickPosition().getY();
        double clickPositionFullZ = (double) packetBlockPosition.getZ() + (double) packet.getClickPosition().getZ();

        Vector3f blockCenter = Vector3f.from(packetBlockPosition.getX() + 0.5f, packetBlockPosition.getY() + 0.5f, packetBlockPosition.getZ() + 0.5f);

        double clickDistanceX = clickPositionFullX - blockCenter.getX();
        double clickDistanceY = clickPositionFullY - blockCenter.getY();
        double clickDistanceZ = clickPositionFullZ - blockCenter.getZ();
        if (!(Math.abs(clickDistanceX) < 1.0000001D && Math.abs(clickDistanceY) < 1.0000001D && Math.abs(clickDistanceZ) < 1.0000001D)) {
            restoreCorrectBlock(session, blockPos);
            return;
        }

        /*
            Block place checks end - client is good to go
         */

        // todo yeet?
//        if (packet.getItemInHand() != null && session.getItemMappings().getMapping(packet.getItemInHand()).getJavaItem() instanceof SpawnEggItem) {
//            BlockState blockState = session.getGeyser().getWorldManager().blockAt(session, packet.getBlockPosition());
//            if (blockState.is(Blocks.WATER) && blockState.getValue(Properties.LEVEL) == 0) {
//                // Otherwise causes multiple mobs to spawn - just send a use item packet
//                //useItem(session, packet, blockState.javaId());
//                //break;
//            }
//        }

        for (Hand hand : Hand.values()) {
            InteractionContext context = InteractionContext.of(session, packet.getBlockPosition(), packet.getClickPosition(),
                packet.getBlockFace(), hand);
            InteractionResult result = simulateUseItemOn(session, context);

            // Storing the block position allows inconsistencies in block place checking from post-1.19 - pre-1.20.5 to be resolved.
            session.getWorldCache().markPositionInSequence(blockPos);

            ServerboundUseItemOnPacket blockPacket = new ServerboundUseItemOnPacket(
                packet.getBlockPosition(),
                Direction.VALUES[packet.getBlockFace()],
                hand,
                packet.getClickPosition().getX(), packet.getClickPosition().getY(), packet.getClickPosition().getZ(),
                false,
                false,
                session.getWorldCache().nextPredictionSequence());
            session.sendDownstreamGamePacket(blockPacket);

            if (result.success()) {
                if (result.shouldSwing()) {
                    if (hand == Hand.OFF_HAND) {
                        AnimateEntityPacket offHandPacket = new AnimateEntityPacket();
                        offHandPacket.setAnimation("animation.player.attack.rotations.offhand");
                        offHandPacket.setNextState("default");
                        offHandPacket.setBlendOutTime(0.0f);
                        offHandPacket.setStopExpression("query.any_animation_finished");
                        offHandPacket.setController("__runtime_controller");
                        offHandPacket.getRuntimeEntityIds().add(session.getPlayerEntity().getGeyserId());
                        session.sendUpstreamPacket(offHandPacket);
                    } else {
                        AnimatePacket animatePacket = new AnimatePacket();
                        animatePacket.setRuntimeEntityId(session.getPlayerEntity().getGeyserId());
                        animatePacket.setAction(AnimatePacket.Action.SWING_ARM);
                        session.sendUpstreamPacket(animatePacket);
                        session.activateArmAnimationTicking();
                    }
                    session.sendDownstreamPacket(new ServerboundSwingPacket(hand));
                }
                return;
            }

            if (result == InteractionResult.FAIL) {
                return;
            }

            if (useItem(context)) {
                return; // Success, don't try the other hand
            }
        }
    }

    private static InteractionResult simulateUseItemOn(GeyserSession session, InteractionContext context) {
        if (session.getGameMode().equals(GameMode.SPECTATOR)) {
            return InteractionResult.CONSUME; // just like java client
        } else {
            boolean eitherHandNotEmpty = !context.mainHand().isEmpty() || !context.offHand().isEmpty();
            boolean something = session.isSneaking() && eitherHandNotEmpty;
            if (!something) {
                InteractionResult result = context.state().block().interactWithItem(context);

                if (result.consumesAction()) {
                    return result;
                }

                if (result == InteractionResult.TRY_EMPTY_HAND && context.isMainHand()) {
                    result = context.state().block().interact(context);
                    if (result.consumesAction()) {
                        return result;
                    }
                }
            }

            GeyserItemStack itemInHand = context.itemInHand();

            if (!itemInHand.isEmpty() && !session.getWorldCache().hasCooldown(itemInHand)) {
                return itemInHand.asItem().attemptUseOn(context);
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    public static boolean useItem(InteractionContext context) {
        GeyserItemStack item = context.itemInHand();
        if (!item.isEmpty() && !context.isSpectator()) {
            // TODO proper values in packet; re-implement bucket hack
            ServerboundUseItemPacket serverboundUseItemPacket = new ServerboundUseItemPacket(context.hand(), 0, 0, 0);
            InteractionResult result;

            if (false) {
                // TODO implement Cooldown check; that is a pass
                result = InteractionResult.PASS;
            } else {
                result = item.asItem().use(context);
            }

            if (result.success()) {
                // TODO held item transformation??
            }

//                ItemDefinition definition = packet.getItemInHand().getDefinition();
//                int blockState = session.getGeyser().getWorldManager().getBlockAt(session, packet.getBlockPosition());
//                // Otherwise boats will not be able to be placed in survival and buckets, lily pads, frogspawn, and glass bottles won't work on mobile
//                if (item instanceof BoatItem || item == Items.LILY_PAD || item == Items.FROGSPAWN) {
//                    //useItem(session, packet, blockState);
//                } else if (item == Items.GLASS_BOTTLE) {
//                    // TODO
//                  if (!session.isSneaking() && BlockStateValues.isCauldron(blockState) && !BlockStateValues.isNonWaterCauldron(blockState)) {
//                      // ServerboundUseItemPacket is not sent for water cauldrons and glass bottles
//                      return;
//                  }
//                    //useItem(session, packet, blockState);
//                } else if (session.getItemMappings().getBuckets().contains(definition)) {
//                    // Don't send ServerboundUseItemPacket for powder snow buckets
//                    if (definition != session.getItemMappings().getStoredItems().powderSnowBucket().getBedrockDefinition()) {
//                        // TODO
//                     if (!session.isSneaking() && BlockStateValues.isCauldron(blockState)) {
//                         // ServerboundUseItemPacket is not sent for cauldrons and buckets
//                         return;
//                     }
//                        //session.setPlacedBucket(//useItem(session, packet, blockState));
//                    } else {
//                        session.setPlacedBucket(true);
//                    }
//                }

            if (result.success()) {
                if (result.shouldSwing()) {
                    if (!context.isMainHand()) {
                        AnimateEntityPacket offHandPacket = new AnimateEntityPacket();
                        offHandPacket.setAnimation("animation.player.attack.rotations.offhand");
                        offHandPacket.setNextState("default");
                        offHandPacket.setBlendOutTime(0.0f);
                        offHandPacket.setStopExpression("query.any_animation_finished");
                        offHandPacket.setController("__runtime_controller");
                        offHandPacket.getRuntimeEntityIds().add(context.session().getPlayerEntity().getGeyserId());
                        context.session().sendUpstreamPacket(offHandPacket);
                    } else {
                        AnimatePacket animatePacket = new AnimatePacket();
                        animatePacket.setRuntimeEntityId(context.session().getPlayerEntity().getGeyserId());
                        animatePacket.setAction(AnimatePacket.Action.SWING_ARM);
                        context.session().sendUpstreamPacket(animatePacket);
                        context.session().activateArmAnimationTicking();
                    }
                    context.session().swing(context.hand());
                }

                return true;
            }

            // TODO move elsewhere
            if (item.asItem() instanceof BlockItem blockItem) {
                context.session().setLastBlockPlacePosition(Vector3i.FORWARD); // TODO move to correct ctx
                context.session().setLastBlockPlaced(blockItem);
            }
            //context.session().setInteracting(true);
        }

        return false;
    }

}
