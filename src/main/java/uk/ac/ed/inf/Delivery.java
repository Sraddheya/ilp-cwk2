package uk.ac.ed.inf;

import java.sql.*;

public class Delivery
{
    private String jdbcString;

    /**
     * Constructor method
     */
    public Delivery(String jdbcString){
        this.jdbcString = jdbcString;
    }

    public boolean createDelivery(){
        try {

            //CONNECTING TO A DATABASE
            Connection conn = DriverManager.getConnection(jdbcString);

            // Create a statement object that we can use for running various SQL statement commands against the database
            Statement statement = conn.createStatement();

            //DROPPING DELIVERIES IF IT ALREADY EXISTS
            DatabaseMetaData databaseMetadata = conn.getMetaData();
            ResultSet resultSet1 = databaseMetadata.getTables(null, null, "DELIVERIES", null);
            if (resultSet1.next()) { statement.execute("drop table deliveries"); }

            //CREATING A DATABASE TABLE
            statement.execute("create table deliveries(" + "orderNo char(8), " + "deliveredTo varchar(19), " + "costInPence int)");

            return true;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public static void main( String[] args ){
        //System.out.println(createDelivery());

    }
}
