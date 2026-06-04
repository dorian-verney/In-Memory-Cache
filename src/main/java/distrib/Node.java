package distrib;

import server.CacheServer;

import java.io.IOException;

public class Node {
    private final String id;
    private final String host;
    private int port;

    private final CacheServer server;

    public Node(String id, String host, int port, int poolSize) {
        this.id   = id;
        this.host = host;
        this.port = port;
        this.server = new CacheServer(host, port, poolSize);
    }

    public String getId(){
        return id;
    }

    public String getHost(){
        return host;
    }

    public int getPort(){
        return port;
    }

    public void start() throws IOException {
        server.start();
    }

    public String toString(){
        return id + " " + host + ":" + port;
    }
}