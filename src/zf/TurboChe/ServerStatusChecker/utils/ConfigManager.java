package zf.TurboChe.ServerStatusChecker.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final JavaPlugin plugin;
    private File configFile;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, ServerInfo> getServers() {
        Map<String, ServerInfo> servers = new HashMap<>();
        
        if (config.contains("servers")) {
            for (String serverId : config.getConfigurationSection("servers").getKeys(false)) {
                String host = config.getString("servers." + serverId + ".host");
                int port = config.getInt("servers." + serverId + ".port", 25565);
                long interval = config.getLong("servers." + serverId + ".interval", 5);
                long timeout = config.getLong("servers." + serverId + ".timeout", 2000);
                
                servers.put(serverId, new ServerInfo(serverId, host, port, timeout, interval));
            }
        }
        
        return servers;
    }
}
