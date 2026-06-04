package server;

import context.client.SessionContext;
import context.server.ConnectionContext;
import cache.CacheStore;
import commands.CommandDispatcher;
import cache.evictionPolicy.ExpirationCleaner;
import cache.evictionPolicy.LRUPolicy;
import pubsub.PubSubBroker;
import utils.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import java.util.UUID;

public class CacheServer {

    private static final int STORE_CAPACITY = 100;
    private static final int CLEANER_INTERVAL_MS = 1000;

    private volatile boolean running = true;

    private int port;
    private String host;
    private final int poolSize;

    private ServerSocket serverSocket;

    private CacheStore cache;
    private final PubSubBroker broker;
    private final CommandDispatcher dispatcher;

    private ConnectionContext context;

    private  static Logger logger = LoggerFactory.create(CacheServer.class, "server.log");


    public CacheServer(String host, int port, int poolSize) {
        this.host       = host;
        this.port       = port;
        this.poolSize   = poolSize;
        this.cache      = new CacheStore(STORE_CAPACITY, new LRUPolicy());
        this.broker     = new PubSubBroker();
        this.dispatcher = new CommandDispatcher(cache, broker);

        // Cleaner
        var thread = new Thread(new ExpirationCleaner(cache, CLEANER_INTERVAL_MS));
        thread.setDaemon(true);
        thread.start();
    }

    public void start(){
        try (var executor = Executors.newFixedThreadPool(this.poolSize);
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

    private void handleNewConnection(Socket clientSocket){
        String name = "[Server] [" + Thread.currentThread().getName() + "] ";
        var clientID = UUID.randomUUID().toString();
        var clientInfo = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
        SessionContext.set(clientID);
        try (var clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             var writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            context = new ConnectionContext(clientIn, writer, dispatcher, clientID, clientInfo);
            context.handle();

        } catch (IOException e){
            throw new RuntimeException(e);
        } finally {
            logger.info(name + clientInfo + " closed");
            SessionContext.clear();
        }
    }

}