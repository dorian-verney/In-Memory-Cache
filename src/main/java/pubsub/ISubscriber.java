package pubsub;

public interface ISubscriber {
    void enqueue(Message msg);
}
