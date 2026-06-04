package distrib;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class NodeConnectionPool {

    private final BlockingQueue<NodeConnection> available; // connexions libres

    public NodeConnectionPool(Node node, int poolSize) throws IOException {
        this.available = new ArrayBlockingQueue<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            available.add(new NodeConnection(node.getHost(), node.getPort()));
        }
    }

    public NodeConnection borrow() throws InterruptedException {
        return available.take();
    }

    public void release(NodeConnection connection) {
        available.offer(connection);
    }

    public void deconnect() throws IOException {
        for (NodeConnection co : available){
            co.deconnect();
        }
    }
}