package uk.ac.ed.inf;

import java.sql.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class App 
{
    private static final String jdbcString = "jdbc:derby://localhost:9876/derbyDB";
    private static final String sqlDate = "2022-04-11";


    public static void main( String[] args ) {

        /**
        //CONNECTING TO A DATABASE----------------------------------------
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(jdbcString);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


        //CREATE TABLES
        Deliveries.createDelivery();
        FlightPath.createFlightPath();

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
