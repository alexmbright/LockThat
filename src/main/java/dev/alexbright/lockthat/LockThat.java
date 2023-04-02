package dev.alexbright.lockthat;

import dev.alexbright.lockthat.commands.LockCommand;
import dev.alexbright.lockthat.handlers.ConfigFile;
import dev.alexbright.lockthat.handlers.LockHandler;
import dev.alexbright.lockthat.listeners.BlockListener;
import dev.alexbright.lockthat.listeners.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;


public final class LockThat extends JavaPlugin {

    private static LockThat instance;
    private static ConfigFile data;
    public static String prefix = ChatColor.DARK_GRAY + "[LockThat] " + ChatColor.WHITE;

    @Override
    public void onEnable() {
        instance = this;
        getCommand("lock").setExecutor(new LockCommand());
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        data = new ConfigFile("data.yml");
        if (!data.getConfig().contains("locks")) {
            data.getConfig().createSection("locks");
            data.save();
        }
        LockHandler.setData(data);
        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
    }

    @Override
    public void onDisable() {
        // do nothing
    }

    public static LockThat getInstance() {
        return instance;
    }

    public static ConfigFile getData() {
        return data;
    }
}
