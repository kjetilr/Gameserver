package server;

import common.AttackData;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.Collection;

/**
 *
 */
public class ArcAttackShape extends AttackShape implements Runnable{
    private Vector2d position;
    private int angle;
    private AbstractCharacter attacker;

    public ArcAttackShape(Vector2d position, int angle, AbstractCharacter attacker) {
        this.position = position;
        this.angle = angle;
        this.attacker = attacker;
    }

    public void run() {
        Shape shape = new Arc2D.Double((int) position.x - AttackData.ArcAttack.getDiameter() / 2,
                (int) position.y - AttackData.ArcAttack.getDiameter() / 2,
                AttackData.ArcAttack.getDiameter(),
                AttackData.ArcAttack.getDiameter(),
                angle - AttackData.ArcAttack.getArcAngle() / 2,
                AttackData.ArcAttack.getArcAngle(), Arc2D.PIE);
        checkForHits(shape, GameWorld.getInstance().getPlayerCharacters().values());
        if (attacker instanceof PlayerCharacter) {
            checkForHits(shape, GameWorld.getInstance().getNPCs().values());
        }
    }

    private void checkForHits(Shape shape, Collection<? extends AbstractCharacter> characters) {
        for (AbstractCharacter defender : characters) {
            if (defender != attacker) {
                if (shape.contains(defender.getPosition().x, defender.getPosition().y)) {
                    informAboutHit(attacker,true);
                    informAboutHit(defender,false);
                }
            }
        }
    }
}
