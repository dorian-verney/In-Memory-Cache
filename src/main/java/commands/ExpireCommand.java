package commands;

import cache.CacheStore;

public class ExpireCommand implements Command {

    private static final int MIN_ARGS = 3;

    @Override
    public CommandResponse execute(CacheStore cache, String[] args) {
        if (args.length < MIN_ARGS)
            return CommandResponse.error("ERROR: invalid number of args (Min. EXPIRE key)");

        long ttl;
        String res;
        try {
            ttl = Long.parseLong(args[2]);
            if (ttl <= 0)
                return CommandResponse.error("ERROR Exception EX must be a positive integer");
            res = cache.expire(args[1], ttl);
        } catch (Exception e) {
            return CommandResponse.error("ERROR Exception " + e.getMessage());
        }

        return CommandResponse.success("(integer) " + res);
    }
}
