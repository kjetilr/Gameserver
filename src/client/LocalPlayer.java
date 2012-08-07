package client;

import common.*;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class LocalPlayer extends Player implements KeyListener {
    boolean[] keys = new boolean[525];
    private long lastNetUpdate = 0;
    private static final long NET_UPDATE_INTERVAL = 100000000; //in nanoseconds
    private ArrayList<Attack> attacks = new ArrayList<Attack>();

    public LocalPlayer(String id, GameObject parent, Vector2d position) {
        super(id, parent);
        setPosition(position);
        setColor(Color.green);
        ClientMain.getInstance().getNetClient().send(new NewPlayerMessage(getId(), getPosition(), CharType.Player));
        configureAttacks();
    }

    //Todo: Read attacks from config file?
    private void configureAttacks() {
        attacks.add(new ArcAttack());
        attacks.add(new ProjectileAttack());
    }

    @Override
    public void update() {
        super.update();
        Vector2d movement = composeMoveVec();
        getMover().scaleAndVerifyMovement(movement);
        updateServer(movement.length() > 0.00001);
    }

    private void updateServer(boolean moved) {
        if (TimeSystem.getInstance().getCurrentTime() - lastNetUpdate > NET_UPDATE_INTERVAL && moved) {
            ClientMain.getInstance().getNetClient().send(new PosUpdateMessage(getId(), getPosition(), getMover().getAngle()));
            lastNetUpdate = TimeSystem.getInstance().getCurrentTime();
        }
    }


    private Vector2d composeMoveVec() {
        Vector2d result = new Vector2d();
        if (keys[KeyEvent.VK_A])
            result.setX(result.getX() - 1);
        if (keys[KeyEvent.VK_D])
            result.setX(result.getX() + 1);
        if (keys[KeyEvent.VK_W])
            result.setY(result.getY() - 1);
        if (keys[KeyEvent.VK_S])
            result.setY(result.getY() + 1);
        if (result.lengthSquared() != 0)
            result.normalize();

        return result;
    }

    public void keyTyped(KeyEvent e) {
    }

    /**
     * A good game needs both state based and event based key processing.
     * Store these in array for state-based effects, and handle event-based keys here.
     */
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() < keys.length)
            keys[e.getKeyCode()] = true;
    }

    /**
     * A good game needs both state based and event based key processing.
     * Store these in array for state-based effects, and handle event-based keys here.
     */
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
            System.exit(0);
        if (e.getKeyCode() == KeyEvent.VK_SPACE)
            attack(0);
        if (e.getKeyCode() == KeyEvent.VK_CONTROL)
            attack(1);
        if (e.getKeyCode() == KeyEvent.VK_SHIFT)
            attack(1);

        if (e.getKeyCode() < keys.length)
            keys[e.getKeyCode()] = false;
    }

    private void attack(int i) {
        attacks.get(i).execute(this);
    }

}
