package uk.ac.ed.inf;

import com.mapbox.geojson.Polygon;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class App
{
    private static final String MACHINE = "localhost";
    private static final String WEBPORT = "9898";
    private static final String JDBCPORT = "9876";
    private static final String TESTDATE = "2023-12-31";


    public static void main( String[] args ) {

        //Create databases
        Delivery delivery = new Delivery(MACHINE, JDBCPORT);
        FlightPath flightPath = new FlightPath(MACHINE, JDBCPORT);

        if (!delivery.createDelivery() || !flightPath.createFlightPath()) {
            //Error message if databases could not be made
            System.out.println("ERROR: Database tables could not be made");
        }

        //Get orders for specific date
        Orders orders = new Orders(MACHINE, JDBCPORT);
        ArrayList<Orders.OrdersInfo> ordersList = orders.getOrders(TESTDATE);

        OrderDetails details = new OrderDetails(MACHINE, JDBCPORT);
        What3Words w3w = new What3Words(MACHINE, WEBPORT);
        Menus menus = new Menus(MACHINE, WEBPORT);
        Landmarks landmarks = new Landmarks(MACHINE, WEBPORT);
        Moves moves = new Moves();

        //Get perimeter
        NoFlyZones nfz = new NoFlyZones(MACHINE,WEBPORT);
        ArrayList<Polygon> polys = nfz.getPolygons();
        ArrayList<Line2D> perimeter = nfz.getPerimeter(polys);

        ArrayList<Integer> deliveryCosts = new ArrayList<>();

        for (Orders.OrdersInfo o : ordersList){
            //Get items for orderNo
            ArrayList<String> itemsList = details.getItems(o.orderNo);
            o.items = itemsList;

            //Get delivery cost for items
            int cost = menus.getDeliveryCost(itemsList);
            deliveryCosts.add(cost);

            //Get coordinates of shops that serve items
            ArrayList<String> coordinates_w3w = menus.getCoordinates(itemsList);

            //Turn all shop coordinates from w3w into longlat
            //LongLat temp= w3w.wToLonLat(o.deliverTo);
            LinkedList<LongLat> coordinates_ll = new LinkedList<>();
            for (String str : coordinates_w3w){
                coordinates_ll.add(w3w.wToLonLat(str));
            }
            o.shopCoordinates = coordinates_ll;
        }

        //Get landmarks for planning moves
        ArrayList<LongLat> landmark_coordinates = landmarks.getLandmarks();

        //Set current location to Appleton Tower at beginning of deliveries;
        LongLat currll = new LongLat(-3.186874, 55.944494);//AT

        //Plan the moves
        int x = 0;
        while (!deliveryCosts.isEmpty()){
            //Get orderNo of order with max delivery cost
            int indexMaxCost = deliveryCosts.indexOf(Collections.max(deliveryCosts));
            Orders.OrdersInfo currOrder = ordersList.get(indexMaxCost);

            //Fly drone until all the shops and the final delivery location have been visited
            LinkedList<LongLat> coordinatesToVisit = currOrder.shopCoordinates;
            coordinatesToVisit.add(w3w.wToLonLat(currOrder.deliverTo));
            while (!coordinatesToVisit.isEmpty()){
                LongLat tempDest = coordinatesToVisit.peek();
                Line2D line = new Line2D.Double(currll.longitude, currll.latitude, tempDest.longitude, tempDest.latitude);

                //Path is intersecting so we need to travel to a landmark instead
                if (moves.isIntersect(line, perimeter)){
                    ArrayList<Double> distances = moves.getLandmarkDistances(landmark_coordinates, currll);

                    //Iterate over landmarks until we find one that does not have an intersecting flightpath where landmarks are sorted in closeness to the current location
                    while (!distances.isEmpty()){
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
                currll = moves.fly(currOrder.orderNo, currll, tempDest, x);
                x = x +1;
                coordinatesToVisit.poll();
            }
            deliveryCosts.remove(indexMaxCost);
        }

        //Add moves to flightpath json
        flightPath.addFlightPath(moves.movement, "1234");

    }
}
