package evictionPolicy;

import cache.CacheStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExpirationCleaner implements Runnable {

    private static final double RATE = 0.20;

    private final int intervalMs;
    private final CacheStore cache;

    public ExpirationCleaner(CacheStore cache, int intervalMs) {
        this.cache = cache;
        this.intervalMs = intervalMs;
    }

    private List<String> randomSample() {
        var keys = new ArrayList<>(cache.getCache().keySet());
        int num = (int) (keys.size() * RATE);
        if (num == 0) return List.of();
        Collections.shuffle(keys);
        return keys.subList(0, num);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            cache.evictExpired(randomSample());
            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // restore flag
                break;
            }
        }
    }
}
