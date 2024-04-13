package com.Nxer.TwistSpaceTechnology.common.machine;

import static gregtech.api.enums.Textures.BlockIcons.*;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import com.Nxer.TwistSpaceTechnology.common.GTCMItemList;
import com.Nxer.TwistSpaceTechnology.common.machine.multiMachineClasses.processingLogics.GTCM_ProcessingLogic;
import com.Nxer.TwistSpaceTechnology.network.TST_Network;
import com.Nxer.TwistSpaceTechnology.util.TextEnums;
import com.Nxer.TwistSpaceTechnology.util.TextLocalization;
import com.dreammaster.gthandler.CustomItemList;
import com.github.bartimaeusnek.bartworks.API.BorosilicateGlass;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_DynamoMulti;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_DynamoTunnel;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_EnergyMulti;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_EnergyTunnel;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_MetaTileEntity_MultiblockBase_EM;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureUtility;

import advsolar.common.tiles.TileEntitySolarPanel;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import emt.tile.solar.TileEntitySolarBase;
import gregtech.api.GregTech_API;
import gregtech.api.enums.*;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Dynamo;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Energy;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.*;
import gregtech.common.blocks.GT_Block_Casings_Abstract;
import gtPlusPlus.core.material.ALLOY;
import io.netty.buffer.ByteBuf;

public class TST_BigBroArray extends GT_MetaTileEntity_MultiblockBase_EM {

    private ItemStack machines;

    private int maxParallelism = 256;

    private String machineType = null;

    private int machineTier = Byte.MAX_VALUE;

    // affects energy hatch
    private int glassTier = Byte.MAX_VALUE;

    // affects machines tier that can be put into array
    private int frameTier = Integer.MAX_VALUE;

    // bonus processing speed and reduce power cost
    private HeatingCoilLevel coilTier = HeatingCoilLevel.MAX;

    // affects maxparallelism
    private int parallelismTier = 5;

    // affects dynamo
    private int casingTier = 14;

    // affects max parallelism
    private int addonCount = 0;
    private String mode;

    private static final String MODE_GENERATOR = "generator";

    private static final String MODE_PROCESSOR = "processor";

    private TileEntity solarTE;

    private static String[] tierNames = new String[] { "LV", "MV", "HV", "EV", "IV", "LuV", "ZPM", "UV", "UHV", "UEV",
        "UIV", "UMV", "UXV", "MAX" };

    // UHV tier is called 'MAX' in legacy GT
    private static String[] tierNamesCasing = new String[] { "LV", "MV", "HV", "EV", "IV", "LuV", "ZPM", "UV", "MAX",
        "UEV", "UIV", "UMV", "UXV" };

    @SideOnly(Side.CLIENT)
    public static ITexture[] DEFAULT_FRONT_ACTIVE;

    @SideOnly(Side.CLIENT)
    public static ITexture[] DEFAULT_FRONT_IDLE;

    @SideOnly(Side.CLIENT)
    private static ITexture[] DEFAULT_CASING_TEXTURE;

    // spotless:off
    public static final String[][] PROCESSING_MACHINE_LIST = new String[][] {
        // OP
        { "Macerator", "Macerator" }, { "OreWasher", "OreWashingPlant" }, { "ChemicalBath", "ChemicalBath" },
        { "ThermalCentrifuge", "ThermalCentrifuge" },
        // Processing
        { "E_Furnace", "ElectricFurnace" }, { "ArcFurnace", "ArcFurnace" }, { "Bender", "BendingMachine" },
        { "Wiremill", "Wiremill" }, { "Lathe", "Lathe" }, { "Hammer", "ForgeHammer" }, { "Extruder", "Extruder" },
        { "FluidExtractor", "FluidExtractor" }, { "Compressor", "Compressor" }, { "Press", "FormingPress" },
        { "FluidSolidifier", "FluidSolidifier" }, { "Extractor", "Extractor" },
        { "LaserEngraver", "PrecisionLaserEngraver" }, { "Autoclave", "Autoclave" }, { "Mixer", "Mixer" },
        { "AlloySmelter", "AlloySmelter" }, { "Electrolyzer", "Electrolyzer" }, { "Sifter", "SiftingMachine" },
        { "ChemicalReactor", "ChemicalReactor" }, { "ElectromagneticSeparator", "ElectromagneticSeparator" },
        { "Recycler", "Recycler" }, { "Massfab", "MassFabricator" }, { "Centrifuge", "Centrifuge" },
        { "Cutter", "CuttingMachine" }, { "Assembler", "AssemblingMachine" }, { "CircuitAssembler", "CircuitAssembler" }
        // TODO: bartworks bio lab
    };

    // spotless:off
    private static final String[][] PATTERN_CORE = new String[][]{
        {
            "   AAAAA   ",
            "  AAAAAAA  ",
            " AAAAAAAAA ",
            "AAAAAAAAAAA",
            "AAAAAAAAAAA",
            "AAAAAAAAAAA",
            "AAAAAAAAAAA",
            "AAAAAAAAAAA",
            " AAAAAAAAA ",
            "  AAAAAAA  ",
            "   AAAAA   "
        },{
        "   CCCCC   ",
        "  CCCCCCC  ",
        " CCCCCCCCC ",
        "CCCCCCCCCCC",
        "CCCCCCCCCCC",
        "CCCCCCCCCCC",
        "CCCCCCCCCCC",
        "CCCCCCCCCCC",
        " CCCCCCCCC ",
        "  CCCCCCC  ",
        "   CCCCC   "
    },{
        "           ",
        "           ",
        "   B   B   ",
        "  B     B  ",
        "           ",
        "           ",
        "           ",
        "  B     B  ",
        "   B   B   ",
        "           ",
        "           "
    },{
        "           ",
        "           ",
        "   B   B   ",
        "  B     B  ",
        "           ",
        "           ",
        "           ",
        "  B     B  ",
        "   B   B   ",
        "           ",
        "           "
    },{
        "           ",
        "           ",
        "   B   B   ",
        "  B     B  ",
        "    DDD    ",
        "    DDD    ",
        "    DDD    ",
        "  B     B  ",
        "   B   B   ",
        "           ",
        "           "
    }, {
        "           ",
        "           ",
        "   B   B   ",
        "  B     B  ",
        "    D~D    ",
        "    D D    ",
        "    DDD    ",
        "  B     B  ",
        "   B   B   ",
        "           ",
        "           "
    },{
        "           ",
        "           ",
        "   B   B   ",
        "  B     B  ",
        "    DDD    ",
        "    DDD    ",
        "    DDD    ",
        "  B     B  ",
        "   B   B   ",
        "           ",
        "           "
    },{
        "     F     ",
        "    EEE    ",
        "   EEEEE   ",
        "  EEEEEEE  ",
        " EEEEEEEEE ",
        "FEEEEEEEEEF",
        " EEEEEEEEE ",
        "  EEEEEEE  ",
        "   EEEEE   ",
        "    EEE    ",
        "     F     "
    }};

    private static final String[][] PATTERN_ADDON = new String[][]{{
        "              ",
        "              ",
        "              ",
        "              ",
        "    A         ",
        "              ",
        "              ",
        "              ",
        "              "
    },{
        "              ",
        "              ",
        "              ",
        "   AAA        ",
        "   ABA        ",
        "   AAA        ",
        "              ",
        "              ",
        "              "
    },{
        "              ",
        "              ",
        "   AAA        ",
        "  ABBBA       ",
        "  ABBBA       ",
        "  ABBBA       ",
        "   AAA        ",
        "              ",
        "              "
    },{
        "              ",
        "   AAA        ",
        "  ABBBA       ",
        " ABBBBBA      ",
        " ABBBBBA      ",
        " ABBBBBA      ",
        "  ABBBA       ",
        "   AAA        ",
        "              "
    },{
        "  AAAAA       ",
        " AABBBAA      ",
        "AABBBBBAA     ",
        "ABBBBBBBA     ",
        "ABBBBBBBA     ",
        "ABBBBBBBA     ",
        "AABBBBBAA     ",
        " AABBBAA      ",
        "  AAAAA       "
    },{
        "  AAAAA       ",
        " AABBBAA      ",
        "AABBBBBAA     ",
        "ABBBBBBBA     ",
        "ABBBBBBBA     ",
        "ABBBBBBBA     ",
        "AABBBBBAA     ",
        " AABBBAA      ",
        "  AAAAA       "
    },{
        "              ",
        "   AAA        ",
        "  ABBBA       ",
        " ABBBBBA      ",
        " ABBBBBA      ",
        " ABBBBBA      ",
        "  ABBBA       ",
        "   AAA        ",
        "              "
    },{
        "              ",
        "   CCC        ",
        "  CAAAC       ",
        " CABBBAC      ",
        " CABBBAC      ",
        " CABBBAC      ",
        "  CAAAC       ",
        "   CCC        ",
        "              "
    },{
        "              ",
        "   CCC        ",
        "  C   C       ",
        " C AAA C      ",
        " C ABA C      ",
        " C AAA C      ",
        "  C   C       ",
        "   CCC        ",
        "              "
    },{
        "              ",
        "   CCC        ",
        "  C   C       ",
        " C     C      ",
        " C  A  C      ",
        " C     C      ",
        "  C   C       ",
        "   CCC        ",
        "              "
    },{
        "              ",
        "   CCC        ",
        "  C   C       ",
        " C     C      ",
        " C  E  C      ",
        " C     C      ",
        "  C   C       ",
        "   CCC        ",
        "              "
    },{
        "              ",
        "   CCC        ",
        "  C   C       ",
        " C     C      ",
        " C  E  C      ",
        " C     C      ",
        "  C   C       ",
        "   CCC        ",
        "              "
    },{
        "              ",
        "   CCC        ",
        "  C   C       ",
        " C     C      ",
        " C  E  C      ",
        " C     C      ",
        "  C   C       ",
        "   CCC        ",
        "              "
    },{
        "              ",
        "   CCC        ",
        "  C   C       ",
        " C EEE C      ",
        " C EEE C      ",
        " C EEE C      ",
        "  C   C       ",
        "   CCC        ",
        "              "
    },{
        "DDDDDDDDD     ",
        "DDDDDDDDD     ",
        "DDEEEEEDD     ",
        "DDEEEEEDD     ",
        "DDEEEEEFFFFFFF",
        "DDEEEEEDD     ",
        "DDEEEEEDD     ",
        "DDDDDDDDD     ",
        "DDDDDDDDD     "
    }};
    // spotless:on

    public static final Map<String, String> overlayMapping = new HashMap<>() {

        {
            put("Macerator", "macerator");
            put("OreWasher", "ore_washer");
            put("ChemicalBath", "chemical_bath");
            put("ThermalCentrifuge", "thermal_centrifuge");
            put("E_Furnace", "electric_furnace");
            put("ArcFurnace", "arc_furnace");
            put("Bender", "bender");
            put("Wiremill", "wiremill");
            put("Lathe", "lathe");
            put("Hammer", "hammer");
            put("Extruder", "extruder");
            put("FluidExtractor", "fluid_extractor");
            put("Compressor", "compressor");
            put("Press", "press");
            put("FluidSolidifier", "fluid_solidifier");
            put("Extractor", "extractor");
            put("LaserEngraver", "laser_engraver");
            put("Autoclave", "autoclave");
            put("Mixer", "mixer");
            put("AlloySmelter", "alloy_smelter");
            put("Electrolyzer", "electrolyzer");
            put("ElectromagneticSeparator", "electromagnetic_separator");
            put("Recycler", "recycler");
            put("Massfab", "amplifab");
            put("Centrifuge", "centrifuge");
            put("Cutter", "cutter");
            put("Assembler", "assembler");
            put("CircuitAssembler", "circuitassembler");
        }
    };

    public static final Map<String, Field> recipeBackendRefMapping = new HashMap<>() {

        {
            try {
                put("Macerator", RecipeMaps.class.getDeclaredField("maceratorRecipes"));
                put("OreWasher", RecipeMaps.class.getDeclaredField("oreWasherRecipes"));
                put("ChemicalBath", RecipeMaps.class.getDeclaredField("chemicalBathRecipes"));
                put("ThermalCentrifuge", RecipeMaps.class.getDeclaredField("thermalCentrifugeRecipes"));
                put("E_Furnace", RecipeMaps.class.getDeclaredField("furnaceRecipes"));
                put("ArcFurnace", RecipeMaps.class.getDeclaredField("arcFurnaceRecipes"));
                put("Bender", RecipeMaps.class.getDeclaredField("benderRecipes"));
                put("Wiremill", RecipeMaps.class.getDeclaredField("wiremillRecipes"));
                put("Lathe", RecipeMaps.class.getDeclaredField("latheRecipes"));
                put("Hammer", RecipeMaps.class.getDeclaredField("hammerRecipes"));
                put("Extruder", RecipeMaps.class.getDeclaredField("extruderRecipes"));
                put("FluidExtractor", RecipeMaps.class.getDeclaredField("fluidExtractionRecipes"));
                put("Compressor", RecipeMaps.class.getDeclaredField("compressorRecipes"));
                put("Press", RecipeMaps.class.getDeclaredField("formingPressRecipes"));
                put("FluidSolidifier", RecipeMaps.class.getDeclaredField("fluidSolidifierRecipes"));
                put("Extractor", RecipeMaps.class.getDeclaredField("extractorRecipes"));
                put("LaserEngraver", RecipeMaps.class.getDeclaredField("laserEngraverRecipes"));
                put("Autoclave", RecipeMaps.class.getDeclaredField("autoclaveRecipes"));
                put("Mixer", RecipeMaps.class.getDeclaredField("mixerNonCellRecipes"));
                put("AlloySmelter", RecipeMaps.class.getDeclaredField("alloySmelterRecipes"));
                put("Electrolyzer", RecipeMaps.class.getDeclaredField("electrolyzerNonCellRecipes"));
                put("ElectromagneticSeparator", RecipeMaps.class.getDeclaredField("electroMagneticSeparatorRecipes"));
                put("Recycler", RecipeMaps.class.getDeclaredField("recyclerRecipes"));
                put("Massfab", RecipeMaps.class.getDeclaredField("massFabFakeRecipes"));
                put("Centrifuge", RecipeMaps.class.getDeclaredField("centrifugeNonCellRecipes"));
                put("Cutter", RecipeMaps.class.getDeclaredField("cutterRecipes"));
                put("Assembler", RecipeMaps.class.getDeclaredField("assemblerRecipes"));
                put("CircuitAssembler", RecipeMaps.class.getDeclaredField("circuitAssemblerRecipes"));
                put("Diesel", RecipeMaps.class.getDeclaredField("dieselFuels"));
                put("Gas_Turbine", RecipeMaps.class.getDeclaredField("gasTurbineFuels"));
                // no recipe map for steam, 2MB steam for 1EU, and 1/80mb distilled water, 1 mb sc for 100EU, and 1mb
                // steam
            } catch (Exception e) {

            }
        }
    };

    // added by getDieselGeneratorsForArray in postInit phase
    public static final Map<String, ItemStack[]> GENERATORS = new HashMap<>();
    public static final String[] GT_GENERATOR_MACHINE_LIST = new String[] { "Diesel", "Gas_Turbine", "Steam_Turbine",
        "SemiFluid", "Naquadah" };

    public static final String[] INTER_MOD_GENERATORS = new String[] { "ASP_Solar", "EMT_Solar" };

    private static final Map<String, float[]> GENERATOR_EFFICIENCY = new HashMap<>() {

        {
            put("Diesel", new float[] { 0.95f, 0.9f, 0.85f, 0.8f, 0.75f });
            put("Gas_Turbine", new float[] { 0.95f, 0.9f, 0.85f, 0.8f, 0.75f });
            put("Steam_Turbine", new float[] { 1f, 1f, 1f });
            put("Semi_Fluid", new float[] { 0.95f, 0.9f, 0.85f, 0.8f, 0.75f });
            put("Naquadah", new float[] { 0.8f, 1f, 1.5f, 2f, 2.5f });
        }
    };

    public static final String GENERATOR_NQ = "Generator_Naquadah";

    private static List<Pair<Block, Integer>> FRAMES;

    private static List<Pair<Block, Integer>> PARALLELISM_CASINGS;

    private static List<Pair<Block, Integer>> MACHINE_CASINGS;
    /*
     * core Structure:
     * Blocks:
     * A -> ofBlock...(BW_GlasBlocks, 5, ...); --channel that restricts energy hatch
     * B -> ofBlock...(block.Pikyonium64B.frame, 0, ...); --channel that restricts machine level that array can accept
     * C -> ofBlock...(gt.blockcasings, 6, ...); -- (Machine casing)casing that restricts dynamo hatch
     * D -> ofBlock...(gt.blockcasings4, 0, ...); --robust tungstensteel
     * E -> ofBlock...(gt.blockcasings4, 10, ...); --stainless steel, cheap
     * Tiles:
     * Special Tiles:
     * F -> ofSpecialTileAdder(gregtech.api.metatileentity.BaseMetaPipeEntity, ...); Laser vacuum pipe casing
     * Structure:
     * Blocks:
     * A -> ofBlock...(BW_GlasBlocks, 5, ...); --channel that restricts energy hatch
     * B -> ofBlock...(MetaBlockCasing01, 3, ...); -- casing that gives additional parallelism
     * C -> ofBlock...(block.Pikyonium64B.frame, 0, ...); --channel that restricts machine level that array can accept
     * D -> ofBlock...(gt.blockcasings4, 10, ...); -- stainless steel, cheap
     * E -> ofBlock...(gt.blockcasings5, 0, ...); --coil that gives bonus
     * Tiles:
     * Special Tiles:
     * D -> ofSpecialTileAdder(gregtech.api.metatileentity.BaseMetaPipeEntity, ...); // You will probably want to change
     * it to something else
     */

    private static IStructureDefinition<TST_BigBroArray> STRUCTURE_DEFINITION;

    @SideOnly(Side.CLIENT)
    private ITexture[] activeTextures;

    @SideOnly(Side.CLIENT)
    private ITexture[] idleTextures;

    public static void initializeMaterials() {
        MACHINE_CASINGS = IntStream.range(0, tierNamesCasing.length)
            .mapToObj(i -> Pair.of(i, tierNamesCasing[i]))
            .map(pair -> {
                String name = "Casing_" + pair.getValue();
                try {

                    ItemStack itemStack = ItemList.valueOf(name)
                        .get(1);
                    int level = itemStack.getItemDamage();
                    return Pair.of(Block.getBlockFromItem(itemStack.getItem()), level);
                } catch (Exception ex) {
                    ItemStack itemStack = CustomItemList.valueOf(name)
                        .get(1);
                    int level = itemStack.getItemDamage();
                    return Pair.of(Block.getBlockFromItem(itemStack.getItem()), level);
                }
            })
            .collect(Collectors.toList());
        FRAMES = Arrays.asList(
            Pair.of(
                Block.getBlockFromItem(
                    ALLOY.ARCANITE.getFrameBox(1)
                        .getItem()),
                1), // IV
            Pair.of(
                Block.getBlockFromItem(
                    ALLOY.ZERON_100.getFrameBox(1)
                        .getItem()),
                2), // LuV
            Pair.of(
                Block.getBlockFromItem(
                    ALLOY.PIKYONIUM.getFrameBox(1)
                        .getItem()),
                3), // ZPM
            Pair.of(
                Block.getBlockFromItem(
                    ALLOY.BOTMIUM.getFrameBox(1)
                        .getItem()),
                4), // UV
            Pair.of(
                Block.getBlockFromItem(
                    ALLOY.ABYSSAL.getFrameBox(1)
                        .getItem()),
                5), // UHV
            Pair.of(
                Block.getBlockFromItem(
                    ALLOY.QUANTUM.getFrameBox(1)
                        .getItem()),
                6)); // UEV - MAX
        PARALLELISM_CASINGS = Arrays.asList(
            Pair.of(
                Block.getBlockFromItem(
                    GTCMItemList.ParallelismCasing0.get(1)
                        .getItem()),
                1),
            Pair.of(
                Block.getBlockFromItem(
                    GTCMItemList.ParallelismCasing1.get(1)
                        .getItem()),
                2),
            Pair.of(
                Block.getBlockFromItem(
                    GTCMItemList.ParallelismCasing2.get(1)
                        .getItem()),
                3),
            Pair.of(
                Block.getBlockFromItem(
                    GTCMItemList.ParallelismCasing3.get(1)
                        .getItem()),
                4),
            Pair.of(
                Block.getBlockFromItem(
                    GTCMItemList.ParallelismCasing4.get(1)
                        .getItem()),
                5));
    }

    public static int getFrameTier(Block block, int meta) {
        for (int i = 0; i < FRAMES.size(); i++) {
            if (block == FRAMES.get(i)
                .getKey()) {
                return i + 1;
            }
        }
        return 0;
    }

    public static int getParallelismCasingTier(Block block, int meta) {
        if (block == GTCMItemList.ParallelismCasing0.getBlock()) {
            return meta - 2;
        }
        return 0;
    }

    public void setCoilTier(HeatingCoilLevel level) {
        this.coilTier = level.getTier() < coilTier.getLevel() ? level : coilTier;
    }

    public HeatingCoilLevel getCoilTier() {
        return coilTier;
    }

    public void setCasingTier(int casingTier) {
        this.casingTier = Math.min(casingTier, this.casingTier);
    }

    public int getCasingTier() {
        return casingTier;
    }

    public static void initializeStructure() {
        STRUCTURE_DEFINITION = StructureDefinition.<TST_BigBroArray>builder()
            .addShape("core", StructureUtility.transpose(PATTERN_CORE))
            .addElement(
                'D',
                GT_HatchElementBuilder.<TST_BigBroArray>builder()
                    .atLeast(
                        GT_HatchElement.Maintenance,
                        GT_HatchElement.InputBus.or(GT_HatchElement.InputHatch),
                        GT_HatchElement.OutputBus.or(GT_HatchElement.OutputHatch),
                        GT_HatchElement.Muffler)
                    .adder(TST_BigBroArray::addToMachineList)
                    .dot(1)
                    .casingIndex(((GT_Block_Casings_Abstract) GregTech_API.sBlockCasings4).getTextureIndex(0))
                    .buildAndChain(GregTech_API.sBlockCasings4, 0))
            .addElement(
                'A',
                StructureUtility.withChannel(
                    "glass",
                    BorosilicateGlass.ofBoroGlass(
                        (byte) Byte.MAX_VALUE,
                        (te, tier) -> te.glassTier = Math.min(tier, te.glassTier),
                        (te) -> (byte) te.glassTier)))
            .addElement(
                'B',
                StructureUtility.withChannel(
                    "frame",
                    StructureUtility.ofBlocksTiered(
                        TST_BigBroArray::getFrameTier,
                        FRAMES,
                        Integer.MAX_VALUE,
                        (te, tier) -> te.frameTier = Math.min(tier, te.frameTier),
                        (te) -> te.frameTier)))
            .addElement('E', StructureUtility.ofBlock(GregTech_API.sBlockCasings4, 1))
            .addElement(
                'F',
                GT_HatchElementBuilder.<TST_BigBroArray>builder()
                    .atLeast(
                        HatchElement.DynamoMulti.or(GT_HatchElement.ExoticEnergy)
                            .or(GT_HatchElement.Dynamo))
                    .adder(TST_BigBroArray::addToMachineList)
                    .dot(2)
                    .casingIndex(((GT_Block_Casings_Abstract) GregTech_API.sBlockCasings4).getTextureIndex(1))
                    .buildAndChain(GregTech_API.sBlockCasings4, 1))
            .addElement(
                'C',
                StructureUtility.withChannel(
                    "casing",
                    StructureUtility.ofBlocksTiered(
                        (block, meta) -> meta,
                        MACHINE_CASINGS,
                        14,
                        TST_BigBroArray::setCasingTier,
                        TST_BigBroArray::getCasingTier)))

            .addShape("addon1", StructureUtility.transpose(PATTERN_ADDON))
            .addElement('D', StructureUtility.ofBlock(GregTech_API.sBlockCasings4, 1))
            .addElement(
                'E',
                StructureUtility.withChannel(
                    "coil",
                    GT_StructureUtility.ofCoil(TST_BigBroArray::setCoilTier, TST_BigBroArray::getCoilTier)))
            .addElement(
                'C',
                StructureUtility.withChannel(
                    "frame",
                    StructureUtility.ofBlocksTiered(
                        TST_BigBroArray::getFrameTier,
                        FRAMES,
                        Integer.MAX_VALUE,
                        (te, tier) -> te.frameTier = Math.min(tier, te.frameTier),
                        (te) -> te.frameTier)))
            .addElement(
                'A',
                StructureUtility.withChannel(
                    "glass",
                    BorosilicateGlass.ofBoroGlass(
                        Byte.MAX_VALUE,
                        (te, tier) -> te.glassTier = Math.min(tier, te.glassTier),
                        (te) -> (byte) te.glassTier)))
            .addElement(
                'B',
                StructureUtility.withChannel(
                    "parallelism",
                    StructureUtility.ofBlocksTiered(
                        TST_BigBroArray::getParallelismCasingTier,
                        PARALLELISM_CASINGS,
                        5,
                        (te, tier) -> te.parallelismTier = Math.min(tier, te.parallelismTier),
                        (te) -> te.parallelismTier)))
            .addElement(
                'F',
                StructureUtility.ofBlock(
                    com.github.technus.tectech.thing.CustomItemList.LASERpipe.getBlock(),
                    com.github.technus.tectech.thing.CustomItemList.LASERpipe.get(1)
                        .getItemDamage()))
            .build();
    }

    @SideOnly(Side.CLIENT)
    public static void initializeDefaultTextures() {
        DEFAULT_FRONT_ACTIVE = new ITexture[] {
            Textures.BlockIcons.getCasingTextureForId(GT_Utility.getCasingTextureIndex(GregTech_API.sBlockCasings4, 0)),
            TextureFactory.builder()
                .addIcon(OVERLAY_FRONT_ASSEMBLY_LINE_ACTIVE)
                .extFacing()
                .build(),
            TextureFactory.builder()
                .addIcon(OVERLAY_FRONT_ASSEMBLY_LINE_ACTIVE_GLOW)
                .extFacing()
                .glow()
                .build(), };

        DEFAULT_FRONT_IDLE = new ITexture[] {
            Textures.BlockIcons.getCasingTextureForId(GT_Utility.getCasingTextureIndex(GregTech_API.sBlockCasings4, 0)),
            TextureFactory.builder()
                .addIcon(OVERLAY_FRONT_ASSEMBLY_LINE)
                .extFacing()
                .build(),
            TextureFactory.builder()
                .addIcon(OVERLAY_FRONT_ASSEMBLY_LINE_GLOW)
                .extFacing()
                .glow()
                .build(), };

        DEFAULT_CASING_TEXTURE = new ITexture[] { Textures.BlockIcons
            .getCasingTextureForId(GT_Utility.getCasingTextureIndex(GregTech_API.sBlockCasings4, 0)) };

    }

    @SideOnly(Side.CLIENT)
    private ITexture[] getActiveTextures(String machineType) {
        if (StringUtils.isEmpty(machineType)) {
            return DEFAULT_FRONT_ACTIVE;
        }
        String overlay = overlayMapping.get(machineType);
        if (overlay == null) {
            return DEFAULT_FRONT_ACTIVE;
        }
        String front = String.format("basicmachines/%s/OVERLAY_FRONT_ACTIVE", overlay);
        String frontGlow = String.format("basicmachines/%s/OVERLAY_FRONT_ACTIVE_GLOW", overlay);
        CustomIcon frontIcon = new CustomIcon(front);
        frontIcon.run();
        CustomIcon frontGlowIcon = new CustomIcon(frontGlow);
        frontGlowIcon.run();
        return new ITexture[] { TextureFactory.builder()
            .addIcon(MACHINE_CASING_ROBUST_TUNGSTENSTEEL)
            .build(),
            TextureFactory.builder()
                .addIcon(frontIcon)
                .build(),
            TextureFactory.builder()
                .addIcon(frontGlowIcon)
                .glow()
                .build() };
    }

    @SideOnly(Side.CLIENT)
    private ITexture[] getIdleTextures(String machineType) {
        if (StringUtils.isEmpty(machineType)) {
            return DEFAULT_FRONT_IDLE;
        }
        String overlay = overlayMapping.get(machineType);
        if (overlay == null) {
            return DEFAULT_FRONT_IDLE;
        }
        String front = String.format("basicmachines/%s/OVERLAY_FRONT", overlay);
        String frontGlow = String.format("basicmachines/%s/OVERLAY_FRONT_GLOW", overlay);
        CustomIcon frontIcon = new CustomIcon(front);
        frontIcon.run();
        CustomIcon frontGlowIcon = new CustomIcon(frontGlow);
        frontGlowIcon.run();
        return new ITexture[] { TextureFactory.builder()
            .addIcon(MACHINE_CASING_ROBUST_TUNGSTENSTEEL)
            .build(),
            TextureFactory.builder()
                .addIcon(frontIcon)
                .build(),
            TextureFactory.builder()
                .addIcon(frontGlowIcon)
                .glow()
                .build() };
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        if (machineType != null) {
            aNBT.setString("machineType", machineType);
        }
        aNBT.setInteger("tier", machineTier);
        if (mode != null) aNBT.setString("mode", mode);

        if (solarTE != null) {
            NBTTagCompound compound = new NBTTagCompound();
            solarTE.writeToNBT(compound);
            aNBT.setTag("solarTE", compound);
        }
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        if (machines != null) {
            machines.writeToNBT(nbtTagCompound);
        }
        aNBT.setTag("machines", nbtTagCompound);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        machineType = aNBT.getString("machineType");
        mode = aNBT.getString("mode");
        machineTier = aNBT.getInteger("tier");
        if (aNBT.hasKey("machines")) machines = ItemStack.loadItemStackFromNBT(aNBT.getCompoundTag("machines"));
        if (aNBT.hasKey("solarTE")) {
            NBTTagCompound compound = aNBT.getCompoundTag("solarTE");
            solarTE = Block.getBlockFromItem(machines.getItem())
                .createTileEntity(null, machines.getItemDamage());
            solarTE.readFromNBT(compound);
        }
    }

    public TST_BigBroArray(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
        this.useLongPower = true;
    }

    public TST_BigBroArray(String aName) {
        super(aName);
        this.useLongPower = true;
    }

    @Override
    public IStructureDefinition<? extends GT_MetaTileEntity_MultiblockBase_EM> getStructure_EM() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected ProcessingLogic createProcessingLogic() {
        GTCM_ProcessingLogic gtcm_processingLogic = new GTCM_ProcessingLogic();
        gtcm_processingLogic.setMaxParallelSupplier(() -> machines != null ? machines.stackSize : 1);
        gtcm_processingLogic.setSpeedBonus(1.1f);
        return gtcm_processingLogic;
    }

    @NotNull
    @Override
    public CheckRecipeResult checkProcessing_EM() {
        if (MODE_PROCESSOR.equals(mode)) {
            return super.checkProcessing_EM();
        } else if (MODE_GENERATOR.equals(mode)) {
            if ("ASP_Solar".equals(machineType) || "EMT_Solar".equals(machineType)) {
                mMaxProgresstime = 20;
                if (!solarTE.hasWorldObj()) {
                    solarTE.setWorldObj(getBaseMetaTileEntity().getWorld());
                }
                solarTE.updateEntity();
                if (solarTE instanceof TileEntitySolarPanel te) {
                    solarTE.updateEntity();
                    long energyOutput = ((long) te.storage) * machines.stackSize * 20;
                    fillAllDynamos(energyOutput);
                    te.storage = 0;
                    return CheckRecipeResultRegistry.SUCCESSFUL;
                } else if (solarTE instanceof TileEntitySolarBase te) {
                    te.checkConditions();
                    long energyOutput = te.energySource.getEnergyStored();
                    fillAllDynamos(energyOutput * machines.stackSize * 20);
                    te.energySource.drawEnergy(energyOutput);
                    return CheckRecipeResultRegistry.SUCCESSFUL;
                }
                return CheckRecipeResultRegistry.NO_RECIPE;
            } else {
                return CheckRecipeResultRegistry.NO_RECIPE;
            }
        } else {
            return CheckRecipeResultRegistry.NO_RECIPE;
        }
    }

    private void fillAllDynamos(long energy) {
        for (GT_MetaTileEntity_Hatch_Dynamo dynamo : mDynamoHatches) {
            long drain = Math.min(energy, dynamo.maxEUStore() - dynamo.getEUVar());
            energy -= drain;
            dynamo.setEUVar(dynamo.getEUVar() + drain);
        }
        for (GT_MetaTileEntity_Hatch_DynamoMulti dynamo : eDynamoMulti) {
            long drain = Math.min(energy, dynamo.maxEUStore() - dynamo.getEUVar());
            energy -= drain;
            dynamo.setEUVar(dynamo.getEUVar() + drain);
        }
    }

    @Override
    public String[] getStructureDescription(ItemStack stackSize) {
        return super.getStructureDescription(stackSize);
    }

    @Override
    protected GT_Multiblock_Tooltip_Builder getTooltip() {
        GT_Multiblock_Tooltip_Builder gt_multiblock_tooltip_builder = new GT_Multiblock_Tooltip_Builder()
            .addMachineType(TextEnums.BigBroArrayType.toString())
            .addInfo(TextEnums.BigBroArrayName.toString())
            .addInfo(TextEnums.BigBroArrayDesc1.toString())
            .addInfo(TextEnums.BigBroArrayDesc2.toString())
            .addInfo(TextEnums.BigBroArrayDesc3.toString())
            .addInfo(TextEnums.BigBroArrayDesc4.toString())
            .addInfo(TextEnums.BigBroArrayDesc5.toString())
            .addInfo(TextEnums.BigBroArrayDesc6.toString())
            .addInfo(TextEnums.BigBroArrayDesc7.toString())
            .addInfo(TextEnums.BigBroArrayDesc8.toString())
            .addInfo(TextEnums.BigBroArrayDesc9.toString())
            .addInfo(TextEnums.BigBroArrayDesc10.toString())
            .addInfo(TextEnums.StructureTooComplex.toString())
            .addInfo(TextLocalization.BLUE_PRINT_INFO);
        gt_multiblock_tooltip_builder.toolTipFinisher(TextLocalization.ModName);
        return gt_multiblock_tooltip_builder;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        if (machines != null) {
            try {
                Field field = recipeBackendRefMapping.get(machineType);
                if (field == null) {
                    return null;
                }
                RecipeMap<?> o = (RecipeMap<?>) field.get(null);
                return o;
            } catch (IllegalAccessException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public void construct(ItemStack itemStack, boolean hintsOnly) {
        buildPiece("core", itemStack, hintsOnly, 5, 5, 4);
    }

    @Override
    protected GT_Multiblock_Tooltip_Builder createTooltip() {
        return getTooltip();
    }

    @Override
    protected boolean checkMachine_EM(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        boolean checkPiece = checkPiece("core", 5, 5, 4);
        if (!checkPiece) return false;
        // dynamo hatch level follows casing level
        for (GT_MetaTileEntity_Hatch_Dynamo mDynamoHatch : mDynamoHatches) {
            if (mDynamoHatch.mTier > casingTier) {
                return false;
            }
        }
        for (GT_MetaTileEntity_Hatch_DynamoMulti gt_metaTileEntity_hatch_dynamoMulti : eDynamoMulti) {
            if (gt_metaTileEntity_hatch_dynamoMulti.mTier > casingTier
                || (gt_metaTileEntity_hatch_dynamoMulti instanceof GT_MetaTileEntity_Hatch_DynamoTunnel
                    && casingTier < 8)) {
                return false;
            }
        }
        // energy hatch level follows glass level
        for (GT_MetaTileEntity_Hatch_Energy mEnergyHatch : mEnergyHatches) {
            if (mEnergyHatch.mTier > glassTier) {
                return false;
            }
        }
        for (GT_MetaTileEntity_Hatch_EnergyMulti gt_metaTileEntity_hatch_energyMulti : eEnergyMulti) {
            if (gt_metaTileEntity_hatch_energyMulti.mTier > glassTier
                || (gt_metaTileEntity_hatch_energyMulti instanceof GT_MetaTileEntity_Hatch_EnergyTunnel
                    && glassTier < 6)) {
                return false;
            }
        }
        // 5 is place holder, max tier is 4
        maxParallelism = (64 << ((parallelismTier == 5 ? 0 : parallelismTier) * 2)) * (1 + addonCount);
        return checkPiece;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new TST_BigBroArray(mName);
    }

    @Override
    public int getPollutionPerTick(ItemStack aStack) {
        return machines != null ? machines.stackSize * 16 : 0;
    }

    @Override
    public void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ) {
        if (machines == null) {
            for (ItemStack storedInput : getStoredInputs()) {
                for (String[] machineType : PROCESSING_MACHINE_LIST) {
                    for (int i = 0; i < tierNames.length; i++) {
                        String tierName = tierNames[i];
                        String name = String.format("Machine_%s_%s", tierName, machineType[0]);
                        ItemStack machineTypeToBeUsed = null;
                        try {
                            ItemList itemList = ItemList.valueOf(name);
                            machineTypeToBeUsed = itemList.get(1);
                        } catch (IllegalArgumentException ex) {
                            name = String.format("%s%s", machineType[1], tierName);
                            try {
                                CustomItemList customItemList = CustomItemList.valueOf(name);
                                machineTypeToBeUsed = customItemList.get(1);
                            } catch (IllegalArgumentException e) {

                            }
                        }
                        if (machineTypeToBeUsed != null) {
                            if (GT_Utility.areStacksEqual(machineTypeToBeUsed, storedInput)) {
                                if (i < frameTier + 5 || addonCount == 0) {
                                    if (machines != null) {
                                        if (GT_Utility.areStacksEqual(machines, storedInput)) {
                                            machines.stackSize += Math
                                                .min(maxParallelism - machines.stackSize, storedInput.stackSize);
                                            storedInput.stackSize -= Math
                                                .min(maxParallelism - machines.stackSize, storedInput.stackSize);
                                        }
                                    } else {
                                        machines = storedInput.copy();
                                        machines.stackSize = Math.min(maxParallelism, machines.stackSize);
                                        storedInput.stackSize -= machines.stackSize;
                                        this.machineType = machineType[0];
                                        machineTier = i;
                                        mode = MODE_PROCESSOR;
                                    }
                                }
                            }
                        }
                    }
                }

                for (String machineType : INTER_MOD_GENERATORS) {
                    for (int i = 0; i < GENERATORS.get(machineType).length; i++) {
                        ItemStack machineTypeToBeUsed = GENERATORS.get(machineType)[i];
                        if (GT_Utility.areStacksEqual(storedInput, machineTypeToBeUsed)) {
                            // create dummy TE for solar generation
                            solarTE = Block.getBlockFromItem(machineTypeToBeUsed.getItem())
                                .createTileEntity(aPlayer.worldObj, machineTypeToBeUsed.getItemDamage());
                            if ("ASP_Solar".equals(machineType) && i >= (frameTier + 4) && addonCount > 0) continue;
                            // calculate tier with log
                            // (int)log(output / 8, 4) = LV(1), MV(2), HV(3), EV(4), IV(5), .......
                            if ("EMT_Solar".equals(machineType)
                                && (int) Math.floor(Math.log(((TileEntitySolarBase) solarTE).output / 8) / Math.log(4))
                                    >= (frameTier + 4)
                                && addonCount > 0) continue;
                            if (machines != null) {
                                if (GT_Utility.areStacksEqual(machines, storedInput)) {
                                    machines.stackSize += Math
                                        .min(maxParallelism - machines.stackSize, storedInput.stackSize);
                                    storedInput.stackSize -= Math
                                        .min(maxParallelism - machines.stackSize, storedInput.stackSize);
                                }
                            } else {
                                mode = MODE_GENERATOR;
                                machines = storedInput.copy();
                                machines.stackSize = Math.min(maxParallelism, machines.stackSize);
                                storedInput.stackSize -= machines.stackSize;
                                this.machineType = machineType;
                                solarTE.setWorldObj(aPlayer.worldObj);
                                int xCoord = getBaseMetaTileEntity().getXCoord();
                                int yCoord = getBaseMetaTileEntity().getYCoord() + 4;
                                int zCoord = getBaseMetaTileEntity().getZCoord();
                                solarTE.xCoord = xCoord;
                                solarTE.yCoord = yCoord;
                                solarTE.zCoord = zCoord;
                            }
                        }
                    }
                }
            }
            if (machineType != null) {
                GT_Utility.sendChatToPlayer(
                    aPlayer,
                    String.format(
                        "Machine [%s] is set, parallelism is %s",
                        machines.getDisplayName(),
                        machines.stackSize));
                int xCoord = getBaseMetaTileEntity().getXCoord();
                int yCoord = getBaseMetaTileEntity().getYCoord();
                int zCoord = getBaseMetaTileEntity().getZCoord();
                TST_Network.tst.sendToAll(new PackSyncMachineType(xCoord, yCoord, zCoord, machineType));
            }
        } else {
            GT_Utility.sendChatToPlayer(aPlayer, "Machines are sent to output bus");
            // clear
            machineType = null;
            addOutput(machines);
            machines = null;
            solarTE = null;
            mode = null;
            int xCoord = getBaseMetaTileEntity().getXCoord();
            int yCoord = getBaseMetaTileEntity().getYCoord();
            int zCoord = getBaseMetaTileEntity().getZCoord();
            TST_Network.tst.sendToAll(new PackSyncMachineType(xCoord, yCoord, zCoord, machineType));
        }
    }

    public static void addRecipes() {
        GT_Values.RA.addAssemblerRecipe(
            new ItemStack[] { ItemList.Processing_Array.get(16), ItemList.Robot_Arm_IV.get(32),
                ItemList.Emitter_IV.get(32), ItemList.Field_Generator_IV.get(32),
                GT_OreDictUnificator.get(OrePrefixes.wireGt01, Materials.SuperconductorIV, 64),
                GT_OreDictUnificator.get(OrePrefixes.wireGt01, Materials.SuperconductorIV, 64),
                GT_OreDictUnificator.get(OrePrefixes.wireGt01, Materials.SuperconductorIV, 64),
                GT_OreDictUnificator.get(OrePrefixes.wireGt01, Materials.SuperconductorIV, 64), },
            ALLOY.NITINOL_60.getFluidStack(24576),
            GTCMItemList.BigBroArray.get(1),
            20 * 1200,
            7680);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
        int colorIndex, boolean active, boolean redstoneLevel) {
        if (activeTextures == null) {
            activeTextures = getActiveTextures(machineType);
        }
        if (idleTextures == null) {
            idleTextures = getIdleTextures(machineType);
        }
        String machineType = ((TST_BigBroArray) baseMetaTileEntity.getMetaTileEntity()).machineType;
        if (machineType == null && baseMetaTileEntity.getWorld() != null) {
            TST_Network.tst.sendToServer(
                new PackRequestMachineType(
                    baseMetaTileEntity.getWorld().provider.dimensionId,
                    baseMetaTileEntity.getXCoord(),
                    baseMetaTileEntity.getYCoord(),
                    baseMetaTileEntity.getZCoord()));
        }
        if (side == facing) {
            if (active) {
                return activeTextures;
            }
            return idleTextures;
        }
        return DEFAULT_CASING_TEXTURE;
    }

    public static class PackRequestMachineType
        implements IMessageHandler<PackRequestMachineType, PackSyncMachineType>, IMessage {

        int worldId;

        int x;

        int y;

        int z;

        public PackRequestMachineType() {}

        public PackRequestMachineType(int worldId, int x, int y, int z) {
            this.worldId = worldId;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.worldId = buf.readInt();
            this.x = buf.readInt();
            this.y = buf.readInt();
            this.z = buf.readInt();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(worldId);
            buf.writeInt(x);
            buf.writeInt(y);
            buf.writeInt(z);
        }

        @Override
        public PackSyncMachineType onMessage(PackRequestMachineType message, MessageContext ctx) {
            WorldServer world = DimensionManager.getWorld(message.worldId);
            if (world != null) {
                TileEntity tileEntity = world.getTileEntity(message.x, message.y, message.z);
                if (tileEntity instanceof BaseMetaTileEntity) {
                    TST_BigBroArray array = (TST_BigBroArray) ((BaseMetaTileEntity) tileEntity).getMetaTileEntity();
                    return new PackSyncMachineType(message.x, message.y, message.z, array.machineType);
                }
            }
            return null;
        }
    }

    public static class PackSyncMachineType
        implements IMessageHandler<PackSyncMachineType, PackSyncMachineType>, IMessage {

        int x;
        int y;
        int z;
        String machineType;

        public PackSyncMachineType() {}

        public PackSyncMachineType(int x, int y, int z, String machineType) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.machineType = machineType;
        }

        @Override
        public PackSyncMachineType onMessage(PackSyncMachineType message, MessageContext ctx) {

            TileEntity tileEntity = Minecraft.getMinecraft().theWorld.getTileEntity(message.x, message.y, message.z);
            if (tileEntity instanceof BaseMetaTileEntity) {
                IMetaTileEntity metaTileEntity = ((BaseMetaTileEntity) tileEntity).getMetaTileEntity();
                if (metaTileEntity instanceof TST_BigBroArray) {
                    TST_BigBroArray bigbro = (TST_BigBroArray) metaTileEntity;
                    bigbro.idleTextures = bigbro.getIdleTextures(message.machineType);
                    bigbro.activeTextures = bigbro.getActiveTextures(message.machineType);
                    bigbro.machineType = message.machineType;
                }
            }
            return null;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.x = buf.readInt();
            this.y = buf.readInt();
            this.z = buf.readInt();
            byte[] bytes = new byte[buf.readShort()];
            buf.readBytes(bytes);
            this.machineType = new String(bytes, StandardCharsets.UTF_8);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(x);
            buf.writeInt(y);
            buf.writeInt(z);
            byte[] bytes = machineType != null ? machineType.getBytes(StandardCharsets.UTF_8) : new byte[0];
            buf.writeShort(bytes.length);
            buf.writeBytes(bytes);
        }
    }
}
