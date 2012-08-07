package server;

import common.Message;
import common.MessageHandler;
import common.PlayerLeftMessage;

import java.nio.channels.SocketChannel;

public class PlayerLeftHandler implements MessageHandler {
    @Override
    public void execute(Message message, SocketChannel channel, NetworkSelector server) {
        PlayerLeftMessage playerLeftMessage = (PlayerLeftMessage) message;
        PlayerCharacter playerCharacter = GameWorld.getInstance().getGameCharacter(playerLeftMessage.getId());
        playerCharacter.die();
    }
}
