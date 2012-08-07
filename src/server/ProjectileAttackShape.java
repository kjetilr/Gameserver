package server;

import common.AttackData;
import common.Configuration;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class ProjectileAttackShape extends AttackShape {
    private Vector2d position;
    private double angle;
    private AbstractCharacter attacker;
    private Rectangle shape;
    private double distance = 0;
    private int size;

    public ProjectileAttackShape(Vector2d position, int angle, AbstractCharacter attacker) {
        super();
        this.position = new Vector2d(position);
        this.angle = angle / 180.0 * Math.PI;
        this.attacker = attacker;
        size = AttackData.ProjectileAttack.getSize() + AttackData.getPlayerDiameter();
        shape = new Rectangle((int) (position.getX() - size / 2.0), (int) (position.getY() - size / 2.0), size, size);
    }

    @Override
    public void run() {
        boolean exploded = false;
        exploded = checkHit(exploded, GameWorld.getInstance().getPlayerCharacters().values());
        if (attacker instanceof PlayerCharacter) {
            exploded = checkHit(exploded, GameWorld.getInstance().getNPCs().values()) || exploded;
        }
        double deltaDistance = AttackData.ProjectileAttack.getSpeed() * (Configuration.getInstance().getUpdateInterval() / 1000.0f);
        distance += deltaDistance;
        Vector2d deltaPos = new Vector2d(deltaDistance * Math.cos(angle), -deltaDistance * Math.sin(angle));
        position.add(deltaPos);
        //System.out.println("position = " + position);
        shape.setLocation((int) (position.getX() - size / 2.0), (int) (position.getY() - size / 2.0));
        if (!exploded && distance < AttackData.ProjectileAttack.getRange()) {
            ServerMain.getExecutor().schedule(this, Configuration.getInstance().getUpdateInterval(), TimeUnit.MILLISECONDS);
        }
    }

    private boolean checkHit(boolean exploded, Collection<? extends AbstractCharacter> characters) {
        for (AbstractCharacter defender : characters) {
            if (defender != attacker) {
                if (shape.contains(defender.getPosition().x, defender.getPosition().y)) {
                    informAboutHit(attacker, true);
                    informAboutHit(defender, false);
                    exploded = true;
                }
            }
        }
        return exploded;
    }
}
