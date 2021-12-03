package uk.ac.ed.inf;

import java.util.ArrayList;
import java.awt.geom.Line2D;


public class Moves {
    /**
     * Constructor method
     */
    public Moves(){
    }

    public boolean isIntersect(Line2D move, ArrayList<Line2D> perimeter){
        for (Line2D line : perimeter){
            if (move.intersectsLine(line)){
                return true;
            }
        }
        return false;
    }

    public LongLat getClosest(ArrayList<LongLat> coordinates, LongLat destination){
        LongLat curr = coordinates.get(0);
        double distance = destination.distanceTo(coordinates.get(0));
        for (int i = 1; i<coordinates.size(); i++){
            double newDist = destination.distanceTo(coordinates.get(i));
            if (newDist<distance){
                curr = coordinates.get(i);
                distance = newDist;
            }
        }

        return curr;
    }

    /**public int getAngle(LongLat currll, LongLat destll){
        double y = currll.latitude - destll.latitude;
        double x = currll.longitude - destll.longitude;
        double angle = Math.atan2(y, x);
        int angle1 = int() Math.round(angle/10.0) * 10;
        return int() Math.round(angle/10.0) * 10;
    }

    public ArrayList<FlightPath.FlightDetails> getMoves(String orderNo, LongLat currll, LongLat destll){
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
