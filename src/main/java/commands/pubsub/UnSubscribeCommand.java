package commands.pubsub;

import Context.client.SessionContext;
import commands.CommandResponse;
import pubsub.PubSubBroker;

import java.util.Arrays;
import java.util.List;

public class UnSubscribeCommand implements PubSubCommand {
    private static final int MIN_ARGS = 1;

    @Override
    public CommandResponse execute(PubSubBroker broker, String[] args) {
        if (args.length < MIN_ARGS)
            return CommandResponse.error("ERROR: invalid number of args " +
                    "(Min. UNSUB)");

        // remove sub from all channels
        if (args.length == 1){
            var numSubs = broker.unSubscribeAll(SessionContext.get());
            return CommandResponse.success("(integer) " + numSubs);
        }

        var numChannelLeft = 0;
        var numChannels = args.length - 1;
        List<String> channels = Arrays.asList(args).subList(1, args.length);
        for (String channel : channels){
            try {
                numChannelLeft = broker.unSubscribe(channel, SessionContext.get());
            } catch (Exception e) {
                return CommandResponse.error("ERROR Exception " + e.getMessage());
            }
        }
        if (numChannelLeft == 0)
            return CommandResponse.success("(integer) " + numChannels);

        return CommandResponse.unsubscribe(channels, numChannelLeft);
    }
}
