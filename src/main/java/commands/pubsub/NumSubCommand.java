package commands.pubsub;

import Context.client.SessionContext;
import commands.CommandResponse;
import pubsub.PubSubBroker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class NumSubCommand implements PubSubCommand {
    private static final int MIN_ARGS = 2;

    @Override
    public CommandResponse execute(PubSubBroker broker, String[] args) {
        if (args.length < MIN_ARGS)
            return CommandResponse.error("ERROR: invalid number of args " +
                    "(Min. PUBSUB NUMSUB)");

        List<String> channels = Arrays.asList(args).subList(2, args.length);
        HashMap<String, Integer> map;
        try {
            map = broker.numSub(channels);
        } catch (Exception e) {
            return CommandResponse.error("ERROR Exception " + e.getMessage());
        }
        return CommandResponse.success(map);
    }
}
