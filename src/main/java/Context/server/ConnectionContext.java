package Context.server;

import Context.State;
import commands.CommandDispatcher;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class ConnectionContext {
    private State state;

    private final BufferedReader reader;
    private final PrintWriter writer;
    private final CommandDispatcher dispatcher;

    private final String clientId;
    private final String clientInfo;

    public ConnectionContext(BufferedReader reader,
                             PrintWriter writer,
                             CommandDispatcher dispatcher,
                             String clientId,
                             String clientInfo) {
        this.reader     = reader;
        this.writer     = writer;
        this.dispatcher = dispatcher;
        this.clientId   = clientId;
        this.clientInfo = clientInfo;
        this.state      = new Normal(this);
    }

    public void changeState(State state){
        this.state = state;
    }

    public void handle() {
        state.handle();
    }

    public PrintWriter getWriter(){
        return writer;
    }

    public BufferedReader getReader(){
        return reader;
    }

    public String getClientId() {
        return clientId;
    }

    public CommandDispatcher getDispatcher() {
        return dispatcher;
    }

    public String getClientInfo() {
        return clientInfo;
    }
}
