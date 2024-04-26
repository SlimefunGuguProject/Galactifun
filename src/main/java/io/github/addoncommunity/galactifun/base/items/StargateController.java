package io.github.addoncommunity.galactifun.base.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.EndGateway;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import com.destroystokyo.paper.event.player.PlayerTeleportEndGatewayEvent;
import io.github.addoncommunity.galactifun.Galactifun;
import io.github.addoncommunity.galactifun.api.worlds.AlienWorld;
import io.github.addoncommunity.galactifun.base.BaseItems;
import io.github.addoncommunity.galactifun.util.BSUtils;
import io.github.mooy1.infinitylib.common.Events;
import io.github.mooy1.infinitylib.common.Scheduler;
import io.github.mooy1.infinitylib.machines.MenuBlock;
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

// TODO clean up if possible
public final class StargateController extends SlimefunItem implements Listener {

    private static final int[] BACKGROUND = new int[] { 1, 2, 6, 7, 8 };
    private static final int ADDRESS_SLOT = 3;
    private static final int DESTINATION_SLOT = 4;
    private static final int DEACTIVATE_SLOT = 5;

    private static final ComponentPosition[] RING_POSITIONS = new ComponentPosition[] {
            // bottom
            new ComponentPosition(0, 1),
            new ComponentPosition(0, -1),

            // corners
            new ComponentPosition(1, -2),
            new ComponentPosition(1, 2),
            new ComponentPosition(5, -2),
            new ComponentPosition(5, 2),

            // left side
            new ComponentPosition(2, 3),
            new ComponentPosition(3, 3),
            new ComponentPosition(4, 3),

            // right side
            new ComponentPosition(2, -3),
            new ComponentPosition(3, -3),
            new ComponentPosition(4, -3),

            // top
            new ComponentPosition(6, -1),
            new ComponentPosition(6, 0),
            new ComponentPosition(6, 1),
    };

    private static final ComponentPosition[] PORTAL_POSITIONS;
    private static final int GATEWAY_TICKS = 201;

    static {
        List<ComponentPosition> portalPositions = new LinkedList<>(Arrays.asList(
                new ComponentPosition(1, -1),
                new ComponentPosition(1, 0),
                new ComponentPosition(1, 1)
        ));
        for (int y = 2; y <= 4; y++) {
            for (int z = -2; z <= 2; z++) {
                portalPositions.add(new ComponentPosition(y, z));
            }
        }
        portalPositions.add(new ComponentPosition(5, -1));
        portalPositions.add(new ComponentPosition(5, 0));
        portalPositions.add(new ComponentPosition(5, 1));

        PORTAL_POSITIONS = portalPositions.toArray(new ComponentPosition[0]);
    }

    public StargateController(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);

        Events.registerListener(this);

        addItemHandler((BlockUseHandler) e -> e.getClickedBlock().ifPresent(b -> onUse(e, e.getPlayer(), b)));

        addItemHandler(new BlockBreakHandler(true, true) {
            @Override
            @ParametersAreNonnullByDefault
            public void onPlayerBreak(BlockBreakEvent e, ItemStack item, List<ItemStack> drops) {
                if (Boolean.parseBoolean(BlockStorage.getLocationInfo(e.getBlock().getLocation(), "locked"))) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(ChatColor.RED + "在摧毁星门之前先关闭它");
                }
            }
        });
    }

    public static boolean isPartOfStargate(@Nonnull Block b) {
        for (ComponentPosition position : RING_POSITIONS) {
            if (!position.isInSameRing(b)) {
                return false;
            }
        }

        return true;
    }

    @Nonnull
    public static Optional<List<Block>> getRingBlocks(@Nonnull Block b) {
        List<Block> rings = new ArrayList<>();
        for (ComponentPosition position : RING_POSITIONS) {
            if (position.isInSameRing(b)) {
                rings.add(position.getBlock(b));
            } else {
                return Optional.empty();
            }
        }

        return Optional.of(rings);
    }

    @Nonnull
    public static Optional<List<Block>> getPortalBlocks(@Nonnull Block b) {
        List<Block> portals = new ArrayList<>();
        for (ComponentPosition position : PORTAL_POSITIONS) {
            if (position.isPortal(b)) {
                portals.add(position.getBlock(b));
            } else {
                return Optional.empty();
            }
        }

        return Optional.of(portals);
    }

    public static void lockBlocks(Block controller, boolean lock) {
        String data = Boolean.toString(lock);
        getRingBlocks(controller).ifPresent(l -> l.forEach(b -> BlockStorage.addBlockInfo(b, "locked", data)));
        getPortalBlocks(controller).ifPresent(l -> l.forEach(b -> BlockStorage.addBlockInfo(b, "locked", data)));
    }

    private void onUse(PlayerRightClickEvent event, Player p, Block b) {
        if (!isPartOfStargate(b)) {
            p.sendMessage(ChatColor.RED + "星门结构不完整!");
            return;
        }
        event.cancel();
        if (getPortalBlocks(b).isEmpty()) {
            for (ComponentPosition position : PORTAL_POSITIONS) {
                Block portal = position.getBlock(b);
                portal.setType(Material.END_GATEWAY);
                EndGateway gateway = (EndGateway) portal.getState();
                gateway.setAge(GATEWAY_TICKS);
                gateway.setExitLocation(b.getLocation());
                gateway.update(false, false);
            }

            String destAddress = BlockStorage.getLocationInfo(b.getLocation(), "destination");
            if (destAddress != null) {
                setDestination(destAddress, b, p);
            }

            lockBlocks(b, true);
            p.sendMessage(ChatColor.YELLOW + "星门已激活!");
            return;
        }

        ChestMenu menu = getMenu(b);
        menu.open(p);
    }

    @Nonnull
    private ChestMenu getMenu(@Nonnull Block b) {
        ChestMenu menu = new ChestMenu(this.getItemName());
        for (int i : BACKGROUND) {
            menu.addItem(i, MenuBlock.BACKGROUND_ITEM, ChestMenuUtils.getEmptyClickHandler());
        }

        Location l = b.getLocation();

        // 汉化版不支持遍历所有方块信息，因此使用Base64编码存储地址信息
        String address = Base64.getEncoder().encodeToString(String.format(
                "%s;%d;%d;%d",
                b.getWorld().getName(),
                l.getBlockX(),
                l.getBlockY(),
                l.getBlockZ()
        ).getBytes());

        String destination = BlockStorage.getLocationInfo(l, "destination");
        destination = destination == null ? "" : destination;

        menu.addItem(ADDRESS_SLOT, new CustomItemStack(
                Material.BOOK,
                "&f星门地址: " + address,
                "&7点击以获取星门地址"
        ), (p, i, s, c) -> {
            p.sendMessage(
                    Component.text()
                            .color(NamedTextColor.YELLOW)
                            .content("星门地址（点击复制）: " + address)
                            .clickEvent(ClickEvent.copyToClipboard(address))
                            .build()
            );
            p.closeInventory();
            return false;
        });

        menu.addItem(DEACTIVATE_SLOT, new CustomItemStack(
                Material.BARRIER,
                "&f点击取消激活星门"
        ), (p, i, s, c) -> {
            getPortalBlocks(b).ifPresent(li -> {
                for (Block block : li) {
                    block.setType(Material.AIR);
                    BlockStorage.clearBlockInfo(block);
                }
            });
            lockBlocks(b, false);
            p.closeInventory();
            return false;
        });

        menu.addItem(DESTINATION_SLOT, new CustomItemStack(
                Material.RAIL,
                "&f点击设置星门目的地",
                "&7当前目的地: " + destination
        ), (p, i, s, c) -> {
            p.sendMessage(ChatColor.YELLOW + "输入目标星门的地址");
            ChatUtils.awaitInput(p, st -> setDestination(st, b, p));
            p.closeInventory();
            return false;
        });

        return menu;
    }

    private static void setDestination(String destination, Block b, Player p) {
        Location dest;
        String bAddress;
        try {
            bAddress = new String(Base64.getDecoder().decode(destination));
        } catch (IllegalArgumentException e) {
            p.sendMessage(ChatColor.RED + "无效的地址!");
            return;
        }

        String[] parts = bAddress.split(";");
        try {
            dest = new Location(
                    Galactifun.instance().getServer().getWorld(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3])
            );
        } catch (NumberFormatException e) {
            p.sendMessage(ChatColor.RED + "无效的地址!");
            return;
        }

        Optional<List<Block>> portalOptional = getPortalBlocks(b);
        if (portalOptional.isEmpty()) {
            p.sendMessage(ChatColor.RED + "星门没有激活...");
            return;
        }

        BSUtils.setStoredLocation(b.getLocation(), "dest", dest);

        p.sendMessage(ChatColor.YELLOW + String.format(
                "设置星门目的地为%s，位于世界%s, x: %d, y: %d, z: %d",
                destination,
                dest.getWorld().getName(),
                dest.getBlockX(),
                dest.getBlockY(),
                dest.getBlockZ()
        ));

        BlockStorage.addBlockInfo(b, "destination", destination);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onGateBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        if (b.getType() == Material.END_GATEWAY &&
                Boolean.parseBoolean(BlockStorage.getLocationInfo(b.getLocation(), "locked"))) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED + "在摧毁星门之前先关闭它");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onUsePortal(PlayerTeleportEndGatewayEvent e) {
        Location exit = e.getGateway().getExitLocation();
        if (exit == null || !(BlockStorage.check(exit) instanceof StargateController)) return;
        Location dest = BSUtils.getStoredLocation(exit, "dest");
        if (dest == null) return;

        e.setCancelled(true);

        Player p = e.getPlayer();
        if (p.hasMetadata("disableStargate")) return;

        Block b = dest.getBlock();
        if (BlockStorage.check(b, BaseItems.STARGATE_CONTROLLER.getItemId()) &&
                StargateController.getPortalBlocks(b).isEmpty()) {
            p.sendMessage(ChatColor.RED + "目标星门未激活");
            return;
        }

        Block destBlock = b.getRelative(1, 0, 0);
        if (destBlock.getType().isEmpty()) {
            // Check if the player is teleporting to an alien world, and if so, allow them to
            AlienWorld world = Galactifun.worldManager().getAlienWorld(destBlock.getWorld());
            if (world != null) {
                e.getPlayer().setMetadata("CanTpAlienWorld", new FixedMetadataValue(Galactifun.instance(), true));
            }
            p.teleportAsync(destBlock.getLocation());
            p.setMetadata("disableStargate", new FixedMetadataValue(Galactifun.instance(), true));
            Scheduler.run(10, () -> p.removeMetadata("disableStargate", Galactifun.instance()));
        } else {
            p.sendMessage(ChatColor.RED + "目标位置无法抵达");
        }
    }

    private static final record ComponentPosition(int y, int z) {

        public boolean isInSameRing(@Nonnull Block b) {
            return BlockStorage.check(b.getRelative(0, this.y, this.z)) instanceof StargateRing;
        }

        @Nonnull
        public Block getBlock(@Nonnull Block b) {
            return b.getRelative(0, this.y, this.z);
        }

        public boolean isPortal(@Nonnull Block b) {
            return b.getRelative(0, this.y, this.z).getType() == Material.END_GATEWAY;
        }

    }

}
