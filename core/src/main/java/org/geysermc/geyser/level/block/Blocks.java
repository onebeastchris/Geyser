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

package org.geysermc.geyser.level.block;

import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.level.block.property.ChestType;
import org.geysermc.geyser.level.block.property.FrontAndTop;
import org.geysermc.geyser.level.block.type.BannerBlock;
import org.geysermc.geyser.level.block.type.BedBlock;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.level.block.type.ChestBlock;
import org.geysermc.geyser.level.block.type.FurnaceBlock;
import org.geysermc.geyser.level.block.type.IceBlock;
import org.geysermc.geyser.level.block.type.MovingPistonBlock;
import org.geysermc.geyser.level.block.type.PistonBlock;
import org.geysermc.geyser.level.block.type.PistonHeadBlock;
import org.geysermc.geyser.level.block.type.SkullBlock;
import org.geysermc.geyser.level.block.type.WallSkullBlock;
import org.geysermc.geyser.level.block.type.WaterBlock;
import org.geysermc.geyser.level.block.type.bonemealable.AlwaysBonemealableBlock;
import org.geysermc.geyser.level.block.type.bonemealable.AzaleaBlock;
import org.geysermc.geyser.level.block.type.bonemealable.BambooBlock;
import org.geysermc.geyser.level.block.type.bonemealable.BambooSaplingBlock;
import org.geysermc.geyser.level.block.type.bonemealable.BigDripleafBlock;
import org.geysermc.geyser.level.block.type.bonemealable.BigDripleafStemBlock;
import org.geysermc.geyser.level.block.type.bonemealable.CocoaBlock;
import org.geysermc.geyser.level.block.type.bonemealable.FungusBlock;
import org.geysermc.geyser.level.block.type.bonemealable.GlowLichenBlock;
import org.geysermc.geyser.level.block.type.bonemealable.GrassBlock;
import org.geysermc.geyser.level.block.type.bonemealable.HangingMossBlock;
import org.geysermc.geyser.level.block.type.bonemealable.MangroveLeavesBlock;
import org.geysermc.geyser.level.block.type.bonemealable.MangrovePropaguleBlock;
import org.geysermc.geyser.level.block.type.bonemealable.MossBlock;
import org.geysermc.geyser.level.block.type.bonemealable.MossyCarpetBlock;
import org.geysermc.geyser.level.block.type.bonemealable.MushroomBlock;
import org.geysermc.geyser.level.block.type.bonemealable.NetherrackBlock;
import org.geysermc.geyser.level.block.type.bonemealable.NyliumBlock;
import org.geysermc.geyser.level.block.type.bonemealable.PinkPetalsBlock;
import org.geysermc.geyser.level.block.type.bonemealable.PitcherCropBlock;
import org.geysermc.geyser.level.block.type.bonemealable.RootedDirtBlock;
import org.geysermc.geyser.level.block.type.bonemealable.SeaPickleBlock;
import org.geysermc.geyser.level.block.type.bonemealable.SeagrassBlock;
import org.geysermc.geyser.level.block.type.bonemealable.SmallDripleafBlock;
import org.geysermc.geyser.level.block.type.bonemealable.StemBlock;
import org.geysermc.geyser.level.block.type.bonemealable.SweetBerryBushBlock;
import org.geysermc.geyser.level.block.type.bonemealable.TallFlowerBlock;
import org.geysermc.geyser.level.block.type.bonemealable.TallGrassBlock;
import org.geysermc.geyser.level.block.type.bonemealable.crop.BeetrootBlock;
import org.geysermc.geyser.level.block.type.bonemealable.crop.CropBlock;
import org.geysermc.geyser.level.block.type.bonemealable.crop.TorchflowerCropBlock;
import org.geysermc.geyser.level.block.type.bonemealable.growingheadblocks.CaveVinesBlock;
import org.geysermc.geyser.level.block.type.bonemealable.growingheadblocks.CaveVinesPlantBlock;
import org.geysermc.geyser.level.block.type.bonemealable.growingheadblocks.KelpBlock;
import org.geysermc.geyser.level.block.type.bonemealable.growingheadblocks.KelpPlantBlock;
import org.geysermc.geyser.level.block.type.bonemealable.growingheadblocks.TwistingVinesBlock;
import org.geysermc.geyser.level.block.type.bonemealable.growingheadblocks.TwistingVinesPlantBlock;
import org.geysermc.geyser.level.block.type.bonemealable.growingheadblocks.WeepingVinesBlock;
import org.geysermc.geyser.level.block.type.bonemealable.growingheadblocks.WeepingVinesPlantBlock;
import org.geysermc.geyser.level.block.type.bush.BushBlock;
import org.geysermc.geyser.level.block.type.cauldrons.CauldronBlock;
import org.geysermc.geyser.level.block.type.cauldrons.LavaCauldronBlock;
import org.geysermc.geyser.level.block.type.cauldrons.PowderedSnowCauldronBlock;
import org.geysermc.geyser.level.block.type.cauldrons.WaterCauldronBlock;
import org.geysermc.geyser.level.block.type.interactive.BeehiveBlock;
import org.geysermc.geyser.level.block.type.interactive.BellBlock;
import org.geysermc.geyser.level.block.type.interactive.ButtonBlock;
import org.geysermc.geyser.level.block.type.interactive.CakeBlock;
import org.geysermc.geyser.level.block.type.interactive.CampfireBlock;
import org.geysermc.geyser.level.block.type.interactive.CandleBlock;
import org.geysermc.geyser.level.block.type.interactive.CandleCakeBlock;
import org.geysermc.geyser.level.block.type.interactive.CeilingHangingSignBlock;
import org.geysermc.geyser.level.block.type.interactive.ChiseledBookShelfBlock;
import org.geysermc.geyser.level.block.type.interactive.CommandBlock;
import org.geysermc.geyser.level.block.type.interactive.ComparatorBlock;
import org.geysermc.geyser.level.block.type.interactive.ComposterBlock;
import org.geysermc.geyser.level.block.type.interactive.DecoratedPotBlock;
import org.geysermc.geyser.level.block.type.interactive.DoorBlock;
import org.geysermc.geyser.level.block.type.interactive.FenceGateBlock;
import org.geysermc.geyser.level.block.type.interactive.FlowerPotBlock;
import org.geysermc.geyser.level.block.type.interactive.JigsawBlock;
import org.geysermc.geyser.level.block.type.interactive.JukeboxBlock;
import org.geysermc.geyser.level.block.type.interactive.LecternBlock;
import org.geysermc.geyser.level.block.type.interactive.LeverBlock;
import org.geysermc.geyser.level.block.type.interactive.NoteBlock;
import org.geysermc.geyser.level.block.type.interactive.PumpkinBlock;
import org.geysermc.geyser.level.block.type.interactive.RedStoneOreBlock;
import org.geysermc.geyser.level.block.type.interactive.RedStoneWireBlock;
import org.geysermc.geyser.level.block.type.interactive.RepeaterBlock;
import org.geysermc.geyser.level.block.type.interactive.RespawnAnchorBlock;
import org.geysermc.geyser.level.block.type.interactive.SignBlock;
import org.geysermc.geyser.level.block.type.interactive.StructureBlock;
import org.geysermc.geyser.level.block.type.interactive.TntBlock;
import org.geysermc.geyser.level.block.type.interactive.TrapDoorBlock;
import org.geysermc.geyser.level.block.type.interactive.VaultBlock;
import org.geysermc.geyser.level.block.type.interactive.WallHangingSignBlock;
import org.geysermc.geyser.level.physics.Axis;
import org.geysermc.geyser.level.physics.Direction;
import org.geysermc.geyser.level.physics.PistonBehavior;
import org.geysermc.geyser.registry.BlockRegistries;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityType;

import static org.geysermc.geyser.level.block.property.Properties.*;
import static org.geysermc.geyser.level.block.type.Block.builder;

@SuppressWarnings("unused")
public final class Blocks {
    public static final Block AIR = register(new Block("air", builder().replaceable().notSolid()));
    public static final Block STONE = register(new Block("stone", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block GRANITE = register(new Block("granite", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block POLISHED_GRANITE = register(new Block("polished_granite", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block DIORITE = register(new Block("diorite", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block POLISHED_DIORITE = register(new Block("polished_diorite", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block ANDESITE = register(new Block("andesite", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block POLISHED_ANDESITE = register(new Block("polished_andesite", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final GrassBlock GRASS_BLOCK = register(new GrassBlock("grass_block", builder().destroyTime(0.6f).solidRender()
        .booleanState(SNOWY)));
    public static final Block DIRT = register(new Block("dirt", builder().destroyTime(0.5f).solidRender()));
    public static final Block COARSE_DIRT = register(new Block("coarse_dirt", builder().destroyTime(0.5f).solidRender()));
    public static final Block PODZOL = register(new Block("podzol", builder().destroyTime(0.5f).solidRender()
        .booleanState(SNOWY)));
    public static final Block COBBLESTONE = register(new Block("cobblestone", builder().requiresCorrectToolForDrops().destroyTime(2.0f).solidRender()));
    public static final Block OAK_PLANKS = register(new Block("oak_planks", builder().destroyTime(2.0f).solidRender()));
    public static final Block SPRUCE_PLANKS = register(new Block("spruce_planks", builder().destroyTime(2.0f).solidRender()));
    public static final Block BIRCH_PLANKS = register(new Block("birch_planks", builder().destroyTime(2.0f).solidRender()));
    public static final Block JUNGLE_PLANKS = register(new Block("jungle_planks", builder().destroyTime(2.0f).solidRender()));
    public static final Block ACACIA_PLANKS = register(new Block("acacia_planks", builder().destroyTime(2.0f).solidRender()));
    public static final Block CHERRY_PLANKS = register(new Block("cherry_planks", builder().destroyTime(2.0f).solidRender()));
    public static final Block DARK_OAK_PLANKS = register(new Block("dark_oak_planks", builder().destroyTime(2.0f).solidRender()));
    public static final Block PALE_OAK_WOOD = register(new Block("pale_oak_wood", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block PALE_OAK_PLANKS = register(new Block("pale_oak_planks", builder().destroyTime(2.0f).solidRender()));
    public static final Block MANGROVE_PLANKS = register(new Block("mangrove_planks", builder().destroyTime(2.0f).solidRender()));
    public static final Block BAMBOO_PLANKS = register(new Block("bamboo_planks", builder().destroyTime(2.0f).solidRender()));
    public static final Block BAMBOO_MOSAIC = register(new Block("bamboo_mosaic", builder().destroyTime(2.0f).solidRender()));
    public static final AlwaysBonemealableBlock OAK_SAPLING = register(new AlwaysBonemealableBlock("oak_sapling", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(STAGE)));
    public static final AlwaysBonemealableBlock SPRUCE_SAPLING = register(new AlwaysBonemealableBlock("spruce_sapling", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(STAGE)));
    public static final AlwaysBonemealableBlock BIRCH_SAPLING = register(new AlwaysBonemealableBlock("birch_sapling", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(STAGE)));
    public static final AlwaysBonemealableBlock JUNGLE_SAPLING = register(new AlwaysBonemealableBlock("jungle_sapling", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(STAGE)));
    public static final AlwaysBonemealableBlock ACACIA_SAPLING = register(new AlwaysBonemealableBlock("acacia_sapling", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(STAGE)));
    public static final AlwaysBonemealableBlock CHERRY_SAPLING = register(new AlwaysBonemealableBlock("cherry_sapling", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(STAGE)));
    public static final AlwaysBonemealableBlock DARK_OAK_SAPLING = register(new AlwaysBonemealableBlock("dark_oak_sapling", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(STAGE)));
    public static final AlwaysBonemealableBlock PALE_OAK_SAPLING = register(new AlwaysBonemealableBlock("pale_oak_sapling", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(STAGE)));
    public static final MangrovePropaguleBlock MANGROVE_PROPAGULE = register(new MangrovePropaguleBlock("mangrove_propagule", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(AGE_4)
        .booleanState(HANGING)
        .intState(STAGE)
        .booleanState(WATERLOGGED)));
    public static final Block BEDROCK = register(new Block("bedrock", builder().destroyTime(-1.0f).solidRender()));
    public static final WaterBlock WATER = register(new WaterBlock("water", builder().destroyTime(100.0f).pushReaction(PistonBehavior.DESTROY).replaceable().notSolid()
        .intState(LEVEL)));
    public static final Block LAVA = register(new Block("lava", builder().destroyTime(100.0f).pushReaction(PistonBehavior.DESTROY).replaceable().notSolid()
        .intState(LEVEL)));
    public static final Block SAND = register(new Block("sand", builder().destroyTime(0.5f).solidRender()));
    public static final Block SUSPICIOUS_SAND = register(new Block("suspicious_sand", builder().setBlockEntity(BlockEntityType.BRUSHABLE_BLOCK).destroyTime(0.25f).pushReaction(PistonBehavior.DESTROY).solidRender()
        .intState(DUSTED)));
    public static final Block RED_SAND = register(new Block("red_sand", builder().destroyTime(0.5f).solidRender()));
    public static final Block GRAVEL = register(new Block("gravel", builder().destroyTime(0.6f).solidRender()));
    public static final Block SUSPICIOUS_GRAVEL = register(new Block("suspicious_gravel", builder().setBlockEntity(BlockEntityType.BRUSHABLE_BLOCK).destroyTime(0.25f).pushReaction(PistonBehavior.DESTROY).solidRender()
        .intState(DUSTED)));
    public static final Block GOLD_ORE = register(new Block("gold_ore", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block DEEPSLATE_GOLD_ORE = register(new Block("deepslate_gold_ore", builder().requiresCorrectToolForDrops().destroyTime(4.5f).solidRender()));
    public static final Block IRON_ORE = register(new Block("iron_ore", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block DEEPSLATE_IRON_ORE = register(new Block("deepslate_iron_ore", builder().requiresCorrectToolForDrops().destroyTime(4.5f).solidRender()));
    public static final Block COAL_ORE = register(new Block("coal_ore", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block DEEPSLATE_COAL_ORE = register(new Block("deepslate_coal_ore", builder().requiresCorrectToolForDrops().destroyTime(4.5f).solidRender()));
    public static final Block NETHER_GOLD_ORE = register(new Block("nether_gold_ore", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block OAK_LOG = register(new Block("oak_log", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block SPRUCE_LOG = register(new Block("spruce_log", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block BIRCH_LOG = register(new Block("birch_log", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block JUNGLE_LOG = register(new Block("jungle_log", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block ACACIA_LOG = register(new Block("acacia_log", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block CHERRY_LOG = register(new Block("cherry_log", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block DARK_OAK_LOG = register(new Block("dark_oak_log", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block PALE_OAK_LOG = register(new Block("pale_oak_log", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block MANGROVE_LOG = register(new Block("mangrove_log", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block MANGROVE_ROOTS = register(new Block("mangrove_roots", builder().destroyTime(0.7f)
        .booleanState(WATERLOGGED)));
    public static final Block MUDDY_MANGROVE_ROOTS = register(new Block("muddy_mangrove_roots", builder().destroyTime(0.7f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block BAMBOO_BLOCK = register(new Block("bamboo_block", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block STRIPPED_SPRUCE_LOG = register(new Block("stripped_spruce_log", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block STRIPPED_BIRCH_LOG = register(new Block("stripped_birch_log", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block STRIPPED_JUNGLE_LOG = register(new Block("stripped_jungle_log", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block STRIPPED_ACACIA_LOG = register(new Block("stripped_acacia_log", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block STRIPPED_CHERRY_LOG = register(new Block("stripped_cherry_log", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block STRIPPED_DARK_OAK_LOG = register(new Block("stripped_dark_oak_log", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block STRIPPED_PALE_OAK_LOG = register(new Block("stripped_pale_oak_log", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block STRIPPED_OAK_LOG = register(new Block("stripped_oak_log", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block STRIPPED_MANGROVE_LOG = register(new Block("stripped_mangrove_log", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block STRIPPED_BAMBOO_BLOCK = register(new Block("stripped_bamboo_block", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block OAK_WOOD = register(new Block("oak_wood", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block SPRUCE_WOOD = register(new Block("spruce_wood", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block BIRCH_WOOD = register(new Block("birch_wood", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block JUNGLE_WOOD = register(new Block("jungle_wood", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block ACACIA_WOOD = register(new Block("acacia_wood", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block CHERRY_WOOD = register(new Block("cherry_wood", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block DARK_OAK_WOOD = register(new Block("dark_oak_wood", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block MANGROVE_WOOD = register(new Block("mangrove_wood", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block STRIPPED_OAK_WOOD = register(new Block("stripped_oak_wood", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block STRIPPED_SPRUCE_WOOD = register(new Block("stripped_spruce_wood", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block STRIPPED_BIRCH_WOOD = register(new Block("stripped_birch_wood", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block STRIPPED_JUNGLE_WOOD = register(new Block("stripped_jungle_wood", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block STRIPPED_ACACIA_WOOD = register(new Block("stripped_acacia_wood", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block STRIPPED_CHERRY_WOOD = register(new Block("stripped_cherry_wood", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block STRIPPED_DARK_OAK_WOOD = register(new Block("stripped_dark_oak_wood", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block STRIPPED_PALE_OAK_WOOD = register(new Block("stripped_pale_oak_wood", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block STRIPPED_MANGROVE_WOOD = register(new Block("stripped_mangrove_wood", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block OAK_LEAVES = register(new Block("oak_leaves", builder().destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .intState(DISTANCE)
        .booleanState(PERSISTENT)
        .booleanState(WATERLOGGED)));
    public static final Block SPRUCE_LEAVES = register(new Block("spruce_leaves", builder().destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .intState(DISTANCE)
        .booleanState(PERSISTENT)
        .booleanState(WATERLOGGED)));
    public static final Block BIRCH_LEAVES = register(new Block("birch_leaves", builder().destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .intState(DISTANCE)
        .booleanState(PERSISTENT)
        .booleanState(WATERLOGGED)));
    public static final Block JUNGLE_LEAVES = register(new Block("jungle_leaves", builder().destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .intState(DISTANCE)
        .booleanState(PERSISTENT)
        .booleanState(WATERLOGGED)));
    public static final Block ACACIA_LEAVES = register(new Block("acacia_leaves", builder().destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .intState(DISTANCE)
        .booleanState(PERSISTENT)
        .booleanState(WATERLOGGED)));
    public static final Block CHERRY_LEAVES = register(new Block("cherry_leaves", builder().destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .intState(DISTANCE)
        .booleanState(PERSISTENT)
        .booleanState(WATERLOGGED)));
    public static final Block DARK_OAK_LEAVES = register(new Block("dark_oak_leaves", builder().destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .intState(DISTANCE)
        .booleanState(PERSISTENT)
        .booleanState(WATERLOGGED)));
    public static final Block PALE_OAK_LEAVES = register(new Block("pale_oak_leaves", builder().destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .intState(DISTANCE)
        .booleanState(PERSISTENT)
        .booleanState(WATERLOGGED)));
    public static final MangroveLeavesBlock MANGROVE_LEAVES = register(new MangroveLeavesBlock("mangrove_leaves", builder().destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .intState(DISTANCE)
        .booleanState(PERSISTENT)
        .booleanState(WATERLOGGED)));
    public static final Block AZALEA_LEAVES = register(new Block("azalea_leaves", builder().destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .intState(DISTANCE)
        .booleanState(PERSISTENT)
        .booleanState(WATERLOGGED)));
    public static final Block FLOWERING_AZALEA_LEAVES = register(new Block("flowering_azalea_leaves", builder().destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .intState(DISTANCE)
        .booleanState(PERSISTENT)
        .booleanState(WATERLOGGED)));
    public static final Block SPONGE = register(new Block("sponge", builder().destroyTime(0.6f).solidRender()));
    public static final Block WET_SPONGE = register(new Block("wet_sponge", builder().destroyTime(0.6f).solidRender()));
    public static final Block GLASS = register(new Block("glass", builder().destroyTime(0.3f)));
    public static final Block LAPIS_ORE = register(new Block("lapis_ore", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block DEEPSLATE_LAPIS_ORE = register(new Block("deepslate_lapis_ore", builder().requiresCorrectToolForDrops().destroyTime(4.5f).solidRender()));
    public static final Block LAPIS_BLOCK = register(new Block("lapis_block", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block DISPENSER = register(new Block("dispenser", builder().setBlockEntity(BlockEntityType.DISPENSER).requiresCorrectToolForDrops().destroyTime(3.5f).solidRender()
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)
        .booleanState(TRIGGERED)));
    public static final Block SANDSTONE = register(new Block("sandstone", builder().requiresCorrectToolForDrops().destroyTime(0.8f).solidRender()));
    public static final Block CHISELED_SANDSTONE = register(new Block("chiseled_sandstone", builder().requiresCorrectToolForDrops().destroyTime(0.8f).solidRender()));
    public static final Block CUT_SANDSTONE = register(new Block("cut_sandstone", builder().requiresCorrectToolForDrops().destroyTime(0.8f).solidRender()));
    public static final NoteBlock NOTE_BLOCK = register(new NoteBlock("note_block", builder().destroyTime(0.8f).solidRender()
        .enumState(NOTEBLOCK_INSTRUMENT)
        .intState(NOTE)
        .booleanState(POWERED)));
    public static final BedBlock WHITE_BED = register(new BedBlock("white_bed", 0, builder().setBlockEntity(BlockEntityType.BED).destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(OCCUPIED)
        .enumState(BED_PART)));
    public static final BedBlock ORANGE_BED = register(new BedBlock("orange_bed", 1, builder().setBlockEntity(BlockEntityType.BED).destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(OCCUPIED)
        .enumState(BED_PART)));
    public static final BedBlock MAGENTA_BED = register(new BedBlock("magenta_bed", 2, builder().setBlockEntity(BlockEntityType.BED).destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(OCCUPIED)
        .enumState(BED_PART)));
    public static final BedBlock LIGHT_BLUE_BED = register(new BedBlock("light_blue_bed", 3, builder().setBlockEntity(BlockEntityType.BED).destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(OCCUPIED)
        .enumState(BED_PART)));
    public static final BedBlock YELLOW_BED = register(new BedBlock("yellow_bed", 4, builder().setBlockEntity(BlockEntityType.BED).destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(OCCUPIED)
        .enumState(BED_PART)));
    public static final BedBlock LIME_BED = register(new BedBlock("lime_bed", 5, builder().setBlockEntity(BlockEntityType.BED).destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(OCCUPIED)
        .enumState(BED_PART)));
    public static final BedBlock PINK_BED = register(new BedBlock("pink_bed", 6, builder().setBlockEntity(BlockEntityType.BED).destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(OCCUPIED)
        .enumState(BED_PART)));
    public static final BedBlock GRAY_BED = register(new BedBlock("gray_bed", 7, builder().setBlockEntity(BlockEntityType.BED).destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(OCCUPIED)
        .enumState(BED_PART)));
    public static final BedBlock LIGHT_GRAY_BED = register(new BedBlock("light_gray_bed", 8, builder().setBlockEntity(BlockEntityType.BED).destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(OCCUPIED)
        .enumState(BED_PART)));
    public static final BedBlock CYAN_BED = register(new BedBlock("cyan_bed", 9, builder().setBlockEntity(BlockEntityType.BED).destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(OCCUPIED)
        .enumState(BED_PART)));
    public static final BedBlock PURPLE_BED = register(new BedBlock("purple_bed", 10, builder().setBlockEntity(BlockEntityType.BED).destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(OCCUPIED)
        .enumState(BED_PART)));
    public static final BedBlock BLUE_BED = register(new BedBlock("blue_bed", 11, builder().setBlockEntity(BlockEntityType.BED).destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(OCCUPIED)
        .enumState(BED_PART)));
    public static final BedBlock BROWN_BED = register(new BedBlock("brown_bed", 12, builder().setBlockEntity(BlockEntityType.BED).destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(OCCUPIED)
        .enumState(BED_PART)));
    public static final BedBlock GREEN_BED = register(new BedBlock("green_bed", 13, builder().setBlockEntity(BlockEntityType.BED).destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(OCCUPIED)
        .enumState(BED_PART)));
    public static final BedBlock RED_BED = register(new BedBlock("red_bed", 14, builder().setBlockEntity(BlockEntityType.BED).destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(OCCUPIED)
        .enumState(BED_PART)));
    public static final BedBlock BLACK_BED = register(new BedBlock("black_bed", 15, builder().setBlockEntity(BlockEntityType.BED).destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(OCCUPIED)
        .enumState(BED_PART)));
    public static final Block POWERED_RAIL = register(new Block("powered_rail", builder().destroyTime(0.7f).notSolid()
        .booleanState(POWERED)
        .enumState(RAIL_SHAPE_STRAIGHT)
        .booleanState(WATERLOGGED)));
    public static final Block DETECTOR_RAIL = register(new Block("detector_rail", builder().destroyTime(0.7f).notSolid()
        .booleanState(POWERED)
        .enumState(RAIL_SHAPE_STRAIGHT)
        .booleanState(WATERLOGGED)));
    public static final PistonBlock STICKY_PISTON = register(new PistonBlock("sticky_piston", builder().destroyTime(1.5f).pushReaction(PistonBehavior.BLOCK).solidRender()
        .booleanState(EXTENDED)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)));
    public static final Block COBWEB = register(new Block("cobweb", builder().requiresCorrectToolForDrops().destroyTime(4.0f).pushReaction(PistonBehavior.DESTROY)));
    public static final TallGrassBlock SHORT_GRASS = register(new TallGrassBlock("short_grass", builder().pushReaction(PistonBehavior.DESTROY).replaceable().notSolid()));
    public static final TallGrassBlock FERN = register(new TallGrassBlock("fern", builder().pushReaction(PistonBehavior.DESTROY).replaceable().notSolid()));
    public static final BushBlock DEAD_BUSH = register(new BushBlock("dead_bush", builder().pushReaction(PistonBehavior.DESTROY).replaceable().notSolid()));
    public static final SeagrassBlock SEAGRASS = register(new SeagrassBlock("seagrass", builder().pushReaction(PistonBehavior.DESTROY).replaceable().notSolid()));
    public static final BushBlock TALL_SEAGRASS = register(new BushBlock("tall_seagrass", builder().pushReaction(PistonBehavior.DESTROY).replaceable().notSolid()
        .enumState(DOUBLE_BLOCK_HALF)));
    public static final PistonBlock PISTON = register(new PistonBlock("piston", builder().destroyTime(1.5f).pushReaction(PistonBehavior.BLOCK).solidRender()
        .booleanState(EXTENDED)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)));
    public static final PistonHeadBlock PISTON_HEAD = register(new PistonHeadBlock("piston_head", builder().destroyTime(1.5f).pushReaction(PistonBehavior.BLOCK)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)
        .booleanState(SHORT)
        .enumState(PISTON_TYPE)));
    public static final Block WHITE_WOOL = register(new Block("white_wool", builder().destroyTime(0.8f).solidRender()));
    public static final Block ORANGE_WOOL = register(new Block("orange_wool", builder().destroyTime(0.8f).solidRender()));
    public static final Block MAGENTA_WOOL = register(new Block("magenta_wool", builder().destroyTime(0.8f).solidRender()));
    public static final Block LIGHT_BLUE_WOOL = register(new Block("light_blue_wool", builder().destroyTime(0.8f).solidRender()));
    public static final Block YELLOW_WOOL = register(new Block("yellow_wool", builder().destroyTime(0.8f).solidRender()));
    public static final Block LIME_WOOL = register(new Block("lime_wool", builder().destroyTime(0.8f).solidRender()));
    public static final Block PINK_WOOL = register(new Block("pink_wool", builder().destroyTime(0.8f).solidRender()));
    public static final Block GRAY_WOOL = register(new Block("gray_wool", builder().destroyTime(0.8f).solidRender()));
    public static final Block LIGHT_GRAY_WOOL = register(new Block("light_gray_wool", builder().destroyTime(0.8f).solidRender()));
    public static final Block CYAN_WOOL = register(new Block("cyan_wool", builder().destroyTime(0.8f).solidRender()));
    public static final Block PURPLE_WOOL = register(new Block("purple_wool", builder().destroyTime(0.8f).solidRender()));
    public static final Block BLUE_WOOL = register(new Block("blue_wool", builder().destroyTime(0.8f).solidRender()));
    public static final Block BROWN_WOOL = register(new Block("brown_wool", builder().destroyTime(0.8f).solidRender()));
    public static final Block GREEN_WOOL = register(new Block("green_wool", builder().destroyTime(0.8f).solidRender()));
    public static final Block RED_WOOL = register(new Block("red_wool", builder().destroyTime(0.8f).solidRender()));
    public static final Block BLACK_WOOL = register(new Block("black_wool", builder().destroyTime(0.8f).solidRender()));
    public static final MovingPistonBlock MOVING_PISTON = register(new MovingPistonBlock("moving_piston", builder().setBlockEntity(BlockEntityType.PISTON).destroyTime(-1.0f).pushReaction(PistonBehavior.BLOCK)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)
        .enumState(PISTON_TYPE)));
    public static final BushBlock DANDELION = register(new BushBlock("dandelion", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final BushBlock TORCHFLOWER = register(new BushBlock("torchflower", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final BushBlock POPPY = register(new BushBlock("poppy", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final BushBlock BLUE_ORCHID = register(new BushBlock("blue_orchid", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final BushBlock ALLIUM = register(new BushBlock("allium", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final BushBlock AZURE_BLUET = register(new BushBlock("azure_bluet", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final BushBlock RED_TULIP = register(new BushBlock("red_tulip", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final BushBlock ORANGE_TULIP = register(new BushBlock("orange_tulip", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final BushBlock WHITE_TULIP = register(new BushBlock("white_tulip", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final BushBlock PINK_TULIP = register(new BushBlock("pink_tulip", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final BushBlock OXEYE_DAISY = register(new BushBlock("oxeye_daisy", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final BushBlock CORNFLOWER = register(new BushBlock("cornflower", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final BushBlock WITHER_ROSE = register(new BushBlock("wither_rose", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final BushBlock LILY_OF_THE_VALLEY = register(new BushBlock("lily_of_the_valley", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final MushroomBlock BROWN_MUSHROOM = register(new MushroomBlock("brown_mushroom", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final MushroomBlock RED_MUSHROOM = register(new MushroomBlock("red_mushroom", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final Block GOLD_BLOCK = register(new Block("gold_block", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block IRON_BLOCK = register(new Block("iron_block", builder().requiresCorrectToolForDrops().destroyTime(5.0f).solidRender()));
    public static final Block BRICKS = register(new Block("bricks", builder().requiresCorrectToolForDrops().destroyTime(2.0f).solidRender()));
    public static final TntBlock TNT = register(new TntBlock("tnt", builder().solidRender()
        .booleanState(UNSTABLE)));
    public static final Block BOOKSHELF = register(new Block("bookshelf", builder().destroyTime(1.5f).solidRender()));
    public static final ChiseledBookShelfBlock CHISELED_BOOKSHELF = register(new ChiseledBookShelfBlock("chiseled_bookshelf", builder().setBlockEntity(BlockEntityType.CHISELED_BOOKSHELF).destroyTime(1.5f).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(CHISELED_BOOKSHELF_SLOT_0_OCCUPIED)
        .booleanState(CHISELED_BOOKSHELF_SLOT_1_OCCUPIED)
        .booleanState(CHISELED_BOOKSHELF_SLOT_2_OCCUPIED)
        .booleanState(CHISELED_BOOKSHELF_SLOT_3_OCCUPIED)
        .booleanState(CHISELED_BOOKSHELF_SLOT_4_OCCUPIED)
        .booleanState(CHISELED_BOOKSHELF_SLOT_5_OCCUPIED)));
    public static final Block MOSSY_COBBLESTONE = register(new Block("mossy_cobblestone", builder().requiresCorrectToolForDrops().destroyTime(2.0f).solidRender()));
    public static final Block OBSIDIAN = register(new Block("obsidian", builder().requiresCorrectToolForDrops().destroyTime(50.0f).solidRender()));
    public static final Block TORCH = register(new Block("torch", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final Block WALL_TORCH = register(new Block("wall_torch", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block FIRE = register(new Block("fire", builder().pushReaction(PistonBehavior.DESTROY).replaceable().notSolid()
        .intState(AGE_15)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(UP)
        .booleanState(WEST)));
    public static final Block SOUL_FIRE = register(new Block("soul_fire", builder().pushReaction(PistonBehavior.DESTROY).replaceable().notSolid()));
    public static final Block SPAWNER = register(new Block("spawner", builder().setBlockEntity(BlockEntityType.MOB_SPAWNER).requiresCorrectToolForDrops().destroyTime(5.0f)));
    public static final Block CREAKING_HEART = register(new Block("creaking_heart", builder().setBlockEntity(BlockEntityType.CREAKING_HEART).destroyTime(10.0f).solidRender()
        .booleanState(ACTIVE)
        .enumState(AXIS, Axis.VALUES)
        .booleanState(NATURAL)));
    public static final Block OAK_STAIRS = register(new Block("oak_stairs", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final ChestBlock CHEST = register(new ChestBlock("chest", builder().setBlockEntity(BlockEntityType.CHEST).destroyTime(2.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(CHEST_TYPE, ChestType.VALUES)
        .booleanState(WATERLOGGED)));
    public static final RedStoneWireBlock REDSTONE_WIRE = register(new RedStoneWireBlock("redstone_wire", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(EAST_REDSTONE)
        .enumState(NORTH_REDSTONE)
        .intState(POWER)
        .enumState(SOUTH_REDSTONE)
        .enumState(WEST_REDSTONE)));
    public static final Block DIAMOND_ORE = register(new Block("diamond_ore", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block DEEPSLATE_DIAMOND_ORE = register(new Block("deepslate_diamond_ore", builder().requiresCorrectToolForDrops().destroyTime(4.5f).solidRender()));
    public static final Block DIAMOND_BLOCK = register(new Block("diamond_block", builder().requiresCorrectToolForDrops().destroyTime(5.0f).solidRender()));
    public static final Block CRAFTING_TABLE = register(new Block("crafting_table", builder().destroyTime(2.5f).solidRender()));
    public static final CropBlock WHEAT = register(new CropBlock("wheat", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(AGE_7)));
    public static final Block FARMLAND = register(new Block("farmland", builder().destroyTime(0.6f)
        .intState(MOISTURE)));
    public static final FurnaceBlock FURNACE = register(new FurnaceBlock("furnace", builder().setBlockEntity(BlockEntityType.FURNACE).requiresCorrectToolForDrops().destroyTime(3.5f).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(LIT)));
    public static final SignBlock OAK_SIGN = register(new SignBlock("oak_sign", builder().setBlockEntity(BlockEntityType.SIGN).destroyTime(1.0f)
        .intState(ROTATION_16)
        .booleanState(WATERLOGGED)));
    public static final Block SPRUCE_SIGN = register(new Block("spruce_sign", builder().setBlockEntity(BlockEntityType.SIGN).destroyTime(1.0f)
        .intState(ROTATION_16)
        .booleanState(WATERLOGGED)));
    public static final Block BIRCH_SIGN = register(new Block("birch_sign", builder().setBlockEntity(BlockEntityType.SIGN).destroyTime(1.0f)
        .intState(ROTATION_16)
        .booleanState(WATERLOGGED)));
    public static final Block ACACIA_SIGN = register(new Block("acacia_sign", builder().setBlockEntity(BlockEntityType.SIGN).destroyTime(1.0f)
        .intState(ROTATION_16)
        .booleanState(WATERLOGGED)));
    public static final Block CHERRY_SIGN = register(new Block("cherry_sign", builder().setBlockEntity(BlockEntityType.SIGN).destroyTime(1.0f)
        .intState(ROTATION_16)
        .booleanState(WATERLOGGED)));
    public static final Block JUNGLE_SIGN = register(new Block("jungle_sign", builder().setBlockEntity(BlockEntityType.SIGN).destroyTime(1.0f)
        .intState(ROTATION_16)
        .booleanState(WATERLOGGED)));
    public static final Block DARK_OAK_SIGN = register(new Block("dark_oak_sign", builder().setBlockEntity(BlockEntityType.SIGN).destroyTime(1.0f)
        .intState(ROTATION_16)
        .booleanState(WATERLOGGED)));
    public static final Block PALE_OAK_SIGN = register(new Block("pale_oak_sign", builder().setBlockEntity(BlockEntityType.SIGN).destroyTime(1.0f)
        .intState(ROTATION_16)
        .booleanState(WATERLOGGED)));
    public static final Block MANGROVE_SIGN = register(new Block("mangrove_sign", builder().setBlockEntity(BlockEntityType.SIGN).destroyTime(1.0f)
        .intState(ROTATION_16)
        .booleanState(WATERLOGGED)));
    public static final Block BAMBOO_SIGN = register(new Block("bamboo_sign", builder().setBlockEntity(BlockEntityType.SIGN).destroyTime(1.0f)
        .intState(ROTATION_16)
        .booleanState(WATERLOGGED)));
    public static final DoorBlock OAK_DOOR = register(new DoorBlock("oak_door", builder().destroyTime(3.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(DOUBLE_BLOCK_HALF)
        .enumState(DOOR_HINGE)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final Block LADDER = register(new Block("ladder", builder().destroyTime(0.4f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block RAIL = register(new Block("rail", builder().destroyTime(0.7f).notSolid()
        .enumState(RAIL_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block COBBLESTONE_STAIRS = register(new Block("cobblestone_stairs", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block OAK_WALL_SIGN = register(new Block("oak_wall_sign", builder().setBlockEntity(BlockEntityType.SIGN).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block SPRUCE_WALL_SIGN = register(new Block("spruce_wall_sign", builder().setBlockEntity(BlockEntityType.SIGN).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block BIRCH_WALL_SIGN = register(new Block("birch_wall_sign", builder().setBlockEntity(BlockEntityType.SIGN).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block ACACIA_WALL_SIGN = register(new Block("acacia_wall_sign", builder().setBlockEntity(BlockEntityType.SIGN).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block CHERRY_WALL_SIGN = register(new Block("cherry_wall_sign", builder().setBlockEntity(BlockEntityType.SIGN).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block JUNGLE_WALL_SIGN = register(new Block("jungle_wall_sign", builder().setBlockEntity(BlockEntityType.SIGN).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block DARK_OAK_WALL_SIGN = register(new Block("dark_oak_wall_sign", builder().setBlockEntity(BlockEntityType.SIGN).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block PALE_OAK_WALL_SIGN = register(new Block("pale_oak_wall_sign", builder().setBlockEntity(BlockEntityType.SIGN).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block MANGROVE_WALL_SIGN = register(new Block("mangrove_wall_sign", builder().setBlockEntity(BlockEntityType.SIGN).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block BAMBOO_WALL_SIGN = register(new Block("bamboo_wall_sign", builder().setBlockEntity(BlockEntityType.SIGN).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final CeilingHangingSignBlock OAK_HANGING_SIGN = register(new CeilingHangingSignBlock("oak_hanging_sign", builder().setBlockEntity(BlockEntityType.HANGING_SIGN).destroyTime(1.0f)
        .booleanState(ATTACHED)
        .intState(ROTATION_16)
        .booleanState(WATERLOGGED)));
    public static final Block SPRUCE_HANGING_SIGN = register(new Block("spruce_hanging_sign", builder().setBlockEntity(BlockEntityType.HANGING_SIGN).destroyTime(1.0f)
        .booleanState(ATTACHED)
        .intState(ROTATION_16)
        .booleanState(WATERLOGGED)));
    public static final Block BIRCH_HANGING_SIGN = register(new Block("birch_hanging_sign", builder().setBlockEntity(BlockEntityType.HANGING_SIGN).destroyTime(1.0f)
        .booleanState(ATTACHED)
        .intState(ROTATION_16)
        .booleanState(WATERLOGGED)));
    public static final Block ACACIA_HANGING_SIGN = register(new Block("acacia_hanging_sign", builder().setBlockEntity(BlockEntityType.HANGING_SIGN).destroyTime(1.0f)
        .booleanState(ATTACHED)
        .intState(ROTATION_16)
        .booleanState(WATERLOGGED)));
    public static final Block CHERRY_HANGING_SIGN = register(new Block("cherry_hanging_sign", builder().setBlockEntity(BlockEntityType.HANGING_SIGN).destroyTime(1.0f)
        .booleanState(ATTACHED)
        .intState(ROTATION_16)
        .booleanState(WATERLOGGED)));
    public static final Block JUNGLE_HANGING_SIGN = register(new Block("jungle_hanging_sign", builder().setBlockEntity(BlockEntityType.HANGING_SIGN).destroyTime(1.0f)
        .booleanState(ATTACHED)
        .intState(ROTATION_16)
        .booleanState(WATERLOGGED)));
    public static final Block DARK_OAK_HANGING_SIGN = register(new Block("dark_oak_hanging_sign", builder().setBlockEntity(BlockEntityType.HANGING_SIGN).destroyTime(1.0f)
        .booleanState(ATTACHED)
        .intState(ROTATION_16)
        .booleanState(WATERLOGGED)));
    public static final Block PALE_OAK_HANGING_SIGN = register(new Block("pale_oak_hanging_sign", builder().setBlockEntity(BlockEntityType.HANGING_SIGN).destroyTime(1.0f)
        .booleanState(ATTACHED)
        .intState(ROTATION_16)
        .booleanState(WATERLOGGED)));
    public static final Block CRIMSON_HANGING_SIGN = register(new Block("crimson_hanging_sign", builder().setBlockEntity(BlockEntityType.HANGING_SIGN).destroyTime(1.0f)
        .booleanState(ATTACHED)
        .intState(ROTATION_16)
        .booleanState(WATERLOGGED)));
    public static final Block WARPED_HANGING_SIGN = register(new Block("warped_hanging_sign", builder().setBlockEntity(BlockEntityType.HANGING_SIGN).destroyTime(1.0f)
        .booleanState(ATTACHED)
        .intState(ROTATION_16)
        .booleanState(WATERLOGGED)));
    public static final Block MANGROVE_HANGING_SIGN = register(new Block("mangrove_hanging_sign", builder().setBlockEntity(BlockEntityType.HANGING_SIGN).destroyTime(1.0f)
        .booleanState(ATTACHED)
        .intState(ROTATION_16)
        .booleanState(WATERLOGGED)));
    public static final Block BAMBOO_HANGING_SIGN = register(new Block("bamboo_hanging_sign", builder().setBlockEntity(BlockEntityType.HANGING_SIGN).destroyTime(1.0f)
        .booleanState(ATTACHED)
        .intState(ROTATION_16)
        .booleanState(WATERLOGGED)));
    public static final WallHangingSignBlock OAK_WALL_HANGING_SIGN = register(new WallHangingSignBlock("oak_wall_hanging_sign", builder().setBlockEntity(BlockEntityType.HANGING_SIGN).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block SPRUCE_WALL_HANGING_SIGN = register(new Block("spruce_wall_hanging_sign", builder().setBlockEntity(BlockEntityType.HANGING_SIGN).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block BIRCH_WALL_HANGING_SIGN = register(new Block("birch_wall_hanging_sign", builder().setBlockEntity(BlockEntityType.HANGING_SIGN).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block ACACIA_WALL_HANGING_SIGN = register(new Block("acacia_wall_hanging_sign", builder().setBlockEntity(BlockEntityType.HANGING_SIGN).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block CHERRY_WALL_HANGING_SIGN = register(new Block("cherry_wall_hanging_sign", builder().setBlockEntity(BlockEntityType.HANGING_SIGN).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block JUNGLE_WALL_HANGING_SIGN = register(new Block("jungle_wall_hanging_sign", builder().setBlockEntity(BlockEntityType.HANGING_SIGN).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block DARK_OAK_WALL_HANGING_SIGN = register(new Block("dark_oak_wall_hanging_sign", builder().setBlockEntity(BlockEntityType.HANGING_SIGN).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block PALE_OAK_WALL_HANGING_SIGN = register(new Block("pale_oak_wall_hanging_sign", builder().setBlockEntity(BlockEntityType.HANGING_SIGN).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block MANGROVE_WALL_HANGING_SIGN = register(new Block("mangrove_wall_hanging_sign", builder().setBlockEntity(BlockEntityType.HANGING_SIGN).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block CRIMSON_WALL_HANGING_SIGN = register(new Block("crimson_wall_hanging_sign", builder().setBlockEntity(BlockEntityType.HANGING_SIGN).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block WARPED_WALL_HANGING_SIGN = register(new Block("warped_wall_hanging_sign", builder().setBlockEntity(BlockEntityType.HANGING_SIGN).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block BAMBOO_WALL_HANGING_SIGN = register(new Block("bamboo_wall_hanging_sign", builder().setBlockEntity(BlockEntityType.HANGING_SIGN).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final LeverBlock LEVER = register(new LeverBlock("lever", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(ATTACH_FACE)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(POWERED)));
    public static final Block STONE_PRESSURE_PLATE = register(new Block("stone_pressure_plate", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY)
        .booleanState(POWERED)));
    public static final DoorBlock IRON_DOOR = register(new DoorBlock("iron_door", builder().destroyTime(5.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(DOUBLE_BLOCK_HALF)
        .enumState(DOOR_HINGE)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final Block OAK_PRESSURE_PLATE = register(new Block("oak_pressure_plate", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY)
        .booleanState(POWERED)));
    public static final Block SPRUCE_PRESSURE_PLATE = register(new Block("spruce_pressure_plate", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY)
        .booleanState(POWERED)));
    public static final Block BIRCH_PRESSURE_PLATE = register(new Block("birch_pressure_plate", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY)
        .booleanState(POWERED)));
    public static final Block JUNGLE_PRESSURE_PLATE = register(new Block("jungle_pressure_plate", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY)
        .booleanState(POWERED)));
    public static final Block ACACIA_PRESSURE_PLATE = register(new Block("acacia_pressure_plate", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY)
        .booleanState(POWERED)));
    public static final Block CHERRY_PRESSURE_PLATE = register(new Block("cherry_pressure_plate", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY)
        .booleanState(POWERED)));
    public static final Block DARK_OAK_PRESSURE_PLATE = register(new Block("dark_oak_pressure_plate", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY)
        .booleanState(POWERED)));
    public static final Block PALE_OAK_PRESSURE_PLATE = register(new Block("pale_oak_pressure_plate", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY)
        .booleanState(POWERED)));
    public static final Block MANGROVE_PRESSURE_PLATE = register(new Block("mangrove_pressure_plate", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY)
        .booleanState(POWERED)));
    public static final Block BAMBOO_PRESSURE_PLATE = register(new Block("bamboo_pressure_plate", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY)
        .booleanState(POWERED)));
    public static final RedStoneOreBlock REDSTONE_ORE = register(new RedStoneOreBlock("redstone_ore", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()
        .booleanState(LIT)));
    public static final Block DEEPSLATE_REDSTONE_ORE = register(new Block("deepslate_redstone_ore", builder().requiresCorrectToolForDrops().destroyTime(4.5f).solidRender()
        .booleanState(LIT)));
    public static final Block REDSTONE_TORCH = register(new Block("redstone_torch", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .booleanState(LIT)));
    public static final Block REDSTONE_WALL_TORCH = register(new Block("redstone_wall_torch", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(LIT)));
    public static final ButtonBlock STONE_BUTTON = register(new ButtonBlock("stone_button", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(ATTACH_FACE)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(POWERED)));
    public static final Block SNOW = register(new Block("snow", builder().requiresCorrectToolForDrops().destroyTime(0.1f).pushReaction(PistonBehavior.DESTROY).replaceable().notSolid()
        .intState(LAYERS)));
    public static final IceBlock ICE = register(new IceBlock("ice", builder().destroyTime(0.5f)));
    public static final Block SNOW_BLOCK = register(new Block("snow_block", builder().requiresCorrectToolForDrops().destroyTime(0.2f).solidRender()));
    public static final Block CACTUS = register(new Block("cactus", builder().destroyTime(0.4f).pushReaction(PistonBehavior.DESTROY)
        .intState(AGE_15)));
    public static final Block CLAY = register(new Block("clay", builder().destroyTime(0.6f).solidRender()));
    public static final Block SUGAR_CANE = register(new Block("sugar_cane", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(AGE_15)));
    public static final JukeboxBlock JUKEBOX = register(new JukeboxBlock("jukebox", builder().setBlockEntity(BlockEntityType.JUKEBOX).destroyTime(2.0f).solidRender()
        .booleanState(HAS_RECORD)));
    public static final Block OAK_FENCE = register(new Block("oak_fence", builder().destroyTime(2.0f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final NetherrackBlock NETHERRACK = register(new NetherrackBlock("netherrack", builder().requiresCorrectToolForDrops().destroyTime(0.4f).solidRender()));
    public static final Block SOUL_SAND = register(new Block("soul_sand", builder().destroyTime(0.5f).solidRender()));
    public static final Block SOUL_SOIL = register(new Block("soul_soil", builder().destroyTime(0.5f).solidRender()));
    public static final Block BASALT = register(new Block("basalt", builder().requiresCorrectToolForDrops().destroyTime(1.25f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block POLISHED_BASALT = register(new Block("polished_basalt", builder().requiresCorrectToolForDrops().destroyTime(1.25f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block SOUL_TORCH = register(new Block("soul_torch", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final Block SOUL_WALL_TORCH = register(new Block("soul_wall_torch", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block GLOWSTONE = register(new Block("glowstone", builder().destroyTime(0.3f).solidRender()));
    public static final Block NETHER_PORTAL = register(new Block("nether_portal", builder().destroyTime(-1.0f).pushReaction(PistonBehavior.BLOCK).notSolid()
        .enumState(HORIZONTAL_AXIS, Axis.X, Axis.Z)));
    public static final Block CARVED_PUMPKIN = register(new Block("carved_pumpkin", builder().destroyTime(1.0f).pushReaction(PistonBehavior.DESTROY).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block JACK_O_LANTERN = register(new Block("jack_o_lantern", builder().destroyTime(1.0f).pushReaction(PistonBehavior.DESTROY).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final CakeBlock CAKE = register(new CakeBlock("cake", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY)
        .intState(BITES)));
    public static final RepeaterBlock REPEATER = register(new RepeaterBlock("repeater", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(DELAY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(LOCKED)
        .booleanState(POWERED)));
    public static final Block WHITE_STAINED_GLASS = register(new Block("white_stained_glass", builder().destroyTime(0.3f)));
    public static final Block ORANGE_STAINED_GLASS = register(new Block("orange_stained_glass", builder().destroyTime(0.3f)));
    public static final Block MAGENTA_STAINED_GLASS = register(new Block("magenta_stained_glass", builder().destroyTime(0.3f)));
    public static final Block LIGHT_BLUE_STAINED_GLASS = register(new Block("light_blue_stained_glass", builder().destroyTime(0.3f)));
    public static final Block YELLOW_STAINED_GLASS = register(new Block("yellow_stained_glass", builder().destroyTime(0.3f)));
    public static final Block LIME_STAINED_GLASS = register(new Block("lime_stained_glass", builder().destroyTime(0.3f)));
    public static final Block PINK_STAINED_GLASS = register(new Block("pink_stained_glass", builder().destroyTime(0.3f)));
    public static final Block GRAY_STAINED_GLASS = register(new Block("gray_stained_glass", builder().destroyTime(0.3f)));
    public static final Block LIGHT_GRAY_STAINED_GLASS = register(new Block("light_gray_stained_glass", builder().destroyTime(0.3f)));
    public static final Block CYAN_STAINED_GLASS = register(new Block("cyan_stained_glass", builder().destroyTime(0.3f)));
    public static final Block PURPLE_STAINED_GLASS = register(new Block("purple_stained_glass", builder().destroyTime(0.3f)));
    public static final Block BLUE_STAINED_GLASS = register(new Block("blue_stained_glass", builder().destroyTime(0.3f)));
    public static final Block BROWN_STAINED_GLASS = register(new Block("brown_stained_glass", builder().destroyTime(0.3f)));
    public static final Block GREEN_STAINED_GLASS = register(new Block("green_stained_glass", builder().destroyTime(0.3f)));
    public static final Block RED_STAINED_GLASS = register(new Block("red_stained_glass", builder().destroyTime(0.3f)));
    public static final Block BLACK_STAINED_GLASS = register(new Block("black_stained_glass", builder().destroyTime(0.3f)));
    public static final TrapDoorBlock OAK_TRAPDOOR = register(new TrapDoorBlock("oak_trapdoor", builder().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .booleanState(OPEN)
        .booleanState(POWERED)
        .booleanState(WATERLOGGED)));
    public static final TrapDoorBlock SPRUCE_TRAPDOOR = register(new TrapDoorBlock("spruce_trapdoor", builder().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .booleanState(OPEN)
        .booleanState(POWERED)
        .booleanState(WATERLOGGED)));
    public static final TrapDoorBlock BIRCH_TRAPDOOR = register(new TrapDoorBlock("birch_trapdoor", builder().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .booleanState(OPEN)
        .booleanState(POWERED)
        .booleanState(WATERLOGGED)));
    public static final TrapDoorBlock JUNGLE_TRAPDOOR = register(new TrapDoorBlock("jungle_trapdoor", builder().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .booleanState(OPEN)
        .booleanState(POWERED)
        .booleanState(WATERLOGGED)));
    public static final TrapDoorBlock ACACIA_TRAPDOOR = register(new TrapDoorBlock("acacia_trapdoor", builder().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .booleanState(OPEN)
        .booleanState(POWERED)
        .booleanState(WATERLOGGED)));
    public static final TrapDoorBlock CHERRY_TRAPDOOR = register(new TrapDoorBlock("cherry_trapdoor", builder().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .booleanState(OPEN)
        .booleanState(POWERED)
        .booleanState(WATERLOGGED)));
    public static final TrapDoorBlock DARK_OAK_TRAPDOOR = register(new TrapDoorBlock("dark_oak_trapdoor", builder().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .booleanState(OPEN)
        .booleanState(POWERED)
        .booleanState(WATERLOGGED)));
    public static final TrapDoorBlock PALE_OAK_TRAPDOOR = register(new TrapDoorBlock("pale_oak_trapdoor", builder().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .booleanState(OPEN)
        .booleanState(POWERED)
        .booleanState(WATERLOGGED)));
    public static final TrapDoorBlock MANGROVE_TRAPDOOR = register(new TrapDoorBlock("mangrove_trapdoor", builder().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .booleanState(OPEN)
        .booleanState(POWERED)
        .booleanState(WATERLOGGED)));
    public static final TrapDoorBlock BAMBOO_TRAPDOOR = register(new TrapDoorBlock("bamboo_trapdoor", builder().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .booleanState(OPEN)
        .booleanState(POWERED)
        .booleanState(WATERLOGGED)));
    public static final Block STONE_BRICKS = register(new Block("stone_bricks", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block MOSSY_STONE_BRICKS = register(new Block("mossy_stone_bricks", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block CRACKED_STONE_BRICKS = register(new Block("cracked_stone_bricks", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block CHISELED_STONE_BRICKS = register(new Block("chiseled_stone_bricks", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block PACKED_MUD = register(new Block("packed_mud", builder().destroyTime(1.0f).solidRender()));
    public static final Block MUD_BRICKS = register(new Block("mud_bricks", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block INFESTED_STONE = register(new Block("infested_stone", builder().destroyTime(0.75f).solidRender()));
    public static final Block INFESTED_COBBLESTONE = register(new Block("infested_cobblestone", builder().destroyTime(1.0f).solidRender()));
    public static final Block INFESTED_STONE_BRICKS = register(new Block("infested_stone_bricks", builder().destroyTime(0.75f).solidRender()));
    public static final Block INFESTED_MOSSY_STONE_BRICKS = register(new Block("infested_mossy_stone_bricks", builder().destroyTime(0.75f).solidRender()));
    public static final Block INFESTED_CRACKED_STONE_BRICKS = register(new Block("infested_cracked_stone_bricks", builder().destroyTime(0.75f).solidRender()));
    public static final Block INFESTED_CHISELED_STONE_BRICKS = register(new Block("infested_chiseled_stone_bricks", builder().destroyTime(0.75f).solidRender()));
    public static final Block BROWN_MUSHROOM_BLOCK = register(new Block("brown_mushroom_block", builder().destroyTime(0.2f).solidRender()
        .booleanState(DOWN)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(UP)
        .booleanState(WEST)));
    public static final Block RED_MUSHROOM_BLOCK = register(new Block("red_mushroom_block", builder().destroyTime(0.2f).solidRender()
        .booleanState(DOWN)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(UP)
        .booleanState(WEST)));
    public static final Block MUSHROOM_STEM = register(new Block("mushroom_stem", builder().destroyTime(0.2f).solidRender()
        .booleanState(DOWN)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(UP)
        .booleanState(WEST)));
    public static final Block IRON_BARS = register(new Block("iron_bars", builder().requiresCorrectToolForDrops().destroyTime(5.0f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block CHAIN = register(new Block("chain", builder().requiresCorrectToolForDrops().destroyTime(5.0f)
        .enumState(AXIS, Axis.VALUES)
        .booleanState(WATERLOGGED)));
    public static final Block GLASS_PANE = register(new Block("glass_pane", builder().destroyTime(0.3f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final PumpkinBlock PUMPKIN = register(new PumpkinBlock("pumpkin", builder().destroyTime(1.0f).pushReaction(PistonBehavior.DESTROY).solidRender()));
    public static final Block MELON = register(new Block("melon", builder().destroyTime(1.0f).pushReaction(PistonBehavior.DESTROY).solidRender()));
    public static final BushBlock ATTACHED_PUMPKIN_STEM = register(new BushBlock("attached_pumpkin_stem", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final BushBlock ATTACHED_MELON_STEM = register(new BushBlock("attached_melon_stem", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final StemBlock PUMPKIN_STEM = register(new StemBlock("pumpkin_stem", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(AGE_7)));
    public static final StemBlock MELON_STEM = register(new StemBlock("melon_stem", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(AGE_7)));
    public static final Block VINE = register(new Block("vine", builder().destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY).replaceable().notSolid()
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(UP)
        .booleanState(WEST)));
    public static final GlowLichenBlock GLOW_LICHEN = register(new GlowLichenBlock("glow_lichen", builder().destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY).replaceable().notSolid()
        .booleanState(DOWN)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block RESIN_CLUMP = register(new Block("resin_clump", builder().pushReaction(PistonBehavior.DESTROY).replaceable().notSolid()
        .booleanState(DOWN)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final FenceGateBlock OAK_FENCE_GATE = register(new FenceGateBlock("oak_fence_gate", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(IN_WALL)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final Block BRICK_STAIRS = register(new Block("brick_stairs", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block STONE_BRICK_STAIRS = register(new Block("stone_brick_stairs", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block MUD_BRICK_STAIRS = register(new Block("mud_brick_stairs", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block MYCELIUM = register(new Block("mycelium", builder().destroyTime(0.6f).solidRender()
        .booleanState(SNOWY)));
    public static final BushBlock LILY_PAD = register(new BushBlock("lily_pad", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final Block RESIN_BLOCK = register(new Block("resin_block", builder().solidRender()));
    public static final Block RESIN_BRICKS = register(new Block("resin_bricks", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block RESIN_BRICK_STAIRS = register(new Block("resin_brick_stairs", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block RESIN_BRICK_SLAB = register(new Block("resin_brick_slab", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block RESIN_BRICK_WALL = register(new Block("resin_brick_wall", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block CHISELED_RESIN_BRICKS = register(new Block("chiseled_resin_bricks", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block NETHER_BRICKS = register(new Block("nether_bricks", builder().requiresCorrectToolForDrops().destroyTime(2.0f).solidRender()));
    public static final Block NETHER_BRICK_FENCE = register(new Block("nether_brick_fence", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block NETHER_BRICK_STAIRS = register(new Block("nether_brick_stairs", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final BushBlock NETHER_WART = register(new BushBlock("nether_wart", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(AGE_3)));
    public static final Block ENCHANTING_TABLE = register(new Block("enchanting_table", builder().setBlockEntity(BlockEntityType.ENCHANTING_TABLE).requiresCorrectToolForDrops().destroyTime(5.0f)));
    public static final Block BREWING_STAND = register(new Block("brewing_stand", builder().setBlockEntity(BlockEntityType.BREWING_STAND).destroyTime(0.5f)
        .booleanState(HAS_BOTTLE_0)
        .booleanState(HAS_BOTTLE_1)
        .booleanState(HAS_BOTTLE_2)));
    public static final CauldronBlock CAULDRON = register(new CauldronBlock("cauldron", builder().requiresCorrectToolForDrops().destroyTime(2.0f)));
    public static final WaterCauldronBlock WATER_CAULDRON = register(new WaterCauldronBlock("water_cauldron", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .intState(LEVEL_CAULDRON)));
    public static final LavaCauldronBlock LAVA_CAULDRON = register(new LavaCauldronBlock("lava_cauldron", builder().requiresCorrectToolForDrops().destroyTime(2.0f)));
    public static final PowderedSnowCauldronBlock POWDER_SNOW_CAULDRON = register(new PowderedSnowCauldronBlock("powder_snow_cauldron", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .intState(LEVEL_CAULDRON)));
    public static final Block END_PORTAL = register(new Block("end_portal", builder().setBlockEntity(BlockEntityType.END_PORTAL).destroyTime(-1.0f).pushReaction(PistonBehavior.BLOCK).notSolid()));
    public static final Block END_PORTAL_FRAME = register(new Block("end_portal_frame", builder().destroyTime(-1.0f)
        .booleanState(EYE)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block END_STONE = register(new Block("end_stone", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block DRAGON_EGG = register(new Block("dragon_egg", builder().destroyTime(3.0f).pushReaction(PistonBehavior.DESTROY)));
    public static final Block REDSTONE_LAMP = register(new Block("redstone_lamp", builder().destroyTime(0.3f).solidRender()
        .booleanState(LIT)));
    public static final CocoaBlock COCOA = register(new CocoaBlock("cocoa", builder().destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(AGE_2)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block SANDSTONE_STAIRS = register(new Block("sandstone_stairs", builder().requiresCorrectToolForDrops().destroyTime(0.8f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block EMERALD_ORE = register(new Block("emerald_ore", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block DEEPSLATE_EMERALD_ORE = register(new Block("deepslate_emerald_ore", builder().requiresCorrectToolForDrops().destroyTime(4.5f).solidRender()));
    public static final Block ENDER_CHEST = register(new Block("ender_chest", builder().setBlockEntity(BlockEntityType.ENDER_CHEST).destroyTime(22.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block TRIPWIRE_HOOK = register(new Block("tripwire_hook", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .booleanState(ATTACHED)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(POWERED)));
    public static final Block TRIPWIRE = register(new Block("tripwire", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .booleanState(ATTACHED)
        .booleanState(DISARMED)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(POWERED)
        .booleanState(SOUTH)
        .booleanState(WEST)));
    public static final Block EMERALD_BLOCK = register(new Block("emerald_block", builder().requiresCorrectToolForDrops().destroyTime(5.0f).solidRender()));
    public static final Block SPRUCE_STAIRS = register(new Block("spruce_stairs", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block BIRCH_STAIRS = register(new Block("birch_stairs", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block JUNGLE_STAIRS = register(new Block("jungle_stairs", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final CommandBlock COMMAND_BLOCK = register(new CommandBlock("command_block", builder().setBlockEntity(BlockEntityType.COMMAND_BLOCK).requiresCorrectToolForDrops().destroyTime(-1.0f).solidRender()
        .booleanState(CONDITIONAL)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)));
    public static final Block BEACON = register(new Block("beacon", builder().setBlockEntity(BlockEntityType.BEACON).destroyTime(3.0f)));
    public static final Block COBBLESTONE_WALL = register(new Block("cobblestone_wall", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block MOSSY_COBBLESTONE_WALL = register(new Block("mossy_cobblestone_wall", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final FlowerPotBlock FLOWER_POT = register(new FlowerPotBlock("flower_pot", AIR, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_TORCHFLOWER = register(new FlowerPotBlock("potted_torchflower", TORCHFLOWER, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_OAK_SAPLING = register(new FlowerPotBlock("potted_oak_sapling", OAK_SAPLING, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_SPRUCE_SAPLING = register(new FlowerPotBlock("potted_spruce_sapling", SPRUCE_SAPLING, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_BIRCH_SAPLING = register(new FlowerPotBlock("potted_birch_sapling", BIRCH_SAPLING, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_JUNGLE_SAPLING = register(new FlowerPotBlock("potted_jungle_sapling", JUNGLE_SAPLING, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_ACACIA_SAPLING = register(new FlowerPotBlock("potted_acacia_sapling", ACACIA_SAPLING, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_CHERRY_SAPLING = register(new FlowerPotBlock("potted_cherry_sapling", CHERRY_SAPLING, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_DARK_OAK_SAPLING = register(new FlowerPotBlock("potted_dark_oak_sapling", DARK_OAK_SAPLING, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_PALE_OAK_SAPLING = register(new FlowerPotBlock("potted_pale_oak_sapling", PALE_OAK_SAPLING, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_MANGROVE_PROPAGULE = register(new FlowerPotBlock("potted_mangrove_propagule", MANGROVE_PROPAGULE, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_FERN = register(new FlowerPotBlock("potted_fern", FERN, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_DANDELION = register(new FlowerPotBlock("potted_dandelion", DANDELION, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_POPPY = register(new FlowerPotBlock("potted_poppy", POPPY, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_BLUE_ORCHID = register(new FlowerPotBlock("potted_blue_orchid", BLUE_ORCHID, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_ALLIUM = register(new FlowerPotBlock("potted_allium", ALLIUM, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_AZURE_BLUET = register(new FlowerPotBlock("potted_azure_bluet", AZURE_BLUET, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_RED_TULIP = register(new FlowerPotBlock("potted_red_tulip", RED_TULIP, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_ORANGE_TULIP = register(new FlowerPotBlock("potted_orange_tulip", ORANGE_TULIP, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_WHITE_TULIP = register(new FlowerPotBlock("potted_white_tulip", WHITE_TULIP, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_PINK_TULIP = register(new FlowerPotBlock("potted_pink_tulip", PINK_TULIP, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_OXEYE_DAISY = register(new FlowerPotBlock("potted_oxeye_daisy", OXEYE_DAISY, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_CORNFLOWER = register(new FlowerPotBlock("potted_cornflower", CORNFLOWER, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_LILY_OF_THE_VALLEY = register(new FlowerPotBlock("potted_lily_of_the_valley", LILY_OF_THE_VALLEY, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_WITHER_ROSE = register(new FlowerPotBlock("potted_wither_rose", WITHER_ROSE, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_RED_MUSHROOM = register(new FlowerPotBlock("potted_red_mushroom", RED_MUSHROOM, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_BROWN_MUSHROOM = register(new FlowerPotBlock("potted_brown_mushroom", BROWN_MUSHROOM, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_DEAD_BUSH = register(new FlowerPotBlock("potted_dead_bush", DEAD_BUSH, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_CACTUS = register(new FlowerPotBlock("potted_cactus", CACTUS, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final CropBlock CARROTS = register(new CropBlock("carrots", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(AGE_7)));
    public static final CropBlock POTATOES = register(new CropBlock("potatoes", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(AGE_7)));
    public static final Block OAK_BUTTON = register(new Block("oak_button", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(ATTACH_FACE)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(POWERED)));
    public static final Block SPRUCE_BUTTON = register(new Block("spruce_button", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(ATTACH_FACE)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(POWERED)));
    public static final Block BIRCH_BUTTON = register(new Block("birch_button", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(ATTACH_FACE)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(POWERED)));
    public static final Block JUNGLE_BUTTON = register(new Block("jungle_button", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(ATTACH_FACE)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(POWERED)));
    public static final Block ACACIA_BUTTON = register(new Block("acacia_button", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(ATTACH_FACE)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(POWERED)));
    public static final Block CHERRY_BUTTON = register(new Block("cherry_button", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(ATTACH_FACE)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(POWERED)));
    public static final Block DARK_OAK_BUTTON = register(new Block("dark_oak_button", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(ATTACH_FACE)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(POWERED)));
    public static final Block PALE_OAK_BUTTON = register(new Block("pale_oak_button", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(ATTACH_FACE)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(POWERED)));
    public static final Block MANGROVE_BUTTON = register(new Block("mangrove_button", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(ATTACH_FACE)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(POWERED)));
    public static final Block BAMBOO_BUTTON = register(new Block("bamboo_button", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(ATTACH_FACE)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(POWERED)));
    public static final SkullBlock SKELETON_SKULL = register(new SkullBlock("skeleton_skull", SkullBlock.Type.SKELETON, builder().setBlockEntity(BlockEntityType.SKULL).destroyTime(1.0f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .booleanState(POWERED)
        .intState(ROTATION_16)));
    public static final WallSkullBlock SKELETON_WALL_SKULL = register(new WallSkullBlock("skeleton_wall_skull", SkullBlock.Type.SKELETON, builder().setBlockEntity(BlockEntityType.SKULL).destroyTime(1.0f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(POWERED)));
    public static final SkullBlock WITHER_SKELETON_SKULL = register(new SkullBlock("wither_skeleton_skull", SkullBlock.Type.WITHER_SKELETON, builder().setBlockEntity(BlockEntityType.SKULL).destroyTime(1.0f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .booleanState(POWERED)
        .intState(ROTATION_16)));
    public static final WallSkullBlock WITHER_SKELETON_WALL_SKULL = register(new WallSkullBlock("wither_skeleton_wall_skull", SkullBlock.Type.WITHER_SKELETON, builder().setBlockEntity(BlockEntityType.SKULL).destroyTime(1.0f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(POWERED)));
    public static final SkullBlock ZOMBIE_HEAD = register(new SkullBlock("zombie_head", SkullBlock.Type.ZOMBIE, builder().setBlockEntity(BlockEntityType.SKULL).destroyTime(1.0f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .booleanState(POWERED)
        .intState(ROTATION_16)));
    public static final WallSkullBlock ZOMBIE_WALL_HEAD = register(new WallSkullBlock("zombie_wall_head", SkullBlock.Type.ZOMBIE, builder().setBlockEntity(BlockEntityType.SKULL).destroyTime(1.0f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(POWERED)));
    public static final SkullBlock PLAYER_HEAD = register(new SkullBlock("player_head", SkullBlock.Type.PLAYER, builder().setBlockEntity(BlockEntityType.SKULL).destroyTime(1.0f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .booleanState(POWERED)
        .intState(ROTATION_16)));
    public static final WallSkullBlock PLAYER_WALL_HEAD = register(new WallSkullBlock("player_wall_head", SkullBlock.Type.PLAYER, builder().setBlockEntity(BlockEntityType.SKULL).destroyTime(1.0f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(POWERED)));
    public static final SkullBlock CREEPER_HEAD = register(new SkullBlock("creeper_head", SkullBlock.Type.CREEPER, builder().setBlockEntity(BlockEntityType.SKULL).destroyTime(1.0f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .booleanState(POWERED)
        .intState(ROTATION_16)));
    public static final WallSkullBlock CREEPER_WALL_HEAD = register(new WallSkullBlock("creeper_wall_head", SkullBlock.Type.CREEPER, builder().setBlockEntity(BlockEntityType.SKULL).destroyTime(1.0f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(POWERED)));
    public static final SkullBlock DRAGON_HEAD = register(new SkullBlock("dragon_head", SkullBlock.Type.DRAGON, builder().setBlockEntity(BlockEntityType.SKULL).destroyTime(1.0f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .booleanState(POWERED)
        .intState(ROTATION_16)));
    public static final WallSkullBlock DRAGON_WALL_HEAD = register(new WallSkullBlock("dragon_wall_head", SkullBlock.Type.DRAGON, builder().setBlockEntity(BlockEntityType.SKULL).destroyTime(1.0f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(POWERED)));
    public static final SkullBlock PIGLIN_HEAD = register(new SkullBlock("piglin_head", SkullBlock.Type.PIGLIN, builder().setBlockEntity(BlockEntityType.SKULL).destroyTime(1.0f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .booleanState(POWERED)
        .intState(ROTATION_16)));
    public static final WallSkullBlock PIGLIN_WALL_HEAD = register(new WallSkullBlock("piglin_wall_head", SkullBlock.Type.PIGLIN, builder().setBlockEntity(BlockEntityType.SKULL).destroyTime(1.0f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(POWERED)));
    public static final Block ANVIL = register(new Block("anvil", builder().requiresCorrectToolForDrops().destroyTime(5.0f).pushReaction(PistonBehavior.BLOCK)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block CHIPPED_ANVIL = register(new Block("chipped_anvil", builder().requiresCorrectToolForDrops().destroyTime(5.0f).pushReaction(PistonBehavior.BLOCK)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block DAMAGED_ANVIL = register(new Block("damaged_anvil", builder().requiresCorrectToolForDrops().destroyTime(5.0f).pushReaction(PistonBehavior.BLOCK)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final ChestBlock TRAPPED_CHEST = register(new ChestBlock("trapped_chest", builder().setBlockEntity(BlockEntityType.TRAPPED_CHEST).destroyTime(2.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(CHEST_TYPE, ChestType.VALUES)
        .booleanState(WATERLOGGED)));
    public static final Block LIGHT_WEIGHTED_PRESSURE_PLATE = register(new Block("light_weighted_pressure_plate", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY)
        .intState(POWER)));
    public static final Block HEAVY_WEIGHTED_PRESSURE_PLATE = register(new Block("heavy_weighted_pressure_plate", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY)
        .intState(POWER)));
    public static final ComparatorBlock COMPARATOR = register(new ComparatorBlock("comparator", builder().setBlockEntity(BlockEntityType.COMPARATOR).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(MODE_COMPARATOR)
        .booleanState(POWERED)));
    public static final Block DAYLIGHT_DETECTOR = register(new Block("daylight_detector", builder().setBlockEntity(BlockEntityType.DAYLIGHT_DETECTOR).destroyTime(0.2f)
        .booleanState(INVERTED)
        .intState(POWER)));
    public static final Block REDSTONE_BLOCK = register(new Block("redstone_block", builder().requiresCorrectToolForDrops().destroyTime(5.0f).solidRender()));
    public static final Block NETHER_QUARTZ_ORE = register(new Block("nether_quartz_ore", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block HOPPER = register(new Block("hopper", builder().setBlockEntity(BlockEntityType.HOPPER).requiresCorrectToolForDrops().destroyTime(3.0f)
        .booleanState(ENABLED)
        .enumState(FACING_HOPPER, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block QUARTZ_BLOCK = register(new Block("quartz_block", builder().requiresCorrectToolForDrops().destroyTime(0.8f).solidRender()));
    public static final Block CHISELED_QUARTZ_BLOCK = register(new Block("chiseled_quartz_block", builder().requiresCorrectToolForDrops().destroyTime(0.8f).solidRender()));
    public static final Block QUARTZ_PILLAR = register(new Block("quartz_pillar", builder().requiresCorrectToolForDrops().destroyTime(0.8f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block QUARTZ_STAIRS = register(new Block("quartz_stairs", builder().requiresCorrectToolForDrops().destroyTime(0.8f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block ACTIVATOR_RAIL = register(new Block("activator_rail", builder().destroyTime(0.7f).notSolid()
        .booleanState(POWERED)
        .enumState(RAIL_SHAPE_STRAIGHT)
        .booleanState(WATERLOGGED)));
    public static final Block DROPPER = register(new Block("dropper", builder().setBlockEntity(BlockEntityType.DROPPER).requiresCorrectToolForDrops().destroyTime(3.5f).solidRender()
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)
        .booleanState(TRIGGERED)));
    public static final Block WHITE_TERRACOTTA = register(new Block("white_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.25f).solidRender()));
    public static final Block ORANGE_TERRACOTTA = register(new Block("orange_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.25f).solidRender()));
    public static final Block MAGENTA_TERRACOTTA = register(new Block("magenta_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.25f).solidRender()));
    public static final Block LIGHT_BLUE_TERRACOTTA = register(new Block("light_blue_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.25f).solidRender()));
    public static final Block YELLOW_TERRACOTTA = register(new Block("yellow_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.25f).solidRender()));
    public static final Block LIME_TERRACOTTA = register(new Block("lime_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.25f).solidRender()));
    public static final Block PINK_TERRACOTTA = register(new Block("pink_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.25f).solidRender()));
    public static final Block GRAY_TERRACOTTA = register(new Block("gray_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.25f).solidRender()));
    public static final Block LIGHT_GRAY_TERRACOTTA = register(new Block("light_gray_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.25f).solidRender()));
    public static final Block CYAN_TERRACOTTA = register(new Block("cyan_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.25f).solidRender()));
    public static final Block PURPLE_TERRACOTTA = register(new Block("purple_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.25f).solidRender()));
    public static final Block BLUE_TERRACOTTA = register(new Block("blue_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.25f).solidRender()));
    public static final Block BROWN_TERRACOTTA = register(new Block("brown_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.25f).solidRender()));
    public static final Block GREEN_TERRACOTTA = register(new Block("green_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.25f).solidRender()));
    public static final Block RED_TERRACOTTA = register(new Block("red_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.25f).solidRender()));
    public static final Block BLACK_TERRACOTTA = register(new Block("black_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.25f).solidRender()));
    public static final Block WHITE_STAINED_GLASS_PANE = register(new Block("white_stained_glass_pane", builder().destroyTime(0.3f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block ORANGE_STAINED_GLASS_PANE = register(new Block("orange_stained_glass_pane", builder().destroyTime(0.3f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block MAGENTA_STAINED_GLASS_PANE = register(new Block("magenta_stained_glass_pane", builder().destroyTime(0.3f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block LIGHT_BLUE_STAINED_GLASS_PANE = register(new Block("light_blue_stained_glass_pane", builder().destroyTime(0.3f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block YELLOW_STAINED_GLASS_PANE = register(new Block("yellow_stained_glass_pane", builder().destroyTime(0.3f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block LIME_STAINED_GLASS_PANE = register(new Block("lime_stained_glass_pane", builder().destroyTime(0.3f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block PINK_STAINED_GLASS_PANE = register(new Block("pink_stained_glass_pane", builder().destroyTime(0.3f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block GRAY_STAINED_GLASS_PANE = register(new Block("gray_stained_glass_pane", builder().destroyTime(0.3f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block LIGHT_GRAY_STAINED_GLASS_PANE = register(new Block("light_gray_stained_glass_pane", builder().destroyTime(0.3f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block CYAN_STAINED_GLASS_PANE = register(new Block("cyan_stained_glass_pane", builder().destroyTime(0.3f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block PURPLE_STAINED_GLASS_PANE = register(new Block("purple_stained_glass_pane", builder().destroyTime(0.3f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block BLUE_STAINED_GLASS_PANE = register(new Block("blue_stained_glass_pane", builder().destroyTime(0.3f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block BROWN_STAINED_GLASS_PANE = register(new Block("brown_stained_glass_pane", builder().destroyTime(0.3f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block GREEN_STAINED_GLASS_PANE = register(new Block("green_stained_glass_pane", builder().destroyTime(0.3f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block RED_STAINED_GLASS_PANE = register(new Block("red_stained_glass_pane", builder().destroyTime(0.3f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block BLACK_STAINED_GLASS_PANE = register(new Block("black_stained_glass_pane", builder().destroyTime(0.3f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block ACACIA_STAIRS = register(new Block("acacia_stairs", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block CHERRY_STAIRS = register(new Block("cherry_stairs", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block DARK_OAK_STAIRS = register(new Block("dark_oak_stairs", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block PALE_OAK_STAIRS = register(new Block("pale_oak_stairs", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block MANGROVE_STAIRS = register(new Block("mangrove_stairs", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block BAMBOO_STAIRS = register(new Block("bamboo_stairs", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block BAMBOO_MOSAIC_STAIRS = register(new Block("bamboo_mosaic_stairs", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block SLIME_BLOCK = register(new Block("slime_block", builder()));
    public static final Block BARRIER = register(new Block("barrier", builder().destroyTime(-1.0f).pushReaction(PistonBehavior.BLOCK)
        .booleanState(WATERLOGGED)));
    public static final Block LIGHT = register(new Block("light", builder().destroyTime(-1.0f).replaceable().notSolid()
        .intState(LEVEL)
        .booleanState(WATERLOGGED)));
    public static final TrapDoorBlock IRON_TRAPDOOR = register(new TrapDoorBlock("iron_trapdoor", builder().requiresCorrectToolForDrops().destroyTime(5.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .booleanState(OPEN)
        .booleanState(POWERED)
        .booleanState(WATERLOGGED)));
    public static final Block PRISMARINE = register(new Block("prismarine", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block PRISMARINE_BRICKS = register(new Block("prismarine_bricks", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block DARK_PRISMARINE = register(new Block("dark_prismarine", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block PRISMARINE_STAIRS = register(new Block("prismarine_stairs", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block PRISMARINE_BRICK_STAIRS = register(new Block("prismarine_brick_stairs", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block DARK_PRISMARINE_STAIRS = register(new Block("dark_prismarine_stairs", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block PRISMARINE_SLAB = register(new Block("prismarine_slab", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block PRISMARINE_BRICK_SLAB = register(new Block("prismarine_brick_slab", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block DARK_PRISMARINE_SLAB = register(new Block("dark_prismarine_slab", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block SEA_LANTERN = register(new Block("sea_lantern", builder().destroyTime(0.3f).solidRender()));
    public static final Block HAY_BLOCK = register(new Block("hay_block", builder().destroyTime(0.5f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block WHITE_CARPET = register(new Block("white_carpet", builder().destroyTime(0.1f).notSolid()));
    public static final Block ORANGE_CARPET = register(new Block("orange_carpet", builder().destroyTime(0.1f).notSolid()));
    public static final Block MAGENTA_CARPET = register(new Block("magenta_carpet", builder().destroyTime(0.1f).notSolid()));
    public static final Block LIGHT_BLUE_CARPET = register(new Block("light_blue_carpet", builder().destroyTime(0.1f).notSolid()));
    public static final Block YELLOW_CARPET = register(new Block("yellow_carpet", builder().destroyTime(0.1f).notSolid()));
    public static final Block LIME_CARPET = register(new Block("lime_carpet", builder().destroyTime(0.1f).notSolid()));
    public static final Block PINK_CARPET = register(new Block("pink_carpet", builder().destroyTime(0.1f).notSolid()));
    public static final Block GRAY_CARPET = register(new Block("gray_carpet", builder().destroyTime(0.1f).notSolid()));
    public static final Block LIGHT_GRAY_CARPET = register(new Block("light_gray_carpet", builder().destroyTime(0.1f).notSolid()));
    public static final Block CYAN_CARPET = register(new Block("cyan_carpet", builder().destroyTime(0.1f).notSolid()));
    public static final Block PURPLE_CARPET = register(new Block("purple_carpet", builder().destroyTime(0.1f).notSolid()));
    public static final Block BLUE_CARPET = register(new Block("blue_carpet", builder().destroyTime(0.1f).notSolid()));
    public static final Block BROWN_CARPET = register(new Block("brown_carpet", builder().destroyTime(0.1f).notSolid()));
    public static final Block GREEN_CARPET = register(new Block("green_carpet", builder().destroyTime(0.1f).notSolid()));
    public static final Block RED_CARPET = register(new Block("red_carpet", builder().destroyTime(0.1f).notSolid()));
    public static final Block BLACK_CARPET = register(new Block("black_carpet", builder().destroyTime(0.1f).notSolid()));
    public static final Block TERRACOTTA = register(new Block("terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.25f).solidRender()));
    public static final Block COAL_BLOCK = register(new Block("coal_block", builder().requiresCorrectToolForDrops().destroyTime(5.0f).solidRender()));
    public static final Block PACKED_ICE = register(new Block("packed_ice", builder().destroyTime(0.5f).solidRender()));
    public static final TallFlowerBlock SUNFLOWER = register(new TallFlowerBlock("sunflower", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(DOUBLE_BLOCK_HALF)));
    public static final TallFlowerBlock LILAC = register(new TallFlowerBlock("lilac", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(DOUBLE_BLOCK_HALF)));
    public static final TallFlowerBlock ROSE_BUSH = register(new TallFlowerBlock("rose_bush", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(DOUBLE_BLOCK_HALF)));
    public static final TallFlowerBlock PEONY = register(new TallFlowerBlock("peony", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(DOUBLE_BLOCK_HALF)));
    public static final BushBlock TALL_GRASS = register(new BushBlock("tall_grass", builder().pushReaction(PistonBehavior.DESTROY).replaceable().notSolid()
        .enumState(DOUBLE_BLOCK_HALF)));
    public static final BushBlock LARGE_FERN = register(new BushBlock("large_fern", builder().pushReaction(PistonBehavior.DESTROY).replaceable().notSolid()
        .enumState(DOUBLE_BLOCK_HALF)));
    public static final BannerBlock WHITE_BANNER = register(new BannerBlock("white_banner", 0, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .intState(ROTATION_16)));
    public static final BannerBlock ORANGE_BANNER = register(new BannerBlock("orange_banner", 1, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .intState(ROTATION_16)));
    public static final BannerBlock MAGENTA_BANNER = register(new BannerBlock("magenta_banner", 2, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .intState(ROTATION_16)));
    public static final BannerBlock LIGHT_BLUE_BANNER = register(new BannerBlock("light_blue_banner", 3, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .intState(ROTATION_16)));
    public static final BannerBlock YELLOW_BANNER = register(new BannerBlock("yellow_banner", 4, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .intState(ROTATION_16)));
    public static final BannerBlock LIME_BANNER = register(new BannerBlock("lime_banner", 5, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .intState(ROTATION_16)));
    public static final BannerBlock PINK_BANNER = register(new BannerBlock("pink_banner", 6, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .intState(ROTATION_16)));
    public static final BannerBlock GRAY_BANNER = register(new BannerBlock("gray_banner", 7, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .intState(ROTATION_16)));
    public static final BannerBlock LIGHT_GRAY_BANNER = register(new BannerBlock("light_gray_banner", 8, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .intState(ROTATION_16)));
    public static final BannerBlock CYAN_BANNER = register(new BannerBlock("cyan_banner", 9, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .intState(ROTATION_16)));
    public static final BannerBlock PURPLE_BANNER = register(new BannerBlock("purple_banner", 10, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .intState(ROTATION_16)));
    public static final BannerBlock BLUE_BANNER = register(new BannerBlock("blue_banner", 11, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .intState(ROTATION_16)));
    public static final BannerBlock BROWN_BANNER = register(new BannerBlock("brown_banner", 12, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .intState(ROTATION_16)));
    public static final BannerBlock GREEN_BANNER = register(new BannerBlock("green_banner", 13, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .intState(ROTATION_16)));
    public static final BannerBlock RED_BANNER = register(new BannerBlock("red_banner", 14, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .intState(ROTATION_16)));
    public static final BannerBlock BLACK_BANNER = register(new BannerBlock("black_banner", 15, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .intState(ROTATION_16)));
    public static final BannerBlock WHITE_WALL_BANNER = register(new BannerBlock("white_wall_banner", 0, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final BannerBlock ORANGE_WALL_BANNER = register(new BannerBlock("orange_wall_banner", 1, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final BannerBlock MAGENTA_WALL_BANNER = register(new BannerBlock("magenta_wall_banner", 2, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final BannerBlock LIGHT_BLUE_WALL_BANNER = register(new BannerBlock("light_blue_wall_banner", 3, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final BannerBlock YELLOW_WALL_BANNER = register(new BannerBlock("yellow_wall_banner", 4, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final BannerBlock LIME_WALL_BANNER = register(new BannerBlock("lime_wall_banner", 5, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final BannerBlock PINK_WALL_BANNER = register(new BannerBlock("pink_wall_banner", 6, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final BannerBlock GRAY_WALL_BANNER = register(new BannerBlock("gray_wall_banner", 7, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final BannerBlock LIGHT_GRAY_WALL_BANNER = register(new BannerBlock("light_gray_wall_banner", 8, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final BannerBlock CYAN_WALL_BANNER = register(new BannerBlock("cyan_wall_banner", 9, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final BannerBlock PURPLE_WALL_BANNER = register(new BannerBlock("purple_wall_banner", 10, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final BannerBlock BLUE_WALL_BANNER = register(new BannerBlock("blue_wall_banner", 11, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final BannerBlock BROWN_WALL_BANNER = register(new BannerBlock("brown_wall_banner", 12, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final BannerBlock GREEN_WALL_BANNER = register(new BannerBlock("green_wall_banner", 13, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final BannerBlock RED_WALL_BANNER = register(new BannerBlock("red_wall_banner", 14, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final BannerBlock BLACK_WALL_BANNER = register(new BannerBlock("black_wall_banner", 15, builder().setBlockEntity(BlockEntityType.BANNER).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block RED_SANDSTONE = register(new Block("red_sandstone", builder().requiresCorrectToolForDrops().destroyTime(0.8f).solidRender()));
    public static final Block CHISELED_RED_SANDSTONE = register(new Block("chiseled_red_sandstone", builder().requiresCorrectToolForDrops().destroyTime(0.8f).solidRender()));
    public static final Block CUT_RED_SANDSTONE = register(new Block("cut_red_sandstone", builder().requiresCorrectToolForDrops().destroyTime(0.8f).solidRender()));
    public static final Block RED_SANDSTONE_STAIRS = register(new Block("red_sandstone_stairs", builder().requiresCorrectToolForDrops().destroyTime(0.8f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block OAK_SLAB = register(new Block("oak_slab", builder().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block SPRUCE_SLAB = register(new Block("spruce_slab", builder().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block BIRCH_SLAB = register(new Block("birch_slab", builder().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block JUNGLE_SLAB = register(new Block("jungle_slab", builder().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block ACACIA_SLAB = register(new Block("acacia_slab", builder().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block CHERRY_SLAB = register(new Block("cherry_slab", builder().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block DARK_OAK_SLAB = register(new Block("dark_oak_slab", builder().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block PALE_OAK_SLAB = register(new Block("pale_oak_slab", builder().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block MANGROVE_SLAB = register(new Block("mangrove_slab", builder().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block BAMBOO_SLAB = register(new Block("bamboo_slab", builder().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block BAMBOO_MOSAIC_SLAB = register(new Block("bamboo_mosaic_slab", builder().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block STONE_SLAB = register(new Block("stone_slab", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block SMOOTH_STONE_SLAB = register(new Block("smooth_stone_slab", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block SANDSTONE_SLAB = register(new Block("sandstone_slab", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block CUT_SANDSTONE_SLAB = register(new Block("cut_sandstone_slab", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block PETRIFIED_OAK_SLAB = register(new Block("petrified_oak_slab", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block COBBLESTONE_SLAB = register(new Block("cobblestone_slab", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block BRICK_SLAB = register(new Block("brick_slab", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block STONE_BRICK_SLAB = register(new Block("stone_brick_slab", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block MUD_BRICK_SLAB = register(new Block("mud_brick_slab", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block NETHER_BRICK_SLAB = register(new Block("nether_brick_slab", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block QUARTZ_SLAB = register(new Block("quartz_slab", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block RED_SANDSTONE_SLAB = register(new Block("red_sandstone_slab", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block CUT_RED_SANDSTONE_SLAB = register(new Block("cut_red_sandstone_slab", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block PURPUR_SLAB = register(new Block("purpur_slab", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block SMOOTH_STONE = register(new Block("smooth_stone", builder().requiresCorrectToolForDrops().destroyTime(2.0f).solidRender()));
    public static final Block SMOOTH_SANDSTONE = register(new Block("smooth_sandstone", builder().requiresCorrectToolForDrops().destroyTime(2.0f).solidRender()));
    public static final Block SMOOTH_QUARTZ = register(new Block("smooth_quartz", builder().requiresCorrectToolForDrops().destroyTime(2.0f).solidRender()));
    public static final Block SMOOTH_RED_SANDSTONE = register(new Block("smooth_red_sandstone", builder().requiresCorrectToolForDrops().destroyTime(2.0f).solidRender()));
    public static final Block SPRUCE_FENCE_GATE = register(new Block("spruce_fence_gate", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(IN_WALL)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final Block BIRCH_FENCE_GATE = register(new Block("birch_fence_gate", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(IN_WALL)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final Block JUNGLE_FENCE_GATE = register(new Block("jungle_fence_gate", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(IN_WALL)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final Block ACACIA_FENCE_GATE = register(new Block("acacia_fence_gate", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(IN_WALL)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final Block CHERRY_FENCE_GATE = register(new Block("cherry_fence_gate", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(IN_WALL)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final Block DARK_OAK_FENCE_GATE = register(new Block("dark_oak_fence_gate", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(IN_WALL)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final Block PALE_OAK_FENCE_GATE = register(new Block("pale_oak_fence_gate", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(IN_WALL)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final Block MANGROVE_FENCE_GATE = register(new Block("mangrove_fence_gate", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(IN_WALL)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final Block BAMBOO_FENCE_GATE = register(new Block("bamboo_fence_gate", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(IN_WALL)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final Block SPRUCE_FENCE = register(new Block("spruce_fence", builder().destroyTime(2.0f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block BIRCH_FENCE = register(new Block("birch_fence", builder().destroyTime(2.0f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block JUNGLE_FENCE = register(new Block("jungle_fence", builder().destroyTime(2.0f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block ACACIA_FENCE = register(new Block("acacia_fence", builder().destroyTime(2.0f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block CHERRY_FENCE = register(new Block("cherry_fence", builder().destroyTime(2.0f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block DARK_OAK_FENCE = register(new Block("dark_oak_fence", builder().destroyTime(2.0f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block PALE_OAK_FENCE = register(new Block("pale_oak_fence", builder().destroyTime(2.0f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block MANGROVE_FENCE = register(new Block("mangrove_fence", builder().destroyTime(2.0f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block BAMBOO_FENCE = register(new Block("bamboo_fence", builder().destroyTime(2.0f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final DoorBlock SPRUCE_DOOR = register(new DoorBlock("spruce_door", builder().destroyTime(3.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(DOUBLE_BLOCK_HALF)
        .enumState(DOOR_HINGE)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final DoorBlock BIRCH_DOOR = register(new DoorBlock("birch_door", builder().destroyTime(3.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(DOUBLE_BLOCK_HALF)
        .enumState(DOOR_HINGE)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final DoorBlock JUNGLE_DOOR = register(new DoorBlock("jungle_door", builder().destroyTime(3.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(DOUBLE_BLOCK_HALF)
        .enumState(DOOR_HINGE)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final DoorBlock ACACIA_DOOR = register(new DoorBlock("acacia_door", builder().destroyTime(3.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(DOUBLE_BLOCK_HALF)
        .enumState(DOOR_HINGE)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final DoorBlock CHERRY_DOOR = register(new DoorBlock("cherry_door", builder().destroyTime(3.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(DOUBLE_BLOCK_HALF)
        .enumState(DOOR_HINGE)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final DoorBlock DARK_OAK_DOOR = register(new DoorBlock("dark_oak_door", builder().destroyTime(3.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(DOUBLE_BLOCK_HALF)
        .enumState(DOOR_HINGE)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final DoorBlock PALE_OAK_DOOR = register(new DoorBlock("pale_oak_door", builder().destroyTime(3.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(DOUBLE_BLOCK_HALF)
        .enumState(DOOR_HINGE)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final DoorBlock MANGROVE_DOOR = register(new DoorBlock("mangrove_door", builder().destroyTime(3.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(DOUBLE_BLOCK_HALF)
        .enumState(DOOR_HINGE)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final DoorBlock BAMBOO_DOOR = register(new DoorBlock("bamboo_door", builder().destroyTime(3.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(DOUBLE_BLOCK_HALF)
        .enumState(DOOR_HINGE)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final Block END_ROD = register(new Block("end_rod", builder().notSolid()
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)));
    public static final Block CHORUS_PLANT = register(new Block("chorus_plant", builder().destroyTime(0.4f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .booleanState(DOWN)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(UP)
        .booleanState(WEST)));
    public static final Block CHORUS_FLOWER = register(new Block("chorus_flower", builder().destroyTime(0.4f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(AGE_5)));
    public static final Block PURPUR_BLOCK = register(new Block("purpur_block", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block PURPUR_PILLAR = register(new Block("purpur_pillar", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block PURPUR_STAIRS = register(new Block("purpur_stairs", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block END_STONE_BRICKS = register(new Block("end_stone_bricks", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final TorchflowerCropBlock TORCHFLOWER_CROP = register(new TorchflowerCropBlock("torchflower_crop", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(AGE_1)));
    public static final PitcherCropBlock PITCHER_CROP = register(new PitcherCropBlock("pitcher_crop", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(AGE_4)
        .enumState(DOUBLE_BLOCK_HALF)));
    public static final BushBlock PITCHER_PLANT = register(new BushBlock("pitcher_plant", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(DOUBLE_BLOCK_HALF)));
    public static final BeetrootBlock BEETROOTS = register(new BeetrootBlock("beetroots", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(AGE_3)));
    public static final Block DIRT_PATH = register(new Block("dirt_path", builder().destroyTime(0.65f)));
    public static final Block END_GATEWAY = register(new Block("end_gateway", builder().setBlockEntity(BlockEntityType.END_GATEWAY).destroyTime(-1.0f).pushReaction(PistonBehavior.BLOCK).notSolid()));
    public static final Block REPEATING_COMMAND_BLOCK = register(new Block("repeating_command_block", builder().setBlockEntity(BlockEntityType.COMMAND_BLOCK).requiresCorrectToolForDrops().destroyTime(-1.0f).solidRender()
        .booleanState(CONDITIONAL)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)));
    public static final Block CHAIN_COMMAND_BLOCK = register(new Block("chain_command_block", builder().setBlockEntity(BlockEntityType.COMMAND_BLOCK).requiresCorrectToolForDrops().destroyTime(-1.0f).solidRender()
        .booleanState(CONDITIONAL)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)));
    public static final IceBlock FROSTED_ICE = register(new IceBlock("frosted_ice", builder().destroyTime(0.5f)
        .intState(AGE_3)));
    public static final Block MAGMA_BLOCK = register(new Block("magma_block", builder().requiresCorrectToolForDrops().destroyTime(0.5f).solidRender()));
    public static final Block NETHER_WART_BLOCK = register(new Block("nether_wart_block", builder().destroyTime(1.0f).solidRender()));
    public static final Block RED_NETHER_BRICKS = register(new Block("red_nether_bricks", builder().requiresCorrectToolForDrops().destroyTime(2.0f).solidRender()));
    public static final Block BONE_BLOCK = register(new Block("bone_block", builder().requiresCorrectToolForDrops().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block STRUCTURE_VOID = register(new Block("structure_void", builder().pushReaction(PistonBehavior.DESTROY).replaceable().notSolid()));
    public static final Block OBSERVER = register(new Block("observer", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)
        .booleanState(POWERED)));
    public static final Block SHULKER_BOX = register(new Block("shulker_box", builder().setBlockEntity(BlockEntityType.SHULKER_BOX).destroyTime(2.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)));
    public static final Block WHITE_SHULKER_BOX = register(new Block("white_shulker_box", builder().setBlockEntity(BlockEntityType.SHULKER_BOX).destroyTime(2.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)));
    public static final Block ORANGE_SHULKER_BOX = register(new Block("orange_shulker_box", builder().setBlockEntity(BlockEntityType.SHULKER_BOX).destroyTime(2.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)));
    public static final Block MAGENTA_SHULKER_BOX = register(new Block("magenta_shulker_box", builder().setBlockEntity(BlockEntityType.SHULKER_BOX).destroyTime(2.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)));
    public static final Block LIGHT_BLUE_SHULKER_BOX = register(new Block("light_blue_shulker_box", builder().setBlockEntity(BlockEntityType.SHULKER_BOX).destroyTime(2.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)));
    public static final Block YELLOW_SHULKER_BOX = register(new Block("yellow_shulker_box", builder().setBlockEntity(BlockEntityType.SHULKER_BOX).destroyTime(2.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)));
    public static final Block LIME_SHULKER_BOX = register(new Block("lime_shulker_box", builder().setBlockEntity(BlockEntityType.SHULKER_BOX).destroyTime(2.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)));
    public static final Block PINK_SHULKER_BOX = register(new Block("pink_shulker_box", builder().setBlockEntity(BlockEntityType.SHULKER_BOX).destroyTime(2.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)));
    public static final Block GRAY_SHULKER_BOX = register(new Block("gray_shulker_box", builder().setBlockEntity(BlockEntityType.SHULKER_BOX).destroyTime(2.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)));
    public static final Block LIGHT_GRAY_SHULKER_BOX = register(new Block("light_gray_shulker_box", builder().setBlockEntity(BlockEntityType.SHULKER_BOX).destroyTime(2.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)));
    public static final Block CYAN_SHULKER_BOX = register(new Block("cyan_shulker_box", builder().setBlockEntity(BlockEntityType.SHULKER_BOX).destroyTime(2.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)));
    public static final Block PURPLE_SHULKER_BOX = register(new Block("purple_shulker_box", builder().setBlockEntity(BlockEntityType.SHULKER_BOX).destroyTime(2.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)));
    public static final Block BLUE_SHULKER_BOX = register(new Block("blue_shulker_box", builder().setBlockEntity(BlockEntityType.SHULKER_BOX).destroyTime(2.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)));
    public static final Block BROWN_SHULKER_BOX = register(new Block("brown_shulker_box", builder().setBlockEntity(BlockEntityType.SHULKER_BOX).destroyTime(2.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)));
    public static final Block GREEN_SHULKER_BOX = register(new Block("green_shulker_box", builder().setBlockEntity(BlockEntityType.SHULKER_BOX).destroyTime(2.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)));
    public static final Block RED_SHULKER_BOX = register(new Block("red_shulker_box", builder().setBlockEntity(BlockEntityType.SHULKER_BOX).destroyTime(2.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)));
    public static final Block BLACK_SHULKER_BOX = register(new Block("black_shulker_box", builder().setBlockEntity(BlockEntityType.SHULKER_BOX).destroyTime(2.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)));
    public static final Block WHITE_GLAZED_TERRACOTTA = register(new Block("white_glazed_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.4f).pushReaction(PistonBehavior.PUSH_ONLY).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block ORANGE_GLAZED_TERRACOTTA = register(new Block("orange_glazed_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.4f).pushReaction(PistonBehavior.PUSH_ONLY).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block MAGENTA_GLAZED_TERRACOTTA = register(new Block("magenta_glazed_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.4f).pushReaction(PistonBehavior.PUSH_ONLY).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block LIGHT_BLUE_GLAZED_TERRACOTTA = register(new Block("light_blue_glazed_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.4f).pushReaction(PistonBehavior.PUSH_ONLY).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block YELLOW_GLAZED_TERRACOTTA = register(new Block("yellow_glazed_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.4f).pushReaction(PistonBehavior.PUSH_ONLY).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block LIME_GLAZED_TERRACOTTA = register(new Block("lime_glazed_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.4f).pushReaction(PistonBehavior.PUSH_ONLY).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block PINK_GLAZED_TERRACOTTA = register(new Block("pink_glazed_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.4f).pushReaction(PistonBehavior.PUSH_ONLY).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block GRAY_GLAZED_TERRACOTTA = register(new Block("gray_glazed_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.4f).pushReaction(PistonBehavior.PUSH_ONLY).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block LIGHT_GRAY_GLAZED_TERRACOTTA = register(new Block("light_gray_glazed_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.4f).pushReaction(PistonBehavior.PUSH_ONLY).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block CYAN_GLAZED_TERRACOTTA = register(new Block("cyan_glazed_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.4f).pushReaction(PistonBehavior.PUSH_ONLY).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block PURPLE_GLAZED_TERRACOTTA = register(new Block("purple_glazed_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.4f).pushReaction(PistonBehavior.PUSH_ONLY).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block BLUE_GLAZED_TERRACOTTA = register(new Block("blue_glazed_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.4f).pushReaction(PistonBehavior.PUSH_ONLY).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block BROWN_GLAZED_TERRACOTTA = register(new Block("brown_glazed_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.4f).pushReaction(PistonBehavior.PUSH_ONLY).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block GREEN_GLAZED_TERRACOTTA = register(new Block("green_glazed_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.4f).pushReaction(PistonBehavior.PUSH_ONLY).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block RED_GLAZED_TERRACOTTA = register(new Block("red_glazed_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.4f).pushReaction(PistonBehavior.PUSH_ONLY).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block BLACK_GLAZED_TERRACOTTA = register(new Block("black_glazed_terracotta", builder().requiresCorrectToolForDrops().destroyTime(1.4f).pushReaction(PistonBehavior.PUSH_ONLY).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block WHITE_CONCRETE = register(new Block("white_concrete", builder().requiresCorrectToolForDrops().destroyTime(1.8f).solidRender()));
    public static final Block ORANGE_CONCRETE = register(new Block("orange_concrete", builder().requiresCorrectToolForDrops().destroyTime(1.8f).solidRender()));
    public static final Block MAGENTA_CONCRETE = register(new Block("magenta_concrete", builder().requiresCorrectToolForDrops().destroyTime(1.8f).solidRender()));
    public static final Block LIGHT_BLUE_CONCRETE = register(new Block("light_blue_concrete", builder().requiresCorrectToolForDrops().destroyTime(1.8f).solidRender()));
    public static final Block YELLOW_CONCRETE = register(new Block("yellow_concrete", builder().requiresCorrectToolForDrops().destroyTime(1.8f).solidRender()));
    public static final Block LIME_CONCRETE = register(new Block("lime_concrete", builder().requiresCorrectToolForDrops().destroyTime(1.8f).solidRender()));
    public static final Block PINK_CONCRETE = register(new Block("pink_concrete", builder().requiresCorrectToolForDrops().destroyTime(1.8f).solidRender()));
    public static final Block GRAY_CONCRETE = register(new Block("gray_concrete", builder().requiresCorrectToolForDrops().destroyTime(1.8f).solidRender()));
    public static final Block LIGHT_GRAY_CONCRETE = register(new Block("light_gray_concrete", builder().requiresCorrectToolForDrops().destroyTime(1.8f).solidRender()));
    public static final Block CYAN_CONCRETE = register(new Block("cyan_concrete", builder().requiresCorrectToolForDrops().destroyTime(1.8f).solidRender()));
    public static final Block PURPLE_CONCRETE = register(new Block("purple_concrete", builder().requiresCorrectToolForDrops().destroyTime(1.8f).solidRender()));
    public static final Block BLUE_CONCRETE = register(new Block("blue_concrete", builder().requiresCorrectToolForDrops().destroyTime(1.8f).solidRender()));
    public static final Block BROWN_CONCRETE = register(new Block("brown_concrete", builder().requiresCorrectToolForDrops().destroyTime(1.8f).solidRender()));
    public static final Block GREEN_CONCRETE = register(new Block("green_concrete", builder().requiresCorrectToolForDrops().destroyTime(1.8f).solidRender()));
    public static final Block RED_CONCRETE = register(new Block("red_concrete", builder().requiresCorrectToolForDrops().destroyTime(1.8f).solidRender()));
    public static final Block BLACK_CONCRETE = register(new Block("black_concrete", builder().requiresCorrectToolForDrops().destroyTime(1.8f).solidRender()));
    public static final Block WHITE_CONCRETE_POWDER = register(new Block("white_concrete_powder", builder().destroyTime(0.5f).solidRender()));
    public static final Block ORANGE_CONCRETE_POWDER = register(new Block("orange_concrete_powder", builder().destroyTime(0.5f).solidRender()));
    public static final Block MAGENTA_CONCRETE_POWDER = register(new Block("magenta_concrete_powder", builder().destroyTime(0.5f).solidRender()));
    public static final Block LIGHT_BLUE_CONCRETE_POWDER = register(new Block("light_blue_concrete_powder", builder().destroyTime(0.5f).solidRender()));
    public static final Block YELLOW_CONCRETE_POWDER = register(new Block("yellow_concrete_powder", builder().destroyTime(0.5f).solidRender()));
    public static final Block LIME_CONCRETE_POWDER = register(new Block("lime_concrete_powder", builder().destroyTime(0.5f).solidRender()));
    public static final Block PINK_CONCRETE_POWDER = register(new Block("pink_concrete_powder", builder().destroyTime(0.5f).solidRender()));
    public static final Block GRAY_CONCRETE_POWDER = register(new Block("gray_concrete_powder", builder().destroyTime(0.5f).solidRender()));
    public static final Block LIGHT_GRAY_CONCRETE_POWDER = register(new Block("light_gray_concrete_powder", builder().destroyTime(0.5f).solidRender()));
    public static final Block CYAN_CONCRETE_POWDER = register(new Block("cyan_concrete_powder", builder().destroyTime(0.5f).solidRender()));
    public static final Block PURPLE_CONCRETE_POWDER = register(new Block("purple_concrete_powder", builder().destroyTime(0.5f).solidRender()));
    public static final Block BLUE_CONCRETE_POWDER = register(new Block("blue_concrete_powder", builder().destroyTime(0.5f).solidRender()));
    public static final Block BROWN_CONCRETE_POWDER = register(new Block("brown_concrete_powder", builder().destroyTime(0.5f).solidRender()));
    public static final Block GREEN_CONCRETE_POWDER = register(new Block("green_concrete_powder", builder().destroyTime(0.5f).solidRender()));
    public static final Block RED_CONCRETE_POWDER = register(new Block("red_concrete_powder", builder().destroyTime(0.5f).solidRender()));
    public static final Block BLACK_CONCRETE_POWDER = register(new Block("black_concrete_powder", builder().destroyTime(0.5f).solidRender()));
    public static final KelpBlock KELP = register(new KelpBlock("kelp", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(AGE_25)));
    public static final KelpPlantBlock KELP_PLANT = register(new KelpPlantBlock("kelp_plant", builder().pushReaction(PistonBehavior.DESTROY).notSolid().pickItem(() -> Items.KELP)));
    public static final Block DRIED_KELP_BLOCK = register(new Block("dried_kelp_block", builder().destroyTime(0.5f).solidRender()));
    public static final Block TURTLE_EGG = register(new Block("turtle_egg", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY)
        .intState(EGGS)
        .intState(HATCH)));
    public static final Block SNIFFER_EGG = register(new Block("sniffer_egg", builder().destroyTime(0.5f)
        .intState(HATCH)));
    public static final Block DEAD_TUBE_CORAL_BLOCK = register(new Block("dead_tube_coral_block", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block DEAD_BRAIN_CORAL_BLOCK = register(new Block("dead_brain_coral_block", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block DEAD_BUBBLE_CORAL_BLOCK = register(new Block("dead_bubble_coral_block", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block DEAD_FIRE_CORAL_BLOCK = register(new Block("dead_fire_coral_block", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block DEAD_HORN_CORAL_BLOCK = register(new Block("dead_horn_coral_block", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block TUBE_CORAL_BLOCK = register(new Block("tube_coral_block", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block BRAIN_CORAL_BLOCK = register(new Block("brain_coral_block", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block BUBBLE_CORAL_BLOCK = register(new Block("bubble_coral_block", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block FIRE_CORAL_BLOCK = register(new Block("fire_coral_block", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block HORN_CORAL_BLOCK = register(new Block("horn_coral_block", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block DEAD_TUBE_CORAL = register(new Block("dead_tube_coral", builder().requiresCorrectToolForDrops()
        .booleanState(WATERLOGGED)));
    public static final Block DEAD_BRAIN_CORAL = register(new Block("dead_brain_coral", builder().requiresCorrectToolForDrops()
        .booleanState(WATERLOGGED)));
    public static final Block DEAD_BUBBLE_CORAL = register(new Block("dead_bubble_coral", builder().requiresCorrectToolForDrops()
        .booleanState(WATERLOGGED)));
    public static final Block DEAD_FIRE_CORAL = register(new Block("dead_fire_coral", builder().requiresCorrectToolForDrops()
        .booleanState(WATERLOGGED)));
    public static final Block DEAD_HORN_CORAL = register(new Block("dead_horn_coral", builder().requiresCorrectToolForDrops()
        .booleanState(WATERLOGGED)));
    public static final Block TUBE_CORAL = register(new Block("tube_coral", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .booleanState(WATERLOGGED)));
    public static final Block BRAIN_CORAL = register(new Block("brain_coral", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .booleanState(WATERLOGGED)));
    public static final Block BUBBLE_CORAL = register(new Block("bubble_coral", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .booleanState(WATERLOGGED)));
    public static final Block FIRE_CORAL = register(new Block("fire_coral", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .booleanState(WATERLOGGED)));
    public static final Block HORN_CORAL = register(new Block("horn_coral", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .booleanState(WATERLOGGED)));
    public static final Block DEAD_TUBE_CORAL_FAN = register(new Block("dead_tube_coral_fan", builder().requiresCorrectToolForDrops()
        .booleanState(WATERLOGGED)));
    public static final Block DEAD_BRAIN_CORAL_FAN = register(new Block("dead_brain_coral_fan", builder().requiresCorrectToolForDrops()
        .booleanState(WATERLOGGED)));
    public static final Block DEAD_BUBBLE_CORAL_FAN = register(new Block("dead_bubble_coral_fan", builder().requiresCorrectToolForDrops()
        .booleanState(WATERLOGGED)));
    public static final Block DEAD_FIRE_CORAL_FAN = register(new Block("dead_fire_coral_fan", builder().requiresCorrectToolForDrops()
        .booleanState(WATERLOGGED)));
    public static final Block DEAD_HORN_CORAL_FAN = register(new Block("dead_horn_coral_fan", builder().requiresCorrectToolForDrops()
        .booleanState(WATERLOGGED)));
    public static final Block TUBE_CORAL_FAN = register(new Block("tube_coral_fan", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .booleanState(WATERLOGGED)));
    public static final Block BRAIN_CORAL_FAN = register(new Block("brain_coral_fan", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .booleanState(WATERLOGGED)));
    public static final Block BUBBLE_CORAL_FAN = register(new Block("bubble_coral_fan", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .booleanState(WATERLOGGED)));
    public static final Block FIRE_CORAL_FAN = register(new Block("fire_coral_fan", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .booleanState(WATERLOGGED)));
    public static final Block HORN_CORAL_FAN = register(new Block("horn_coral_fan", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .booleanState(WATERLOGGED)));
    public static final Block DEAD_TUBE_CORAL_WALL_FAN = register(new Block("dead_tube_coral_wall_fan", builder().requiresCorrectToolForDrops()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block DEAD_BRAIN_CORAL_WALL_FAN = register(new Block("dead_brain_coral_wall_fan", builder().requiresCorrectToolForDrops()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block DEAD_BUBBLE_CORAL_WALL_FAN = register(new Block("dead_bubble_coral_wall_fan", builder().requiresCorrectToolForDrops()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block DEAD_FIRE_CORAL_WALL_FAN = register(new Block("dead_fire_coral_wall_fan", builder().requiresCorrectToolForDrops()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block DEAD_HORN_CORAL_WALL_FAN = register(new Block("dead_horn_coral_wall_fan", builder().requiresCorrectToolForDrops()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block TUBE_CORAL_WALL_FAN = register(new Block("tube_coral_wall_fan", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block BRAIN_CORAL_WALL_FAN = register(new Block("brain_coral_wall_fan", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block BUBBLE_CORAL_WALL_FAN = register(new Block("bubble_coral_wall_fan", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block FIRE_CORAL_WALL_FAN = register(new Block("fire_coral_wall_fan", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block HORN_CORAL_WALL_FAN = register(new Block("horn_coral_wall_fan", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final SeaPickleBlock SEA_PICKLE = register(new SeaPickleBlock("sea_pickle", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(PICKLES)
        .booleanState(WATERLOGGED)));
    public static final Block BLUE_ICE = register(new Block("blue_ice", builder().destroyTime(2.8f).solidRender()));
    public static final Block CONDUIT = register(new Block("conduit", builder().setBlockEntity(BlockEntityType.CONDUIT).destroyTime(3.0f)
        .booleanState(WATERLOGGED)));
    public static final BambooSaplingBlock BAMBOO_SAPLING = register(new BambooSaplingBlock("bamboo_sapling", builder().destroyTime(1.0f).pushReaction(PistonBehavior.DESTROY).pickItem(() -> Items.BAMBOO)));
    public static final BambooBlock BAMBOO = register(new BambooBlock("bamboo", builder().destroyTime(1.0f).pushReaction(PistonBehavior.DESTROY)
        .intState(AGE_1)
        .enumState(BAMBOO_LEAVES)
        .intState(STAGE)));
    public static final FlowerPotBlock POTTED_BAMBOO = register(new FlowerPotBlock("potted_bamboo", BAMBOO, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final Block VOID_AIR = register(new Block("void_air", builder().replaceable().notSolid()));
    public static final Block CAVE_AIR = register(new Block("cave_air", builder().replaceable().notSolid()));
    public static final Block BUBBLE_COLUMN = register(new Block("bubble_column", builder().pushReaction(PistonBehavior.DESTROY).replaceable().notSolid()
        .booleanState(DRAG)));
    public static final Block POLISHED_GRANITE_STAIRS = register(new Block("polished_granite_stairs", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block SMOOTH_RED_SANDSTONE_STAIRS = register(new Block("smooth_red_sandstone_stairs", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block MOSSY_STONE_BRICK_STAIRS = register(new Block("mossy_stone_brick_stairs", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block POLISHED_DIORITE_STAIRS = register(new Block("polished_diorite_stairs", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block MOSSY_COBBLESTONE_STAIRS = register(new Block("mossy_cobblestone_stairs", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block END_STONE_BRICK_STAIRS = register(new Block("end_stone_brick_stairs", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block STONE_STAIRS = register(new Block("stone_stairs", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block SMOOTH_SANDSTONE_STAIRS = register(new Block("smooth_sandstone_stairs", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block SMOOTH_QUARTZ_STAIRS = register(new Block("smooth_quartz_stairs", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block GRANITE_STAIRS = register(new Block("granite_stairs", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block ANDESITE_STAIRS = register(new Block("andesite_stairs", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block RED_NETHER_BRICK_STAIRS = register(new Block("red_nether_brick_stairs", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block POLISHED_ANDESITE_STAIRS = register(new Block("polished_andesite_stairs", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block DIORITE_STAIRS = register(new Block("diorite_stairs", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block POLISHED_GRANITE_SLAB = register(new Block("polished_granite_slab", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block SMOOTH_RED_SANDSTONE_SLAB = register(new Block("smooth_red_sandstone_slab", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block MOSSY_STONE_BRICK_SLAB = register(new Block("mossy_stone_brick_slab", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block POLISHED_DIORITE_SLAB = register(new Block("polished_diorite_slab", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block MOSSY_COBBLESTONE_SLAB = register(new Block("mossy_cobblestone_slab", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block END_STONE_BRICK_SLAB = register(new Block("end_stone_brick_slab", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block SMOOTH_SANDSTONE_SLAB = register(new Block("smooth_sandstone_slab", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block SMOOTH_QUARTZ_SLAB = register(new Block("smooth_quartz_slab", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block GRANITE_SLAB = register(new Block("granite_slab", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block ANDESITE_SLAB = register(new Block("andesite_slab", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block RED_NETHER_BRICK_SLAB = register(new Block("red_nether_brick_slab", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block POLISHED_ANDESITE_SLAB = register(new Block("polished_andesite_slab", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block DIORITE_SLAB = register(new Block("diorite_slab", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block BRICK_WALL = register(new Block("brick_wall", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block PRISMARINE_WALL = register(new Block("prismarine_wall", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block RED_SANDSTONE_WALL = register(new Block("red_sandstone_wall", builder().requiresCorrectToolForDrops().destroyTime(0.8f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block MOSSY_STONE_BRICK_WALL = register(new Block("mossy_stone_brick_wall", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block GRANITE_WALL = register(new Block("granite_wall", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block STONE_BRICK_WALL = register(new Block("stone_brick_wall", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block MUD_BRICK_WALL = register(new Block("mud_brick_wall", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block NETHER_BRICK_WALL = register(new Block("nether_brick_wall", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block ANDESITE_WALL = register(new Block("andesite_wall", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block RED_NETHER_BRICK_WALL = register(new Block("red_nether_brick_wall", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block SANDSTONE_WALL = register(new Block("sandstone_wall", builder().requiresCorrectToolForDrops().destroyTime(0.8f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block END_STONE_BRICK_WALL = register(new Block("end_stone_brick_wall", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block DIORITE_WALL = register(new Block("diorite_wall", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block SCAFFOLDING = register(new Block("scaffolding", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .booleanState(BOTTOM)
        .intState(STABILITY_DISTANCE)
        .booleanState(WATERLOGGED)));
    public static final Block LOOM = register(new Block("loom", builder().destroyTime(2.5f).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final Block BARREL = register(new Block("barrel", builder().setBlockEntity(BlockEntityType.BARREL).destroyTime(2.5f).solidRender()
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)
        .booleanState(OPEN)));
    public static final Block SMOKER = register(new Block("smoker", builder().setBlockEntity(BlockEntityType.SMOKER).requiresCorrectToolForDrops().destroyTime(3.5f).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(LIT)));
    public static final Block BLAST_FURNACE = register(new Block("blast_furnace", builder().setBlockEntity(BlockEntityType.BLAST_FURNACE).requiresCorrectToolForDrops().destroyTime(3.5f).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(LIT)));
    public static final Block CARTOGRAPHY_TABLE = register(new Block("cartography_table", builder().destroyTime(2.5f).solidRender()));
    public static final Block FLETCHING_TABLE = register(new Block("fletching_table", builder().destroyTime(2.5f).solidRender()));
    public static final Block GRINDSTONE = register(new Block("grindstone", builder().requiresCorrectToolForDrops().destroyTime(2.0f).pushReaction(PistonBehavior.BLOCK)
        .enumState(ATTACH_FACE)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final LecternBlock LECTERN = register(new LecternBlock("lectern", builder().setBlockEntity(BlockEntityType.LECTERN).destroyTime(2.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(HAS_BOOK)
        .booleanState(POWERED)));
    public static final Block SMITHING_TABLE = register(new Block("smithing_table", builder().destroyTime(2.5f).solidRender()));
    public static final Block STONECUTTER = register(new Block("stonecutter", builder().requiresCorrectToolForDrops().destroyTime(3.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
    public static final BellBlock BELL = register(new BellBlock("bell", builder().setBlockEntity(BlockEntityType.BELL).destroyTime(5.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(BELL_ATTACHMENT)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(POWERED)));
    public static final Block LANTERN = register(new Block("lantern", builder().destroyTime(3.5f).pushReaction(PistonBehavior.DESTROY)
        .booleanState(HANGING)
        .booleanState(WATERLOGGED)));
    public static final Block SOUL_LANTERN = register(new Block("soul_lantern", builder().destroyTime(3.5f).pushReaction(PistonBehavior.DESTROY)
        .booleanState(HANGING)
        .booleanState(WATERLOGGED)));
    public static final CampfireBlock CAMPFIRE = register(new CampfireBlock("campfire", builder().setBlockEntity(BlockEntityType.CAMPFIRE).destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(LIT)
        .booleanState(SIGNAL_FIRE)
        .booleanState(WATERLOGGED)));
    public static final Block SOUL_CAMPFIRE = register(new Block("soul_campfire", builder().setBlockEntity(BlockEntityType.CAMPFIRE).destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(LIT)
        .booleanState(SIGNAL_FIRE)
        .booleanState(WATERLOGGED)));
    public static final SweetBerryBushBlock SWEET_BERRY_BUSH = register(new SweetBerryBushBlock("sweet_berry_bush", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(AGE_3)));
    public static final Block WARPED_STEM = register(new Block("warped_stem", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block STRIPPED_WARPED_STEM = register(new Block("stripped_warped_stem", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block WARPED_HYPHAE = register(new Block("warped_hyphae", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block STRIPPED_WARPED_HYPHAE = register(new Block("stripped_warped_hyphae", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final NyliumBlock WARPED_NYLIUM = register(new NyliumBlock("warped_nylium", builder().requiresCorrectToolForDrops().destroyTime(0.4f).solidRender()));
    public static final FungusBlock WARPED_FUNGUS = register(new FungusBlock("warped_fungus", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final Block WARPED_WART_BLOCK = register(new Block("warped_wart_block", builder().destroyTime(1.0f).solidRender()));
    public static final BushBlock WARPED_ROOTS = register(new BushBlock("warped_roots", builder().pushReaction(PistonBehavior.DESTROY).replaceable().notSolid()));
    public static final BushBlock NETHER_SPROUTS = register(new BushBlock("nether_sprouts", builder().pushReaction(PistonBehavior.DESTROY).replaceable().notSolid()));
    public static final Block CRIMSON_STEM = register(new Block("crimson_stem", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block STRIPPED_CRIMSON_STEM = register(new Block("stripped_crimson_stem", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block CRIMSON_HYPHAE = register(new Block("crimson_hyphae", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block STRIPPED_CRIMSON_HYPHAE = register(new Block("stripped_crimson_hyphae", builder().destroyTime(2.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final NyliumBlock CRIMSON_NYLIUM = register(new NyliumBlock("crimson_nylium", builder().requiresCorrectToolForDrops().destroyTime(0.4f).solidRender()));
    public static final FungusBlock CRIMSON_FUNGUS = register(new FungusBlock("crimson_fungus", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final Block SHROOMLIGHT = register(new Block("shroomlight", builder().destroyTime(1.0f).solidRender()));
    public static final WeepingVinesBlock WEEPING_VINES = register(new WeepingVinesBlock("weeping_vines", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(AGE_25)));
    public static final WeepingVinesPlantBlock WEEPING_VINES_PLANT = register(new WeepingVinesPlantBlock("weeping_vines_plant", builder().pushReaction(PistonBehavior.DESTROY).notSolid().pickItem(() -> Items.WEEPING_VINES)));
    public static final TwistingVinesBlock TWISTING_VINES = register(new TwistingVinesBlock("twisting_vines", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(AGE_25)));
    public static final TwistingVinesPlantBlock TWISTING_VINES_PLANT = register(new TwistingVinesPlantBlock("twisting_vines_plant", builder().pushReaction(PistonBehavior.DESTROY).notSolid().pickItem(() -> Items.TWISTING_VINES)));
    public static final BushBlock CRIMSON_ROOTS = register(new BushBlock("crimson_roots", builder().pushReaction(PistonBehavior.DESTROY).replaceable().notSolid()));
    public static final Block CRIMSON_PLANKS = register(new Block("crimson_planks", builder().destroyTime(2.0f).solidRender()));
    public static final Block WARPED_PLANKS = register(new Block("warped_planks", builder().destroyTime(2.0f).solidRender()));
    public static final Block CRIMSON_SLAB = register(new Block("crimson_slab", builder().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block WARPED_SLAB = register(new Block("warped_slab", builder().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block CRIMSON_PRESSURE_PLATE = register(new Block("crimson_pressure_plate", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY)
        .booleanState(POWERED)));
    public static final Block WARPED_PRESSURE_PLATE = register(new Block("warped_pressure_plate", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY)
        .booleanState(POWERED)));
    public static final Block CRIMSON_FENCE = register(new Block("crimson_fence", builder().destroyTime(2.0f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block WARPED_FENCE = register(new Block("warped_fence", builder().destroyTime(2.0f)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final TrapDoorBlock CRIMSON_TRAPDOOR = register(new TrapDoorBlock("crimson_trapdoor", builder().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .booleanState(OPEN)
        .booleanState(POWERED)
        .booleanState(WATERLOGGED)));
    public static final TrapDoorBlock WARPED_TRAPDOOR = register(new TrapDoorBlock("warped_trapdoor", builder().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .booleanState(OPEN)
        .booleanState(POWERED)
        .booleanState(WATERLOGGED)));
    public static final Block CRIMSON_FENCE_GATE = register(new Block("crimson_fence_gate", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(IN_WALL)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final Block WARPED_FENCE_GATE = register(new Block("warped_fence_gate", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(IN_WALL)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final Block CRIMSON_STAIRS = register(new Block("crimson_stairs", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block WARPED_STAIRS = register(new Block("warped_stairs", builder().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block CRIMSON_BUTTON = register(new Block("crimson_button", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(ATTACH_FACE)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(POWERED)));
    public static final Block WARPED_BUTTON = register(new Block("warped_button", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(ATTACH_FACE)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(POWERED)));
    public static final DoorBlock CRIMSON_DOOR = register(new DoorBlock("crimson_door", builder().destroyTime(3.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(DOUBLE_BLOCK_HALF)
        .enumState(DOOR_HINGE)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final DoorBlock WARPED_DOOR = register(new DoorBlock("warped_door", builder().destroyTime(3.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(DOUBLE_BLOCK_HALF)
        .enumState(DOOR_HINGE)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final Block CRIMSON_SIGN = register(new Block("crimson_sign", builder().setBlockEntity(BlockEntityType.SIGN).destroyTime(1.0f)
        .intState(ROTATION_16)
        .booleanState(WATERLOGGED)));
    public static final Block WARPED_SIGN = register(new Block("warped_sign", builder().setBlockEntity(BlockEntityType.SIGN).destroyTime(1.0f)
        .intState(ROTATION_16)
        .booleanState(WATERLOGGED)));
    public static final Block CRIMSON_WALL_SIGN = register(new Block("crimson_wall_sign", builder().setBlockEntity(BlockEntityType.SIGN).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block WARPED_WALL_SIGN = register(new Block("warped_wall_sign", builder().setBlockEntity(BlockEntityType.SIGN).destroyTime(1.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final StructureBlock STRUCTURE_BLOCK = register(new StructureBlock("structure_block", builder().setBlockEntity(BlockEntityType.STRUCTURE_BLOCK).requiresCorrectToolForDrops().destroyTime(-1.0f).solidRender()
        .enumState(STRUCTUREBLOCK_MODE)));
    public static final JigsawBlock JIGSAW = register(new JigsawBlock("jigsaw", builder().setBlockEntity(BlockEntityType.JIGSAW).requiresCorrectToolForDrops().destroyTime(-1.0f).solidRender()
        .enumState(ORIENTATION, FrontAndTop.VALUES)));
    public static final ComposterBlock COMPOSTER = register(new ComposterBlock("composter", builder().destroyTime(0.6f)
        .intState(LEVEL_COMPOSTER)));
    public static final Block TARGET = register(new Block("target", builder().destroyTime(0.5f).solidRender()
        .intState(POWER)));
    public static final BeehiveBlock BEE_NEST = register(new BeehiveBlock("bee_nest", builder().setBlockEntity(BlockEntityType.BEEHIVE).destroyTime(0.3f).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .intState(LEVEL_HONEY)));
    public static final Block BEEHIVE = register(new Block("beehive", builder().setBlockEntity(BlockEntityType.BEEHIVE).destroyTime(0.6f).solidRender()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .intState(LEVEL_HONEY)));
    public static final Block HONEY_BLOCK = register(new Block("honey_block", builder()));
    public static final Block HONEYCOMB_BLOCK = register(new Block("honeycomb_block", builder().destroyTime(0.6f).solidRender()));
    public static final Block NETHERITE_BLOCK = register(new Block("netherite_block", builder().requiresCorrectToolForDrops().destroyTime(50.0f).solidRender()));
    public static final Block ANCIENT_DEBRIS = register(new Block("ancient_debris", builder().requiresCorrectToolForDrops().destroyTime(30.0f).solidRender()));
    public static final Block CRYING_OBSIDIAN = register(new Block("crying_obsidian", builder().requiresCorrectToolForDrops().destroyTime(50.0f).solidRender()));
    public static final RespawnAnchorBlock RESPAWN_ANCHOR = register(new RespawnAnchorBlock("respawn_anchor", builder().requiresCorrectToolForDrops().destroyTime(50.0f).solidRender()
        .intState(RESPAWN_ANCHOR_CHARGES)));
    public static final FlowerPotBlock POTTED_CRIMSON_FUNGUS = register(new FlowerPotBlock("potted_crimson_fungus", CRIMSON_FUNGUS, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_WARPED_FUNGUS = register(new FlowerPotBlock("potted_warped_fungus", WARPED_FUNGUS, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_CRIMSON_ROOTS = register(new FlowerPotBlock("potted_crimson_roots", CRIMSON_ROOTS, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_WARPED_ROOTS = register(new FlowerPotBlock("potted_warped_roots", WARPED_ROOTS, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final Block LODESTONE = register(new Block("lodestone", builder().requiresCorrectToolForDrops().destroyTime(3.5f).pushReaction(PistonBehavior.BLOCK).solidRender()));
    public static final Block BLACKSTONE = register(new Block("blackstone", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block BLACKSTONE_STAIRS = register(new Block("blackstone_stairs", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block BLACKSTONE_WALL = register(new Block("blackstone_wall", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block BLACKSTONE_SLAB = register(new Block("blackstone_slab", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block POLISHED_BLACKSTONE = register(new Block("polished_blackstone", builder().requiresCorrectToolForDrops().destroyTime(2.0f).solidRender()));
    public static final Block POLISHED_BLACKSTONE_BRICKS = register(new Block("polished_blackstone_bricks", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block CRACKED_POLISHED_BLACKSTONE_BRICKS = register(new Block("cracked_polished_blackstone_bricks", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block CHISELED_POLISHED_BLACKSTONE = register(new Block("chiseled_polished_blackstone", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block POLISHED_BLACKSTONE_BRICK_SLAB = register(new Block("polished_blackstone_brick_slab", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block POLISHED_BLACKSTONE_BRICK_STAIRS = register(new Block("polished_blackstone_brick_stairs", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block POLISHED_BLACKSTONE_BRICK_WALL = register(new Block("polished_blackstone_brick_wall", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block GILDED_BLACKSTONE = register(new Block("gilded_blackstone", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block POLISHED_BLACKSTONE_STAIRS = register(new Block("polished_blackstone_stairs", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block POLISHED_BLACKSTONE_SLAB = register(new Block("polished_blackstone_slab", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block POLISHED_BLACKSTONE_PRESSURE_PLATE = register(new Block("polished_blackstone_pressure_plate", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY)
        .booleanState(POWERED)));
    public static final Block POLISHED_BLACKSTONE_BUTTON = register(new Block("polished_blackstone_button", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(ATTACH_FACE)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(POWERED)));
    public static final Block POLISHED_BLACKSTONE_WALL = register(new Block("polished_blackstone_wall", builder().requiresCorrectToolForDrops().destroyTime(2.0f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block CHISELED_NETHER_BRICKS = register(new Block("chiseled_nether_bricks", builder().requiresCorrectToolForDrops().destroyTime(2.0f).solidRender()));
    public static final Block CRACKED_NETHER_BRICKS = register(new Block("cracked_nether_bricks", builder().requiresCorrectToolForDrops().destroyTime(2.0f).solidRender()));
    public static final Block QUARTZ_BRICKS = register(new Block("quartz_bricks", builder().requiresCorrectToolForDrops().destroyTime(0.8f).solidRender()));
    public static final CandleBlock CANDLE = register(new CandleBlock("candle", builder().destroyTime(0.1f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(CANDLES)
        .booleanState(LIT)
        .booleanState(WATERLOGGED)));
    public static final Block WHITE_CANDLE = register(new Block("white_candle", builder().destroyTime(0.1f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(CANDLES)
        .booleanState(LIT)
        .booleanState(WATERLOGGED)));
    public static final Block ORANGE_CANDLE = register(new Block("orange_candle", builder().destroyTime(0.1f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(CANDLES)
        .booleanState(LIT)
        .booleanState(WATERLOGGED)));
    public static final Block MAGENTA_CANDLE = register(new Block("magenta_candle", builder().destroyTime(0.1f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(CANDLES)
        .booleanState(LIT)
        .booleanState(WATERLOGGED)));
    public static final Block LIGHT_BLUE_CANDLE = register(new Block("light_blue_candle", builder().destroyTime(0.1f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(CANDLES)
        .booleanState(LIT)
        .booleanState(WATERLOGGED)));
    public static final Block YELLOW_CANDLE = register(new Block("yellow_candle", builder().destroyTime(0.1f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(CANDLES)
        .booleanState(LIT)
        .booleanState(WATERLOGGED)));
    public static final Block LIME_CANDLE = register(new Block("lime_candle", builder().destroyTime(0.1f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(CANDLES)
        .booleanState(LIT)
        .booleanState(WATERLOGGED)));
    public static final Block PINK_CANDLE = register(new Block("pink_candle", builder().destroyTime(0.1f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(CANDLES)
        .booleanState(LIT)
        .booleanState(WATERLOGGED)));
    public static final Block GRAY_CANDLE = register(new Block("gray_candle", builder().destroyTime(0.1f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(CANDLES)
        .booleanState(LIT)
        .booleanState(WATERLOGGED)));
    public static final Block LIGHT_GRAY_CANDLE = register(new Block("light_gray_candle", builder().destroyTime(0.1f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(CANDLES)
        .booleanState(LIT)
        .booleanState(WATERLOGGED)));
    public static final Block CYAN_CANDLE = register(new Block("cyan_candle", builder().destroyTime(0.1f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(CANDLES)
        .booleanState(LIT)
        .booleanState(WATERLOGGED)));
    public static final Block PURPLE_CANDLE = register(new Block("purple_candle", builder().destroyTime(0.1f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(CANDLES)
        .booleanState(LIT)
        .booleanState(WATERLOGGED)));
    public static final Block BLUE_CANDLE = register(new Block("blue_candle", builder().destroyTime(0.1f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(CANDLES)
        .booleanState(LIT)
        .booleanState(WATERLOGGED)));
    public static final Block BROWN_CANDLE = register(new Block("brown_candle", builder().destroyTime(0.1f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(CANDLES)
        .booleanState(LIT)
        .booleanState(WATERLOGGED)));
    public static final Block GREEN_CANDLE = register(new Block("green_candle", builder().destroyTime(0.1f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(CANDLES)
        .booleanState(LIT)
        .booleanState(WATERLOGGED)));
    public static final Block RED_CANDLE = register(new Block("red_candle", builder().destroyTime(0.1f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(CANDLES)
        .booleanState(LIT)
        .booleanState(WATERLOGGED)));
    public static final Block BLACK_CANDLE = register(new Block("black_candle", builder().destroyTime(0.1f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(CANDLES)
        .booleanState(LIT)
        .booleanState(WATERLOGGED)));
    public static final CandleCakeBlock CANDLE_CAKE = register(new CandleCakeBlock("candle_cake", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).pickItem(() -> Items.CAKE)
        .booleanState(LIT)));
    public static final Block WHITE_CANDLE_CAKE = register(new Block("white_candle_cake", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).pickItem(() -> Items.CAKE)
        .booleanState(LIT)));
    public static final Block ORANGE_CANDLE_CAKE = register(new Block("orange_candle_cake", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).pickItem(() -> Items.CAKE)
        .booleanState(LIT)));
    public static final Block MAGENTA_CANDLE_CAKE = register(new Block("magenta_candle_cake", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).pickItem(() -> Items.CAKE)
        .booleanState(LIT)));
    public static final Block LIGHT_BLUE_CANDLE_CAKE = register(new Block("light_blue_candle_cake", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).pickItem(() -> Items.CAKE)
        .booleanState(LIT)));
    public static final Block YELLOW_CANDLE_CAKE = register(new Block("yellow_candle_cake", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).pickItem(() -> Items.CAKE)
        .booleanState(LIT)));
    public static final Block LIME_CANDLE_CAKE = register(new Block("lime_candle_cake", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).pickItem(() -> Items.CAKE)
        .booleanState(LIT)));
    public static final Block PINK_CANDLE_CAKE = register(new Block("pink_candle_cake", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).pickItem(() -> Items.CAKE)
        .booleanState(LIT)));
    public static final Block GRAY_CANDLE_CAKE = register(new Block("gray_candle_cake", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).pickItem(() -> Items.CAKE)
        .booleanState(LIT)));
    public static final Block LIGHT_GRAY_CANDLE_CAKE = register(new Block("light_gray_candle_cake", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).pickItem(() -> Items.CAKE)
        .booleanState(LIT)));
    public static final Block CYAN_CANDLE_CAKE = register(new Block("cyan_candle_cake", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).pickItem(() -> Items.CAKE)
        .booleanState(LIT)));
    public static final Block PURPLE_CANDLE_CAKE = register(new Block("purple_candle_cake", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).pickItem(() -> Items.CAKE)
        .booleanState(LIT)));
    public static final Block BLUE_CANDLE_CAKE = register(new Block("blue_candle_cake", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).pickItem(() -> Items.CAKE)
        .booleanState(LIT)));
    public static final Block BROWN_CANDLE_CAKE = register(new Block("brown_candle_cake", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).pickItem(() -> Items.CAKE)
        .booleanState(LIT)));
    public static final Block GREEN_CANDLE_CAKE = register(new Block("green_candle_cake", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).pickItem(() -> Items.CAKE)
        .booleanState(LIT)));
    public static final Block RED_CANDLE_CAKE = register(new Block("red_candle_cake", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).pickItem(() -> Items.CAKE)
        .booleanState(LIT)));
    public static final Block BLACK_CANDLE_CAKE = register(new Block("black_candle_cake", builder().destroyTime(0.5f).pushReaction(PistonBehavior.DESTROY).pickItem(() -> Items.CAKE)
        .booleanState(LIT)));
    public static final Block AMETHYST_BLOCK = register(new Block("amethyst_block", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block BUDDING_AMETHYST = register(new Block("budding_amethyst", builder().requiresCorrectToolForDrops().destroyTime(1.5f).pushReaction(PistonBehavior.DESTROY).solidRender()));
    public static final Block AMETHYST_CLUSTER = register(new Block("amethyst_cluster", builder().destroyTime(1.5f).pushReaction(PistonBehavior.DESTROY)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)
        .booleanState(WATERLOGGED)));
    public static final Block LARGE_AMETHYST_BUD = register(new Block("large_amethyst_bud", builder().destroyTime(1.5f).pushReaction(PistonBehavior.DESTROY)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)
        .booleanState(WATERLOGGED)));
    public static final Block MEDIUM_AMETHYST_BUD = register(new Block("medium_amethyst_bud", builder().destroyTime(1.5f).pushReaction(PistonBehavior.DESTROY)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)
        .booleanState(WATERLOGGED)));
    public static final Block SMALL_AMETHYST_BUD = register(new Block("small_amethyst_bud", builder().destroyTime(1.5f).pushReaction(PistonBehavior.DESTROY)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)
        .booleanState(WATERLOGGED)));
    public static final Block TUFF = register(new Block("tuff", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block TUFF_SLAB = register(new Block("tuff_slab", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block TUFF_STAIRS = register(new Block("tuff_stairs", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block TUFF_WALL = register(new Block("tuff_wall", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block POLISHED_TUFF = register(new Block("polished_tuff", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block POLISHED_TUFF_SLAB = register(new Block("polished_tuff_slab", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block POLISHED_TUFF_STAIRS = register(new Block("polished_tuff_stairs", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block POLISHED_TUFF_WALL = register(new Block("polished_tuff_wall", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block CHISELED_TUFF = register(new Block("chiseled_tuff", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block TUFF_BRICKS = register(new Block("tuff_bricks", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block TUFF_BRICK_SLAB = register(new Block("tuff_brick_slab", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block TUFF_BRICK_STAIRS = register(new Block("tuff_brick_stairs", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block TUFF_BRICK_WALL = register(new Block("tuff_brick_wall", builder().requiresCorrectToolForDrops().destroyTime(1.5f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block CHISELED_TUFF_BRICKS = register(new Block("chiseled_tuff_bricks", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final Block CALCITE = register(new Block("calcite", builder().requiresCorrectToolForDrops().destroyTime(0.75f).solidRender()));
    public static final Block TINTED_GLASS = register(new Block("tinted_glass", builder().destroyTime(0.3f)));
    public static final Block POWDER_SNOW = register(new Block("powder_snow", builder().destroyTime(0.25f).notSolid()));
    public static final Block SCULK_SENSOR = register(new Block("sculk_sensor", builder().setBlockEntity(BlockEntityType.SCULK_SENSOR).destroyTime(1.5f)
        .intState(POWER)
        .enumState(SCULK_SENSOR_PHASE)
        .booleanState(WATERLOGGED)));
    public static final Block CALIBRATED_SCULK_SENSOR = register(new Block("calibrated_sculk_sensor", builder().setBlockEntity(BlockEntityType.CALIBRATED_SCULK_SENSOR).destroyTime(1.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .intState(POWER)
        .enumState(SCULK_SENSOR_PHASE)
        .booleanState(WATERLOGGED)));
    public static final Block SCULK = register(new Block("sculk", builder().destroyTime(0.2f).solidRender()));
    public static final Block SCULK_VEIN = register(new Block("sculk_vein", builder().destroyTime(0.2f).pushReaction(PistonBehavior.DESTROY)
        .booleanState(DOWN)
        .booleanState(EAST)
        .booleanState(NORTH)
        .booleanState(SOUTH)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .booleanState(WEST)));
    public static final Block SCULK_CATALYST = register(new Block("sculk_catalyst", builder().setBlockEntity(BlockEntityType.SCULK_CATALYST).destroyTime(3.0f).solidRender()
        .booleanState(BLOOM)));
    public static final Block SCULK_SHRIEKER = register(new Block("sculk_shrieker", builder().setBlockEntity(BlockEntityType.SCULK_SHRIEKER).destroyTime(3.0f)
        .booleanState(CAN_SUMMON)
        .booleanState(SHRIEKING)
        .booleanState(WATERLOGGED)));
    public static final Block COPPER_BLOCK = register(new Block("copper_block", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block EXPOSED_COPPER = register(new Block("exposed_copper", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block WEATHERED_COPPER = register(new Block("weathered_copper", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block OXIDIZED_COPPER = register(new Block("oxidized_copper", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block COPPER_ORE = register(new Block("copper_ore", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block DEEPSLATE_COPPER_ORE = register(new Block("deepslate_copper_ore", builder().requiresCorrectToolForDrops().destroyTime(4.5f).solidRender()));
    public static final Block OXIDIZED_CUT_COPPER = register(new Block("oxidized_cut_copper", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block WEATHERED_CUT_COPPER = register(new Block("weathered_cut_copper", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block EXPOSED_CUT_COPPER = register(new Block("exposed_cut_copper", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block CUT_COPPER = register(new Block("cut_copper", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block OXIDIZED_CHISELED_COPPER = register(new Block("oxidized_chiseled_copper", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block WEATHERED_CHISELED_COPPER = register(new Block("weathered_chiseled_copper", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block EXPOSED_CHISELED_COPPER = register(new Block("exposed_chiseled_copper", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block CHISELED_COPPER = register(new Block("chiseled_copper", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block WAXED_OXIDIZED_CHISELED_COPPER = register(new Block("waxed_oxidized_chiseled_copper", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block WAXED_WEATHERED_CHISELED_COPPER = register(new Block("waxed_weathered_chiseled_copper", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block WAXED_EXPOSED_CHISELED_COPPER = register(new Block("waxed_exposed_chiseled_copper", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block WAXED_CHISELED_COPPER = register(new Block("waxed_chiseled_copper", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block OXIDIZED_CUT_COPPER_STAIRS = register(new Block("oxidized_cut_copper_stairs", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block WEATHERED_CUT_COPPER_STAIRS = register(new Block("weathered_cut_copper_stairs", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block EXPOSED_CUT_COPPER_STAIRS = register(new Block("exposed_cut_copper_stairs", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block CUT_COPPER_STAIRS = register(new Block("cut_copper_stairs", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block OXIDIZED_CUT_COPPER_SLAB = register(new Block("oxidized_cut_copper_slab", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block WEATHERED_CUT_COPPER_SLAB = register(new Block("weathered_cut_copper_slab", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block EXPOSED_CUT_COPPER_SLAB = register(new Block("exposed_cut_copper_slab", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block CUT_COPPER_SLAB = register(new Block("cut_copper_slab", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block WAXED_COPPER_BLOCK = register(new Block("waxed_copper_block", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block WAXED_WEATHERED_COPPER = register(new Block("waxed_weathered_copper", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block WAXED_EXPOSED_COPPER = register(new Block("waxed_exposed_copper", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block WAXED_OXIDIZED_COPPER = register(new Block("waxed_oxidized_copper", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block WAXED_OXIDIZED_CUT_COPPER = register(new Block("waxed_oxidized_cut_copper", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block WAXED_WEATHERED_CUT_COPPER = register(new Block("waxed_weathered_cut_copper", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block WAXED_EXPOSED_CUT_COPPER = register(new Block("waxed_exposed_cut_copper", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block WAXED_CUT_COPPER = register(new Block("waxed_cut_copper", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()));
    public static final Block WAXED_OXIDIZED_CUT_COPPER_STAIRS = register(new Block("waxed_oxidized_cut_copper_stairs", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block WAXED_WEATHERED_CUT_COPPER_STAIRS = register(new Block("waxed_weathered_cut_copper_stairs", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block WAXED_EXPOSED_CUT_COPPER_STAIRS = register(new Block("waxed_exposed_cut_copper_stairs", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block WAXED_CUT_COPPER_STAIRS = register(new Block("waxed_cut_copper_stairs", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block WAXED_OXIDIZED_CUT_COPPER_SLAB = register(new Block("waxed_oxidized_cut_copper_slab", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block WAXED_WEATHERED_CUT_COPPER_SLAB = register(new Block("waxed_weathered_cut_copper_slab", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block WAXED_EXPOSED_CUT_COPPER_SLAB = register(new Block("waxed_exposed_cut_copper_slab", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block WAXED_CUT_COPPER_SLAB = register(new Block("waxed_cut_copper_slab", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final DoorBlock COPPER_DOOR = register(new DoorBlock("copper_door", builder().destroyTime(3.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(DOUBLE_BLOCK_HALF)
        .enumState(DOOR_HINGE)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final DoorBlock EXPOSED_COPPER_DOOR = register(new DoorBlock("exposed_copper_door", builder().destroyTime(3.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(DOUBLE_BLOCK_HALF)
        .enumState(DOOR_HINGE)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final DoorBlock OXIDIZED_COPPER_DOOR = register(new DoorBlock("oxidized_copper_door", builder().destroyTime(3.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(DOUBLE_BLOCK_HALF)
        .enumState(DOOR_HINGE)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final DoorBlock WEATHERED_COPPER_DOOR = register(new DoorBlock("weathered_copper_door", builder().destroyTime(3.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(DOUBLE_BLOCK_HALF)
        .enumState(DOOR_HINGE)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final DoorBlock WAXED_COPPER_DOOR = register(new DoorBlock("waxed_copper_door", builder().destroyTime(3.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(DOUBLE_BLOCK_HALF)
        .enumState(DOOR_HINGE)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final DoorBlock WAXED_EXPOSED_COPPER_DOOR = register(new DoorBlock("waxed_exposed_copper_door", builder().destroyTime(3.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(DOUBLE_BLOCK_HALF)
        .enumState(DOOR_HINGE)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final DoorBlock WAXED_OXIDIZED_COPPER_DOOR = register(new DoorBlock("waxed_oxidized_copper_door", builder().destroyTime(3.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(DOUBLE_BLOCK_HALF)
        .enumState(DOOR_HINGE)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final DoorBlock WAXED_WEATHERED_COPPER_DOOR = register(new DoorBlock("waxed_weathered_copper_door", builder().destroyTime(3.0f).pushReaction(PistonBehavior.DESTROY)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(DOUBLE_BLOCK_HALF)
        .enumState(DOOR_HINGE)
        .booleanState(OPEN)
        .booleanState(POWERED)));
    public static final TrapDoorBlock COPPER_TRAPDOOR = register(new TrapDoorBlock("copper_trapdoor", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .booleanState(OPEN)
        .booleanState(POWERED)
        .booleanState(WATERLOGGED)));
    public static final TrapDoorBlock EXPOSED_COPPER_TRAPDOOR = register(new TrapDoorBlock("exposed_copper_trapdoor", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .booleanState(OPEN)
        .booleanState(POWERED)
        .booleanState(WATERLOGGED)));
    public static final TrapDoorBlock OXIDIZED_COPPER_TRAPDOOR = register(new TrapDoorBlock("oxidized_copper_trapdoor", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .booleanState(OPEN)
        .booleanState(POWERED)
        .booleanState(WATERLOGGED)));
    public static final TrapDoorBlock WEATHERED_COPPER_TRAPDOOR = register(new TrapDoorBlock("weathered_copper_trapdoor", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .booleanState(OPEN)
        .booleanState(POWERED)
        .booleanState(WATERLOGGED)));
    public static final TrapDoorBlock WAXED_COPPER_TRAPDOOR = register(new TrapDoorBlock("waxed_copper_trapdoor", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .booleanState(OPEN)
        .booleanState(POWERED)
        .booleanState(WATERLOGGED)));
    public static final TrapDoorBlock WAXED_EXPOSED_COPPER_TRAPDOOR = register(new TrapDoorBlock("waxed_exposed_copper_trapdoor", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .booleanState(OPEN)
        .booleanState(POWERED)
        .booleanState(WATERLOGGED)));
    public static final TrapDoorBlock WAXED_OXIDIZED_COPPER_TRAPDOOR = register(new TrapDoorBlock("waxed_oxidized_copper_trapdoor", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .booleanState(OPEN)
        .booleanState(POWERED)
        .booleanState(WATERLOGGED)));
    public static final TrapDoorBlock WAXED_WEATHERED_COPPER_TRAPDOOR = register(new TrapDoorBlock("waxed_weathered_copper_trapdoor", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .booleanState(OPEN)
        .booleanState(POWERED)
        .booleanState(WATERLOGGED)));
    public static final Block COPPER_GRATE = register(new Block("copper_grate", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .booleanState(WATERLOGGED)));
    public static final Block EXPOSED_COPPER_GRATE = register(new Block("exposed_copper_grate", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .booleanState(WATERLOGGED)));
    public static final Block WEATHERED_COPPER_GRATE = register(new Block("weathered_copper_grate", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .booleanState(WATERLOGGED)));
    public static final Block OXIDIZED_COPPER_GRATE = register(new Block("oxidized_copper_grate", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .booleanState(WATERLOGGED)));
    public static final Block WAXED_COPPER_GRATE = register(new Block("waxed_copper_grate", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .booleanState(WATERLOGGED)));
    public static final Block WAXED_EXPOSED_COPPER_GRATE = register(new Block("waxed_exposed_copper_grate", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .booleanState(WATERLOGGED)));
    public static final Block WAXED_WEATHERED_COPPER_GRATE = register(new Block("waxed_weathered_copper_grate", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .booleanState(WATERLOGGED)));
    public static final Block WAXED_OXIDIZED_COPPER_GRATE = register(new Block("waxed_oxidized_copper_grate", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .booleanState(WATERLOGGED)));
    public static final Block COPPER_BULB = register(new Block("copper_bulb", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()
        .booleanState(LIT)
        .booleanState(POWERED)));
    public static final Block EXPOSED_COPPER_BULB = register(new Block("exposed_copper_bulb", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()
        .booleanState(LIT)
        .booleanState(POWERED)));
    public static final Block WEATHERED_COPPER_BULB = register(new Block("weathered_copper_bulb", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()
        .booleanState(LIT)
        .booleanState(POWERED)));
    public static final Block OXIDIZED_COPPER_BULB = register(new Block("oxidized_copper_bulb", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()
        .booleanState(LIT)
        .booleanState(POWERED)));
    public static final Block WAXED_COPPER_BULB = register(new Block("waxed_copper_bulb", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()
        .booleanState(LIT)
        .booleanState(POWERED)));
    public static final Block WAXED_EXPOSED_COPPER_BULB = register(new Block("waxed_exposed_copper_bulb", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()
        .booleanState(LIT)
        .booleanState(POWERED)));
    public static final Block WAXED_WEATHERED_COPPER_BULB = register(new Block("waxed_weathered_copper_bulb", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()
        .booleanState(LIT)
        .booleanState(POWERED)));
    public static final Block WAXED_OXIDIZED_COPPER_BULB = register(new Block("waxed_oxidized_copper_bulb", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()
        .booleanState(LIT)
        .booleanState(POWERED)));
    public static final Block LIGHTNING_ROD = register(new Block("lightning_rod", builder().requiresCorrectToolForDrops().destroyTime(3.0f)
        .enumState(FACING, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN)
        .booleanState(POWERED)
        .booleanState(WATERLOGGED)));
    public static final Block POINTED_DRIPSTONE = register(new Block("pointed_dripstone", builder().destroyTime(1.5f).pushReaction(PistonBehavior.DESTROY)
        .enumState(DRIPSTONE_THICKNESS)
        .enumState(VERTICAL_DIRECTION, Direction.UP, Direction.DOWN)
        .booleanState(WATERLOGGED)));
    public static final Block DRIPSTONE_BLOCK = register(new Block("dripstone_block", builder().requiresCorrectToolForDrops().destroyTime(1.5f).solidRender()));
    public static final CaveVinesBlock CAVE_VINES = register(new CaveVinesBlock("cave_vines", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .intState(AGE_25)
        .booleanState(BERRIES)));
    public static final CaveVinesPlantBlock CAVE_VINES_PLANT = register(new CaveVinesPlantBlock("cave_vines_plant", builder().pushReaction(PistonBehavior.DESTROY).notSolid().pickItem(() -> Items.GLOW_BERRIES)
        .booleanState(BERRIES)));
    public static final Block SPORE_BLOSSOM = register(new Block("spore_blossom", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final AzaleaBlock AZALEA = register(new AzaleaBlock("azalea", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final AzaleaBlock FLOWERING_AZALEA = register(new AzaleaBlock("flowering_azalea", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final Block MOSS_CARPET = register(new Block("moss_carpet", builder().destroyTime(0.1f).pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final PinkPetalsBlock PINK_PETALS = register(new PinkPetalsBlock("pink_petals", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .intState(FLOWER_AMOUNT)));
    public static final MossBlock MOSS_BLOCK = register(new MossBlock("moss_block", builder().destroyTime(0.1f).pushReaction(PistonBehavior.DESTROY).solidRender()));
    public static final BigDripleafBlock BIG_DRIPLEAF = register(new BigDripleafBlock("big_dripleaf", builder().destroyTime(0.1f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(TILT)
        .booleanState(WATERLOGGED)));
    public static final BigDripleafStemBlock BIG_DRIPLEAF_STEM = register(new BigDripleafStemBlock("big_dripleaf_stem", builder().destroyTime(0.1f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final SmallDripleafBlock SMALL_DRIPLEAF = register(new SmallDripleafBlock("small_dripleaf", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(DOUBLE_BLOCK_HALF)
        .booleanState(WATERLOGGED)));
    public static final Block HANGING_ROOTS = register(new Block("hanging_roots", builder().pushReaction(PistonBehavior.DESTROY).replaceable().notSolid()
        .booleanState(WATERLOGGED)));
    public static final RootedDirtBlock ROOTED_DIRT = register(new RootedDirtBlock("rooted_dirt", builder().destroyTime(0.5f).solidRender()));
    public static final Block MUD = register(new Block("mud", builder().destroyTime(0.5f).solidRender()));
    public static final Block DEEPSLATE = register(new Block("deepslate", builder().requiresCorrectToolForDrops().destroyTime(3.0f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block COBBLED_DEEPSLATE = register(new Block("cobbled_deepslate", builder().requiresCorrectToolForDrops().destroyTime(3.5f).solidRender()));
    public static final Block COBBLED_DEEPSLATE_STAIRS = register(new Block("cobbled_deepslate_stairs", builder().requiresCorrectToolForDrops().destroyTime(3.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block COBBLED_DEEPSLATE_SLAB = register(new Block("cobbled_deepslate_slab", builder().requiresCorrectToolForDrops().destroyTime(3.5f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block COBBLED_DEEPSLATE_WALL = register(new Block("cobbled_deepslate_wall", builder().requiresCorrectToolForDrops().destroyTime(3.5f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block POLISHED_DEEPSLATE = register(new Block("polished_deepslate", builder().requiresCorrectToolForDrops().destroyTime(3.5f).solidRender()));
    public static final Block POLISHED_DEEPSLATE_STAIRS = register(new Block("polished_deepslate_stairs", builder().requiresCorrectToolForDrops().destroyTime(3.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block POLISHED_DEEPSLATE_SLAB = register(new Block("polished_deepslate_slab", builder().requiresCorrectToolForDrops().destroyTime(3.5f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block POLISHED_DEEPSLATE_WALL = register(new Block("polished_deepslate_wall", builder().requiresCorrectToolForDrops().destroyTime(3.5f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block DEEPSLATE_TILES = register(new Block("deepslate_tiles", builder().requiresCorrectToolForDrops().destroyTime(3.5f).solidRender()));
    public static final Block DEEPSLATE_TILE_STAIRS = register(new Block("deepslate_tile_stairs", builder().requiresCorrectToolForDrops().destroyTime(3.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block DEEPSLATE_TILE_SLAB = register(new Block("deepslate_tile_slab", builder().requiresCorrectToolForDrops().destroyTime(3.5f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block DEEPSLATE_TILE_WALL = register(new Block("deepslate_tile_wall", builder().requiresCorrectToolForDrops().destroyTime(3.5f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block DEEPSLATE_BRICKS = register(new Block("deepslate_bricks", builder().requiresCorrectToolForDrops().destroyTime(3.5f).solidRender()));
    public static final Block DEEPSLATE_BRICK_STAIRS = register(new Block("deepslate_brick_stairs", builder().requiresCorrectToolForDrops().destroyTime(3.5f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .enumState(HALF)
        .enumState(STAIRS_SHAPE)
        .booleanState(WATERLOGGED)));
    public static final Block DEEPSLATE_BRICK_SLAB = register(new Block("deepslate_brick_slab", builder().requiresCorrectToolForDrops().destroyTime(3.5f)
        .enumState(SLAB_TYPE)
        .booleanState(WATERLOGGED)));
    public static final Block DEEPSLATE_BRICK_WALL = register(new Block("deepslate_brick_wall", builder().requiresCorrectToolForDrops().destroyTime(3.5f)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .booleanState(UP)
        .booleanState(WATERLOGGED)
        .enumState(WEST_WALL)));
    public static final Block CHISELED_DEEPSLATE = register(new Block("chiseled_deepslate", builder().requiresCorrectToolForDrops().destroyTime(3.5f).solidRender()));
    public static final Block CRACKED_DEEPSLATE_BRICKS = register(new Block("cracked_deepslate_bricks", builder().requiresCorrectToolForDrops().destroyTime(3.5f).solidRender()));
    public static final Block CRACKED_DEEPSLATE_TILES = register(new Block("cracked_deepslate_tiles", builder().requiresCorrectToolForDrops().destroyTime(3.5f).solidRender()));
    public static final Block INFESTED_DEEPSLATE = register(new Block("infested_deepslate", builder().destroyTime(1.5f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block SMOOTH_BASALT = register(new Block("smooth_basalt", builder().requiresCorrectToolForDrops().destroyTime(1.25f).solidRender()));
    public static final Block RAW_IRON_BLOCK = register(new Block("raw_iron_block", builder().requiresCorrectToolForDrops().destroyTime(5.0f).solidRender()));
    public static final Block RAW_COPPER_BLOCK = register(new Block("raw_copper_block", builder().requiresCorrectToolForDrops().destroyTime(5.0f).solidRender()));
    public static final Block RAW_GOLD_BLOCK = register(new Block("raw_gold_block", builder().requiresCorrectToolForDrops().destroyTime(5.0f).solidRender()));
    public static final FlowerPotBlock POTTED_AZALEA_BUSH = register(new FlowerPotBlock("potted_azalea_bush", AZALEA, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_FLOWERING_AZALEA_BUSH = register(new FlowerPotBlock("potted_flowering_azalea_bush", FLOWERING_AZALEA, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final Block OCHRE_FROGLIGHT = register(new Block("ochre_froglight", builder().destroyTime(0.3f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block VERDANT_FROGLIGHT = register(new Block("verdant_froglight", builder().destroyTime(0.3f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block PEARLESCENT_FROGLIGHT = register(new Block("pearlescent_froglight", builder().destroyTime(0.3f).solidRender()
        .enumState(AXIS, Axis.VALUES)));
    public static final Block FROGSPAWN = register(new Block("frogspawn", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final Block REINFORCED_DEEPSLATE = register(new Block("reinforced_deepslate", builder().destroyTime(55.0f).solidRender()));
    public static final DecoratedPotBlock DECORATED_POT = register(new DecoratedPotBlock("decorated_pot", builder().setBlockEntity(BlockEntityType.DECORATED_POT).pushReaction(PistonBehavior.DESTROY)
        .booleanState(CRACKED)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(WATERLOGGED)));
    public static final Block CRAFTER = register(new Block("crafter", builder().setBlockEntity(BlockEntityType.CRAFTER).destroyTime(1.5f).solidRender()
        .booleanState(CRAFTING)
        .enumState(ORIENTATION, FrontAndTop.VALUES)
        .booleanState(TRIGGERED)));
    public static final Block TRIAL_SPAWNER = register(new Block("trial_spawner", builder().setBlockEntity(BlockEntityType.TRIAL_SPAWNER).destroyTime(50.0f)
        .booleanState(OMINOUS)
        .enumState(TRIAL_SPAWNER_STATE)));
    public static final VaultBlock VAULT = register(new VaultBlock("vault", builder().setBlockEntity(BlockEntityType.VAULT).destroyTime(50.0f)
        .enumState(HORIZONTAL_FACING, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        .booleanState(OMINOUS)
        .enumState(VAULT_STATE)));
    public static final Block HEAVY_CORE = register(new Block("heavy_core", builder().destroyTime(10.0f).notSolid()
        .booleanState(WATERLOGGED)));
    public static final MossBlock PALE_MOSS_BLOCK = register(new MossBlock("pale_moss_block", builder().destroyTime(0.1f).pushReaction(PistonBehavior.DESTROY).solidRender()));
    public static final MossyCarpetBlock PALE_MOSS_CARPET = register(new MossyCarpetBlock("pale_moss_carpet", builder().destroyTime(0.1f).pushReaction(PistonBehavior.DESTROY).notSolid()
        .booleanState(BOTTOM)
        .enumState(EAST_WALL)
        .enumState(NORTH_WALL)
        .enumState(SOUTH_WALL)
        .enumState(WEST_WALL)));
    public static final HangingMossBlock PALE_HANGING_MOSS = register(new HangingMossBlock("pale_hanging_moss", builder().pushReaction(PistonBehavior.DESTROY).notSolid()
        .booleanState(TIP)));
    public static final BushBlock OPEN_EYEBLOSSOM = register(new BushBlock("open_eyeblossom", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final BushBlock CLOSED_EYEBLOSSOM = register(new BushBlock("closed_eyeblossom", builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_OPEN_EYEBLOSSOM = register(new FlowerPotBlock("potted_open_eyeblossom", OPEN_EYEBLOSSOM, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));
    public static final FlowerPotBlock POTTED_CLOSED_EYEBLOSSOM = register(new FlowerPotBlock("potted_closed_eyeblossom", CLOSED_EYEBLOSSOM, builder().pushReaction(PistonBehavior.DESTROY).notSolid()));

    private static <T extends Block> T register(T block) {
        block.setJavaId(BlockRegistries.JAVA_BLOCKS.get().size());
        BlockRegistries.JAVA_BLOCKS.get().add(block);
        return block;
    }

    private Blocks() {
    }
}
