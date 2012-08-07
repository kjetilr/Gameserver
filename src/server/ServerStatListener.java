package server;

public interface ServerStatListener {
    void newCPUDataEvent(double cpuLoadValue);

    void newDelayDataEvent(double delayAvg, double delayMax, double delayMin, double messageQueueSizeAvg, double networkQueueSizeAvg, int numPlayers, int queueLengthAcc);
}
