package commands.storage;

import cache.CacheStore;
import commands.CommandResponse;

import java.util.List;

public class KeysCommand implements BasicCommand {

    private static final int MIN_ARGS = 1;

    @Override
    public CommandResponse execute(CacheStore cache, String[] args) {
        if (args.length < MIN_ARGS)
            return CommandResponse.error("ERROR: invalid number of args (Min. KEYS)");

        List<String> value;
        try {
            value = cache.keys();
        } catch (Exception e) {
            return CommandResponse.error("ERROR Exception " + e.getMessage());
        }

        return CommandResponse.success(value);
    }
}
