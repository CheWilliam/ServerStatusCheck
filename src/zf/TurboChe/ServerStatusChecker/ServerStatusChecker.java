package zf.TurboChe.ServerStatusChecker;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import zf.TurboChe.ServerStatusChecker.utils.ConfigManager;
import zf.TurboChe.ServerStatusChecker.utils.ServerInfo;
import zf.TurboChe.ServerStatusChecker.utils.ServerStatus;

import java.util.HashMap;
import java.util.Map;

public class ServerStatusChecker extends JavaPlugin implements CommandExecutor {
    private ConfigManager configManager;
    private Map<String, ServerInfo> servers = new HashMap<>();
    private StatusCache statusCache;

    @Override
    public void onEnable() {
        // 1. 首先初始化配置管理器
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        // 2. 然后加载服务器配置（确保 configManager 已初始化）
        loadServers();
        
        // 3. 初始化状态缓存
        statusCache = new StatusCache();
        
        // 4. 注册命令
        getCommand("serverstatus").setExecutor(this);
        
        // 5. 注册占位符
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new StatusExpansion(this).register();
            getLogger().info(ChatColor.GREEN + "已成功注册 PlaceholderAPI 扩展");
        } else {
            getLogger().warning(ChatColor.YELLOW + "未找到 PlaceholderAPI，占位符功能将不可用");
        }
        
        // 6. 启动状态检查任务
        new StatusCheckerTask(this).startTask();
        
        getLogger().info(ChatColor.GREEN + "ServerStatusChecker 已启用，共加载 " + servers.size() + " 个服务器");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
            if (servers != null && statusCache != null) {
                servers.forEach((id, info) -> {
                    ServerStatus status = statusCache.getStatus(id);
                    sender.sendMessage(ChatColor.GRAY + "- " + id + ": " + 
                            (status.isOnline() ? ChatColor.GREEN + "在线" : ChatColor.RED + "离线"));
                });
            } else {
                sender.sendMessage(ChatColor.RED + "服务器状态系统未初始化完成");
            }
        }
        return true;
    }

    private void loadServers() {
        servers.clear();
        // 确保 configManager 不为 null
        if (configManager != null) {
            servers = configManager.getServers();
            for (ServerInfo server : servers.values()) {
                // 确保 statusCache 不为 null
                if (statusCache != null) {
                    statusCache.updateStatus(server.getId(), false, 0);
                }
                getLogger().info(ChatColor.GREEN + "已加载服务器配置: " + server.getId() + " (" + server.getHost() + ":" + server.getPort() + ")");
            }
        } else {
            getLogger().severe("配置管理器未初始化，无法加载服务器配置！");
        }
    }

    public StatusCache getStatusCache() {
        return statusCache;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
