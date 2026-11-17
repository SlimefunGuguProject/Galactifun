package io.github.addoncommunity.galactifun.api.items;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Skull;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import io.github.addoncommunity.galactifun.Galactifun;
import me.mrCookieSlime.Slimefun.api.BlockStorage;

/** Durable evidence for manual recovery, never an automatically replayed transfer. */
final class RocketLaunchRecovery {

    private final File file;
    private final YamlConfiguration data = new YamlConfiguration();

    RocketLaunchRecovery(RocketLaunchSession launch, Skull source, org.bukkit.block.Block target,
                         List<ItemStack> delivery, ItemStack[] before, ItemStack[] after) throws IOException {
        file = new File(Galactifun.instance().getDataFolder(), "rocket-recovery/" + launch.id + ".yml");
        data.set("session", launch.id);
        data.set("player", launch.player.getUniqueId().toString());
        data.set("source.location", launch.source.getLocation());
        data.set("source.id", launch.rocket.getId());
        data.set("source.block-data", source.getBlockData().getAsString());
        data.set("source.fuel", BlockStorage.getLocationInfo(launch.source.getLocation(), "fuel"));
        data.set("source.fuel-type", BlockStorage.getLocationInfo(launch.source.getLocation(), "fuelType"));
        ItemStack head = new ItemStack(source.getType());
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setPlayerProfile(source.getPlayerProfile());
        copyData(source.getPersistentDataContainer(), meta.getPersistentDataContainer());
        head.setItemMeta(meta);
        data.set("source.head-with-pdc", head);
        data.set("target.location", target.getLocation());
        data.set("target.before", Arrays.asList(before));
        data.set("target.after", Arrays.asList(after));
        data.set("delivery", delivery);
        save("prepared");
    }

    void save(String phase) throws IOException {
        data.set("phase", phase);
        Files.createDirectories(file.toPath().getParent());
        File temporary = new File(file.getParentFile(), file.getName() + ".tmp");
        data.save(temporary);
        try {
            Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    String path() {
        return file.getPath();
    }

    // Bukkit 1.18 has no PDC copy/byte-serialization API. Preserve its primitive types,
    // including custom byte-serialized cargo, without decoding/re-encoding each key.
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void copyData(PersistentDataContainer from, PersistentDataContainer to) {
        PersistentDataType[] types = { PersistentDataType.BYTE, PersistentDataType.SHORT,
                PersistentDataType.INTEGER, PersistentDataType.LONG, PersistentDataType.FLOAT,
                PersistentDataType.DOUBLE, PersistentDataType.STRING, PersistentDataType.BYTE_ARRAY,
                PersistentDataType.INTEGER_ARRAY, PersistentDataType.LONG_ARRAY,
                PersistentDataType.TAG_CONTAINER, PersistentDataType.TAG_CONTAINER_ARRAY };
        for (NamespacedKey key : from.getKeys()) {
            boolean copied = false;
            for (PersistentDataType type : types) {
                if (from.has(key, type)) {
                    to.set(key, type, from.get(key, type));
                    copied = true;
                    break;
                }
            }
            if (!copied) throw new IllegalStateException("Cannot back up rocket PDC key " + key);
        }
    }
}
