package server;

/**
 *
 */
public abstract class AttackShape implements Runnable{
    protected void informAboutHit(final AbstractCharacter character, final boolean dealt) {
        character.sendMessage(new Runnable() {
            @Override
            public void run() {
                if (dealt) {
                    character.addHitDealt();
                } else {
                    character.addHitTaken();
                }
            }
        });
    }
}
