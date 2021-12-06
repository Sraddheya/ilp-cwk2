package uk.ac.ed.inf;

import com.mapbox.geojson.Polygon;

import java.awt.geom.Line2D;
import java.util.*;

public class CreateFlightPathIsIntersect
{
    private static final String MACHINE = "localhost";
    private static final String WEBPORT = "9898";
    private static final String JDBCPORT = "9876";
    private static final String TESTDATE = "2023-12-31";


    public static void main( String[] args ) {

        /**
         //Create databases
         Delivery delivery = new Delivery(MACHINE, JDBCPORT);
         FlightPath flightPath = new FlightPath(MACHINE, JDBCPORT);

         if (!delivery.createDelivery() || !flightPath.createFlightPath()) {
         //Error message if databases could not be made
         System.out.println("ERROR: Database tables could not be made");
         }**/

        Menus menus = new Menus("localhost","9898");
        OrderDetails orderDetails = new OrderDetails("localhost", "9876");
        Moves moves = new Moves();
        Landmarks landmarks = new Landmarks("localhost","9898");
        ArrayList<String> items = new ArrayList<>();
        items.add("Ham and mozzarella Italian roll");
        menus.getDeliveryCost(items);

        //Get perimeter
        NoFlyZones nfz = new NoFlyZones("localhost","9898");
        ArrayList<Polygon> polys = nfz.getPolygons();
        ArrayList<Line2D> perimeter = nfz.getPerimeter(polys);

        String orderNo = "1234";
        LongLat at = new LongLat(-3.186874, 55.944494);//AT
        LongLat currll = new LongLat(-3.186103, 55.944656); //nile valley
        LongLat dest = new LongLat(-3.188174, 55.943551);//George square

        LinkedList<LongLat> linkedList = new LinkedList<>();
        LongLat temp1 = new LongLat(-3.191065, 55.945626);//rudis
        LongLat temp2 = new LongLat(-3.191257, 55.945626);//greggs
        linkedList.add(temp1);
        linkedList.add(temp2);
        linkedList.add(dest);

        //Get landmarks for planning moves
        ArrayList<LongLat> landmark_coordinates = landmarks.getLandmarks();

        //Fly drone until all the shops and the final delivery location have been visited
        LinkedList<LongLat> coordinatesToVisit = linkedList;
        System.out.println(linkedList.size());
        while (!coordinatesToVisit.isEmpty()){
            LongLat tempDest = coordinatesToVisit.peek();
            Line2D line = new Line2D.Double(currll.longitude, currll.latitude, tempDest.longitude, tempDest.latitude);

            //Path is intersecting so we need to travel to a landmark instead
            if (moves.isIntersect(line, perimeter)){
                moves.toLandmark = true;
                ArrayList<Double> distances = moves.getLandmarkDistances(landmark_coordinates, currll);

                //Iterate over landmarks until we find one that does not have an intersecting flightpath where landmarks are sorted in closeness to the current location
                while (!distances.isEmpty()) {
                    int indexMinll = distances.indexOf(Collections.min(distances));
                    tempDest = landmark_coordinates.get(indexMinll);
                    line = new Line2D.Double(currll.longitude, currll.latitude, tempDest.longitude, tempDest.latitude);
                    if (!moves.isIntersect(line, perimeter)) {
                        //Found suitable landmark
                        break;
                    } else {
                        //Flight to landmark still intersects with polygon
                        landmark_coordinates.remove(indexMinll);
                    }
                }
            }
            currll = moves.fly(orderNo, currll, tempDest);
            if (moves.toLandmark){
                moves.toLandmark = false;
            } else {
                coordinatesToVisit.remove();
            }

        }

        //Check if there are enough moves to fly back to Appleton
        int movesToAppleton = moves.flyToAppleton(orderNo, currll);
        int movesAfterOrder = moves.movesRemaining - moves.movesToTempDest;
        if (movesToAppleton >= movesAfterOrder){
            //clear orders
            moves.movement.addAll(moves.atMovement);
            moves.movesRemaining -= movesToAppleton;
        } else {
            moves.movement.addAll(moves.tempMovement);
            moves.movesRemaining = movesAfterOrder;
        }
        moves.movesToTempDest = 0;
        moves.tempMovement = new ArrayList<>();


        System.out.println(moves.movesRemaining);
        FlightPath flightPath = new FlightPath(MACHINE, JDBCPORT);
        flightPath.addFlightPathToJson(moves.movement, "1234");



    }
}
