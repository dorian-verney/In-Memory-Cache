package exclusionPolicy;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import entry.CacheEntry;

public class LeastRecentlyUsed implements ExclusionStrategies
{

    /** Exclude the least recent used element from cache (LRU)
     * @param cache cache where key:value are stored
     */
    @Override
    public void excludeElement(ConcurrentHashMap<String, CacheEntry> cache)
    {

        long timeRef = System.currentTimeMillis();
        cache.entrySet().stream()
                .max(Comparator.comparingLong(
                        e -> (timeRef - e.getValue().getLastAccessTime()))
                )
                .map(Map.Entry::getKey)
                .ifPresent(cache::remove);
    }
}
