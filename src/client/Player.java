package client;

import common.AttackData;

import java.awt.*;

/**
 */
public class Player extends VisibleGameObject{

    public void setColor(Color color) {
        this.color = color;
    }

    private Color color;
    protected Player(String id, GameObject parent) {
        super(id, parent);
    }

    @Override
    public void draw(Graphics graphics) {
        super.draw(graphics);
        graphics.setColor(color);
        graphics.fillOval((int) getPosition().x - AttackData.getPlayerDiameter() / 2, (int) getPosition().y - AttackData.getPlayerDiameter() / 2, AttackData.getPlayerDiameter(), AttackData.getPlayerDiameter());
        graphics.setColor(Color.BLUE);
        graphics.fillArc((int) getPosition().x - AttackData.getPlayerDiameter() / 2, (int) getPosition().y - AttackData.getPlayerDiameter() / 2, AttackData.getPlayerDiameter(), AttackData.getPlayerDiameter(),
                getMover().getAngle() - 23, 46);

    }

}
