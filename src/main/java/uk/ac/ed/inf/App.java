package uk.ac.ed.inf;

import java.sql.*;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final String jdbcString = "jdbc:derby://localhost:9876/derbyDB";

    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );

        try {

            //CONNECTING TO A DATABASE----------------------------------------
            Connection conn = DriverManager.getConnection(jdbcString);

            // Create a statement object that we can use for running various
            // SQL statement commands against the database
            Statement statement = conn.createStatement();

            //DROPPING A TABLE IF IT EXISTS-----------------------------------
            DatabaseMetaData databaseMetadata = conn.getMetaData();
            // Note: must capitalise STUDENTS in the call to getTables
            ResultSet resultSet = databaseMetadata.getTables(null, null, "STUDENTS", null);
            // If the resultSet is not empty then the table exists, so we can drop it
            if (resultSet.next()) {statement.execute("drop table students");
            }

            //CREATING A DATABASE TABLE---------------------------------------
            statement.execute("create table students(" + "name varchar(48), " + "matric char(8), " + "edinyear int)");

            //INSERTING DATA INTO A TABLE-------------------------------------
            PreparedStatement psStudent = conn.prepareStatement(
                    "insert into students values (?, ?, ?)");
            for (Student s : studentList) {
                psStudent.setString(1, s.name);
                psStudent.setString(2, s.matric);
                psStudent.setInt(3, s.year);
                psStudent.execute();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }
}
