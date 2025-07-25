package zf.TurboChe.ServerStatusChecker.utils;

public class ServerStatus {
    private final boolean isOnline;
    private final int playerCount;
    private final long lastCheckTime;

    public ServerStatus(boolean isOnline, int playerCount, long lastCheckTime) {
        this.isOnline = isOnline;
        this.playerCount = playerCount;
        this.lastCheckTime = lastCheckTime;
    }

    // 获取状态文本（在线/离线）
    public String getStatusText() {
        return isOnline ? "§a在线" : "§c离线";
    }

    // Getter 方法
    public boolean isOnline() { return isOnline; }
    public int getPlayerCount() { return playerCount; }
    public long getLastCheckTime() { return lastCheckTime; }
}