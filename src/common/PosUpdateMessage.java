package common;

import javax.vecmath.Vector2d;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PosUpdateMessage extends Message {
    private static final MessageType MESSAGE_TYPE = MessageType.POS_UPDATE;
    private Vector2d position;
    private int angle;

    public PosUpdateMessage() {
    }

    public PosUpdateMessage(String id, Vector2d position,int angle) {
        this.id = id;
        this.position = position;
        this.angle = angle;
    }

    public MessageType getMessageType() {
        return MESSAGE_TYPE;
    }

    public void write(DataOutputStream outputStream) throws IOException {
        outputStream.writeUTF(id);
        outputStream.writeDouble(position.getX());
        outputStream.writeDouble(position.getY());
        outputStream.writeInt(angle);
    }

    public void read(DataInputStream inputStream) throws IOException {
        id=inputStream.readUTF();
        position=new Vector2d(inputStream.readDouble(),inputStream.readDouble());
        angle = inputStream.readInt();
    }

    @Override
    public String toString() {
        return "PosUpdateMessage{" +
                "id='" + id + '\'' +
                ", position=" + position +
                '}';
    }

    public Vector2d getPos() {
        return position;
    }

    public int getAngle() {
        return angle;
    }

    public void setPosition(Vector2d position) {
        this.position = position;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }
}
