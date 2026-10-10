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

package org.geysermc.geyser.util;

/**
 * Mirror of Java Edition's interaction result. Java models this as a sealed interface; we only need to know
 * whether an action was consumed and who swings the arm, so a flat enum is enough.
 */
public enum InteractionResult {
    /**
     * The action succeeded and the client swings its arm.
     */
    SUCCESS(true, SwingSource.CLIENT),
    /**
     * The action succeeded and the server takes care of the arm swing.
     */
    SUCCESS_SERVER(true, SwingSource.SERVER),
    /**
     * The action succeeded without any arm swing.
     */
    CONSUME(true, SwingSource.NONE),
    /**
     * Nothing happened; try the next action.
     */
    PASS(false, SwingSource.NONE),
    /**
     * The action failed; do not try any other action for this click.
     */
    FAIL(false, SwingSource.NONE),
    /**
     * Returned by block item interactions to indicate that the empty-hand interaction of the block should be tried.
     */
    TRY_WITH_EMPTY_HAND(false, SwingSource.NONE);

    private final boolean consumesAction;
    private final SwingSource swingSource;

    InteractionResult(boolean consumesAction, SwingSource swingSource) {
        this.consumesAction = consumesAction;
        this.swingSource = swingSource;
    }

    public boolean consumesAction() {
        return consumesAction;
    }

    public SwingSource swingSource() {
        return swingSource;
    }

    /**
     * @return whether the client swings its arm for this result
     */
    public boolean shouldSwing() {
        return swingSource == SwingSource.CLIENT;
    }

    public enum SwingSource {
        NONE,
        CLIENT,
        SERVER
    }
}
