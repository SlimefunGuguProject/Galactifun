package io.github.addoncommunity.galactifun;

import java.io.File;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lombok.Getter;

import net.guizhanss.guizhanlibplugin.updater.GuizhanUpdater;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;

import io.github.addoncommunity.galactifun.api.worlds.AlienWorld;
import io.github.addoncommunity.galactifun.api.worlds.PlanetaryWorld;
import io.github.addoncommunity.galactifun.base.BaseAlien;
import io.github.addoncommunity.galactifun.base.BaseItems;
import io.github.addoncommunity.galactifun.base.BaseMats;
import io.github.addoncommunity.galactifun.base.BaseUniverse;
import io.github.addoncommunity.galactifun.core.CoreItemGroup;
import io.github.addoncommunity.galactifun.core.commands.AlienRemoveCommand;
import io.github.addoncommunity.galactifun.core.commands.AlienSpawnCommand;
import io.github.addoncommunity.galactifun.core.commands.EffectsCommand;
import io.github.addoncommunity.galactifun.core.commands.GalactiportCommand;
import io.github.addoncommunity.galactifun.core.commands.SealedCommand;
import io.github.addoncommunity.galactifun.core.commands.StructureCommand;
import io.github.addoncommunity.galactifun.core.managers.AlienManager;
import io.github.addoncommunity.galactifun.core.managers.ProtectionManager;
import io.github.addoncommunity.galactifun.core.managers.WorldManager;
import io.github.mooy1.infinitylib.common.Scheduler;
import io.github.mooy1.infinitylib.core.AbstractAddon;
import io.github.mooy1.infinitylib.metrics.bukkit.Metrics;
import io.github.thebusybiscuit.slimefun4.api.MinecraftVersion;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.updater.BlobBuildUpdater;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;


public final class Galactifun extends AbstractAddon {

    @Getter
    private static Galactifun instance;

    private boolean isTest = false;

    private AlienManager alienManager;
    private WorldManager worldManager;
    private ProtectionManager protectionManager;

    private boolean shouldDisable = false;

    public Galactifun() {
        super("SlimefunGuguProject", "Galactifun", "master", "auto-update");
    }

    public Galactifun(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file, "SlimefunGuguProject", "Galactifun", "master", "auto-update");
        isTest = true;
    }

    public static AlienManager alienManager() {
        return instance.alienManager;
    }

    public static WorldManager worldManager() {
        return instance.worldManager;
    }

    public static ProtectionManager protectionManager() {
        return instance.protectionManager;
    }

    @Override
    protected void enable() {
        instance = this;

        if (!isTest) {
            boolean shouldDisable = false;
            if (!getServer().getPluginManager().isPluginEnabled("GuizhanLibPlugin")) {
                getLogger().log(Level.SEVERE, "本插件需要 鬼斩前置库插件(GuizhanLibPlugin) 才能运行!");
                getLogger().log(Level.SEVERE, "从此处下载: https://50l.cc/gzlib");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            if (!PaperLib.isPaper()) {
                log(Level.SEVERE, "Galactifun 只支持Paper以及Paper的衍生服务端(例如Airplane和Purpur)");
                log(Level.SEVERE, "请使用Paper或者Paper的衍生服务端");
                shouldDisable = true;
            }
            if (Slimefun.getMinecraftVersion().isBefore(MinecraftVersion.MINECRAFT_1_17)) {
                log(Level.SEVERE, "Galactifun需要在 Minecraft 1.17 及以上版本中运行");
                log(Level.SEVERE, "请使用Minecraft 1.17 及以上版本");
                shouldDisable = true;
            }
            if (Bukkit.getPluginManager().isPluginEnabled("ClayTech")) {
                log(Level.SEVERE, "Galactifun 不能和 ClayTech 同时运行");
                log(Level.SEVERE, "请移除 ClayTech 或 Galactifun");
                shouldDisable = true;
            }

            if (Bukkit.getPluginManager().isPluginEnabled("ChatColor2")) {
                log(Level.SEVERE, "Galactifun will not work properly with ChatColor2");
                log(Level.SEVERE, "Please disable ChatColor2");
                shouldDisable = true;
            }

            if (shouldDisable) {
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            if (getConfig().getBoolean("auto-update", true) && getDescription().getVersion().startsWith("Build")) {
                GuizhanUpdater.start(this, getFile(), "SlimefunGuguProject", "Galactifun", "master");
            }
        }

        new Metrics(this, 11613);

        this.alienManager = new AlienManager(this);
        this.worldManager = new WorldManager(this);
        this.protectionManager = new ProtectionManager();

        BaseAlien.setup(this.alienManager);
        if (!isTest) {
            BaseUniverse.setup(this);
        }
        CoreItemGroup.setup(this);
        BaseMats.setup();
        BaseItems.setup(this);

        // log after startup
        Scheduler.run(() -> log(Level.INFO,
                "################# Galactifun " + getPluginVersion() + " #################",
                "",
                "Galactifun是开源的，如果有任何问题, 请在此汇报: ",
                getBugTrackerURL(),
                "加入Slimefun插件社区: discord.gg/SqD3gg5SAU",
                "",
                "###################################################"
        ));

        getAddonCommand()
                .addSub(new GalactiportCommand())
                .addSub(new AlienSpawnCommand())
                .addSub(new AlienRemoveCommand())
                .addSub(new StructureCommand(this))
                .addSub(new SealedCommand())
                .addSub(new EffectsCommand());
    }

    @Override
    protected void disable() {
        if (shouldDisable) return;

        this.alienManager.onDisable();

        // Do this last
        instance = null;
    }

    @Override
    public void load() {
        if (!isTest) {
            // Default to not logging world settings
            Bukkit.spigot().getConfig().set("world-settings.default.verbose", false);
        }
    }

    @Nullable
    @Override
    public ChunkGenerator getDefaultWorldGenerator(@Nonnull String worldName, @Nullable String id) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        PlanetaryWorld planetaryWorld = this.worldManager.getWorld(world);
        if (planetaryWorld instanceof AlienWorld) {
            return planetaryWorld.world().getGenerator();
        }

        return null;
    }

}
