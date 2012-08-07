package client;

import java.awt.*;

/**
 * Incredibly primitive HUD for displaying rudimentary info
 */
public class HUD implements HitUpdateListener {
    private int hitsDealt;
    private int hitsTaken;

    public HUD() {
        ClientMain.getInstance().getNetClient().setHitUpdateListener(this);
    }

    private Dimension size;

    public void draw(Graphics bufferGraphics) {
        bufferGraphics.setColor(Color.WHITE);
        bufferGraphics.setFont(new Font("Arial", Font.PLAIN, 20));
        bufferGraphics.drawString("Dealt: " + hitsDealt, 5, 20);
        bufferGraphics.drawString("Taken: " + hitsTaken, (int) (size.getWidth() - 150), 20);
    }


    public void setSize(Dimension size) {
        this.size = size;
    }

    @Override
    public void hitsUpdated(String id, int hitsDealt, int hitsTaken) {

        this.hitsDealt = hitsDealt;
        this.hitsTaken = hitsTaken;
    }
}
