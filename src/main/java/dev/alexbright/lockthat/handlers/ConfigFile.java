package dev.alexbright.lockthat.handlers;

import dev.alexbright.lockthat.LockThat;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ConfigFile {

    private LockThat plugin = LockThat.getInstance();
    private File file;
    private FileConfiguration config;

    public ConfigFile(String fileName) {
        file = new File(plugin.getDataFolder(), fileName);
        createFile();
    }

    private void createFile() {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        config = YamlConfiguration.loadConfiguration(file);
        Bukkit.getLogger().log(Level.INFO, "[" + plugin.getName() + "] " + file.getName() + " initialized");
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
