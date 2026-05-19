package cache;

import evictionPolicy.EvictionPolicy;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

public class CacheStore implements Servable {

    private final int maxCapacity;
    private final ConcurrentHashMap<String, CacheEntry> cache;
    private final EvictionPolicy policy;

    public CacheStore(EvictionPolicy policy) {
        maxCapacity = 100;
        this.policy = policy;
        cache = new ConcurrentHashMap<>(maxCapacity);
    }

    public CacheStore(int maxCapacity, EvictionPolicy policy) {
        this.maxCapacity = maxCapacity;
        this.policy = policy;
        cache = new ConcurrentHashMap<>(maxCapacity);
    }

    public ConcurrentHashMap<String, CacheEntry> getCache(){
        return cache;
    }

    // -------------------------------------------------------------------------
    // Expiration eviction
    // -------------------------------------------------------------------------
    public synchronized void evictExpired(List<String> sample) {

        for (String e : sample){
            var entry = cache.get(e);
            if (entry != null && entry.isExpired()) {
                cache.remove(e);
                policy.onRemove(e);
            }
        }
    }


    // -------------------------------------------------------------------------
    // Servable
    // -------------------------------------------------------------------------

    @Override
    public synchronized String set(String key, String value, long ttlSec) {
        if (!cache.containsKey(key)) {
            // add it
            if (cache.size() == maxCapacity) {
                var evicted = policy.evict();
                cache.remove(evicted);
            }
            var entry = new CacheEntry(key, value, ttlSec);
            if (cache.putIfAbsent(key, entry) == null)
                policy.onInsert(key);

            return key;
        }

        var entry = cache.get(key);
        entry.onSet(value, ttlSec);
        cache.put(key, entry);
        policy.onAccess(key);
        return key;
    }

    @Override
    public synchronized String get(String key) {
        var entry = cache.get(key);

        if (entry == null)
            throw new NoSuchElementException("Cache does not contains " + key);

        // lazy expiration
        if (entry.isExpired()) {
            cache.remove(key);
            policy.onRemove(key);
            throw new NoSuchElementException("Cache does not contains " + key);
        }
        entry.recordAccess();
        return entry.getValue();
    }

    @Override
    public synchronized String del(String key) {
        var res = cache.remove(key);
        if (res == null)
            return null;

        policy.onRemove(key);
        return key;
    }

    @Override
    public synchronized String ttl(String key) {
        var res = cache.get(key);
        if (res == null) {
            throw new NoSuchElementException("Cache does not contains " + key);
        }
        return String.valueOf(res.getTTL());
    }

    @Override
    public synchronized String expire(String key, long ttlSec) {
        var res = cache.get(key);
        if (res == null) {
            throw new NoSuchElementException("Cache does not contains " + key);
        }
        res.setExpire(true);
        res.setTTL(ttlSec);
        return String.valueOf(ttlSec);
    }

    // -------------------------------------------------------------------------
    // Others
    // -------------------------------------------------------------------------
    public synchronized CacheEntry getEntry(String key) {
        return cache.get(key);
    }

    public void printStorage(){
        cache.values().forEach(IO::println);
    }


}
