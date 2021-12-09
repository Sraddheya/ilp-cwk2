package uk.ac.ed.inf;

import com.mapbox.geojson.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class Databases {
    private String machine;
    private String port;
    private Connection conn;

    /**
     * Class to help deserialise the JSON record
     */
    public static class FlightDetails{
        String orderNo;
        double fromLong;
        double fromLat;
        int angle;
        double toLong;
        double toLat;
    }

    /**
     * Constructor method
     */
    public Databases(String machine, String port){
        this.machine = machine;
        this.port = port;
        setUpConnection();
    }

    public void setUpConnection(){
        try {
            String link = "jdbc:derby://" + this.machine + ":" + this.port + "/derbyDB";
            this.conn = DriverManager.getConnection(link);
        } catch (Exception e){
            System.err.println("Fatal error: Unable to connect to " + this.machine + " at port " + this.port + ".");
            System.exit(1);
        }
    }

    public void createDelivery(){
        try {
           // Create a statement object that we can use for running various SQL statement commands against the database
            Statement statement = this.conn.createStatement();

            //DROPPING DELIVERIES IF IT ALREADY EXISTS
            DatabaseMetaData databaseMetadata = this.conn.getMetaData();
            ResultSet resultSet1 = databaseMetadata.getTables(null, null, "DELIVERIES", null);
            if (resultSet1.next()) { statement.execute("drop table deliveries"); }

            //CREATING A DATABASE TABLE
            statement.execute("create table deliveries(" + "orderNo char(8), " + "deliveredTo varchar(19), " + "costInPence int)");

        } catch (SQLException throwables) {
            System.err.println("Error: Unable to create table 'Deliveries'.");
            System.exit(1);

        }
    }

    public void addDeliveriesToDB(ArrayList<Orders.OrderInfo> orders, ArrayList<Integer> costs){
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

    public void createFlightPath(){
        try {
            // Create a statement object that we can use for running various SQL statement commands against the database
            Statement statement = this.conn.createStatement();

            //DROPPING FLIGHTPATH IF IT ALREADY EXISTS
            DatabaseMetaData databaseMetadata = this.conn.getMetaData();
            ResultSet resultSet2 = databaseMetadata.getTables(null, null, "FLIGHTPATH", null);
            if (resultSet2.next()) {statement.execute("drop table flightpath");}

            //CREATING A DATABASE TABLE
            statement.execute("create table flightpath(" + "orderNo char(8), " + "fromLongitude double, " + "fromLatitude double, " + "angle integer, " + "toLongitude double, " + "toLatitude double)");

        } catch (SQLException throwables) {
            System.err.println("Error: Unable to create table 'FlightPath'.");
            System.exit(1);
        }
    }

    public void addFlightPathToJson(ArrayList<FlightDetails> flightDetails, String ordDate){
        ArrayList<Feature> fList = new ArrayList<>();
        for (FlightDetails f : flightDetails) {
            ArrayList<Point> points = new ArrayList<>();
            Point fromPoint = Point.fromLngLat(f.fromLong, f.fromLat);
            Point toPoint = Point.fromLngLat(f.toLong, f.toLat);
            points.add(fromPoint);
            points.add(toPoint);
            LineString l = LineString.fromLngLats(points);
            Geometry g = (Geometry)l;
            Feature feat = Feature.fromGeometry(g);
            fList.add(feat);
        }

        FeatureCollection fc = FeatureCollection.fromFeatures(fList);

        try {
            File file = new File("drone-" + ordDate + ".txt");
            FileWriter writer = new FileWriter("drone-" + ordDate + ".txt");

            writer.write(fc.toJson());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addFlightPathToDB(ArrayList<FlightDetails> movements){
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

    public ArrayList<Orders.OrderInfo> getOrders(String ordDate){

        //CREATE array of orders
        ArrayList<Orders.OrderInfo> ordersList = new ArrayList<>();

        //READ ORDERS OF SPECIFIC DATE
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

    public ArrayList<String> getItems(String ordNo){

        //CREATE array of orders
        ArrayList<String> itemList = new ArrayList<>();

        //READ ORDERS OF SPECIFIC DATE
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
