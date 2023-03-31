package dev.alexbright.lockthat.handlers;

import dev.alexbright.lockthat.LockThat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

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
                if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        config = YamlConfiguration.loadConfiguration(file);
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
