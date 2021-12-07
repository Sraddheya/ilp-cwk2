package uk.ac.ed.inf;

import java.awt.geom.Line2D;
import java.util.*;

public class Drone {
    static int movesRemaining = 1500;
    static int movesToTempDest = 0;
    static int movesToAppleton = 0;
    boolean toLandmark = false;
    static ArrayList<Databases.FlightDetails> tempMovement = new ArrayList<>();
    static ArrayList<Databases.FlightDetails> appletonMovement = new ArrayList<>();
    static ArrayList<Databases.FlightDetails> movementsDelivered = new ArrayList<>();
    private static WebRequests webRequests;
    ArrayList<LongLat> shopCoordinates = webRequests.getShopCoordinates();
    LongLat AT = new LongLat(-3.186874, 55.944494);

    public static boolean isIntersect(Line2D move, ArrayList<Line2D> perimeter){
        for (Line2D line : perimeter){
            if (move.intersectsLine(line)){
                return true;
            }
        }
        return false;
    }

    public static int getAngle(LongLat currll, LongLat destll){
        double y = destll.latitude - currll.latitude;
        double x = destll.longitude - currll.longitude;
        double angle = Math.toDegrees(Math.atan2(y, x));
        return (int) Math.round(angle/10.0) * 10;
    }

    public static ArrayList<Double> getLandmarkDistances(ArrayList<LongLat> landmarks, LongLat curr){
        ArrayList<Double> distances = new ArrayList<>();

        for (LongLat l: landmarks){
            double distance = curr.distanceTo(l);
            distances.add(distance);
        }

        return distances;
    }

    public static LongLat getIntermediate(LongLat currll, ArrayList<LongLat> landmarksll, ArrayList<Line2D> perimeter){
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

    public static LongLat getMoves(String orderNo, LongLat curr, LongLat dest, boolean toLandmark, boolean toAppleton){
        ArrayList<Databases.FlightDetails> moves = new ArrayList<>();
        int numMoves = 0;

        while (!curr.closeTo(dest)){
            Databases.FlightDetails newMove = new Databases.FlightDetails();
            newMove.orderNo = orderNo;
            newMove.fromLong = curr.longitude;
            newMove.fromLat = curr.latitude;
            newMove.angle = getAngle(curr, dest);
            curr = curr.nextPosition(newMove.angle);
            newMove.toLong = curr.longitude;
            newMove.toLat = curr.latitude;
            moves.add(newMove);
            numMoves++;
        }
        if (!toLandmark || !toAppleton){
            Databases.FlightDetails newMove = new Databases.FlightDetails();
            newMove.orderNo = orderNo;
            newMove.fromLong = curr.longitude;
            newMove.fromLat = curr.latitude;
            newMove.angle = -999;
            newMove.toLong = curr.longitude;
            newMove.toLat = curr.latitude;
            moves.add(newMove);
            numMoves++;
        }

        if (toAppleton){
            movesToAppleton = numMoves;
            appletonMovement = moves;
        } else {
            movesToTempDest = numMoves;
            tempMovement = moves;
        }
        return curr;
    }

    public static LongLat fly(String orderNo, LongLat curr, LongLat dest, ArrayList<LongLat> shops, boolean toAppleton){
        ArrayList<Line2D> noFlyZones = webRequests.getNoFlyZone();
        ArrayList<LongLat> landmarkCoordinates = webRequests.getLandmarkCoordinates();

        LongLat tempCurrll = curr;

        while (!shops.isEmpty()){
            LongLat tempDest = shops.get(0);
            Line2D line = new Line2D.Double(tempCurrll.longitude, tempCurrll.latitude, tempDest.longitude, tempDest.latitude);

            //Path is intersecting so we need to travel to a landmark instead
            if (isIntersect(line, noFlyZones)) {
                tempDest = getIntermediate(tempCurrll, landmarkCoordinates, noFlyZones);
                tempCurrll = getMoves(orderNo, tempCurrll, tempDest, true, false);
            } else {
                shops.remove(0);
                tempCurrll = getMoves(orderNo, tempCurrll, tempDest, false, false);
            }

        }

        return tempCurrll;
    }

}
