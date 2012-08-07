package client;

import common.CollisionChecker;
import common.Configuration;
import common.RealtimeGameTimer;
import common.TimeSystem;

import javax.swing.*;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 */
public class ClientMain extends JFrame {
    private Graphics bufferGraphics;
    private Image offscreen;
    private RealtimeGameTimer realtimeGameTimer;
    private static final long FRAME_PERIOD = 25;
    private static ClientMain instance;
    private NetClient netClient;

    public static ClientMain getInstance() {
        return instance;
    }

    public VisibleGameObject getGameWorld() {
        return gameWorld;
    }

    private VisibleGameObject gameWorld;
    private HUD hud;

    public void init() {
        instance = this;
        realtimeGameTimer = new RealtimeGameTimer();
        TimeSystem.setInstance(realtimeGameTimer);
        gameWorld = new VisibleGameObject("GameWorld", null);

        netClient = new NetClient();
        try {
            netClient.connect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        hud = new HUD();
        Random random = new Random();
        Vector2d position = new Vector2d(Math.abs(random.nextInt()) % (getWidth() - (getInsets().left + getInsets().right)),
                Math.abs(random.nextInt()) % (getHeight() - (getInsets().top + getInsets().bottom)));

        LocalPlayer localPlayer = new LocalPlayer("me" + (new Random()).nextInt(), gameWorld, position);
        OtherPlayerUpdater otherPlayerUpdater = new OtherPlayerUpdater(gameWorld);
        gameWorld.addChild(localPlayer);
        addKeyListener(localPlayer);
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                resized();
            }
        });
        Timer frameTimer = new Timer();
        frameTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                repaint();
            }
        }, FRAME_PERIOD, FRAME_PERIOD);
    }

    private void resized() {
        setBackground(Color.black);
        hud.setSize(getSize());
        offscreen = createImage(getSize().width, getSize().height);
//        CollisionChecker.instance().setShape(new Rectangle2D.Double(0, 0,
//                getSize().getWidth() - (getInsets().left + getInsets().right),
//                getSize().getHeight() - (getInsets().top + getInsets().bottom)));
        bufferGraphics = offscreen.getGraphics();
    }

    public void paint(Graphics g) {
        if (offscreen == null) {
            resized();
        }
        realtimeGameTimer.update();
        netClient.update();
        bufferGraphics.clearRect(0, 0, getSize().width, getSize().height);
        gameWorld.update();
        CollisionChecker.instance().draw(bufferGraphics);
        gameWorld.draw(bufferGraphics);
        hud.draw(bufferGraphics);
        g.drawImage(offscreen, getInsets().left, getInsets().top, this);
    }

    public void update(Graphics g) {
        paint(g);
    }

    public static void main(String[] args) {
        ClientMain clientMain = new ClientMain();
        clientMain.setSize(Configuration.getInstance().getWindowSize(), Configuration.getInstance().getWindowSize());
        clientMain.setDefaultCloseOperation(EXIT_ON_CLOSE);
        clientMain.init();
        clientMain.setVisible(true);
        Insets insets = clientMain.getInsets();
        clientMain.setSize(Configuration.getInstance().getWindowSize() + insets.left + insets.right, Configuration.getInstance().getWindowSize() + insets.top + insets.bottom);
    }

    public NetClient getNetClient() {
        return netClient;
    }
}
