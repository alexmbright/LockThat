package dev.alexbright.lockthat.listeners;

import dev.alexbright.lockthat.LockThat;
import dev.alexbright.lockthat.handlers.LockHandler;
import dev.alexbright.lockthat.handlers.LockHandler.RequestType;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final HashMap<UUID, RequestType> pendingLocks = LockHandler.pendingLocks;
    private final HashMap<UUID, Player> pendingOwner = LockHandler.pendingOwner;
    private final HashMap<UUID, Player> pendingUser = LockHandler.pendingUser;
    private final List<UUID> pendingChecks = LockHandler.pendingChecks;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        // if player left-clicked
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block b = LockHandler.findRootBlock(e.getClickedBlock());

            // if player has a pending lock request
            if (pendingLocks.containsKey(p.getUniqueId())) {
                RequestType req = pendingLocks.remove(p.getUniqueId());

                // if block is not lockable state, cancel request
                if (!(b.getState() instanceof Container || b.getBlockData() instanceof Openable)) {
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "This block is not lockable!");
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "Request cancelled");
                    return;
                }

                // if pending request is to create a new lock
                if (req == RequestType.LOCK) {

                    // check if lock already exists
                    if (LockHandler.contains(b)) {
                        p.sendMessage(LockThat.prefix + ChatColor.RED + ChatColor.BOLD + "This is already locked!");
                        p.sendMessage(LockThat.prefix + ChatColor.RED + "Request cancelled");
                        return;
                    }

                    // check if lock addition was successful and cancel if not
                    if (!LockHandler.add(b, p)) {
                        p.sendMessage(LockThat.prefix + ChatColor.RED + "Unknown error occurred... please try again");
                        return;
                    }

                    // success message
                    p.sendMessage(LockThat.prefix + ChatColor.GREEN + ChatColor.BOLD + "Success! " + ChatColor.RESET
                            + ChatColor.YELLOW + "Lock placed at (" + LockHandler.getLocationString(b.getLocation()) + ")");


                    // if pending request is to unlock
                } else if (req == RequestType.UNLOCK) {

                    // check if lock exists and player is lock owner
                    if (LockHandler.contains(b)) {
                        if (!LockHandler.isOwner(p, b)) {
                            p.sendMessage(LockThat.prefix + ChatColor.RED + "You do not own this lock!");
                            p.sendMessage(LockThat.prefix + ChatColor.RED + "Request cancelled");
                            return;
                        }

                        // check if lock removal was successful and cancel if not
                        if (!LockHandler.remove(b)) {
                            p.sendMessage(LockThat.prefix + ChatColor.RED + "Unknown error occurred... please try again");
                            return;
                        }

                        // success message
                        p.sendMessage(LockThat.prefix + ChatColor.GREEN + ChatColor.BOLD + "Success! " + ChatColor.RESET
                                + ChatColor.YELLOW + "Removed lock at (" + LockHandler.getLocationString(b.getLocation()) + ")");

                        // if lock doesn't exist
                    } else {
                        p.sendMessage(LockThat.prefix + ChatColor.RED + "No lock exists here");
                        p.sendMessage(LockThat.prefix + ChatColor.RED + "Request cancelled");
                    }
                }

                // if pending request is to add owner
            } else if (pendingOwner.containsKey(p.getUniqueId())) {
                Player other = pendingOwner.remove(p.getUniqueId());

                // check if lock exists and player is lock owner
                if (LockHandler.contains(b)) {
                    if (!LockHandler.isOwner(p, b)) {
                        p.sendMessage(LockThat.prefix + ChatColor.RED + "You do not own this lock!");
                        p.sendMessage(LockThat.prefix + ChatColor.RED + "Request cancelled");
                        return;
                    }

                    if (LockHandler.isOwner(other, b)) {
                        p.sendMessage(LockThat.prefix + ChatColor.RED + ChatColor.BOLD + other.getName() + ChatColor.RESET
                                + ChatColor.RED + " is already an owner of this lock!");
                        p.sendMessage(LockThat.prefix + ChatColor.RED + "Request cancelled");
                        return;
                    }

                    if (!LockHandler.addOwner(b, p)) {
                        p.sendMessage(LockThat.prefix + ChatColor.RED + "Unknown error occurred... please try again");
                        return;
                    }

                    p.sendMessage(LockThat.prefix + ChatColor.GREEN + "Success! " + ChatColor.YELLOW + ChatColor.BOLD
                            + ChatColor.YELLOW + other.getName() + " has been added as an owner to this lock");
                } else {
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "No lock exists here");
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "Request cancelled");
                }

                // if pending request is to add user
            } else if (pendingUser.containsKey(p.getUniqueId())) {
                Player other = pendingUser.remove(p.getUniqueId());

                // check if lock exists and player is lock owner
                if (LockHandler.contains(b)) {
                    if (!LockHandler.isOwner(p, b)) {
                        p.sendMessage(LockThat.prefix + ChatColor.RED + "You do not own this lock!");
                        p.sendMessage(LockThat.prefix + ChatColor.RED + "Request cancelled");
                        return;
                    }

                    if (LockHandler.hasAccess(other, b)) {
                        p.sendMessage(LockThat.prefix + ChatColor.RED + ChatColor.BOLD + other.getName() + ChatColor.RESET
                                + ChatColor.RED + " already has access to this lock!");
                        p.sendMessage(LockThat.prefix + ChatColor.RED + "Request cancelled");
                        return;
                    }

                    if (!LockHandler.addUser(b, p)) {
                        p.sendMessage(LockThat.prefix + ChatColor.RED + "Unknown error occurred... please try again");
                        return;
                    }

                    p.sendMessage(LockThat.prefix + ChatColor.GREEN + "Success! " + ChatColor.YELLOW + ChatColor.BOLD
                            + ChatColor.YELLOW + other.getName() + " has been given access to this lock");
                } else {
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "No lock exists here");
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "Request cancelled");
                }
            } else if (pendingChecks.contains(p.getUniqueId())) {

                p.sendMessage("Block is type: " + b.getType().name());
                // check if lock exists
                if (LockHandler.contains(b)) {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.YELLOW + "Locked by " + ChatColor.ITALIC + LockHandler.getMainOwner(b).getName()));
                } else {
                    if (b.getState() instanceof Container || b.getBlockData() instanceof Openable)
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "Eligible to be locked"));
                    else
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Not eligible to be locked"));
                }

            }

        // if player right-clicked
        } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block b = LockHandler.findRootBlock(e.getClickedBlock());
            if (LockHandler.contains(b)) {
                if (!LockHandler.hasAccess(p, b)) {
                    e.setCancelled(true);
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "This is locked by " + ChatColor.ITALIC + LockHandler.getMainOwner(b).getName()));
                }
            }
        }
    }

    // remove player from pending lists if they leave
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        LockHandler.cancelPending(e.getPlayer());
    }

    // remove player from pending lists if they get kicked
    @EventHandler
    public void onPlayerKick(PlayerKickEvent e) {
        LockHandler.cancelPending(e.getPlayer());
    }

}
