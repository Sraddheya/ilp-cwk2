package uk.ac.ed.inf;

import java.util.ArrayList;

public class OrderTest {
    public static void main(String[] args) {
        Orders orders = new Orders("localhost", "9876");

        ArrayList<Orders.OrdersInfo> ordersInfo = orders.getOrders("2023-12-31");

        //7f4e551f
        //8cbb522e
        //62b4f805
        //1ad5f1ff
        for (Orders.OrdersInfo o: ordersInfo){
            System.out.println(o.orderNo);
        }
    }
}
