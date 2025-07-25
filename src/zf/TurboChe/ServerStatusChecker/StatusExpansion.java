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

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (params == null || params.isEmpty()) {
            return null;
        }

        String[] parts = params.split("_");
        if (parts.length < 2) {
            return "§c格式错误";
        }

        String serverId = parts[0];
        String type = parts[1].toLowerCase();
        ServerStatus status = plugin.getStatusCache().getStatus(serverId);

        switch (type) {
            case "status":
                return status.isOnline() ? "§a在线" : "§c离线";
            case "online":
                return String.valueOf(status.isOnline());
            case "players":
                return String.valueOf(status.getPlayerCount());
            default:
                return "§c未知类型";
        }
    }
}
