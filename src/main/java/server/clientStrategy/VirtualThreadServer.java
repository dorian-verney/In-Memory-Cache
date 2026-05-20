package server.clientStrategy;

import server.CacheServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

public class VirtualThreadServer extends CacheServer
{
    @Override
    public void start(int port) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var serverSocket = new ServerSocket(port);) {
            IO.println("Server listening to: " + port);
            while (running) {
                Socket clientSocket = serverSocket.accept();
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