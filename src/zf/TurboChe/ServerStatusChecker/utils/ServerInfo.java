package zf.TurboChe.ServerStatusChecker.utils;

public class ServerInfo {
    private final String id;
    private final String host;
    private final int port;
    private final long timeout; // 超时时间（毫秒）
    private final long interval; // 检测间隔（秒）

    public ServerInfo(String id, String host, int port, long timeout, long interval) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        this.interval = interval;
    }

    // Getter 方法
    public String getId() { return id; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public long getTimeout() { return timeout; }
    public long getInterval() { return interval; }
}
