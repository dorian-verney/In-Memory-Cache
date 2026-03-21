package cache;

import entry.CacheEntry;
import exclusionPolicy.ExclusionStrategies;
import expirationCleaner.SubscriberTTL;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;


public class CacheStore implements SubscriberTTL, Servable
{
    private final int maxCapacity;
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private ExclusionStrategies exclusionStrategy;

    public CacheStore() {this.maxCapacity = 100;}
    public CacheStore(int maxCapacity) {this.maxCapacity = maxCapacity;}

    // -------------------------------------------------------------------------
    // Getter
    // -------------------------------------------------------------------------
    public int getMaxCapacity() {return this.maxCapacity;}
    public ConcurrentHashMap<String, CacheEntry> getStorage() {return this.cache;}

    // -------------------------------------------------------------------------
    // Exclusion Principle (LRU, LFU, FIFO...)
    // -------------------------------------------------------------------------
    public void setExclusionStrategy(ExclusionStrategies s){this.exclusionStrategy = s;}
    public void executeExclusion() {this.exclusionStrategy.excludeElement(this.cache);}

    // -------------------------------------------------------------------------
    // Expiration Principle of elements (TTL)
    // -------------------------------------------------------------------------
    @Override
    public void updateExpiration()
    {
        this.cache.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    // -------------------------------------------------------------------------
    // Servable
    // -------------------------------------------------------------------------
    /**
     * @param key
     * @param value
     * @param ttlMillis
     * @return
     */
    @Override
    public String add(String key, String value, long ttlMillis) {
        if (this.cache.size() == this.maxCapacity) {this.executeExclusion();}

        CacheEntry entry = new CacheEntry(key, value, ttlMillis, System.currentTimeMillis());
        this.cache.putIfAbsent(key, entry);

        return key;
    }

    /**
     * @param key
     * @return
     */
    @Override
    public String get(String key) {
        CacheEntry entry = this.cache.get(key);
        if (entry == null) throw new NoSuchElementException("Storage does not contains " + key);

        // lazy expiration
        if (entry.isExpired()) {
            cache.remove(key);
            throw new NoSuchElementException("Storage does not contains " + key);
        }
        entry.recordAccess();
        return entry.getValue();
    }

    /**
     * @param key
     * @param value
     * @param ttlMillis
     * @return
     */
    @Override
    public String set(String key, String value, long ttlMillis) {
        if (!this.cache.containsKey(key)) throw new NoSuchElementException("Storage does not contains " + key);

        CacheEntry entry = this.cache.get(key);
        entry.onPut(value, ttlMillis);
        this.cache.put(key, entry);
        return key;
    }

    /**
     * @param key
     * @return
     */
    @Override
    public String del(String key) {
        CacheEntry res = this.cache.remove(key);
        if (res == null) {throw new NoSuchElementException("Storage does not contains " + key);}

        return key;
    }
    // -------------------------------------------------------------------------
    // Others
    // -------------------------------------------------------------------------

    public CacheEntry getEntry(String key){return this.cache.get(key);}

    public void printStorage(){
        this.cache.values().forEach(IO::println);
    }


}
