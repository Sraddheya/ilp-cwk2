package uk.ac.ed.inf;

import java.util.*;

public class App
{
    private static final LongLat AT_COORDINATES = new LongLat(-3.186874, 55.944494);
    private static final String MACHINE = "localhost";
    private static final String WEBPORT = "9898";
    private static final String JDBCPORT = "9876";
    private static final String TESTDATE = "2023-09-08";

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

            //Get the coordinates of the shops to pick up items from for this order
            Orders.OrderInfo currentOrder = allOrders.get(toDeliver.get(0));
            ArrayList<LongLat> shops = orders.sortByShopDistance(webRequests, curr, currentOrder.shops);
            LongLat dest = webRequests.w3wToLongLat(currentOrder.deliverTo);
            shops.add(dest);

            //Check destination and shops are in confinement area
            if (!orders.allConfined(shops)){
                toDeliver.remove(0);
                System.err.println("Order " + currentOrder.orderNo + " has a drop of location or shop to pick up an item from outside of the drone confinement area.");
                continue;
            }

            //Flying from curr to final destination after picking up items
            LongLat tempCurr = drone.getFlightPath(currentOrder.orderNo, curr, shops, false);

            //Flying back to Appleton
            shops.clear();
            shops.add(AT_COORDINATES);
            drone.getFlightPath(currentOrder.orderNo, curr, shops, true);

            //Check if there are enough moves to fly back to Appleton
            int movesRemainingAfterDelivery = drone.getRemainingMoves() - drone.getMovesToTempDest();

            if (movesRemainingAfterDelivery <= drone.getMovesToAppleton()){
                //Not enough moves so fly back to Appleton
                toDeliver.clear();
            } else {
                //Enough moves so make delivery
                curr = tempCurr;
                drone.setRemainingMoves(movesRemainingAfterDelivery);
                drone.addToDeliveredMovement(false);
                delivered.add(toDeliver.get(0));
                toDeliver.remove(0);
                System.out.println("move");
                drone.resetTempMovement();
                drone.resetMovesToTempDest();
            }

        }

        //Add flightpath for flying back to Appleton
        drone.addToDeliveredMovement(true);

        //Add flightpaths
        databases.addFlightPathToJson(drone.getDeliveredMovement(), "1234");
        databases.addFlightPathToDB(drone.getDeliveredMovement());
        System.out.println(drone.getRemainingMoves());

        //Add deliveries
        ArrayList<Orders.OrderInfo> deliveredInfo = new ArrayList<>();
        ArrayList<Integer> costs = new ArrayList<>();

        for (String o : delivered){
            deliveredInfo.add(allOrders.get(o));
            costs.add(sortedOrdersNo.get(o));
        }
        databases.addDeliveriesToDB(deliveredInfo, costs);
    }

}
