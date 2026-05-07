package exclusionPolicy;

import entry.CacheEntry;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LeastFrequentlyUsed implements ExclusionStrategy
{

    /** Exclude the least frequent used element from cache (LFU)
     * @param cache cache where key:value are stored
     */
    @Override
    public void excludeElement(ConcurrentHashMap<String, CacheEntry> cache)
    {
        cache.entrySet().stream()
                .min(Comparator.comparingInt(
                        e -> e.getValue().getAccessCount())
                )
                .map(Map.Entry::getKey)
                .ifPresent(cache::remove);
    }
}
