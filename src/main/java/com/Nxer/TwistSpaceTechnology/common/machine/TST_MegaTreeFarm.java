package com.Nxer.TwistSpaceTechnology.common.machine;

import static com.Nxer.TwistSpaceTechnology.common.block.BasicBlocks.MetaBlockCasing01;
import static com.Nxer.TwistSpaceTechnology.util.TextLocalization.ModName;
import static com.Nxer.TwistSpaceTechnology.util.TextLocalization.textUseBlueprint;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlockAnyMeta;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofChain;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.enums.GT_HatchElement.Energy;
import static gregtech.api.enums.GT_HatchElement.ExoticEnergy;
import static gregtech.api.enums.GT_HatchElement.InputBus;
import static gregtech.api.enums.GT_HatchElement.OutputBus;
import static gregtech.api.util.GT_StructureUtility.ofFrame;
import static gtPlusPlus.xmod.gregtech.common.tileentities.machines.multi.production.GregtechMetaTileEntityTreeFarm.Mode;
import static gtPlusPlus.xmod.gregtech.common.tileentities.machines.multi.production.GregtechMetaTileEntityTreeFarm.treeProductsMap;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.Nxer.TwistSpaceTechnology.common.api.ModBlocksHandler;
import com.Nxer.TwistSpaceTechnology.common.machine.multiMachineClasses.GTCM_MultiMachineBase;
import com.Nxer.TwistSpaceTechnology.common.machine.multiMachineClasses.processingLogics.GTCM_ProcessingLogic;
import com.Nxer.TwistSpaceTechnology.common.recipeMap.GTCMRecipe;
import com.Nxer.TwistSpaceTechnology.util.LoaderReference;
import com.Nxer.TwistSpaceTechnology.util.TextEnums;
import com.github.bartimaeusnek.bartworks.API.BorosilicateGlass;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import forestry.api.arboriculture.ITree;
import forestry.api.arboriculture.TreeManager;
import galaxyspace.BarnardsSystem.BRFluids;
import gregtech.api.GregTech_API;
import gregtech.api.enums.Materials;
import gregtech.api.enums.Mods;
import gregtech.api.enums.TAE;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.SimpleCheckRecipeResult;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_HatchElementBuilder;
import gregtech.api.util.GT_ModHandler;
import gregtech.api.util.GT_Multiblock_Tooltip_Builder;
import gregtech.common.items.GT_IntegratedCircuit_Item;
import gtPlusPlus.core.block.ModBlocks;
import gtPlusPlus.xmod.gregtech.common.blocks.textures.TexturesGtBlock;

public class TST_MegaTreeFarm extends GTCM_MultiMachineBase<TST_MegaTreeFarm> {

    // region Class Constructor
    public TST_MegaTreeFarm(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public TST_MegaTreeFarm(String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new TST_MegaTreeFarm(this.mName);
    }

    // region Structure

    private static final String STRUCTURE_PIECE_MAIN = "mainMegaTreeFarm";
    private static final String STRUCTURE_PIECE_WATER = "waterMegaTreeFarm";
    private static IStructureDefinition<TST_MegaTreeFarm> STRUCTURE_DEFINITION = null;

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        repairMachine();
        this.buildPiece(STRUCTURE_PIECE_MAIN, stackSize, hintsOnly, 16, 38, 7);
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        repairMachine();
        // setDebugEnabled(true);
        return checkPiece(STRUCTURE_PIECE_MAIN, 16, 38, 7) & checkPiece(STRUCTURE_PIECE_WATER, 0, 37, -9);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        return survivialBuildPiece(STRUCTURE_PIECE_MAIN, stackSize, 16, 38, 7, elementBudget, env, false, true)
            & survivialBuildPiece(STRUCTURE_PIECE_WATER, stackSize, 0, 37, -9, elementBudget, env, false, true);
    }

    @Override
    public IStructureDefinition<TST_MegaTreeFarm> getStructureDefinition() {
        if (STRUCTURE_DEFINITION == null) {
            STRUCTURE_DEFINITION = StructureDefinition.<TST_MegaTreeFarm>builder()
                .addShape(STRUCTURE_PIECE_MAIN, transpose(shape))
                .addShape(STRUCTURE_PIECE_WATER, transpose(water))
                .addElement('A', BorosilicateGlass.ofBoroGlassAnyTier())
                .addElement('B', ofBlock(MetaBlockCasing01, 9))
                .addElement('C', ofBlock(MetaBlockCasing01, 10))
                .addElement('D', ofBlock(GregTech_API.sBlockCasings1, 10))
                .addElement('E', ofBlock(GregTech_API.sBlockCasings8, 5))
                .addElement('F', ofBlock(GregTech_API.sBlockCasings8, 10))
                .addElement('G', ofBlock(GregTech_API.sBlockCasings9, 1))
                .addElement('H', ofBlock(ModBlocks.blockCasings2Misc, 15))
                .addElement('I', ofBlock(ModBlocks.blockCasingsTieredGTPP, 8))
                .addElement(
                    'J',
                    ofBlock(ModBlocksHandler.BlockTranslucent.getLeft(), ModBlocksHandler.BlockTranslucent.getRight()))
                .addElement(
                    'K',
                    ofBlock(ModBlocksHandler.AirCrystalBlock.getLeft(), ModBlocksHandler.AirCrystalBlock.getRight()))
                .addElement(
                    'L',
                    ofBlock(
                        ModBlocksHandler.WaterCrystalBlock.getLeft(),
                        ModBlocksHandler.WaterCrystalBlock.getRight()))
                .addElement(
                    'M',
                    ofBlock(
                        ModBlocksHandler.EarthCrystalBlock.getLeft(),
                        ModBlocksHandler.EarthCrystalBlock.getRight()))
                .addElement('N', ofBlock(Block.getBlockFromName("Forestry:soil"), 0))
                .addElement('O', ofChain(ofBlock(Blocks.grass, 0), ofBlockAnyMeta(Blocks.dirt, 0)))
                .addElement(
                    'P',
                    LoaderReference.ProjRedIllumination
                        ? ofBlock(Block.getBlockFromName("ProjRed|Illumination:projectred.illumination.lamp"), 10)
                        : ofBlock(Blocks.redstone_lamp, 0))
                .addElement('Q', ofBlock(Blocks.water, 0))
                .addElement(
                    'R',
                    ofChain(
                        ofBlock(ModBlocks.blockCasings2Misc, 15),
                        GT_HatchElementBuilder.<TST_MegaTreeFarm>builder()
                            .atLeast(InputBus, OutputBus, Energy.or(ExoticEnergy))
                            .adder(TST_MegaTreeFarm::addToMachineList)
                            .dot(1)
                            .casingIndex(TAE.getIndexFromPage(1, 15))
                            .build()))
                .addElement(
                    'S',
                    ofChain(
                        ofBlock(ModBlocks.blockCasings2Misc, 15),
                        GT_HatchElementBuilder.<TST_MegaTreeFarm>builder()
                            .atLeast(Energy.or(ExoticEnergy))
                            .adder(TST_MegaTreeFarm::addToMachineList)
                            .dot(2)
                            .casingIndex(TAE.getIndexFromPage(1, 15))
                            .build()))
                .addElement('T', ofFrame(Materials.Vulcanite))
                .build();
        }
        return STRUCTURE_DEFINITION;
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing,
        int colorIndex, boolean aActive, boolean redstoneLevel) {
        if (side == aFacing) {
            if (aActive) {
                return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(TAE.getIndexFromPage(1, 15)),
                    TextureFactory.builder()
                        .addIcon(TexturesGtBlock.Overlay_Machine_Controller_Advanced)
                        .extFacing()
                        .build(),
                    TextureFactory.builder()
                        .addIcon(TexturesGtBlock.Overlay_Machine_Controller_Advanced_Active)
                        .extFacing()
                        .glow()
                        .build() };
            }

            return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(TAE.getIndexFromPage(1, 15)),
                TextureFactory.builder()
                    .addIcon(TexturesGtBlock.Overlay_Machine_Controller_Advanced)
                    .extFacing()
                    .build() };
        }

        return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(TAE.getIndexFromPage(1, 15)) };
    }

    // spotless:off

    /*
    A -> ofBlock...(BW_GlasBlocks, 0, ...);
    B -> ofBlock...(MetaBlockCasing01, 9, ...);
    C -> ofBlock...(MetaBlockCasing01, 10, ...);
    D -> ofBlock...(gt.blockcasings, 10, ...);
    E -> ofBlock...(gt.blockcasings8, 5, ...);
    F -> ofBlock...(gt.blockcasings8, 10, ...);
    G -> ofBlock...(gt.blockcasings9, 1, ...);
    H -> ofBlock...(gtplusplus.blockcasings.2, 15, ...);
    I -> ofBlock...(gtplusplus.blocktieredcasings.1, 8, ...);
    J -> ofBlock...(tile.blockTranslucent, 0, ...);
    K -> ofBlock...(tile.crystalBlock, 0, ...);
    L -> ofBlock...(tile.crystalBlock, 2, ...);
    M -> ofBlock...(tile.crystalBlock, 3, ...);
    N -> ofBlock...(tile.for.soil, 0, ...);
    O -> ofBlock...(tile.grass, 0, ...);
    P -> ofBlock...(tile.redstoneLight, 0, ...);
    Q -> ofBlock...(tile.water, 0, ...);
    R -> ofBlock...(tile.wood, 0, ...);     Any Hatch
    S -> ofBlock...(tile.wood, 1, ...);     Energy Hatch
    T -> ofSpecialTileAdder(gregtech.api.metatileentity.BaseMetaPipeEntity, ...);
    */

    private final String[][] shape = new String[][]{
        {"                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","              DDDDD              ","             DDDDDDD             ","             DDDDDDD             ","             DDDDDDD             ","             DDDDDDD             ","             DDDDDDD             ","              DDDDD              ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","               D D               ","             GGD DGG             ","            GGGD DGGG            ","           GGGHH HHGGG           ","           GGHTH HTHGG           ","          DDDHHH HHHDDD          ","                                 ","          DDDHHH HHHDDD          ","           GGHTH HTHGG           ","           GGGHH HHGGG           ","            GGGD DGGG            ","             GGD DGG             ","               D D               ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","             GGD DGG             ","           DGGGTDTGGGD           ","          DGG  TDT  GGD          ","          GG  HHDHH  GG          ","         GG  HIIDIIH  GG         ","         GG HIIIDIIIH GG         ","         DTTHIIIDIIIHTTD         ","          DDDDDDDDDDDDD          ","         DTTHIIIDIIIHTTD         ","         GG HIIIDIIIH GG         ","         GG  HIIDIIH  GG         ","          GG  HHDHH  GG          ","          DGG  TDT  GGD          ","           DGGGTDTGGGD           ","             GGD DGG             ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","               D D               ","            DGGD DGGD            ","           AD  TDT  DA           ","          A     T     A          ","         A      T      A         ","        DD    DDDDD    DD        ","        G    DPPPPPD    G        ","        G   DPPPPPPPD   G        ","       DDT  DPPPPPPPD  TDD       ","         DTTDPPPPPPPDTTD         ","       DDT  DPPPPPPPD  TDD       ","        G   DPPPPPPPD   G        ","        G    DPPPPPD    G        ","        DD    DDDDD    DD        ","         A      T      A         ","          A     T     A          ","           AD  TDT  DA           ","            DGGD DGGD            ","               D D               ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","               D D               ","            ADDTDTDDA            ","          AA   TDT   AA          ","         AA           AA         ","        AA             AA        ","        A               A        ","       A                 A       ","       D                 D       ","       D                 D       ","      DTT               TTD      ","       DD               DD       ","      DTT               TTD      ","       D                 D       ","       D                 D       ","       A                 A       ","        A               A        ","        AA             AA        ","         AA           AA         ","          AA   TDT   AA          ","            ADDTDTDDA            ","               D D               ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","            AAAD DAAA            ","          AA   TDT   AA          ","         A             A         ","        A               A        ","       A                 A       ","       A                 A       ","      A                   A      ","      A                   A      ","      A                   A      ","      DT                 TD      ","       D                 D       ","      DT                 TD      ","      A                   A      ","      A                   A      ","      A                   A      ","       A                 A       ","       A                 A       ","        A               A        ","         A             A         ","          AA   TDT   AA          ","            AAAD DAAA            ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                                 ","             AAD DAA             ","           AA  TDT  AA           ","         AA           AA         ","        A               A        ","       A                 A       ","       A                 A       ","      A                   A      ","      A                   A      ","     A                     A     ","     A                     A     ","     DT                   TD     ","      D                   D      ","     DT                   TD     ","     A                     A     ","     A                     A     ","      A                   A      ","      A                   A      ","       A                 A       ","       A                 A       ","        A               A        ","         AA           AA         ","           AA  TDT  AA           ","             AAD DAA             ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","               D D               ","           AAAATDTAAAA           ","          A           A          ","        AA             AA        ","       A                 A       ","       A                 A       ","      A                   A      ","     A                     A     ","     A                     A     ","     A                     A     ","     A                     A     ","    DT                     TD    ","     D                     D     ","    DT                     TD    ","     A                     A     ","     A                     A     ","     A                     A     ","     A                     A     ","      A                   A      ","       A                 A       ","       A                 A       ","        AA             AA        ","          A           A          ","           AAAATDTAAAA           ","               D D               ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","            DDDD DDDD            ","          DD   T T   DD          ","        DD             DD        ","       D                 D       ","      D                   D      ","      D                   D      ","     D                     D     ","     D                     D     ","    D                       D    ","    D                       D    ","    D                       D    ","    DT                     TD    ","                                 ","    DT                     TD    ","    D                       D    ","    D                       D    ","    D                       D    ","     D                     D     ","     D                     D     ","      D                   D      ","      D                   D      ","       D                 D       ","        DD             DD        ","          DD   T T   DD          ","            DDDD DDDD            ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","            AAADDDAAA            ","          AA   T T   AA          ","        AA             AA        ","       A                 A       ","      A                   A      ","      A                   A      ","     A                     A     ","     A                     A     ","    A                       A    ","    A                       A    ","    A                       A    ","    DT                     TD    ","    D                       D    ","    DT                     TD    ","    A                       A    ","    A                       A    ","    A                       A    ","     A                     A     ","     A                     A     ","      A                   A      ","      A                   A      ","       A                 A       ","        AA             AA        ","          AA   T T   AA          ","            AAADDDAAA            ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","              AA AA              ","           AAA TDT AAA           ","         AA           AA         ","        A               A        ","       A                 A       ","      A                   A      ","     A                     A     ","     A                     A     ","    A                       A    ","    A                       A    ","    A                       A    ","   A                         A   ","   AT                       TA   ","    D                       D    ","   AT                       TA   ","    A                        A   ","    A                       A    ","    A                       A    ","    A                       A    ","     A                     A     ","     A                     A     ","      A                   A      ","       A                 A       ","        A               A        ","         AA           AA         ","           AAA TDT AAA           ","              AA AA              ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","             AAADAAA             ","           AA       AA           ","         AA           AA         ","        A               A        ","       A                 A       ","      A                   A      ","     A                     A     ","     A                     A     ","    A                       A    ","    A                       A    ","   A                         A   ","   A                         A   ","   A                         A   ","   D                         D   ","   A                         A   ","   A                         A   ","   A                         A   ","    A                       A    ","    A                       A    ","     A                     A     ","     A                     A     ","      A                   A      ","       A                 A       ","        A               A        ","         AA           AA         ","           AA       AA           ","             AAADAAA             ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","             AAADAAA             ","          AAA       AAA          ","         A             A         ","       AA               AA       ","      A                   A      ","      A                   A      ","     A                     A     ","    A                       A    ","    A                       A    ","    A                       A    ","   A                         A   ","   A                         A   ","   A                         A   ","   D                         D   ","   A                         A   ","   A                         A   ","   A                         A   ","    A                       A    ","    A                       A    ","    A                       A    ","     A                     A     ","      A                   A      ","      A                   A      ","       AA               AA       ","         A             A         ","          AAA       AAA          ","             AAADAAA             ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","             AAADAAA             ","          AAA       AAA          ","         A             A         ","       AA               AA       ","      A                   A      ","      A                   A      ","     A                     A     ","    A                       A    ","    A                       A    ","    A                       A    ","   A                         A   ","   A                         A   ","   A                         A   ","   D                         D   ","   A                         A   ","   A                         A   ","   A                         A   ","    A                       A    ","    A                       A    ","    A                       A    ","     A                     A     ","      A                   A      ","      A                   A      ","       AA               AA       ","         A             A         ","          AAA       AAA          ","             AAADAAA             ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","             AAADAAA             ","          AAA       AAA          ","         A             A         ","       AA               AA       ","      A                   A      ","      A                   A      ","     A                     A     ","    A                       A    ","    A                       A    ","    A                       A    ","   A                         A   ","   A                         A   ","   A                         A   ","   D                         D   ","   A                         A   ","   A                         A   ","   A                         A   ","    A                       A    ","    A                       A    ","    A                       A    ","     A                     A     ","      A                   A      ","      A                   A      ","       AA               AA       ","         A             A         ","          AAA       AAA          ","             AAADAAA             ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","             AAADAAA             ","           AA       AA           ","         AA           AA         ","        A               A        ","       A                 A       ","      A                   A      ","     A                     A     ","     A                     A     ","    A                       A    ","    A                       A    ","   A                         A   ","   A                         A   ","   A                         A   ","   D                         D   ","   A                         A   ","   A                         A   ","   A                         A   ","    A                       A    ","    A                       A    ","     A                     A     ","     A                     A     ","      A                   A      ","       A                 A       ","        A               A        ","         AA           AA         ","           AA       AA           ","             AAADAAA             ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","              AADAA              ","           AAA     AAA           ","         AA           AA         ","        A               A        ","       A                 A       ","      A                   A      ","     A                     A     ","     A                     A     ","    A                       A    ","    A                       A    ","    A                       A    ","   A                         A   ","   A                         A   ","   D                         D   ","   A                         A   ","   A                         A   ","    A                       A    ","    A                       A    ","    A                       A    ","     A                     A     ","     A                     A     ","      A                   A      ","       A                 A       ","        A               A        ","         AA           AA         ","           AAA     AAA           ","              AADAA              ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","            AAAADAAAA            ","          AA         AA          ","        AA             AA        ","       A                 A       ","      A                   A      ","      A                   A      ","     A                     A     ","     A                     A     ","    A                       A    ","    A                       A    ","    A                       A    ","    A                       A    ","    D                       D    ","    A                       A    ","    A                       A    ","    A                       A    ","    A                       A    ","     A                     A     ","     A                     A     ","      A                   A      ","      A                   A      ","       A                 A       ","        AA             AA        ","          AA         AA          ","            AAAADAAAA            ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","             AAADAAA             ","          AAA       AAA          ","         A             A         ","        A               A        ","       A                 A       ","      A                   A      ","     A                     A     ","     A                     A     ","     A                     A     ","    A                       A    ","    A                       A    ","    A                       A    ","    D                       D    ","    A                       A    ","    A                       A    ","    A                       A    ","     A                     A     ","     A                     A     ","     A                     A     ","      A                   A      ","       A                 A       ","        A               A        ","         A             A         ","          AAA       AAA          ","             AAADAAA             ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","               ADA               ","           AAAA   AAAA           ","          A           A          ","        AA             AA        ","       A                 A       ","       A                 A       ","      A                   A      ","     A                     A     ","     A                     A     ","     A                     A     ","     A                     A     ","    A                       A    ","    D                       D    ","    A                       A    ","     A                     A     ","     A                     A     ","     A                     A     ","     A                     A     ","      A                   A      ","       A                 A       ","       A                 A       ","        AA             AA        ","          A           A          ","           AAAA   AAAA           ","               ADA               ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                D                ","             AAAHAAA             ","           AA       AA           ","         AA           AA         ","        A               A        ","       A                 A       ","       A                 A       ","      A                   A      ","      A                   A      ","     A                     A     ","     A                     A     ","     A                     A     ","    DH                     HD    ","     A                     A     ","     A                     A     ","     A                     A     ","      A                   A      ","      A                   A      ","       A                 A       ","       A                 A       ","        A               A        ","         AA           AA         ","           AA       AA           ","             AAAHAAA             ","                D                ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                                 ","            DDDDDDDDD            ","          DDHHHHHHHHHDD          ","         DHHNNNN NNNNHHD         ","        DHNNNNNNNNNNNNNHD        ","       DHNNNNNNNNNNNNNNNHD       ","      DHNNNNNNNNNNNNNNNNNHD      ","      DHNNNNNNNNNNNNNNNNNHD      ","     DHNNNNNNNNNNNNNNNNNNNHD     ","     DHNNNNNNNNNNNNNNNNNNNHD     ","     DHNNNNNNNNNNNNNNNNNNNHD     ","     DHNNNNNNNNNNNNNNNNNNNHD     ","     DH NNNNNNNNNNNNNNNNN HD     ","     DHNNNNNNNNNNNNNNNNNNNHD     ","     DHNNNNNNNNNNNNNNNNNNNHD     ","     DHNNNNNNNNNNNNNNNNNNNHD     ","     DHNNNNNNNNNNNNNNNNNNNHD     ","      DHNNNNNNNNNNNNNNNNNHD      ","      DHNNNNNNNNNNNNNNNNNHD      ","       DHNNNNNNNNNNNNNNNHD       ","        DHNNNNNNNNNNNNNHD        ","         DHHNNNN NNNNHHD         ","          DDHHHHHHHHHDD          ","            DDDDDDDDD            ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                                 ","                D                ","            TTTDHDTTT            ","          TTAAAN NAAATT          ","         TAANNNN NNNNAAT         ","        TAANNNNN NNNNNAAT        ","       TAANNNNNNNNNNNNNAAT       ","       TANNNNNNNNNNNNNNNAT       ","      TANNNNNNNNNNNNNNNNNAT      ","      TANNNNNNNNNNNNNNNNNAT      ","      TANNNNNNNNNNNNNNNNNAT      ","      DNNNNNNNNNNNNNNNNNNND      ","     DH   NNNNNNNNNNNNN   HD     ","      DNNNNNNNNNNNNNNNNNNND      ","      TANNNNNNNNNNNNNNNNNAT      ","      TANNNNNNNNNNNNNNNNNAT      ","      TANNNNNNNNNNNNNNNNNAT      ","       TANNNNNNNNNNNNNNNAT       ","       TAANNNNNNNNNNNNNAAT       ","        TAANNNNN NNNNNAAT        ","         TAANNNN NNNNAAT         ","          TTAAAN NAAATT          ","            TTTDHDTTT            ","                D                ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                D                ","               AHA               ","            AAAAHAAAA            ","           AANNN NNNAA           ","          ANNNNN NNNNNA          ","         ANNNNNNNNNNNNNA         ","        AANNNNNNNNNNNNNAA        ","        ANNNNNNNNNNNNNNNA        ","        ANNNNNNNNNNNNNNNA        ","       AANNNNNNNNNNNNNNNAA       ","      DHH  NNNNNNNNNNN  HHD      ","       AANNNNNNNNNNNNNNNAA       ","        ANNNNNNNNNNNNNNNA        ","        ANNNNNNNNNNNNNNNA        ","        AANNNNNNNNNNNNNAA        ","         ANNNNNNNNNNNNNA         ","          ANNNNN NNNNNA          ","           AANNN NNNAA           ","            AAAAHAAAA            ","               AHA               ","                D                ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                D                ","                D                ","             AAAHAAA             ","           AAAAN NAAAA           ","          AAANNN NNNAAA          ","          AANNNN NNNNAA          ","         AANNNNN NNNNNAA         ","         AANNNNNNNNNNNAA         ","         ANNNNNNNNNNNNNA         ","       DDH    NNNNN    HDD       ","         ANNNNNNNNNNNNNA         ","         AANNNNNNNNNNNAA         ","         AANNNNN NNNNNAA         ","          AANNNN NNNNAA          ","          AAANNN NNNAAA          ","           AAAAN NAAAA           ","             AAAHAAA             ","                D                ","                D                ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                D                ","               AHA               ","             AAAHAAA             ","            AAAAHAAAA            ","           AAANN NNAAA           ","           AANNN NNNAA           ","          AAANNN NNNAAA          ","         DHHH       HHHD         ","          AAANNN NNNAAA          ","           AANNN NNNAA           ","           AAANN NNAAA           ","            AAAAHAAAA            ","             AAAHAAA             ","               AHA               ","                D                ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                D                ","                D                ","              DDDDD              ","             DHHHHHD             ","            DHHHHHHHD            ","            DHHHHHHHD            ","          DDDHHH HHHDDD          ","            DHHHHHHHD            ","            DHHHHHHHD            ","             DHHHHHD             ","              DDDDD              ","                D                ","                D                ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","              DDDDD              ","             DDJDJDD             ","             DJJDJJD             ","             DDD DDD             ","             DJJDJJD             ","             DDJDJDD             ","              DDDDD              ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                L                ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                L                ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                L                ","               LLL               ","                L                ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                L                ","               LLL               ","                L                ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","              EEEEE              ","             E     E             ","            E       E            ","            E   L   E            ","            E  LLL  E            ","            E   L   E            ","            E       E            ","             E     E             ","              EEEEE              ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","             DDDDDDD             ","            DDTTTTTDD            ","          DDTTGGGGGTTDD          ","          DTGGGGGGGGGTD          ","         DTGGGEEEEEGGGTD         ","        DDTGGFBBBBBFGGTDD        ","        DTGGEBB   BBEGGTD        ","        DTGGEB     BEGGTD        ","        DTGGEB  J  BEGGTD        ","        DTGGEB     BEGGTD        ","        DTGGEBB   BBEGGTD        ","        DDTGGFBBBBBFGGTDD        ","         DTGGGEEEEEGGGTD         ","          DTGGGGGGGGGTD          ","          DDTTGGGGGTTDD          ","            DDTTTTTDD            ","             DDDDDDD             ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","                                 ","                                 ","             D     D             ","             DTR~RTD             ","            TDIIIIIDT            ","          TTIIIIIIIIITT          ","         TIIIIIIIIIIIIIT         ","         TIIIIIIIIIIIIIT         ","        TIIIIIEEEEEIIIIIT        ","      DDDIIIIFMMMMMFIIIIDDD      ","       TIIIIEMMBBBMMEIIIIT       ","       TIIIIEMBBBBBMEIIIIT       ","       TIIIIEMBBBBBMEIIIIT       ","       TIIIIEMBBBBBMEIIIIT       ","       TIIIIEMMBBBMMEIIIIT       ","      DDDIIIIFMMMMMFIIIIDDD      ","        TIIIIIEEEEEIIIIIT        ","         TIIIIIIIIIIIIIT         ","         TIIIIIIIIIIIIIT         ","          TTIIIIIIIIITT          ","            TDIIIIIDT            ","             DTTTTTD             ","             D     D             ","                                 ","                                 ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","                                 ","             DDDDDDD             ","           DDDRRRRRDDD           ","         DDRRHRRRRRHRRDD         ","        DRRRRHGHHHGHRRRRD        ","       DRRRRGHIIIIIHGRRRRD       ","      DRRRGGIIKKKKKIIGGRRRD      ","      DRRGIIKKKKKKKKKIIGRRD      ","     DRRRGIKKKKKKKKKKKIGRRRD     ","     DRRGIKKKKEEEEEKKKKIGRRD     ","    DDHHHIKKKFMMMMMFKKKIHHHDD    ","    DRRGIKKKEMMMMMMMEKKKIGRRD    ","    DRRGIKKKEMMMMMMMEKKKIGRRD    ","    DRRGIKKKEMMMMMMMEKKKIGRRD    ","    DRRGIKKKEMMMMMMMEKKKIGRRD    ","    DRRGIKKKEMMMMMMMEKKKIGRRD    ","    DDHHHIKKKFMMMMMFKKKIHHHDD    ","     DRRGIKKKKEEEEEKKKKIGRRD     ","     DRRRGIKKKKKKKKKKKIGRRRD     ","      DRRGIIKKKKKKKKKIIGRRD      ","      DRRRGGIIKKKKKIIGGRRRD      ","       DRRRRGHIIIIIHGRRRRD       ","        DRRRRHGGGGGHRRRRD        ","         DDRRHRRRRRHRRDD         ","           DDDRRRRRDDD           ","             DDDDDDD             ","                                 ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","                                 ","            DHHHHHHHD            ","           HDHHHDHHHDH           ","         HHHHDDDDDDDHHHH         ","        HHHDDCCCCCCCDDHHH        ","       HHDDCCCCCCCCCCCDDHH       ","      HHDCCCCCDDDDDCCCCCDHH      ","     HHDCCCCHHCCCCCHHCCCCDHH     ","     HHDCCHHCCCCCCCCCHHCCDHH     ","    HHDCCCHCCCCCCCCCCCHCCCDHH    ","   DDHDCCHCCCCEEEEECCCCHCCDHDD   ","   HHDCCCHCCCFMMMMMFCCCHCCCDHH   ","   HHDCCDCCCEMMMMMMMECCCDCCDHH   ","   HHDCCDCCCEMMMMMMMECCCDCCDHH   ","   HDDCCDCCCEMMMMMMMECCCDCCDDH   ","   HHDCCDCCCEMMMMMMMECCCDCCDHH   ","   HHDCCDCCCEMMMMMMMECCCDCCDHH   ","   HHDCCCHCCCFMMMMMFCCCHCCCDHH   ","   DDHDCCHCCCCEEEEECCCCHCCDHDD   ","    HHDCCCHCCCCCCCCCCCHCCCDHH    ","     HHDCCHHCCCCCCCCCHHCCDHH     ","     HHDCCCCHHCCCCCHHCCCCDHH     ","      HHDCCCCCDDDDDCCCCCDHH      ","       HHDDCCCCCCCCCCCDDHH       ","        HHHDDCCCCCCCDDHHH        ","         HHHHDDDDDDDHHHH         ","           HDHHHDHHHDH           ","            DHHHHHHHD            ","                                 ","                                 ","                                 "},
        {"                                 ","                                 ","            D       D            ","            THHJJJHHT            ","           HHHHHDHHHHH           ","         JJHHCCCCCCCHHJJ         ","        HHHCCCCCCCCCCCHHH        ","       JHCCCCCCCCCCCCCCCHJ       ","      HHCCCCCCHHHHHCCCCCCHH      ","     JHCCCCCHHGGGGGHHCCCCCHJ     ","     JHCCCHHGGGGGGGGGHHCCCHJ     ","    HHCCCCHGGGGGGGGGGGHCCCCHH    ","  DTHHCCCHGGGGEEEEEGGGGHCCCHHTD  ","   HHCCCCHGGGFMMMMMFGGGHCCCCHH   ","   HHCCCHGGGEMMMMMMMEGGGHCCCHH   ","   JHCCCHGGGEMMMMMMMEGGGHCCCHJ   ","   JDCCCHGGGEMMMMMMMEGGGHCCCDJ   ","   JHCCCHGGGEMMMMMMMEGGGHCCCHJ   ","   HHCCCHGGGEMMMMMMMEGGGHCCCHH   ","   HHCCCCHGGGFMMMMMFGGGHCCCCHH   ","  DTHHCCCHGGGGEEEEEGGGGHCCCHHTD  ","    HHCCCCHGGGGGGGGGGGHCCCCHH    ","     JHCCCHHGGGGGGGGGHHCCCHJ     ","     JHCCCCCHHGGGGGHHCCCCCHJ     ","      HHCCCCCCHHHHHCCCCCCHH      ","       JHCCCCCCCCCCCCCCCHJ       ","        HHHCCCCCCCCCCCHHH        ","         JJHHCCCCCCCHHJJ         ","           HHHHHDHHHHH           ","            THHJJJHHT            ","            D       D            ","                                 ","                                 "},
        {"                                 ","            D       D            ","            TT     TT            ","            THSSSSSHT            ","           HHHHHDHHHHH           ","         HHHHHHHHHHHHHHH         ","        HHHSSHHHHHHHSSHHH        ","       HHSSSJSHHHHHSJSSSHH       ","      HHSSJSSSHHHHHSSSJSSHH      ","     HHSSSSSHH     HHSSSSSHH     ","     HHSJSHH         HHSJSHH     ","    HHSSSSH           HSSSSHH    "," DTTHHSJSH    EEEEE    HSJSHHTTD ","  THHHHSSH   FMMMMMF   HSSHHHHT  ","   SHHHHH   EMMMMMMME   HHHHHS   ","   SHHHHH   EMMMMMMME   HHHHHS   ","   SDHHHH   EMMMMMMME   HHHHDS   ","   SHHHHH   EMMMMMMME   HHHHHS   ","   SHHHHH   EMMMMMMME   HHHHHS   ","  THHHHSSH   FMMMMMF   HSSHHHHT  "," DTTHHSJSH    EEEEE    HSJSHHTTD ","    HHSSSSH           HSSSSHH    ","     HHSJSHH         HHSJSHH     ","     HHSSSSSHH     HHSSSSSHH     ","      HHSSJSSSHHHHHSSSJSSHH      ","       HHSSSJSHHHHHSJSSSHH       ","        HHHSSHHHHHHHSSHHH        ","         HHHHHHHHHHHHHHH         ","           HHHHHDHHHHH           ","            THSSSSSHT            ","            TT     TT            ","            D       D            ","                                 "},
        {"            DDDDDDDDD            ","            DTTTTTTTD            ","           DDD     DDD           ","           DDDDDDDDDDD           ","           DDDDDDDDDDD           ","         DDDDDDDDDDDDDDD         ","        DDD  DDDDDDD  DDD        ","       DD     DDDDD     DD       ","      DD      DDDDD      DD      ","     DD     DD     DD     DD     ","     DD   DD         DD   DD     ","  DDDD    D           D    DDDD  ","DDDDDD   D    EJJJE    D   DDDDDD","DTDDDDD  D   FMMMMMF   D  DDDDDTD","DT DDDDDD   EMMMMMMME   DDDDDD TD","DT DDDDDD   JMMMMMMMJ   DDDDDD TD","DT DDDDDD   JMMMMMMMJ   DDDDDD TD","DT DDDDDD   JMMMMMMMJ   DDDDDD TD","DT DDDDDD   EMMMMMMME   DDDDDD TD","DTDDDDD  D   FMMMMMF   D  DDDDDTD","DDDDDD   D    EJJJE    D   DDDDDD","  DDDD    D           D    DDDD  ","     DD   DD         DD   DD     ","     DD     DD     DD     DD     ","      DD      DDDDD      DD      ","       DD     DDDDD     DD       ","        DDD  DDDDDDD  DDD        ","         DDDDDDDDDDDDDDD         ","           DDDDDDDDDDD           ","           DDDDDDDDDDD           ","           DDD     DDD           ","            DTTTTTTTD            ","            DDDDDDDDD            "},
        {"            BBBBBBBBB            ","          BBB       BBB          ","        BBOBBB     BBBOBB        ","      BBOOOBBBBBBBBBBBOOOBB      ","     BOOOOOBBBBBBBBBBBOOOOOB     ","    BOOOOBBBBBBBBBBBBBBBOOOOB    ","   BOOOOBBB  BBBBBBB  BBBOOOOB   ","   BOOOBB     BBBBB     BBOOOB   ","  BOOOBB      BBBBB      BBOOOB  ","  BOOBB     BB     BB     BBOOB  "," BOOOBB   BB         BB   BBOOOB "," BBBBB    B           B    BBBBB ","BBBBBB   B    EEEEE    B   BBBBBB","B BBBBB  B   EFFFFFE   B  BBBBB B","B  BBBBBB   EFFMMMFFE   BBBBBB  B","B  BBBBBB   EFMMMMMFE   BBBBBB  B","B  BBBBBB   EFMMMMMFE   BBBBBB  B","B  BBBBBB   EFMMMMMFE   BBBBBB  B","B  BBBBBB   EFFMMMFFE   BBBBBB  B","B BBBBB  B   EFFFFFE   B  BBBBB B","BBBBBB   B    EEEEE    B   BBBBBB"," BBBBB    B           B    BBBBB "," BOOOBB   BB         BB   BBOOOB ","  BOOBB     BB     BB     BBOOB  ","  BOOOBB      BBBBB      BBOOOB  ","   BOOOBB     BBBBB     BBOOOB   ","   BOOOOBBB  BBBBBBB  BBBOOOOB   ","    BOOOOBBBBBBBBBBBBBBBOOOOB    ","     BOOOOOBBBBBBBBBBBOOOOOB     ","      BBOOOBBBBBBBBBBBOOOBB      ","        BBOBBB     BBBOBB        ","          BBB       BBB          ","            BBBBBBBBB            "}
    };

    private final String[][] water =new String[][]{
        {"Q"}
    };

    @Override
    protected GT_Multiblock_Tooltip_Builder createTooltip() {
        final GT_Multiblock_Tooltip_Builder tt = new GT_Multiblock_Tooltip_Builder();
        // #tr Tooltip_MegaTreeFarm_MachineType
        // # Tree Farm
        // #zh_CN 树厂
        tt.addMachineType(TextEnums.tr("Tooltip_MegaTreeFarm_MachineType"))
            // #tr Tooltip_MegaTreeFarm_Controller
            // # Controller block for the BioSphere Growth Simulator
            // #zh_CN 拟生圈的控制方块
            .addInfo(TextEnums.tr("Tooltip_MegaTreeFarm_Controller"))
            .addController(textUseBlueprint)
            .addInputBus(textUseBlueprint, 1)
            .addOutputBus(textUseBlueprint, 1)
            .addEnergyHatch(textUseBlueprint, 2)
            .toolTipFinisher(ModName);
        return tt;
    }

    // spotless:on
    // region Processing Logic
    double tierMultiplier = 1;
    int tier = 1;
    static FluidStack WaterStack = Materials.Water.getFluid(1000);

    @Override
    protected boolean isEnablePerfectOverclock() {
        return false;
    }

    @Override
    protected float getSpeedBonus() {
        return 1;
    }

    @Override
    protected int getMaxParallelRecipes() {
        return 1;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return GTCMRecipe.TreeGrowthSimulatorWithoutToolFakeRecipes;
        // return GTPPRecipeMaps.treeGrowthSimulatorFakeRecipes;
    }

    @Override
    public boolean supportsInputSeparation() {
        return false;
    }

    @Override
    public boolean supportsBatchMode() {
        return false;
    }

    @Override
    public boolean supportsSingleRecipeLocking() {
        return false;
    }

    private static int getTierMultiplier(int tier) {
        return (int) Math
            .floor(2 * Math.pow(2, 0.1 * (tier - 1) * (8 + Math.log(25 + Math.exp(25 - tier)) / Math.log(5))));
        // Math.floor(Math.pow(Math.pow(2, tier + 1), 0.8 + 0.1 * Math.log(25 + Math.exp(25 - tier)) / Math.log(5)));
    }

    /**
     * Use the highest bonus from the original Recipe.
     */

    public static int getModeMultiplier(Mode mode) {
        return switch (mode) {
            case LOG -> 20;
            case SAPLING -> 3;
            case LEAVES -> 8;
            case FRUIT -> 1;
        };

    }

    public int getModeOutput(Mode mode) {
        Map<Integer, Mode> damageModeMap = new HashMap<>();
        damageModeMap.put(1, Mode.LOG);
        damageModeMap.put(2, Mode.SAPLING);
        damageModeMap.put(3, Mode.LEAVES);
        damageModeMap.put(4, Mode.FRUIT);

        for (ItemStack stack : getStoredInputs()) {
            if (stack.getItem() instanceof GT_IntegratedCircuit_Item && stack.getItemDamage() > 0) {
                Mode mappedMode = damageModeMap.get(stack.getItemDamage());
                if (mode == mappedMode) {
                    return 1;
                }
            }
        }
        return -1;
    }

    // public static ItemStack queryTreeProduct(ItemStack sapling, GregtechMetaTileEntityTreeFarm.Mode mode) {
    // String key = Item.itemRegistry.getNameForObject(sapling.getItem()) + ":" + sapling.getItemDamage();
    //
    // EnumMap<GregtechMetaTileEntityTreeFarm.Mode, ItemStack> productsMap = treeProductsMap.get(key);
    // return productsMap.get(mode);
    // }
    public static EnumMap<Mode, ItemStack> queryTreeProduct(ItemStack sapling) {
        String key = Item.itemRegistry.getNameForObject(sapling.getItem()) + ":" + sapling.getItemDamage();
        EnumMap<Mode, ItemStack> ProductMap = treeProductsMap.get(key);
        if (ProductMap != null) {
            return ProductMap;
        }
        return getOutputsForForestrySapling(sapling);
    }

    // copy form GTPP_TGS
    public static EnumMap<Mode, ItemStack> getOutputsForForestrySapling(ItemStack sapling) {
        ITree tree = TreeManager.treeRoot.getMember(sapling);
        if (tree == null) return null;

        String speciesUUID = tree.getIdent();

        EnumMap<Mode, ItemStack> defaultMap = treeProductsMap.get("Forestry:sapling:" + speciesUUID);
        if (defaultMap == null) return null;

        // We need to make a new map so that we don't modify the stored amounts of outputs.
        EnumMap<Mode, ItemStack> adjustedMap = new EnumMap<>(Mode.class);

        ItemStack log = defaultMap.get(Mode.LOG);
        if (log != null) {
            double height = Math.max(
                3 * (tree.getGenome()
                    .getHeight() - 1),
                0) + 1;
            double girth = tree.getGenome()
                .getGirth();

            log = log.copy();
            log.stackSize = (int) (log.stackSize * height * girth);
            adjustedMap.put(Mode.LOG, log);
        }

        ItemStack saplingOut = defaultMap.get(Mode.SAPLING);
        if (saplingOut != null) {
            // Lowest = 0.01 ... Average = 0.05 ... Highest = 0.3
            double fertility = tree.getGenome()
                .getFertility() * 10;

            // Return a copy of the *input* sapling, retaining its genetics.
            int stackSize = Math.max(1, (int) (saplingOut.stackSize * fertility));
            saplingOut = sapling.copy();
            saplingOut.stackSize = stackSize;
            adjustedMap.put(Mode.SAPLING, saplingOut);
        }

        ItemStack leaves = defaultMap.get(Mode.LEAVES);
        if (leaves != null) {
            adjustedMap.put(Mode.LEAVES, leaves.copy());
        }

        ItemStack fruit = defaultMap.get(Mode.FRUIT);
        if (fruit != null) {
            // Lowest = 0.025 ... Average = 0.2 ... Highest = 0.4
            double yield = tree.getGenome()
                .getYield() * 10;

            fruit = fruit.copy();
            fruit.stackSize = (int) (fruit.stackSize * yield);
            adjustedMap.put(Mode.FRUIT, fruit);
        }
        return adjustedMap;
    }

    @Override
    public GTCM_ProcessingLogic createProcessingLogic() {
        return new GTCM_ProcessingLogic() {

            @Override
            @Nonnull
            public CheckRecipeResult process() {
                if (inputItems == null) {
                    inputItems = new ItemStack[0];
                }
                if (inputFluids == null) {
                    inputFluids = new FluidStack[0];
                }

                ItemStack sapling = getControllerSlot();
                if (sapling == null) return SimpleCheckRecipeResult.ofFailure("no_sapling");
                EnumMap<Mode, ItemStack> outputPerMode = queryTreeProduct(sapling);
                if (outputPerMode == null) return SimpleCheckRecipeResult.ofFailure("no_sapling");

                tier = (int) Math.max(0, Math.log((double) (availableVoltage * availableAmperage) / 8) / Math.log(4));
                tierMultiplier = getTierMultiplier(tier);

                int RunMode = 0;
                ArrayList<FluidStack> InputFluids = getStoredFluids();
                for (FluidStack aFluid : InputFluids) {
                    if (aFluid.getFluid()
                        .equals(FluidRegistry.WATER)) RunMode = 1;
                    if (aFluid.getFluid()
                        .equals(BRFluids.UnknowWater)) RunMode = 2;
                    if (RunMode > 0) {
                        inputFluids[0] = aFluid;
                        break;
                    }
                }
                if (RunMode == 0) return SimpleCheckRecipeResult.ofFailure("no_fluid");

                List<ItemStack> outputs = new ArrayList<>();
                switch (RunMode) {
                    case 1: { // Normal Trees
                        for (Mode mode : Mode.values()) {
                            int checkMode = getModeOutput(mode);
                            ItemStack output = outputPerMode.get(mode);
                            if (output == null) continue; // This sapling has no output in this mode.
                            int ModeMultiplier = getModeMultiplier(mode);
                            if (checkMode < 0) continue; // No valid Circuit for this mode found.

                            ItemStack out = output.copy();
                            long outputStackSize = (long) (out.stackSize * ModeMultiplier * tierMultiplier * checkMode);
                            while (outputStackSize > Integer.MAX_VALUE) {
                                ItemStack outUnion = out.copy();
                                outUnion.stackSize = Integer.MAX_VALUE;
                                outputs.add(outUnion);
                                outputStackSize -= Integer.MAX_VALUE;
                            }
                            out.stackSize = (int) outputStackSize;
                            outputs.add(out);
                        }

                        if (outputs.isEmpty()) {
                            // No outputs can be produced using the tools we have available.
                            return SimpleCheckRecipeResult.ofFailure("no_correct_Circuit");
                        }
                    }
                    case 2: { // Normal to Barnarda C
                        ItemStack BarnardaCSapling = GT_ModHandler
                            .getModItem(Mods.GalaxySpace.ID, "barnardaCsapling", 1, 1);
                    }
                }

                outputItems = outputs.toArray(new ItemStack[0]);

                duration = 20;
                calculatedEut = availableVoltage * availableAmperage * 15 / 16;

                return SimpleCheckRecipeResult.ofSuccess("growing_trees");
            }

            // @Nonnull
            // @Override
            // protected GT_ParallelHelper createParallelHelper(@Nonnull GT_Recipe recipe) {
            // return super.createParallelHelper(recipe).setChanceMultiplier(tierMultiplier);
            // }
        };
    }

    public String[] getInfoData() {
        String[] origin = super.getInfoData();
        String[] ret = new String[origin.length + 2];
        System.arraycopy(origin, 0, ret, 0, origin.length);
        ret[origin.length] = EnumChatFormatting.AQUA + "tierMultiplier"
            + " : "
            + EnumChatFormatting.GOLD
            + (int) this.tierMultiplier;
        ret[origin.length + 1] = EnumChatFormatting.AQUA + "Tier" + " : " + EnumChatFormatting.GOLD + this.tier;
        return ret;
    }
}
