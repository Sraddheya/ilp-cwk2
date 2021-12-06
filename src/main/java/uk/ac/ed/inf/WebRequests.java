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
    private String machine;
    private String port;
    public ArrayList<ShopDetails1> shopList = new ArrayList<>();
    public Map<String, Integer> itemCostMap = new HashMap<>();
    public Map<String, LongLat> itemShopMap = new HashMap<>();

    /**
     * Immutable single client to be used for all requests
     */
    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * Class to help deserialise the JSON record
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
     * @param port port to be use din HTTP requests
     * @return cost of delivery
     * @throws ConnectException a connect error occurred because the webserver is
     *         not running or not running on the correct port
     * @throws IOException an Input/Output error occurred
     * @throws InterruptedException an interrupt error occurred
     */
    public WebRequests (String machine, String port){
        this.machine = machine;
        this.port = port;
    }

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

    public void parseMenu (){

        //Perform request (HttpRequest assumes that it is a GET request by default)
        String urlString = "http://" + machine + ":" + port + "/menus/menus.json";
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
        Type listType = new TypeToken<ArrayList<ShopDetails1>>() {}.getType();
        this.shopList = new Gson().fromJson(response.body(), listType);

        makeItemCost();
        makeItemsShop();
    }

    public void makeItemCost(){
        for (ShopDetails1 sd : this.shopList){
            for (ShopDetails1.Menu m : sd.menu){
                itemCostMap.put(m.item, m.pence);
            }
        }
    }

    public void makeItemsShop(){
        for (ShopDetails1 sd : this.shopList){
            for (ShopDetails1.Menu m : sd.menu){
                itemShopMap.put(m.item, w3wToLongLat(sd.location));
            }
        }
    }

    public int getDeliveryCost(ArrayList<String> args){
        //Standard delivery charge
        int total = 50;

        for (String a : args){
            total += itemCostMap.get(a);
        }

        return total;
    }

    public ArrayList<LongLat> getDeliveryCoordinates(ArrayList<String> args){
        ArrayList<LongLat> coordinates = new ArrayList<>();

        for (String a : args){
            coordinates.add(itemShopMap.get(a));
        }

        return coordinates;
    }

    public ArrayList<LongLat> getShopCoordinates(){
        ArrayList<LongLat> coordinates = new ArrayList<>();

        for (ShopDetails1 sd : shopList){
            coordinates.add(w3wToLongLat(sd.location));
        }

        return coordinates;
    }

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
