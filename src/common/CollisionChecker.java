package common;

import com.vividsolutions.jts.geom.Polygon;
import org.jfree.util.ShapeUtilities;
import straightedge.geom.KPoint;
import straightedge.geom.KPolygon;
import straightedge.geom.PolygonConverter;
import straightedge.geom.path.NodeConnector;
import straightedge.geom.path.PathBlockingObstacleImpl;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.util.ArrayList;

// Code stolen from:
// https://code.google.com/p/straightedge/wiki/PathFindingExample

public class CollisionChecker {
    private ArrayList<PathBlockingObstacleImpl> fattenedObstacles;
    private static CollisionChecker instnace = new CollisionChecker();
    private Area shape;

    public CollisionChecker() {
        shape = new Area(new Rectangle(0, 0, Configuration.getInstance().getWindowSize(), Configuration.getInstance().getWindowSize()));
        createObstacles();
        for (PathBlockingObstacleImpl stationaryObstacle : fattenedObstacles) {
            shape.subtract(new Area(stationaryObstacle.getPolygon()));
        }
        fattenPolygons();
    }

    private void fattenPolygons() {
        PolygonConverter converter = new PolygonConverter();
        ArrayList<PathBlockingObstacleImpl> newStationaryObstacles = new ArrayList<>();
        for (PathBlockingObstacleImpl stationaryObstacle : fattenedObstacles) {
            Polygon polygon = converter.makeJTSPolygonFrom(stationaryObstacle.getPolygon());
            polygon = (Polygon) polygon.buffer(AttackData.getPlayerDiameter() / 2);
            newStationaryObstacles.add(PathBlockingObstacleImpl.createObstacleFromInnerPolygon(converter.makeKPolygonFromExterior(polygon)));
        }
        fattenedObstacles = newStationaryObstacles;
    }


    //Creates some obstacle shapes. Currently a bunch of stars
    public void createObstacles() {
        fattenedObstacles = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            for (int j = (i % 2); j < 4; j += 2) {
                ArrayList<KPoint> pointList = new ArrayList<>();
                int numPoints = 6;
                double angleIncrement = Math.PI * 2f / (numPoints * 2);
                float rBig = 50;
                float rSmall = 20;
                double currentAngle = 0;
                for (int k = 0; k < numPoints; k++) {
                    double x = rBig * Math.cos(currentAngle);
                    double y = rBig * Math.sin(currentAngle);
                    pointList.add(new KPoint((float) x, (float) y));
                    currentAngle += angleIncrement;
                    x = rSmall * Math.cos(currentAngle);
                    y = rSmall * Math.sin(currentAngle);
                    pointList.add(new KPoint((float) x, (float) y));
                    currentAngle += angleIncrement;
                }
                KPolygon poly = new KPolygon(pointList);
                assert poly.isCounterClockWise();
                poly.rotate(i + j);
                poly.translate(100 + 100 * i, 100 + 100 * j);
                fattenedObstacles.add(PathBlockingObstacleImpl.createObstacleFromInnerPolygon(poly));
            }
        }
    }

    public static CollisionChecker instance() {
        return instnace;
    }

    public boolean hasCollided(Vector2d start, Vector2d end) {
        Line2D line = new Line2D.Double(start.getX(), start.getY(), end.getX(), end.getY());
        Area lineRegion = new Area(ShapeUtilities.createLineRegion(line, 0.5f));
        lineRegion.subtract(shape);
        return !lineRegion.isEmpty();
    }

    public void draw(Graphics bufferGraphics) {
        Graphics2D graphics = (Graphics2D) bufferGraphics;
        graphics.setColor(Color.GRAY);
        graphics.draw(shape);
        graphics.setColor(Color.DARK_GRAY);
        for (PathBlockingObstacleImpl stationaryObstacle : fattenedObstacles) {
            graphics.draw(stationaryObstacle.getOuterPolygon());
        }
    }

    public ArrayList<PathBlockingObstacleImpl> getStationaryObstacles() {
        ArrayList<PathBlockingObstacleImpl> newList = new ArrayList<>();
        for (PathBlockingObstacleImpl stationaryObstacle : fattenedObstacles) {
            newList.add(new PathBlockingObstacleImpl(stationaryObstacle.getOuterPolygon(), stationaryObstacle.getInnerPolygon()));
        }
        return newList;
    }

    public NodeConnector<PathBlockingObstacleImpl> getNodeConnector(ArrayList<PathBlockingObstacleImpl> obstacles) {
        // Connect the obstacles' nodes so that the PathFinder can do its work:
        float maxConnectionDistanceBetweenObstacles = 1000f;
        NodeConnector<PathBlockingObstacleImpl> nodeConnector;
        nodeConnector = new NodeConnector<>();
        for (PathBlockingObstacleImpl obstacle : obstacles) {
            nodeConnector.addObstacle(obstacle, obstacles, maxConnectionDistanceBetweenObstacles);
        }
        return nodeConnector;
    }

    public Vector2d getNearestPointOutsideOfObstacles(Vector2d point) {
        // check that the target point isn't inside any obstacles.
        // if so, move it.
        KPoint movedPoint = new KPoint(point.getX(), point.getY());
        boolean targetIsInsideObstacle = false;
        int count = 0;
        while (true) {
            for (PathBlockingObstacleImpl obst : fattenedObstacles) {
                if (obst.getOuterPolygon().contains(movedPoint)) {
                    targetIsInsideObstacle = true;
                    KPolygon poly = obst.getOuterPolygon();
                    KPoint p = poly.getBoundaryPointClosestTo(movedPoint);
                    if (p != null) {
                        movedPoint.x = p.x;
                        movedPoint.y = p.y;
                    }
                }
            }
            count++;
            if (!targetIsInsideObstacle || count >= 3) {
                break;
            }
        }
        return new Vector2d(movedPoint.getX(), movedPoint.getY());
    }

}
