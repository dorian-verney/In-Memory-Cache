package distrib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NodeConnection {

    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;

    public NodeConnection(String host, int port) throws IOException {
        this.socket = new Socket(host, port); // connexion immédiate
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
    }


    public void deconnect() throws IOException {
        this.socket.close();
    }

    public String sendAndReceive(String command) throws IOException {
        writer.println(command);
        IO.println("coté NODE connection jai envoyé " + command);
        return reader.readLine();
    }
}