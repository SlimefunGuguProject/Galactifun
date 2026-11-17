package rockettest;

import io.github.addoncommunity.galactifun.Galactifun;
import io.github.addoncommunity.galactifun.api.items.Rocket;
import io.github.addoncommunity.galactifun.api.worlds.PlanetaryWorld;
import io.github.addoncommunity.galactifun.base.BaseUniverse;
import io.github.addoncommunity.galactifun.base.items.LaunchPadCore;
import io.github.addoncommunity.galactifun.infinitylib.common.PersistentType;
import io.github.addoncommunity.galactifun.infinitylib.common.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.*;

import me.mrCookieSlime.Slimefun.api.BlockStorage;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.*;
import java.util.*;

@SuppressWarnings({"deprecation", "unchecked"})
public final class RocketTest extends JavaPlugin
        implements CommandExecutor, org.bukkit.event.Listener {
    private boolean deny;
    private int index;
    private final Map<String, Object> results = new LinkedHashMap<>();
    private final List<String> messages = new ArrayList<>();
    private final Map<Location, String> expectedSessions = new HashMap<>();
    private World world;
    private Rocket rocket;

    public void onEnable() {
        if (!Boolean.getBoolean("galactifun.rocket-launch-tests")
                || !Bukkit.getIp().equals("127.0.0.1")
                || Bukkit.getPort() != 25595) throw new IllegalStateException("ISOLATED LAB ONLY");
        getCommand("rockettest").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, this);
        world = Bukkit.getWorld("rocket-test");
        rocket = (Rocket) SlimefunItem.getById("ROCKET_TIER_ONE");
        later(
                30,
                () -> {
                    try {
                        Field f =
                                Slimefun.getProtectionManager()
                                        .getClass()
                                        .getDeclaredField("protectionModules");
                        f.setAccessible(true);
                        ((Set<ProtectionModule>) f.get(Slimefun.getProtectionManager()))
                                .add(
                                        new ProtectionModule() {
                                            public void load() {}

                                            public org.bukkit.plugin.Plugin getPlugin() {
                                                return RocketTest.this;
                                            }

                                            public boolean hasPermission(
                                                    org.bukkit.OfflinePlayer p,
                                                    Location l,
                                                    Interaction i) {
                                                return !deny;
                                            }
                                        });
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                });
    }

    @org.bukkit.event.EventHandler
    public void onTeleport(org.bukkit.event.entity.EntityTeleportEvent event) {
        if (event.getEntity().getScoreboardTags().contains("rockettest-block-teleport"))
            event.setCancelled(true);
    }

    private Player player() {
        UUID id = UUID.randomUUID();
        return (Player)
                Proxy.newProxyInstance(
                        Player.class.getClassLoader(),
                        new Class[] {Player.class},
                        (p, m, a) -> {
                            switch (m.getName()) {
                                case "getUniqueId":
                                    return id;
                                case "getName":
                                    return "RocketTest";
                                case "isOnline":
                                case "isValid":
                                    return true;
                                case "isOp":
                                case "hasPermission":
                                case "isPermissionSet":
                                    return false;
                                case "getWorld":
                                    return world;
                                case "getLocation":
                                    return new Location(world, 0, 70, 0);
                                case "getServer":
                                    return Bukkit.getServer();
                                case "getGameMode":
                                    return GameMode.SURVIVAL;
                                case "sendMessage":
                                    messages.add(Arrays.toString(a));
                                    return null;
                                case "hashCode":
                                    return id.hashCode();
                                case "equals":
                                    return p == a[0];
                                case "toString":
                                    return "RocketTest/" + id;
                                case "getMetadata":
                                    return new ArrayList<>();
                            }
                            Class<?> t = m.getReturnType();
                            if (t == boolean.class) return false;
                            if (t == int.class) return 0;
                            if (t == long.class) return 0L;
                            if (t == float.class) return 0f;
                            if (t == double.class) return 0d;
                            if (t == Set.class) return new HashSet<>();
                            if (t == List.class) return new ArrayList<>();
                            return null;
                        });
    }

    private String fuelId() {
        return StackUtils.getIdOrType(SlimefunItems.FUEL_BUCKET);
    }

    private Block source() {
        Block b = world.getBlockAt(20 + index++ * 3, 70, 20);
        b.getChunk().addPluginChunkTicket(this);
        b.setType(Material.PLAYER_HEAD);
        var ctl = Slimefun.getDatabaseManager().getBlockDataController();
        if (ctl.getBlockData(b.getLocation()) != null) ctl.removeBlock(b.getLocation());
        ctl.createBlock(b.getLocation(), rocket.getId());
        var data = ctl.getBlockData(b.getLocation());
        if (!data.isDataLoaded()) ctl.loadBlockData(data);
        data.setData("fuel", "12");
        data.setData("fuelType", fuelId());
        data.setData("isLaunching", "false");
        Skull skull = (Skull) b.getState();
        var profile =
                ((org.bukkit.inventory.meta.SkullMeta) rocket.getItem().getItemMeta())
                        .getPlayerProfile();
        if (profile != null) skull.setPlayerProfile(profile);
        skull.getPersistentDataContainer()
                .set(
                        new NamespacedKey(this, "sentinel"),
                        org.bukkit.persistence.PersistentDataType.LONG_ARRAY,
                        new long[] {13, 21});
        skull.getPersistentDataContainer()
                .set(
                        Rocket.CARGO_KEY,
                        PersistentType.ITEM_STACK_LIST,
                        List.of(new ItemStack(Material.DIAMOND, 7)));
        skull.update(true, false);
        return b;
    }

    private void select(Player p, Block b) throws Exception {
        selectTo(p, b, BaseUniverse.EARTH);
    }

    private PlanetaryWorld fuelDestination(int cost) {
        return new PlanetaryWorld(
                "rocket_test_fuel_" + index, "Fuel test",
                io.github.addoncommunity.galactifun.api.universe.types.PlanetaryType.TERRESTRIAL,
                io.github.addoncommunity.galactifun.api.universe.attributes.Orbit.lightYears(0, 0),
                BaseUniverse.SOLAR_SYSTEM, new ItemStack(Material.STONE),
                io.github.addoncommunity.galactifun.api.universe.attributes.DayCycle.EARTH_LIKE,
                io.github.addoncommunity.galactifun.api.universe.attributes.atmosphere.Atmosphere.EARTH_LIKE,
                io.github.addoncommunity.galactifun.api.universe.attributes.Gravity.EARTH_LIKE) {
            @Override protected World loadWorld() { return RocketTest.this.world; }
            @Override public World world() { return RocketTest.this.world; }
            @Override public double distanceTo(io.github.addoncommunity.galactifun.api.universe.UniversalObject other) {
                // Avoid a rounding-boundary fixture: ceil(cost - 0.25) must equal cost.
                return (cost - 0.25) * 2_000_000 / io.github.addoncommunity.galactifun.util.Util.KM_PER_LY;
            }
        };
    }

    private void selectTo(Player p, Block b, PlanetaryWorld destination) throws Exception {
        try {
            Method m =
                    Rocket.class.getDeclaredMethod(
                            "beginLaunch", Player.class, Block.class, PlanetaryWorld.class);
            m.setAccessible(true);
            m.invoke(rocket, p, b, destination);
        } catch (NoSuchMethodException missing) {
            Method m =
                    Arrays.stream(Rocket.class.getDeclaredMethods())
                            .filter(
                                    x ->
                                            x.getName().startsWith("lambda$openGUI$")
                                                    && x.getParameterCount() == 8)
                            .findFirst()
                            .orElseThrow();
            m.setAccessible(true);
            m.invoke(rocket, BaseUniverse.EARTH, 1D, b, p, fuelId(), 12, p, destination);
        }
        expectedSessions.put(
                b.getLocation(), BlockStorage.getLocationInfo(b.getLocation(), "launchSession"));
    }

    private void chat(Player p, String s) {
        Bukkit.getPluginManager().callEvent(new AsyncPlayerChatEvent(false, p, s, new HashSet<>()));
    }

    private boolean locked(Block b) {
        return Boolean.parseBoolean(BlockStorage.getLocationInfo(b.getLocation(), "isLaunching"));
    }

    private boolean intact(Block b) {
        return b.getType() == Material.PLAYER_HEAD
                && "12".equals(BlockStorage.getLocationInfo(b.getLocation(), "fuel"))
                && ((Skull) b.getState())
                                        .getPersistentDataContainer()
                                        .get(Rocket.CARGO_KEY, PersistentType.ITEM_STACK_LIST)
                                        .stream()
                                        .mapToInt(ItemStack::getAmount)
                                        .sum()
                        == 7;
    }

    private Block chest() {
        Block b = world.getBlockAt(100 + index * 3, 65, 100);
        b.getChunk().addPluginChunkTicket(this);
        b.setType(Material.CHEST);
        ((Chest) b.getState()).getBlockInventory().clear();
        ((Chest) b.getState()).getBlockInventory().addItem(new ItemStack(Material.EMERALD, 3));
        return b;
    }

    private int countId(Block b, String id) {
        if (!(b.getState() instanceof Chest c)) return 0;
        return Arrays.stream(c.getBlockInventory().getStorageContents())
                .filter(Objects::nonNull)
                .filter(i -> id.equals(StackUtils.getId(i)))
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

    private int count(Block b, Material m) {
        if (!(b.getState() instanceof Chest c)) return 0;
        return Arrays.stream(c.getBlockInventory().getStorageContents())
                .filter(Objects::nonNull)
                .filter(i -> i.getType() == m)
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

    private Block preflightFailure(Block source) {
        int[] calls = {0};
        return (Block)
                Proxy.newProxyInstance(
                        Block.class.getClassLoader(),
                        new Class[] {Block.class},
                        (p, m, a) -> {
                            if (m.getName().equals("getState") && calls[0]++ > 0)
                                throw new IllegalStateException(
                                        "injected source preflight failure");
                            return m.invoke(source, a);
                        });
    }

    private Block partialTarget(Block target) {
        Chest state = (Chest) target.getState();
        Inventory real = state.getBlockInventory();
        int[] additions = {0};
        Inventory inventory =
                (Inventory)
                        Proxy.newProxyInstance(
                                Inventory.class.getClassLoader(),
                                new Class[] {Inventory.class},
                                (p, m, a) -> {
                                    if (m.getName().equals("setStorageContents")) {
                                        ItemStack[] contents = (ItemStack[]) a[0];
                                        real.setItem(0, contents[0]);
                                        real.setItem(1, contents[1].asOne());
                                        throw new IllegalStateException(
                                                "injected partial target write");
                                    }
                                    if (m.getName().equals("addItem") && additions[0]++ > 0)
                                        throw new IllegalStateException(
                                                "injected legacy partial target write");
                                    return m.invoke(real, a);
                                });
        Chest proxy =
                (Chest)
                        Proxy.newProxyInstance(
                                Chest.class.getClassLoader(),
                                new Class[] {Chest.class},
                                (p, m, a) ->
                                        m.getName().equals("getBlockInventory")
                                                ? inventory
                                                : m.invoke(state, a));
        return (Block)
                Proxy.newProxyInstance(
                        Block.class.getClassLoader(),
                        new Class[] {Block.class},
                        (p, m, a) -> m.getName().equals("getState") ? proxy : m.invoke(target, a));
    }

    private void transferInto(Player p, Block b, Block target) throws Exception {
        try {
            Class<?> type = Class.forName(Rocket.class.getPackageName() + ".RocketLaunchSession");
            Field sessions = type.getDeclaredField("SESSIONS");
            sessions.setAccessible(true);
            Object session = ((Map<?, ?>) sessions.get(null)).get(p.getUniqueId());
            Method transfer = Rocket.class.getDeclaredMethod("transfer", type, Block.class);
            transfer.setAccessible(true);
            transfer.invoke(rocket, session, target);
        } catch (ClassNotFoundException missing) {
            Method launch =
                    Rocket.class.getDeclaredMethod(
                            "launch",
                            Player.class,
                            Block.class,
                            ItemStack.class,
                            PlanetaryWorld.class,
                            Block.class);
            launch.setAccessible(true);
            launch.invoke(
                    rocket,
                    p,
                    b,
                    SlimefunItems.FUEL_BUCKET.asQuantity(12),
                    BaseUniverse.EARTH,
                    target);
        }
    }

    private boolean restoreJournal() throws Exception {
        var folder = new java.io.File(Galactifun.instance().getDataFolder(), "rocket-recovery");
        var files = folder.listFiles((d, n) -> n.endsWith(".yml"));
        if (files == null) return false;
        Arrays.sort(
                files, java.util.Comparator.comparingLong(java.io.File::lastModified).reversed());
        for (var f : files) {
            var y = YamlConfiguration.loadConfiguration(f);
            if (!"recovery-required".equals(y.getString("phase"))) continue;
            ItemStack head = y.getItemStack("source.head-with-pdc");
            Block restored = source();
            restored.setBlockData(Bukkit.createBlockData(y.getString("source.block-data")));
            Skull skull = (Skull) restored.getState();
            if (((org.bukkit.inventory.meta.SkullMeta) head.getItemMeta()).getPlayerProfile()
                    != null)
                skull.setPlayerProfile(
                        ((org.bukkit.inventory.meta.SkullMeta) head.getItemMeta())
                                .getPlayerProfile());
            skull.getPersistentDataContainer()
                    .readFromBytes(
                            head.getItemMeta().getPersistentDataContainer().serializeToBytes(),
                            true);
            skull.update(true, false);
            BlockStorage.addBlockInfo(restored, "fuel", y.getString("source.fuel"));
            BlockStorage.addBlockInfo(restored, "fuelType", y.getString("source.fuel-type"));
            return intact(restored)
                    && BlockStorage.check(restored, y.getString("source.id"))
                    && Arrays.equals(
                            ((Skull) restored.getState())
                                    .getPersistentDataContainer()
                                    .get(
                                            new NamespacedKey(this, "sentinel"),
                                            org.bukkit.persistence.PersistentDataType.LONG_ARRAY),
                            new long[] {13, 21});
        }
        return false;
    }

    private Block failingSource(Block source, boolean removeFirst) {
        return (Block)
                Proxy.newProxyInstance(
                        Block.class.getClassLoader(),
                        new Class[] {Block.class},
                        (p, m, a) -> {
                            if (m.getName().equals("setType") && a[0] == Material.AIR) {
                                if (removeFirst) m.invoke(source, a);
                                throw new IllegalStateException(
                                        "injected source removal failure; removeFirst="
                                                + removeFirst);
                            }
                            return m.invoke(source, a);
                        });
    }

    private boolean journalValid(Block b, Block target) {
        java.io.File folder =
                new java.io.File(Galactifun.instance().getDataFolder(), "rocket-recovery");
        java.io.File[] files = folder.listFiles((d, n) -> n.endsWith(".yml"));
        if (files == null) return false;
        for (var f : files) {
            YamlConfiguration y = YamlConfiguration.loadConfiguration(f);
            if (b.getLocation().equals(y.getLocation("source.location"))
                    && Objects.equals(
                            y.getString("session"), expectedSessions.get(b.getLocation()))) {
                ItemStack head = y.getItemStack("source.head-with-pdc");
                var cargo =
                        head.getItemMeta()
                                .getPersistentDataContainer()
                                .get(Rocket.CARGO_KEY, PersistentType.ITEM_STACK_LIST);
                return y.getString("phase").equals("recovery-required")
                        && y.getString("source.fuel").equals("12")
                        && cargo.stream().mapToInt(ItemStack::getAmount).sum() == 7
                        && Arrays.equals(
                                head.getItemMeta()
                                        .getPersistentDataContainer()
                                        .get(
                                                new NamespacedKey(this, "sentinel"),
                                                org.bukkit.persistence.PersistentDataType
                                                        .LONG_ARRAY),
                                new long[] {13, 21})
                        && y.getList("target.before").size() == 27
                        && y.getList("target.after").size() == 27
                        && !y.getList("delivery").isEmpty();
            }
        }
        return false;
    }

    private void result(String n, boolean ok, String detail) {
        results.put(n, Map.of("passed", ok, "detail", detail));
        getLogger().info("ROCKET_ASSERT " + n + " " + ok + " " + detail);
        save();
    }

    private void save() {
        try {
            YamlConfiguration y = new YamlConfiguration();
            results.forEach((k, v) -> y.set(k, v));
            y.save(getDataFolder().toPath().resolve("results.yml").toFile());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void later(long tick, Runnable r) {
        Bukkit.getScheduler().runTaskLater(this, r, tick);
    }

    public boolean onCommand(CommandSender s, Command c, String l, String[] args) {
        try {
            String n = args[0];
            Player p = player();
            Block b = source();
            messages.clear();
            deny = false;
            switch (n) {
                case "deny":
                    deny = true;
                    select(p, b);
                    chat(p, "0 0");
                    later(
                            3,
                            () -> {
                                result(
                                        n,
                                        !locked(b) && intact(b),
                                        "locked=" + locked(b) + " intact=" + intact(b));
                                deny = false;
                            });
                    break;
                case "bounds":
                    world.getWorldBorder().setSize(40);
                    select(p, b);
                    chat(p, "100 100");
                    later(
                            3,
                            () -> {
                                result(n, !locked(b) && intact(b), "locked=" + locked(b));
                                world.getWorldBorder().setSize(59999968);
                            });
                    break;
                case "overflow":
                    select(p, b);
                    chat(p, "999999999999999999999999 0");
                    later(3, () -> result(n, !locked(b) && intact(b), "locked=" + locked(b)));
                    break;
                case "cancel":
                    select(p, b);
                    chat(p, "cancel");
                    later(3, () -> result(n, !locked(b) && intact(b), "locked=" + locked(b)));
                    break;
                case "quit":
                    select(p, b);
                    Bukkit.getPluginManager().callEvent(new PlayerQuitEvent(p, net.kyori.adventure.text.Component.empty(), PlayerQuitEvent.QuitReason.DISCONNECTED));
                    later(3, () -> result(n, !locked(b) && intact(b), "locked=" + locked(b)));
                    break;
                case "kick":
                    select(p, b);
                    Bukkit.getPluginManager().callEvent(new PlayerKickEvent(p, net.kyori.adventure.text.Component.text("test"), net.kyori.adventure.text.Component.empty(), PlayerKickEvent.Cause.PLUGIN));
                    later(3, () -> result(n, !locked(b) && intact(b), "locked=" + locked(b)));
                    break;
                case "timeout":
                    select(p, b);
                    later(1250, () -> result(n, !locked(b) && intact(b), "locked=" + locked(b)));
                    break;
                case "preflight":
                    select(p, preflightFailure(b));
                    chat(p, "0 0");
                    later(3, () -> result(n, !locked(b) && intact(b), "locked=" + locked(b)));
                    break;
                case "asynccancel":
                    {
                        select(p, b);
                        Bukkit.getScheduler()
                                .runTaskAsynchronously(
                                        this,
                                        () ->
                                                Bukkit.getPluginManager()
                                                        .callEvent(
                                                                new AsyncPlayerChatEvent(
                                                                        true,
                                                                        p,
                                                                        "cancel",
                                                                        new HashSet<>())));
                        later(8, () -> result(n, !locked(b) && intact(b), "locked=" + locked(b)));
                        break;
                    }
                case "restore":
                    result(
                            n,
                            restoreJournal(),
                            "restored serialized head PDC, fuel/type and SF block id into real"
                                + " Skull");
                    break;
                case "targetpartial":
                    {
                        Block target = chest();
                        select(p, b);
                        boolean modern =
                                Arrays.stream(Rocket.class.getDeclaredMethods())
                                        .anyMatch(m -> m.getName().equals("transfer"));
                        if (modern) chat(p, target.getX() + " " + target.getZ());
                        later(
                                3,
                                () -> {
                                    try {
                                        transferInto(p, b, partialTarget(target));
                                    } catch (Exception e) {
                                        throw new IllegalStateException(e);
                                    }
                                });
                        later(
                                230,
                                () ->
                                        result(
                                                n,
                                                b.getType() == Material.AIR
                                                        && journalValid(b, target)
                                                        && countId(target, fuelId()) > 0
                                                        && countId(target, fuelId()) < 12
                                                        && count(target, Material.DIAMOND) == 0,
                                                "source="
                                                        + b.getType()
                                                        + " partialFuel="
                                                        + countId(target, fuelId())
                                                        + " journal="
                                                        + journalValid(b, target)));
                        break;
                    }
                case "teleportfail":
                    {
                        Block target = chest();
                        var passenger =
                                world.spawn(
                                        b.getLocation().add(.5, 1, .5),
                                        org.bukkit.entity.Pig.class);
                        passenger.setAI(false);
                        passenger.setGravity(false);
                        passenger.setInvulnerable(true);
                        passenger.addScoreboardTag("rockettest-block-teleport");
                        select(p, b);
                        chat(p, target.getX() + " " + target.getZ());
                        later(
                                230,
                                () -> {
                                    result(
                                            n,
                                            b.getType() == Material.AIR
                                                    && count(target, Material.DIAMOND) == 7
                                                    && countId(target, fuelId()) == 12
                                                    && passenger
                                                                    .getLocation()
                                                                    .distanceSquared(
                                                                            b.getLocation())
                                                            < 25
                                                    && messages.stream()
                                                            .anyMatch(m -> m.contains("传送未完成")),
                                            "passenger distance="
                                                    + passenger
                                                            .getLocation()
                                                            .distanceSquared(b.getLocation())
                                                    + " failureReported="
                                                    + messages.stream()
                                                            .anyMatch(m -> m.contains("传送未完成")));
                                    passenger.remove();
                                });
                        break;
                    }
                case "fuelzero":
                case "fuelpartial": {
                    int cost = n.equals("fuelzero") ? 12 : 5;
                    Block target = chest();
                    selectTo(p, b, fuelDestination(cost));
                    chat(p, target.getX() + " " + target.getZ());
                    later(225, () -> result(n, b.getType() == Material.AIR
                            && countId(target, fuelId()) == 12 - cost
                            && countId(target, rocket.getId()) == 1
                            && count(target, Material.DIAMOND) == 7
                            && count(target, Material.EMERALD) == 3,
                            "used=" + cost + " remaining=" + countId(target, fuelId())));
                    break;
                }
                case "unknownfuel":
                    {
                        BlockStorage.addBlockInfo(b, "fuelType", "UNKNOWN_FUEL");
                        boolean rejected = false;
                        try {
                            Method m =
                                    Rocket.class.getDeclaredMethod(
                                            "openGUI", Player.class, Block.class);
                            m.setAccessible(true);
                            m.invoke(rocket, p, b);
                            rejected = !messages.isEmpty();
                        } catch (InvocationTargetException e) {
                            messages.add(e.getCause().toString());
                        }
                        result(n, rejected && !locked(b), messages.toString());
                        break;
                    }
                case "journalfail":
                    {
                        Block target = chest();
                        select(p, b);
                        chat(p, target.getX() + " " + target.getZ());
                        var folder =
                                Galactifun.instance()
                                        .getDataFolder()
                                        .toPath()
                                        .resolve("rocket-recovery");
                        var saved = folder.resolveSibling("rocket-recovery-saved");
                        later(
                                3,
                                () -> {
                                    try {
                                        if (java.nio.file.Files.exists(folder))
                                            java.nio.file.Files.move(folder, saved);
                                        java.nio.file.Files.writeString(
                                                folder, "intentional non-directory");
                                    } catch (Exception e) {
                                        throw new IllegalStateException(e);
                                    }
                                });
                        later(
                                225,
                                () -> {
                                    try {
                                        result(
                                                n,
                                                !locked(b)
                                                        && intact(b)
                                                        && count(target, Material.DIAMOND) == 0,
                                                "locked=" + locked(b));
                                    } finally {
                                        try {
                                            java.nio.file.Files.delete(folder);
                                            if (java.nio.file.Files.exists(saved))
                                                java.nio.file.Files.move(saved, folder);
                                        } catch (Exception e) {
                                            throw new IllegalStateException(e);
                                        }
                                    }
                                });
                        break;
                    }
                case "twoplayers":
                    {
                        Player second = player();
                        Block firstTarget = chest();
                        index++;
                        Block secondTarget = chest();
                        select(p, b);
                        select(second, b);
                        chat(p, firstTarget.getX() + " " + firstTarget.getZ());
                        chat(second, secondTarget.getX() + " " + secondTarget.getZ());
                        later(
                                225,
                                () ->
                                        result(
                                                n,
                                                b.getType() == Material.AIR
                                                        && countId(firstTarget, rocket.getId())
                                                                        + countId(
                                                                                secondTarget,
                                                                                rocket.getId())
                                                                == 1
                                                        && count(firstTarget, Material.DIAMOND)
                                                                        + count(
                                                                                secondTarget,
                                                                                Material.DIAMOND)
                                                                == 7,
                                                "rockets="
                                                        + (countId(firstTarget, rocket.getId())
                                                                + countId(
                                                                        secondTarget,
                                                                        rocket.getId()))));
                        break;
                    }
                case "cancelledkick":
                    {
                        select(p, b);
                        PlayerKickEvent event = new PlayerKickEvent(p, net.kyori.adventure.text.Component.text("test"), net.kyori.adventure.text.Component.empty(), PlayerKickEvent.Cause.PLUGIN);
                        event.setCancelled(true);
                        Bukkit.getPluginManager().callEvent(event);
                        result(n, locked(b), "cancelled kick retains active input");
                        chat(p, "cancel");
                        break;
                    }
                case "quitcountdown":
                    {
                        Block target = chest();
                        select(p, b);
                        chat(p, target.getX() + " " + target.getZ());
                        later(
                                3,
                                () ->
                                        Bukkit.getPluginManager()
                                                .callEvent(new PlayerQuitEvent(p, net.kyori.adventure.text.Component.empty(), PlayerQuitEvent.QuitReason.DISCONNECTED)));
                        later(
                                225,
                                () ->
                                        result(
                                                n,
                                                !locked(b)
                                                        && intact(b)
                                                        && count(target, Material.DIAMOND) == 0,
                                                "locked=" + locked(b)));
                        break;
                    }
                case "disable":
                    {
                        select(p, b);
                        Bukkit.getPluginManager().disablePlugin(Galactifun.instance());
                        result(n, !locked(b) && intact(b), "locked=" + locked(b));
                        break;
                    }
                case "command":
                    select(p, b);
                    Bukkit.getPluginManager()
                            .callEvent(new PlayerCommandPreprocessEvent(p, "/anything"));
                    later(3, () -> result(n, !locked(b) && intact(b), "locked=" + locked(b)));
                    break;
                case "normal":
                case "missing":
                case "full":
                case "sourcefail":
                case "removedfail":
                    {
                        Block target = chest();
                        if (n.equals("full"))
                            for (int i = 0; i < 27; i++)
                                ((Chest) target.getState())
                                        .getBlockInventory()
                                        .setItem(i, new ItemStack(Material.STONE, 64));
                        select(
                                p,
                                n.equals("sourcefail")
                                        ? failingSource(b, false)
                                        : n.equals("removedfail") ? failingSource(b, true) : b);
                        chat(p, target.getX() + " " + target.getZ());
                        later(
                                3,
                                () -> {
                                    result(
                                            n + "-countdown-lock",
                                            locked(b)
                                                    && !LaunchPadCore.canBreak(
                                                            p, b.getRelative(BlockFace.DOWN)),
                                            "locked=" + locked(b));
                                    if (n.equals("missing")) target.setType(Material.AIR);
                                });
                        later(
                                225,
                                () -> {
                                    if (n.equals("normal"))
                                        result(
                                                n,
                                                b.getType() == Material.AIR
                                                        && count(target, Material.DIAMOND) == 7
                                                        && count(target, Material.EMERALD) == 3
                                                        && countId(target, fuelId()) == 12
                                                        && countId(target, rocket.getId()) == 1,
                                                "source="
                                                        + b.getType()
                                                        + " diamond="
                                                        + count(target, Material.DIAMOND)
                                                        + " emerald="
                                                        + count(target, Material.EMERALD));
                                    else if (n.equals("sourcefail") || n.equals("removedfail"))
                                        result(
                                                n,
                                                journalValid(b, target)
                                                        && count(target, Material.DIAMOND) == 0
                                                        && (n.equals("sourcefail")
                                                                ? locked(b) && intact(b)
                                                                : b.getType() == Material.AIR),
                                                "journal="
                                                        + journalValid(b, target)
                                                        + " source="
                                                        + b.getType()
                                                        + " diamond="
                                                        + count(target, Material.DIAMOND));
                                    else
                                        result(
                                                n,
                                                !locked(b) && intact(b),
                                                "locked=" + locked(b) + " intact=" + intact(b));
                                });
                        break;
                    }
                case "stale":
                    {
                        select(p, b);
                        chat(p, "0 0");
                        Bukkit.getPluginManager().callEvent(new PlayerQuitEvent(p, net.kyori.adventure.text.Component.empty(), PlayerQuitEvent.QuitReason.DISCONNECTED));
                        Block newer = source();
                        select(p, newer);
                        later(
                                3,
                                () -> {
                                    result(
                                            n,
                                            !locked(b) && intact(b) && locked(newer),
                                            "old=" + locked(b) + " new=" + locked(newer));
                                    chat(p, "cancel");
                                });
                        break;
                    }
                case "duplicate":
                    {
                        select(p, b);
                        Block other = source();
                        select(p, other);
                        chat(p, "cancel");
                        later(
                                3,
                                () ->
                                        result(
                                                n,
                                                !locked(b) && !locked(other),
                                                "first=" + locked(b) + " second=" + locked(other)));
                        break;
                    }
                default:
                    throw new IllegalArgumentException(n);
            }
            s.sendMessage("scheduled " + n + " " + b.getLocation());
        } catch (Throwable e) {
            s.sendMessage(e.toString());
            e.printStackTrace();
        }
        return true;
    }
}
