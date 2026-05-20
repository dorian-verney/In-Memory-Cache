package cache.evictionPolicy;

public interface EvictionPolicy {

    void onAccess(String key);

    void onInsert(String key);

    void onRemove(String key);

    String evict();

}
