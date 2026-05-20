package pubsub;

public interface ISubscriber {
    boolean enqueue(Message msg);
}
