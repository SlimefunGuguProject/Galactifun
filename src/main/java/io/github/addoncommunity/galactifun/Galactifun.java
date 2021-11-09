package io.github.addoncommunity.galactifun;

import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import io.github.addoncommunity.galactifun.api.worlds.AlienWorld;
import io.github.addoncommunity.galactifun.api.worlds.PlanetaryWorld;
import io.github.addoncommunity.galactifun.base.BaseAlien;
import io.github.addoncommunity.galactifun.base.BaseItems;
import io.github.addoncommunity.galactifun.base.BaseMats;
import io.github.addoncommunity.galactifun.base.BaseUniverse;
import io.github.addoncommunity.galactifun.core.CoreItemGroup;
import io.github.addoncommunity.galactifun.core.commands.AlienSpawnCommand;
import io.github.addoncommunity.galactifun.core.commands.ChunkverCommand;
import io.github.addoncommunity.galactifun.core.commands.EffectsCommand;
import io.github.addoncommunity.galactifun.core.commands.GalactiportCommand;
import io.github.addoncommunity.galactifun.core.commands.SealedCommand;
import io.github.addoncommunity.galactifun.core.commands.SphereCommand;
import io.github.addoncommunity.galactifun.core.commands.StructureCommand;
import io.github.addoncommunity.galactifun.core.managers.AlienManager;
import io.github.addoncommunity.galactifun.core.managers.ProtectionManager;
import io.github.addoncommunity.galactifun.core.managers.WorldManager;
import io.github.mooy1.infinitylib.common.Scheduler;
import io.github.mooy1.infinitylib.core.AbstractAddon;
import io.github.mooy1.infinitylib.metrics.bukkit.Metrics;
import io.github.thebusybiscuit.slimefun4.api.MinecraftVersion;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;


public final class Galactifun extends AbstractAddon {

    @Getter
    private static Galactifun instance;

    private AlienManager alienManager;
    private WorldManager worldManager;
    private ProtectionManager protectionManager;

    public Galactifun() {
        super("Slimefun-Addon-Community", "Galactifun", "master", "auto-update");
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

        boolean shouldDisable = false;
        if (!PaperLib.isPaper()) {
            log(Level.SEVERE, "Galactifun只支持Paper和他的分支(例如Airplane和urpur)");
            log(Level.SEVERE, "请使用Paper或者Paper的分支");
            shouldDisable = true;
        }
        if (Slimefun.getMinecraftVersion().isBefore(MinecraftVersion.MINECRAFT_1_17)) {
            log(Level.SEVERE, "Galactifun需要在Minecraft 1.17中运行");
            log(Level.SEVERE, "请使用Minecraft 1.17");
            shouldDisable = true;
        }
        if (Bukkit.getPluginManager().isPluginEnabled("ClayTech")) {
            log(Level.SEVERE, "Galactifun不能和ClayTech在一起运行");
            log(Level.SEVERE, "请关闭ClayTech");
            shouldDisable = true;
        }

        if (shouldDisable) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        new Metrics(this, 11613);

        this.alienManager = new AlienManager(this);
        this.worldManager = new WorldManager(this);
        this.protectionManager = new ProtectionManager();

        BaseAlien.setup(this.alienManager);
        BaseUniverse.setup(this);
        CoreItemGroup.setup(this);
        BaseMats.setup();
        BaseItems.setup(this);

        // log after startup
        Scheduler.run(() -> log(Level.INFO,
                "################# Galactifun " + getPluginVersion() + " #################",
                "",
                "Galactifun是开源的，您可以在(英文): ",
                getBugTrackerURL(),
                "加入Slimefun插件社区: discord.gg/SqD3gg5SAU",
                "",
                "###################################################"
        ));

        getAddonCommand()
                .addSub(new GalactiportCommand())
                .addSub(new AlienSpawnCommand())
                .addSub(new SphereCommand())
                .addSub(new StructureCommand(this))
                .addSub(new SealedCommand())
                .addSub(new EffectsCommand())
                .addSub(new ChunkverCommand());
    }

    @Override
    protected void disable() {
        this.alienManager.onDisable();

        // Do this last
        instance = null;
    }

    @Override
    public void load() {
        // Default to not logging world settings
        Bukkit.spigot().getConfig().set("world-settings.default.verbose", false);
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
