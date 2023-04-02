package dev.alexbright.lockthat.listeners;

import dev.alexbright.lockthat.LockThat;
import dev.alexbright.lockthat.handlers.LockHandler;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.Openable;
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
        Block b = LockHandler.findRootBlock(e.getBlock());

        // if player has pending lock removal request
        if (pendingBreaks.containsKey(p.getUniqueId())) {
            if (pendingBreaks.remove(p.getUniqueId()).equals(LockHandler.getLocationString(b.getLocation()))) {
                if (!LockHandler.remove(b)) {
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "Unknown error occurred... please try again");
                    return;
                }
                p.sendMessage(LockThat.prefix + ChatColor.GREEN + "Removed lock at " + LockHandler.getLocationString(b.getLocation()));
                return;
            }
        }

        if (b.getState() instanceof Container) {

            // if block is chest, handle both double chest and single chest
            if (b.getState() instanceof Chest) {
                Chest chest = (Chest) b.getState();
                InventoryHolder holder = chest.getInventory().getHolder();

                // if block is double chest, check if either side is locked
                if (holder instanceof DoubleChest) {
                    if (LockHandler.contains(b)) {
                        if (!LockHandler.isOwner(p, b)) {
                            e.setCancelled(true);
                            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "This is locked by " + ChatColor.BOLD + LockHandler.getMainOwner(b).getName()));
                            return;
                        }
                        e.setCancelled(true);
                        pendingBreaks.put(p.getUniqueId(), LockHandler.getLocationString(b.getLocation()));
                        p.sendMessage(LockThat.prefix + ChatColor.RED + ChatColor.BOLD + "This double chest is locked!");
                        p.sendMessage(LockThat.prefix + ChatColor.RED + "Breaking part of it will remove the lock");
                        p.sendMessage(LockThat.prefix + ChatColor.YELLOW + "To continue, attempt to break it again");
                    }

                // if block is single chest
                } else {
                    if (LockHandler.contains(b)) {
                        if (!LockHandler.isOwner(p, b)) {
                            e.setCancelled(true);
                            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "This is locked by " + ChatColor.BOLD + LockHandler.getMainOwner(b).getName()));
                            return;
                        }
                        e.setCancelled(true);
                        pendingBreaks.put(p.getUniqueId(), LockHandler.getLocationString(b.getLocation()));
                        p.sendMessage(LockThat.prefix + ChatColor.RED + ChatColor.BOLD + "This chest is locked!");
                        p.sendMessage(LockThat.prefix + ChatColor.RED + "Breaking it will remove the lock");
                        p.sendMessage(LockThat.prefix + ChatColor.YELLOW + "To continue, attempt to break it again");
                    }
                }

            // if block is other type of container
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
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "Breaking it will remove the lock");
                    p.sendMessage(LockThat.prefix + ChatColor.YELLOW + "To continue, attempt to break it again");
                }
            }

        // if block is not container, but still openable
        } else if (b.getBlockData() instanceof Openable) {

            if (LockHandler.contains(b)) {
                if (!LockHandler.isOwner(p, b)) {
                    e.setCancelled(true);
                    // send message with owner name
                    return;
                }
                e.setCancelled(true);
                pendingBreaks.put(p.getUniqueId(), LockHandler.getLocationString(b.getLocation()));
                p.sendMessage(LockThat.prefix + ChatColor.RED + ChatColor.BOLD + "This openable block is locked!");
                p.sendMessage(LockThat.prefix + ChatColor.RED + "Breaking it will remove the lock");
                p.sendMessage(LockThat.prefix + ChatColor.YELLOW + "To continue, attempt to break it again");
            }
        }
    }

}
