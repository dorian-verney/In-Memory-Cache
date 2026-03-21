package cache;

public interface Servable
{
    String add(String key, String value, long ttlMillis);

    String get(String key);

    String set(String key, String value, long ttlMillis);

    String del(String key);
}
