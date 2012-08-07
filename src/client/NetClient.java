package client;

import common.*;

import javax.vecmath.Vector2d;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;

/**
 * Not using NIO for client for simplicity
 */
public class NetClient {
    static Random random = new Random();
    private DataInputStream inputStream;
    private PlayerStatusListener playerStatusListener;
    private PosUpdateListener posUpdateListener;
    private OtherPlayerAttackedListener otherPlayerAttackedListener;
    private HitUpdateListener hitUpdateListener;

    private DataOutputStream outputStream;
    private ByteArrayOutputStream bufferByteStream = new ByteArrayOutputStream();
    private DataOutputStream dataBuffer = new DataOutputStream(bufferByteStream);
    private boolean dummy = false;
    private Socket socket;
    private boolean toDie=false;

    public void setHitUpdateListener(HitUpdateListener hitUpdateListener) {
        this.hitUpdateListener = hitUpdateListener;
    }

    public synchronized void update() {
        try {
            if (!dummy) {
                while (inputStream.available() > 0) {
                    int size = inputStream.readInt();
                    parseMessage(inputStream);
                }
            } else { //For dummy clients, just empty the stream. Do nothing.
                inputStream.skipBytes(inputStream.available());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void parseMessage(DataInputStream stream) {
        //Massive laziness. But this is not the new WoW, now is it?
        Message message = MessageFactory.createMessage(stream);
        switch (message.getMessageType()) {
            case NEW_PLAYER:
                handleNewPlayer(message);
                break;
            case POS_UPDATE:
                handleOtherPlayerPos(message);
                break;
            case HIT_UPDATE:
                hanleHitUpdate(message);
                break;
            case ATTACK:
                handleOtherPlayerAttacked(message);
                break;
            case PLAYER_LEFT:
                handleOtherPlayerLeft(message);
                break;
        }
    }

    private void handleOtherPlayerAttacked(Message message) {
        AttackMessage attackMessage = (AttackMessage) message;
        fireOtherPlayerAttacked(attackMessage.getId(), attackMessage.getAttackType());
    }

    private void fireOtherPlayerAttacked(String id, AttackMessage.AttackType attackType) {
        if (otherPlayerAttackedListener != null)
            otherPlayerAttackedListener.playerAttacked(id, attackType);
    }

    private void hanleHitUpdate(Message message) {
        HitUpdateMessage hitUpdateMessage = (HitUpdateMessage) message;
        fireHitUpdate(hitUpdateMessage.getId(), hitUpdateMessage.getHitsDealt(), hitUpdateMessage.getHitsTaken());
    }

    private void fireHitUpdate(String id, int hitsDealt, int hitsTaken) {
        if (hitUpdateListener != null) {
            hitUpdateListener.hitsUpdated(id, hitsDealt, hitsTaken);
        }
    }

    private void handleOtherPlayerPos(Message message) {
        PosUpdateMessage posUpdateMessage = (PosUpdateMessage) message;
        firePosUpdate(posUpdateMessage.getId(), posUpdateMessage.getPos(), posUpdateMessage.getAngle());
    }

    private void firePosUpdate(String playerID, Vector2d playerPos, int angle) {
        if (posUpdateListener != null) {
            posUpdateListener.playerPosUpdated(playerID, playerPos, angle);
        }
    }

    private void handleNewPlayer(Message message) {
        NewPlayerMessage newPlayerMessage = (NewPlayerMessage) message;
        fireNewPlayer(newPlayerMessage.getId(), newPlayerMessage.getPosition(), newPlayerMessage.getCharType());
    }

    private void handleOtherPlayerLeft(Message message) {
        PlayerLeftMessage playerLeftMessage = (PlayerLeftMessage) message;
        firePlayerLeft(playerLeftMessage.getId());
    }

    private void firePlayerLeft(String playerID) {
        if (playerStatusListener != null) {
            playerStatusListener.playerLeft(playerID);
        }
    }

    private void fireNewPlayer(String playerID, Vector2d position, CharType type) {
        if (playerStatusListener != null) {
            playerStatusListener.playerArrived(playerID, position, type);
        }
    }

    public void connect() throws IOException {
        int port = Configuration.getInstance().getServerPort() + (Math.abs(random.nextInt()) % Configuration.getInstance().getNumSelectors());
        System.out.println("Connecting to port: " + port);
        socket = new Socket(Configuration.getInstance().getServerURL(), port);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }

    public void setPlayerStatusListener(PlayerStatusListener playerStatusListener) {
        this.playerStatusListener = playerStatusListener;
    }

    public void setPosUpdateListener(PosUpdateListener posUpdateListener) {
        this.posUpdateListener = posUpdateListener;
    }

    public void send(Message message) {
        try {
            //This might somewhat complicated, but trust me, you don't want messages to count their own size!
            message.write(dataBuffer);
            outputStream.writeInt(bufferByteStream.size() + 8); //8 bytes header added here.
            outputStream.writeInt(message.getMessageType().ordinal());
            outputStream.write(bufferByteStream.toByteArray());
            outputStream.flush();
            bufferByteStream.reset();

        } catch (IOException e) {
            System.exit(-1);
            throw new RuntimeException(e);
        }
    }

    public void setOtherPlayerAttackedListener(OtherPlayerAttackedListener otherPlayerAttackedListener) {
        this.otherPlayerAttackedListener = otherPlayerAttackedListener;
    }

    public void setDummy() {
        dummy = true;
    }

    public synchronized void disconnect() {
        try {
            inputStream.close();
            outputStream.close();
            socket.close();
            toDie=true;
        } catch (IOException e) {
            System.out.println("Failed at disconnecting client.");
        }
    }

    public boolean isToDie() {
        return toDie;
    }
}
