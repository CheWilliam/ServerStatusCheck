package zf.TurboChe.ServerStatusChecker;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import zf.TurboChe.ServerStatusChecker.utils.ServerStatus;

public class StatusExpansion extends PlaceholderExpansion {
    private final ServerStatusChecker plugin;

    public StatusExpansion(ServerStatusChecker plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "serverstatus";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    // 移除 persist() 方法（1.7.10的PlaceholderAPI不支持）

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (params == null || params.isEmpty()) {
            return null;
        }

        // 格式: %serverstatus_<serverId>_<type>%
        String[] parts = params.split("_");
        if (parts.length < 2) {
            return "§c格式错误";
        }

        String serverId = parts[0];
        String type = parts[1].toLowerCase();
        ServerStatus status = plugin.getStatusCache().getStatus(serverId);

        switch (type) {
            case "status":
                return status.getStatusText();
            case "online":
                return status.isOnline() ? "true" : "false";
            case "players":
                return String.valueOf(status.getPlayerCount());
            default:
                return "§c未知类型";
        }
    }
}
