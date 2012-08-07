package common;

import testrig.SetPlayerNumberMessage;

/**
 *
 */
public enum MessageType {
    NEW_PLAYER(NewPlayerMessage.class),
    POS_UPDATE(PosUpdateMessage.class),
    ATTACK(AttackMessage.class),
    HIT_UPDATE(HitUpdateMessage.class),
    PLAYER_LEFT(PlayerLeftMessage.class),
    SET_PLAYER_NUMBER(SetPlayerNumberMessage.class);
    private Class<? extends Message> messageClass;
    private MessageHandler handler;

    MessageType(Class<? extends Message> messageClass) {
        this.messageClass = messageClass;
    }

    public Class<? extends Message> messageClass() {
        return messageClass;
    }

    public void setHandler(MessageHandler handler) {
        this.handler = handler;
    }

    public MessageHandler getHandler() {
        return handler;
    }
}
