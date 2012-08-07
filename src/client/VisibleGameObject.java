package client;

import common.Mover;

import javax.vecmath.Vector2d;
import java.awt.*;

/**
 */
public class VisibleGameObject extends GameObject {

    private Mover mover = new Mover();

    public VisibleGameObject(String id, GameObject parent) {
        super(id, parent);
    }

    public void draw(Graphics graphics) {
        for (GameObject child : getChildren().values()) {
            if (child instanceof VisibleGameObject)
                ((VisibleGameObject) child).draw(graphics);
        }
    }

    public Vector2d getPosition() {
        return mover.getPosition();
    }

    public void setPosition(Vector2d position) {
        mover.setPosition(position);
    }

    public Mover getMover() {
        return mover;
    }
}
