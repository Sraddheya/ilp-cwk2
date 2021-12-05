package uk.ac.ed.inf;

import com.mapbox.geojson.Point;
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
        LongLat curr = new LongLat(-3.186874, 55.944494);//AT
        LongLat dest = new LongLat(-3.188174, 55.943551);//George square

        LinkedList<LongLat> linkedList = new LinkedList<>();
        LongLat temp1 = new LongLat(-3.191065, 55.945626);//rudis
        LongLat temp2 = new LongLat(-3.191257, 55.945626);//greggs
        linkedList.add(temp1);
        linkedList.add(temp2);

        ArrayList<LongLat> landmarkll = landmarks.getLandmarks();
        while (!linkedList.isEmpty()){
            LongLat tempDest = linkedList.peek();
            Line2D line = new Line2D.Double(curr.longitude, curr.latitude, tempDest.longitude, tempDest.latitude);
            if (moves.isIntersect(line, perimeter)){
                ArrayList<Double> distances = moves.getLandmarkDistances(landmarkll, curr);
                while (distances.size()!=0){
                    int index_min = distances.indexOf(Collections.min(distances));
                    tempDest = landmarkll.get(index_min);
                    line = new Line2D.Double(curr.longitude, curr.latitude, tempDest.longitude, tempDest.latitude);
                    if (!moves.isIntersect(line, perimeter)) {
                        break;
                    } else {
                        landmarkll.remove(index_min);
                    }
                }
            }
            curr = moves.fly(orderNo, curr, tempDest);
            linkedList.poll();

        }
        moves.fly(orderNo, curr, dest);

        FlightPath flightPath = new FlightPath(MACHINE, JDBCPORT);
        flightPath.addFlightPath(moves.movement, "1234");



    }
}
