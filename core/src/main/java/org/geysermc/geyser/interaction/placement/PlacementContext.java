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

package org.geysermc.geyser.interaction.placement;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.cloudburstmc.math.vector.Vector3d;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.interaction.BlockHit;
import org.geysermc.geyser.interaction.InteractionContext;
import org.geysermc.geyser.interaction.InteractionRegistry;
import org.geysermc.geyser.interaction.UseOnContext;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;

/**
 * Mirror of Java's {@code BlockPlaceContext}: where and how a block item would place its block.
 * <p>
 * The placement position is the clicked block if it can be replaced, otherwise the block next to the clicked face.
 */
public final class PlacementContext {
    private final InteractionContext player;
    private final Hand hand;
    private final GeyserItemStack stack;
    private final BlockHit hit;
    private final Vector3i relativePos;
    private boolean replaceClicked;

    private PlacementContext(InteractionContext player, Hand hand, GeyserItemStack stack, BlockHit hit) {
        this.player = player;
        this.hand = hand;
        this.stack = stack;
        this.hit = hit;
        this.relativePos = hit.adjacentPosition();
        // Like vanilla, the clicked block is asked whether it can be replaced while clickedPos() and
        // replacingClickedOnBlock() still refer to the clicked block
        this.replaceClicked = true;
        this.replaceClicked = canBeReplaced(player.blockAt(hit.position()), true);
    }

    public static @NonNull PlacementContext of(@NonNull UseOnContext context) {
        return new PlacementContext(context.player(), context.hand(), context.stack(), context.hit());
    }

    /**
     * Mirrors {@code BlockPlaceContext#at}: a context placing at the given position, as if its given face was clicked.
     */
    public static @NonNull PlacementContext at(@NonNull PlacementContext context, @NonNull Vector3i pos, @NonNull Direction face) {
        Vector3i step = face.getUnitVector();
        Vector3d location = Vector3d.from(pos.getX() + 0.5 + step.getX() * 0.5, pos.getY() + 0.5 + step.getY() * 0.5,
            pos.getZ() + 0.5 + step.getZ() * 0.5);
        return new PlacementContext(context.player, context.hand, context.stack, new BlockHit(pos, face, location, false));
    }

    /**
     * @param replacingClicked whether the clicked block itself is checked; see {@link #replacingClickedOnBlock()}
     */
    private boolean canBeReplaced(BlockState state, boolean replacingClicked) {
        return InteractionRegistry.blockCanBeReplaced(state.block()).canBeReplaced(this, state, replacingClicked);
    }

    public @NonNull InteractionContext player() {
        return player;
    }

    public @NonNull Hand hand() {
        return hand;
    }

    public @NonNull GeyserItemStack itemInHand() {
        return stack;
    }

    public @NonNull BlockHit hit() {
        return hit;
    }

    /**
     * @return where the block would be placed
     */
    public @NonNull Vector3i clickedPos() {
        return replaceClicked ? hit.position() : relativePos;
    }

    public @NonNull Direction clickedFace() {
        return hit.face();
    }

    public @NonNull Vector3d clickLocation() {
        return hit.location();
    }

    public boolean isInside() {
        return hit.inside();
    }

    /**
     * Mirrors {@code BlockPlaceContext#canPlace}.
     */
    public boolean canPlace() {
        return replaceClicked || canBeReplaced(player.blockAt(clickedPos()), false);
    }

    public boolean replacingClickedOnBlock() {
        return replaceClicked;
    }

    public boolean isSecondaryUseActive() {
        return player.isSecondaryUseActive();
    }

    /**
     * @return the Java yaw of the player
     */
    public float rotation() {
        return player.yaw();
    }

    /**
     * Mirrors {@code UseOnContext#getHorizontalDirection}: {@code Direction#fromYRot} of the player's yaw.
     */
    public @NonNull Direction horizontalDirection() {
        return Directions.fromYRot(player.yaw());
    }

    /**
     * Mirrors {@code BlockPlaceContext#getNearestLookingDirection}.
     */
    public @NonNull Direction nearestLookingDirection() {
        return Directions.orderedByNearest(player.yaw(), player.pitch())[0];
    }

    /**
     * Mirrors {@code BlockPlaceContext#getNearestLookingVerticalDirection}.
     */
    public @NonNull Direction nearestLookingVerticalDirection() {
        return player.pitch() < 0 ? Direction.UP : Direction.DOWN;
    }

    /**
     * Mirrors {@code BlockPlaceContext#getNearestLookingDirections}: the directions ordered by how directly the player
     * looks at them. When placing next to the clicked block, the direction towards the clicked block comes first.
     */
    public @NonNull Direction @NonNull [] nearestLookingDirections() {
        Direction[] directions = Directions.orderedByNearest(player.yaw(), player.pitch());
        if (replaceClicked) {
            return directions;
        }
        Direction towardsClicked = clickedFace().reversed();
        int index = 0;
        while (index < directions.length && directions[index] != towardsClicked) {
            index++;
        }
        if (index > 0) {
            System.arraycopy(directions, 0, directions, 1, index);
            directions[0] = towardsClicked;
        }
        return directions;
    }

    /**
     * @return the block state at the given position
     */
    public @NonNull BlockState blockAt(@NonNull Vector3i pos) {
        return player.blockAt(pos);
    }
}
