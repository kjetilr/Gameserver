package server;

import common.Configuration;
import common.RealtimeGameTimer;
import common.TimeSystem;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * This code is taken from http://rox-xmlrpc.sourceforge.net/niotut/
 * which is without a copyright notice.
 */
public class NetworkSelector implements Runnable {

    // The host:port combination to listen on
    private InetAddress hostAddress;
    private int port;

    // The channel on which we'll accept connections
    private ServerSocketChannel serverChannel;

    // The selector we'll be monitoring
    private Selector selector;

    // The buffer into which we'll read data when it's available
    private ByteBuffer readBuffer = ByteBuffer.allocate(8192);

    // A list of PendingChange instances
    private BlockingQueue<ChangeRequest> pendingChanges = new ArrayBlockingQueue<>(Configuration.getInstance().getChangeReqQueueLength());

    // Maps a SocketChannel to a list of ByteBuffer instances
    private final ConcurrentMap<SocketChannel, ConnectionInstance> connections = new ConcurrentHashMap<>();
    private static final long SEND_TIMEOUT = 10;

    public NetworkSelector(InetAddress hostAddress, int port) throws IOException {
        this.hostAddress = hostAddress;
        this.port = port;
        this.selector = initSelector();
        final RealtimeGameTimer gameTimer = new RealtimeGameTimer();
        TimeSystem.setInstance(gameTimer);
        ServerMain.getExecutor().schedule(new Runnable() {
            public void run() {
                gameTimer.update();
                ServerMain.getExecutor().schedule(this, 100, TimeUnit.MILLISECONDS);
            }
        }, 100, TimeUnit.MILLISECONDS);

    }


    public void send(SocketChannel socket, byte[] data) {
        // Indicate we want the interest ops set changed
        try {
            if (!pendingChanges.offer(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE), SEND_TIMEOUT, TimeUnit.MILLISECONDS)) {
                pendingChanges.clear();
                ServerMain.getStatisticsSystem().addSendTimeout();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // And queue the data we want written
        ConnectionInstance queue = connections.get(socket);
        try {
            if (!queue.getDataList().offer(ByteBuffer.wrap(data), SEND_TIMEOUT, TimeUnit.MILLISECONDS))
                queue.getDataList().clear();
            ServerMain.getStatisticsSystem().addSendTimeout();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Finally, wake up our selecting thread so it can make the required changes
        // Selecting thread will only ever sleep 50ms now. Saves us this call which blocks, and is called from a lot of places.
        //selector.wakeup();
    }

    /**
     *
     */
    public void run() {
        // Process any pending changes
        while (!pendingChanges.isEmpty()) {
            ChangeRequest change = pendingChanges.remove();
            switch (change.type) {
                case ChangeRequest.CHANGEOPS:
                    SelectionKey key = change.socket.keyFor(selector);
                    if (key == null || !key.isValid()) {
                        continue;
                    }
                    key.interestOps(change.ops);
            }
        }

        // Wait for an event one of the registered channels
        try {
            selector.select(50);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Iterate over the set of keys for which events are available
        Iterator selectedKeys = selector.selectedKeys().iterator();
        while (selectedKeys.hasNext()) {
            SelectionKey key = (SelectionKey) selectedKeys.next();
            selectedKeys.remove();

            if (!key.isValid()) {
                continue;
            }

            // Check what event is available and deal with it
            try {
                if (key.isAcceptable()) {
                    accept(key);
                } else if (key.isReadable()) {
                    read(key);
                } else if (key.isWritable()) {
                    write(key);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }

        }
        ServerMain.getExecutor().schedule(this, 10, TimeUnit.NANOSECONDS);
    }

    private void accept(SelectionKey key) throws IOException {
        // For an accept to be pending the channel must be a server socket channel.
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        // Accept the connection and make it non-blocking
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        connections.put(socketChannel, new ConnectionInstance(socketChannel, this));
        // Register the new SocketChannel with our Selector, indicating
        // we'd like to be notified when there's data waiting to be read
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        // Clear out our read buffer so it's ready for new data
        readBuffer.clear();

        // Attempt to read off the channel
        int numRead;
        try {
            numRead = socketChannel.read(readBuffer);
        } catch (IOException e) {
            // The remote forcibly closed the connection, cancel
            // the selection key and close the channel.
            disconnect(key);
            return;
        }

        if (numRead == -1) {
            // Remote entity shut the socket down cleanly. Do the
            // same from our end and cancel the channel.
            disconnect(key);
            return;
        }

        sendToExecutor(socketChannel, numRead);
    }

    private void disconnect(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        key.cancel();
        socketChannel.close();
        connections.get(socketChannel).getNetworkWorker().die();
        connections.remove(socketChannel);
    }

    private void sendToExecutor(SocketChannel socketChannel, int numRead) {
        ConnectionInstance connectionInstance = connections.get(socketChannel);
        NetworkWorker networkWorker = connectionInstance.getNetworkWorker();
        networkWorker.processData(readBuffer.array(), numRead);
        ServerMain.getExecutor().execute(networkWorker);
    }

    private void write(SelectionKey key) throws IOException {

        synchronized (connections) {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            Queue<ByteBuffer> queue = connections.get(socketChannel).getDataList();
            try {

                // Write until there's not more data ...
                while (!queue.isEmpty() && socketChannel != null) {
                    ByteBuffer buf = queue.remove();
                    socketChannel.write(buf);
                    if (buf.remaining() > 0) {
                        // ... or the socket's buffer fills up
                        break;
                    }
                }

                if (queue.isEmpty()) {
                    // We wrote away all data, so we're no longer interested
                    // in writing on this socket. Switch back to waiting for
                    // data.
                    key.interestOps(SelectionKey.OP_READ);
                }
            } catch (IOException e) {
                disconnect(key);
            }
        }
    }

    private Selector initSelector() throws IOException {
        // Create a new selector
        Selector socketSelector = SelectorProvider.provider().openSelector();

        // Create a new non-blocking server socket channel
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // Bind the server socket to the specified address and port
        InetSocketAddress isa = new InetSocketAddress(hostAddress, port);
        try {
            serverChannel.socket().bind(isa);
        } catch (BindException e) {
            System.out.println("Socket already in use. Previous server instance running?");
            System.exit(-1);
        }
        // Register the server socket channel, indicating an interest in
        // accepting new connections
        serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

        return socketSelector;
    }

    ConnectionInstance getConnectionInstance(SocketChannel channel) {
        return connections.get(channel);
    }

    public int getQueueLength() {
        return pendingChanges.size();
    }
}
