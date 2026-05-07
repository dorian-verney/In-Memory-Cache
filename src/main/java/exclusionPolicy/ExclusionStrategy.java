package exclusionPolicy;

import java.util.concurrent.ConcurrentHashMap;
import entry.CacheEntry;

public interface ExclusionStrategy
{
    void excludeElement(ConcurrentHashMap<String, CacheEntry> cache);
}
