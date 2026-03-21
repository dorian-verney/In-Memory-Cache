package commands;

import cache.CacheStore;

public interface Command
{
    boolean isValid(String[] args);
    CommandResponse execute(CacheStore cache, String[] args);
}
