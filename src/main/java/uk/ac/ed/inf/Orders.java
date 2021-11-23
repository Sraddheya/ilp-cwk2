package uk.ac.ed.inf;

import java.sql.*;
import java.util.ArrayList;

public class Orders{
    private String machine;
    private String port;

    public class OrdersInfo{
        String orderNo;
        String customer;
        String deliverTo;
    }

    public Orders (String machine, String port){
        this.machine = machine;
        this.port = port;
    }

    public void getOrders(String ordDate){

        //CONNECTING TO A DATABASE
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:derby://" + machine + ":" + port + "/derbyDB");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        //CREATE array of orders
        ArrayList<OrdersInfo> ordersList = new ArrayList<>();

        //READ ORDERS OF SPECIFIC DATE
        final String coursesQuery = "select * from orders where deliveryDate=(?)";
        try {
            PreparedStatement psCourseQuery = conn.prepareStatement(coursesQuery);
            psCourseQuery.setString(1, ordDate);
            ResultSet rs = psCourseQuery.executeQuery();
            while (rs.next()) {
                OrdersInfo order = new OrdersInfo();
                order.orderNo = rs.getString("orderNo");
                order.customer = rs.getString("customer");
                order.deliverTo = rs.getString("deliverTo");
                System.out.println(order.orderNo);
                ordersList.add(order);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        //return ordersList;
    }

}
