package dev.alexbright.lockthat.listeners;

import dev.alexbright.lockthat.LockThat;
import dev.alexbright.lockthat.handlers.LockHandler;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.UUID;

public class BlockListener implements Listener {

    private final HashMap<UUID, String> pendingBreaks = LockHandler.pendingBreaks;

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();
        if (pendingBreaks.containsKey(p.getUniqueId())) {
            if (pendingBreaks.get(p.getUniqueId()).equals(LockHandler.getLocationString(b.getLocation()))) {
                if (!LockHandler.remove(b)) {
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "Unknown error occurred... please try again");
                    return;
                }
                p.sendMessage(LockThat.prefix + ChatColor.GREEN + "Removed lock at " + LockHandler.getLocationString(b.getLocation()));
                return;
            }
            pendingBreaks.remove(p.getUniqueId());
        }
        if (b.getState() instanceof Container) {
            if (b.getState() instanceof Chest) {
                Chest chest = (Chest) b.getState();
                InventoryHolder holder = chest.getInventory().getHolder();
                if (holder instanceof DoubleChest) {
                    DoubleChest doubleChest = (DoubleChest) holder;
                    Chest leftChest = (Chest) doubleChest.getLeftSide();
                    Chest rightChest = (Chest) doubleChest.getRightSide();
                    if (LockHandler.contains(leftChest.getBlock()) || LockHandler.contains(rightChest.getBlock())) {
                        b = LockHandler.contains(rightChest.getBlock()) ? rightChest.getBlock() : leftChest.getBlock();
                        if (!LockHandler.isOwner(p, b)) {
                            e.setCancelled(true);
                            // send message with owner name
                            return;
                        }
                        e.setCancelled(true);
                        pendingBreaks.put(p.getUniqueId(), LockHandler.getLocationString(b.getLocation()));
                        p.sendMessage(LockThat.prefix + ChatColor.RED + ChatColor.BOLD + "This double chest is locked!");
                        p.sendMessage(LockThat.prefix + ChatColor.RED + "Breaking part of it will remove the lock.");
                        p.sendMessage(LockThat.prefix + ChatColor.YELLOW + "To continue, attempt to break it again.");
                    }
                } else {
                    if (LockHandler.contains(b)) {
                        if (!LockHandler.isOwner(p, b)) {
                            e.setCancelled(true);
                            // send message with owner name
                            return;
                        }
                        e.setCancelled(true);
                        pendingBreaks.put(p.getUniqueId(), LockHandler.getLocationString(b.getLocation()));
                        p.sendMessage(LockThat.prefix + ChatColor.RED + ChatColor.BOLD + "This chest is locked!");
                        p.sendMessage(LockThat.prefix + ChatColor.RED + "Breaking it will remove the lock.");
                        p.sendMessage(LockThat.prefix + ChatColor.YELLOW + "To continue, attempt to break it again.");
                    }
                }
            } else {
                if (LockHandler.contains(b)) {
                    if (!LockHandler.isOwner(p, b)) {
                        e.setCancelled(true);
                        // send message with owner name
                        return;
                    }
                    e.setCancelled(true);
                    pendingBreaks.put(p.getUniqueId(), LockHandler.getLocationString(b.getLocation()));
                    p.sendMessage(LockThat.prefix + ChatColor.RED + ChatColor.BOLD + "This container is locked!");
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "Breaking it will remove the lock.");
                    p.sendMessage(LockThat.prefix + ChatColor.YELLOW + "To continue, attempt to break it again.");
                }
            }
        } else if (b.getState() instanceof Openable) {
            if (b.getBlockData() instanceof Door) {
                Door d = (Door) b.getBlockData();
                Location loc = b.getLocation();
                if (d.getHalf() == Bisected.Half.TOP) {
                    loc = loc.subtract(0, 1, 0);
                }
                b = loc.getBlock();
            }
            if (LockHandler.contains(b)) {
                if (!LockHandler.isOwner(p, b)) {
                    e.setCancelled(true);
                    // send message with owner name
                    return;
                }
                e.setCancelled(true);
                pendingBreaks.put(p.getUniqueId(), LockHandler.getLocationString(b.getLocation()));
                p.sendMessage(LockThat.prefix + ChatColor.RED + ChatColor.BOLD + "This openable block is locked!");
                p.sendMessage(LockThat.prefix + ChatColor.RED + "Breaking it will remove the lock.");
                p.sendMessage(LockThat.prefix + ChatColor.YELLOW + "To continue, attempt to break it again.");
            }
        }
    }

}
