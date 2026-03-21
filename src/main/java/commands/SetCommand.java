package commands;

import cache.CacheStore;

public class SetCommand implements Command
{

    /**
     * @param args
     * @return
     */
    @Override
    public boolean isValid(String[] args) {
        if (args.length != 4)
            return false;
        try {
            Long.parseLong(args[3]);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * @param cache
     * @param args
     * @return
     */
    @Override
    public CommandResponse execute(CacheStore cache, String[] args) {
        if (! this.isValid(args)){
            return CommandResponse.error("ERROR invalid args for Set Command");}

        String key = args[1];
        String value = args[2];
        long ttl = Long.parseLong(args[3]);

        try {
            cache.set(key, value, ttl);
        } catch (Exception e) {
            return CommandResponse.error("ERROR Exception " + e.toString() + " for Set Command");
        }

        return CommandResponse.success(key);
    }
}
