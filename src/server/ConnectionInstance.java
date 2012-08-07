package server;

import common.Configuration;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 */
public class ConnectionInstance {
    private BlockingQueue<ByteBuffer> dataList;
    private NetworkWorker networkWorker;
    private SocketChannel socketChannel;

    public ConnectionInstance(SocketChannel socketChannel, NetworkSelector server) {
        this.socketChannel = socketChannel;
        this.dataList = new ArrayBlockingQueue<ByteBuffer>(Configuration.getInstance().getNetMessageQueueLength());
        networkWorker = new NetworkWorker(socketChannel, server);
    }

    public BlockingQueue<ByteBuffer> getDataList() {
        return dataList;
    }

    public NetworkWorker getNetworkWorker() {
        return networkWorker;
    }
}
