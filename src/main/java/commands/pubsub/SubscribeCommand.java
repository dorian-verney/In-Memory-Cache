package commands.pubsub;

import context.client.SessionContext;
import commands.CommandResponse;
import pubsub.PubSubBroker;

import java.util.Arrays;
import java.util.List;

public class SubscribeCommand implements PubSubCommand {

    private static final int MIN_ARGS = 2;

    @Override
    public CommandResponse execute(PubSubBroker broker, String[] args) {
        if (args.length < MIN_ARGS)
            return CommandResponse.error("ERROR: invalid number of args " +
                    "(Min. SUB channel)");

        int numChannelSub = 0;
        List<String> channels = Arrays.asList(args).subList(1, args.length);
        for (String channel : channels){
            try {
                numChannelSub = broker.subscribe(channel, SessionContext.get());
            } catch (Exception e) {
                return CommandResponse.error("ERROR Exception " + e.getMessage());
            }
        }
        return CommandResponse.subscribe(channels, numChannelSub);
    }

}
