package server;

import common.*;

import javax.vecmath.Vector2d;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public abstract class AbstractCharacter implements Runnable {
    protected Mover mover = new Mover();
    protected String id;
    protected BlockingQueue<Runnable> messages = new ArrayBlockingQueue<Runnable>(Configuration.getInstance().getMessageQueueLength());
    protected int hitsTaken = 0;
    protected int hitDealt = 0;
    private Vector2d distanceToOther = new Vector2d();  //Cache for spam-avoidance.
    private int numCollisions = 0;
    protected PosUpdateMessage posUpMessage;
    protected long lastRun;
    protected boolean toDie = false;

    public AbstractCharacter(Vector2d position, String id) {
        mover.setPosition(position);
        this.id = id;
        posUpMessage = new PosUpdateMessage(getId(), getPosition(), getAngle());
        lastRun = System.currentTimeMillis();
    }

    protected void updateOthersAboutMe() {
        Message message = new NewPlayerMessage(getId(), getPosition(), getCharType());
        sendMessageToAllOthers(message);
    }

    protected abstract CharType getCharType();

    protected void sendMessageToAllOthers(final Message message) {
        Map<String, PlayerCharacter> gameCharacters = GameWorld.getInstance().getPlayerCharacters();
        for (PlayerCharacter playerCharacter1 : gameCharacters.values()) {
            if (playerCharacter1 != this) {
                final PlayerCharacter playerCharacter = playerCharacter1;
                playerCharacter.sendMessage(new Runnable() {
                    public void run() {
                        playerCharacter.getNetworkWorker().send(message);
                    }
                });
            }

        }
    }

    public void sendMessage(Runnable runnable) {
        try {
            if (!messages.offer(runnable, 10, TimeUnit.MILLISECONDS)) {
                messages.clear();
                ServerMain.getStatisticsSystem().addSendTimeout();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("This should not happen.");
        }
    }

    public void updatePosition(Vector2d position, int angle) {
        mover.setAngle(angle);
        for (AbstractCharacter character : GameWorld.getInstance().getPlayerCharacters().values()) {
            if (character != this) {
                distanceToOther.sub(character.getPosition(), position);
                if (distanceToOther.length() < AttackData.getPlayerDiameter()) {
                    ++numCollisions;
                    //networkWorker.send(new PosUpdateMessage(getId(), getPosition(), getAngle()));
                    //return;
                }
            }
        }

        mover.setPosition(position);
        posUpMessage.setPosition(getPosition());
        posUpMessage.setAngle(getAngle());
        sendMessageToAllOthers(posUpMessage);
    }

    public String getId() {
        return id;
    }

    public abstract void run();

    /**
     * Stuff that needs to be run in this characters "thread".
     * This should possibly be limited, so it will not run forever.
     * Leaving messages in this queue is not a problem. This
     * will happen if a posUpMessage is added between isEmpty() and take(),
     * or if we are somehow interrupted.
     */
    protected void processMessages() {
        while (!messages.isEmpty()) {
            try {
                messages.take().run();
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public Vector2d getPosition() {
        return mover.getPosition();
    }

    public abstract void addHitTaken();

    public abstract void addHitDealt();

    public int getAngle() {
        return mover.getAngle();
    }

    protected void updateAndReschedule() {
        long curtime = System.currentTimeMillis();
        long diff = curtime - lastRun;
        ServerMain.getStatisticsSystem().addTimeDelay(diff, getId(), messages.size());
        lastRun = curtime;
        ServerMain.getExecutor().schedule(this, Configuration.getInstance().getUpdateInterval(), TimeUnit.MILLISECONDS);
        processMessages();
    }

    public void die() {
        updateOthersAboutMeLeaving();
        toDie = true;
    }

    private void updateOthersAboutMeLeaving() {
        Message message = new PlayerLeftMessage(getId());
        sendMessageToAllOthers(message);
    }
}
