package common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 */
public class AttackMessage extends Message {
    private int angle;
    private AttackType type;



    public enum AttackType{
        ARC_ATTACK,
        PROJECTILE_ATTACK;
    }

    public int getAngle() {
        return angle;
    }

    public AttackType getAttackType() {
        return type;
    }


    public AttackMessage() {
    }

    public MessageType getMessageType() {
        return MessageType.ATTACK;
    }

    public AttackMessage(String id, int angle, AttackType type) {
        this.id = id;
        this.angle = angle;
        this.type = type;
    }

    public void write(DataOutputStream outputStream) throws IOException {
        outputStream.writeUTF(id);
        outputStream.writeInt(angle);
        outputStream.writeInt(type.ordinal());
    }

    public void read(DataInputStream dataInputStream) throws IOException {
        id = dataInputStream.readUTF();
        angle = dataInputStream.readInt();
        type = AttackType.values()[dataInputStream.readInt()];
    }
}
