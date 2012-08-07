package client;

import common.CharType;

import javax.vecmath.Vector2d;
import java.awt.*;

/**
 *
 */
public class OtherPlayer extends Player {
    public OtherPlayer(String playerID, GameObject parent, Vector2d position,CharType type) {
        super(playerID, parent);
        setPosition(position);
        if (type == CharType.Player) {
            setColor(Color.red);
        }else if (type == CharType.NPC) {
            setColor(Color.MAGENTA);
        }
    }

}
