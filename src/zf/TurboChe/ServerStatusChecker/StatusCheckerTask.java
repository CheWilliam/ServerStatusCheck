package zf.TurboChe.ServerStatusChecker;

import org.bukkit.scheduler.BukkitRunnable;
import zf.TurboChe.ServerStatusChecker.utils.ServerInfo;
import zf.TurboChe.ServerStatusChecker.utils.ConfigManager;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

public class StatusCheckerTask {
    private final ServerStatusChecker plugin;

    public StatusCheckerTask(ServerStatusChecker plugin) {
        this.plugin = plugin;
    }

    // 启动定时检测任务
    public void startTask() {
        // 每20 ticks（1秒）检查一次是否需要更新状态
        new BukkitRunnable() {
            @Override
            public void run() {
                ConfigManager configManager = plugin.getConfigManager();
                Map<String, ServerInfo> servers = configManager.getServers();

                // 遍历所有服务器，异步检测状态
                for (ServerInfo server : servers.values()) {
                    checkServerAsync(server);
                }
            }
        }.runTaskTimer(plugin, 0, 20); // 立即执行，每1秒重复一次
    }

    // 异步检测服务器状态（避免阻塞主线程）
    private void checkServerAsync(ServerInfo server) {
        new BukkitRunnable() {
            @Override
            public void run() {
                boolean isOnline = false;
                int playerCount = 0;

                // 尝试连接服务器
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(server.getHost(), server.getPort()), (int) server.getTimeout());
                    isOnline = true;
                    // TODO: 这里可以添加玩家数检测逻辑
                } catch (Exception e) {
                    isOnline = false;
                }

                // 创建 final 变量来保存值
                final boolean finalIsOnline = isOnline;
                final int finalPlayerCount = playerCount;

                // 更新缓存状态（回到主线程更新）
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        plugin.getStatusCache().updateStatus(server.getId(), finalIsOnline, finalPlayerCount);
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin); // 异步执行
    }
}
