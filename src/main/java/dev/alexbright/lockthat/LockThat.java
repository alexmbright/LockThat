package dev.alexbright.lockthat;

import dev.alexbright.lockthat.handlers.ConfigFile;
import dev.alexbright.lockthat.listeners.BlockListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class LockThat extends JavaPlugin {

    private static LockThat instance;
    private ConfigFile data;
    public static String prefix = ChatColor.GRAY + "[LockThat] " + ChatColor.WHITE;

    @Override
    public void onEnable() {
        instance = this;
        data = new ConfigFile("chests.yml");
        if (!data.getConfig().contains("chests")) {
            data.getConfig().createSection("chests");
            data.save();
        }
        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
    }

    @Override
    public void onDisable() {
        // do nothing
    }

    public static LockThat getInstance() {
        return instance;
    }

    public ConfigFile getData() {
        return data;
    }
}
