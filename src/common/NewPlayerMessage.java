package common;

import javax.vecmath.Vector2d;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NewPlayerMessage extends Message {
    private Vector2d position;
    private CharType charType;

    public NewPlayerMessage() {
    }

    public NewPlayerMessage(String id, Vector2d position, CharType charType) {
        this.charType = charType;
        this.id = id;
        this.position = position;
    }

    public MessageType getMessageType() {
        return MessageType.NEW_PLAYER;
    }

    public void write(DataOutputStream outputStream) throws IOException {
        outputStream.writeUTF(id);
        outputStream.writeDouble(position.getX());
        outputStream.writeDouble(position.getY());
        outputStream.writeInt(charType.ordinal());
    }

    public void read(DataInputStream inputStream) throws IOException {
        id = inputStream.readUTF();
        position = new Vector2d(inputStream.readDouble(), inputStream.readDouble());
        charType = CharType.values()[inputStream.readInt()];
    }

    public Vector2d getPosition() {
        return position;
    }

    public CharType getCharType() {
        return charType;
    }
}
