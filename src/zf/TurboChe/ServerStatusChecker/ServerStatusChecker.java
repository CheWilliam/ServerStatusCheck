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

public class ServerStatusChecker extends JavaPlugin {
    private FileConfiguration config;
    private Map<String, ServerInfo> servers = new HashMap<>();
    private Map<String, ServerStatus> statusCache = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        // 加载服务器配置
        loadServers();

        // 注册命令
        getCommand("serverstatus").setExecutor((sender, command, label, args) -> {
            if (args.length > 0) {
                String serverId = args[0];
                if (servers.containsKey(serverId)) {
                    ServerStatus status = statusCache.getOrDefault(serverId, new ServerStatus(false, 0, System.currentTimeMillis()));
                    sender.sendMessage(ChatColor.GREEN + "服务器 " + serverId + " 状态: " +
                            (status.isOnline() ? ChatColor.GREEN + "在线" : ChatColor.RED + "离线"));
                } else {
                    sender.sendMessage(ChatColor.RED + "未知服务器: " + serverId);
                }
            } else {
                sender.sendMessage(ChatColor.GOLD + "=== 服务器状态 ===");
                servers.forEach((id, info) -> {
                    ServerStatus status = statusCache.getOrDefault(id, new ServerStatus(false, 0, System.currentTimeMillis()));
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

    private void loadServers() {
        servers.clear();
        if (config.contains("servers")) {
            for (String serverId : config.getConfigurationSection("servers").getKeys(false)) {
                String host = config.getString("servers." + serverId + ".host");
                int port = config.getInt("servers." + serverId + ".port", 25565);
                long checkInterval = config.getLong("servers." + serverId + ".check-interval", 30) * 20L;
                long timeout = config.getLong("servers." + serverId + ".timeout", 2000);

                servers.put(serverId, new ServerInfo(serverId, host, port, checkInterval, timeout));
                statusCache.put(serverId, new ServerStatus(false, 0, System.currentTimeMillis()));

                getLogger().info("已加载服务器配置: " + serverId + " (" + host + ":" + port + ")");
            }
        } else {
            // 添加默认服务器配置
            config.set("servers.vanilla.host", "127.0.0.1");
            config.set("servers.vanilla.port", 25566);
            config.set("servers.vanilla.check-interval", 30);
            config.set("servers.vanilla.timeout", 2000);

            config.set("servers.lobby.host", "127.0.0.1");
            config.set("servers.lobby.port", 25565);
            config.set("servers.lobby.check-interval", 30);
            config.set("servers.lobby.timeout", 2000);

            saveConfig();
            loadServers(); // 重新加载配置
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
        ServerStatus lastStatus = statusCache.get(server.getId());
        long currentTime = System.currentTimeMillis();

        // 检查是否需要更新状态（避免过于频繁的检测）
        if (currentTime - lastStatus.getLastCheckTime() < server.getCheckInterval() * 50) {
            return;
        }

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(server.getHost(), server.getPort()), (int) server.getTimeout());
            statusCache.put(server.getId(), new ServerStatus(true, 0, currentTime));
            getLogger().fine("服务器 " + server.getId() + " 在线");
        } catch (IOException e) {
            statusCache.put(server.getId(), new ServerStatus(false, 0, currentTime));
            getLogger().fine("服务器 " + server.getId() + " 离线: " + e.getMessage());
        }
    }

    public ServerStatus getServerStatus(String serverId) {
        return statusCache.getOrDefault(serverId, new ServerStatus(false, 0, System.currentTimeMillis()));
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