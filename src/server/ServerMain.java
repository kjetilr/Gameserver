package server;

import common.Configuration;
import common.Message;
import common.MessageType;
import common.TimeSystem;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 *
 */
public class ServerMain {
    //These three need to be static and unique.
    private static ScheduledExecutorService executor;
    private static StatisticsSystem statisticsSystem = new StatisticsSystem();
    private static DataOutputStream logDataOutStream;
    private static ArrayList<Float> synthLoadVector = new ArrayList<Float>();
    private static Map<String, ClientSimulator> simulators = new ConcurrentHashMap<String, ClientSimulator>();

    private static int synthLoad = Configuration.getInstance().getLoadIntensity();
    private static List<NetworkSelector> serverList = new CopyOnWriteArrayList<NetworkSelector>();
    private static Random masterRandom=new Random();

    public static int getSynthLoad() {
        return synthLoad;
    }

    public static List<NetworkSelector> getServers() {
        return serverList;
    }

    public static StatisticsSystem getStatisticsSystem() {
        return statisticsSystem;
    }

    public static ScheduledExecutorService getExecutor() {
        return executor;
    }

    public static void main(String[] args) throws IOException {
        createExecutor();
        System.out.println("Number of selectors: " + Configuration.getInstance().getNumSelectors());
        setupNetworkTraceLogging();
        createKilltimeThread();
        executor.schedule(statisticsSystem, 1, TimeUnit.SECONDS);
        setupGui();
        setupHandlers();
        populateSynthLoadVector();
        for (int i = 0; i < Configuration.getInstance().getNumSelectors(); i++) {
            final NetworkSelector networkSelector = new NetworkSelector(null, Configuration.getInstance().getServerPort() + i);
            getExecutor().schedule(networkSelector, 10, TimeUnit.NANOSECONDS);
            serverList.add(networkSelector);
        }
    }

    private static void createKilltimeThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(Configuration.getInstance().getKillTime()));
                } catch (InterruptedException e) {
                    System.out.println("Going down early due to killer thread interrupted!");
                }
                System.out.println("Going down due to killtime parameter!");
                System.exit(0);
            }
        });
    }

    private static void setupNetworkTraceLogging() {
        if (Configuration.getInstance().isLoggingData()) {
            try {
                logDataOutStream = new DataOutputStream(new FileOutputStream(Configuration.getInstance().getTraceFileName()));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void createExecutor() {
        if (Configuration.getInstance().isSingleThreaded()) {
            System.out.println("Creating singlethreaded executor.");
            executor = Executors.newSingleThreadScheduledExecutor();
        } else {
            System.out.println("Creating executor with " + Configuration.getInstance().getNumThreads() + " threads.");
            executor = Executors.newScheduledThreadPool(Configuration.getInstance().getNumThreads());
        }
    }

    private static void setupGui() {
        (new ServerControlGui()).open();
    }

    private static void setupHandlers() {
        MessageType.POS_UPDATE.setHandler(new PosUpdateHandler());
        MessageType.NEW_PLAYER.setHandler(new NewPlayerHandler());
        MessageType.ATTACK.setHandler(new AttackHandler());
        MessageType.PLAYER_LEFT.setHandler(new PlayerLeftHandler());
    }


    /**
     * This ugly synchronized method will destroy A LOT of parallelism. But I don't care, since it is
     * never going to be used in "production" runs.
     */
    public static void logMessage(Message message) {
        try {
            logDataOutStream.writeLong(TimeSystem.getInstance().getElapsedTime());
            logDataOutStream.writeInt(message.getMessageType().ordinal());
            message.write(logDataOutStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setSynthLoad(int value) {
        synthLoad = value;
    }

    public static void setNumthreads(int numThreads) {
        ExecutorService dyingService = executor;
        executor = Executors.newScheduledThreadPool(numThreads);
        dyingService.shutdown();
    }

    private static void populateSynthLoadVector() {
        Random randomGenerator = new Random();
        for (int i = 0; i < Configuration.getInstance().getLoadVectorSize(); i++) {
            synthLoadVector.add(randomGenerator.nextFloat());
        }
    }

    public static ArrayList<Float> getSynthLoadVector() {
        return synthLoadVector;
    }
    public static void addClientSimulator(ClientSimulator simulator) {
        simulators.put(simulator.getId(), simulator);
    }

    public static void setNumClients(int numClients) {
        if (simulators.size() == 0) {
            System.out.println("No simulators available to run clients.");
            return;
        }
        int clientsEach = numClients / simulators.size();
        for (Iterator<ClientSimulator> iterator = simulators.values().iterator(); iterator.hasNext(); ) {
            ClientSimulator clientSimulator = iterator.next();
            if (!iterator.hasNext())
                clientSimulator.setNumClients(clientsEach + numClients % simulators.size());
            else
                clientSimulator.setNumClients(clientsEach);
        }
    }

    public static int getSimulatorSize() {
        return simulators.size();
    }

    public static void setNumNPCs(int numNPCs) {
        int newNPCs = numNPCs - GameWorld.getInstance().getNumNPCs();
        while (newNPCs > 0) {
            GameWorld.getInstance().spawnNPC();
            newNPCs--;

        }
        while (newNPCs < 0) {
            GameWorld.getInstance().removeNPC();
            newNPCs++;
        }
    }

    public static Random getMasterRandom() {
        return masterRandom;
    }
}
