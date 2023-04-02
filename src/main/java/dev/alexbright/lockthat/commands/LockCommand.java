package dev.alexbright.lockthat.commands;

import dev.alexbright.lockthat.LockThat;
import dev.alexbright.lockthat.handlers.LockHandler;
import dev.alexbright.lockthat.handlers.LockHandler.RequestType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class LockCommand implements CommandExecutor {

    private final HashMap<UUID, LockHandler.RequestType> pendingLocks = LockHandler.pendingLocks;
    private final HashMap<UUID, Player> pendingOwner = LockHandler.pendingOwner;
    private final HashMap<UUID, Player> pendingUser = LockHandler.pendingUser;
    private final List<UUID> pendingChecks = LockHandler.pendingChecks;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(LockThat.prefix + "Only players can execute commands.");
            return false;
        }

        Player p = (Player) sender;

        if (args.length == 0) {
            p.sendMessage(LockThat.prefix + ChatColor.YELLOW + ChatColor.BOLD + "Help menu for LockThat:");
            p.sendMessage(LockThat.prefix + ChatColor.YELLOW + ChatColor.ITALIC + "/lock set" + ChatColor.RESET
                    + ChatColor.WHITE + " - Set a new lock");
            p.sendMessage(LockThat.prefix + ChatColor.YELLOW + ChatColor.ITALIC + "/lock addowner <name>" + ChatColor.RESET
                    + ChatColor.WHITE + " - Add another owner to your lock");
            p.sendMessage(LockThat.prefix + ChatColor.YELLOW + ChatColor.ITALIC + "/lock adduser <name>" + ChatColor.RESET
                    + ChatColor.WHITE + " - Grant use to another player");
            p.sendMessage(LockThat.prefix + ChatColor.YELLOW + ChatColor.ITALIC  + "/lock remove" + ChatColor.RESET
                    + ChatColor.WHITE + " - Remove a lock");
            p.sendMessage(LockThat.prefix + ChatColor.YELLOW + ChatColor.ITALIC  + "/lock check" + ChatColor.RESET
                    + ChatColor.WHITE + " - Check for lock eligibility");
            p.sendMessage(LockThat.prefix + ChatColor.YELLOW + ChatColor.ITALIC + "/lock cancel" + ChatColor.RESET
                    + ChatColor.WHITE + " - Cancel pending requests");
            p.sendMessage(LockThat.prefix + ChatColor.GRAY + "Developed with " + ChatColor.LIGHT_PURPLE + "❤ " + ChatColor.GRAY + "by AlexTurbo");
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("set")) {
                if (LockHandler.hasPending(p)) {
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "Error: You already have a pending request...");
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "To cancel, use " + ChatColor.ITALIC + "/lock cancel");
                    return false;
                }
                //p.sendMessage(LockThat.prefix + ChatColor.YELLOW + "Lock request initiated!");
                p.sendMessage(LockThat.prefix + ChatColor.YELLOW + "Please punch the block you want to lock...");
                p.sendMessage(LockThat.prefix + ChatColor.GOLD + "To cancel, use " + ChatColor.ITALIC + "/lock cancel");
                pendingLocks.put(p.getUniqueId(), RequestType.LOCK);
            } else if (args[0].equalsIgnoreCase("addowner")) {
                p.sendMessage(LockThat.prefix + ChatColor.RED + "Please specify a player name");
                p.sendMessage(LockThat.prefix + ChatColor.RED + "Usage: /lock addowner <name>");
            } else if (args[0].equalsIgnoreCase("adduser")) {
                p.sendMessage(LockThat.prefix + ChatColor.RED + "Please specify a player name");
                p.sendMessage(LockThat.prefix + ChatColor.RED + "Usage: /lock adduser <name>");
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (LockHandler.hasPending(p)) {
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "You already have a pending request...");
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "To cancel, use " + ChatColor.ITALIC + "/lock cancel");
                    return false;
                }
                p.sendMessage(LockThat.prefix + ChatColor.YELLOW + "Please punch the block you want to unlock...");
                p.sendMessage(LockThat.prefix + ChatColor.GOLD + "To cancel, use " + ChatColor.ITALIC + "/lock cancel");
                pendingLocks.put(p.getUniqueId(), RequestType.UNLOCK);
            } else if (args[0].equalsIgnoreCase("check")) {
                if (LockHandler.hasPending(p)) {
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "You are either in check mode or have an open request");
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "To cancel, use " + ChatColor.ITALIC + "/lock cancel");
                    return false;
                }
                p.sendMessage(LockThat.prefix + ChatColor.YELLOW + ChatColor.BOLD + "You are now in lock check mode");
                p.sendMessage(LockThat.prefix + ChatColor.YELLOW + "Punch a block to check its lock status and eligibility");
                p.sendMessage(LockThat.prefix + ChatColor.GOLD + "Use " + ChatColor.ITALIC + "/lock cancel"
                        + ChatColor.RESET + ChatColor.GOLD + " to exit check mode.");
                pendingChecks.add(p.getUniqueId());
            } else if (args[0].equalsIgnoreCase("cancel")) {
                if (!LockHandler.hasPending(p)) {
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "No pending requests found");
                    return true;
                }
                LockHandler.cancelPending(p);
                p.sendMessage(LockThat.prefix + ChatColor.YELLOW + "Pending requests cancelled");
            } else {
                p.sendMessage(LockThat.prefix + ChatColor.RED + "Command not recognized");
                return false;
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("addowner")) {
                if (LockHandler.hasPending(p)) {
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "You already have a pending request...");
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "To cancel, use " + ChatColor.ITALIC + "/lock cancel");
                    return false;
                }
                if (args[1].equalsIgnoreCase(p.getName())) {
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "Please specify another player");
                    return false;
                }
                Player reqPlayer = Bukkit.getPlayer(args[1]);
                if (reqPlayer == null) {
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "Error: Player is either offline or does not exist");
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "You may only add online players");
                    return false;
                }
                p.sendMessage(LockThat.prefix + ChatColor.DARK_RED + ChatColor.BOLD + "WARNING: " + ChatColor.RED + "This action grants full lock control forever");
                p.sendMessage(LockThat.prefix + ChatColor.YELLOW + "To confirm request, punch the locked block");
                p.sendMessage(LockThat.prefix + ChatColor.GOLD + "To cancel, use " + ChatColor.ITALIC + "/lock cancel");
                pendingOwner.put(p.getUniqueId(), reqPlayer);
            } else if (args[0].equalsIgnoreCase("adduser")) {
                if (LockHandler.hasPending(p)) {
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "You already have a pending request...");
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "To cancel, use " + ChatColor.ITALIC + "/lock cancel");
                    return false;
                }
                Player reqPlayer = Bukkit.getPlayer(args[1]);
                if (reqPlayer == null) {
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "Error: Player is either offline or does not exist");
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "You may only add online players");
                    return false;
                }
                p.sendMessage(LockThat.prefix + ChatColor.DARK_RED + ChatColor.BOLD + "WARNING: " + ChatColor.RED + "This will grant lock access forever");
                p.sendMessage(LockThat.prefix + ChatColor.YELLOW + "To confirm request, punch the locked block");
                p.sendMessage(LockThat.prefix + ChatColor.GOLD + "To cancel, use " + ChatColor.ITALIC + "/lock cancel");
                pendingUser.put(p.getUniqueId(), reqPlayer);
            } else {
                p.sendMessage(LockThat.prefix + ChatColor.RED + "Command not recognized");
                return false;
            }
        } else {
            p.sendMessage(LockThat.prefix + ChatColor.RED + "Invalid command");
            return false;
        }

        return true;
    }

}
