package uk.ac.ed.inf;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final String jdbcString = "jdbc:derby://localhost:9876/derbyDB";

    public class Delivery{
        String orderNo;
        String deliveredTo;
        int costInPence;
    }


    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );

        try {

            //CONNECTING TO A DATABASE----------------------------------------
            Connection conn = DriverManager.getConnection(jdbcString);

            // Create a statement object that we can use for running various
            // SQL statement commands against the database
            Statement statement = conn.createStatement();

            //DROPPING DELIVERIES IF IT EXISTS-----------------------------------
            DatabaseMetaData databaseMetadata = conn.getMetaData();
            // Note: must capitalise STUDENTS in the call to getTables
            ResultSet resultSet1 = databaseMetadata.getTables(null, null, "DELIVERIES", null);
            // If the resultSet is not empty then the table exists, so we can drop it
            if (resultSet1.next()) {statement.execute("drop table deliveries");}
            //CREATING A DATABASE TABLE---------------------------------------
            statement.execute("create table deliveries(" + "orderNo char(8), " + "deliveredTo varchar(19), " + "costInPence int)");

            //DROPPING FLIGHTPATH IF IT EXISTS-----------------------------------
            // Note: must capitalise STUDENTS in the call to getTables
            ResultSet resultSet2 = databaseMetadata.getTables(null, null, "FLIGHTPATH", null);
            // If the resultSet is not empty then the table exists, so we can drop it
            if (resultSet2.next()) {statement.execute("drop table flightpath");}
            //CREATING A DATABASE TABLE---------------------------------------
            statement.execute("create table flightpath(" + "orderNo char(8), " + "fromLongitude double, " + "fromLatitude double, " + "angle integer, " + "toLongitude double, " + "toLatitude double)");

            /**
            //INSERTING DATA INTO A TABLE-------------------------------------
            PreparedStatement psOrder = conn.prepareStatement("insert into order values (?, ?, ?)");
            for (Delivery d : deliveryList) {
                psOrder.setString(1, d.orderNo);
                psOrder.setString(2, d.deliveredTo);
                psOrder.setInt(3, d.costInPence);
                psOrder.execute();
            }
             **/

            /**
            //READING DATA FROM A TABLE
            final String coursesQuery = "select * from orders where customer=(?)";
            PreparedStatement psCourseQuery = conn.prepareStatement(coursesQuery);
            psCourseQuery.setString(1, "s2271919");

            // Search for the student’s courses and add them to a list
            ArrayList<String> orderList = new ArrayList<>();
            ResultSet rs = psCourseQuery.executeQuery();
            while (rs.next()) {
                String order = rs.getString("orderNo");
                orderList.add(order);
                System.out.println(order);
            }
             **/

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }
}
