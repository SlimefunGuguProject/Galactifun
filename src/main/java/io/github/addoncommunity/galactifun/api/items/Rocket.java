package io.github.addoncommunity.galactifun.api.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import io.github.addoncommunity.galactifun.Galactifun;
import io.github.addoncommunity.galactifun.api.worlds.PlanetaryWorld;
import io.github.addoncommunity.galactifun.base.BaseItems;
import io.github.addoncommunity.galactifun.base.items.knowledge.KnowledgeLevel;
import io.github.addoncommunity.galactifun.core.WorldSelector;
import io.github.addoncommunity.galactifun.core.managers.WorldManager;
import io.github.addoncommunity.galactifun.util.BSUtils;
import io.github.addoncommunity.galactifun.util.Util;
import io.github.mooy1.infinitylib.common.PersistentType;
import io.github.mooy1.infinitylib.common.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.RandomizedSet;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemUtils;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public abstract class Rocket extends SlimefunItem implements RecipeDisplayItem {

    public static final NamespacedKey CARGO_KEY = Galactifun.createKey("cargo");

    // todo Move static to some sort of RocketManager
    private static final List<String> LAUNCH_MESSAGES = Galactifun.instance().getConfig().getStringList("rockets.launch-msgs");
    private static final double DISTANCE_PER_FUEL = 2_000_000 / Util.KM_PER_LY;

    @Getter
    private final int fuelCapacity;
    @Getter
    private final int storageCapacity;
    @Getter
    private final Map<String, Double> allowedFuels = new HashMap<>();

    public Rocket(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, int fuelCapacity, int storageCapacity) {
        super(category, item, recipeType, recipe);

        this.fuelCapacity = fuelCapacity;
        this.storageCapacity = storageCapacity;
        for (Map.Entry<ItemStack, Double> entry : getAllowedFuels().entrySet()) {
            allowedFuels.put(StackUtils.getIdOrType(entry.getKey()), entry.getValue());
        }

        addItemHandler((BlockUseHandler) e -> e.getClickedBlock().ifPresent(block -> {
            e.cancel();
            openGUI(e.getPlayer(), block);
        }));

        addItemHandler(new BlockPlaceHandler(false) {
            @Override
            public void onPlayerPlace(@Nonnull BlockPlaceEvent e) {
                Block b = e.getBlock();
                BlockData data = b.getBlockData();
                if (data instanceof Rotatable) {
                    ((Rotatable) data).setRotation(BlockFace.NORTH);
                }
                b.setBlockData(data, true);
            }
        });

        addItemHandler(new BlockBreakHandler(false, false) {
            @Override
            @ParametersAreNonnullByDefault
            public void onPlayerBreak(BlockBreakEvent e, ItemStack itemStack, List<ItemStack> list) {
                if (Boolean.parseBoolean(BlockStorage.getLocationInfo(e.getBlock().getLocation(), "isLaunching"))) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(ChatColor.RED + "火箭正在发射!");
                }
            }
        });
    }

    private void openGUI(@Nonnull Player p, @Nonnull Block b) {
        if (!BlockStorage.check(b, this.getId())) return;

        if (BSUtils.getStoredBoolean(b.getLocation(), "isLaunching")) {
            p.sendMessage(ChatColor.RED + "火箭正在发射!");
            return;
        }

        WorldManager worldManager = Galactifun.worldManager();
        PlanetaryWorld currentWorld = worldManager.getWorld(p.getWorld());
        if (currentWorld == null) {
            p.sendMessage(ChatColor.RED + "你不能从这个世界前往太空!");
            return;
        }

        int fuel = BSUtils.getStoredInt(b.getLocation(), "fuel");
        if (fuel == 0) {
            p.sendMessage(ChatColor.RED + "火箭没有燃料!");
            return;
        }

        String fuelType = BlockStorage.getLocationInfo(b.getLocation(), "fuelType");
        Double eff = allowedFuels.get(fuelType);
        if (eff == null || eff <= 0 || !Double.isFinite(eff)) {
            p.sendMessage(ChatColor.RED + "火箭燃料类型无效，请联系管理员");
            return;
        }

        // ly
        double maxDistance = fuel * DISTANCE_PER_FUEL * eff;

        new WorldSelector((player, obj, lore) -> {
            if (obj instanceof PlanetaryWorld) {
                double distance = obj.distanceTo(currentWorld);
                if (distance > maxDistance) return false;

                lore.add(Component.empty());
                lore.add(Component.text()
                        .color(NamedTextColor.YELLOW)
                        .append(Component.text("燃料: "))
                        .append(Component.text((long) Math.ceil(distance / (DISTANCE_PER_FUEL * eff))))
                        .build());
            }
            return true;
        }, (player, destination) -> {
            player.closeInventory();
            beginLaunch(player, b, destination);
        }).open(p);
    }

    private void beginLaunch(Player p, Block rocket, PlanetaryWorld destination) {
        if (BSUtils.getStoredBoolean(rocket.getLocation(), "isLaunching")) {
            p.sendMessage(ChatColor.RED + "火箭正在发射!");
            return;
        }
        if (!BlockStorage.check(rocket, getId()) || !(rocket.getState() instanceof Skull)) return;
        // The selector may have been open while the rocket/world/fuel changed.
        PlanetaryWorld current = Galactifun.worldManager().getWorld(p.getWorld());
        String fuelType = BlockStorage.getLocationInfo(rocket.getLocation(), "fuelType");
        Double efficiency = allowedFuels.get(fuelType);
        if (current == null || p.getWorld() != rocket.getWorld() || efficiency == null || efficiency <= 0) return;
        double needed = Math.ceil(destination.distanceTo(current) / (DISTANCE_PER_FUEL * efficiency));
        if (!Double.isFinite(needed) || needed < 0 || needed > BSUtils.getStoredInt(rocket.getLocation(), "fuel")) {
            p.sendMessage(ChatColor.RED + "火箭燃料不足或已改变");
            return;
        }
        int usedFuel = (int) needed;
        RocketLaunchSession session = new RocketLaunchSession(this, p, rocket, destination, usedFuel);
        if (!session.start()) {
            p.sendMessage(ChatColor.RED + "你已有一个发射任务，请先完成或取消");
            return;
        }
        p.sendMessage(ChatColor.YELLOW + "请输入你要降落的坐标 <x> <z> (例如: -123 456) 或输入其他内容取消 (60秒内):");
    }

    void acceptInput(RocketLaunchSession session, String input) {
        try {
            if (!Util.COORD_PATTERN.matcher(input).matches()) {
                session.cancel("已取消发射");
                return;
            }
            String[] coords = Util.SPACE_PATTERN.split(input);
            int x = Integer.parseInt(coords[0]);
            int z = Integer.parseInt(coords[1]);
            World world = session.destination.world();
            if (world == null || !world.getWorldBorder().isInside(new Location(world, x + 0.5, 0, z + 0.5))) {
                session.cancel("目标正处于世界边界之外");
                return;
            }
            Block target = Util.getHighestBlockAt(world, x, z,
                    l -> (l.isBuildable() || l.isLiquid()) && !BlockStorage.check(l, BaseItems.LANDING_HATCH.getItemId()));
            Block down = target.getRelative(BlockFace.DOWN);
            Block chest = down.getType() == Material.CHEST ? down : target;
            validateLanding(session, target);
            validateLanding(session, chest);
            validateSource(session);
            if (chest.getType() != Material.CHEST) chest.setType(Material.CHEST);
            launch(session, chest);
        } catch (NumberFormatException e) {
            session.cancel("坐标超出范围，已取消发射");
        } catch (Exception e) {
            session.cancel("无法准备发射: " + e.getMessage());
            Galactifun.instance().getLogger().log(Level.WARNING, "Rocket preflight failed at " + session.source.getLocation(), e);
        }
    }

    private void validateLanding(RocketLaunchSession session, Block target) {
        if (!target.getWorld().getWorldBorder().isInside(target.getLocation())) {
            throw new IllegalStateException("目标正处于世界边界之外");
        }
        if (!Slimefun.getProtectionManager().hasPermission(session.player, target, Interaction.PLACE_BLOCK)) {
            throw new IllegalStateException("你没有权限降落在目标地点!");
        }
        if (BlockStorage.check(target) != null && !BlockStorage.check(target, BaseItems.LANDING_HATCH.getItemId())) {
            throw new IllegalStateException("目标地点有粘液方块");
        }
    }

    private Skull validateSource(RocketLaunchSession session) {
        if (!session.player.isOnline() || !session.current()
                || !session.id.equals(BlockStorage.getLocationInfo(session.source.getLocation(), "launchSession"))
                || !BSUtils.getStoredBoolean(session.source.getLocation(), "isLaunching")
                || !BlockStorage.check(session.source, getId()) || !(session.source.getState() instanceof Skull skull)) {
            throw new IllegalStateException("源火箭已改变");
        }
        String fuelType = BlockStorage.getLocationInfo(session.source.getLocation(), "fuelType");
        if (!session.fuelType.equals(fuelType) || !allowedFuels.containsKey(fuelType) || session.usedFuel < 0
                || BSUtils.getStoredInt(session.source.getLocation(), "fuel") != session.fuel
                || session.fuel < session.usedFuel) {
            throw new IllegalStateException("火箭燃料不足或已改变");
        }
        return skull;
    }

    private void launch(RocketLaunchSession session, Block destBlock) {
        session.stage = RocketLaunchSession.Stage.COUNTDOWN;
        BlockStorage.addBlockInfo(session.source, "launchState", "countdown");
        Player p = session.player;
        Block rocket = session.source;
        World playerWorld = rocket.getWorld();
        session.tasks.add(new BukkitRunnable() {
            private final Block pad = rocket.getRelative(BlockFace.DOWN);
            private int times;

            @Override
            public void run() {
                if (times++ >= 20) {
                    cancel();
                    return;
                }
                try {
                    for (BlockFace face : Util.SURROUNDING_FACES) {
                        playerWorld.spawnParticle(getLaunchParticles(), pad.getRelative(face).getLocation(), 100, 0.5, 0.5, 0.5);
                    }
                } catch (RuntimeException e) {
                    session.cancel("发射动画失败，已取消发射");
                    Galactifun.instance().getLogger().log(Level.WARNING, "Rocket countdown failed", e);
                }
            }
        }.runTaskTimer(Galactifun.instance(), 0, 10));

        RandomizedSet<String> launchMessages = new RandomizedSet<>(LAUNCH_MESSAGES);
        for (int delay : new int[] { 40, 80, 120, 160 }) {
            String msg = launchMessages.isEmpty() ? "准备起飞" : launchMessages.getRandom();
            launchMessages.remove(msg);
            session.tasks.add(Bukkit.getScheduler().runTaskLater(Galactifun.instance(),
                    () -> p.sendMessage(ChatColor.GOLD + msg + "..."), delay));
        }
        session.tasks.add(Bukkit.getScheduler().runTaskLater(Galactifun.instance(), () -> transfer(session, destBlock), 200));
    }

    private void transfer(RocketLaunchSession session, Block destBlock) {
        RocketLaunchRecovery recovery = null;
        ItemStack head;
        try {
            if (session.stage != RocketLaunchSession.Stage.COUNTDOWN) return;
            Skull source = validateSource(session);
            validateLanding(session, destBlock);
            if (!(destBlock.getState() instanceof Chest chest)) throw new IllegalStateException("目标箱子已消失");
            // getBlockInventory() on the real state, not a PaperLib snapshot inventory.
            Inventory target = chest.getBlockInventory();
            List<ItemStack> delivery = new ArrayList<>();
            int remaining = BSUtils.getStoredInt(session.source.getLocation(), "fuel") - session.usedFuel;
            String fuelType = BlockStorage.getLocationInfo(session.source.getLocation(), "fuelType");
            ItemStack fuel = StackUtils.itemByIdOrType(fuelType);
            while (remaining > 0) {
                int amount = Math.min(remaining, fuel.getMaxStackSize());
                delivery.add(fuel.asQuantity(amount));
                remaining -= amount;
            }
            delivery.add(getItem().clone());
            source.getPersistentDataContainer().getOrDefault(CARGO_KEY, PersistentType.ITEM_STACK_LIST, new ArrayList<>())
                    .forEach(item -> delivery.add(item.clone()));
            Inventory planned = Bukkit.createInventory(null, target.getSize());
            ItemStack[] before = target.getStorageContents();
            planned.setStorageContents(before);
            for (ItemStack item : delivery) {
                if (!planned.addItem(item.clone()).isEmpty()) throw new IllegalStateException("目标箱子空间不足");
            }
            ItemStack[] after = planned.getStorageContents();
            head = new ItemStack(source.getType());
            head.editMeta(meta -> ((SkullMeta) meta).setPlayerProfile(source.getPlayerProfile()));
            // Persist all recovery material before the first source/destination mutation.
            recovery = new RocketLaunchRecovery(session, source, destBlock, delivery, before, after);
            BlockStorage.addBlockInfo(session.source, "launchRecovery", session.id);
            BlockStorage.addBlockInfo(session.source, "launchState", "transferring");
            session.stage = RocketLaunchSession.Stage.TRANSFERRING;
            // Remove the sole usable source before delivering anything. An exception after
            // this point requires inspection of the journal; it must never unlock/retry.
            session.source.setType(Material.AIR, false);
            BlockStorage.clearBlockInfo(session.source);
            recovery.save("source-removed");
            target.setStorageContents(after);
            recovery.save("delivered");
            session.stage = RocketLaunchSession.Stage.COMPLETE;
            session.close();
        } catch (Exception e) {
            if (session.stage == RocketLaunchSession.Stage.TRANSFERRING) {
                session.stage = RocketLaunchSession.Stage.RECOVERY;
                session.close();
                try {
                    if (session.id.equals(BlockStorage.getLocationInfo(session.source.getLocation(), "launchSession"))) {
                        BlockStorage.addBlockInfo(session.source, "launchState", "recovery");
                    }
                } catch (Exception markerError) { e.addSuppressed(markerError); }
                if (recovery != null) {
                    try { recovery.save("recovery-required"); } catch (Exception saveError) { e.addSuppressed(saveError); }
                }
                session.player.sendMessage(ChatColor.RED + "发射转移中断，请联系管理员。恢复编号: " + session.id);
                Galactifun.instance().getLogger().log(Level.SEVERE, "Rocket transfer requires manual recovery "
                        + session.id + " from " + session.source.getLocation() + " to " + destBlock.getLocation()
                        + "; journal: " + (recovery == null ? "unavailable" : recovery.path()) + ". Do not replay automatically.", e);
            } else {
                session.cancel("已取消发射: " + e.getMessage());
                Galactifun.instance().getLogger().log(Level.WARNING, "Rocket launch aborted before transfer at " + session.source.getLocation(), e);
            }
            return;
        }
        // Items are committed before optional animation/teleport. Failure here cannot
        // bring back the source rocket or lose its cargo; the target chest has them.
        try {
            session.player.sendMessage(ChatColor.YELLOW + "起飞...");
            teleportAndAnimate(session, destBlock, head);
        } catch (Exception e) {
            reportTeleportFailure(session.player, destBlock, e);
        }
    }

    private void teleportAndAnimate(RocketLaunchSession session, Block destBlock, ItemStack head) {
        Block rocket = session.source;
        World playerWorld = rocket.getWorld();
        boolean showLaunchAnimation = false;
        for (Entity entity : playerWorld.getEntities()) {
            if ((entity instanceof LivingEntity && !(entity instanceof ArmorStand)) || entity instanceof Item) {
                double distance = entity.getLocation().distanceSquared(rocket.getLocation());
                if (distance <= 25) {
                    if (entity instanceof Player) entity.setMetadata("CanTpAlienWorld", new FixedMetadataValue(Galactifun.instance(), true));
                    PaperLib.teleportAsync(entity, destBlock.getLocation().add(0, 1, 0)).whenComplete((success, error) -> {
                        if (!session.plugin.isEnabled()) return;
                        Bukkit.getScheduler().runTask(session.plugin, () -> {
                            if (entity instanceof Player passenger) passenger.removeMetadata("CanTpAlienWorld", session.plugin);
                            if (error != null || !Boolean.TRUE.equals(success)) {
                                reportTeleportFailure(session.player, destBlock, error);
                            } else if (entity.getUniqueId().equals(session.player.getUniqueId())
                                    && KnowledgeLevel.get(session.player, session.destination) == KnowledgeLevel.NONE) {
                                KnowledgeLevel.BASIC.set(session.player, session.destination);
                            }
                        });
                    });
                } else if (distance <= 64 * 64 && entity instanceof Player) {
                    showLaunchAnimation = true;
                }
            }
        }
        if (showLaunchAnimation) {
            Location rocketLocation = rocket.getLocation().add(0.5, -1, 0.5);
            ArmorStand armorStand = playerWorld.spawn(rocketLocation, ArmorStand.class);
            armorStand.getEquipment().setHelmet(head);
            armorStand.setInvisible(true);
            armorStand.setInvulnerable(true);
            armorStand.setMarker(false);
            armorStand.setBasePlate(false);
            new BukkitRunnable() {
                private int i;
                @Override
                public void run() {
                    i++;
                    armorStand.setVelocity(new Vector(0, 0.8 + i / 10D, 0));
                    playerWorld.spawnParticle(getLaunchParticles(), armorStand.getLocation(), 10);
                    if (i > 40) { armorStand.remove(); cancel(); }
                }
            }.runTaskTimer(Galactifun.instance(), 0, 8);
        }
    }

    private static void reportTeleportFailure(Player pilot, Block target, Throwable error) {
        String location = target.getWorld().getName() + " " + target.getX() + " " + target.getY() + " " + target.getZ();
        pilot.sendMessage(ChatColor.RED + "传送未完成，火箭、剩余燃料和货物保存在目标箱子: " + location);
        Galactifun.instance().getLogger().log(Level.WARNING, "Rocket teleport failed; items remain in target chest at " + location, error);
    }

    protected abstract Map<ItemStack, Double> getAllowedFuels();

    @Nonnull
    protected Particle getLaunchParticles() {
        return Particle.ASH;
    }

    @Nonnull
    @Override
    public List<ItemStack> getDisplayRecipes() {
        List<ItemStack> ret = new ArrayList<>();

        for (Map.Entry<ItemStack, Double> entry : getAllowedFuels().entrySet()) {
            ret.add(new CustomItemStack(
                    entry.getKey(),
                    ItemUtils.getItemName(entry.getKey()),
                    "&7效率: " + entry.getValue() + 'x'
            ));
        }

        return ret;
    }
}
