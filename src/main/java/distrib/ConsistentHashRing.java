package distrib;

import java.util.TreeMap;

public class ConsistentHashRing {

    private final TreeMap<Integer, Node> ring = new TreeMap<>();
    private final int virtualNodes; // number of vnodes per real node

    public ConsistentHashRing(int virtualNodes) {
        this.virtualNodes = virtualNodes;
    }

    public void addNode(Node node) {
        for (int i=0; i < virtualNodes; i++){
            var vNodeName = node.getId() + "-vnode-" + i;
            ring.put(hash(vNodeName), node);
        }
    }

    public void removeNode(Node node) {
        for (int i=0; i < virtualNodes; i++){
            var vNodeName = node.getId() + "-vnode-" + i;
            ring.remove(hash(vNodeName));
        }
    }

    public Node getNode(String key) {
        if (ring.isEmpty()){
            return null;
        }
        var entry = ring.ceilingEntry(hash(key));

        // get the first
        if (entry == null){
            return ring.firstEntry().getValue();
        }

        return entry.getValue();
    }

    private int hash(String key) {
        return Math.abs(key.hashCode());
    }
}