package dev.alexbright.lockthat.listeners;

import dev.alexbright.lockthat.handlers.LockHandler;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();
        if (e.getBlock() instanceof Chest) {
            if (LockHandler.contains(b)) {
                if (!LockHandler.isOwner(p, b)) {
                    e.setCancelled(true);
                    return;
                }

            }
        } else if (b.getBlockData() instanceof Openable) {

        }
    }

}
