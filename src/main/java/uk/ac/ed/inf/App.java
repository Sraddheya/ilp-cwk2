package uk.ac.ed.inf;

import com.mapbox.geojson.Polygon;

import java.awt.geom.Line2D;
import java.lang.reflect.Array;
import java.util.*;

public class App
{
    private static final String MACHINE = "localhost";
    private static final String WEBPORT = "9898";
    private static final String JDBCPORT = "9876";
    private static final String TESTDATE = "2023-12-31";

    public static void main(String[] args) {
        //Connect to web server
        WebRequests webRequests = new WebRequests(MACHINE, WEBPORT);
        webRequests.parseMenu();

        //Connect to databases
        Databases databases = new Databases(MACHINE, JDBCPORT);

        Orders orders = new Orders();

        //Get orders
        Map<String, Orders.OrderInfo> allOrders = orders.getOrdersInfo(webRequests, databases, TESTDATE);

        //Sort orders by delivery cost
        HashMap<String, Integer> sortedOrdersNo = orders.sortByDeliveryCost(webRequests, databases, allOrders.values());

        //Get OrderNos of sortedOrderNo
        ArrayList<String> toDeliver = new ArrayList<>();
        toDeliver.addAll(sortedOrdersNo.keySet());

        //Set current location to Appleton Tower at beginning of deliveries;
        LongLat curr= new LongLat(-3.186874, 55.944494);
        LongLat at = new LongLat(-3.186874, 55.944494);

        //Create drone
        Drone drone = new Drone(webRequests.getNoFlyZone(), webRequests.getLandmarkCoordinates());

        while(!toDeliver.isEmpty()){

            Orders.OrderInfo currentOrder = allOrders.get(toDeliver.get(toDeliver.size() - 1));
            ArrayList<LongLat> shops = Orders.sortByShopDistance(webRequests, databases, curr, currentOrder.shops);
            LongLat dest = webRequests.w3wToLongLat(currentOrder.deliverTo);
            shops.add(dest);

            //Flying from curr to final destination after picking up items
            LongLat tempCurr = Drone.fly(currentOrder.orderNo, curr, shops, false);

            //Flying back to Appleton
            shops.clear();
            shops.add(at);
            Drone.fly(currentOrder.orderNo, curr, shops, true);

            int movesRemainingAfterDelivery = drone.remainingMoves - drone.movesToTempDest;

            if (movesRemainingAfterDelivery <= drone.movesToAppleton){
                //Fly back to Appleton
                //drone.remainingMoves = drone.remainingMoves - drone.movesToAppleton;
                //drone.deliveredMovement.addAll(drone.appletonMovement);
                toDeliver.clear();
            } else {
                //Make delivery
                curr = tempCurr;
                drone.remainingMoves = movesRemainingAfterDelivery;
                drone.deliveredMovement.addAll(drone.tempMovement);
                toDeliver.remove(toDeliver.size() - 1);
                drone.tempMovement = new ArrayList<>();
                drone.movesToTempDest = 0;
            }

        }

        //Fly back to Appleton
        drone.remainingMoves = drone.remainingMoves - drone.movesToAppleton;
        drone.deliveredMovement.addAll(drone.appletonMovement);

        Databases.addFlightPathToJson(drone.deliveredMovement, "1234");

        //Write orders to databases
    }


     /**
    public static void main( String[] args ) {

        //Connect to web servers
        Menus menus = new Menus(MACHINE, WEBPORT);
        What3Words w3w = new What3Words(MACHINE, WEBPORT);
        Landmarks landmarks = new Landmarks(MACHINE, WEBPORT);
        Moves moves = new Moves();

        //Connect to databases
        Delivery delivery = new Delivery(MACHINE, JDBCPORT);
        FlightPath flightPath = new FlightPath(MACHINE, JDBCPORT);
        Orders orders = new Orders(MACHINE, JDBCPORT);
        OrderDetails details = new OrderDetails(MACHINE, JDBCPORT);

        if (!delivery.createDelivery() || !flightPath.createFlightPath()) {
            //Error message if databases could not be made
            System.out.println("ERROR: Database tables could not be made");
        }

        //Get orders for specific date
        ArrayList<OrderInfo> ordersToDeliver = orders.getOrders(TESTDATE);

        //Array to store our delivery costs
        ArrayList<Integer> deliveryCosts = new ArrayList<>();

        for (OrderInfo o : ordersToDeliver) {
            //Get items for orderNo
            ArrayList<String> itemsList = details.getItems(o.orderNo);
            o.items = itemsList;

            //Get delivery cost for items
            int cost = menus.getDeliveryCost(itemsList);
            deliveryCosts.add(cost);

            //Get coordinates of shops that serve items
            ArrayList<String> coordinates_w3w = menus.getCoordinates(itemsList);

            //Turn all shop coordinates from w3w into longlat
            LinkedList<LongLat> coordinates_ll = new LinkedList<>();
            for (String str : coordinates_w3w) {
                coordinates_ll.add(w3w.wToLonLat(str));
            }
            o.shopsll = coordinates_ll;
        }

        //Get perimeter of no fly zones for planning flightpath
        NoFlyZones nfz = new NoFlyZones(MACHINE, WEBPORT);
        ArrayList<Polygon> polys = nfz.getPolygons();
        ArrayList<Line2D> perimeter = nfz.getPerimeter(polys);

        //Get landmarks for planning flightpath
        ArrayList<LongLat> allLandmarks = landmarks.getLandmarks();

        //Set current location to Appleton Tower at beginning of deliveries;
        LongLat currll = new LongLat(-3.186874, 55.944494);

        //Delivered
        ArrayList<OrderInfo> ordersDelivered = new ArrayList<>();
        ArrayList<Integer> costsOfDelivered = new ArrayList<>();

        while (!ordersToDeliver.isEmpty()) {
            //Get orderNo of order with max delivery cost
            int indexMaxCost = deliveryCosts.indexOf(Collections.max(deliveryCosts));
            OrderInfo currOrder = ordersToDeliver.get(indexMaxCost);

            //Fly drone until all the shops and the final delivery location have been visited
            LinkedList<LongLat> llToVisit = currOrder.shopsll;
            llToVisit.add(w3w.wToLonLat(currOrder.deliverTo));

            LongLat tempCurrll = currll;

            while (!llToVisit.isEmpty()) {
                LongLat tempDest = llToVisit.peek();
                Line2D line = new Line2D.Double(tempCurrll.longitude, tempCurrll.latitude, tempDest.longitude, tempDest.latitude);

                //Path is intersecting so we need to travel to a landmark instead
                if (moves.isIntersect(line, perimeter)) {
                    moves.toLandmark = true;
                    tempDest = moves.getIntermediate(tempCurrll, allLandmarks, perimeter);
                } else {
                    llToVisit.remove();
                }

                tempCurrll = moves.flyToDelivery(currOrder.orderNo, tempCurrll, tempDest);
                moves.toLandmark = false;

            }

            //Check if there are enough moves to fly back to Appleton
            int movesToAppleton = moves.flyToAppleton(currOrder.orderNo, currll);
            int movesRemainingAfterDelivery = moves.movesRemaining - moves.movesToTempDest;
            if (movesToAppleton >= movesRemainingAfterDelivery){
                //Do not make any more deliveries and go back to Appleton
                deliveryCosts.clear();
                ordersToDeliver.clear();
            } else {
                //Can make the delivery
                costsOfDelivered.add(deliveryCosts.get(indexMaxCost));
                deliveryCosts.remove(indexMaxCost);
                ordersDelivered.add(ordersToDeliver.get(indexMaxCost));
                ordersToDeliver.remove(indexMaxCost);
                moves.movementsDelivered.addAll(moves.tempMovement);
                moves.movesRemaining = movesRemainingAfterDelivery;
            }
            moves.movesToTempDest = 0;
            moves.tempMovement = new ArrayList<>();
        }

        //Drone flies back to Appleton
        moves.movementsDelivered.addAll(moves.atMovement);

        System.out.println(moves.movesRemaining);
        flightPath.addFlightPathToJson(moves.movementsDelivered, "1234");

    }**/
}
