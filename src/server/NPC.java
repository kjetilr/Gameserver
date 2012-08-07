package server;

import common.*;
import straightedge.geom.KPoint;
import straightedge.geom.path.*;

import javax.vecmath.Vector2d;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class NPC extends AbstractCharacter {

    private final Random random = new Random(ServerMain.getMasterRandom().nextLong());
    private long nextAttackTime;
    private PlayerCharacter target;
    private PathFinder pathFinder;
    private PathData pathData;
    private NodeConnector<PathBlockingObstacleImpl> nodeConnector;
    private ArrayList<PathBlockingObstacleImpl> stationaryObstacles;

    public NPC(String id) {
        super(new Vector2d(), id);
        mover.setPosition(getRandomPos());
        updateOthersAboutMe();
        pathFinder = new PathFinder();
        stationaryObstacles = CollisionChecker.instance().getStationaryObstacles();
        nodeConnector = CollisionChecker.instance().getNodeConnector(stationaryObstacles);
    }

    @Override
    protected CharType getCharType() {
        return CharType.NPC;
    }

    @Override
    public void run() {
        if (!toDie) {
            updateAndReschedule();
        }

        boolean newTarget = findBestTarget();
        if (target != null) {
            Vector2d targetVec = new Vector2d(target.getPosition());
            targetVec.sub(getPosition());
            if (!newTarget && targetVec.length() < AttackData.ArcAttack.getDiameter() * 0.4) {
                turnToTarget(target);
                attack();
            } else {
                Vector2d nextPathPoint = pathfindToPoint(target.getPosition());
                if (nextPathPoint != null) {
                    moveTowardsPoint(nextPathPoint);
                }
            }
        }
    }

    private synchronized Vector2d pathfindToPoint(Vector2d destination) {
        KPoint destinationAsK = new KPoint(destination.getX(), destination.getY());
        KPoint startPointAsK = new KPoint(getPosition().getX(), getPosition().getY());
        pathData = pathFinder.calc(startPointAsK, destinationAsK, 1000, nodeConnector, stationaryObstacles);
        if (pathData.isError() || pathData.getNodes().isEmpty()) {
            return null;
        }
        KNode nextNode = pathData.getNodes().get(1);
        return new Vector2d(nextNode.getPoint().getX(), nextNode.getPoint().getY());
    }

    private void attack() {
        if (TimeSystem.getInstance().getCurrentTime() > nextAttackTime) {
            nextAttackTime = TimeSystem.getInstance().getCurrentTime() + TimeUnit.MILLISECONDS.toNanos(AttackData.ArcAttack.getCoolDown());

            AttackShape attackShape = AttackHandler.createAttackShape(getPosition(), getAngle(), AttackMessage.AttackType.ARC_ATTACK, this);

            for (PlayerCharacter defender : GameWorld.getInstance().getPlayerCharacters().values()) {
                AttackHandler.informAboutAttack(this, defender, AttackMessage.AttackType.ARC_ATTACK);
            }
            ServerMain.getExecutor().execute(attackShape);
        }
    }

    private void moveTowardsPoint(Vector2d position) {
        Vector2d move = new Vector2d((mover.getPosition()));
        move.sub(position);
        if (move.length() < 0.001)
            return;
        move.normalize();
        move.scale(-0.5);
        mover.scaleAndVerifyMovement(move);
        updatePosition(mover.getPosition(), mover.getAngle());
    }

    private void turnToTarget(PlayerCharacter target) {
        Vector2d move = new Vector2d((mover.getPosition()));
        move.sub(target.getPosition());
        if (move.length() < 0.001)
            return;
        move.normalize();
        move.scale(-1);
        mover.updateDirection(move);
        updatePosition(mover.getPosition(), mover.getAngle());
    }

    private boolean findBestTarget() {
        Collection<PlayerCharacter> characters = GameWorld.getInstance().getPlayerCharacters().values();
        PlayerCharacter bestTarget = Collections.min(characters, new Comparator<PlayerCharacter>() {
            @Override
            public int compare(PlayerCharacter o1, PlayerCharacter o2) {
                return Math.round(o1.getTargetValue(NPC.this) - o2.getTargetValue(NPC.this));
            }
        });
        if ((target == null || bestTarget.getTargetValue(this) < (target.getTargetValue(this) + AIData.getStickiness())) && target != bestTarget) {
            target = bestTarget;
            return true;
        } else {
            return false;
        }

    }

    @Override
    public void addHitTaken() {
        hitsTaken++;
        if (hitsTaken == 3) {
            GameWorld.getInstance().killNPC(this);
            ServerMain.getExecutor().schedule(new Runnable() {
                @Override
                public void run() {
                    GameWorld.getInstance().spawnNPC();
                }
            }, AIData.getRespawnTime(), TimeUnit.SECONDS);
        }

    }

    @Override
    public void addHitDealt() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private Vector2d getRandomPos() {
        Vector2d randomPos = new Vector2d(random.nextInt(Configuration.getInstance().getWindowSize()), random.nextInt(Configuration.getInstance().getWindowSize()));
        randomPos = CollisionChecker.instance().getNearestPointOutsideOfObstacles(randomPos);
        return randomPos;
    }
}
