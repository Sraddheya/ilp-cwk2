package uk.ac.ed.inf;

import java.sql.*;
import java.util.ArrayList;

public class OrderDetails {
    private String machine;
    private String port;

    public OrderDetails(String machine, String port){
        this.machine = machine;
        this.port = port;
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
                System.out.println(item);
                itemList.add(item);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return itemList;
    }

}
