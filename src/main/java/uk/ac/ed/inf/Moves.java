package uk.ac.ed.inf;

import java.util.*;
import java.awt.geom.Line2D;


public class Moves {

    public boolean isIntersect(Line2D move, ArrayList<Line2D> perimeter){
        for (Line2D line : perimeter){
            if (move.intersectsLine(line)){
                return true;
            }
        }
        return false;
    }

    public HashMap<LongLat, Double> getClosestBuilding(ArrayList<LongLat> buildings, LongLat curr){
        HashMap<LongLat, Double> unsortedDistances = new HashMap<LongLat, Double>();
        HashMap<LongLat, Double> sortedDistances = new HashMap<LongLat, Double>();

        for (LongLat b: buildings){
            double distance = curr.distanceTo(b);
            unsortedDistances.put(b, distance);
        }
        unsortedDistances.entrySet().stream().sorted(Map.Entry.comparingByValue())
                .forEachOrdered(x -> sortedDistances.put(x.getKey(), x.getValue()));

        return sortedDistances;
    }

    public int getAngle(LongLat currll, LongLat destll){
        double y = currll.latitude - destll.latitude;
        double x = currll.longitude - destll.longitude;
        double angle = Math.atan2(y, x);
        return (int) Math.round(angle/10.0) * 10;
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
