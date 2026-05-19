package Context.client;

import Context.State;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Scanner;

public class SessionContext {
    private static final ThreadLocal<String> clientId = new ThreadLocal<>();
    private final String clientInfo;

    private State state;

    private final Scanner scanner;
    private final BufferedReader reader;
    private final PrintWriter writer;


    public SessionContext(BufferedReader reader,
                          PrintWriter writer,
                          Scanner scanner,
                          String clientInfo) {
        this.reader     = reader;
        this.writer     = writer;
        this.scanner    = scanner;
        this.clientInfo = clientInfo;
        this.state      = new Normal(this);
    }

    public void changeState(State state){
        this.state = state;
    }

    public void handle(){
        state.handle();
    }

    public Scanner getScanner(){
        return scanner;
    }

    public PrintWriter getWriter(){
        return writer;
    }

    public BufferedReader getReader(){
        return reader;
    }

    public static void set(String id) { clientId.set(id); }
    public static String get() { return clientId.get(); }
    public static void clear() { clientId.remove(); }
}
