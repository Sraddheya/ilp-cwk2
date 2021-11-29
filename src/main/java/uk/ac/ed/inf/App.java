package uk.ac.ed.inf;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

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

        /**What3Words w3w = new What3Words(MACHINE, WEBPORT);

        String orderNo = "1ad5f1ff";

        ArrayList<String> items = new ArrayList<>();
        items.add("Can of Fanta");
        items.add("Chicken and avocado wrap");
        items.add("Hummus, falafel and spicy tomato French country roll");

        ArrayList<LongL at> locs = new ArrayList<>();
        locs.add(w3w.wToLonLat("pest.round.peanut"));
        locs.add(w3w.wToLonLat("sketch.spill.puzzle"));**/

        //55.944377, 03.189904

        /**
        ArrayList<FlightPath.FlightDetails> flight = new ArrayList<>();
        FlightPath.FlightDetails f = new FlightPath.FlightDetails();
        f.orderNo = "123";
        f.fromLong = -3.191248;
        f.fromLat = 55.943892;
        f.angle = 0;
        f.toLong = -3.187052;
        f.toLat = 55.944534;

        FlightPath.FlightDetails g = new FlightPath.FlightDetails();
        g.orderNo = "124";
        g.fromLong = -3.187052;
        g.fromLat = 55.944534;
        g.angle = 0;
        g.toLong = -3.187675;
        g.toLat = 55.943989;

        flight.add(f);
        flight.add(g);

        FlightPath fp = new FlightPath(MACHINE, WEBPORT);
        fp.addFlightPath(flight, TESTDATE);
         **/

    }
}
