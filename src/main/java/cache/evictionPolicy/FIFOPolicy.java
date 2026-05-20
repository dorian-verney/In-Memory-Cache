package cache.evictionPolicy;

import java.util.LinkedHashSet;

public class FIFOPolicy implements EvictionPolicy {

    private final LinkedHashSet<String> accessOrder;

    public FIFOPolicy(){
        accessOrder = new LinkedHashSet<>();
    }

    @Override
    public void onAccess(String key) {
        // no operation, FIFO DOES NOT reorder
    }

    // time complex. O(1)
    @Override
    public  void onInsert(String key) {
        accessOrder.addLast(key);
    }

    // time complex. O(1)
    @Override
    public  void onRemove(String key) {
        accessOrder.remove(key);
    }

    // time complex. O(1)
    @Override
    public  String evict() {
        return accessOrder.removeFirst();
    }
}
