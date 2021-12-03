package uk.ac.ed.inf;

import java.util.ArrayList;

public class MenusTest {
    public static void main(String[] args) {
        Menus menus = new Menus("localhost","9898");

        ArrayList<String> items = new ArrayList<>();

        items.add("Flaming tiger latte");
        items.add("Dirty matcha latte");
        items.add("Strawberry matcha latte");
        items.add("Fresh taro latte");

        //1890
        int cost = menus.getDeliveryCost(items);
        System.out.println(cost);

        //looks.cloud.daring
        ArrayList<String> locations = menus.getLocations(items);
        for (String l : locations){
            System.out.println(l);
        }
    }
}
