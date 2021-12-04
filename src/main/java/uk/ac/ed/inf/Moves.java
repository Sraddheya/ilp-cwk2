package uk.ac.ed.inf;

import java.util.*;
import java.awt.geom.Line2D;


public class Moves {
    ArrayList<FlightPath.FlightDetails> movement = new ArrayList<>();

    public boolean isIntersect(Line2D move, ArrayList<Line2D> perimeter){
        for (Line2D line : perimeter){
            if (move.intersectsLine(line)){
                return true;
            }
        }
        return false;
    }

    /**public HashMap<LongLat, Double> getClosestBuilding(ArrayList<LongLat> buildings, LongLat curr){
        HashMap<LongLat, Double> unsortedDistances = new HashMap<LongLat, Double>();
        HashMap<LongLat, Double> sortedDistances = new HashMap<LongLat, Double>();

        for (LongLat b: buildings){
            double distance = curr.distanceTo(b);
            unsortedDistances.put(b, distance);
        }
        unsortedDistances.entrySet().stream().sorted(Map.Entry.comparingByValue())
                .forEachOrdered(x -> sortedDistances.put(x.getKey(), x.getValue()));

        return sortedDistances;
    }**/

    public int getAngle(LongLat currll, LongLat destll){
        double y = destll.latitude - currll.latitude;
        double x = destll.longitude - currll.longitude;
        double angle = Math.toDegrees(Math.atan2(y, x));
        return (int) Math.round(angle/10.0) * 10;
    }

    public LongLat fly(String orderNo, LongLat curr, LongLat dest){
        //for (int i = 0; i<50; i++){
        while (!curr.closeTo(dest)){
            FlightPath.FlightDetails newMove = new FlightPath.FlightDetails();
            newMove.orderNo = orderNo;
            newMove.fromLong = curr.longitude;
            newMove.fromLat = curr.latitude;
            newMove.angle = getAngle(curr, dest);
            curr = curr.nextPosition(newMove.angle);
            newMove.toLong = curr.longitude;
            newMove.toLat = curr.latitude;
            System.out.println("move");
            movement.add(newMove);
        }
        return curr;
    }

    /**public ArrayList<FlightPath.FlightDetails> getMoves(String orderNo, LongLat currll, LongLat destll){
        ArrayList<FlightPath.FlightDetails> moves = new ArrayList<>();

        while (!currll.closeTo(destll)) {
            FlightPath.FlightDetails tempMove = new FlightPath.FlightDetails();
            tempMove.orderNo = orderNo;
            tempMove.fromLong = currll.longitude;;
            tempMove.fromLat = currll.longitude;

            double angle = getAngle(currll, destll);
            tempMove.angle = angle;

            tempMove.toLong;
            tempMove.toLat;
        }

        return moves;
    }**/
}
