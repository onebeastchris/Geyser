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

import org.geysermc.geyser.interaction.InteractionRegistry;
import org.geysermc.geyser.level.block.property.Properties;

/**
 * Placement rules ({@code canSurvive}, {@code getStateForPlacement}, {@code canBeReplaced}) of vanilla block classes M to Z.
 * <p>
 * Classes whose {@code getStateForPlacement} only sets properties that neither decide survival nor change the collision
 * (waterlogging, rotation, connections without collision, power) are registered with the default state.
 */
public final class PlacementRulesMZ {

    public static void register(InteractionRegistry.Registrar registrar) {
        // Orientation, waterlogging, power, block entity mode or connections without collision only; also
        // MossyCarpetBlock (only adds sides, the base stays), PitcherCropBlock (only places the lower half, without
        // checking the space above), PotentSulfurBlock (TODO verify against 26.2: validBlockState is assumed to never
        // refuse the placement)
        registrar.sameAsDefault(InteractionRegistry.BLOCK_STATE_FOR_PLACEMENT, "MangroveRootsBlock", "MossyCarpetBlock",
            "NoteBlock", "PitcherCropBlock", "PotentSulfurBlock", "RedStoneWireBlock", "RedstoneLampBlock", "SculkSensorBlock",
            "SculkShriekerBlock", "SkullBlock", "SnowyBlock", "StandingSignBlock", "TestBlock", "TripWireBlock",
            "WaterloggedTransparentBlock");

        // Plants and stackable blocks
        registrar.blockCanSurvive("VegetationBlock", RulesMZPlants::vegetationCanSurvive);
        registrar.blockCanSurvive("MushroomBlock", RulesMZPlants::mushroomCanSurvive);

        registrar.blockCanSurvive("PitcherCropBlock", RulesMZPlants::pitcherCropCanSurvive);
        registrar.blockCanBeReplaced("PitcherCropBlock", (context, state, replacingClickedOnBlock) -> false);

        registrar.blockCanSurvive("MangrovePropaguleBlock", RulesMZPlants::mangrovePropaguleCanSurvive);
        registrar.blockStateForPlacement("MangrovePropaguleBlock", RulesMZPlants::mangrovePropaguleStateForPlacement);

        registrar.blockCanSurvive("SeaPickleBlock", RulesMZPlants::vegetationCanSurvive);
        registrar.blockStateForPlacement("SeaPickleBlock", (context, block) ->
            RulesMZPlants.stackStateForPlacement(context, block, Properties.PICKLES));
        registrar.blockCanBeReplaced("SeaPickleBlock", (context, state, replacingClickedOnBlock) ->
            RulesMZPlants.stackCanBeReplaced(context, state, replacingClickedOnBlock, Properties.PICKLES));

        registrar.blockStateForPlacement("TurtleEggBlock", (context, block) ->
            RulesMZPlants.stackStateForPlacement(context, block, Properties.EGGS));
        registrar.blockCanBeReplaced("TurtleEggBlock", (context, state, replacingClickedOnBlock) ->
            RulesMZPlants.stackCanBeReplaced(context, state, replacingClickedOnBlock, Properties.EGGS));

        registrar.blockCanSurvive("SmallDripleafBlock", RulesMZPlants::smallDripleafCanSurvive);
        registrar.blockStateForPlacement("SmallDripleafBlock", RulesMZPlants::smallDripleafStateForPlacement);

        registrar.blockStateForPlacement("SeagrassBlock", RulesMZPlants::seagrassStateForPlacement);
        registrar.blockCanSurvive("TallSeagrassBlock", RulesMZPlants::tallSeagrassCanSurvive);
        registrar.blockStateForPlacement("TallSeagrassBlock", RulesMZPlants::tallSeagrassStateForPlacement);

        registrar.blockCanSurvive("SugarCaneBlock", RulesMZPlants::sugarCaneCanSurvive);
        registrar.blockCanSurvive("SporeBlossomBlock", RulesMZPlants::sporeBlossomCanSurvive);

        registrar.blockCanSurvive("MossyCarpetBlock", RulesMZPlants::mossyCarpetCanSurvive);

        // Attached blocks
        registrar.blockCanSurvive("WallTorchBlock", RulesMZAttached::wallTorchCanSurvive);
        registrar.blockStateForPlacement("WallTorchBlock", RulesMZAttached::wallStateForPlacement);
        registrar.blockCanSurvive("RedstoneWallTorchBlock", RulesMZAttached::wallTorchCanSurvive);
        registrar.blockStateForPlacement("RedstoneWallTorchBlock", RulesMZAttached::redstoneWallTorchStateForPlacement);

        registrar.blockCanSurvive("WallSignBlock", RulesMZAttached::solidBehindCanSurvive);
        registrar.blockStateForPlacement("WallSignBlock", RulesMZAttached::wallStateForPlacement);
        registrar.blockCanSurvive("StandingSignBlock", RulesMZAttached::standingSignCanSurvive);
        registrar.blockStateForPlacement("WallHangingSignBlock", RulesMZAttached::wallHangingSignStateForPlacement);

        registrar.blockCanSurvive("WallBannerBlock", RulesMZAttached::solidBehindCanSurvive);
        registrar.blockStateForPlacement("WallBannerBlock", RulesMZAttached::wallStateForPlacement);

        registrar.blockStateForPlacement("WallSkullBlock", RulesMZAttached::wallSkullStateForPlacement);

        registrar.blockCanSurvive("TripWireHookBlock", RulesMZAttached::tripWireHookCanSurvive);
        registrar.blockStateForPlacement("TripWireHookBlock", RulesMZAttached::wallStateForPlacement);

        registrar.blockCanSurvive("VineBlock", RulesMZAttached::vineCanSurvive);
        registrar.blockStateForPlacement("VineBlock", RulesMZAttached::vineStateForPlacement);
        registrar.blockCanBeReplaced("VineBlock", RulesMZAttached::vineCanBeReplaced);

        registrar.blockCanSurvive("MultifaceBlock", RulesMZAttached::multifaceCanSurvive);
        registrar.blockStateForPlacement("MultifaceBlock", RulesMZAttached::multifaceStateForPlacement);
        registrar.blockCanBeReplaced("MultifaceBlock", RulesMZAttached::multifaceCanBeReplaced);

        registrar.blockCanSurvive("PistonHeadBlock", RulesMZAttached::pistonHeadCanSurvive);

        // Shapes
        registrar.blockStateForPlacement("SlabBlock", RulesMZShapes::slabStateForPlacement);
        registrar.blockCanBeReplaced("SlabBlock", RulesMZShapes::slabCanBeReplaced);
        registrar.blockStateForPlacement("StairBlock", RulesMZShapes::stairStateForPlacement);
        registrar.blockStateForPlacement("WallBlock", RulesMZShapes::wallStateForPlacement);
        registrar.blockStateForPlacement("TrapDoorBlock", RulesMZShapes::trapDoorStateForPlacement);

        registrar.blockCanSurvive("SnowLayerBlock", RulesMZShapes::snowLayerCanSurvive);
        registrar.blockStateForPlacement("SnowLayerBlock", RulesMZShapes::snowLayerStateForPlacement);
        registrar.blockCanBeReplaced("SnowLayerBlock", RulesMZShapes::snowLayerCanBeReplaced);

        registrar.blockCanSurvive("ScaffoldingBlock", RulesMZShapes::scaffoldingCanSurvive);
        registrar.blockStateForPlacement("ScaffoldingBlock", RulesMZShapes::scaffoldingStateForPlacement);
        registrar.blockCanBeReplaced("ScaffoldingBlock", RulesMZShapes::scaffoldingCanBeReplaced);

        registrar.blockCanSurvive("SpeleothemBlock", RulesMZShapes::speleothemCanSurvive);
        registrar.blockStateForPlacement("SpeleothemBlock", RulesMZShapes::speleothemStateForPlacement);

        registrar.blockCanSurvive("RedStoneWireBlock", RulesMZShapes::redstoneWireCanSurvive);
        registrar.blockCanSurvive("SoulFireBlock", RulesMZShapes::soulFireCanSurvive);

        registrar.blockStateForPlacement("PistonBaseBlock", RulesMZShapes::pistonStateForPlacement);
        registrar.blockStateForPlacement("ObserverBlock", RulesMZShapes::observerStateForPlacement);
        registrar.blockStateForPlacement("ShulkerBoxBlock", RulesMZShapes::shulkerBoxStateForPlacement);
        registrar.blockStateForPlacement("RotatedPillarBlock", RulesMZShapes::rotatedPillarStateForPlacement);
        registrar.blockStateForPlacement("RepeaterBlock", RulesMZShapes::facingAwayHorizontally);
        registrar.blockStateForPlacement("ShelfBlock", RulesMZShapes::facingAwayHorizontally);
        registrar.blockStateForPlacement("StonecutterBlock", RulesMZShapes::facingAwayHorizontally);
        registrar.blockStateForPlacement("VaultBlock", RulesMZShapes::facingAwayHorizontally);
    }

    private PlacementRulesMZ() {
    }
}
