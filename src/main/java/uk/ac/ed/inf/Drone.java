package uk.ac.ed.inf;

import java.awt.geom.Line2D;
import java.util.*;

public class Drone {
    static int remainingMoves = 1500;
    static int movesToAppleton = 0;
    static int movesToTempDest = 0;
    static ArrayList<Databases.FlightDetails> deliveredMovement = new ArrayList<>();
    static ArrayList<Databases.FlightDetails> appletonMovement = new ArrayList<>();
    static ArrayList<Databases.FlightDetails> tempMovement = new ArrayList<>();
    static ArrayList<Line2D> noFlyZones = new ArrayList<>();
    static ArrayList<LongLat> landmarkCoordinates = new ArrayList<>();

    public Drone(ArrayList<Line2D> noFlyZones, ArrayList<LongLat> landmarkCoordinates){
        this.noFlyZones = noFlyZones;
        this.landmarkCoordinates = landmarkCoordinates;

    }

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

    public static ArrayList<LongLat> getLandmarkDistances(LongLat curr){
        HashMap<LongLat, Double> landmarkMap = new HashMap<>();

        for (LongLat ll : landmarkCoordinates){
            landmarkMap.put(ll, curr.distanceTo(ll));
        }

        //System.out.println(deliveryMap.keySet());
        //System.out.println(deliveryMap.values());
        landmarkMap = Orders.sortByValueDouble(landmarkMap);
        //System.out.println(deliveryMap.keySet());
        //System.out.println(deliveryMap.values());

        ArrayList<LongLat> ll = new ArrayList<>();
        ll.addAll(landmarkMap.keySet());

        return ll;
    }

    public static LongLat getIntermediate(LongLat currll){
        ArrayList<LongLat> ll = getLandmarkDistances(currll);

        while (!ll.isEmpty()) {
            LongLat closestLandmark = ll.get(0);
            Line2D line = new Line2D.Double(currll.longitude, currll.latitude, closestLandmark.longitude, closestLandmark.latitude);

            if (isIntersect(line, noFlyZones)) {
                ll.remove(0);
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
            movesToTempDest += numMoves;
            tempMovement.addAll(moves);
        }
        return curr;
    }

    public static LongLat fly(String orderNo, LongLat curr, ArrayList<LongLat> shops, boolean toAppleton){
        LongLat tempCurr = curr;

        while (!shops.isEmpty()){
            LongLat tempDest = shops.get(0);
            Line2D line = new Line2D.Double(tempCurr.longitude, tempCurr.latitude, tempDest.longitude, tempDest.latitude);

            //Path is intersecting so we need to travel to a landmark instead
            if (isIntersect(line, noFlyZones)) {
                tempDest = getIntermediate(tempCurr);
                tempCurr = getMoves(orderNo, tempCurr, tempDest, true, false);
            } else {
                shops.remove(0);
                tempCurr = getMoves(orderNo, tempCurr, tempDest, false, false);
            }

        }

        return tempCurr;
    }

}
