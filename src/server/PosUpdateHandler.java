package server;

import common.Message;
import common.MessageHandler;
import common.PosUpdateMessage;

import java.nio.channels.SocketChannel;

/**

 */
public class PosUpdateHandler implements MessageHandler {
    public void execute(Message message, SocketChannel channel, NetworkSelector server) {
        final PosUpdateMessage posUpdateMessage = (PosUpdateMessage) message;
        final AbstractCharacter character = GameWorld.getInstance().getGameCharacter(posUpdateMessage.getId());
        character.sendMessage(new Runnable() {
            @Override
            public void run() {
                character.updatePosition(posUpdateMessage.getPos(), posUpdateMessage.getAngle());
            }
        });
    }
}
