package dev.alexbright.lockthat.handlers;

import dev.alexbright.lockthat.enums.LockType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.*;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.TrapDoor;
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
    public static final HashMap<UUID, Date> lastChecked = new HashMap<>();

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

    public static boolean removeAll(Player p) {
        ConfigurationSection configSection = data.getConfig().getConfigurationSection("locks");
        return true;
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
        if (!owners.contains(p.getUniqueId().toString())) owners.add(p.getUniqueId().toString());
        configSection.set("owners", owners);
        data.save();
        return true;
    }

    public static boolean addUser(Block block, Player p) {
        if (hasAccess(p, block)) return false;
        ConfigurationSection configSection = getConfigSection(block.getLocation());
        List<String> users = configSection.getStringList("users");
        if (!users.contains(p.getUniqueId().toString())) users.add(p.getUniqueId().toString());
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
        lastChecked.remove(id);
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
        LockType type = getType(block);

        // check if block is double chest and set block to whichever one is in data file
        if (type == LockType.DOUBLE_CHEST) {
            DoubleChest dc = (DoubleChest) ((Chest) block.getState()).getInventory().getHolder();
            Chest left = (Chest) dc.getLeftSide();
            Chest right = (Chest) dc.getRightSide();
            if (LockHandler.contains(right.getBlock()) || LockHandler.contains(left.getBlock()))
                block = LockHandler.contains(right.getBlock()) ? right.getBlock() : left.getBlock();

        // check if block is Door and make sure block is bottom half
        } else if (type == LockType.DOOR) {
            Door d = (Door) block.getBlockData();
            Location loc = block.getLocation();
            if (d.getHalf() == Bisected.Half.TOP) {
                loc = loc.subtract(0, 1, 0);
            }
            block = loc.getBlock();
        }

        // if
        return block;
    }

    public static LockType getType(Block block) {
        LockType type = null;
        BlockState state = block.getState();
        BlockData bData = block.getBlockData();

        if (state instanceof Container) {
            if (state instanceof Chest) {
                if (((Chest) state).getInventory().getHolder() instanceof DoubleChest) type = LockType.DOUBLE_CHEST;
                else type = LockType.CHEST;
            }
            else if (state instanceof Barrel) type = LockType.BARREL;
            else if (state instanceof BlastFurnace) type = LockType.BLAST_FURNACE;
            else if (state instanceof BrewingStand) type = LockType.BREWING_STAND;
            else if (state instanceof Dispenser) type = LockType.DISPENSER;
            else if (state instanceof Dropper) type = LockType.DROPPER;
            else if (state instanceof Furnace && !(state instanceof Smoker)) type = LockType.FURNACE;
            else if (state instanceof Hopper) type = LockType.HOPPER;
            else if (state instanceof ShulkerBox) type = LockType.SHULKER_BOX;
            else if (state instanceof Smoker) type = LockType.SMOKER;
            else type = LockType.CONTAINER;
        } else if (bData instanceof Openable) {
            if (bData instanceof Door) type = LockType.DOOR;
            else if (bData instanceof TrapDoor) type = LockType.TRAPDOOR;
            else if (bData instanceof Barrel) type = LockType.BARREL;
            else if (bData instanceof Gate) type = LockType.GATE;
            else type = LockType.OPENABLE;
        }

        return type;
    }

}
