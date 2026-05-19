package Context.client;

import Context.State;

import java.io.IOException;

public class Normal implements State {

    private final SessionContext context;

    public Normal(SessionContext context){
        this.context = context;
    }

    @Override
    public void handle() {
        String userIn;
        String serverRes;
        while (true) {
            userIn = context.getScanner().nextLine();
            if (userIn.equalsIgnoreCase("q")) {
                context.getWriter().println("QUIT");
                break;
            }
            // WRITE data to server
            context.getWriter().println(userIn);

            // READ data from server
            if (userIn.endsWith(";")) {
                try {
                    serverRes = context.getReader().readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                IO.println("response : " + serverRes);

                // Transition to state Listening
                if (serverRes != null && serverRes.startsWith("subscribed")) {
                    context.changeState(new Listening(context));
                    context.handle();
                }
            }
        }
    }

}
