package dev.alexbright.lockthat.handlers;

import dev.alexbright.lockthat.LockThat;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class LockHandler {

    public static final HashMap<UUID, String> pendingBreaks = new HashMap<>();
    public static final List<UUID> pendingLocks = new ArrayList<>();

    private static LockThat plugin = LockThat.getInstance();
    private static final ConfigFile data = plugin.getData();

    public static boolean add(Block block, Player owner) {
        if (contains(block)) return false;
        ConfigurationSection configSection = data.getConfig().getConfigurationSection("locks")
                .createSection(getLocationString(block.getLocation()));
        configSection.set("type", block.getType().name());
        configSection.set("owners", Collections.singletonList(owner.getUniqueId().toString()));
        configSection.set("users", Collections.emptyList());
        data.save();
        return contains(block);
    }

    public static boolean remove(Block block) {
        if (!contains(block)) return false;
        ConfigurationSection configSection = data.getConfig().getConfigurationSection("locks");
        configSection.set(getLocationString(block.getLocation()), null);
        data.save();
        return !configSection.contains(getLocationString(block.getLocation()));
    }

    public static boolean contains(Block block) {
        return contains(block.getLocation());
    }

    public static boolean contains(Location loc) {
        return data.getConfig().getConfigurationSection("locks").contains(getLocationString(loc));
    }

    private static List<UUID> getAccessIds(Block block, boolean ownersOnly) {
        List<UUID> ids = new ArrayList<>();
        if (!contains(block)) return ids;
        ConfigurationSection configSection = data.getConfig().getConfigurationSection("locks")
                .getConfigurationSection(getLocationString(block.getLocation()));
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

    public static String getLocationString(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

}
