package commands.pubsub;

import commands.CommandResponse;
import pubsub.PubSubBroker;

import java.util.List;

public class ChannelCommand implements PubSubCommand{

    private static final int MIN_ARGS = 2;

    @Override
    public CommandResponse execute(PubSubBroker broker, String[] args) {
        if (args.length < MIN_ARGS)
            return CommandResponse.error("ERROR: invalid number of args " +
                    "(Min. PUBSUB CHANNELS)");

        List<String> channels;
        try {
            channels = broker.getChannels();
        } catch (Exception e) {
            return CommandResponse.error("ERROR Exception " + e.getMessage());
        }
        return CommandResponse.success(channels);
    }
}
