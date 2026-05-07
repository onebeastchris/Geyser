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

package org.geysermc.geyser.translator.protocol.java;

import org.cloudburstmc.protocol.bedrock.packet.UnlockedRecipesPacket;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.PacketTranslator;
import org.geysermc.geyser.translator.protocol.Translator;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundRecipeBookRemovePacket;

import java.util.ArrayList;
import java.util.List;

@Translator(packet = ClientboundRecipeBookRemovePacket.class)
public class JavaRecipeBookRemoveTranslator extends PacketTranslator<ClientboundRecipeBookRemovePacket> {

    @Override
    public void translate(GeyserSession session, ClientboundRecipeBookRemovePacket packet) {
        List<String> recipes = getBedrockRecipes(session, packet.getRecipes());
        if (recipes.isEmpty()) {
            // Sending an empty list here will crash the client as of 1.20.60
            return;
        }
        UnlockedRecipesPacket recipesPacket = new UnlockedRecipesPacket();
        recipesPacket.setAction(UnlockedRecipesPacket.ActionType.REMOVE_UNLOCKED);
        recipesPacket.getUnlockedRecipes().addAll(recipes);
        session.sendUpstreamPacket(recipesPacket);

        // testing 123
//        if (GameProtocol.is1_26_20orHigher(session.protocolVersion())) {
//            UnlockedRecipesPacket recipesPacket2 = new UnlockedRecipesPacket();
//            recipesPacket2.setAction(UnlockedRecipesPacket.ActionType.INITIALLY_UNLOCKED);
//            recipesPacket2.getUnlockedRecipes().addAll(List.of(
//                "minecraft:furnace_acacia_wood",
//                "minecraft:furnace_stripped_spruce_wood",
//                "minecraft:furnace_birch_wood",
//                "minecraft:furnace_log_jungle",
//                "minecraft:furnace_dark_oak_wood",
//                "minecraft:furnace_log2_acacia",
//                "minecraft:furnace_log2_dark_oak",
//                "minecraft:furnace_log_birch",
//                "minecraft:furnace_jungle_wood",
//                "minecraft:furnace_log_oak",
//                "minecraft:furnace_log_spruce",
//                "minecraft:furnace_oak_wood",
//                "minecraft:furnace_spruce_wood",
//                "minecraft:furnace_stripped_birch_wood",
//                "minecraft:furnace_stripped_acacia_wood",
//                "minecraft:furnace_stripped_dark_oak_wood",
//                "minecraft:furnace_stripped_jungle_wood",
//                "minecraft:furnace_stripped_oak_wood"//,
//                //"minecraft:WorkBench_recipeId_from_oak",
//                //"minecraft:WorkBench_recipeId"
//            ));
//            session.sendUpstreamPacket(recipesPacket2);
//        }
    }

    private List<String> getBedrockRecipes(GeyserSession session, int[] javaRecipeIds) {
        List<String> recipes = new ArrayList<>();
        for (int javaIdentifier : javaRecipeIds) {
            List<String> bedrockRecipes = session.getJavaToBedrockRecipeIds().get(javaIdentifier);
            // Some recipes are not (un)lockable on Bedrock edition, like furnace or stonecutter recipes.
            // So we don't store/send these.
            if (bedrockRecipes != null) {
                recipes.addAll(bedrockRecipes);
            }
        }
        return recipes;
    }
}
