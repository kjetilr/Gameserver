package server;

import common.AttackMessage;
import common.Message;
import common.MessageHandler;

import javax.vecmath.Vector2d;
import java.nio.channels.SocketChannel;

/**
 * This has some common functionality that might come in handy in other attacks as well.
 * Refactor as needed.
 */
public class AttackHandler implements MessageHandler {
    public void execute(Message message, SocketChannel channel, NetworkSelector server) {
        final AttackMessage attackMessage = (AttackMessage) message;
        final AbstractCharacter attacker = GameWorld.getInstance().getGameCharacter(attackMessage.getId());
        AttackShape attackShape = createAttackShape(attacker.getPosition(), attackMessage.getAngle(), attackMessage.getAttackType(), attacker);

        for (PlayerCharacter defender : GameWorld.getInstance().getPlayerCharacters().values()) {
            informAboutAttack(attacker, defender, attackMessage.getAttackType());
        }
        ServerMain.getExecutor().execute(attackShape);
    }

    public static void informAboutAttack(final AbstractCharacter attacker, final PlayerCharacter defender, final AttackMessage.AttackType attackType) {
        defender.sendMessage(new Runnable() {
            @Override
            public void run() {
                defender.getNetworkWorker().send(new AttackMessage(attacker.getId(), attacker.getAngle(), attackType));
            }
        });
    }

    public static AttackShape createAttackShape(Vector2d position, int angle, AttackMessage.AttackType attackType, AbstractCharacter attacker) {
        switch (attackType) {
            case ARC_ATTACK:
                return new ArcAttackShape(position, angle, attacker);
            case PROJECTILE_ATTACK:
                return new ProjectileAttackShape(position, angle, attacker);
        }
        throw new RuntimeException("Unknown attack type: " + attackType);
    }
}
