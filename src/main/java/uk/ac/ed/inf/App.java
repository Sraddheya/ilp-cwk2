package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.List;

public class App 
{
    private static final String MACHINE = "localhost";
    private static final String WEBPORT = "9898";
    private static final String JDBCPORT = "9876";
    private static final String JDBCSTR = "jdbc:derby://localhost:9876/derbyDB";
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
        //details.getItems("1ad5f1ff");

        //What3Words w3w = new What3Words(MACHINE, PORT);

        for (Orders.OrdersInfo o : ordersList){
            //Get items for orderNo
            ArrayList<String> itemsList = details.getItems(o.orderNo);

            //Get location of restaurants that serve items
            //Plan route
            //Check if there is enough moves
            //Add to databases
        }

    }
}
