package exclusionPolicy;

import java.util.concurrent.ConcurrentHashMap;
import entry.CacheEntry;

public interface ExclusionStrategies
{
    void excludeElement(ConcurrentHashMap<String, CacheEntry> cache);
}
