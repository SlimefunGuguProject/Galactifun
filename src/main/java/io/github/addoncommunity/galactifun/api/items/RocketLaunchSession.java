package io.github.addoncommunity.galactifun.api.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.scheduler.BukkitTask;

import io.github.addoncommunity.galactifun.Galactifun;
import io.github.addoncommunity.galactifun.api.worlds.PlanetaryWorld;
import io.github.addoncommunity.galactifun.util.BSUtils;
import me.mrCookieSlime.Slimefun.api.BlockStorage;

/** Owns the input and countdown, but never unlocks a transfer that needs recovery. */
final class RocketLaunchSession implements Listener {

    private static final Map<UUID, RocketLaunchSession> SESSIONS = new ConcurrentHashMap<>();
    private static final long INPUT_TIMEOUT = 20 * 60;

    enum Stage { INPUT, COUNTDOWN, TRANSFERRING, RECOVERY, COMPLETE, CANCELLED }

    final Galactifun plugin = Galactifun.instance();
    final String id = UUID.randomUUID().toString();
    final Rocket rocket;
    final Player player;
    final Block source;
    final PlanetaryWorld destination;
    final int usedFuel;
    final String fuelType;
    final int fuel;
    final List<BukkitTask> tasks = new ArrayList<>();
    private final AtomicBoolean inputPending = new AtomicBoolean(true);
    Stage stage = Stage.INPUT;

    RocketLaunchSession(Rocket rocket, Player player, Block source, PlanetaryWorld destination, int usedFuel) {
        this.rocket = rocket;
        this.player = player;
        this.source = source;
        this.destination = destination;
        this.usedFuel = usedFuel;
        this.fuelType = BlockStorage.getLocationInfo(source.getLocation(), "fuelType");
        this.fuel = BSUtils.getStoredInt(source.getLocation(), "fuel");
    }

    boolean start() {
        if (SESSIONS.putIfAbsent(player.getUniqueId(), this) != null) return false;
        try {
            BlockStorage.addBlockInfo(source, "launchSession", id);
            BSUtils.addBlockInfo(source, "isLaunching", true);
            BlockStorage.addBlockInfo(source, "launchState", "input");
            Bukkit.getPluginManager().registerEvents(this, plugin);
            tasks.add(Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (stage == Stage.INPUT) cancel("输入超时，已取消发射");
            }, INPUT_TIMEOUT));
            return true;
        } catch (RuntimeException e) {
            cancel("无法开始发射，请重试");
            throw e;
        }
    }

    boolean current() {
        return SESSIONS.get(player.getUniqueId()) == this;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.getPlayer().getUniqueId().equals(player.getUniqueId()) && current()
                && inputPending.compareAndSet(true, false)) {
            event.setCancelled(true);
            String input = event.getMessage();
            // Capture this session, not just the UUID: quit/rejoin may start another launch.
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (current() && stage == Stage.INPUT) rocket.acceptInput(this, input);
            });
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (event.getPlayer().getUniqueId().equals(player.getUniqueId()) && current()
                && inputPending.compareAndSet(true, false)) {
            event.setCancelled(true);
            cancel("已取消发射");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (event.getPlayer().getUniqueId().equals(player.getUniqueId())) cancel(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKick(PlayerKickEvent event) {
        if (event.getPlayer().getUniqueId().equals(player.getUniqueId())) cancel(null);
    }

    @EventHandler
    public void onDisable(PluginDisableEvent event) {
        if (event.getPlugin() == plugin) cancel(null);
    }

    void cancel(String message) {
        if (stage != Stage.INPUT && stage != Stage.COUNTDOWN) return;
        stage = Stage.CANCELLED;
        close();
        // A delayed callback must not unlock a different rocket/session at this location.
        if (id.equals(BlockStorage.getLocationInfo(source.getLocation(), "launchSession"))) {
            BSUtils.addBlockInfo(source, "isLaunching", false);
            BlockStorage.addBlockInfo(source, "launchState", "cancelled");
        }
        if (message != null && player.isOnline()) player.sendMessage(ChatColor.RED + message);
    }

    void close() {
        inputPending.set(false);
        SESSIONS.remove(player.getUniqueId(), this);
        tasks.forEach(BukkitTask::cancel);
        HandlerList.unregisterAll(this);
    }
}
