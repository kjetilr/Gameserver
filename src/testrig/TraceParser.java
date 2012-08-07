package testrig;

import common.Configuration;
import common.Message;
import common.MessageFactory;

import javax.xml.transform.Result;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 *
 * */
public class TraceParser {
    private Map<String, Queue<MessageInfo>> playerMap;

    public static void main(String[] args) {
        try {
            (new TraceParser()).run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void run() throws IOException {
        parseData();
        System.out.println("Number of players in trace: " + playerMap.size());
        System.out.println("ID             Number of messages");
        for (String id : playerMap.keySet()) {
            String printId = new String(id);
            while (printId.length() < 15) {
                printId += " ";
            }
            System.out.println(printId + playerMap.get(id).size());
        }
        saveData();
    }

    private void saveData() throws IOException {
        DataOutputStream ostream = new DataOutputStream(new FileOutputStream(Configuration.getInstance().getTraceFileName() + ".new"));
        boolean moreData = true;
        while (moreData) {
            long nextTime = Long.MAX_VALUE;
            String nextId = "";
            moreData = false;
            for (String id : playerMap.keySet()) {
                Queue<MessageInfo> messageInfos = playerMap.get(id);
                if (messageInfos.size() > 0 && messageInfos.peek().time < nextTime) {
                    nextId = id;
                    moreData = true;
                    nextTime = messageInfos.peek().time;
                }
            }
            if (moreData) {
                MessageInfo remove = playerMap.get(nextId).remove();
                ostream.writeLong(remove.time);
                ostream.writeInt(remove.message.getMessageType().ordinal());
                remove.message.write(ostream);
            }
        }
        ostream.flush();
        ostream.close();
    }

    public void parseData() throws IOException {
        DataInputStream stream = new DataInputStream(new FileInputStream(Configuration.getInstance().getTraceFileName()));
        playerMap = new HashMap<String, Queue<MessageInfo>>();
        while (stream.available() > 8) {
            long messageTime = stream.readLong();
            Message message = MessageFactory.createMessage(stream);
            Queue<MessageInfo> messageInfos = playerMap.get(message.getId());
            if (messageInfos == null) {
                messageInfos = new LinkedList<MessageInfo>();
                playerMap.put(message.getId(), messageInfos);
            }
            MessageInfo messageInfo = new MessageInfo(messageTime, message);
            messageInfos.add(messageInfo);
        }

        stream.close();
    }

    public Map<String, Queue<MessageInfo>> getData() {
        return playerMap;
    }

    public class MessageInfo {
        public long time;
        public Message message;

        public MessageInfo(long time, Message message) {
            this.time = time;
            this.message = message;
        }
    }
}
