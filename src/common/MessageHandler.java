package common;

import server.NetworkSelector;

import java.nio.channels.SocketChannel;

/**

 */
public interface MessageHandler {
    void execute(Message message, SocketChannel channel, NetworkSelector server);
}
