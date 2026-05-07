package Server.ClientStrategy;

import Server.CacheServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NIOSelectorServer extends CacheServer
{
    public NIOSelectorServer()
    {
        super();
    }

    record ClientState(ByteBuffer buffer, StringBuilder accumulator) {}

    @Override
    public void start(int port) {
        try (var serverChannel = ServerSocketChannel.open();
             var selector = Selector.open();) {

            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.configureBlocking(false);

            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            Set<SelectionKey> keys;
            Iterator<SelectionKey> iter;

            IO.println("Server listening to: " + port);

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
                    if (key.isAcceptable()) {
                        if (key.channel() instanceof ServerSocketChannel channel) {
                            var client = channel.accept();
                            var socket = client.socket();
                            var clientInfo = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
                            IO.println("CONNECTED : " + clientInfo);

                            client.configureBlocking(false);
                            // each client has its own buffer
                            client.register(selector, SelectionKey.OP_READ,
                                    new ClientState(ByteBuffer.allocate(1024), new StringBuilder()));
                        } else {
                            throw new RuntimeException("Unknown channel");
                        }

                    // SECOND - READ client socket
                    } else {
                        if (key.isReadable()) {
                            if (key.channel() instanceof SocketChannel client) {
                                var socket = client.socket();
                                var clientInfo = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();

                                ClientState attach = (ClientState) key.attachment();
                                var buffer = attach.buffer();
                                var accumulator = attach.accumulator();
                                var byteRead = client.read(buffer);

                                if (byteRead == -1) {
                                    IO.println("DECONNECTED : " + clientInfo);
                                    socket.close();
                                }

                                buffer.flip();
                                var data = new String(buffer.array(), 0, byteRead);
                                accumulator.append(data);
                                buffer.clear(); // clear it for the next client

                                if (data.endsWith(";")) {
                                    var acc = accumulator.toString().replace(";", "");
                                    var out = handleServerResponse(acc, clientInfo);
                                    client.write(buffer.clear().put(out.getBytes()).flip());
                                    accumulator.setLength(0);
                                }
                            } else {
                                throw new RuntimeException("Unknown channel");
                            }
                        }
                    }

                }
            }

        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop(){

    }
}
