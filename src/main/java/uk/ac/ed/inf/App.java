package uk.ac.ed.inf;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import java.awt.geom.Line2D;
import java.util.ArrayList;

public class App
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
        }

        //Get orders for specific date
        Orders orders = new Orders(MACHINE, JDBCPORT);
        ArrayList<Orders.OrdersInfo> ordersList = orders.getOrders(TESTDATE);

        //Sort orders

        OrderDetails details = new OrderDetails(MACHINE, JDBCPORT);
        What3Words w3w = new What3Words(MACHINE, WEBPORT);
        Menus menus = new Menus(MACHINE, WEBPORT);

        for (Orders.OrdersInfo o : ordersList){
            //Get items for orderNo
            ArrayList<String> itemsList = details.getItems(o.orderNo);

            //Get location of restaurants that serve items
            ArrayList<String> locationList = menus.getLocations(itemsList);

            //Turn all locations from w3w into longlat
            LongLat ordll = w3w.wToLonLat(o.deliverTo);
            ArrayList<LongLat> locListll = new ArrayList<>();

            for (String l: locationList){
                locListll.add(w3w.wToLonLat(l));
            }

            //Plan route
            //Check if there is enough moves
            //Add to databases
        }**/

        /**
         //Create databases
         Delivery delivery = new Delivery(MACHINE, JDBCPORT);
         FlightPath flightPath = new FlightPath(MACHINE, JDBCPORT);

         if (!delivery.createDelivery() || !flightPath.createFlightPath()) {
         //Error message if databases could not be made
         System.out.println("ERROR: Database tables could not be made");
         }**/

        //TEST WITH ONLY ONE ORDER
        What3Words w3w = new What3Words(MACHINE, WEBPORT);
        Menus menus = new Menus(MACHINE, WEBPORT);

        //Hard coded order details for one order
        Orders.OrdersInfo order = new Orders.OrdersInfo();
        order.orderNo = "1ad5f1ff";
        order.customer = "s2335903";
        order.deliverTo = "spell.stick.scale";

        //Longlat coordinate of order destination
        LongLat ordDest = w3w.wToLonLat(order.deliverTo);

        //Got order items for the one order
        ArrayList<String> items = new ArrayList<>();
        items.add("Can of Fanta");
        items.add("Chicken and avocado wrap");
        items.add("Hummus, falafel and spicy tomato French country roll");

        //Get location of restaurants that serve items
        ArrayList<String> locationList = menus.getLocations(items);

        //Turn all locations from w3w into longlat
        ArrayList<LongLat> locListll = new ArrayList<>();
        for (String l: locationList){
            locListll.add(w3w.wToLonLat(l));
        }
        locListll.add(ordDest);

        //Get perimeter
        NoFlyZones nfz = new NoFlyZones(MACHINE, WEBPORT);
        ArrayList<Polygon> polys = nfz.getPolygons();
        ArrayList<Line2D> lines = nfz.getPerimeter(polys);

        //Starting point
        LongLat currll = new LongLat(-3.186874, 55.9444494);

        //Store moves
        ArrayList<FlightPath.FlightDetails> move = new ArrayList<>();

        while (!currll.closeTo(ordDest)){
            //Get closest location

        }

    }
}
