package common;

/**
 *
 */
public class TimeSystem {

    private static GameTimer instance;

    public static void setInstance(GameTimer instance) {
        TimeSystem.instance = instance;
    }

    public static GameTimer getInstance() {
        return instance;
    }
}
