package entry;


public class CacheEntry
{
    private final String key;
    private volatile String value;

    private volatile boolean expire = false;
    private volatile long ttlSec;

    private volatile long createdAt;
    private volatile long lastAccessTime;

    public CacheEntry(String key, String value){
        this.key = key;
        this.value = value;
        initTimeCreation();
    }

    public CacheEntry(String key, String value, long ttlSec) {
        this(key, value);
        if (ttlSec != -1) {
            expire = true;
        }
        this.ttlSec = ttlSec;
    }

    public void initTimeCreation(){
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
        if (!expire) return -1;
        return this.ttlSec;
    }

    public void setTTL(long ttlSec){
        this.ttlSec = ttlSec;
    }

    public void setExpire(boolean expire){
        this.expire = expire;
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

    public void onSet(String value, long ttlSec) {
        createdAt = System.currentTimeMillis();
        this.value = value;
        if (ttlSec != -1) {
            this.ttlSec = ttlSec;
            expire = true;
        }
        lastAccessTime = createdAt;
    }

    public boolean isExpired() {
        return expire && (this.ttlSec < ((System.currentTimeMillis()/1000) -  this.createdAt));
    }

    public String toString()
    {
        return "(" + this.key + ", " + this.value + ", " + this.ttlSec + ")";
    }
}
