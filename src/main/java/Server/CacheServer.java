import cache.CacheStore;
import commands.CommandDispatcher;
import exclusionPolicy.ExclusionStrategies;
import exclusionPolicy.LeastRecentlyUsed;
import expirationCleaner.ExpirationCleaner;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CacheServer {
    private final CommandDispatcher dispatcher;

    public CacheServer()
    {
        // Eviction Strategy
        ExclusionStrategies strategy = new LeastRecentlyUsed();

        // Expiration
        ExpirationCleaner cleaner = new ExpirationCleaner(10_000);

        // Cache
        CacheStore cache = new CacheStore(3);
        cache.setExclusionStrategy(strategy);
        cleaner.subscribe(cache);
        cleaner.start();

        // Dispatcher
        this.dispatcher = new CommandDispatcher(cache);

    }

    public void start(int port) throws IOException {
        
    }
}