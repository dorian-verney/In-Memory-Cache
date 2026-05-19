package Server;

import Context.client.SessionContext;
import Context.server.ConnectionContext;
import cache.CacheStore;
import commands.CommandDispatcher;
import commands.CommandResponse;
import commands.ResponseType;
import evictionPolicy.ExpirationCleaner;
import evictionPolicy.LRUPolicy;
import pubsub.PubSubBroker;
import utils.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import java.util.UUID;

public abstract class CacheServer {

    private static final int STORE_CAPACITY = 100;
    private static final int CLEANER_INTERVAL_MS = 1000;

    protected volatile boolean running = true;

    protected ServerSocket serverSocket;

    private CacheStore cache;
    private final PubSubBroker broker;
    protected final CommandDispatcher dispatcher;

    private ConnectionContext context;

    protected static Logger logger = LoggerFactory.create(CacheServer.class, "server.log");


    public CacheServer() {

        this.cache = new CacheStore(STORE_CAPACITY,new LRUPolicy());
        this.broker = new PubSubBroker();
        this.dispatcher = new CommandDispatcher(cache, broker);

        // Cleaner
        var thread = new Thread(new ExpirationCleaner(cache, CLEANER_INTERVAL_MS));
        thread.setDaemon(true);
        thread.start();

    }

    public abstract void start(int port);

    public abstract void stop();

    protected void handleNewConnection(Socket clientSocket){
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