package server;

import context.client.SessionContext;
import io.ClientIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class ClientHandler {

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private SessionContext clientContext;
    private final ClientIO clientIO;

    public ClientHandler(ClientIO clientIO){
        this.clientIO = clientIO;
    }


    public void start(int port){
        try {
            var socket = new Socket();
            socket.setReuseAddress(true);
            socket.connect(new InetSocketAddress("127.0.0.1", port));

            try (socket;
                 var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 var writer = new PrintWriter(socket.getOutputStream(), true)) {

                //IO.println("Connection established with port:" + port);
                var info = socket.getLocalAddress().getHostAddress() + ":" + socket.getLocalPort();
                clientContext = new SessionContext(reader, writer, clientIO, info);
                clientContext.handle();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void connect(int port) throws IOException {
        socket = new Socket();
        socket.setReuseAddress(true);
        socket.connect(new InetSocketAddress("127.0.0.1", port));
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);

        IO.println("Connection established with port:" + port);
        var info = socket.getLocalAddress().getHostAddress() + ":" + socket.getLocalPort();
        clientContext = new SessionContext(reader, writer, clientIO, info);
        clientContext.handle();

    }

    public void sendCommand(String cmd) {
        clientContext.getWriter().println(cmd);
    }

    public String readResponse() throws IOException {
        return clientContext.getReader().readLine();
    }

    public void disconnect() throws IOException {
        writer.println("QUIT");
        socket.close();
    }


    /////// NIO //////


    public void startNIO(int port){
        try (var serverChannel = SocketChannel.open()) {
            serverChannel.connect(new InetSocketAddress(port));
            IO.println("Connection established with port:" + port);
            var buffer = ByteBuffer.allocate(1024);

            String userIn;
            while (true) {
                buffer.clear();
                userIn = clientIO.readInput();
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
//                        IO.println("data = " + data);
                        clientIO.writeOutput("data = " + data);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
