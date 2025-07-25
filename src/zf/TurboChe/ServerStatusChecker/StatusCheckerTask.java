package zf.TurboChe.ServerStatusChecker;

import org.bukkit.scheduler.BukkitRunnable;
import zf.TurboChe.ServerStatusChecker.utils.ServerInfo;

import java.net.InetSocketAddress;
import java.net.Socket;

public class StatusCheckerTask {
    private final ServerStatusChecker plugin;

    public StatusCheckerTask(ServerStatusChecker plugin) {
        this.plugin = plugin;
    }

    public void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getConfigManager().getServers().forEach((id, server) -> checkServerAsync(server));
            }
        }.runTaskTimer(plugin, 0L, 20L); // 每秒执行一次
    }

    private void checkServerAsync(ServerInfo server) {
        new BukkitRunnable() {
            @Override
            public void run() {
                boolean isOnline = false;
                int playerCount = 0;

                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(server.getHost(), server.getPort()), (int) server.getTimeout());
                    isOnline = true;
                } catch (Exception e) {
                    isOnline = false;
                }

                final boolean finalIsOnline = isOnline;
                final int finalPlayerCount = playerCount;

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        plugin.getStatusCache().updateStatus(server.getId(), finalIsOnline, finalPlayerCount);
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }
}
