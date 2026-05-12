package evictionPolicy;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class LFUPolicy implements EvictionPolicy {

    class FreqNode {
        int freq;
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        FreqNode prev, next;

        public FreqNode(int f) {
            freq = f;
        }
    }

    // TODO handle concurrency !! (lock striping)

    private final Map<String, Integer> keyFreq;                    // key → frequency

    private final FreqNode HEAD; // sentinel
    private final FreqNode TAIL; // sentinel
    private final Map<Integer, FreqNode> freqBuckets; // freq → keys (insertion order)

    public LFUPolicy() {
        keyFreq = new HashMap<>();
        HEAD = new FreqNode(-1);
        TAIL = new FreqNode(Integer.MAX_VALUE);
        HEAD.next = TAIL;
        TAIL.prev = HEAD;
        freqBuckets = new HashMap<>();
    }

    // time complex. O(1)
    @Override
    public void onAccess(String key) {
        var curFreq = keyFreq.get(key);
        keyFreq.put(key, curFreq + 1);

        var curNode = freqBuckets.get(curFreq);
        curNode.keys.remove(key);

        // Update new freq inside freqBucket
        FreqNode newNode;
        if (!freqBuckets.containsKey(curFreq+1)){
            newNode = new FreqNode(curFreq+1);
            newNode.keys.addFirst(key);
            freqBuckets.put(curFreq+1, newNode);
            var next = curNode.next;
            curNode.next = newNode;
            newNode.prev = curNode;
            newNode.next = next;
            next.prev = newNode;
        } else {
            freqBuckets.get(curFreq+1).keys.addLast(key);
            newNode = freqBuckets.get(curFreq+1);
        }

        if (freqBuckets.get(curFreq).keys.isEmpty()) {
            var prev = curNode.prev;
            prev.next = newNode;
            newNode.prev = prev;
            freqBuckets.remove(curFreq);
        }
    }

    // time complex. O(1)
    @Override
    public void onInsert(String key) {
        keyFreq.put(key, 1);

        if (!freqBuckets.containsKey(1)) {
            var node = new FreqNode(1);
            node.keys.addFirst(key);
            var next = HEAD.next;
            node.next = next;
            next.prev = node;
            HEAD.next = node;
            node.prev = HEAD;
            freqBuckets.put(1, node);
        } else {
            freqBuckets.get(1).keys.addLast(key);
        }
    }

    // time complex. O(1)
    @Override
    public void onRemove(String key) {
        var freq = keyFreq.remove(key);
        var node = freqBuckets.get(freq);
        node.keys.remove(key);
        if (node.keys.isEmpty()) {
            freqBuckets.remove(freq);
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }
    }

    // time complex. O(1)
    @Override
    public String evict() {
        var minFreq = HEAD.next.freq;
        var last = freqBuckets.get(minFreq).keys.removeLast();
        if (HEAD.next.keys.isEmpty()) {
            freqBuckets.remove(minFreq);
            var next = HEAD.next.next;
            next.prev = HEAD;
            HEAD.next = next;
        }
        keyFreq.remove(last);
        return last;
    }

}
