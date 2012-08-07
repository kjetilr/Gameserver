package common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlayerLeftMessage extends Message {

    public PlayerLeftMessage() {
    }

    public PlayerLeftMessage(String id) {
        this.id = id;
    }

    public MessageType getMessageType() {
        return MessageType.PLAYER_LEFT;
    }

    public void write(DataOutputStream outputStream) throws IOException {
        outputStream.writeUTF(id);

    }

    public void read(DataInputStream dataInputStream) throws IOException {
        id = dataInputStream.readUTF();
    }
}
