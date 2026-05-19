package pubsub;

import Server.CacheServer;
import utils.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class PubSubBroker {

    private final ConcurrentHashMap<String, Subscriber> subscribers;
    private final ConcurrentHashMap<String, Set<Subscriber>> channelToSubscribers;

    private static final Logger logger = LoggerFactory.create(PubSubBroker.class, "Broker.log");

    public PubSubBroker() {
        subscribers          = new ConcurrentHashMap<>();
        channelToSubscribers = new ConcurrentHashMap<>();
    }

    // return the number of channel subscribed
    public int subscribe(String channel, String clientId){
        subscribers.computeIfAbsent(clientId, Subscriber::new)
                .subscribe(channel);

        channelToSubscribers.computeIfAbsent(channel, c -> ConcurrentHashMap.newKeySet())
                .add(subscribers.get(clientId));

        logger.info("%s SUBSCRIBE to channel %s".formatted(clientId, channel));
        return subscribers.get(clientId).getChannels().size();
    }

    // return the number of left channel subscribed
    public int unSubscribe(String channel, String clientId){
        subscribers.get(clientId).unSubscribe(channel);

        var subs = channelToSubscribers.get(channel);
        if (subs != null) subs.remove(subscribers.get(clientId));

        logger.info("%s UNSUBSCRIBE to channel %s".formatted(clientId, channel));
        return subscribers.get(clientId).getChannels().size();
    }

    public int unSubscribeAll(String clientId){
        var sub = subscribers.get(clientId);
        int numChannels = sub.getChannels().size();
        var channels = sub.getChannels();
        for (String channel : channels){
            channelToSubscribers.get(channel).remove(sub);
        }
        sub.unSubscriberAll();
        subscribers.remove(clientId);

        logger.info("%s UNSUBSCRIBE to all its channels %s".formatted(clientId, channels));

        return numChannels;
    }

    public int publish(String channel, String payload) {
        var subs = channelToSubscribers.get(channel);
        if (!subs.isEmpty())
            for (Subscriber sub : subs){
                var message = new Message(channel, payload);
                sub.enqueue(message);
                logger.info("PUBLISH %s to channel %s".formatted(message.payload(), message.channel()));
            }
        return subs.size();
    }

    public HashMap<String, Integer> numSub(List<String> channels){
        List<String> keys;
        if (channels.isEmpty())
            keys = List.copyOf(channelToSubscribers.keySet());
        else
            keys = channels;
        HashMap<String, Integer> numSub = new HashMap<>();
        for (String channel : keys){
            var entry = channelToSubscribers.get(channel);
            if (entry != null){
                numSub.put(channel, entry.size());
            }
        }
        return numSub;
    }

    public String poll(String clientId, int timeout) throws InterruptedException {
        logger.info("POLLING %s queue".formatted(clientId));
        var msg = subscribers.get(clientId).poll(timeout, TimeUnit.MILLISECONDS);
        if (msg != null) {
            logger.info("MESSAGE from polling %s : %s".formatted(clientId, msg.payload()));
            return msg.toString();
        }
        return null;
    }
}
