package distrib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClusterRouter {

    private final Map<Socket, String> clients;

    private final ConsistentHashRing ring;

    private final int poolSize;
    private final Map<Node, NodeConnectionPool> connections; // une connexion pool par noeud

    private ServerSocket serverSocket;

    public ClusterRouter(int virtualNodes, int poolSize) {
        this.clients     = new HashMap<>();
        this.poolSize    = poolSize;
        this.ring        = new ConsistentHashRing(virtualNodes);
        this.connections = new HashMap<>();
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        while (true) {
            Socket client = serverSocket.accept();
            new Thread(() -> {
                try {
                    handleClient(client);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

        }
    }

    // TODO marche que pour cmd key val
    private void handleClient(Socket client) throws InterruptedException {
        IO.println(client.getPort() + " " + client.getLocalPort());
        // save client
        this.clients.put(client, client.getInetAddress().getHostAddress() + ":" + client.getPort());


        try (var clientIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
             var writer = new PrintWriter(client.getOutputStream(), true)) {

            // recup key user:42 from : GET user:42 Dorian
            while (true) {
                var in = clientIn.readLine();
                if (in == null || in.equals("QUIT")) break;
                IO.println("in = "+ in);
                String[] cmd = in.split(" ");
                String res = " ";
                if (cmd.length > 1) {
                    var key = cmd[1];

                    // get a connection from pool
                    var node = ring.getNode(key);
                    var nodeCoPool = connections.get(node);
                    IO.println(node);

                    // can wait here
                    var nodeConnection = nodeCoPool.borrow();
                    res = nodeConnection.sendAndReceive(in);
                    nodeCoPool.release(nodeConnection);

                }
                IO.println("res = " + res);
                writer.println(res);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addNode(Node node) throws IOException {
        ring.addNode(node);
        connections.put(node, new NodeConnectionPool(node, poolSize));
    }

    public void removeNode(Node node) throws IOException {
        NodeConnectionPool n = connections.get(node);
        if (n == null) return;

        n.deconnect();
        connections.remove(node);
        ring.removeNode(node);
    }
}