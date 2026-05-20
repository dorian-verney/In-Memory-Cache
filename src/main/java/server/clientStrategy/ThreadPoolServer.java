package server.clientStrategy;

import server.CacheServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;

public class ThreadPoolServer extends CacheServer
{
    private final int numThreads;

    public ThreadPoolServer(int numThreads) {
        super();
        this.numThreads = numThreads;
    }

    @Override
    public void start(int port) {
        try (var executor = Executors.newFixedThreadPool(this.numThreads);
             var serverSocket = new ServerSocket(port);) {
            IO.println("Server listening to: " + port);
            while (running) {
                var clientSocket = serverSocket.accept(); // block until connexion
                var clientInfo = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
                logger.info("CONNECTED: " + clientInfo);

                executor.execute(() -> handleNewConnection(clientSocket));
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Server disconnected : " + e.getMessage());
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error stopping server : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
