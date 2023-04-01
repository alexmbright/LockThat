package dev.alexbright.lockthat.listeners;

import dev.alexbright.lockthat.LockThat;
import dev.alexbright.lockthat.handlers.LockHandler;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final List<UUID> pendingLocks = LockHandler.pendingLocks;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block b = e.getClickedBlock();
            if (pendingLocks.contains(p.getUniqueId())) {
                pendingLocks.remove(p.getUniqueId());
                if (b.getState() instanceof Container || b.getState() instanceof Openable) {
                    if (LockHandler.contains(b)) {
                        p.sendMessage(LockThat.prefix + ChatColor.RED + ChatColor.RED + "This is already locked!");
                        p.sendMessage(LockThat.prefix + ChatColor.RED + "Lock request cancelled.");
                        return;
                    }
                    if (!LockHandler.add(b, p)) {
                        p.sendMessage(LockThat.prefix + ChatColor.RED + "Unknown error occurred... please try again.");
                        return;
                    }
                    p.sendMessage(LockThat.prefix + ChatColor.GREEN + ChatColor.BOLD + "Success! " + ChatColor.RESET
                            + ChatColor.YELLOW + "Lock placed at " + LockHandler.getLocationString(b.getLocation()));
                } else {
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "This block is not lockable!");
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "Lock request cancelled.");
                }
            }
        } else if (e.getAction() == Action.)
    }

}
