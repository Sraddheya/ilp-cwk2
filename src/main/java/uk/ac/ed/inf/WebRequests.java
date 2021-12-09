package uk.ac.ed.inf;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.*;

import java.awt.geom.Line2D;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebRequests {
    /**
     * The machine where the web server is running.
     */
    private String machine;
    /**
     * The port where the web server is running.
     */
    private String port;

    /**
     * The shops from which order are allowed to be placed.
     */
    public ArrayList<ShopDetails1> shopList = new ArrayList<>();
    /**
     * Map of each item and their corresponding cost.
     */
    public Map<String, Integer> itemCostMap = new HashMap<>();
    /**
     * Map of each item and their corresponding shop.
     */
    public Map<String, String> itemShopMap = new HashMap<>();
    /**
     * Immutable single client to be used for all requests.
     */
    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * Class to help deserialise the JSON record from Menu.
     */
    public class ShopDetails1{
        String name;
        String location;
        List<Menu> menu;

        public class Menu{
            String item;
            int pence;
        }
    }

    /**
     * Class to help deserialise the JSON record from each What3words file.
     */
    public static class WordDetails{
        ll coordinates;

        public static class ll{
            double lng;
            double lat;
        }
    }

    /**
     * Constructor method
     *
     * @param machine machine to be used in HTTP requests
     * @param port port to be used in HTTP requests
     */
    public WebRequests (String machine, String port){
        this.machine = machine;
        this.port = port;
    }

    /**
     * Converts what3words string to LongLat object.
     *
     * @param w3w string to be converted
     * @return Longlat object
     * @throws ConnectException a connect error occurred because the webserver is
     * not running or not running on the correct port
     * @throws IOException an Input/Output error occurred
     * @throws InterruptedException an interrupt error occurred
     */
    public LongLat w3wToLongLat(String w3w){
        String[] words = w3w.split("\\.");

        //Perform request (HttpRequest assumes that it is a GET request by default)
        String urlString = "http://" + machine + ":" + port + "/words/" + words[0] + "/" + words[1] + "/" + words[2] + "/details.json";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();

        //Send request to the Http Client
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (ConnectException e){
            System.out.println("Fatal error: Unable to connect to " + machine + " at port " + port + ".");
            System.exit(1); // Exit the application
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        //Deserialise response
        Type listType = new TypeToken<WordDetails>() {}.getType();
        WordDetails c = new Gson().fromJson(response.body(),listType);

        return new LongLat(c.coordinates.lng, c.coordinates.lat);
    }

    /**
     * Gets the information about each menu for each shop. Also calls the methods to get the maps
     * of the items and their cost and the items and their shop.
     *
     * @throws ConnectException a connect error occurred because the webserver is not running or
     * not running on the correct port
     * @throws IOException an Input/Output error occurred
     * @throws InterruptedException an interrupt error occurred
     */
    public void parseMenu (){

        //Perform request (HttpRequest assumes that it is a GET request by default)
        String urlString = "http://" + machine + ":" + port + "/menus/menus.json";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();

        //Send request to the Http Client
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (ConnectException e){
            System.err.println("Fatal error: Unable to connect to " + machine + " at port " + port + ".");
            System.exit(1); // Exit the application
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        //Deserialise response
        Type listType = new TypeToken<ArrayList<ShopDetails1>>() {}.getType();
        this.shopList = new Gson().fromJson(response.body(), listType);

        setItemCost();
        setItemsShop();
    }

    /**
     * Reformat the details of the menu into a map with the item name as the key and the item
     * cost as the value.
     */
    public void setItemCost(){
        for (ShopDetails1 sd : this.shopList){
            for (ShopDetails1.Menu m : sd.menu){
                itemCostMap.put(m.item, m.pence);
            }
        }
    }

    /**
     * Reformat the details of the menu into a map with the item name as the key and the coordinates
     * of the shop the item comes from as the value.
     */
    public void setItemsShop(){
        for (ShopDetails1 sd : this.shopList){
            for (ShopDetails1.Menu m : sd.menu){
                itemShopMap.put(m.item, sd.location);
            }
        }
    }

    /**
     * Calculates the total cost of having the given items delivered (including
     * the standard delivery charge of 50p).
     *
     * @param args items to be delivered
     * @return cost of delivery
     */
    public int getDeliveryCost(ArrayList<String> args){
        //Standard delivery charge
        int total = 50;

        for (String a : args){
            total += itemCostMap.get(a);
        }

        return total;
    }

    /**
     * Gets what3word coordinates of the shops the specified item needs to be picked up from. The
     * Each shop is only added once so that that all the items from that shop are picked up in one go.
     *
     * @param args items to be delivered
     * @return what3word coordinates of location of shop item needs to be picked up from
     */
    public ArrayList<String> getDeliveryCoordinates(ArrayList<String> args){
        ArrayList<String> coordinates = new ArrayList<>();

        for (String a : args){
            String shop = itemShopMap.get(a);
            if (!coordinates.contains(shop)) {
                coordinates.add(shop);
            }
        }

        return coordinates;
    }

    /**
     * Gets LongLat coordinates of the all the shops each order is allowed to order from.
     *
     * @return LongLat coordinates of location of shops
     */
    public ArrayList<LongLat> getShopCoordinates(){
        ArrayList<LongLat> coordinates = new ArrayList<>();

        for (ShopDetails1 sd : shopList){
            coordinates.add(w3wToLongLat(sd.location));
        }

        return coordinates;
    }

    /**
     * Gets LongLat coordinates of the all the landmarks.
     *
     * @return LongLat coordinates of landmarks
     */
    public ArrayList<LongLat> getLandmarkCoordinates(){
        //Perform request (HttpRequest assumes that it is a GET request by default)
        String urlString = "http://" + machine + ":" + port + "/buildings/landmarks.geojson";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();

        //Send request to the Http Client
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (ConnectException e){
            System.out.println("Fatal error: Unable to connect to " + machine + " at port " + port + ".");
            System.exit(1); // Exit the application
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        //Deserialise response
        FeatureCollection fc = FeatureCollection.fromJson(response.body());
        List<Feature> fCollection = fc.features();

        ArrayList<LongLat> coordinates = new ArrayList<>();

        for (Feature f: fCollection){
            Geometry g = f.geometry();
            Point p = (Point)g;
            LongLat ll = new LongLat(p.coordinates().get(0), p.coordinates().get(1));
            coordinates.add(ll);
        }

        return coordinates;
    }

    /**
     * Gets the no-fly zone as a list of Line2D objects.
     *
     * @return no-fly zone
     * @throws ConnectException a connect error occurred because the webserver is not running or
     * not running on the correct port
     * @throws IOException an Input/Output error occurred
     * @throws InterruptedException an interrupt error occurred
     */
    public ArrayList<Line2D> getNoFlyZone(){

        //Perform request (HttpRequest assumes that it is a GET request by default)
        String urlString = "http://" + machine + ":" + port + "/buildings/no-fly-zones.geojson";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();

        //Send request to the Http Client
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (ConnectException e){
            System.out.println("Fatal error: Unable to connect to " + machine + " at port " + port + ".");
            System.exit(1); // Exit the application
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        FeatureCollection fc = FeatureCollection.fromJson(response.body());
        List<Feature> fcList = fc.features();

        ArrayList<Polygon> polygons = new ArrayList<>();

        for (Feature f : fcList){
            Geometry g = f.geometry();
            Polygon p = (Polygon)g;
            polygons.add(p);
        }

        //Getting lines that make up the polygon
        ArrayList<Line2D> lines = new ArrayList<>();

        for (Polygon p : polygons){
            int numPoints = p.coordinates().get(0).size();

            for (int i = 0; i<numPoints-1; i++){
                Point p1 = p.coordinates().get(0).get(i);
                Point p2 = p.coordinates().get(0).get(i+1);
                Line2D line = new Line2D.Double(p1.longitude(), p1.latitude(), p2.longitude(), p2.latitude());
                lines.add(line);
            }
            //Add line from end to beginning of perimeter
            Point p1 = p.coordinates().get(0).get(numPoints-1);
            Point p2 = p.coordinates().get(0).get(0);
            Line2D line = new Line2D.Double(p1.longitude(), p1.latitude(), p2.longitude(), p2.latitude());
            lines.add(line);
        }
        return lines;
    }

}
