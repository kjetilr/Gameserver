package common;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * TODO: USE on client!
 */
public class MessageFactory {
    public static Message createMessage(DataInputStream dataInputStream) {
        Message message = null;
        try {
            int messageTypeId = dataInputStream.readInt();
            MessageType messageType = MessageType.values()[messageTypeId];
            Constructor<? extends Message> constructor = messageType.messageClass().getConstructor();
            message = constructor.newInstance(); //Messages need no-param constructor
            message.read(dataInputStream);
        } catch (ArrayIndexOutOfBoundsException | NoSuchMethodException | InvocationTargetException |
                IllegalAccessException | InstantiationException | IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return message;
    }

}
