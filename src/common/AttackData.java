package common;

import javax.print.attribute.Size2DSyntax;

/**
 * Should seriously be replaced by some XML or something
 *
 */
public class AttackData {
    private static final int PLAYER_DIAMETER = 20;

    public static int getPlayerDiameter() {
        return PLAYER_DIAMETER;
    }

    public static class ProjectileAttack{

        private static int SIZE=15;
        private static int RANGE=200;
        private static double SPEED=150;

        public static int getSize() {
            return SIZE;
        }

        public static int getRange() {
            return RANGE;
        }

        public static double getSpeed() {
            return SPEED;
        }
    }

    public static class ArcAttack{
        private static final int ARC_ANGLE = 45;
        private static final int DIAMETER = 100;

        public static int getDiameter() {
            return DIAMETER;
        }

        public static int getArcAngle() {
            return ARC_ANGLE;
        }

        public static long getCoolDown() {
            return 500;
        }
    }

}
