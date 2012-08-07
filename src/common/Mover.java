package common;

import javax.vecmath.Vector2d;

public class Mover {
    private Vector2d position;
    protected double angle;
    private static final double MOVE_SPEED = 100;
    private static final Vector2d REFERENCE_DIRECTION = new Vector2d(1, 0);

    public Mover() {
        this.position = new Vector2d();
    }

    public Vector2d getPosition() {
        return position;
    }

    public void setPosition(Vector2d position) {
        this.position.set(position);
    }

    public void updateDirection(Vector2d movement) {
        if (movement.length() > 0.000001) {
            angle = movement.angle(REFERENCE_DIRECTION) / Math.PI * 180;
            if (movement.getY() > 0) {
                angle = -angle;
            }
        }
    }

    public void scaleAndVerifyMovement(Vector2d movement) {
        updateDirection(movement);
        movement.scale(MOVE_SPEED);
        movement.scale(TimeSystem.getInstance().getDT());
        Vector2d newPos = new Vector2d(getPosition());
        newPos.add(movement);
        Vector2d nearestPointOutsideOfObstacles = CollisionChecker.instance().getNearestPointOutsideOfObstacles(newPos);
        setPosition(nearestPointOutsideOfObstacles);

    }


    public void setAngle(double angle) {
        this.angle = angle;
    }

    public int getAngle() {
        return (int) angle;
    }

}
