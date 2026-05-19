package cache;

import java.util.List;

public interface Servable
{
    String get(String key);

    String set(String key, String value, long ttlSec);

    String del(String key);

    String ttl(String key);

    String expire(String key, long ttlSec);

    List<String> keys();
}
