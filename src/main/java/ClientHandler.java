import commands.CommandDispatcher;
import commands.CommandResponse;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable
{
    private final CommandDispatcher dispatcher;
    private final Socket socket;

    public ClientHandler(Socket clientSocket, CommandDispatcher dispatcher)
    {
        this.dispatcher = dispatcher;
        this.socket = clientSocket;
    }
    /**
     *
     */
    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream()), true
             )
        ){

            String input;
            while ((input = in.readLine()) != null) {
                CommandResponse response = dispatcher.dispatch(input);

                if (!response.isSuccess()) {
                    out.println(response.getMessage());
                    System.err.println("Command failed: " + response.getMessage());
                } else {
                    out.println("OK " + response.getMessage());
                    response.getValue()
                            .ifPresent(val -> out.println("VALUE " + val));
                }
            }

        } catch (IOException e) {
            System.err.println("Client déconnecté : " + e.getMessage());
        }
    }
}