package uk.ac.ed.inf;

import java.sql.*;

public class FlightPath
{
    private String jdbcString;

    /**
     * Constructor method
     */
    public FlightPath(String jdbcString){
        this.jdbcString = jdbcString;
    }

    public boolean createFlightPath(){
        try {

            //CONNECTING TO A DATABASE
            Connection conn = DriverManager.getConnection(jdbcString);

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



    public static void main( String[] args ){
        //System.out.println(createFlightPath());
    }
}
