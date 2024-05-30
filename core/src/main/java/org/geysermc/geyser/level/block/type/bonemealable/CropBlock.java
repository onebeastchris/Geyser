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

package org.geysermc.geyser.level.block.type.bonemealable;

import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.property.Property;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.util.InteractionContext;

public class CropBlock extends Block implements BoneMealableBlock {

    private final static int MAX_AGE = 7;
    private final Property<Integer> AGE;

    public CropBlock(String javaIdentifier, Builder builder) {
        super(javaIdentifier, builder);
        AGE = Properties.AGE_7;
    }

    public CropBlock(String javaIdentifier, Builder builder, Property<Integer> ageProperty) {
        super(javaIdentifier, builder);
        AGE = ageProperty;
    }

    @Override
    public boolean bonemealEffective(InteractionContext context) {
        return context.state().getValue(AGE) > this.getMaxAge();
    }

    int getMaxAge() {
        return MAX_AGE;
    }
}
