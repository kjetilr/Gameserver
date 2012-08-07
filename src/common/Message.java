package common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

public abstract class Message implements Cloneable{
    protected String id;

    public abstract MessageType getMessageType();

    public abstract void write(DataOutputStream outputStream) throws IOException;

    public abstract void read(DataInputStream dataInputStream) throws IOException;

    public String getId() {
        return id;
    }
    public Message clone() throws CloneNotSupportedException{
        return (Message) super.clone();
    }

    public void setId(String id) {
        this.id=id;
    }
}
