package zf.TurboChe.ServerStatusChecker.utils;

import org.bukkit.ChatColor;

public class ServerStatus {
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
