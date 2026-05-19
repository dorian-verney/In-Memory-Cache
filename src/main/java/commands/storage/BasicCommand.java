package commands.storage;

import cache.CacheStore;
import commands.CommandResponse;

public interface BasicCommand {
    CommandResponse execute(CacheStore cache, String[] args);

}
