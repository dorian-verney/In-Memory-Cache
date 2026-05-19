package commands.pubsub;

import commands.CommandResponse;
import pubsub.PubSubBroker;

public class PublishCommand implements PubSubCommand {

    private static final int MIN_ARGS = 3;

    @Override
    public CommandResponse execute(PubSubBroker broker, String[] args) {
        if (args.length < MIN_ARGS)
            return CommandResponse.error("ERROR: invalid number of args " +
                                         "(Min. PUB channel message)");

        int numClients;
        try {
            numClients = broker.publish(args[1], args[2]);
        } catch (Exception e) {
            return CommandResponse.error("ERROR Exception " + e.getMessage());
        }

        return CommandResponse.success(String.valueOf(numClients));
    }
}
