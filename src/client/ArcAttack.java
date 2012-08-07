package client;

import common.AttackMessage;
import common.AttackData;
import common.TimeSystem;

import java.util.concurrent.TimeUnit;

/**
 * Simplest attack I could think of.
 */
public class ArcAttack implements Attack {
    private long nextAttackTime=0;
    private static final long COOLDOWN = TimeUnit.MILLISECONDS.toNanos(AttackData.ArcAttack.getCoolDown());
    private LocalPlayer attacker;

    public void execute(LocalPlayer attacker) {
        this.attacker=attacker;
        if (TimeSystem.getInstance().getCurrentTime() > nextAttackTime) {
            VisibleGameObject attackShape = new ArcAttackShape(attacker);
            nextAttackTime=TimeSystem.getInstance().getCurrentTime()+COOLDOWN;
            attacker.addChild(attackShape);
            tellServer();
        }
    }

    private void tellServer() {
        ClientMain.getInstance().getNetClient().send(new AttackMessage(attacker.getId(), attacker.getMover().getAngle(), AttackMessage.AttackType.ARC_ATTACK));
    }

}
