package common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 */
public class HitUpdateMessage extends Message {
    private static final MessageType MESSAGE_TYPE = MessageType.HIT_UPDATE;
    private int hitsDealt;
    private int hitsTaken;

    public HitUpdateMessage() {
    }

    public int getHitsDealt() {
        return hitsDealt;
    }

    public int getHitsTaken() {
        return hitsTaken;
    }

    public HitUpdateMessage(String id, int hitsDealt, int hitsTaken) {
        this.id = id;
        this.hitsDealt = hitsDealt;
        this.hitsTaken = hitsTaken;
    }

    public MessageType getMessageType() {
        return MESSAGE_TYPE;
    }

    public void write(DataOutputStream outputStream) throws IOException {
        outputStream.writeUTF(id);
        outputStream.writeInt(hitsDealt);
        outputStream.writeInt(hitsTaken);
    }

    public void read(DataInputStream inputStream) throws IOException {
        id = inputStream.readUTF();
        hitsDealt = inputStream.readInt();
        hitsTaken = inputStream.readInt();
    }

    @Override
    public String toString() {
        return "HitUpdateMessage{" +
                "id='" + id + '\'' +
                ", hitsDealt=" + hitsDealt +
                ", hitsTaken=" + hitsTaken +
                '}';
    }
}
