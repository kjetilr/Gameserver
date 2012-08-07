package testrig;

import common.Message;
import common.MessageType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SetPlayerNumberMessage extends Message {
    private int newNumber;

    public SetPlayerNumberMessage() {
    }

    public SetPlayerNumberMessage(int newNumber) {
        this.newNumber = newNumber;

    }

    @Override
    public MessageType getMessageType() {
        return MessageType.SET_PLAYER_NUMBER;
    }

    @Override
    public void write(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(newNumber);

    }

    @Override
    public void read(DataInputStream dataInputStream) throws IOException {
        newNumber = dataInputStream.readInt();
    }

    public int getNewNumber() {
        return newNumber;
    }
}
