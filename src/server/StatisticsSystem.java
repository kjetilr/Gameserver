package server;

import common.Configuration;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Logs runtime info from the server
 */
public class StatisticsSystem implements Runnable {
    private FileWriter delayDataFile = null;
    private FileWriter cpuDataFile = null;
    private BlockingQueue<DelayData> delayData;
    private ThreadMXBean mxBean;
    private long lastCPUTime;
    private long lastTime;
    private NumberFormat numberFormat;
    private ArrayList<ServerStatListener> serverStatListeners =new ArrayList<ServerStatListener>();
    private AtomicInteger sendTimeouts=new AtomicInteger(0);
    private FileWriter timeoutDataFile=null;

    public StatisticsSystem() {
        delayData = new ArrayBlockingQueue<DelayData>(100000);
        try {
            delayDataFile = new FileWriter(Configuration.getInstance().getDelayStatisticsFileName());
            cpuDataFile = new FileWriter(Configuration.getInstance().getCPUStatisticsFileName());
            timeoutDataFile = new FileWriter(Configuration.getInstance().getTimeoutsFileName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mxBean = ManagementFactory.getThreadMXBean();
        mxBean.setThreadContentionMonitoringEnabled(true);
        numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(4);
        numberFormat.setMinimumFractionDigits(4);
    }

    @Override
    public void run() {
        try {
            writeCPUTime();
            writeDelays();
            writeSendTimeouts();
            ServerMain.getExecutor().schedule(this, 1, TimeUnit.SECONDS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeSendTimeouts() {
        try {
            timeoutDataFile.write(System.currentTimeMillis() + "\t" + sendTimeouts.intValue() + "\n");
            timeoutDataFile.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sendTimeouts.set(0);
    }

    private void writeCPUTime() throws IOException {
        long[] allThreadIds = mxBean.getAllThreadIds();
        long cpuTime = 0;
        long currentTime = System.nanoTime();
        for (long threadId : allThreadIds) {
            cpuTime += mxBean.getThreadCpuTime(threadId);
        }
        double cpuUsageFraction=0;
        if (lastCPUTime != 0) {
            cpuUsageFraction = ((double) cpuTime - lastCPUTime) / (currentTime - lastTime);
            cpuDataFile.write(System.currentTimeMillis() + "\t" + numberFormat.format(cpuUsageFraction) + "\n");
            cpuDataFile.flush();
        }
        lastTime = currentTime;
        lastCPUTime = cpuTime;
        sendCPUDataEvent(cpuUsageFraction);
    }

    private void sendCPUDataEvent(double cpuUsageFraction) {
        for (ServerStatListener serverStatListener : serverStatListeners) {
            serverStatListener.newCPUDataEvent(cpuUsageFraction);
        }
    }
    private void sendDelayDataEvent(double delayMin, double delayAvg, double delayMax, double messageQueueSizeAvg, double networkQueueSizeAvg, int numPlayers, int queueLengthAcc) {
        for (ServerStatListener serverStatListener : serverStatListeners) {
            serverStatListener.newDelayDataEvent(delayAvg, delayMax, delayMin, messageQueueSizeAvg,networkQueueSizeAvg,numPlayers,queueLengthAcc);
        }
    }

    private void writeDelays() throws IOException {
        long diffAccumulator=0;
        int messageQueAcc=0;
        int netQueueAcc=0;
        int values=0;
        double delayMin=Double.MAX_VALUE;
        double delayMax=0;
        while (!delayData.isEmpty()) {
            values++;
            DelayData data = delayData.remove();
            diffAccumulator += data.diff;
            delayMax = Math.max(delayMax, data.diff);
            delayMin = Math.min(delayMin, data.diff);
            messageQueAcc+= data.messageQueueSize;
            netQueueAcc += data.networkQueueSize;
            delayDataFile.write(data.id + "\t" + data.timeStamp + "\t" + data.diff + "\t" + data.messageQueueSize + "\t" + data.networkQueueSize + "\n");
        }
        int queueLengthAcc=0;
        for (NetworkSelector server : ServerMain.getServers()) {
            queueLengthAcc+=server.getQueueLength();
        }
        sendDelayDataEvent(
                delayMin, values==0?0:diffAccumulator/((double)values),
                delayMax, values==0?0:messageQueAcc/((double)values),
                values==0?0:netQueueAcc/((double)values),
                GameWorld.getInstance().getPlayerCharacters().size(),queueLengthAcc);
    }

    public void addTimeDelay(long diff, String id, int messageQueueSize) {
        //Consider pooling...
        delayData.add(new DelayData(System.currentTimeMillis(), diff, id, messageQueueSize));
    }

    public void addServerStatListener(ServerStatListener serverStatListener) {
        serverStatListeners.add(serverStatListener);
    }

    public void addSendTimeout() {
        sendTimeouts.addAndGet(1);
    }

    private class DelayData {
        long timeStamp;
        long diff;
        String id;
        int messageQueueSize;
        int networkQueueSize;

        public DelayData(long timeStamp, long diff, String id, int messageQueueSize) {
            this.timeStamp = timeStamp;
            this.diff = diff;
            this.id = id;
            this.messageQueueSize = messageQueueSize;
            this.networkQueueSize = networkQueueSize;
        }
    }
}
