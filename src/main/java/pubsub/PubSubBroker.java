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

    // thread safe
    // return the number of channel subscribed
    public int subscribe(String channel, String clientId){
        var sub = subscribers.computeIfAbsent(clientId, Subscriber::new);

        sub.subscribe(channel);

        // ATOMIC
        channelToSubscribers.computeIfAbsent(channel,
                        c -> ConcurrentHashMap.newKeySet()).add(sub);

        logger.info("%s SUBSCRIBE to channel %s".formatted(clientId, channel));
        return sub.getChannels().size();
    }

    // return the number of left channel subscribed
    public int unSubscribe(String channel, String clientId){
        var sub = subscribers.get(clientId);
        if (sub == null) return 0;

        sub.unSubscribe(channel);
        if (!sub.isActive()) subscribers.remove(clientId);

        // ATOMIC
        channelToSubscribers.computeIfPresent(channel, (k, subs) -> {
            subs.remove(sub);
            return subs.isEmpty() ? null : subs;
        });

        logger.info("%s UNSUBSCRIBE to channel %s".formatted(clientId, channel));
        return sub.getChannels().size();
    }

    public int unSubscribeAll(String clientId){
        var sub = subscribers.remove(clientId); // atomic
        if (sub == null) return 0;

        var channels = sub.getChannels();
        for (String channel : channels) {
            channelToSubscribers.computeIfPresent(channel, (k, subs) -> {
                subs.remove(sub);
                return subs.isEmpty() ? null : subs;
            });
        }
        sub.unSubscriberAll();

        logger.info("%s UNSUBSCRIBE to all its channels %s".formatted(clientId, channels));
        return channels.size();
    }

    public int publish(String channel, String payload) {
        var subs = channelToSubscribers.get(channel);
        if (subs == null || subs.isEmpty()) return 0;

        for (Subscriber sub : subs){
            var msg = new Message(channel, payload);
            if (sub.isActive()) {
                if (!sub.enqueue(msg))
                    logger.warning("Queue full for subscriber %s, message dropped".formatted(sub.getId()));
            }
            logger.info("PUBLISH %s to channel %s".formatted(msg.payload(), msg.channel()));
        }
        return subs.size();
    }

    public List<String> getChannels(){
        return new ArrayList<>(channelToSubscribers.keySet());
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

    public String poll(String clientId, int timeout) {
        logger.info("POLLING %s queue".formatted(clientId));
        var sub = subscribers.get(clientId);
        if (sub == null) return null;
        var msg = sub.poll(timeout, TimeUnit.MILLISECONDS);
        if (msg != null) {
            logger.info("MESSAGE from polling %s : %s".formatted(clientId, msg.payload()));
            return msg.toString();
        }
        return null;
    }
}
