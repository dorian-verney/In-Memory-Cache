package context.server;

import context.State;
import commands.CommandResponse;
import commands.ResponseType;
import utils.LoggerFactory;

import java.io.IOException;
import java.util.logging.Logger;

public class Normal implements State {

    private static final Logger logger = LoggerFactory.create(Normal.class, "server.log");
    private final ConnectionContext context;

    public Normal(ConnectionContext context){
        this.context = context;
    }

    @Override
    public void handle() {
        String name = "[Server] [" + Thread.currentThread().getName() + "] ";

        listening:
        while (true){
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
                IO.println(sb);
            } while (!in.endsWith(";"));


            CommandResponse response = context.getDispatcher().dispatch(sb.toString());

            logger.info(name + context.getClientInfo() + "; Response : " + response.getMessage());

            if (response.getResponseType() == ResponseType.SUBSCRIBE_MODE) {
                context.getWriter().println("subscribed " + response.getMessage());
                context.changeState(new Subscribe(context));
                context.handle();

            } else {
                if (!response.isSuccess()) {
                    context.getWriter().println(response.getMessage());
                } else {
                    context.getWriter().println("OK " + response.getMessage() +
                            response.getValue().map(v -> " " + v).orElse(""));
                }
            }
        }
    }
}
