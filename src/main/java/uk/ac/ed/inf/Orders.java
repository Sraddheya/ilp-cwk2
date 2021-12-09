package uk.ac.ed.inf;

import java.util.*;
import java.util.stream.Collectors;

public class Orders {

    /**
     * Class to store information about each order.
     */
    public static class OrderInfo{
        String orderNo;
        String customer;
        String deliverTo;
        ArrayList<String> items;
        ArrayList<String> shops;
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

    /**
     * Gets information about each order placed on the specified date.
     *
     * @param webRequests WebRequest object
     * @param databases Databases object
     * @param date date that the orders were placed
     * @return Map of orders where the key is the orderNo and the value is the information about the order
     */
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

    /**
     * Sorts the orders by their delivery cost in descending order.
     *
     * @param webRequests WebRequest object
     * @param orders Orders to be sorted
     * @return Orders sorted in Hashmap format where the key is the orderNo and the value is it's delivery cost
     */
    public HashMap<String, Integer> sortByDeliveryCost(WebRequests webRequests, Collection<OrderInfo> orders){
        HashMap<String, Integer> unsortedMap = new HashMap<>();

        for (OrderInfo o : orders){
            unsortedMap.put(o.orderNo, webRequests.getDeliveryCost(o.items));
        }

        //LinkedHashMap preserve the ordering of elements in which they are inserted
        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();

        //Use Comparator.reverseOrder() for reverse ordering
        unsortedMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));

        return  sortedMap;
    }

    /**
     * Sorts all the shops by their distance from the given coordinate in ascending order.
     *
     * @param webRequests WebRequest object
     * @param curr location which the distances need to be calculated from
     * @param shops all the shops order can be placed from
     * @return List of coordinates of shops sorted by their distance form the given coordinate
     */
    public ArrayList<LongLat> sortByShopDistance(WebRequests webRequests, LongLat curr, ArrayList<String> shops){
        HashMap<LongLat, Double> shopMap = new HashMap<>();

        for (String s : shops){
            LongLat ll = webRequests.w3wToLongLat(s);
            shopMap.put(ll, curr.distanceTo(ll));
        }

        shopMap = sortByValueDouble(shopMap);

        ArrayList<LongLat> ll = new ArrayList<>();
        ll.addAll(shopMap.keySet());

        return ll;
    }

    /**
     * Checks if all the points given are within the confinement zone.
     *
     * @param coordinates points to check
     * @return whether all the points are in the confinement zone or not
     */
    public boolean allConfined(ArrayList<LongLat> coordinates){
        for (LongLat l : coordinates){
            if (!l.isConfined()){
                return false;
            }
        }
        return true;
    }

}
