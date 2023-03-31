package dev.alexbright.lockthat.handlers;

import dev.alexbright.lockthat.LockThat;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChestHandler {

    private static LockThat plugin = LockThat.getInstance();
    private static final ConfigFile data = plugin.getData();

    public static boolean add(Block block, Player player) {
        if (contains(block)) return false;
        ConfigurationSection configSection = data.getConfig().getConfigurationSection("chests")
                .getConfigurationSection(getLocationString(block.getLocation()));
        
    }

    public static boolean remove(Block block, Player player) {

    }

    public static boolean contains(Block block) {
        return contains(block.getLocation());
    }

    public static boolean contains(Location loc) {
        return data.getConfig().getConfigurationSection("chests").contains(getLocationString(loc));
    }

    private static List<UUID> getAccessIds(Block block, boolean ownersOnly) {
        List<UUID> ids = new ArrayList<>();
        if (!isLocked(block)) return ids;
        ConfigurationSection configSection = data.getConfig().getConfigurationSection(getLocationString(block.getLocation()));
        List<String> users = configSection.getStringList("owners");
        users.forEach(s -> ids.add(UUID.fromString(s)));
        if (!ownersOnly) {
            users = configSection.getStringList("users");
            users.forEach(s -> ids.add(UUID.fromString(s)));
        }
        return ids;
    }

    public static boolean hasAccess(Player p, Block block) {
        return getAccessIds(block, false).contains(p.getUniqueId());
    }

    public static boolean isOwner(Player p, Block block) {
        return getAccessIds(block, true).contains(p.getUniqueId());
    }

    private static String getLocationString(Location location) {
        return location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

}
