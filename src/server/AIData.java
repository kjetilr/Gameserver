package server;

import javax.vecmath.Vector2d;

public class AIData {

    private static final float distanceWeight=1;
    private static final float stickiness=5;
    private static long respawnTime=10;

    public static float getDistanceWeight() {
        return distanceWeight;
    }

    public static float getStickiness() {
        return stickiness;
    }

    public static long getRespawnTime() {
        return respawnTime;
    }
}
