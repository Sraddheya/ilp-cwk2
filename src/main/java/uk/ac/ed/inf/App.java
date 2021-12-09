package uk.ac.ed.inf;

import com.mapbox.geojson.Polygon;

import java.awt.geom.Line2D;
import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.*;

public class App
{
    private static final LongLat AT_COORDINATES = new LongLat(-3.186874, 55.944494);
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
        databases.createDelivery();
        databases.createFlightPath();

        Orders orders = new Orders();

        //Get orders
        Map<String, Orders.OrderInfo> allOrders = orders.getOrdersInfo(webRequests, databases, TESTDATE);

        //Sort orders by delivery cost
        HashMap<String, Integer> sortedOrdersNo = orders.sortByDeliveryCost(webRequests, allOrders.values());

        //Get OrderNos of sortedOrderNo
        ArrayList<String> toDeliver = new ArrayList<>();
        toDeliver.addAll(sortedOrdersNo.keySet());

        //Store OrderNos of delivered orders
        ArrayList<String> delivered = new ArrayList<>();

        //Set current location to Appleton Tower at beginning of deliveries;
        LongLat curr= AT_COORDINATES;

        //Create drone
        Drone drone = new Drone(webRequests.getNoFlyZone(), webRequests.getLandmarkCoordinates(), webRequests.getShopCoordinates());

        while(!toDeliver.isEmpty()){

            Orders.OrderInfo currentOrder = allOrders.get(toDeliver.get(0));
            ArrayList<LongLat> shops = orders.sortByShopDistance(webRequests, curr, currentOrder.shops);
            LongLat dest = webRequests.w3wToLongLat(currentOrder.deliverTo);
            shops.add(dest);

            //Check destination and shops are in confinement area
            if (!orders.allConfined(shops)){
                toDeliver.remove(0);
                drone.tempMovement = new ArrayList<>();
                drone.movesToTempDest = 0;
                System.err.println("Order " + currentOrder.orderNo + "has a drop of location or shop to pick up an item from outside of the drone confinement area");
                continue;
            }

            //Flying from curr to final destination after picking up items
            LongLat tempCurr = drone.fly(currentOrder.orderNo, curr, shops, false);

            //Flying back to Appleton
            shops.clear();
            shops.add(AT_COORDINATES);
            drone.fly(currentOrder.orderNo, curr, shops, true);

            int movesRemainingAfterDelivery = drone.remainingMoves - drone.movesToTempDest;

            if (movesRemainingAfterDelivery <= drone.movesToAppleton){
                //Fly back to Appleton
                toDeliver.clear();
            } else {
                //Make delivery
                curr = tempCurr;
                drone.remainingMoves = movesRemainingAfterDelivery;
                drone.deliveredMovement.addAll(drone.tempMovement);
                delivered.add(toDeliver.get(0));
                toDeliver.remove(0);
                drone.tempMovement = new ArrayList<>();
                drone.movesToTempDest = 0;
                System.out.println("move"
                );
            }

        }

        //Fly back to Appleton
        drone.remainingMoves = drone.remainingMoves - drone.movesToAppleton;
        drone.deliveredMovement.addAll(drone.appletonMovement);

        databases.addFlightPathToJson(drone.deliveredMovement, "1234");
        databases.addFlightPathToDB(drone.deliveredMovement);

        ArrayList<Orders.OrderInfo> deliveredInfo = new ArrayList<>();
        ArrayList<Integer> costs = new ArrayList<>();

        for (String o : delivered){
            deliveredInfo.add(allOrders.get(o));
            costs.add(sortedOrdersNo.get(o));
        }
        databases.addDeliveriesToDB(deliveredInfo, costs);
    }

}
