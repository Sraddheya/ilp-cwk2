package uk.ac.ed.inf;

import java.awt.geom.Line2D;
import java.util.*;

/**
 * Class with methods related to flying the drone and creating the flightpath.
 */

public class Drone {
    private int remainingMoves = 1500;
    private int movesToAppleton = 0;
    private int movesToTempDest = 0;
    private ArrayList<Databases.FlightDetails> deliveredMovement = new ArrayList<>();
    private ArrayList<Databases.FlightDetails> appletonMovement = new ArrayList<>();
    private ArrayList<Databases.FlightDetails> tempMovement = new ArrayList<>();
    private ArrayList<Line2D> noFlyZones = new ArrayList<>();
    private ArrayList<LongLat> landmarkCoordinates = new ArrayList<>();
    private ArrayList<LongLat> shopCoordinates = new ArrayList<>();

    /**
     * Constructor method.
     *
     * @param noFlyZones no-fly zones
     * @param landmarkCoordinates coordinates of all the landmarks
     * @param shopCoordinates coordinates of all the shops we can order from
     */
    protected Drone(ArrayList<Line2D> noFlyZones, ArrayList<LongLat> landmarkCoordinates, ArrayList<LongLat> shopCoordinates){
        this.noFlyZones = noFlyZones;
        this.landmarkCoordinates = landmarkCoordinates;
        this.shopCoordinates = shopCoordinates;

    }

    /**
     * Getter method
     *
     * @return number of moves left until battery runs out
     */
    protected int getRemainingMoves(){
        return this.remainingMoves;
    }

    /**
     * Getter method
     *
     * @return number of moves taken to reach Appleton
     */
    protected int getMovesToAppleton(){
        return this.movesToAppleton;
    }

    /**
     * Getter method
     *
     * @return number of moves to reach the destination
     */
    protected int getMovesToTempDest(){
        return this.movesToTempDest;
    }

    /**
     * Getter method
     *
     * @return Flightpath to reach appleton or a delivery point
     */
    protected ArrayList<Databases.FlightDetails> getDeliveredMovement(){
        return this.deliveredMovement;
    }

    protected void addToDeliveredMovement(boolean toAppleton){
        if (toAppleton){
            this.deliveredMovement.addAll(appletonMovement);
        } else {
            this.deliveredMovement.addAll(tempMovement);
        }
    }

    /**
     * Setter method
     *
     * @param numMoves number of moves remaining until battery runs out
     */
    protected void setRemainingMoves(int numMoves){
        remainingMoves = numMoves;
    }

    /**
     * Resets number of moves taken to reach the destination to 0.
     */
    protected void resetMovesToTempDest(){
        this.movesToTempDest = 0;
    }

    /**
     * Resets flightpath to reach destination to new list
     */
    protected void resetTempMovement(){
        this.tempMovement = new ArrayList<>();
    }

    /**
     * Checks if the given line intersects with the perimeter of the no-fly zone.
     *
     * @param move line to checked
     * @param perimeter perimeter of the no-fly zone
     * @return whether the line intersects
     */
    private boolean isIntersect(Line2D move, ArrayList<Line2D> perimeter){
        for (Line2D line : perimeter){
            if (move.intersectsLine(line)){
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates the angle between two coordinates.
     *
     * @param currll starting coordinate
     * @param destll destination coordinate
     * @return angle between the coordinates
     */
    private int getAngle(LongLat currll, LongLat destll){
        double y = destll.latitude - currll.latitude;
        double x = destll.longitude - currll.longitude;
        double angle = Math.toDegrees(Math.atan2(y, x));
        return (int) Math.round(angle/10.0) * 10;
    }

    /**
     * Sort the given buildings by their distance from the given coordinate in ascending order.
     *
     * @param curr coordinate distance needs to be measured from
     * @param buildings buildings distance needs to be measured from
     * @return Sorted lost of building coordinates
     */
    private ArrayList<LongLat> sortBuildingsByDistances(LongLat curr, ArrayList<LongLat> buildings){
        HashMap<LongLat, Double> buildingMap = new HashMap<>();

        for (LongLat ll : buildings){
            buildingMap.put(ll, curr.distanceTo(ll));
        }

        buildingMap = Orders.sortByValueDouble(buildingMap);

        ArrayList<LongLat> ll = new ArrayList<>();
        ll.addAll(buildingMap.keySet());

        return ll;
    }

    /**
     * Getting the coordinate of the intermediate location the drone should fly to if it cannot fly to its
     * destination without centering the no-fly zone. The first choice of the intermediate destination should
     * be a landmark, but in the event that no landmarks are reachable the drone should fly to a shop.
     *
     * @param currll current location of the drone
     * @return intermediate destination
     */
    private LongLat getIntermediate(LongLat currll){
        ArrayList<LongLat> llLandmarks = sortBuildingsByDistances(currll, this.landmarkCoordinates);

        //Find the closest landmark
        while (!llLandmarks.isEmpty()) {
            LongLat closestLandmark = llLandmarks.get(0);
            Line2D line = new Line2D.Double(currll.longitude, currll.latitude, closestLandmark.longitude, closestLandmark.latitude);

            if (isIntersect(line, this.noFlyZones) || currll.closeTo(closestLandmark) || !closestLandmark.isConfined()) {
                llLandmarks.remove(0);
            } else {
                return closestLandmark;
            }
        }

        //Find the furthest shop
        ArrayList<LongLat> llshops = sortBuildingsByDistances(currll, this.shopCoordinates);
        while (!llshops.isEmpty()) {
            LongLat closestShop = llshops.get(llshops.size()-1);
            Line2D line = new Line2D.Double(currll.longitude, currll.latitude, closestShop.longitude, closestShop.latitude);

            if (isIntersect(line, this.noFlyZones) || currll.closeTo(closestShop) || !closestShop.isConfined()) {
                llshops.remove(llshops.size()-1);
            } else {
                return closestShop;
            }
        }

        return null;
    }

    /**
     * Get the flight path of the drone from the current to the destination location.
     *
     * @param orderNo order number of the order the drone is carrying
     * @param curr current location
     * @param dest destination location
     * @param toIntermediate is the drone flying to an intermediate location? If so, the drone should not hover
     * @param toAppleton is the drone flying to Appleton? If so, the drone should not hover
     * @return Coordinates at the end of the flightpath
     */
    private LongLat getMove(String orderNo, LongLat curr, LongLat dest, boolean toIntermediate, boolean toAppleton){
        ArrayList<Databases.FlightDetails> moves = new ArrayList<>();
        int numMoves = 0;
        String tempOrderNo;

        if(toAppleton){
            tempOrderNo = "00000000";
        } else {
            tempOrderNo = orderNo;
        }

        //Fly
        while (!curr.closeTo(dest)){
            Databases.FlightDetails newMove = new Databases.FlightDetails();
            newMove.orderNo = tempOrderNo;
            newMove.fromLong = curr.longitude;
            newMove.fromLat = curr.latitude;
            newMove.angle = getAngle(curr, dest);
            curr = curr.nextPosition(newMove.angle);
            newMove.toLong = curr.longitude;
            newMove.toLat = curr.latitude;
            moves.add(newMove);
            numMoves++;
        }
        //Hover
        if (!toIntermediate && !toAppleton){
            Databases.FlightDetails newMove = new Databases.FlightDetails();
            newMove.orderNo = tempOrderNo;
            newMove.fromLong = curr.longitude;
            newMove.fromLat = curr.latitude;
            newMove.angle = -999;
            newMove.toLong = curr.longitude;
            newMove.toLat = curr.latitude;
            moves.add(newMove);
            numMoves++;
        }

        if (toAppleton){
            this.movesToAppleton = numMoves;
            this.appletonMovement = moves;
        } else {
            this.movesToTempDest += numMoves;
            this.tempMovement.addAll(moves);
        }
        return curr;
    }

    /**
     * Get the flightpath of the drone for delivering all the orders or returning to Appleton.
     *
     * @param orderNo order number of the order the drone is carrying
     * @param curr current location
     * @param shops coordinate of shops the drone must pick up items from
     * @param toAppleton is the drone flying to Appleton? If so, the drone should not hover
     * @return Coordinates at the end of the flightpath
     */
    protected LongLat getFlightPath(String orderNo, LongLat curr, ArrayList<LongLat> shops, boolean toAppleton){
        LongLat tempCurr = curr;

        while (!shops.isEmpty()){
            LongLat tempDest = shops.get(0);
            Line2D line = new Line2D.Double(tempCurr.longitude, tempCurr.latitude, tempDest.longitude, tempDest.latitude);


            if (isIntersect(line, this.noFlyZones)) {
                //Path is intersecting so we need to travel to a landmark instead
                tempDest = getIntermediate(tempCurr);
                tempCurr = getMove(orderNo, tempCurr, tempDest, true, toAppleton);
            } else {
                //Can move straight to the destination
                shops.remove(0);
                tempCurr = getMove(orderNo, tempCurr, tempDest, false, toAppleton);
            }

        }

        return tempCurr;
    }

}
