package dev.alexbright.lockthat.listeners;

import dev.alexbright.lockthat.LockThat;
import dev.alexbright.lockthat.enums.LockType;
import dev.alexbright.lockthat.handlers.LockHandler;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

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
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "Removed lock at " + ChatColor.ITALIC + LockHandler.getLocationString(b.getLocation())));
                return;
            }
        }

        LockType type = LockHandler.getType(b);

        // if block is not lockable type
        if (type == null)
            return;

        if (LockHandler.contains(b)) {
            if (!LockHandler.isOwner(p, b)) {
                e.setCancelled(true);
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "This " + type + " is locked by " + ChatColor.BOLD + LockHandler.getMainOwner(b).getName()));
                return;
            }
            e.setCancelled(true);
            pendingBreaks.put(p.getUniqueId(), LockHandler.getLocationString(b.getLocation()));
            p.sendMessage(LockThat.prefix + ChatColor.RED + ChatColor.BOLD + "This " + type + " is locked!");
            p.sendMessage(LockThat.prefix + ChatColor.RED + "Breaking "
                    + (type == LockType.DOUBLE_CHEST ? "part of it" : "it")
                    + " will remove the lock");
            p.sendMessage(LockThat.prefix + ChatColor.YELLOW + "To continue, attempt to break it again");
        }

    }

}
