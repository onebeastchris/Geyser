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

/**
 * The small amount of block entity state the interaction simulation needs and which is not part of the block state.
 * Only blocks whose client-side interaction depends on block entity data are tracked, and only the fields that
 * matter are kept.
 * <p>
 * Currently only signs are tracked, and only once they are waxed: the click commands of a sign only matter when it
 * is waxed ({@code SignBlockEntity#canExecuteClickCommands}).
 *
 * @param waxed for signs: whether the sign has been waxed
 * @param frontClickCommand for waxed signs: whether a line of the front text has a {@code run_command} click event
 * @param backClickCommand for waxed signs: whether a line of the back text has a {@code run_command} click event
 */
public record BlockEntityInteractionData(boolean waxed, boolean frontClickCommand, boolean backClickCommand) {
    public static final BlockEntityInteractionData NONE = new BlockEntityInteractionData(false, false, false);

    /**
     * @return the data for a sign, or {@link #NONE} if nothing of it needs to be tracked
     */
    public static BlockEntityInteractionData sign(boolean waxed, boolean frontClickCommand, boolean backClickCommand) {
        return waxed ? new BlockEntityInteractionData(true, frontClickCommand, backClickCommand) : NONE;
    }
}
