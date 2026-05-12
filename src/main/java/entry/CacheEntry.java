package entry;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CacheEntry implements ExpirationPolicy
{
    private final String key;

    private volatile String value;
    private volatile long ttlMillis;
    private long createdAt;
    private volatile long lastAccessTime;

    public CacheEntry(String key, String value, long ttlMillis) {
        this.key = key;
        this.value = value;
        this.ttlMillis = ttlMillis;
        createdAt = System.currentTimeMillis();
        lastAccessTime = createdAt;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public long getTTL() {
        return this.ttlMillis;
    }

    public long getCreatedAt() {
        return this.createdAt;
    }

    public long getLastAccessTime() {
        return this.lastAccessTime;
    }

    public synchronized void recordAccess() {
        lastAccessTime = System.currentTimeMillis();
    }

    public void onPut(String value, long ttlMillis) {
        createdAt = System.currentTimeMillis();
        this.value = value;
        this.ttlMillis = ttlMillis;
        lastAccessTime = createdAt;
    }

    @Override
    public boolean isExpired() {
        return this.ttlMillis < (System.currentTimeMillis() -  this.createdAt);
    }


    public String toString()
    {
        return "(" + this.key + ", " + this.value + ", " + this.ttlMillis + ")";
    }
}
