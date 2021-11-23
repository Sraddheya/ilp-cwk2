package uk.ac.ed.inf;

import java.sql.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class App 
{
    private static final String MACHINE = "localhost";
    private static final String PORT = "9898";
    private static final String JDBCPORT = "9876";
    private static final String JDBCSTR = "jdbc:derby://localhost:9876/derbyDB";
    private static final String TESTDATE = "2022-04-11";


    public static void main( String[] args ) {

        //Create databases
        Delivery delivery = new Delivery(JDBCSTR);
        FlightPath flightPath = new FlightPath(JDBCSTR);

        if (!delivery.createDelivery() || !flightPath.createFlightPath()) {
            //Error message if databases could not be made
            System.out.println("ERROR: Database tables could not be made");
        }

        //Get orders for specific date
        Orders orders = new Orders(MACHINE, JDBCPORT);
        orders.getOrders(TESTDATE);
        //ArrayList<Orders.OrdersInfo> ordersList = orders.getOrders(TESTDATE);

        /**
        for (Orders.OrdersInfo o : ordersList) {
            System.out.println(o.orderNo);
            System.out.println(o.customer);
            System.out.println(o.deliverTo);
        }**/


        //What3Words w3w = new What3Words(MACHINE, PORT);

        /**
        //CONNECTING TO A DATABASE----------------------------------------
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(jdbcString);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        //CREATE array of orders
        ArrayList<Orders> orderList = new ArrayList<>();

        //READ ORDERS OF SPECIFIC DATE
        final String coursesQuery = "select * from orders where deliveryDate=(?)";
        try {
            PreparedStatement psCourseQuery = conn.prepareStatement(coursesQuery);
            psCourseQuery.setString(1, sqlDate);
            ResultSet rs = psCourseQuery.executeQuery();
            while (rs.next()) {
                Orders temp = new Orders(rs.getString("orderNo"), rs.getString("customer"), rs.getString("deliverTo"));
                orderList.add(temp);
                System.out.println(temp.customer);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }**/

        //CONVERT WHAT3WORDS INTO LONG LAT




    }
}
