package client;

import common.AttackMessage;
import common.CharType;

import javax.vecmath.Vector2d;

public class OtherPlayerUpdater {
    private VisibleGameObject gameWorld;

    public OtherPlayerUpdater(VisibleGameObject gameWorld) {
        this.gameWorld = gameWorld;
        ClientMain.getInstance().getNetClient().setPlayerStatusListener(new PlayerStatusListener() {
            public void playerArrived(String playerID, Vector2d position, CharType type) {
                OtherPlayer otherPlayer = new OtherPlayer(playerID, OtherPlayerUpdater.this.gameWorld,position,type);
                OtherPlayerUpdater.this.gameWorld.addChild(otherPlayer);
            }

            @Override
            public void playerLeft(String playerID) {
                GameObject child = OtherPlayerUpdater.this.gameWorld.getChild(playerID);
                if (child == null) {
                    return;
                }
                OtherPlayerUpdater.this.gameWorld.remove(child);
            }
        });
        ClientMain.getInstance().getNetClient().setPosUpdateListener(new PosUpdateListener() {
            public void playerPosUpdated(String playerID, Vector2d playerPos, int angle) {
                GameObject otherPlayer = OtherPlayerUpdater.this.gameWorld.getChild(playerID);
                if (otherPlayer == null) {
                    return;
                }
                ((Player) otherPlayer).setPosition(playerPos);
                ((Player) otherPlayer).getMover().setAngle(angle);
            }
        });
        ClientMain.getInstance().getNetClient().setOtherPlayerAttackedListener(new OtherPlayerAttackedListener() {
            @Override
            public void playerAttacked(String id, AttackMessage.AttackType attackType) {
                Player otherPlayer = (Player) OtherPlayerUpdater.this.gameWorld.getChild(id);
                if (otherPlayer == null) {
                    return;
                }
                VisibleGameObject attackShape = null;
                switch (attackType) {
                    case ARC_ATTACK:
                        attackShape = new ArcAttackShape(otherPlayer);
                        break;
                    case PROJECTILE_ATTACK:
                        attackShape = new ProjectileAttackShape(otherPlayer);
                        break;
                }
                otherPlayer.addChild(attackShape);

            }
        });

    }
}
