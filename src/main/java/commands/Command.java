package commands;

import cache.CacheStore;

public interface Command {
    CommandResponse execute(CacheStore cache, String[] args);

}
