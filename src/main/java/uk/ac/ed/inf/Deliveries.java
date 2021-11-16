package uk.ac.ed.inf;

import java.sql.*;

public class Deliveries
{
    private static final String jdbcString = "jdbc:derby://localhost:9876/derbyDB";

    /**
     * Constructor method
     */
    public Deliveries (){
    }

    public static boolean createDelivery(){
        try {

            //CONNECTING TO A DATABASE----------------------------------------
            Connection conn = DriverManager.getConnection(jdbcString);

            // Create a statement object that we can use for running various
            // SQL statement commands against the database
            Statement statement = conn.createStatement();

            //DROPPING DELIVERIES IF IT EXISTS-----------------------------------
            DatabaseMetaData databaseMetadata = conn.getMetaData();
            ResultSet resultSet1 = databaseMetadata.getTables(null, null, "DELIVERIES", null);
            // If the resultSet is not empty then the table exists, so we can drop it
            if (resultSet1.next()) {statement.execute("drop table deliveries");}
            //CREATING A DATABASE TABLE---------------------------------------
            statement.execute("create table deliveries(" + "orderNo char(8), " + "deliveredTo varchar(19), " + "costInPence int)");

            return true;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public boolean addDelivery(){

        return true;
    }

    public static void main( String[] args ){
        System.out.println(createDelivery());
    }

}
