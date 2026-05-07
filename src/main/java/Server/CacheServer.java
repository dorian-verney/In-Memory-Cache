package Server;

import cache.CacheStore;
import commands.CommandDispatcher;
import commands.CommandResponse;
import exclusionPolicy.ExclusionStrategy;
import exclusionPolicy.LeastRecentlyUsed;
import expirationCleaner.ExpirationCleaner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public abstract class CacheServer {
    protected final CommandDispatcher dispatcher;
    protected volatile boolean running = true;

    protected ServerSocket serverSocket;

    protected static Logger logger = Logger.getLogger("Server");

    static {
        try {
            new java.io.File("logs").mkdirs();
            var fh = new FileHandler("logs/server.log", true);
            fh.setFormatter(new SimpleFormatter());
            logger.setUseParentHandlers(false);
            logger.addHandler(fh);
        } catch (IOException e) {
            System.err.println("Logger setup failed: " + e.getMessage());
        }
    }

    public CacheServer()
    {
        // Eviction Strategy
        ExclusionStrategy strategy = new LeastRecentlyUsed();

        // Expiration
        ExpirationCleaner cleaner = new ExpirationCleaner(10_000);

        // Cache
        CacheStore cache = new CacheStore(10000);
        cache.setExclusionStrategy(strategy);
        cleaner.subscribe(cache);
//        cleaner.start();

        // Dispatcher
        this.dispatcher = new CommandDispatcher(cache);
    }

    public abstract void start(int port);

    public abstract void stop();

    protected void handleNewConnection(Socket clientSocket){
        var clientInfo = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
        String name = "[Server] [" + Thread.currentThread().getName() + "] ";
        try (var clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             var writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            listening:
            while (true){
                var sb = new StringBuilder();
                String in;
                do {
                    in = clientIn.readLine();
                    if (in == null || in.equals("QUIT")) break listening;

                    sb.append(in.replace(";", ""));
                    IO.println(sb);
                } while (!in.endsWith(";"));

                var out = handleServerResponse(sb.toString(), clientInfo);
                writer.println(out);
            }
            logger.info(name + clientInfo + " closed");
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    protected String handleServerResponse(String userCmd, String clientInfo){
        String name = "[Server] [" + Thread.currentThread().getName() + "] ";
        // Handle real command from client
        CommandResponse response = dispatcher.dispatch(userCmd);
        logger.info(name + clientInfo + "; Response : " + response.getMessage());
        if (!response.isSuccess()) {
            return response.getMessage();
        } else {
            return "OK " + response.getMessage() + " " +
                    response.getValue().map(v -> " " + v).orElse("");
        }
    }

}