package uk.ac.ed.inf;

import java.lang.reflect.Array;
import java.util.*;
import java.awt.geom.Line2D;


public class Moves {
    int movesRemaining = 1500;
    boolean toLandmark = false;
    int movesToTempDest = 0;
    ArrayList<FlightPath.FlightDetails> tempMovement = new ArrayList<>();
    ArrayList<FlightPath.FlightDetails> atMovement = new ArrayList<>();
    ArrayList<FlightPath.FlightDetails> movementsDelivered = new ArrayList<>();

    public boolean isIntersect(Line2D move, ArrayList<Line2D> perimeter){
        for (Line2D line : perimeter){
            if (move.intersectsLine(line)){
                return true;
            }
        }
        return false;
    }

    public int getAngle(LongLat currll, LongLat destll){
        double y = destll.latitude - currll.latitude;
        double x = destll.longitude - currll.longitude;
        double angle = Math.toDegrees(Math.atan2(y, x));
        return (int) Math.round(angle/10.0) * 10;
    }

    public ArrayList<Double> getLandmarkDistances(ArrayList<LongLat> landmarks, LongLat curr){
        ArrayList<Double> distances = new ArrayList<>();

        for (LongLat l: landmarks){
            double distance = curr.distanceTo(l);
            distances.add(distance);
        }

        return distances;
    }

    public LongLat getIntermediate(LongLat currll, ArrayList<LongLat> landmarksll, ArrayList<Line2D> perimeter){
        ArrayList<LongLat> tempLandmarks = landmarksll;
        ArrayList<Double> distances = getLandmarkDistances(landmarksll, currll);

        while (!tempLandmarks.isEmpty()) {
            int indexMinDistance = distances.indexOf(Collections.min(distances));
            LongLat closestLandmark = tempLandmarks.get(indexMinDistance);

            Line2D line = new Line2D.Double(currll.longitude, currll.latitude, closestLandmark.longitude, closestLandmark.latitude);

            if (isIntersect(line, perimeter)) {
                distances.remove(indexMinDistance);
                tempLandmarks.remove(indexMinDistance);
            } else {
                return closestLandmark;
            }
        }
        return null;
    }

    public LongLat flyToDelivery(String orderNo, LongLat curr, LongLat dest){
        while (!curr.closeTo(dest)){
            FlightPath.FlightDetails newMove = new FlightPath.FlightDetails();
            newMove.orderNo = orderNo;
            newMove.fromLong = curr.longitude;
            newMove.fromLat = curr.latitude;
            newMove.angle = getAngle(curr, dest);
            curr = curr.nextPosition(newMove.angle);
            newMove.toLong = curr.longitude;
            newMove.toLat = curr.latitude;
            tempMovement.add(newMove);
            movesToTempDest++;
        }
        if (!toLandmark){
            FlightPath.FlightDetails newMove = new FlightPath.FlightDetails();
            newMove.orderNo = orderNo;
            newMove.fromLong = curr.longitude;
            newMove.fromLat = curr.latitude;
            newMove.angle = -999;
            newMove.toLong = curr.longitude;
            newMove.toLat = curr.latitude;
            tempMovement.add(newMove);
            movesToTempDest++;
        }
        return curr;
    }

    public int flyToAppleton(String orderNo, LongLat curr){
        LongLat dest = new LongLat(-3.186874, 55.944494);
        int movesToAppleton = 0;
        while (!curr.closeTo(dest)){
            FlightPath.FlightDetails newMove = new FlightPath.FlightDetails();
            newMove.orderNo = orderNo;
            newMove.fromLong = curr.longitude;
            newMove.fromLat = curr.latitude;
            newMove.angle = getAngle(curr, dest);
            curr = curr.nextPosition(newMove.angle);
            newMove.toLong = curr.longitude;
            newMove.toLat = curr.latitude;
            atMovement.add(newMove);
            movesToAppleton++;

        }

        return movesToAppleton;
    }

}
