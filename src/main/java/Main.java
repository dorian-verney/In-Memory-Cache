import Server.CacheServer;
import Server.ClientStrategy.NIOSelectorServer;
import Server.ClientHandler;
import Server.ClientStrategy.ThreadPerConnectionServer;
import org.apache.commons.cli.*;

void main(String[] args) throws InterruptedException, IOException, ParseException {
    int port = 6379;
    CacheServer server;
    ClientHandler clientHandler;
    try (var scanner = new Scanner(System.in)) {
        IO.println("Server ? (y/n)");
        if (scanner.nextLine().equalsIgnoreCase("y")){
            new ThreadPerConnectionServer().start(port);

        } else {
            new ClientHandler(scanner).start(port);
        }
    }
}