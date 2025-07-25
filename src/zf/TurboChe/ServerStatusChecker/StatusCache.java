package zf.TurboChe.ServerStatusChecker;

import zf.TurboChe.ServerStatusChecker.utils.ServerStatus;

import java.util.HashMap;
import java.util.Map;

public class StatusCache {
    private final Map<String, ServerStatus> statusMap = new HashMap<>();

    // 更新服务器状态
    public void updateStatus(String serverId, boolean isOnline, int playerCount) {
        statusMap.put(serverId, new ServerStatus(isOnline, playerCount, System.currentTimeMillis()));
    }

    // 获取服务器状态
    public ServerStatus getStatus(String serverId) {
        return statusMap.getOrDefault(serverId, new ServerStatus(false, 0, 0));
    }
}