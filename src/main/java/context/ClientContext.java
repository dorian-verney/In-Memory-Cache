package context;

public class ClientContext {
    private static final ThreadLocal<String> clientId = new ThreadLocal<>();

    private ClientContext() {} // prevent instantiation

    public static void set(String id) { clientId.set(id); }
    public static String get() { return clientId.get(); }
    public static void clear() { clientId.remove(); }
}
