package zf.TurboChe.ServerStatusChecker;

import zf.TurboChe.ServerStatusChecker.utils.ServerStatus;

import java.util.HashMap;
import java.util.Map;

public class StatusCache {
    private final Map<String, ServerStatus> cache = new HashMap<>();

    public ServerStatus getStatus(String serverId) {
        return cache.computeIfAbsent(serverId, 
                k -> new ServerStatus(false, 0, System.currentTimeMillis()));
    }

    public void updateStatus(String serverId, boolean isOnline, int playerCount) {
        cache.put(serverId, new ServerStatus(isOnline, playerCount, System.currentTimeMillis()));
    }
}
