package uk.ac.ed.inf;

import java.lang.reflect.Array;
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

    // function to sort hashmap by values
    public static HashMap<LongLat, Double> sortByValue(HashMap<LongLat, Double> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<LongLat, Double> > list =
                new LinkedList<Map.Entry<LongLat, Double> >(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<LongLat, Double> >() {
            public int compare(Map.Entry<LongLat, Double> o1,
                               Map.Entry<LongLat, Double> o2)
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<LongLat, Double> temp = new LinkedHashMap<LongLat, Double>();
        for (Map.Entry<LongLat, Double> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    public ArrayList<Double> getLandmarkDistances(ArrayList<LongLat> landmarks, LongLat curr){
        ArrayList<Double> distances = new ArrayList<>();

        for (LongLat l: landmarks){
            double distance = curr.distanceTo(l);
            distances.add(distance);
        }

        return distances;
    }

    public int getAngle(LongLat currll, LongLat destll){
        double y = destll.latitude - currll.latitude;
        double x = destll.longitude - currll.longitude;
        double angle = Math.toDegrees(Math.atan2(y, x));
        return (int) Math.round(angle/10.0) * 10;
    }

    public LongLat fly(String orderNo, LongLat curr, LongLat dest, int x){
        while (!curr.closeTo(dest)){
            FlightPath.FlightDetails newMove = new FlightPath.FlightDetails();
            newMove.orderNo = orderNo;
            newMove.fromLong = curr.longitude;
            newMove.fromLat = curr.latitude;
            newMove.angle = getAngle(curr, dest);
            curr = curr.nextPosition(newMove.angle);
            newMove.toLong = curr.longitude;
            newMove.toLat = curr.latitude;
            System.out.println("move" + x);
            movement.add(newMove);
        }
        return curr;
    }

}
