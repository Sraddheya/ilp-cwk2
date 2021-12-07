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
    }

    public void createDelivery(){
        try {
            //CONNECTING TO A DATABASE
            Connection conn = DriverManager.getConnection("jdbc:derby://" + machine + ":" + port + "/derbyDB");

            // Create a statement object that we can use for running various SQL statement commands against the database
            Statement statement = conn.createStatement();

            //DROPPING DELIVERIES IF IT ALREADY EXISTS
            DatabaseMetaData databaseMetadata = conn.getMetaData();
            ResultSet resultSet1 = databaseMetadata.getTables(null, null, "DELIVERIES", null);
            if (resultSet1.next()) { statement.execute("drop table deliveries"); }

            //CREATING A DATABASE TABLE
            statement.execute("create table deliveries(" + "orderNo char(8), " + "deliveredTo varchar(19), " + "costInPence int)");


        } catch (SQLException throwables) {
            throwables.printStackTrace();

        }
    }

    public void createFlightPath(){
        try {

            //CONNECTING TO A DATABASE
            Connection conn = DriverManager.getConnection("jdbc:derby://" + machine + ":" + port + "/derbyDB");

            // Create a statement object that we can use for running various SQL statement commands against the database
            Statement statement = conn.createStatement();

            //DROPPING FLIGHTPATH IF IT ALREADY EXISTS
            DatabaseMetaData databaseMetadata = conn.getMetaData();
            ResultSet resultSet2 = databaseMetadata.getTables(null, null, "FLIGHTPATH", null);
            if (resultSet2.next()) {statement.execute("drop table flightpath");}

            //CREATING A DATABASE TABLE
            statement.execute("create table flightpath(" + "orderNo char(8), " + "fromLongitude double, " + "fromLatitude double, " + "angle integer, " + "toLongitude double, " + "toLatitude double)");


        } catch (SQLException throwables) {
            throwables.printStackTrace();

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

    public void addFlightPathToDB(ArrayList<FlightDetails> flightDetails, String ordDate){
        /**
         try {
         //CONNECTING TO A DATABASE
         Connection conn = DriverManager.getConnection("jdbc:derby://" + machine + ":" + port + "/derbyDB");

         PreparedStatement psFlight = conn.prepareStatement("insert into flightpath values (?, ?, ?, ?, ?, ?)");
         for (FlightPath.FlightDetails f : flightDetails) {
         psFlight.setString(1, f.orderNo);
         psFlight.setDouble(2, f.fromLong);
         psFlight.setDouble(3, f.fromLat);
         psFlight.setInt(4, f.angle);
         psFlight.setDouble(5, f.toLong);
         psFlight.setDouble(6, f.toLat);
         psFlight.execute();
         }

         } catch (SQLException throwables) {
         throwables.printStackTrace();
         }**/
    }

    public ArrayList<Orders.OrderInfo> getOrders(String ordDate){

        //CONNECTING TO A DATABASE
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:derby://" + machine + ":" + port + "/derbyDB");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        //CREATE array of orders
        ArrayList<Orders.OrderInfo> ordersList = new ArrayList<>();

        //READ ORDERS OF SPECIFIC DATE
        final String coursesQuery = "select * from orders where deliveryDate=(?)";
        try {
            PreparedStatement psCourseQuery = conn.prepareStatement(coursesQuery);
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
            throwables.printStackTrace();
        }

        return ordersList;
    }

    public ArrayList<String> getItems(String ordNo){

        //CONNECTING TO A DATABASE
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:derby://" + machine + ":" + port + "/derbyDB");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        //CREATE array of orders
        ArrayList<String> itemList = new ArrayList<>();

        //READ ORDERS OF SPECIFIC DATE
        final String coursesQuery = "select * from orderDetails where orderNo=(?)";
        try {
            PreparedStatement psCourseQuery = conn.prepareStatement(coursesQuery);
            psCourseQuery.setString(1, ordNo);
            ResultSet rs = psCourseQuery.executeQuery();
            while (rs.next()) {
                String item = rs.getString("item");
                itemList.add(item);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return itemList;
    }
}
