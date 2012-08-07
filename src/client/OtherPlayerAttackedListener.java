package client;

import common.AttackMessage;

/**
 *
 */
public interface OtherPlayerAttackedListener {
    void playerAttacked(String id, AttackMessage.AttackType attackType);
}
