package commands;

import cache.CacheStore;

public class TTLCommand implements Command {

    private static final int MIN_ARGS = 2;

    @Override
    public CommandResponse execute(CacheStore cache, String[] args) {
        if (args.length < MIN_ARGS)
            return CommandResponse.error("ERROR: invalid number of args (Min. TTL key)");

        String value;
        try {
            value = cache.ttl(args[1]);
        } catch (Exception e) {
            return CommandResponse.error("ERROR Exception " + e.getMessage());
        }

        return CommandResponse.success("(integer) " + value);
    }
}
