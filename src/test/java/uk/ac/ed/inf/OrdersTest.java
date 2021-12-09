package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrdersTest {
    private static final String MACHINE = "localhost";
    private static final String WEBPORT = "9898";
    private static final String JDBCPORT = "9876";
    private static final String TESTDATE = "2023-12-31";

    public static void main(String[] args) {
        //Connect to web server
        WebRequests webRequests = new WebRequests(MACHINE, WEBPORT);
        webRequests.parseMenu();

        //Connect to databases
        Databases databases = new Databases(MACHINE, JDBCPORT);

        Orders orders = new Orders();

        //Get orders
        Map<String, Orders.OrderInfo> allOrders = orders.getOrdersInfo(webRequests, databases, TESTDATE);
        HashMap<String, Integer> sortedOrders = orders.sortByDeliveryCost(webRequests, allOrders.values());

    }
}
