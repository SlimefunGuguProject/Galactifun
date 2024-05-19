package io.github.addoncommunity.galactifun.base;

import lombok.experimental.UtilityClass;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import io.github.addoncommunity.galactifun.Galactifun;
import io.github.addoncommunity.galactifun.api.items.Relic;
import io.github.addoncommunity.galactifun.api.items.spacesuit.SpaceSuit;
import io.github.addoncommunity.galactifun.api.items.spacesuit.SpaceSuitHelmet;
import io.github.addoncommunity.galactifun.api.items.spacesuit.SpaceSuitStat;
import io.github.addoncommunity.galactifun.api.items.spacesuit.SpaceSuitUpgrade;
import io.github.addoncommunity.galactifun.api.universe.attributes.atmosphere.Gas;
import io.github.addoncommunity.galactifun.api.worlds.AlienWorld;
import io.github.addoncommunity.galactifun.base.items.AssemblyTable;
import io.github.addoncommunity.galactifun.base.items.AtmosphericHarvester;
import io.github.addoncommunity.galactifun.base.items.AutomaticDoor;
import io.github.addoncommunity.galactifun.base.items.CircuitPress;
import io.github.addoncommunity.galactifun.base.items.DiamondAnvil;
import io.github.addoncommunity.galactifun.base.items.Electrolyzer;
import io.github.addoncommunity.galactifun.base.items.FusionReactor;
import io.github.addoncommunity.galactifun.base.items.LaunchPadCore;
import io.github.addoncommunity.galactifun.base.items.LaunchPadFloor;
import io.github.addoncommunity.galactifun.base.items.OxygenFiller;
import io.github.addoncommunity.galactifun.base.items.SpaceSuitUpgrader;
import io.github.addoncommunity.galactifun.base.items.StargateController;
import io.github.addoncommunity.galactifun.base.items.StargateRing;
import io.github.addoncommunity.galactifun.base.items.TechnologicalSalvager;
import io.github.addoncommunity.galactifun.base.items.knowledge.Observatory;
import io.github.addoncommunity.galactifun.base.items.knowledge.PlanetaryAnalyzer;
import io.github.addoncommunity.galactifun.base.items.protection.CoolingUnit;
import io.github.addoncommunity.galactifun.base.items.protection.ForcefieldGenerator;
import io.github.addoncommunity.galactifun.base.items.protection.IonDisperser;
import io.github.addoncommunity.galactifun.base.items.protection.OxygenSealer;
import io.github.addoncommunity.galactifun.base.items.protection.SpaceHeater;
import io.github.addoncommunity.galactifun.base.items.rockets.ChemicalRocket;
import io.github.addoncommunity.galactifun.base.items.rockets.IonRocket;
import io.github.addoncommunity.galactifun.core.CoreItemGroup;
import io.github.mooy1.infinitylib.machines.MachineBlock;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.MachineTier;
import io.github.thebusybiscuit.slimefun4.core.attributes.MachineType;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.utils.HeadTexture;
import io.github.thebusybiscuit.slimefun4.utils.LoreBuilder;

/**
 * Holds the base machines and setup
 */
@UtilityClass
public final class BaseItems {

    //<editor-fold desc="Space Suits, Oxygen, Upgrades" defaultstate="collapsed">
    public static final SlimefunItemStack OXYGEN_FILLER = new SlimefunItemStack(
            "OXYGEN_FILLER",
            new ItemStack(Material.QUARTZ_BLOCK),
            "&f氧气填充器",
            "",
            "&7让宇航服充满氧气",
            "",
            LoreBuilder.machine(MachineTier.AVERAGE, MachineType.MACHINE),
            LoreBuilder.powerPerSecond(100),
            LoreBuilder.speed(1)
    );
    public static final SlimefunItemStack SPACE_SUIT_UPGRADER = new SlimefunItemStack(
            "SPACE_SUIT_UPGRADER",
            new ItemStack(Material.IRON_BLOCK),
            "&f航天服升级模块",
            "",
            "&7为你的宇航服升级",
            "",
            LoreBuilder.machine(MachineTier.AVERAGE, MachineType.MACHINE),
            LoreBuilder.powerPerSecond(200),
            LoreBuilder.speed(1)
    );
    public static final SlimefunItemStack HEAT_RESISTANCE_UPGRADE = new SlimefunItemStack(
            "HEAT_RESISTANCE_UPGRADE",
            Material.IRON_BLOCK,
            "&7耐热性升级模块",
            "",
            "&8增强你的宇航服的耐热性"
    );
    public static final SlimefunItemStack COLD_RESISTANCE_UPGRADE = new SlimefunItemStack(
            "COLD_RESISTANCE_UPGRADE",
            Material.IRON_BLOCK,
            "&7抗寒性升级模块",
            "",
            "&8增强你的宇航服的抗寒性"
    );
    public static final SlimefunItemStack RADIATION_RESISTANCE_UPGRADE = new SlimefunItemStack(
            "RADIATION_RESISTANCE_UPGRADE",
            Material.IRON_BLOCK,
            "&7抗辐射升级模块",
            "",
            "&8增强你的宇航服的抗辐射"
    );
    public static final SlimefunItemStack SPACE_SUIT_HELMET = new SlimefunItemStack(
            "SPACE_SUIT_HELMET",
            Material.GLASS,
            "&f宇航服头盔",
            "",
            "&7基础宇航服头盔",
            "",
            LoreBuilder.RIGHT_CLICK_TO_USE
    );
    public static final SlimefunItemStack SPACE_SUIT_CHEST = new SlimefunItemStack(
            "SPACE_SUIT_CHEST",
            Material.IRON_CHESTPLATE,
            "&f宇航服胸甲",
            "",
            "&7基础宇航服胸甲",
            "",
            SpaceSuit.oxygenLore(0, 3600)
    );
    public static final SlimefunItemStack SPACE_SUIT_PANTS = new SlimefunItemStack(
            "SPACE_SUIT_PANTS",
            Material.IRON_LEGGINGS,
            "&f宇航服护腿",
            "",
            "&7基础宇航服护腿"
    );
    public static final SlimefunItemStack SPACE_SUIT_BOOTS = new SlimefunItemStack(
            "SPACE_SUIT_BOOTS",
            Material.IRON_BOOTS,
            "&f宇航服靴子",
            "",
            "&7基础宇航服靴子"
    );
    //</editor-fold>
    //<editor-fold desc="Random Stuff" defaultstate="collapsed">
    public static final SlimefunItemStack LAUNCH_PAD_CORE = new SlimefunItemStack(
            "LAUNCH_PAD_CORE",
            Material.STONE,
            "&f发射台核心",
            "",
            "&7需要用8个&f发射台地板&7包围发射台核心",
            "&7来使用",
            " ",
            "&4说明:",
            "&4火箭请不要直接放到发射台上，先把发射台核心换成普通方块,",
            "&4放上去之后再换成发射台核心，否则放火箭的时候会打开发射台核心的界面,",
            "&4导致火箭坏掉变成头!"
    );

    public static final SlimefunItemStack LAUNCH_PAD_FLOOR = new SlimefunItemStack(
            "LAUNCH_PAD_FLOOR",
            Material.STONE_SLAB,
            "&f发射台地板",
            "",
            "&7用于建造发射台"
    );

    public static final SlimefunItemStack CIRCUIT_PRESS = new SlimefunItemStack(
            "CIRCUIT_PRESS",
            Material.PISTON,
            "&f循环压力机",
            "",
            "&7建造各种物品",
            LoreBuilder.machine(MachineTier.ADVANCED, MachineType.MACHINE),
            LoreBuilder.powerBuffer(512),
            LoreBuilder.powerPerSecond(256)
    );
    public static final SlimefunItemStack ASSEMBLY_TABLE = new SlimefunItemStack(
            "ASSEMBLY_TABLE",
            Material.SMITHING_TABLE,
            "&f星系装配台",
            "",
            "&7建造各种物品",
            "",
            LoreBuilder.machine(MachineTier.END_GAME, MachineType.MACHINE),
            LoreBuilder.power(2048, " 每个物品")
    );
    public static final SlimefunItemStack STARGATE_RING = new SlimefunItemStack(
            "STARGATE_RING",
            Material.QUARTZ_BLOCK,
            "&9星门环",
            "",
            "&7用于建造星门"
    );
    public static final SlimefunItemStack STARGATE_CONTROLLER = new SlimefunItemStack(
            "STARGATE_CONTROLLER",
            Material.CHISELED_QUARTZ_BLOCK,
            "&9星门控制器",
            "",
            "&7用于控制星门"
    );
    public static final SlimefunItemStack OBSERVATORY = new SlimefunItemStack(
            "OBSERVATORY",
            Material.GLASS,
            "&f星系天文台",
            "",
            "&7允许您远程发现有关星球的基本信息",
            "&7仅适用于距离小于0.25光年",
            "&7(236520000000公里)的星球"
    );
    public static final SlimefunItemStack PLANETARY_ANALYZER = new SlimefunItemStack(
            "PLANETARY_ANALYZER",
            Material.SEA_LANTERN,
            "&f行星分析仪",
            "",
            "&7允许您发现有关当前行星的高级信息"
    );
    public static final SlimefunItemStack DIAMOND_ANVIL = new SlimefunItemStack(
            "DIAMOND_ANVIL",
            Material.PISTON,
            "&f钻石砧",
            "",
            "&7材料的密度压缩更加的微小",
            "&7让他完全变成其他物品",
            LoreBuilder.machine(MachineTier.END_GAME, MachineType.MACHINE),
            LoreBuilder.powerPerSecond(1024),
            LoreBuilder.powerBuffer(2048)
    );
    public static final SlimefunItemStack FUSION_REACTOR = new SlimefunItemStack(
            "FUSION_REACTOR",
            HeadTexture.NUCLEAR_REACTOR.getAsItemStack(),
            "&f聚变反应堆",
            "",
            "&7使用激光将氦-3颗粒熔合",
            "&7并在熔合过程中产生大量能量",
            "",
            LoreBuilder.powerPerSecond(65_536),
            LoreBuilder.powerBuffer(65_536)
    );
    public static final SlimefunItemStack ATMOSPHERIC_HARVESTER = new SlimefunItemStack(
            "ATMOSPHERIC_HARVESTER",
            GalactifunHead.ATMOSPHERIC_HARVESTER,
            "&f大气收集机",
            "",
            "&7收集大气中的气体",
            LoreBuilder.powerPerSecond(64),
            LoreBuilder.powerBuffer(128),
            "",
            "&f&o材质作者:haseir"
    );
    public static final SlimefunItemStack CHEMICAL_REACTOR = new SlimefunItemStack(
            "CHEMICAL_REACTOR",
            Material.SEA_LANTERN,
            "&f化学反应器",
            "",
            "&7化学反应产生新的化学物质",
            LoreBuilder.powerPerSecond(256),
            LoreBuilder.powerBuffer(512)
    );
    public static final SlimefunItemStack ELECTROLYZER = new SlimefunItemStack(
            "ELECTROLYZER",
            Material.LANTERN,
            "&f电解器",
            "",
            "&7用电把化学物质分解成别的成分",
            LoreBuilder.powerPerSecond(256),
            LoreBuilder.powerBuffer(512)
    );
    //</editor-fold>
    //<editor-fold desc="Protecting Blocks" defaultstate="collapsed">
    public static final SlimefunItemStack COOLING_UNIT_1 = new SlimefunItemStack(
            "COOLING_UNIT_1",
            HeadTexture.COOLING_UNIT.getTexture(),
            "&b空间冷却器 I",
            "",
            "&7效果级: 热",
            "&7保护级: 2",
            "&7范围: 1000个方块",
            LoreBuilder.powerPerSecond(1024),
            LoreBuilder.powerBuffer(2048)
    );
    public static final SlimefunItemStack COOLING_UNIT_2 = new SlimefunItemStack(
            "COOLING_UNIT_2",
            HeadTexture.COOLING_UNIT.getTexture(),
            "&b空间冷却器 II",
            "",
            "&7效果级: 热",
            "&7保护级: 4",
            "&7范围: 1500个方块",
            LoreBuilder.powerPerSecond(2048),
            LoreBuilder.powerBuffer(4096)
    );
    public static final SlimefunItemStack COOLING_UNIT_3 = new SlimefunItemStack(
            "COOLING_UNIT_3",
            HeadTexture.COOLING_UNIT.getTexture(),
            "&b空间冷却器 III",
            "",
            "&7效果级: 热",
            "&7保护级: 6",
            "&7范围: 2000个方块",
            LoreBuilder.powerPerSecond(3072),
            LoreBuilder.powerBuffer(6144)
    );
    public static final SlimefunItemStack SPACE_HEATER_1 = new SlimefunItemStack(
            "SPACE_HEATER_1",
            Material.SHROOMLIGHT,
            "&6空间加热器 I",
            "",
            "&7效果级: 冷",
            "&7保护级: 2",
            "&7范围: 1000个方块",
            LoreBuilder.powerPerSecond(512),
            LoreBuilder.powerBuffer(1024)
    );
    public static final SlimefunItemStack SPACE_HEATER_2 = new SlimefunItemStack(
            "SPACE_HEATER_2",
            Material.SHROOMLIGHT,
            "&6空间加热器 II",
            "",
            "&7效果级: 冷",
            "&7保护级: 4",
            "&7范围: 1500个方块",
            LoreBuilder.powerPerSecond(1024),
            LoreBuilder.powerBuffer(2048)
    );
    public static final SlimefunItemStack SPACE_HEATER_3 = new SlimefunItemStack(
            "SPACE_HEATER_3",
            Material.SHROOMLIGHT,
            "&6空间加热器 III",
            "",
            "&7效果级: 冷",
            "&7保护级: 6",
            "&7范围: 2000个方块",
            LoreBuilder.powerPerSecond(2048),
            LoreBuilder.powerBuffer(4096)
    );
    public static final SlimefunItemStack ION_DISPERSER_1 = new SlimefunItemStack(
            "ION_DISPERSER_1",
            Material.PRISMARINE,
            "&f离子分散器 I",
            "",
            "&7效果级: 辐射",
            "&7保护级: 2",
            "&7范围: 1000个方块",
            LoreBuilder.powerPerSecond(512),
            LoreBuilder.powerBuffer(1024)
    );
    public static final SlimefunItemStack ION_DISPERSER_2 = new SlimefunItemStack(
            "ION_DISPERSER_2",
            Material.PRISMARINE,
            "&f离子分散器 II",
            "",
            "&7效果级: Radiation",
            "&7保护级: 4",
            "&7范围: 1500个方块",
            LoreBuilder.powerPerSecond(1024),
            LoreBuilder.powerBuffer(2048)
    );
    public static final SlimefunItemStack OXYGEN_SEALER = new SlimefunItemStack(
            "OXYGEN_SEALER",
            Material.FURNACE,
            "&f氧气散播机",
            "",
            "&7用氧气填充密封区域",
            "范围: 1000个方块"
    );
    public static final SlimefunItemStack LANDING_HATCH = new SlimefunItemStack(
            "LANDING_HATCH",
            Material.IRON_TRAPDOOR,
            "&f起落舱",
            "",
            "&7火箭降落时会在下方的最高位置中",
            "&7里面为密封空间,可以填充氧气"
    );
    public static final SlimefunItemStack SUPER_FAN = new SlimefunItemStack(
            "SUPER_FAN",
            Material.WHITE_WOOL,
            "&7加强鼓风机",
            "",
            "&7将其放置在需要密封区域",
            "&7影响范围会扩大15%"
    );
    public static final SlimefunItemStack AUTOMATIC_DOOR = new SlimefunItemStack(
            "AUTOMATIC_DOOR",
            Material.OBSERVER,
            "&f自动门",
            "",
            "&7当您离开/进入时",
            "&7将会自动打开/关闭"
    );
    public static final SlimefunItemStack ENVIRONMENTAL_FORCEFIELD_GENERATOR = new SlimefunItemStack(
            "ENVIRONMENTAL_FORCEFIELD_GENERATOR",
            Material.DISPENSER,
            "&f试验性力场发生器",
            "",
            "&7可以创建一个保留气体，",
            "&7允许实体通过的力场"
    );
    //</editor-fold>
    //<editor-fold desc="Relics" defaultstate="collapsed">
    public static final SlimefunItemStack ENGINE_RELIC = new SlimefunItemStack(
            "ENGINE_RELIC",
            Material.SEA_LANTERN,
            "&f遗迹: 'Letagivd Yynmukav",
            "",
            "&7Dorepv 'lbarok 'tagivd yboths amukav",
            "&7uyigrene teuz'lopsi 'letagivd tote"
    );
    public static final SlimefunItemStack REACTOR_RELIC = new SlimefunItemStack(
            "REACTOR_RELIC",
            Material.BEACON,
            "&f遗迹: Rotarenegortkele Yynmukav",
            "",
            "&7Ovehcin zi 'taribos onhzom uyigrene",
            "&7oths lamud otk?"
    );
    public static final SlimefunItemStack COMPUTER_RELIC = new SlimefunItemStack(
            "COMPUTER_RELIC",
            GalactifunHead.CORE,
            "&f遗迹: Retuypmok Yyntnavk",
            "",
            "&7Aretupmoc eyenhcybo mehc eyertsyb ogonman",
            "&7einelsihcsar 'taled tugom aretuypmok eyntnavk"
    );
    public static final SlimefunItemStack BROKEN_SOLAR_PANEL_RELIC = new SlimefunItemStack(
            "BROKEN_SOLAR_PANEL_RELIC",
            Material.DAYLIGHT_DETECTOR,
            "&7遗迹: 'Lenap Ayanhcenlos Ayanamolop",
            "",
            "&7'Lenap ayanhcenlos ayarats 'nehco"
    );
    public static final SlimefunItemStack FALLEN_SATELLITE_RELIC = new SlimefunItemStack(
            "FALLEN_SATELLITE_RELIC",
            Material.CHISELED_QUARTZ_BLOCK,
            "&7遗迹: Kintups Yyhsdap",
            "",
            "&7Aben s lapu yyrotok, kintups"
    );
    public static final SlimefunItemStack TECHNOLOGICAL_SALVAGER = new SlimefunItemStack(
            "TECHNOLOGICAL_SALVAGER",
            GalactifunHead.CORE,
            "&f遗迹搜查机",
            "",
            "&7从遗迹中搜寻有价值的东西",
            LoreBuilder.powerPerSecond(64),
            LoreBuilder.powerBuffer(64)
    );
    //</editor-fold>
    //<editor-fold desc="Rock It" defaultstate="collapsed">
    private static final int TIER_ONE_FUEL = 10;
    private static final int TIER_ONE_STORAGE = 9;
    public static final SlimefunItemStack TIER_ONE = new SlimefunItemStack(
            "ROCKET_TIER_ONE",
            GalactifunHead.ROCKET,
            "&4一阶化工火箭",
            "",
            "&7燃料容量: " + TIER_ONE_FUEL,
            "&7可载重量: " + TIER_ONE_STORAGE,
            " ",
            "&4说明:",
            "&4火箭请不要直接放到发射台上，先把发射台核心换成普通方块,",
            "&4放上去之后再换成发射台核心，否则放火箭的时候会打开发射台核心的界面,",
            "&4导致火箭坏掉变成头!"
    );
    private static final int TIER_TWO_FUEL = 100;
    private static final int TIER_TWO_STORAGE = 18;
    public static final SlimefunItemStack TIER_TWO = new SlimefunItemStack(
            "ROCKET_TIER_TWO",
            GalactifunHead.ROCKET,
            "&4二阶化工火箭",
            "",
            "&7燃料容量: " + TIER_TWO_FUEL,
            "&7可载重量: " + TIER_TWO_STORAGE,
            " ",
            "&4说明:",
            "&4火箭请不要直接放到发射台上，先把发射台核心换成普通方块,",
            "&4放上去之后再换成发射台核心，否则放火箭的时候会打开发射台核心的界面,",
            "&4导致火箭坏掉变成头!"
    );
    private static final int TIER_THREE_FUEL = 500;
    private static final int TIER_THREE_STORAGE = 36;
    public static final SlimefunItemStack TIER_THREE = new SlimefunItemStack(
            "ROCKET_TIER_THREE",
            GalactifunHead.ROCKET,
            "&4三阶化工火箭",
            "",
            "&7燃料容量: " + TIER_THREE_FUEL,
            "&7可载重量: " + TIER_THREE_STORAGE,
            " ",
            "&4说明:",
            "&4火箭请不要直接放到发射台上，先把发射台核心换成普通方块,",
            "&4放上去之后再换成发射台核心，否则放火箭的时候会打开发射台核心的界面,",
            "&4导致火箭坏掉变成头!"
    );
    public static final SlimefunItemStack ION_ROCKET = new SlimefunItemStack(
            "ION_ROCKET",
            GalactifunHead.ION_ROCKET,
            "&b离子火箭",
            "",
            "&7它安装了专属的引擎",
            "&7该引擎会高速排出离子",
            "&7并提供极高的燃油效率",
            "",
            "&7燃料容量: 500",
            "&7可载重量: 18",
            " ",
            "&4说明:",
            "&4火箭请不要直接放到发射台上，先把发射台核心换成普通方块,",
            "&4放上去之后再换成发射台核心，否则放火箭的时候会打开发射台核心的界面,",
            "&4导致火箭坏掉变成头!"
    );
    //</editor-fold>

    public static void setup(Galactifun galactifun) {
        new OxygenFiller(CoreItemGroup.MACHINES, OXYGEN_FILLER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                BaseMats.ALUMINUM_COMPOSITE, BaseMats.REINFORCED_CHANNEL, BaseMats.ALUMINUM_COMPOSITE,
                BaseMats.ALUMINUM_COMPOSITE, BaseMats.FAN_BLADE, BaseMats.ALUMINUM_COMPOSITE,
                BaseMats.ALUMINUM_COMPOSITE, null, BaseMats.ALUMINUM_COMPOSITE
        }).setCapacity(200).setEnergyConsumption(100).setProcessingSpeed(1).register(galactifun);
        new SpaceSuitUpgrader(CoreItemGroup.MACHINES, SPACE_SUIT_UPGRADER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                BaseMats.ALUMINUM_COMPOSITE, SlimefunItems.HARDENED_GLASS, BaseMats.ALUMINUM_COMPOSITE,
                BaseMats.ALUMINUM_COMPOSITE, null, BaseMats.ALUMINUM_COMPOSITE,
                BaseMats.ALUMINUM_COMPOSITE, SlimefunItems.STEEL_INGOT, BaseMats.ALUMINUM_COMPOSITE
        }).setCapacity(400).setEnergyConsumption(200).setProcessingSpeed(1).register(galactifun);

        new SpaceSuitUpgrade(CoreItemGroup.EQUIPMENT, HEAT_RESISTANCE_UPGRADE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.COOLING_UNIT, SlimefunItems.COOLING_UNIT, SlimefunItems.COOLING_UNIT,
                SlimefunItems.COOLING_UNIT, new ItemStack(Material.IRON_BLOCK), SlimefunItems.COOLING_UNIT,
                SlimefunItems.COOLING_UNIT, SlimefunItems.COOLING_UNIT, SlimefunItems.COOLING_UNIT
        }, SpaceSuitStat.HEAT_RESISTANCE, 1).register(galactifun);
        new SpaceSuitUpgrade(CoreItemGroup.EQUIPMENT, COLD_RESISTANCE_UPGRADE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.HEATING_COIL, SlimefunItems.HEATING_COIL, SlimefunItems.HEATING_COIL,
                SlimefunItems.HEATING_COIL, new ItemStack(Material.IRON_BLOCK), SlimefunItems.HEATING_COIL,
                SlimefunItems.HEATING_COIL, SlimefunItems.HEATING_COIL, SlimefunItems.HEATING_COIL
        }, SpaceSuitStat.COLD_RESISTANCE, 1).register(galactifun);
        new SpaceSuitUpgrade(CoreItemGroup.EQUIPMENT, RADIATION_RESISTANCE_UPGRADE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.LEAD_INGOT, SlimefunItems.LEAD_INGOT, SlimefunItems.LEAD_INGOT,
                SlimefunItems.LEAD_INGOT, new ItemStack(Material.IRON_BLOCK), SlimefunItems.LEAD_INGOT,
                SlimefunItems.LEAD_INGOT, SlimefunItems.LEAD_INGOT, SlimefunItems.LEAD_INGOT
        }, SpaceSuitStat.RADIATION_RESISTANCE, 1).register(galactifun);

        new SpaceSuitHelmet(CoreItemGroup.EQUIPMENT, SPACE_SUIT_HELMET, RecipeType.ARMOR_FORGE, new ItemStack[] {
                BaseMats.ALUMINUM_COMPOSITE_SHEET, BaseMats.GOLD_FOIL, BaseMats.ALUMINUM_COMPOSITE_SHEET,
                SlimefunItems.REINFORCED_CLOTH, SlimefunItems.SCUBA_HELMET, SlimefunItems.REINFORCED_CLOTH,
                BaseMats.REINFORCED_CHANNEL, BaseMats.OXYGEN_REGENERATOR, BaseMats.FAN_BLADE
        }, 3, 0).register(galactifun);
        new SpaceSuit(CoreItemGroup.EQUIPMENT, SPACE_SUIT_CHEST, RecipeType.ARMOR_FORGE, new ItemStack[] {
                BaseMats.ALUMINUM_COMPOSITE_SHEET, SlimefunItems.REINFORCED_CLOTH, BaseMats.REINFORCED_CHANNEL,
                BaseMats.FAN_BLADE, SlimefunItems.HAZMAT_CHESTPLATE, BaseMats.FAN_BLADE,
                BaseMats.ALUMINUM_COMPOSITE_SHEET, BaseMats.ALUMINUM_COMPOSITE_SHEET, BaseMats.ALUMINUM_COMPOSITE_SHEET
        }, 3, 3600).register(galactifun);
        new SpaceSuit(CoreItemGroup.EQUIPMENT, SPACE_SUIT_PANTS, RecipeType.ARMOR_FORGE, new ItemStack[] {
                BaseMats.ALUMINUM_COMPOSITE_SHEET, SlimefunItems.HAZMAT_LEGGINGS, BaseMats.ALUMINUM_COMPOSITE_SHEET,
                BaseMats.ALUMINUM_COMPOSITE_SHEET, null, BaseMats.ALUMINUM_COMPOSITE_SHEET,
                BaseMats.ALUMINUM_COMPOSITE_SHEET, null, BaseMats.ALUMINUM_COMPOSITE_SHEET
        }, 2, 0).register(galactifun);
        new SpaceSuit(CoreItemGroup.EQUIPMENT, SPACE_SUIT_BOOTS, RecipeType.ARMOR_FORGE, new ItemStack[] {
                null, null, null,
                BaseMats.ALUMINUM_COMPOSITE_SHEET, null, BaseMats.ALUMINUM_COMPOSITE_SHEET,
                BaseMats.ALUMINUM_COMPOSITE_SHEET, SlimefunItems.HAZMAT_BOOTS, BaseMats.ALUMINUM_COMPOSITE_SHEET
        }, 2, 0).register(galactifun);

        new CircuitPress(CoreItemGroup.MACHINES, CIRCUIT_PRESS, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.HEATING_COIL, new ItemStack(Material.PISTON), SlimefunItems.HEATING_COIL,
                BaseMats.ALUMINUM_COMPOSITE_SHEET, null, BaseMats.ALUMINUM_COMPOSITE_SHEET,
                SlimefunItems.HEATING_COIL, new ItemStack(Material.PISTON), SlimefunItems.HEATING_COIL
        }).setCapacity(512).setEnergyConsumption(128).setProcessingSpeed(1).register(galactifun);

        new AssemblyTable(ASSEMBLY_TABLE, new ItemStack[] {
                SlimefunItems.STEEL_PLATE, SlimefunItems.ENHANCED_AUTO_CRAFTER, SlimefunItems.STEEL_PLATE,
                SlimefunItems.CARGO_MOTOR, BaseMats.ADVANCED_PROCESSING_UNIT, SlimefunItems.CARGO_MOTOR,
                SlimefunItems.REINFORCED_PLATE, SlimefunItems.REINFORCED_PLATE, SlimefunItems.REINFORCED_PLATE
        }, 2048).register(galactifun);

        new StargateRing(CoreItemGroup.COMPONENTS, STARGATE_RING, AssemblyTable.TYPE, new ItemStack[] {
                BaseMats.ULTRA_DUTY_SHEET, BaseMats.ULTRA_DUTY_SHEET, BaseMats.ULTRA_DUTY_SHEET, BaseMats.ULTRA_DUTY_SHEET, BaseMats.ULTRA_DUTY_SHEET, BaseMats.ULTRA_DUTY_SHEET,
                BaseMats.ULTRA_DUTY_SHEET, BaseMats.ENDER_BLOCK, BaseMats.ENDER_BLOCK, BaseMats.ENDER_BLOCK, BaseMats.ENDER_BLOCK, BaseMats.ULTRA_DUTY_SHEET,
                BaseMats.ULTRA_DUTY_SHEET, BaseMats.ENDER_BLOCK, BaseMats.ENDER_BLOCK, BaseMats.ENDER_BLOCK, BaseMats.ENDER_BLOCK, BaseMats.ULTRA_DUTY_SHEET,
                BaseMats.ULTRA_DUTY_SHEET, BaseMats.ENDER_BLOCK, BaseMats.ENDER_BLOCK, BaseMats.ENDER_BLOCK, BaseMats.ENDER_BLOCK, BaseMats.ULTRA_DUTY_SHEET,
                BaseMats.ULTRA_DUTY_SHEET, BaseMats.ENDER_BLOCK, BaseMats.ENDER_BLOCK, BaseMats.ENDER_BLOCK, BaseMats.ENDER_BLOCK, BaseMats.ULTRA_DUTY_SHEET,
                BaseMats.ULTRA_DUTY_SHEET, BaseMats.ULTRA_DUTY_SHEET, BaseMats.ULTRA_DUTY_SHEET, BaseMats.ULTRA_DUTY_SHEET, BaseMats.ULTRA_DUTY_SHEET, BaseMats.ULTRA_DUTY_SHEET
        }).register(galactifun);
        new StargateController(CoreItemGroup.COMPONENTS, STARGATE_CONTROLLER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                STARGATE_RING, BaseMats.GLOWSTONE_CIRCUIT, STARGATE_RING,
                BaseMats.REDSTONE_CIRCUIT, BaseMats.DIAMOND_CIRCUIT, BaseMats.REDSTONE_CIRCUIT,
                STARGATE_RING, BaseMats.LAPIS_CIRCUIT, STARGATE_RING
        }).register(galactifun);

        new ChemicalRocket(CoreItemGroup.ITEMS, TIER_ONE, AssemblyTable.TYPE, new ItemStack[] {
                null, null, BaseMats.NOSE_CONE, BaseMats.NOSE_CONE, null, null,
                null, null, BaseMats.HEAVY_DUTY_SHEET, BaseMats.HEAVY_DUTY_SHEET, null, null,
                null, BaseMats.HEAVY_DUTY_SHEET, BaseMats.ADVANCED_PROCESSING_UNIT, BaseMats.ADVANCED_PROCESSING_UNIT, BaseMats.HEAVY_DUTY_SHEET, null,
                BaseMats.HEAVY_DUTY_SHEET, BaseMats.FUEL_TANK, BaseMats.LIFE_SUPPORT_MODULE, BaseMats.LIFE_SUPPORT_MODULE, BaseMats.FUEL_TANK, BaseMats.HEAVY_DUTY_SHEET,
                BaseMats.HEAVY_DUTY_SHEET, BaseMats.FUEL_TANK, BaseMats.FUEL_TANK, BaseMats.FUEL_TANK, BaseMats.FUEL_TANK, BaseMats.HEAVY_DUTY_SHEET,
                BaseMats.HEAVY_DUTY_SHEET, null, BaseMats.ROCKET_ENGINE, BaseMats.ROCKET_ENGINE, null, BaseMats.HEAVY_DUTY_SHEET
        }, TIER_ONE_FUEL, TIER_ONE_STORAGE).register(galactifun);
        new ChemicalRocket(CoreItemGroup.ITEMS, TIER_TWO, AssemblyTable.TYPE, new ItemStack[] {
                null, null, BaseMats.NOSE_CONE, BaseMats.NOSE_CONE, null, null,
                null, null, BaseMats.HEAVY_DUTY_SHEET, BaseMats.HEAVY_DUTY_SHEET, null, null,
                null, BaseMats.HEAVY_DUTY_SHEET, BaseMats.ADVANCED_PROCESSING_UNIT, BaseMats.ADVANCED_PROCESSING_UNIT, BaseMats.HEAVY_DUTY_SHEET, null,
                BaseMats.HEAVY_DUTY_SHEET, BaseMats.FUEL_TANK, BaseMats.LIFE_SUPPORT_MODULE, BaseMats.LIFE_SUPPORT_MODULE, BaseMats.FUEL_TANK, BaseMats.HEAVY_DUTY_SHEET,
                BaseMats.HEAVY_DUTY_SHEET, BaseMats.FUEL_TANK, BaseMats.FUEL_TANK, BaseMats.FUEL_TANK, BaseMats.FUEL_TANK, BaseMats.HEAVY_DUTY_SHEET,
                BaseMats.HEAVY_DUTY_SHEET, null, BaseMats.ROCKET_ENGINE_2, BaseMats.ROCKET_ENGINE_2, null, BaseMats.HEAVY_DUTY_SHEET
        }, TIER_TWO_FUEL, TIER_TWO_STORAGE).register(galactifun);
        new ChemicalRocket(CoreItemGroup.ITEMS, TIER_THREE, AssemblyTable.TYPE, new ItemStack[] {
                null, null, BaseMats.NOSE_CONE, BaseMats.NOSE_CONE, null, null,
                null, null, BaseMats.ULTRA_DUTY_SHEET, BaseMats.ULTRA_DUTY_SHEET, null, null,
                null, BaseMats.HEAVY_DUTY_SHEET, BaseMats.ADVANCED_PROCESSING_UNIT, BaseMats.ADVANCED_PROCESSING_UNIT, BaseMats.ULTRA_DUTY_SHEET, null,
                BaseMats.ULTRA_DUTY_SHEET, BaseMats.FUEL_TANK_2, BaseMats.LIFE_SUPPORT_MODULE, BaseMats.LIFE_SUPPORT_MODULE, BaseMats.FUEL_TANK_2, BaseMats.ULTRA_DUTY_SHEET,
                BaseMats.ULTRA_DUTY_SHEET, BaseMats.FUEL_TANK_2, BaseMats.FUEL_TANK, BaseMats.FUEL_TANK, BaseMats.FUEL_TANK_2, BaseMats.ULTRA_DUTY_SHEET,
                BaseMats.ULTRA_DUTY_SHEET, null, BaseMats.ROCKET_ENGINE_3, BaseMats.ROCKET_ENGINE_3, null, BaseMats.ULTRA_DUTY_SHEET
        }, TIER_THREE_FUEL, TIER_THREE_STORAGE).register(galactifun);

        new SlimefunItem(CoreItemGroup.ITEMS, LANDING_HATCH, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                BaseMats.HEAVY_DUTY_SHEET, new ItemStack(Material.IRON_TRAPDOOR), BaseMats.HEAVY_DUTY_SHEET,
                BaseMats.SPACE_GRADE_PLATE, null, BaseMats.SPACE_GRADE_PLATE,
                BaseMats.SPACE_GRADE_PLATE, null, BaseMats.SPACE_GRADE_PLATE
        }).register(galactifun);

        new LaunchPadFloor(CoreItemGroup.ITEMS, BaseItems.LAUNCH_PAD_FLOOR, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                null, null, null,
                BaseMats.HEAVY_DUTY_SHEET, BaseMats.HEAVY_DUTY_SHEET, BaseMats.HEAVY_DUTY_SHEET,
                BaseMats.HEAVY_DUTY_SHEET, BaseMats.HEAVY_DUTY_SHEET, BaseMats.HEAVY_DUTY_SHEET,
        }).register(galactifun);

        new LaunchPadCore(CoreItemGroup.ITEMS, BaseItems.LAUNCH_PAD_CORE, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.REINFORCED_PLATE, BaseMats.NOZZLE, SlimefunItems.REINFORCED_PLATE,
                SlimefunItems.CARGO_MOTOR, SlimefunItems.OIL_PUMP, SlimefunItems.CARGO_MOTOR,
                SlimefunItems.REINFORCED_PLATE, BaseMats.ADVANCED_PROCESSING_UNIT, SlimefunItems.REINFORCED_PLATE,
        }).register(galactifun);

        new CoolingUnit(COOLING_UNIT_1, new ItemStack[] {
                BaseMats.ALUMINUM_COMPOSITE_SHEET, SlimefunItems.COOLING_UNIT, BaseMats.ALUMINUM_COMPOSITE_SHEET,
                SlimefunItems.COOLING_UNIT, BaseMats.FAN_BLADE, SlimefunItems.COOLING_UNIT,
                BaseMats.ALUMINUM_COMPOSITE_SHEET, SlimefunItems.COOLING_UNIT, BaseMats.ALUMINUM_COMPOSITE_SHEET
        }, 1).register(galactifun);

        new CoolingUnit(COOLING_UNIT_2, new ItemStack[] {
                BaseMats.SPACE_GRADE_PLATE, BaseMats.DRY_ICE, BaseMats.SPACE_GRADE_PLATE,
                BaseMats.DRY_ICE, COOLING_UNIT_1, BaseMats.DRY_ICE,
                BaseMats.SPACE_GRADE_PLATE, BaseMats.DRY_ICE, BaseMats.SPACE_GRADE_PLATE
        }, 2).register(galactifun);

        new CoolingUnit(COOLING_UNIT_3, new ItemStack[] {
                BaseMats.HEAVY_DUTY_SHEET, BaseMats.DRY_ICE, BaseMats.HEAVY_DUTY_SHEET,
                BaseMats.DRY_ICE, COOLING_UNIT_2, BaseMats.DRY_ICE,
                BaseMats.HEAVY_DUTY_SHEET, BaseMats.DRY_ICE, BaseMats.HEAVY_DUTY_SHEET
        }, 3).register(galactifun);

        new SpaceHeater(SPACE_HEATER_1, new ItemStack[] {
                BaseMats.ALUMINUM_COMPOSITE_SHEET, SlimefunItems.HEATING_COIL, BaseMats.ALUMINUM_COMPOSITE_SHEET,
                SlimefunItems.HEATING_COIL, BaseMats.FAN_BLADE, SlimefunItems.HEATING_COIL,
                BaseMats.ALUMINUM_COMPOSITE_SHEET, SlimefunItems.HEATING_COIL, BaseMats.ALUMINUM_COMPOSITE_SHEET
        }, 1).register(galactifun);

        new SpaceHeater(SPACE_HEATER_2, new ItemStack[] {
                BaseMats.SPACE_GRADE_PLATE, new ItemStack(Material.LAVA_BUCKET), BaseMats.SPACE_GRADE_PLATE,
                BaseMats.VENTSTONE, SPACE_HEATER_1, BaseMats.VENTSTONE,
                BaseMats.SPACE_GRADE_PLATE, SlimefunItems.HEATING_COIL, BaseMats.SPACE_GRADE_PLATE
        }, 2).register(galactifun);

        new SpaceHeater(SPACE_HEATER_3, new ItemStack[] {
                BaseMats.HEAVY_DUTY_SHEET, BaseMats.VOLCANIC_INGOT, BaseMats.HEAVY_DUTY_SHEET,
                BaseMats.VOLCANIC_INGOT, SPACE_HEATER_2, BaseMats.VOLCANIC_INGOT,
                BaseMats.HEAVY_DUTY_SHEET, BaseMats.BLISTERING_VOLCANIC_INGOT, BaseMats.HEAVY_DUTY_SHEET
        }, 3).register(galactifun);

        new IonDisperser(ION_DISPERSER_1, new ItemStack[] {
                BaseMats.ALUMINUM_COMPOSITE_SHEET, BaseMats.FAN_BLADE, BaseMats.ALUMINUM_COMPOSITE_SHEET,
                new ItemStack(Material.PRISMARINE_CRYSTALS), BaseMats.SULFUR_BLOCK, new ItemStack(Material.PRISMARINE_CRYSTALS),
                BaseMats.ALUMINUM_COMPOSITE_SHEET, BaseMats.VENTSTONE, BaseMats.ALUMINUM_COMPOSITE_SHEET
        }, 1).register(galactifun);

        new IonDisperser(ION_DISPERSER_2, new ItemStack[] {
                BaseMats.SPACE_GRADE_PLATE, BaseMats.FAN_BLADE, BaseMats.SPACE_GRADE_PLATE,
                BaseMats.SULFUR_BLOCK, ION_DISPERSER_1, BaseMats.SULFUR_BLOCK,
                BaseMats.SPACE_GRADE_PLATE, BaseMats.BLISTERING_VOLCANIC_INGOT, BaseMats.SPACE_GRADE_PLATE
        }, 2).register(galactifun);

        new Observatory(OBSERVATORY, new ItemStack[] {
                new ItemStack(Material.IRON_BLOCK), new ItemStack(Material.PISTON), new ItemStack(Material.IRON_BLOCK),
                new ItemStack(Material.PISTON), BaseMats.LUNAR_GLASS, new ItemStack(Material.PISTON),
                new ItemStack(Material.IRON_BLOCK), new ItemStack(Material.PISTON), new ItemStack(Material.IRON_BLOCK)
        }).register(galactifun);
        new PlanetaryAnalyzer(PLANETARY_ANALYZER, new ItemStack[] {
                BaseMats.TUNGSTEN_INGOT, SlimefunItems.GPS_TRANSMITTER_4, BaseMats.TUNGSTEN_INGOT,
                BaseMats.SPACE_GRADE_PLATE, SlimefunItems.ENERGIZED_CAPACITOR, BaseMats.SPACE_GRADE_PLATE,
                BaseMats.TUNGSTEN_INGOT, BaseMats.VOLCANIC_INGOT, BaseMats.TUNGSTEN_INGOT
        }).register(galactifun);
        new DiamondAnvil(DIAMOND_ANVIL, new ItemStack[] {
                BaseMats.ULTRA_DUTY_SHEET, new ItemStack(Material.GLASS), BaseMats.ULTRA_DUTY_SHEET,
                BaseMats.ULTRA_DUTY_SHEET, BaseMats.DIAMOND_ANVIL_CELL, BaseMats.ULTRA_DUTY_SHEET,
                BaseMats.ULTRA_DUTY_SHEET, new ItemStack(Material.ANVIL), BaseMats.ULTRA_DUTY_SHEET
        }).setCapacity(2048).setEnergyConsumption(512).setProcessingSpeed(1).register(galactifun);

        new OxygenSealer(OXYGEN_SEALER, new ItemStack[] {
                BaseMats.ALUMINUM_COMPOSITE_SHEET, BaseMats.FAN_BLADE, BaseMats.ALUMINUM_COMPOSITE_SHEET,
                BaseMats.ALUMINUM_COMPOSITE_SHEET, BaseMats.REINFORCED_CHANNEL, BaseMats.ALUMINUM_COMPOSITE_SHEET,
                BaseMats.ALUMINUM_COMPOSITE_SHEET, BaseMats.OXYGEN_REGENERATOR, BaseMats.ALUMINUM_COMPOSITE_SHEET
        }, 1000).register(galactifun);

        new SlimefunItem(CoreItemGroup.ITEMS, SUPER_FAN, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                BaseMats.ALUMINUM_COMPOSITE_SHEET, BaseMats.ALUMINUM_COMPOSITE_SHEET, BaseMats.ALUMINUM_COMPOSITE_SHEET,
                BaseMats.ALUMINUM_COMPOSITE_SHEET, BaseMats.FAN_BLADE, BaseMats.ALUMINUM_COMPOSITE_SHEET,
                BaseMats.ALUMINUM_COMPOSITE_SHEET, BaseMats.VENTSTONE, BaseMats.ALUMINUM_COMPOSITE_SHEET
        }).register(galactifun);

        new AutomaticDoor(AUTOMATIC_DOOR, new ItemStack[] {
                BaseMats.ALUMINUM_COMPOSITE_SHEET, new ItemStack(Material.OBSERVER), BaseMats.ALUMINUM_COMPOSITE_SHEET,
                BaseMats.ALUMINUM_COMPOSITE_SHEET, SlimefunItems.PROGRAMMABLE_ANDROID_MINER, BaseMats.ALUMINUM_COMPOSITE_SHEET,
                BaseMats.ALUMINUM_COMPOSITE_SHEET, SlimefunItems.STEEL_THRUSTER, BaseMats.ALUMINUM_COMPOSITE_SHEET
        }).register(galactifun);

        new ForcefieldGenerator(ENVIRONMENTAL_FORCEFIELD_GENERATOR, new ItemStack[] {
                BaseMats.ALUMINUM_COMPOSITE_SHEET, BaseMats.ENDER_BLOCK, BaseMats.ALUMINUM_COMPOSITE_SHEET,
                BaseMats.ALUMINUM_COMPOSITE_SHEET, ION_DISPERSER_1, BaseMats.ALUMINUM_COMPOSITE_SHEET,
                BaseMats.ALUMINUM_COMPOSITE_SHEET, BaseMats.SPACE_GRADE_PLATE, BaseMats.ALUMINUM_COMPOSITE_SHEET
        }).register(galactifun);

        new FusionReactor(FUSION_REACTOR, new ItemStack[] {
                BaseMats.ULTRA_DUTY_SHEET, BaseMats.ULTRA_DUTY_SHEET, BaseMats.ADVANCED_PROCESSING_UNIT, BaseMats.ADVANCED_PROCESSING_UNIT, BaseMats.ULTRA_DUTY_SHEET, BaseMats.ULTRA_DUTY_SHEET,
                BaseMats.ULTRA_DUTY_SHEET, BaseMats.BLISTERING_VOLCANIC_INGOT, BaseMats.LASERITE, BaseMats.LASERITE, BaseMats.BLISTERING_VOLCANIC_INGOT, BaseMats.ULTRA_DUTY_SHEET,
                BaseMats.ULTRA_DUTY_SHEET, BaseMats.LASERITE, BaseMats.FUSION_PELLET, BaseMats.FUSION_PELLET, BaseMats.LASERITE, BaseMats.ULTRA_DUTY_SHEET,
                BaseMats.ULTRA_DUTY_SHEET, BaseMats.LASERITE, BaseMats.FUSION_PELLET, BaseMats.FUSION_PELLET, BaseMats.LASERITE, BaseMats.ULTRA_DUTY_SHEET,
                BaseMats.ULTRA_DUTY_SHEET, BaseMats.BLISTERING_VOLCANIC_INGOT, BaseMats.LASERITE, BaseMats.LASERITE, BaseMats.BLISTERING_VOLCANIC_INGOT, BaseMats.ULTRA_DUTY_SHEET,
                BaseMats.ULTRA_DUTY_SHEET, BaseMats.ULTRA_DUTY_SHEET, BaseMats.ULTRA_DUTY_SHEET, BaseMats.ULTRA_DUTY_SHEET, BaseMats.ULTRA_DUTY_SHEET, BaseMats.ULTRA_DUTY_SHEET,
        }).register(galactifun);

        new AtmosphericHarvester(ATMOSPHERIC_HARVESTER, new ItemStack[] {
                BaseMats.SPACE_GRADE_PLATE, BaseMats.FAN_BLADE, BaseMats.SPACE_GRADE_PLATE,
                BaseMats.SPACE_GRADE_PLATE, null, BaseMats.SPACE_GRADE_PLATE,
                BaseMats.SPACE_GRADE_PLATE, BaseMats.SPACE_GRADE_PLATE, BaseMats.SPACE_GRADE_PLATE
        }).register(galactifun);

        new IonRocket(ION_ROCKET, new ItemStack[] {
                null, null, BaseMats.NOSE_CONE, BaseMats.NOSE_CONE, null, null,
                null, null, BaseMats.ULTRA_DUTY_SHEET, BaseMats.ULTRA_DUTY_SHEET, null, null,
                null, BaseMats.HEAVY_DUTY_SHEET, BaseMats.ADVANCED_PROCESSING_UNIT, BaseMats.ADVANCED_PROCESSING_UNIT, BaseMats.ULTRA_DUTY_SHEET, null,
                BaseMats.ULTRA_DUTY_SHEET, FUSION_REACTOR, BaseMats.LIFE_SUPPORT_MODULE, BaseMats.LIFE_SUPPORT_MODULE, SlimefunItems.ENERGIZED_CAPACITOR, BaseMats.ULTRA_DUTY_SHEET,
                BaseMats.ULTRA_DUTY_SHEET, BaseMats.FUEL_TANK_2, BaseMats.FUEL_TANK, BaseMats.FUEL_TANK, BaseMats.FUEL_TANK_2, BaseMats.ULTRA_DUTY_SHEET,
                BaseMats.ULTRA_DUTY_SHEET, null, BaseMats.ION_ENGINE, BaseMats.ION_ENGINE, null, BaseMats.ULTRA_DUTY_SHEET
        }, 500, 18).register(galactifun);

        MachineBlock chemicalReactor = new MachineBlock(CoreItemGroup.MACHINES, CHEMICAL_REACTOR, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                BaseMats.SPACE_GRADE_PLATE, null, BaseMats.SPACE_GRADE_PLATE,
                BaseMats.ULTRA_DUTY_SHEET, BaseMats.BLISTERING_VOLCANIC_INGOT, BaseMats.ULTRA_DUTY_SHEET,
                BaseMats.ADVANCED_PROCESSING_UNIT, BaseMats.ULTRA_DUTY_SHEET, BaseMats.ADVANCED_PROCESSING_UNIT
        });

        chemicalReactor.addRecipe(new ItemStack(Material.WATER_BUCKET), Gas.WATER.item(), new ItemStack(Material.BUCKET));
        chemicalReactor.addRecipe(Gas.WATER.item(), Gas.OXYGEN.item(), Gas.HYDROGEN.item().asQuantity(2));

        chemicalReactor.addRecipe(Gas.CARBON_DIOXIDE.item(), SlimefunItems.CARBON, Gas.OXYGEN.item().asQuantity(2));
        chemicalReactor.addRecipe(Gas.METHANE.item(), SlimefunItems.CARBON, Gas.HYDROGEN.item().asQuantity(4));
        chemicalReactor.addRecipe(Gas.HYDROCARBONS.item(), Gas.METHANE.item().asQuantity(6));
        chemicalReactor.addRecipe(SlimefunItems.OIL_BUCKET, Gas.HYDROCARBONS.item(), new ItemStack(Material.BUCKET));

        chemicalReactor.addRecipe(Gas.AMMONIA.item(), Gas.NITROGEN.item(), Gas.HYDROGEN.item().asQuantity(3));

        chemicalReactor.energyCapacity(512);
        chemicalReactor.energyPerTick(128);
        chemicalReactor.ticksPerOutput(20);
        chemicalReactor.register(galactifun);

        new Electrolyzer(ELECTROLYZER, new ItemStack[] {
                BaseMats.SPACE_GRADE_PLATE, null, BaseMats.SPACE_GRADE_PLATE,
                SlimefunItems.SILVER_INGOT, BaseMats.BLISTERING_VOLCANIC_INGOT, SlimefunItems.SILVER_INGOT,
                BaseMats.ADVANCED_PROCESSING_UNIT, BaseMats.ULTRA_DUTY_SHEET, BaseMats.ADVANCED_PROCESSING_UNIT
        }).register(galactifun);

        // relics
        new Relic(BROKEN_SOLAR_PANEL_RELIC, new Relic.RelicSettings()
                .addRequired(SlimefunItems.SILICON, 2, 5)
                .addRequired(SlimefunItems.SOLAR_PANEL, 1, 3)
                .addOptional(SlimefunItems.SOLAR_GENERATOR, 0.20f)
                .addOptional(SlimefunItems.SOLAR_GENERATOR_2, 0.10f)
                .addOptional(SlimefunItems.SOLAR_GENERATOR_3, 0.02f),
                Galactifun.worldManager().alienWorlds().stream()
                        .filter(a -> a.getSetting("generate-fallen-satellites", Boolean.class, true))
                        .toArray(AlienWorld[]::new)).register(galactifun);

        new Relic(FALLEN_SATELLITE_RELIC, new Relic.RelicSettings()
                .addRequired(BaseMats.HEAVY_DUTY_SHEET, 3, 4)
                .addRequired(BaseMats.SPACE_GRADE_PLATE, 1, 3)
                .addRequired(SlimefunItems.BASIC_CIRCUIT_BOARD, 0, 2)
                .addOptional(SlimefunItems.ADVANCED_CIRCUIT_BOARD, 0.15f)
                .addOptional(BaseMats.ADVANCED_PROCESSING_UNIT, 0.10f),
                Galactifun.worldManager().alienWorlds().stream()
                        .filter(a -> a.getSetting("generate-fallen-satellites", Boolean.class, true))
                        .toArray(AlienWorld[]::new)).register(galactifun);

        new TechnologicalSalvager(TECHNOLOGICAL_SALVAGER, new ItemStack[] {
                BaseMats.ULTRA_DUTY_SHEET, null, BaseMats.ULTRA_DUTY_SHEET,
                BaseMats.ADVANCED_PROCESSING_UNIT, null, BaseMats.ADVANCED_PROCESSING_UNIT,
                BaseMats.ULTRA_DUTY_SHEET, new ItemStack(Material.STICKY_PISTON), BaseMats.ULTRA_DUTY_SHEET
        }).register(galactifun);
    }

}
