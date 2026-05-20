package context.server;

import context.State;
import context.client.SessionContext;
import commands.CommandResponse;
import commands.ResponseType;
import utils.LoggerFactory;

import java.io.IOException;
import java.util.logging.Logger;

public class Subscribe implements State {

    private static final Logger logger = LoggerFactory.create(Subscribe.class, "server.log");
    private final ConnectionContext context;

    public Subscribe(ConnectionContext context){
        this.context = context;
    }

    @Override
    public void handle() {
        try {
            listening:
            while (true) {
                // POLLING QUEUE
                String msg = context.getDispatcher().getBroker()
                                .poll(SessionContext.get(), 200);

                if (msg != null) {
                    context.getWriter().println(msg);
                }

                // window to read UNSUBSCRIBE ou QUIT
                if (context.getReader().ready()) {
                    var sb = new StringBuilder();
                    String in;
                    do {
                        try {
                            in = context.getReader().readLine();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        if (in == null || in.equals("QUIT")) break listening;

                        sb.append(in.replace(";", ""));
                    } while (!in.endsWith(";"));

                    CommandResponse response = context.getDispatcher().dispatch(sb.toString());
                    if (response.getResponseType() == ResponseType.NORMAL) {
                        IO.println("UNNNNSBBSB");
                        context.getWriter().println("Unsubscribed " + response.getMessage());
                        context.changeState(new Normal(context));
                        context.handle();
                    }
                }
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}