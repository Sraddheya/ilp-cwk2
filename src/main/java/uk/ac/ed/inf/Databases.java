package uk.ac.ed.inf;

import com.mapbox.geojson.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;

/**
 * Class to handle methods where access to the database is needed.
 */

public class Databases {
    /**
     * The machine where the database is running.
     */
    private String machine;
    /**
     * The port where the database is running.
     */
    private String port;
    /**
     * The Connection object use to send SQL queries.
     */
    private Connection conn;

    /**
     * Class to store flight path details.
     */
    protected static class FlightDetails{
        String orderNo;
        double fromLong;
        double fromLat;
        int angle;
        double toLong;
        double toLat;
    }

    /**
     * Constructor method
     *
     * @param machine machine to be used.
     * @param port port to be used.
     */
    protected Databases(String machine, String port){
        this.machine = machine;
        this.port = port;
        setUpConnection();
    }

    /**
     * Assigns the Connection object for the SQL queries.
     *
     * @throws Exception if the connection is not able to be established.
     */
    private void setUpConnection(){
        try {
            String link = "jdbc:derby://" + this.machine + ":" + this.port + "/derbyDB";
            this.conn = DriverManager.getConnection(link);
        } catch (Exception e){
            System.err.println("Fatal error: Unable to connect to " + this.machine + " at port " + this.port + ".");
            System.exit(1);
        }
    }

    /**
     * Creates the Delivery table in the database. If the table already exists, it is
     * first deleted, then created.
     *
     * @throws Exception if the table was not able to be created.
     */
    protected void createDelivery(){
        try {
           // Create a statement object that we can use for running various SQL statement commands against the database
            Statement statement = this.conn.createStatement();

            //Dropping deliveries if it already exists
            DatabaseMetaData databaseMetadata = this.conn.getMetaData();
            ResultSet resultSet1 = databaseMetadata.getTables(null, null, "DELIVERIES", null);
            if (resultSet1.next()) { statement.execute("drop table deliveries"); }

            //Creating the table
            statement.execute("create table deliveries(" + "orderNo char(8), " + "deliveredTo varchar(19), " + "costInPence int)");

        } catch (SQLException throwables) {
            System.err.println("Error: Unable to create table 'Deliveries'.");
            System.exit(1);

        }
    }

    /**
     * Adds the delivered orders to the Delivery table.
     *
     * @param orders order to be added to the table.
     * @param costs delivery costs of the order to be added to the table.
     * @throws Exception if the order could not be added.
     */
    protected void addDeliveriesToDB(ArrayList<Orders.OrderInfo> orders, ArrayList<Integer> costs){
      for (int i = 0; i<orders.size(); i++) {
            try {
                PreparedStatement stmt = this.conn.prepareStatement("insert into deliveries values (?,?,?)");
                stmt.setString(1, orders.get(i).orderNo);
                stmt.setString(2, orders.get(i).deliverTo);
                stmt.setInt(3, costs.get(i));
                stmt.execute();
            } catch (Exception e) {
                System.err.println("Error: Unable to add to table 'Deliveries'.");
                System.exit(1);
            }
        }
    }

    /**
     * Creates the FlightPath table in the database. If the table already exists, it is
     * first deleted, then created.
     *
     * @throws Exception if the table was not able to be created.
     */
    protected void createFlightPath(){
        try {
            // Create a statement object that we can use for running various SQL statement commands against the database
            Statement statement = this.conn.createStatement();

            //Dropping flightpath if it already exists
            DatabaseMetaData databaseMetadata = this.conn.getMetaData();
            ResultSet resultSet2 = databaseMetadata.getTables(null, null, "FLIGHTPATH", null);
            if (resultSet2.next()) {statement.execute("drop table flightpath");}

            //Creating the table
            statement.execute("create table flightpath(" + "orderNo char(8), " + "fromLongitude double, " + "fromLatitude double, " + "angle integer, " + "toLongitude double, " + "toLatitude double)");

        } catch (SQLException throwables) {
            System.err.println("Error: Unable to create table 'FlightPath'.");
            System.exit(1);
        }
    }

    /**
     * Adds the flight paths to a Json formatted file for testing.
     *
     * @param flightDetails flight paths to be added.
     * @param ordDate date of which we are delivering the orders.
     * @throws Exception
     */
    protected void addFlightPathToJson(ArrayList<FlightDetails> flightDetails, String[] ordDate){
        ArrayList<Point> pointList = new ArrayList<>();
        for (FlightDetails f : flightDetails) {
            Point fromPoint = Point.fromLngLat(f.fromLong, f.fromLat);
            Point toPoint = Point.fromLngLat(f.toLong, f.toLat);
            pointList.add(fromPoint);
            pointList.add(toPoint);
        }

        //Making geoJson file
        LineString finalPath = LineString.fromLngLats(pointList);
        Feature f = Feature.fromGeometry(finalPath);
        FeatureCollection fc = FeatureCollection.fromFeature(f);
        String json = fc.toJson();
        try {
            File geojsonPath = new File("drone-" + ordDate[2] + "-" + ordDate[1] + "-" + ordDate[0] + ".geojson");
            FileWriter writer = new FileWriter("drone-" + ordDate[2] + "-" + ordDate[1] + "-" + ordDate[0] + ".geojson", false);
            PrintWriter print_line = new PrintWriter(writer);
            print_line.println(json);
            print_line.close();
        } catch (Exception e) {
            System.err.println("Error: Unable to add  flight paths to GeoJson file.");
            System.exit(1);
        }
    }

    /**
     * Adds the delivered orders to the Delivery table.
     *
     * @param movements the flight paths to be added.
     * @throws Exception if the order could not be added.
     */
    protected void addFlightPathToDB(ArrayList<FlightDetails> movements){
        for (FlightDetails fd: movements){
            try{
                PreparedStatement stmt = this.conn.prepareStatement("insert into flightpath values (?,?,?,?,?,?)");
                stmt.setString(1,fd.orderNo);
                stmt.setDouble(2, fd.fromLong);
                stmt.setDouble(3, fd.fromLat);
                stmt.setInt(4,fd.angle);
                stmt.setDouble(5,fd.toLong);
                stmt.setDouble(6,fd.toLat);
                stmt.execute();
            } catch (Exception e) {
                System.err.println("Error: Unable to add to table 'FlightPath'.");
                System.exit(1);
            }
        }

    }

    /**
     * Gets the information about the orders placed on a specific date from the orders table.
     *
     * @param ordDate date of orders.
     * @return orders placed and their details.
     * @throws Exception if unable to get the orders on the date.
     */
    protected ArrayList<Orders.OrderInfo> getOrders(String ordDate){

        ArrayList<Orders.OrderInfo> ordersList = new ArrayList<>();

        //Reading orders placed on a specific date
        final String coursesQuery = "select * from orders where deliveryDate=(?)";

        try {
            PreparedStatement psCourseQuery = this.conn.prepareStatement(coursesQuery);
            psCourseQuery.setString(1, ordDate);
            ResultSet rs = psCourseQuery.executeQuery();
            while (rs.next()) {
                Orders.OrderInfo order = new Orders.OrderInfo();
                order.orderNo = rs.getString("orderNo");
                order.customer = rs.getString("customer");
                order.deliverTo = rs.getString("deliverTo");
                ordersList.add(order);
            }
        } catch (SQLException throwables) {
            System.err.println("Error: Unable get orders for date " + ordDate + ".");
            System.exit(1);
        }

        return ordersList;
    }

    /**
     * Gets the items listed for each order.
     *
     * @param ordNo the order number we want to get the items for.
     * @return items in that order.
     * @throws Exception if unable to get the items for that order.
     */
    protected ArrayList<String> getItems(String ordNo){

        ArrayList<String> itemList = new ArrayList<>();

        //Reading items for a specific order
        final String coursesQuery = "select * from orderDetails where orderNo=(?)";

        try {
            PreparedStatement psCourseQuery = this.conn.prepareStatement(coursesQuery);
            psCourseQuery.setString(1, ordNo);
            ResultSet rs = psCourseQuery.executeQuery();
            while (rs.next()) {
                String item = rs.getString("item");
                itemList.add(item);
            }
        } catch (SQLException throwables) {
            System.err.println("Error: Unable get orders for order number " + ordNo + ".");
            System.exit(1);
        }

        return itemList;
    }
}
