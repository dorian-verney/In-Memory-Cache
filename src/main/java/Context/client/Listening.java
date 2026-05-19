package Context.client;

import Context.State;
import java.io.IOException;
import java.util.Set;

public class Listening implements State {
    private final SessionContext context;

    private static final Set<String> allowedCmd = Set.of("UNSUB");

    public Listening(SessionContext context){
        this.context = context;
    }

    @Override
    public void handle() {
        // Thread task only for reading new messages (unsubscribe)
        var listenerThread = Thread.ofVirtual().start(() -> {
            try {
                while (true) {
                    var incoming = context.getReader().readLine();
                    if (incoming == null) break;
                    if (incoming.equalsIgnoreCase("unsubscribed")){
                        break;
                    }
                    IO.println("Response : " + incoming);
//                    if (allowedCmd.contains(incoming.toUpperCase())) break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // thread principal read keyboard
        while (listenerThread.isAlive()) {
            String userIn = context.getScanner().nextLine();
            context.getWriter().println(userIn);
        }

        // Transition to state Normal
        context.changeState(new Normal(context));
        context.handle();
    }
}
