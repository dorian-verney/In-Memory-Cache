package pubsub;

import utils.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


public class Subscriber implements ISubscriber {

    private final String id;
    private final Set<String> channels;
    private final BlockingQueue<Message> queue = new LinkedBlockingQueue<>(100);

    private static final Logger logger = LoggerFactory.create(Subscriber.class, "Broker.log");

    public Subscriber(String clientId){
        id = clientId;
        channels = new HashSet<>();
    }

    public void subscribe(String channel){
        channels.add(channel);
    }

    public void unSubscribe(String channel){
        channels.remove(channel);
    }

    public void unSubscriberAll(){
        channels.removeAll(channels);
    }

    public String getId(){
        return id;
    }

    public Set<String> getChannels(){
        return channels;
    }

    public Message poll(int timeout, TimeUnit unit) {
        try {
            return queue.poll(timeout, unit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void enqueue(Message msg) {
        var added = queue.offer(msg);
    }
}
