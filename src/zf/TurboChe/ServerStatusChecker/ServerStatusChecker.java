package zf.TurboChe.ServerStatusChecker;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import zf.TurboChe.ServerStatusChecker.utils.ConfigManager;

public class ServerStatusChecker extends JavaPlugin {
    private FileConfiguration config;
    private Map<String, ServerInfo> servers = new HashMap<>();
    private Map<String, ServerStatus> statusCache = new HashMap<>();
    private ConfigManager configManager;
    private StatusCache statusCacheObj;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        saveDefaultConfig();
        config = getConfig();

        // 加载服务器配置
        loadServers();

        statusCacheObj = new StatusCache();

        // 注册命令
        getCommand("serverstatus").setExecutor((sender, command, label, args) -> {
            if (args.length > 0) {
                String serverId = args[0];
                if (servers.containsKey(serverId)) {
                    ServerStatus status = statusCacheObj.getStatus(serverId);
                    sender.sendMessage(ChatColor.GREEN + "服务器 " + serverId + " 状态: " +
                            (status.isOnline() ? ChatColor.GREEN + "在线" : ChatColor.RED + "离线"));
                } else {
                    sender.sendMessage(ChatColor.RED + "未知服务器: " + serverId);
                }
            } else {
                sender.sendMessage(ChatColor.GOLD + "=== 服务器状态 ===");
                servers.forEach((id, info) -> {
                    ServerStatus status = statusCacheObj.getStatus(id);
                    sender.sendMessage(ChatColor.GRAY + "- " + id + ": " +
                            (status.isOnline() ? ChatColor.GREEN + "在线" : ChatColor.RED + "离线"));
                });
            }
            return true;
        });

        // 注册占位符
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new StatusExpansion(this).register();
            getLogger().info("已成功注册 PlaceholderAPI 扩展");
        } else {
            getLogger().warning("未找到 PlaceholderAPI，占位符功能将不可用");
        }

        // 启动定时检测任务
        startStatusCheckTask();

        getLogger().info("ServerStatusChecker 已启用，共加载 " + servers.size() + " 个服务器");
    }

    // 添加 getConfigManager 方法
    public ConfigManager getConfigManager() {
        return configManager;
    }

    public StatusCache getStatusCache() {
        return statusCacheObj;
    }

    private void loadServers() {
        servers.clear();
        servers = configManager.getServers();

        for (ServerInfo server : servers.values()) {
            statusCacheObj.updateStatus(server.getId(), false, 0);
            getLogger().info("已加载服务器配置: " + server.getId() + " (" + server.getHost() + ":" + server.getPort() + ")");
        }
    }

    private void startStatusCheckTask() {
        new StatusCheckerTask(this).startTask();
    }

    public static class ServerInfo {
        private final String id;
        private final String host;
        private final int port;
        private final long checkInterval; // 检测间隔（秒）
        private final long timeout; // 超时时间（毫秒）

        public ServerInfo(String id, String host, int port, long checkInterval, long timeout) {
            this.id = id;
            this.host = host;
            this.port = port;
            this.checkInterval = checkInterval;
            this.timeout = timeout;
        }

        public String getId() {
            return id;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public long getCheckInterval() {
            return checkInterval;
        }

        public long getTimeout() {
            return timeout;
        }
    }

    public static class ServerStatus {
        private final boolean online;
        private final int playerCount;
        private final long lastCheckTime;

        public ServerStatus(boolean online, int playerCount, long lastCheckTime) {
            this.online = online;
            this.playerCount = playerCount;
            this.lastCheckTime = lastCheckTime;
        }

        public boolean isOnline() {
            return online;
        }

        public int getPlayerCount() {
            return playerCount;
        }

        public long getLastCheckTime() {
            return lastCheckTime;
        }

        public String getStatusText() {
            return online ? ChatColor.GREEN + "在线" : ChatColor.RED + "离线";
        }
    }
}
