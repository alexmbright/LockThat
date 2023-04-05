package dev.alexbright.lockthat.commands;

import dev.alexbright.lockthat.LockThat;
import dev.alexbright.lockthat.handlers.LockHandler;
import dev.alexbright.lockthat.handlers.LockHandler.RequestType;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class LockCommand implements CommandExecutor {

    private final HashMap<UUID, RequestType> pendingLocks = LockHandler.pendingLocks;
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
            p.sendMessage(LockThat.prefix + ChatColor.YELLOW + "/lock set" + ChatColor.WHITE + ": set a new lock");
            p.sendMessage(LockThat.prefix + ChatColor.YELLOW + "/lock addowner <name>" + ChatColor.WHITE + ": add another owner to your lock");
            p.sendMessage(LockThat.prefix + ChatColor.YELLOW + "/lock adduser <name>" + ChatColor.WHITE + ": grant use to another player");
            p.sendMessage(LockThat.prefix + ChatColor.YELLOW  + "/lock remove" + ChatColor.WHITE + ": remove a lock");
            p.sendMessage(LockThat.prefix + ChatColor.YELLOW  + "/lock check" + ChatColor.WHITE + ": check for lock eligibility");
            p.sendMessage(LockThat.prefix + ChatColor.YELLOW + "/lock cancel" + ChatColor.WHITE + ": cancel pending requests");
            p.sendMessage(LockThat.prefix + ChatColor.GRAY + "Developed with " + ChatColor.LIGHT_PURPLE + "❤ " + ChatColor.GRAY + "by AlexTurbo");
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("set")) {
                if (LockHandler.hasPending(p)) {
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "Error: You already have a pending request...");
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "To cancel, use " + ChatColor.ITALIC + "/lock cancel");
                    return false;
                }
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
                if (pendingChecks.contains(p.getUniqueId())) {
                    pendingChecks.remove(p.getUniqueId());
                    LockHandler.lastChecked.remove(p.getUniqueId());
                    p.sendMessage(LockThat.prefix + ChatColor.YELLOW + "You are no longer in lock check mode");
                    return true;
                }
                if (LockHandler.hasPending(p)) {
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "You have a pending request...");
                    p.sendMessage(LockThat.prefix + ChatColor.RED + "To cancel, use " + ChatColor.ITALIC + "/lock cancel");
                    return false;
                }

                // alert the player of lock check mode in action bar until turned off
                Bukkit.getScheduler().runTaskTimer(LockThat.getInstance(), task -> {
                    if (!pendingChecks.contains(p.getUniqueId())) {
                        task.cancel();
                        return;
                    }
                    if (LockHandler.lastChecked.containsKey(p.getUniqueId())) {
                        Date date = LockHandler.lastChecked.get(p.getUniqueId());
                        if (Math.abs(new Date().getTime() - date.getTime()) < 2000) return;
                    }
                    LockHandler.lastChecked.remove(p.getUniqueId());
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.YELLOW + "You are currently in lock check mode"));
                }, 0L, 10L);

                p.sendMessage(LockThat.prefix + ChatColor.YELLOW + "Punch a block to check its lock status and eligibility");
                p.sendMessage(LockThat.prefix + ChatColor.GOLD + "Use " + ChatColor.ITALIC + "/lock check"
                        + ChatColor.GOLD + " to exit");
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
                p.sendMessage(LockThat.prefix + ChatColor.DARK_RED + ChatColor.BOLD + "WARNING: " + ChatColor.RED + "This grants full lock control forever!");
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
                p.sendMessage(LockThat.prefix + ChatColor.DARK_RED + ChatColor.BOLD + "WARNING: " + ChatColor.RED + "This will grant lock access forever!");
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
