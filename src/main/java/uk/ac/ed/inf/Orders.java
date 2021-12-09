package uk.ac.ed.inf;

import javax.xml.crypto.Data;
import java.util.*;
import java.util.stream.Collectors;

public class Orders {

    public static class OrderInfo{
        String orderNo;
        String customer;
        String deliverTo;
        ArrayList<String> items;
        ArrayList<String> shops;
    }

    // function to sort hashmap by values in ascending order
    public static HashMap<String, Integer>
    sortByValue(HashMap<String, Integer> hm)
    {
        HashMap<String, Integer> temp
                = hm.entrySet()
                .stream()
                .sorted((i1, i2)
                        -> i1.getValue().compareTo(
                        i2.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));

        return temp;
    }

    // function to sort hashmap by values in ascending order
    public static HashMap<LongLat, Double>
    sortByValueDouble(HashMap<LongLat, Double> hm)
    {
        HashMap<LongLat, Double> temp
                = hm.entrySet()
                .stream()
                .sorted((i1, i2)
                        -> i1.getValue().compareTo(
                        i2.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));

        return temp;
    }

    public Map<String, OrderInfo> getOrdersInfo(WebRequests webRequests, Databases databases, String date){
        Map<String, OrderInfo> ordersMap = new HashMap<>();

        ArrayList<OrderInfo> orders = databases.getOrders(date);

        for (OrderInfo o : orders){
            o.items = databases.getItems(o.orderNo);
            o.shops = webRequests.getDeliveryCoordinates(o.items);
            ordersMap.put(o.orderNo, o);
        }
        return ordersMap;
    }

    public HashMap<String, Integer> sortByDeliveryCost(WebRequests webRequests, Databases databases, Collection<OrderInfo> orders){
        HashMap<String, Integer> deliveryMap = new HashMap<>();

        for (OrderInfo o : orders){
            deliveryMap.put(o.orderNo, webRequests.getDeliveryCost(o.items));
        }

        //System.out.println(deliveryMap.keySet());
        //System.out.println(deliveryMap.values());
        deliveryMap = sortByValue(deliveryMap);
        //System.out.println(deliveryMap.keySet());
        //System.out.println(deliveryMap.values());

        return  deliveryMap;
    }

    public static ArrayList<LongLat> sortByShopDistance(WebRequests webRequests, LongLat curr, ArrayList<String> shops){
        HashMap<LongLat, Double> shopMap = new HashMap<>();

        for (String s : shops){
            LongLat ll = webRequests.w3wToLongLat(s);
            shopMap.put(ll, curr.distanceTo(ll));
        }

        //System.out.println(deliveryMap.keySet());
        //System.out.println(deliveryMap.values());
        shopMap = sortByValueDouble(shopMap);
        //System.out.println(deliveryMap.keySet());
        //System.out.println(deliveryMap.values());

        ArrayList<LongLat> ll = new ArrayList<>();
        ll.addAll(shopMap.keySet());

        return ll;
    }

}
