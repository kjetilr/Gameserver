package common;

/**
 *
 */
public class RealtimeGameTimer implements GameTimer{
    private long currentTime;
    private long prevTime;
    private volatile double deltaTime;
    private static GameTimer instance=new RealtimeGameTimer();
    private long startTime;

    public RealtimeGameTimer() {
        currentTime=System.nanoTime();
        prevTime=currentTime;
        startTime=currentTime;
    }
    public void update(){
        prevTime=currentTime;
        currentTime=System.nanoTime();
        deltaTime=(currentTime -prevTime)/1000000000.0;
    }
    public double getDT() {
        return deltaTime;
    }

    public long getCurrentTime() {
        return currentTime;
    }
    public long getElapsedTime() {
        return currentTime - startTime;
    }
}
