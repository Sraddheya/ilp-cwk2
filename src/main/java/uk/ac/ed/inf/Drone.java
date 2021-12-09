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
    static ArrayList<LongLat> shopCoordinates = new ArrayList<>();

    public Drone(ArrayList<Line2D> noFlyZones, ArrayList<LongLat> landmarkCoordinates, ArrayList<LongLat> shopCoordinates){
        this.noFlyZones = noFlyZones;
        this.landmarkCoordinates = landmarkCoordinates;
        this.shopCoordinates = shopCoordinates;

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

    public static ArrayList<LongLat> getShopDistances(LongLat curr){
        HashMap<LongLat, Double> shopMap = new HashMap<>();

        for (LongLat ll : shopCoordinates){
            shopMap.put(ll, curr.distanceTo(ll));
        }

        //System.out.println(deliveryMap.keySet());
        //System.out.println(deliveryMap.values());
        shopMap = Orders.sortByValueDouble(shopMap);
        //System.out.println(deliveryMap.keySet());
        //System.out.println(deliveryMap.values());

        ArrayList<LongLat> ll = new ArrayList<>();
        ll.addAll(shopMap.keySet());

        return ll;
    }

    public static LongLat getIntermediate(LongLat currll){
        ArrayList<LongLat> llLandmarks = getLandmarkDistances(currll);

        while (!llLandmarks.isEmpty()) {
            LongLat closestLandmark = llLandmarks.get(0);
            Line2D line = new Line2D.Double(currll.longitude, currll.latitude, closestLandmark.longitude, closestLandmark.latitude);

            if (isIntersect(line, noFlyZones) || currll.closeTo(closestLandmark)) {
                llLandmarks.remove(0);
            } else {
                return closestLandmark;
            }
        }

        ArrayList<LongLat> llshops = getShopDistances(currll);
        while (!llshops.isEmpty()) {
            LongLat closestShop = llshops.get(llshops.size()-1);
            Line2D line = new Line2D.Double(currll.longitude, currll.latitude, closestShop.longitude, closestShop.latitude);

            if (isIntersect(line, noFlyZones) || currll.closeTo(closestShop)) {
                llshops.remove(llshops.size()-1);
            } else {
                return closestShop;
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
        if (!toLandmark && !toAppleton){
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
                System.out.println(orderNo);
                System.out.println(tempCurr.longitude + " " + tempCurr.latitude);
                System.out.println(tempDest.longitude + " " + tempDest.latitude);
                tempCurr = getMoves(orderNo, tempCurr, tempDest, true, toAppleton);
            } else {
                shops.remove(0);
                System.out.println(orderNo);
                System.out.println(tempCurr.longitude + " " + tempCurr.latitude);
                System.out.println(tempDest.longitude + " " + tempDest.latitude);
                tempCurr = getMoves(orderNo, tempCurr, tempDest, false, toAppleton);
            }

        }

        return tempCurr;
    }

}
