package uk.ac.ed.inf;

import java.util.*;

/**
 * The main class which acts as the controller and is run when the jar file is executed.
 */

public class App
{
    /**
     * Immutable LongLat object coordinate of Appleton tower. This should be the start and end location
     */
    private static final LongLat AT_COORDINATES = new LongLat(-3.186874, 55.944494);
    /**
     * The machine where the webserver and the database will be running from.
     */
    private static String MACHINE = "localhost";
    /**
     * The port where the web server will be running.
     */
    private static String WEBPORT;
    /**
     * The port where the database will be running.
     */
    private static String JDBCPORT;
    /**
     * The date of the orders we want to calculate the flightpath for.
     */
    private static String DATE;

    public static void main(String[] args) {
        //Reading in data
        String[] dateArray = Arrays.copyOfRange(args, 0, 3);
        WEBPORT = args[3];
        JDBCPORT = args[4];

        //Formatting date from ["DD", "MM", "YYYY"] into YYYY-MM-DD
        Collections.reverse(Arrays.asList(dateArray));
        String formattedDate = String.join(",", dateArray);
        DATE = formattedDate.replace(",", "-");


        //Connect to web server
        WebRequests webRequests = new WebRequests(MACHINE, WEBPORT);
        webRequests.parseMenu();

        //Connect to databases
        Databases databases = new Databases(MACHINE, JDBCPORT);
        databases.createDelivery();
        databases.createFlightPath();

        Orders orders = new Orders();

        //Get orders
        Map<String, Orders.OrderInfo> allOrders = orders.getOrdersInfo(webRequests, databases, DATE);

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
                System.err.println("Error: Order " + currentOrder.orderNo + " has a drop of location or shop to pick up an item from outside of the drone confinement area.");
                continue;
            }

            //Check that the order has a minimum of one item and a maximum of four
            if(currentOrder.items.size()<1 || currentOrder.items.size()>4){
                toDeliver.remove(0);
                System.err.println("Error: Order should have a minimum of one item, and a maximum of four. This order has " + currentOrder.items.size() + " .");
                continue;
            }

            //Check that the order is made up of items from no more than two shops.
            //Note that the delivery location has been added as the final element in the shops list.
            if(currentOrder.shops.size()>3){
                toDeliver.remove(0);
                System.err.println("Error: Order can be made of items from no more than two shops. This order has items from " + (currentOrder.shops.size()-1) + " shops.");
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
        databases.addFlightPathToJson(drone.getDeliveredMovement(), dateArray);
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
