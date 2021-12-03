package uk.ac.ed.inf;

import java.util.ArrayList;

public class OrderDetailsTest {
    public static void main(String[] args) {
        OrderDetails orderDetails = new OrderDetails("localhost", "9876");

        //1ad5f1ff|Can of Fanta
        //1ad5f1ff|Chicken and avocado wrap
        //1ad5f1ff|Hummus, falafel and spicy tomato French country roll
        ArrayList<String> items = orderDetails.getItems("1ad5f1ff");
        for (String s : items){
            System.out.println(s);
        }
    }
}
