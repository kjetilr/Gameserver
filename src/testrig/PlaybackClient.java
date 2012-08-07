package testrig;

import client.NetClient;
import common.*;

import javax.vecmath.Vector2d;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;

/**
 *
 */
public class PlaybackClient implements Runnable {
    private Map<String, SimulatedClient> idMap = new ConcurrentHashMap<String, SimulatedClient>();
    private Random random = new Random();
    private long fakeNanoTime = 0;
    private static ScheduledExecutorService executor;
    private boolean firstRun = true;
    private Queue<TraceParser.MessageInfo> messageInfos;
    private Iterator<TraceParser.MessageInfo> messageIterator;
    private TraceParser.MessageInfo currentMessage;
    private SimulatedClient simulatedClient;
    private static TraceParser traceParser;
    private static Iterator<Queue<TraceParser.MessageInfo>> clientTracesIt;
    private static long delay = Configuration.getInstance().getLogonDelay();
    private static BlockingQueue<PlaybackClient> runningClients;
    private boolean toDie = false;

    public PlaybackClient(Queue<TraceParser.MessageInfo> messageInfos) {
        this.messageInfos = messageInfos;
        messageIterator = messageInfos.iterator();
        scheduleNext(true);
    }

    public static void main(String[] args) {
        runningClients = new LinkedBlockingDeque<PlaybackClient>();
        executor = Executors.newScheduledThreadPool(Configuration.getInstance().getNumThreads());
        createControlClient();
        createTimedKillThread();
        traceParser = new TraceParser();
        try {
            traceParser.parseData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        clientTracesIt = traceParser.getData().values().iterator();
        kickOffClients(Configuration.getInstance().getStartupClients());
        runLineInput();
    }

    private static void createControlClient() {
        final NetClient netClient = new NetClient() {
            @Override
            protected void parseMessage(DataInputStream stream) {
                Message message = MessageFactory.createMessage(stream);
                switch (message.getMessageType()) {
                    case SET_PLAYER_NUMBER:
                        SetPlayerNumberMessage playerNumberMessage = (SetPlayerNumberMessage) message;
                        PlaybackClient.setNumPlayers((playerNumberMessage.getNewNumber()));
                        break;
                }
            }
        };
        connectAndRunNetClient(netClient);
        try {
            netClient.send(new NewPlayerMessage("cotrol-client:" + InetAddress.getLocalHost().getHostName(), new Vector2d(-1, -1), CharType.Player));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setNumPlayers(int newNumber) {
        if (newNumber > runningClients.size()) {
            kickOffClients(newNumber - runningClients.size());
        } else {
            killClients(runningClients.size() - newNumber);
        }
    }

    private static void runLineInput() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.println("Enter commnad: ");
            try {
                String command = in.readLine();
                if (command.trim().length() == 0) {
                    continue;
                }
                StringTokenizer tokenizer = new StringTokenizer(command, " ");
                String token = tokenizer.nextToken();
                if (token.equalsIgnoreCase("quit")) {
                    System.exit(0);
                } else if (token.equalsIgnoreCase("startclients")) {
                    token = tokenizer.nextToken();
                    int number = 0;
                    try {
                        number = Integer.parseInt(token);
                    } catch (NumberFormatException e) {
                        System.out.println("Not an int: " + token);
                        continue;
                    }
                    System.out.println("Starting " + number + " clients.");
                    kickOffClients(number);
                } else if (token.equalsIgnoreCase("killclients")) {
                    token = tokenizer.nextToken();
                    int number = 0;
                    try {
                        number = Integer.parseInt(token);
                    } catch (NumberFormatException e) {
                        System.out.println("Not an int: " + token);
                        continue;
                    }
                    System.out.println("Killing " + number + " clients.");
                    killClients(number);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private static void killClients(int number) {
        for (int i = 0; i < number && runningClients.size() > 0; i++) {
            PlaybackClient remove = runningClients.remove();
            remove.die();
        }
    }

    private void die() {
        toDie = true;
    }

    private static void kickOffClients(int players) {
        for (int i = 0; i < players; i++) {
            if (!clientTracesIt.hasNext())
                clientTracesIt = traceParser.getData().values().iterator();
            runningClients.add(new PlaybackClient(clientTracesIt.next()));
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                //ignore!
            }
        }
    }

    private static void createTimedKillThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(Configuration.getInstance().getKillTime()));
                    System.out.println("Quiting due to killtime parameter!");
                    System.exit(0);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        }).start();
    }

    public void run() {

        if (simulatedClient == null) {
            NetClient netClient = new NetClient();
            netClient.setDummy();
            connectAndRunNetClient(netClient);
            simulatedClient = new SimulatedClient("dummy" + random.nextInt(), netClient);
        }

        if (!messageIterator.hasNext()) {
            firstRun = false;
            messageIterator = messageInfos.iterator();
            scheduleNext(true);
            return;
        }
        Message nextMessage = null;
        try {
            nextMessage = currentMessage.message.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        nextMessage.setId(simulatedClient.getId());
        if (firstRun || nextMessage.getMessageType() != MessageType.NEW_PLAYER) {
            simulatedClient.getNetClient().send(nextMessage);
        }

        long delay = currentMessage.time - getFakeNanoTime();
        if (-delay > TimeUnit.MILLISECONDS.toNanos(10)) {
            System.out.println("Playback delayed! " + TimeUnit.NANOSECONDS.toMillis(-delay));
        }

        scheduleNext(false);
    }

    private void scheduleNext(boolean resetTimer) {
        if (toDie) {
            simulatedClient.getNetClient().disconnect();
        } else {
            currentMessage = messageIterator.next();
            if (resetTimer)
                fakeNanoTime = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()) - currentMessage.time;
            long delay = currentMessage.time - getFakeNanoTime();
            executor.schedule(this, delay, TimeUnit.NANOSECONDS);
        }
    }

    private long getFakeNanoTime() {
        long elapsed = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()) - fakeNanoTime;
        return elapsed;
    }

    private static NetClient connectAndRunNetClient(final NetClient netClient) {
        try {
            tryConnect(netClient);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        executor.execute(new Runnable() {
            public void run() {
                if (!netClient.isToDie()) {
                    netClient.update();
                    executor.schedule(this, 50, TimeUnit.MILLISECONDS);
                }
            }
        });
        return netClient;
    }

    private static void tryConnect(NetClient netClient) throws IOException {
        String paramValue = System.getProperty("playback.maxRetryTime");
        long maxRetryTime = 1;
        if (paramValue != null) {
            maxRetryTime = Long.parseLong(paramValue);
        }
        long retryTime = 1;
        IOException exception = null;
        while (retryTime <= maxRetryTime) {
            try {
                netClient.connect();

            } catch (IOException e) {
                exception = e;
                try {
                    System.out.println("Connection failed. Trying again in: " + retryTime + "s.");
                    Thread.sleep(TimeUnit.SECONDS.toMillis(retryTime));
                    retryTime *= 2;
                    continue;
                } catch (InterruptedException e1) {
                    //ignore
                }
            }
            retryTime = Long.MAX_VALUE;
        }
        if (retryTime != Long.MAX_VALUE) {
            throw exception;
        }
    }

    private static class SimulatedClient {
        private String id;
        private NetClient netClient;

        public SimulatedClient(String id, NetClient netClient) {
            this.id = id;
            this.netClient = netClient;
        }

        public String getId() {
            return id;
        }

        public NetClient getNetClient() {
            return netClient;
        }
    }
}
