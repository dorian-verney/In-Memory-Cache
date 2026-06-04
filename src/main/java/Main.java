import distrib.ClusterRouter;
import distrib.Node;
import io.ClientIO;
import io.HumanIO;
import server.CacheServer;
import server.ClientHandler;
import org.apache.commons.cli.*;

void main(String[] args) throws InterruptedException, IOException, ParseException {
    int port = 6378;
    int poolSize = 10;
    CacheServer server;
    ClientHandler clientHandler;
    try (var scanner = new Scanner(System.in)) {
        IO.println("Server ? (y/n)");
        if (scanner.nextLine().equalsIgnoreCase("y")){
            // on lance 2 nodes

            var router = new ClusterRouter(2, poolSize);

            var nodeA = new Node("nodeA", "127.0.0.1", 6379, poolSize);

            var nodeB = new Node("nodeB", "127.0.0.1", 6380, poolSize);

            new Thread(() -> {
                try { nodeA.start(); }
                catch (IOException e) { e.printStackTrace(); }
            }).start();

            new Thread(() -> {
                try { nodeB.start(); }
                catch (IOException e) { e.printStackTrace(); }
            }).start();

            Thread.sleep(500); // laisse le temps aux serveurs de démarrer
            router.addNode(nodeA);
            router.addNode(nodeB);

            router.start(port);

        } else {
            ClientIO io = new HumanIO(scanner);
            new ClientHandler(io).start(port);
        }
    }
}
// mono server
/*
void main(String[] args) throws InterruptedException, IOException, ParseException {
    int port = 6379;
    CacheServer server;
    ClientHandler clientHandler;
    try (var scanner = new Scanner(System.in)) {
        IO.println("Server ? (y/n)");
        if (scanner.nextLine().equalsIgnoreCase("y")){
            new ThreadPerConnectionServer(port).start();

        } else {
            ClientIO io = new HumanIO(scanner);
            new ClientHandler(io).start(port);
        }
    }
}*/
