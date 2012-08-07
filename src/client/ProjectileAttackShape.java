package client;

import common.AttackData;
import common.Configuration;
import common.TimeSystem;
import server.GameWorld;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.Map;

/**
 *
 */
public class ProjectileAttackShape extends VisibleGameObject {
    private double originalAngle;
    private double totalDistance;

    public ProjectileAttackShape(Player attacker) {
        super("projectile: " + attacker.getId(), attacker);
        setPosition(new Vector2d(attacker.getPosition()));
        originalAngle = attacker.getMover().getAngle()/180.0*Math.PI;
    }

    @Override
    public void draw(Graphics graphics) {
        super.draw(graphics);
        graphics.setColor(Color.YELLOW);
        int size = AttackData.ProjectileAttack.getSize();
        graphics.fillRect((int) getPosition().x - size / 2, (int) getPosition().y - size / 2, size, size);

    }

    @Override
    public void update() {
        super.update();
        double deltaDistance = AttackData.ProjectileAttack.getSpeed()*(TimeSystem.getInstance().getDT());
        Vector2d deltaPos = new Vector2d(deltaDistance*Math.cos(originalAngle), -deltaDistance*Math.sin(originalAngle));
        getPosition().add(deltaPos);
        totalDistance += deltaDistance;
        int size = AttackData.ProjectileAttack.getSize()+AttackData.getPlayerDiameter();
        Rectangle shape = new Rectangle((int) (getPosition().getX() - size / 2.0), (int) (getPosition().getY() - size / 2.0), size, size);

        boolean exploded = false;
        Map<String,GameObject> children = ClientMain.getInstance().getGameWorld().getChildren();
        for (GameObject gameObject : children.values()) {
            if (gameObject instanceof Player && !gameObject.getId().equals(parent.getId())) {
                Player player = (Player) gameObject;
                if (shape.contains(player.getPosition().getX(),player.getPosition().getY())) {
                    exploded = true;
                }
            }
        }
        if (AttackData.ProjectileAttack.getRange() < totalDistance || exploded) {
            getParent().remove(this);
        }
    }

}
