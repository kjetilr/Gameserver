package server;

import common.CharType;
import common.HitUpdateMessage;
import common.NewPlayerMessage;

import javax.vecmath.Vector2d;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

/**
 */
public class PlayerCharacter extends AbstractCharacter {

    private NetworkWorker networkWorker;
    private Random randomGenerator = new Random();
    private Float synthLoadSeed = randomGenerator.nextFloat();
    private int synthLoadSum = 0;

    public PlayerCharacter(String id, Vector2d position, NetworkWorker networkWorker) {
        super(position, id);

        this.networkWorker = networkWorker;
        updateClientAboutOthers(GameWorld.getInstance().getPlayerCharacters());
        updateClientAboutOthers(GameWorld.getInstance().getNPCs());
        updateOthersAboutMe();
    }

    private void updateClientAboutOthers(Map<String, ? extends AbstractCharacter> gameCharacters) {
        for (AbstractCharacter abstractCharacter : gameCharacters.values()) {
            NewPlayerMessage message = new NewPlayerMessage(abstractCharacter.getId(), abstractCharacter.getPosition(), abstractCharacter.getCharType());
            networkWorker.send(message);
        }
    }


    @Override
    protected CharType getCharType() {
        return CharType.Player;
    }

    /**
     * Will be run periodically.
     */
    @Override
    public void run() {
        if (!toDie) {
            updateAndReschedule();
            networkWorker.handleOutgoingMessages();
            syntheticLoad();
        } else {
            GameWorld.getInstance().removePlayer(this);
            System.out.println("Player left. ID: " + getId());
        }
    }

    private void syntheticLoad() {
        ArrayList<Float> vec = ServerMain.getSynthLoadVector();

        int posIdx;
        posIdx = (int) mover.getPosition().getX() + (int) mover.getPosition().getY() * 50000;

        float sum = 0;
        for (int i = 0; i < ServerMain.getSynthLoad(); i++) {
            float floatVal = vec.get((i + posIdx) % vec.size());
            sum += (floatVal * floatVal * synthLoadSeed);
        }
        synthLoadSum += sum;
    }

    @Override
    public void addHitTaken() {
        hitsTaken++;
        sendHitMessageToClient();
    }

    @Override
    public void addHitDealt() {
        hitDealt++;
        sendHitMessageToClient();
    }

    private void sendHitMessageToClient() {
        HitUpdateMessage message = new HitUpdateMessage(getId(), hitDealt, hitsTaken);
        networkWorker.send(message);
    }

    public NetworkWorker getNetworkWorker() {
        return networkWorker;
    }

    public float getTargetValue(NPC npc) {
        if (toDie) {
            return Float.MAX_VALUE;
        }
        float value = 0;
        Vector2d distance = new Vector2d(getPosition());
        distance.sub(npc.getPosition());
        value += distance.length() * AIData.getDistanceWeight();
        return value;
    }
}
