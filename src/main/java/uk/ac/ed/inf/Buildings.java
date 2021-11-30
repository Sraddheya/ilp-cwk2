package uk.ac.ed.inf;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class Buildings
{
    private static String machine = "localhost";
    private static String port = "9898";

    /**
     * Immutable single client to be used for all requests
     */
    private static final HttpClient client = HttpClient.newHttpClient();

    public Buildings (String machine, String port){
        this.machine = machine;
        this.port = port;
    }

    public void getLandmarks(){
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
        List<Feature> x = fc.features();

        System.out.println(x);
    }

    public static void main( String[] args ) {
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
        List<Feature> fcList = fc.features();

        ArrayList<Point> landmarks = new ArrayList<>();

        for (Feature f : fcList){
            Geometry g = f.geometry();
            Point p = (Point)g;
            landmarks.add(p);
            System.out.println(p);
        }

    }
}
