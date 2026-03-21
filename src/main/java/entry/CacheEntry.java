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
    private int accessCount;

    private Lock lock = new ReentrantLock();

    public CacheEntry(String key, String value, long ttlMillis, long createdAt)
    {
        this.key = key;
        this.value = value;
        this.ttlMillis = ttlMillis;
        this.createdAt = createdAt;
        this.lastAccessTime = createdAt;
        this.accessCount = 0;
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

    public int getAccessCount() {
        return this.accessCount;
    }

    public void recordAccess()
    {
        this.lastAccessTime = System.currentTimeMillis();

        lock.lock();
        this.accessCount++;
        lock.unlock();
    }

    public void onPut(String value, long ttlMillis)
    {
        this.createdAt = System.currentTimeMillis();
        this.value = value;
        this.ttlMillis = ttlMillis;
        this.recordAccess();
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
