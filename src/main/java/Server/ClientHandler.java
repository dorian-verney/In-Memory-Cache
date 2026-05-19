package Server;

import Context.client.SessionContext;
import utils.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.logging.Logger;

public class ClientHandler {
    private static final Logger logger = LoggerFactory.create(ClientHandler.class, "clientHandler.log");

    private SessionContext clientContext;
    private final Scanner scanner;

    public ClientHandler(Scanner scanner){
        this.scanner = scanner;
    }


    public void start(int port){
        try (var socket = new Socket("localhost", port);
             var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             var writer = new PrintWriter(socket.getOutputStream(), true)) {
            IO.println("Connection established with port:" + port);

            var info = socket.getLocalAddress().getHostAddress()+":"+socket.getLocalPort();
            clientContext = new SessionContext(reader, writer, scanner, info);
            clientContext.handle();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void startNIO(int port){
        try (var serverChannel = SocketChannel.open()) {
            serverChannel.connect(new InetSocketAddress(port));
            IO.println("Connection established with port:" + port);
            var buffer = ByteBuffer.allocate(1024);

            String userIn;
            while (true) {
                buffer.clear();
                userIn = scanner.nextLine();
                if (userIn.equalsIgnoreCase("q")) {
                    buffer.clear().put("QUIT".getBytes()).flip();
                    break;
                }
                // WRITE data to server
                buffer.clear().put(userIn.getBytes()).flip();
                while (buffer.hasRemaining())
                    serverChannel.write(buffer);


                if (userIn.endsWith(";")) {
                    // READ data from server
                    buffer.clear();
                    var byteRead = serverChannel.read(buffer);
                    if (byteRead > 0) {
                        buffer.flip();
                        var data = new String(buffer.array(), buffer.position(), byteRead);
                        IO.println("data = " + data);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
