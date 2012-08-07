package client;

import javax.vecmath.Vector2d;

/**
 *
 */
public interface PosUpdateListener {
    void playerPosUpdated(String playerID, Vector2d playerPos, int angle);
}
