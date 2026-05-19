package commands.pubsub;


import commands.CommandResponse;
import pubsub.PubSubBroker;

public interface PubSubCommand {
    CommandResponse execute(PubSubBroker broker, String[] args);

}
