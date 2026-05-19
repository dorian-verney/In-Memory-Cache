package Server.ClientStrategy;

import Server.CacheServer;
import commands.CommandResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

public class NIOSelectorServer extends CacheServer
{
    private final Set<SelectionKey> inProgress = ConcurrentHashMap.newKeySet();

    public NIOSelectorServer()
    {
        super();
    }

    record ClientState(ByteBuffer buffer, StringBuilder accumulator) {}

    @Override
    public void start(int port) {
        try (var serverChannel = ServerSocketChannel.open();
             var selector = Selector.open();
             var workers = Executors.newFixedThreadPool(2);) {
            IO.println("Server listening to: " + port);

            // INIT Server Channel
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            Set<SelectionKey> keys;
            Iterator<SelectionKey> iter;
            while (running) {
                if (selector.select() == 0){
                    continue;
                }
                keys = selector.selectedKeys();
                iter = keys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove(); // important !

                    // FIRST - ACCEPT client socket
                    if (key.isAcceptable())
                        acceptClient(selector, key);

                    // SECOND - READ client socket
                    else if (key.isReadable()) {
                        if (inProgress.add(key)) {
                            key.interestOps(0); // Ensure that same client are not handle at same the time
                            workers.execute(() -> {
                                try {
                                    readExecuteWriteClient(key);
                                } finally {
                                    inProgress.remove(key);
                                    key.interestOps(SelectionKey.OP_READ);
                                    selector.wakeup();
                                }
                            });
                        }
                    }
                }
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private void acceptClient(Selector selector, SelectionKey key){
        try {
            var channel = (ServerSocketChannel) key.channel();
            var client = channel.accept();
            var socket = client.socket();
            var clientInfo = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
            IO.println("CONNECTED : " + clientInfo);

            client.configureBlocking(false);
            // each client has its own buffer
            client.register(selector, SelectionKey.OP_READ,
                    new ClientState(ByteBuffer.allocate(1024), new StringBuilder()));

        } catch (ClassCastException e){
            throw new RuntimeException("Unknown channel " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readExecuteWriteClient(SelectionKey key){
        try {
            if (!(key.channel() instanceof SocketChannel client)) {
                return; // ignore ServerSocketChannel
            }
            var socket = client.socket();
            var clientInfo = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();

            // READ from client
            ClientState attach = (ClientState) key.attachment();
            var buffer = attach.buffer();
            var accumulator = attach.accumulator();
            var byteRead = client.read(buffer);

            if (byteRead == -1) {
                IO.println("DECONNECTED : " + clientInfo);
                socket.close();
            }

            // Append client data to accumulator
            buffer.flip();
            var data = new String(buffer.array(), 0, byteRead);
            accumulator.append(data);
            buffer.clear(); // clear it for the next client

            if (data.endsWith(";")) {
                var acc = accumulator.toString().replace(";", "");

                // this can be the heaviest task
                var out = handleServerResponse(acc, clientInfo);
                client.write(buffer.clear().put(out.getBytes()).flip());
                accumulator.setLength(0);
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }


    protected String handleServerResponse(String userCmd, String clientInfo){
        String name = "[Server] [" + Thread.currentThread().getName() + "] ";
        // Handle real command from client
        CommandResponse response = dispatcher.dispatch(userCmd);
        logger.info(name + clientInfo + "; Response : " + response.getMessage());
        if (!response.isSuccess()) {
            return response.getMessage();
        } else {
            return "OK " + response.getMessage() + " " +
                    response.getValue().map(v -> " " + v).orElse("");
        }
    }


    @Override
    public void stop(){

    }
}
