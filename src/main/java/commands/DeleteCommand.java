package commands;

import cache.CacheStore;

import java.util.Arrays;

public class DeleteCommand implements Command {
    private static final int MIN_ARGS = 2;

    @Override
    public CommandResponse execute(CacheStore cache, String[] args) {
        if (args.length < MIN_ARGS)
            return CommandResponse.error("ERROR: invalid number of args (Min. DEL key)");

        // can be DEL key1 key2 key3 ...
        int numDel = 0;
        for (String key : Arrays.asList(args).subList(1, args.length)){
            try {
                var res = cache.del(key);
                if (res != null)
                    numDel++;
            } catch (Exception e) {
                return CommandResponse.error("ERROR Exception " + e.getMessage());
            }
        }

        return CommandResponse.success(String.valueOf("(integer) " + numDel));
    }
}
