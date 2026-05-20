package server.clientStrategy;

import server.CacheServer;

import java.io.IOException;
import java.net.ServerSocket;

public class ThreadPerConnectionServer extends CacheServer
{

    @Override
    public void start(int port) {
        try (var serverSocket = new ServerSocket(port);) {
            IO.println("Server listening to: " + port);
            while (running) {
                var clientSocket = serverSocket.accept(); // block until connexion
                var clientInfo = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
                logger.info("CONNECTED: " + clientInfo);

                new Thread(() -> handleNewConnection(clientSocket)).start();
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Server disconnected : " + e.getMessage());
            }
            throw new RuntimeException(e);
        }
    }


    private void handlingClient(){

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
