package dev.alexbright.lockthat.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class LockHandler {

    public static final HashMap<UUID, String> pendingBreaks = new HashMap<>();
    public enum RequestType { LOCK, UNLOCK }
    public static final HashMap<UUID, RequestType> pendingLocks = new HashMap<>();
    public static final HashMap<UUID, Player> pendingOwner = new HashMap<>();
    public static final HashMap<UUID, Player> pendingUser = new HashMap<>();
    public static final List<UUID> pendingChecks = new ArrayList<>();

    private static ConfigFile data;

    public static void setData(ConfigFile file) {
        data = file;
    }

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
        ConfigurationSection configSection = getConfigSection(block.getLocation());
        List<String> users = configSection.getStringList("owners");
        users.forEach(s -> ids.add(UUID.fromString(s)));
        if (!ownersOnly) {
            users = configSection.getStringList("users");
            users.forEach(s -> ids.add(UUID.fromString(s)));
        }
        return ids;
    }

    public static List<UUID> getOwners(Block block) {
        return getAccessIds(block, true);
    }

    public static OfflinePlayer getMainOwner(Block block) {
        if (!contains(block)) return null;
        return Bukkit.getOfflinePlayer(getAccessIds(block, true).get(0));
    }

    public static boolean addOwner(Block block, Player p) {
        if (isOwner(p, block)) return false;
        if (hasAccess(p, block)) removeUser(block, p);
        ConfigurationSection configSection = getConfigSection(block.getLocation());
        List<String> owners = configSection.getStringList("owners");
        if (!owners.contains(p.getUniqueId().toString())) return false;
        owners.add(p.getUniqueId().toString());
        configSection.set("owners", owners);
        data.save();
        return true;
    }

    public static boolean addUser(Block block, Player p) {
        if (hasAccess(p, block)) return false;
        ConfigurationSection configSection = getConfigSection(block.getLocation());
        List<String> users = configSection.getStringList("users");
        if (!users.contains(p.getUniqueId().toString())) return false;
        users.add(p.getUniqueId().toString());
        configSection.set("users", users);
        data.save();
        return true;
    }

    public static boolean removeUser(Block block, Player p) {
        if (!contains(block)) return false;
        ConfigurationSection configSection = getConfigSection(block.getLocation());
        List<String> users = configSection.getStringList("users");
        boolean removed = users.remove(p.getUniqueId().toString());
        if (removed) {
            configSection.set("users", users);
            data.save();
        }
        return removed;
    }

//    public static boolean removeOwner(Block block, Player p) {
//        if (!contains(block)) return false;
//        ConfigurationSection configSection = getConfigSection(block.getLocation());
//        List<String> owners = configSection.getStringList("owners");
//        if (!owners.contains(p.getUniqueId().toString())) return false;
//        boolean removed = owners.remove(p.getUniqueId().toString());
//        if (removed) {
//            configSection.set("owners", owners);
//            data.save();
//        }
//        return removed;
//    }

    public static boolean hasAccess(Player p, Block block) {
        return getAccessIds(block, false).contains(p.getUniqueId());
    }

    public static boolean isOwner(Player p, Block block) {
        return getAccessIds(block, true).contains(p.getUniqueId());
    }

    public static String getLocationString(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    private static ConfigurationSection getConfigSection(Location loc) {
        return data.getConfig().getConfigurationSection("locks").getConfigurationSection(getLocationString(loc));
    }

    public static void cancelPending(Player p) {
        UUID id = p.getUniqueId();
        pendingLocks.remove(id);
        pendingBreaks.remove(id);
        pendingOwner.remove(id);
        pendingUser.remove(id);
        pendingChecks.remove(id);
    }

    public static boolean hasPending(Player p) {
        UUID id = p.getUniqueId();
        return pendingLocks.containsKey(id)
                || pendingBreaks.containsKey(id)
                || pendingOwner.containsKey(id)
                || pendingUser.containsKey(id)
                || pendingChecks.contains(id);
    }

    public static Block findRootBlock(Block block) {
        Block b = block;

        // check if block is double chest
        if (b.getState() instanceof Chest && ((Chest) b.getState()).getInventory().getHolder() instanceof DoubleChest) {
            DoubleChest dc = (DoubleChest) ((Chest) b.getState()).getInventory().getHolder();
            Chest left = (Chest) dc.getLeftSide();
            Chest right = (Chest) dc.getRightSide();
            if (LockHandler.contains(right.getBlock()) || LockHandler.contains(left.getBlock()))
                b = LockHandler.contains(right.getBlock()) ? right.getBlock() : left.getBlock();

        // check if block is Door and make sure block is bottom half
        } else if (b.getBlockData() instanceof Door) {
            Door d = (Door) b.getBlockData();
            Location loc = b.getLocation();
            if (d.getHalf() == Bisected.Half.TOP) {
                loc = loc.subtract(0, 1, 0);
            }
            b = loc.getBlock();
        }

        return b;
    }

}
