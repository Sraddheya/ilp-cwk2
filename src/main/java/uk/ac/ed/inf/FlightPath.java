package uk.ac.ed.inf;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileWriter;

import com.mapbox.geojson.*;

public class FlightPath
{
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
    public FlightPath(String machine, String port){
        this.machine = machine;
        this.port = port;
    }

    public boolean createFlightPath(){
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

            return true;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public void addFlightPath(ArrayList<FlightPath.FlightDetails> flightDetails, String ordDate){

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

        ArrayList<Feature> fList = new ArrayList<>();
        for (FlightPath.FlightDetails f : flightDetails) {
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

}
