package cache.evictionPolicy;

import java.util.LinkedHashSet;

public class LRUPolicy implements EvictionPolicy {

    // TODO handle concurrency !! (lock striping)
    private final LinkedHashSet<String> accessOrder;

    public LRUPolicy(){
        accessOrder = new LinkedHashSet<>();
    }

    // time complex. O(1)
    @Override
    public void onAccess(String key) {
        accessOrder.remove(key);
        accessOrder.addFirst(key);
    }

    // time complex. O(1)
    @Override
    public void onInsert(String key) {
        // suppose key IS NOT in accessOrder
        accessOrder.addFirst(key);
    }

    // time complex. O(1)
    @Override
    public void onRemove(String key) {
        accessOrder.remove(key);
    }

    // time complex. O(1)
    @Override
    public String evict() {
        var last = accessOrder.getLast();
        accessOrder.removeLast();
        return last;
    }

}
