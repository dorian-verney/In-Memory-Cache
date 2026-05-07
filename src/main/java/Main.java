import Server.CacheServer;
import Server.ClientStrategy.NIOSelectorServer;
import Server.ClientHandler;

void main() throws InterruptedException, IOException
{
    int port = 6379;
    CacheServer server;
    ClientHandler clientHandler;
    try (var scanner = new Scanner(System.in)) {
        IO.println("Server ? (y/n)");
        if (scanner.nextLine().equalsIgnoreCase("y")){
            new NIOSelectorServer().start(port);

        } else {
            new ClientHandler(scanner).startNIO(port);
        }
    }
}