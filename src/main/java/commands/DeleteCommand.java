package commands;

import cache.CacheStore;

public class DeleteCommand implements Command
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
            return CommandResponse.error("ERROR invalid args for Delete Command");}

        String key = args[1];
        try {
            cache.del(key);
        } catch (Exception e) {
            return CommandResponse.error("ERROR Exception " + e.toString() + " for Delete Command");
        }

        return CommandResponse.success(key);
    }
}
