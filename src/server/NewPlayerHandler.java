package server;

import common.Configuration;
import common.Message;
import common.MessageHandler;
import common.NewPlayerMessage;

import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

/**
 */
public class NewPlayerHandler implements MessageHandler {
    public void execute(Message message, SocketChannel channel, NetworkSelector server) {
        NewPlayerMessage newPlayerMessage = (NewPlayerMessage) message;
        if (newPlayerMessage.getPosition().getX() >= 0) {
            createNormalPlayer(channel, server, newPlayerMessage);
        } else {
            createController(channel, server, newPlayerMessage);
        }

    }

    private void createController(SocketChannel channel, NetworkSelector server, NewPlayerMessage newPlayerMessage) {
        NetworkWorker networkWorker = server.getConnectionInstance(channel).getNetworkWorker();
        final ClientSimulator simulator=new ClientSimulator(newPlayerMessage.getId(),networkWorker);
        ServerMain.addClientSimulator(simulator);
        System.out.println("Client Simulator logged on: " + newPlayerMessage.getId());

    }

    private void createNormalPlayer(SocketChannel channel, NetworkSelector server, NewPlayerMessage newPlayerMessage) {
        if (GameWorld.getInstance().getGameCharacter(newPlayerMessage.getId()) != null)
            throw new RuntimeException("Player added twice!");
        NetworkWorker networkWorker = server.getConnectionInstance(channel).getNetworkWorker();
        final PlayerCharacter playerCharacter = new PlayerCharacter(newPlayerMessage.getId(), newPlayerMessage.getPosition(), networkWorker);
        networkWorker.addDieListener(new DieListener() {
            public void die() {
                playerCharacter.die();
            }
        });
        GameWorld.getInstance().addPlayer(playerCharacter);
        System.out.println("Player created: " + newPlayerMessage.getId() + " (" + GameWorld.getInstance().getPlayerCharacters().size() + ")");
        ServerMain.getExecutor().schedule(playerCharacter, Configuration.getInstance().getUpdateInterval(), TimeUnit.MILLISECONDS);
    }
}
