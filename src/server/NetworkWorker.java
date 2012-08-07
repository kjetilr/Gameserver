package server;

import common.Configuration;
import common.Message;
import common.MessageFactory;

import java.io.*;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This code is taken from http://rox-xmlrpc.sourceforge.net/niotut/
 * which is without a copyright notice.
 */
public class NetworkWorker implements Runnable {
    public static final int INT_SIZE = Integer.SIZE / Byte.SIZE;
    private static final int MAX_BUFFER_SIZE = 256 * 1024;
    private BlockingQueue<ServerDataEvent> queue = new ArrayBlockingQueue<ServerDataEvent>(Configuration.getInstance().getNetMessageQueueLength());
    private ByteArrayOutputStream inDataBuffer = new ByteArrayOutputStream();
    private ByteArrayOutputStream outDataBuffer = new ByteArrayOutputStream();
    private DataOutputStream outDataStream = new DataOutputStream(this.outDataBuffer);
    private ByteArrayOutputStream finalOutDataBuffer = new ByteArrayOutputStream(MAX_BUFFER_SIZE);
    private DataOutputStream finalOutDataStream = new DataOutputStream(this.finalOutDataBuffer);
    private int packetSize = 0;
    private SocketChannel socketChannel;
    private NetworkSelector server;
    private List<DieListener> dieListeners = new LinkedList<DieListener>();

    public NetworkWorker(SocketChannel socketChannel, NetworkSelector server) {
        this.socketChannel = socketChannel;
        this.server = server;
        System.out.println("Constructing NetworkWorker!");
    }

    public void processData(byte[] data, int count) {
        byte[] dataCopy = new byte[count];
        System.arraycopy(data, 0, dataCopy, 0, count);
        try {
            queue.put(new ServerDataEvent(server, socketChannel, dataCopy));
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public synchronized void run() {
        handleIncomingMessages();
    }

    public void handleOutgoingMessages() {
        if (finalOutDataBuffer.size() > 0) {
            server.send(socketChannel, finalOutDataBuffer.toByteArray());
            finalOutDataBuffer.reset();
        }
    }

    private void handleIncomingMessages() {
        ServerDataEvent dataEvent;
        // Wait for data to become available
        try {
            dataEvent = queue.take();
        } catch (InterruptedException e) {
            return;
        }

        //Build a complete message before parsing
        try {
            inDataBuffer.write(dataEvent.data);
            int leftInBuffer = inDataBuffer.size();
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(inDataBuffer.toByteArray()));
            while (leftInBuffer >= INT_SIZE) {
                if (packetSize == 0) {
                    packetSize = dataInputStream.readInt();
                    leftInBuffer -= INT_SIZE;
                }
                if (leftInBuffer >= packetSize - INT_SIZE) {
                    Message message = handleMessage(dataInputStream);
                    if (Configuration.getInstance().isLoggingData()) {
                        ServerMain.logMessage(message);
                    }
                    leftInBuffer -= (packetSize - INT_SIZE);
                    packetSize = 0;
                } else break;
            }
            if (leftInBuffer > 0) {
                byte[] leftovers = new byte[leftInBuffer];
                dataInputStream.read(leftovers);
                inDataBuffer.reset();
                inDataBuffer.write(leftovers);
            } else
                inDataBuffer.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Determine message type, and create a message of the correct type using reflection.
     * Then let the message object read it's values from the stream.
     *
     * @param dataInputStream
     */
    private Message handleMessage(DataInputStream dataInputStream) {
        Message message = MessageFactory.createMessage(dataInputStream);
        message.getMessageType().getHandler().execute(message, socketChannel, server);
        return message;
    }

    public void send(Message message) {
        try {
            //This might somewhat complicated, but trust me, you don't want messages to count their own size!
            if (finalOutDataBuffer.size() > MAX_BUFFER_SIZE) {
                finalOutDataBuffer.reset();
                ServerMain.getStatisticsSystem().addSendTimeout();
                return;
            }
            message.write(outDataStream);
            finalOutDataStream.writeInt(outDataBuffer.size() + 8); //8 bytes header added here.
            finalOutDataStream.writeInt(message.getMessageType().ordinal());
            outDataBuffer.writeTo(finalOutDataStream);
            finalOutDataStream.flush();
            outDataBuffer.reset();

        } catch (IOException e) {
            System.exit(-1);
            throw new RuntimeException(e);
        }
    }

    public void addDieListener(DieListener dieListener) {
        dieListeners.add(dieListener);
    }

    public void die() {
        for (DieListener dieListener : dieListeners) {
            dieListener.die();
        }
    }

    public int getMessageQueueSize() {
        return queue.size();
    }
}
