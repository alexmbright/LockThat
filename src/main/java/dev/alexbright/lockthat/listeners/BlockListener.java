package dev.alexbright.lockthat.listeners;

import dev.alexbright.lockthat.handlers.ChestHandler;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();
        if (e.getBlock().getType() == Material.CHEST) {
            if (ChestHandler.contains(b)) {
                if (!ChestHandler.isOwner(p, b)) {
                    e.setCancelled(true);
                    return;
                }

            }
        }
    }

}
