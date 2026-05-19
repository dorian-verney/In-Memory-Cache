package commands.storage;

import cache.CacheStore;
import commands.CommandResponse;

public class GetCommand implements BasicCommand {

    private static final int MIN_ARGS = 2;

    @Override
    public CommandResponse execute(CacheStore cache, String[] args) {
        if (args.length < MIN_ARGS)
            return CommandResponse.error("ERROR: invalid number of args (Min. GET key)");

        String key = args[1];
        String value;
        try {
            value = cache.get(key);
        } catch (Exception e) {
            return CommandResponse.error("ERROR Exception " + e.getMessage());
        }

        return CommandResponse.success(key, value);
    }
}
