package commands;

import cache.CacheStore;

public class GetCommand implements Command
{

    /**
     * @param args
     * @return
     */
    @Override
    public boolean isValid(String[] args)
    {
        return (args.length == 2);
    }

    /**
     * @param cache
     * @param args
     * @return
     */
    @Override
    public CommandResponse execute(CacheStore cache, String[] args) {
        if (! this.isValid(args)){
            return CommandResponse.error("ERROR invalid args for Get Command");}

        String key = args[1];
        String value;
        try {
            value = cache.get(key);
        } catch (Exception e) {
            return CommandResponse.error("ERROR Exception " + e.toString() + " for Get Command");
        }

        return CommandResponse.success(key, value);
    }
}
