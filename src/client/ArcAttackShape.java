package client;

import common.AttackData;
import common.TimeSystem;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.concurrent.TimeUnit;

/**
 *
 */
class ArcAttackShape extends VisibleGameObject {
    private static final long LINGER_TIME = TimeUnit.MILLISECONDS.toNanos(100);
    private int originalAngle;
    private long removeTime;

    public ArcAttackShape(Player attacker) {
        super("attackCone: " + attacker.getId(), attacker);
        setPosition(new Vector2d(attacker.getPosition()));
        originalAngle = attacker.getMover().getAngle();
        removeTime = TimeSystem.getInstance().getCurrentTime() + LINGER_TIME;
    }

    @Override
    public void draw(Graphics graphics) {
        super.draw(graphics);
        graphics.setColor(Color.YELLOW);
        graphics.fillArc((int) getPosition().x - AttackData.ArcAttack.getDiameter() / 2,
                (int) getPosition().y - AttackData.ArcAttack.getDiameter() / 2,
                AttackData.ArcAttack.getDiameter(),
                AttackData.ArcAttack.getDiameter(),
                originalAngle - AttackData.ArcAttack.getArcAngle() / 2, AttackData.ArcAttack.getArcAngle());

    }

    @Override
    public void update() {
        super.update();
        if (TimeSystem.getInstance().getCurrentTime() > removeTime) {
            getParent().remove(this);
        }
    }
}
