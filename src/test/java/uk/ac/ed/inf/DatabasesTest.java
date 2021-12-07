package uk.ac.ed.inf;

import java.util.ArrayList;

public class DatabasesTest {
    private static final String MACHINE = "localhost";
    private static final String JDBCPORT = "9876";
    private static final String TESTDATE = "2023-12-31";

    public static void main(String[] args) {

        //Connect to databases
        Databases databases = new Databases(MACHINE, JDBCPORT);

        //Should not throw error message
        databases.createDelivery();
        databases.createFlightPath();

        //Test addDeliveryToDB

        //Test addFlightPathtoDB

        //Test addFlightPathToJaon

        //Test getOrders
        ArrayList<Orders.OrderInfo> orders = databases.getOrders(TESTDATE);
        int ordersSize = orders.size();
        System.out.println(orders.get(ordersSize-1).orderNo.equals("1ad5f1ff"));
        System.out.println(orders.get(ordersSize-2).orderNo.equals("62b4f805"));

        //Test getItems
        ArrayList<String> items = databases.getItems("1ad5f1ff");
        String item1 = "Can of Fanta";
        String item2 = "Chicken and avocado wrap";
        String item3 = "Hummus, falafel and spicy tomato French country roll";
        System.out.println(items.get(0).equals(item1));
        System.out.println(items.get(1).equals(item2));
        System.out.println(items.get(2).equals(item3));
    }
}
