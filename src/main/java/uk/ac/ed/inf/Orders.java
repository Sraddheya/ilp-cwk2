package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.LinkedList;

public class Orders {

    public static class OrderInfo{
        String orderNo;
        String customer;
        String deliverTo;
        ArrayList<String> items;
        LinkedList<LongLat> shopsll;
    }

}
