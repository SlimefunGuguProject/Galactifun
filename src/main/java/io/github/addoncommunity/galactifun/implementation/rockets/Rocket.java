package io.github.addoncommunity.galactifun.implementation.rockets;

import com.google.common.collect.BiMap;
import io.github.addoncommunity.galactifun.Galactifun;
import io.github.addoncommunity.galactifun.api.universe.world.CelestialWorld;
import io.github.addoncommunity.galactifun.implementation.lists.Categories;
import io.github.addoncommunity.galactifun.implementation.lists.Heads;
import io.github.addoncommunity.galactifun.util.Util;
import io.github.mooy1.infinitylib.ConfigUtils;
import io.github.mooy1.infinitylib.PluginUtils;
import io.github.mooy1.infinitylib.presets.LorePreset;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import lombok.Getter;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import me.mrCookieSlime.Slimefun.cscorelib2.chat.ChatColors;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rotatable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

@Getter
public enum Rocket {
    ONE(1, 10, 9, new ItemStack[9]),
    TWO(2, 100, 18, new ItemStack[9]),
    ;

    private final int tier;
    private final int fuelCapacity;
    private final int storageCapacity;
    @Nonnull
    private final ItemStack[] recipe;
    @Nonnull
    private final SlimefunItemStack item;

    Rocket(int tier, int fuelCapacity, int storageCapacity, @Nonnull ItemStack... recipe) {
        this.tier = tier;
        this.fuelCapacity = fuelCapacity;
        this.storageCapacity = storageCapacity;
        this.recipe = recipe;
        this.item = new SlimefunItemStack(
            "ROCKET_TIER_" + this.name(),
            Heads.ROCKET.getTexture(),
            "&4Rocket Tier " + tier,
            "",
            "&7Fuel Capacity: " + fuelCapacity,
            "&7Cargo Capacity: " + storageCapacity
        );
    }

    public static void setup(Galactifun addon) {
        for (Rocket rocket : Rocket.values()) {
            new RocketItem(Categories.MAIN_CATEGORY, rocket.getItem(), RecipeType.ENHANCED_CRAFTING_TABLE, rocket.getRecipe()).register(addon);
        }
    }

    @Nullable
    public static Rocket getById(String id) {
        if (id == null) return null;

        for (Rocket rocket : Rocket.values()) {
            if (rocket.getItem().getItemId().equals(id)) {
                return rocket;
            }
        }

        return null;
    }

    private static class RocketItem extends SlimefunItem {

        public RocketItem(Category category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
            super(category, item, recipeType, recipe);

            addItemHandler((BlockUseHandler) e -> e.getClickedBlock().ifPresent(block -> openGUI(e.getPlayer(), block)));

            addItemHandler(new BlockPlaceHandler(true) {
                @Override
                public void onPlayerPlace(BlockPlaceEvent e) {
                    Block b = e.getBlock();
                    BlockData data = b.getBlockData();
                    if (data instanceof Rotatable) {
                        ((Rotatable) data).setRotation(BlockFace.NORTH);
                    }
                    b.setBlockData(data, true);
                }
            });
        }

        private void openGUI(@Nonnull Player p, @Nonnull Block b) {
            if (!BlockStorage.check(b, this.getId())) return;

            String s = BlockStorage.getLocationInfo(b.getLocation(), "isLaunching");
            if (Boolean.parseBoolean(s)) {
                p.sendMessage(ChatColor.RED + "The rocket is already launching!");
                return;
            }

            CelestialWorld world = CelestialWorld.getByWorld(p.getWorld());
            if (world == null) return;

            s = BlockStorage.getLocationInfo(b.getLocation(), "fuel");
            if (s == null) return;
            int fuel = Integer.parseInt(s);
            if (fuel == 0) {
                p.sendMessage(ChatColor.RED + "The rocket has no fuel!");
                return;
            }

            s = BlockStorage.getLocationInfo(b.getLocation(), "fuelEff");
            if (s == null) return;
            double eff = Double.parseDouble(s);
            if (eff == 0) {
                throw new IllegalStateException("Fuel not zero but efficiency zero!");
            }

            double trueEff = eff / fuel;
            long maxDistance = Math.round(2_000_000 * (fuel * trueEff));

            List<CelestialWorld> reachable = getReachable(world, maxDistance);

            if (reachable.isEmpty()) {
                p.sendMessage(ChatColor.RED + "No known destinations within range!");
                return;
            }

            ChestMenu menu = new ChestMenu("Choose a destination");
            menu.setEmptySlotsClickable(false);

            int i = 0;
            for (CelestialWorld celestialWorld : reachable) {
                double distance = celestialWorld.getDistanceTo(world);
                ItemStack item = celestialWorld.getItem().clone();
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    List<String> lore = meta.getLore();
                    if (lore != null) {
                        lore.remove(lore.size() - 1);

                        if (distance > 0) {
                            lore.add(ChatColors.color("&7Distance: " + (distance < 1
                                ? LorePreset.format(distance * Util.KM_PER_LY) + " Kilometers"
                                : distance + " Light Years")
                            ));
                        } else {
                            lore.add(ChatColors.color("&7You are here!"));
                        }

                        meta.setLore(lore);
                        item = item.clone();
                        item.setItemMeta(meta);
                    }
                }

                menu.addItem(i++, item, (p1, slot, it, action) -> {
                    p1.closeInventory();
                    int usedFuel = (int) Math.ceil(((distance * Util.KM_PER_LY) / 2_000_000) / trueEff);
                    p1.sendMessage(ChatColor.GOLD + "You are going to " + celestialWorld.getName() + " and will use " +
                        usedFuel + " fuel. Are you sure you want to do that? (yes/no)");
                    ChatUtils.awaitInput(p1, (input) -> {
                        if (input.equalsIgnoreCase("yes")) {
                            launch(p1, b, celestialWorld, fuel - usedFuel, trueEff);
                        }
                    });
                    return false;
                });
            }

            menu.open(p);
        }

        private void launch(@Nonnull Player p, @Nonnull Block b, CelestialWorld worldTo, int fuelLeft, double eff) {
            ConfigurationSection section = ConfigUtils.load("config.yml").getConfiguration().getConfigurationSection("rockets");
            if (section == null) {
                PluginUtils.log(Level.SEVERE, "Could not load launch messages!");
                return;
            }
            List<String> messages = section.getStringList("launch-msgs");

            // yes ik boolean#tostring isn't needed but just for safety
            BlockStorage.addBlockInfo(b, "isLaunching", Boolean.toString(true));

            World world = p.getWorld();

            new BukkitRunnable() {
                private int times = 0;
                private final Block pad = b.getRelative(BlockFace.DOWN);

                @Override
                public void run() {
                    if (times++ < 20) {
                        for (BlockFace face : Util.SURROUNDING_FACES) {
                            Block block = pad.getRelative(face);
                            world.spawnParticle(Particle.ASH, block.getLocation(), 100, 0.5, 0.5, 0.5);
                        }
                    } else {
                        this.cancel();
                    }
                }
            }.runTaskTimer(Galactifun.getInstance(), 0, 10);

            World to = worldTo.getWorld();

            Chunk destChunk = to.getChunkAt(0, 0);
            if (!destChunk.isLoaded()) {
                destChunk.load(true);
            }

            Block destBlock = to.getHighestBlockAt(8, 8).getRelative(BlockFace.UP);

            PluginUtils.runSync(() -> {
                destBlock.setType(Material.CHEST);
                BlockData data = destBlock.getBlockData();
                if (data instanceof Rotatable) {
                    ((Rotatable) data).setRotation(BlockFace.NORTH);
                }
                destBlock.setBlockData(data, true);

                BlockState state = PaperLib.getBlockState(destBlock, false).getState();
                if (state instanceof Chest) {
                    Chest chest = (Chest) state;
                    Inventory inv = chest.getInventory();
                    inv.clear(); // just in case
                    inv.addItem(this.getItem().clone());

                    BiMap<ItemStack, Double> fuels = LaunchPadCore.getFuels();
                    ItemStack fuel = fuels.inverse().get(Util.getClosest(fuels.values(), eff));
                    if (fuel != null) {
                        fuel = fuel.clone();
                        fuel.setAmount(fuelLeft);
                        inv.addItem(fuel);
                    }
                }
                state.update();

                p.sendMessage(ChatColor.GOLD + messages.get(ThreadLocalRandom.current().nextInt(messages.size())) + "...");
            }, 40);

            PluginUtils.runSync(() -> p.sendMessage(ChatColor.GOLD + messages.get(ThreadLocalRandom.current().nextInt(messages.size())) + "..."), 80);
            PluginUtils.runSync(() -> p.sendMessage(ChatColor.GOLD + messages.get(ThreadLocalRandom.current().nextInt(messages.size())) + "..."), 120);
            PluginUtils.runSync(() -> p.sendMessage(ChatColor.GOLD + messages.get(ThreadLocalRandom.current().nextInt(messages.size())) + "..."), 160);
            PluginUtils.runSync(() -> {
                p.sendMessage(ChatColor.GOLD + "Verifying blast awesomeness...");

                for (Entity entity : world.getEntities()) {
                    if ((entity instanceof LivingEntity && !(entity instanceof ArmorStand)) || entity instanceof Item) {
                        if (entity.getLocation().distanceSquared(b.getLocation()) <= 25) {
                            PaperLib.teleportAsync(entity, destBlock.getLocation().add(0, 1, 0));
                        }
                    }
                }

                b.setType(Material.AIR);
                BlockStorage.clearBlockInfo(b);
            }, 200);
        }

        private static List<CelestialWorld> getReachable(CelestialWorld current, long maxDist) {
            List<CelestialWorld> reachable = new ArrayList<>();
            for (CelestialWorld world : CelestialWorld.getWorlds().values()) {
                if (world.getDistanceTo(current) * Util.KM_PER_LY <= maxDist) {
                    reachable.add(world);
                }
            }

            return reachable;
        }
    }
}
