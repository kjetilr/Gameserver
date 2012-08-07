package client;

import common.CharType;

import javax.vecmath.Vector2d;

/**
 *
 */
public interface PlayerStatusListener {
    void playerArrived(String playerID, Vector2d position, CharType type);
    void playerLeft(String playerID);
}
