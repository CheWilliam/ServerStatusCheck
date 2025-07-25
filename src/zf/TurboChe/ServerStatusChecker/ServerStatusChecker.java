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
import zf.TurboChe.ServerStatusChecker.utils.ServerInfo;
import zf.TurboChe.ServerStatusChecker.utils.ServerStatus;

public class ServerStatusChecker extends JavaPlugin {
    private FileConfiguration config;
    private Map<String, ServerInfo> servers = new HashMap<>();
    private StatusCache statusCache;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        saveDefaultConfig();
        config = getConfig();

        // 加载服务器配置
        loadServers();

        statusCache = new StatusCache();

        // 注册命令
        getCommand("serverstatus").setExecutor((sender, command, label, args) -> {
            if (args.length > 0) {
                String serverId = args[0];
                if (servers.containsKey(serverId)) {
                    ServerStatus status = statusCache.getStatus(serverId);
                    sender.sendMessage(ChatColor.GREEN + "服务器 " + serverId + " 状态: " +
                            (status.isOnline() ? ChatColor.GREEN + "在线" : ChatColor.RED + "离线"));
                } else {
                    sender.sendMessage(ChatColor.RED + "未知服务器: " + serverId);
                }
            } else {
                sender.sendMessage(ChatColor.GOLD + "=== 服务器状态 ===");
                servers.forEach((id, info) -> {
                    ServerStatus status = statusCache.getStatus(id);
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

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public StatusCache getStatusCache() {
        return statusCache;
    }

    private void loadServers() {
        servers.clear();
        servers = configManager.getServers();

        for (ServerInfo server : servers.values()) {
            statusCache.updateStatus(server.getId(), false, 0);
            getLogger().info("已加载服务器配置: " + server.getId() + " (" + server.getHost() + ":" + server.getPort() + ")");
        }
    }

    private void startStatusCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (ServerInfo server : servers.values()) {
                    checkServerStatus(server);
                }
            }
        }.runTaskTimerAsynchronously(this, 0L, 20L); // 每秒检查一次
    }

    private void checkServerStatus(ServerInfo server) {
        ServerStatus lastStatus = statusCache.getStatus(server.getId());
        long currentTime = System.currentTimeMillis();

        // 检查是否需要更新状态（避免过于频繁的检测）
        if (currentTime - lastStatus.getLastCheckTime() < server.getInterval() * 1000) {
            return;
        }

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(server.getHost(), server.getPort()), (int) server.getTimeout());
            statusCache.updateStatus(server.getId(), true, 0);
            getLogger().fine("服务器 " + server.getId() + " 在线");
        } catch (IOException e) {
            statusCache.updateStatus(server.getId(), false, 0);
            getLogger().fine("服务器 " + server.getId() + " 离线: " + e.getMessage());
        }
    }

    public ServerStatus getServerStatus(String serverId) {
        return statusCache.getStatus(serverId);
    }
}
