package zf.TurboChe.ServerStatusChecker.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import zf.TurboChe.ServerStatusChecker.ServerStatusChecker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final ServerStatusChecker plugin;
    private File configFile;
    private FileConfiguration config;

    public ConfigManager(ServerStatusChecker plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
    }

    // 加载配置文件（如果不存在则生成默认配置）
    public void loadConfig() {
        // 创建插件数据文件夹（如果不存在）
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // 如果配置文件不存在，复制默认配置
        if (!configFile.exists()) {
            try (InputStream in = plugin.getResource("config.yml")) {
                if (in != null) {
                    Files.copy(in, configFile.toPath()); // 复制内置默认配置
                    plugin.getLogger().info("已生成默认配置文件：config.yml");
                } else {
                    configFile.createNewFile(); // 创建空文件
                    plugin.getLogger().warning("未找到默认配置，创建空配置文件！");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("生成配置文件失败：" + e.getMessage());
            }
        }

        // 加载配置文件
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    // 保存配置文件
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("保存配置文件失败：" + e.getMessage());
        }
    }

    // 获取配置文件对象
    public FileConfiguration getConfig() {
        return config;
    }

    // 读取服务器配置列表（从config.yml中）
    public Map<String, ServerInfo> getServers() {
        Map<String, ServerInfo> servers = new HashMap<>();

        // 读取配置中的 "servers" 节点
        if (config.contains("servers")) {
            for (String serverId : config.getConfigurationSection("servers").getKeys(false)) {
                String path = "servers." + serverId + ".";
                String host = config.getString(path + "host", "127.0.0.1");
                int port = config.getInt(path + "port", 25565);
                long timeout = config.getLong(path + "timeout", 2000); // 超时时间（毫秒）
                long interval = config.getLong(path + "interval", 5); // 检测间隔（秒）

                servers.put(serverId, new ServerInfo(serverId, host, port, timeout, interval));
            }
        } else {
            // 如果配置中没有服务器，添加默认服务器（防止空列表）
            servers.put("vanilla", new ServerInfo("vanilla", "127.0.0.1", 25566, 2000, 5));
            servers.put("lobby", new ServerInfo("lobby", "127.0.0.1", 25565, 2000, 5));
            saveDefaultServers(servers); // 保存到配置文件
        }

        return servers;
    }

    // 保存默认服务器配置到文件
    private void saveDefaultServers(Map<String, ServerInfo> servers) {
        for (ServerInfo server : servers.values()) {
            String path = "servers." + server.getId() + ".";
            config.set(path + "host", server.getHost());
            config.set(path + "port", server.getPort());
            config.set(path + "timeout", server.getTimeout());
            config.set(path + "interval", server.getInterval());
        }
        saveConfig();
        plugin.getLogger().info("已添加默认服务器配置到 config.yml");
    }
}
