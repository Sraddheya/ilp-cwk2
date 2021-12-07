package uk.ac.ed.inf;

import java.util.ArrayList;

public class WebRequestsTest {
    private static final String MACHINE = "localhost";
    private static final String WEBPORT = "9898";

    public static void main( String[] args ) {

        //Connect to web servers
        WebRequests webRequests = new WebRequests(MACHINE, WEBPORT);

        //Test w3wToLongLat
        String w3w1 = "army.monks.grapes";
        LongLat ll1 = new LongLat(-3.191594, 55.943658);
        System.out.println((ll1.longitude == webRequests.w3wToLongLat(w3w1).longitude) && (ll1.latitude == webRequests.w3wToLongLat(w3w1).latitude)) ;

        webRequests.parseMenu();

        //Test getDeliveryCost and getDeliveryCoordinates
        ArrayList<String> items = new ArrayList<>();
        items.add("Flaming tiger latte");
        items.add("Dirty matcha latte");
        items.add("Strawberry matcha latte");
        items.add("Fresh taro latte");
        System.out.println((4 * 460 + 50) == webRequests.getDeliveryCost(items));
        ArrayList<String> deliveryCoordinates = webRequests.getDeliveryCoordinates(items);
        //LongLat delCord1 = new LongLat(-3.185332, 55.944656);//Bing tea
        //System.out.println((delCord1.longitude == deliveryCoordinates.get(0).longitude) && (delCord1.latitude == deliveryCoordinates.get(0).latitude));
        System.out.println(deliveryCoordinates.get(0).equals("looks.clouds.daring"));
        System.out.println(deliveryCoordinates.size()==1);

        //Test getShopCoordinates
        LongLat shop = new LongLat(-3.191065, 55.945626);//Rudis
        ArrayList<LongLat> shopCoordinates = webRequests.getShopCoordinates();
        System.out.println((shop.longitude == shopCoordinates.get(0).longitude) && (shop.latitude == shopCoordinates.get(0).latitude));


        //Test getLandmarkCoordinates
        LongLat landmark = new LongLat(-3.191594, 55.943658);//Soderberg
        ArrayList<LongLat> landmarkCoordinates = webRequests.getLandmarkCoordinates();
        System.out.println((landmark.longitude == landmarkCoordinates.get(0).longitude) && (landmark.latitude == landmarkCoordinates.get(0).latitude));

        System.out.println(webRequests.getNoFlyZone().size());//100
    }
}
