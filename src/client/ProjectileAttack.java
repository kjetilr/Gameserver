package client;

import common.AttackData;
import common.AttackMessage;
import common.TimeSystem;

import java.util.concurrent.TimeUnit;

/**
 *
 */
public class ProjectileAttack implements Attack {
    private LocalPlayer attacker;
    private static final long COOLDOWN = TimeUnit.MILLISECONDS.toNanos(AttackData.ArcAttack.getCoolDown());
    private long nextAttackTime = 0;

    public void execute(LocalPlayer attacker) {
        this.attacker = attacker;
        if (TimeSystem.getInstance().getCurrentTime() > nextAttackTime) {
            nextAttackTime = TimeSystem.getInstance().getCurrentTime() + COOLDOWN;
            tellServer();
        }
    }

    private void tellServer() {
        ClientMain.getInstance().getNetClient().send(new AttackMessage(attacker.getId(), attacker.getMover().getAngle(), AttackMessage.AttackType.PROJECTILE_ATTACK));
    }
}
