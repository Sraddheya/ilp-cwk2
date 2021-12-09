package uk.ac.ed.inf;

import java.awt.geom.Line2D;
import java.util.*;

public class Drone {
    int remainingMoves = 1500;
    int movesToAppleton = 0;
    int movesToTempDest = 0;
    ArrayList<Databases.FlightDetails> deliveredMovement = new ArrayList<>();
    ArrayList<Databases.FlightDetails> appletonMovement = new ArrayList<>();
    ArrayList<Databases.FlightDetails> tempMovement = new ArrayList<>();
    ArrayList<Line2D> noFlyZones = new ArrayList<>();
    ArrayList<LongLat> landmarkCoordinates = new ArrayList<>();
    ArrayList<LongLat> shopCoordinates = new ArrayList<>();

    public Drone(ArrayList<Line2D> noFlyZones, ArrayList<LongLat> landmarkCoordinates, ArrayList<LongLat> shopCoordinates){
        this.noFlyZones = noFlyZones;
        this.landmarkCoordinates = landmarkCoordinates;
        this.shopCoordinates = shopCoordinates;

    }

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

    public ArrayList<LongLat> getBuildingDistances(LongLat curr, ArrayList<LongLat> buildings){
        HashMap<LongLat, Double> buildingMap = new HashMap<>();

        for (LongLat ll : buildings){
            buildingMap.put(ll, curr.distanceTo(ll));
        }

        buildingMap = Orders.sortByValueDouble(buildingMap);

        ArrayList<LongLat> ll = new ArrayList<>();
        ll.addAll(buildingMap.keySet());

        return ll;
    }

    public LongLat getIntermediate(LongLat currll){
        ArrayList<LongLat> llLandmarks = getBuildingDistances(currll, landmarkCoordinates);

        while (!llLandmarks.isEmpty()) {
            LongLat closestLandmark = llLandmarks.get(0);
            Line2D line = new Line2D.Double(currll.longitude, currll.latitude, closestLandmark.longitude, closestLandmark.latitude);

            if (isIntersect(line, noFlyZones) || currll.closeTo(closestLandmark)) {
                llLandmarks.remove(0);
            } else {
                return closestLandmark;
            }
        }

        ArrayList<LongLat> llshops = getBuildingDistances(currll, shopCoordinates);
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

    public LongLat getMoves(String orderNo, LongLat curr, LongLat dest, boolean toLandmark, boolean toAppleton){
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

    public LongLat fly(String orderNo, LongLat curr, ArrayList<LongLat> shops, boolean toAppleton){
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
