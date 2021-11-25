package uk.ac.ed.inf;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Polygon;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;

/**
 * Class related to getting details about the shops and their menu's by connecting
 * to a web server.
 */
public class NoFlyZones {
    private String machine;
    private String port;

    /**
     * Immutable single client to be used for all requests
     */
    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * Constructor method
     */
    public NoFlyZones(String machine, String port){
        this.machine = machine;
        this.port = port;

        //Perform request (HttpRequest assumes that it is a GET request by default)
        String urlString = "http://" + machine + ":" + port + "/buildings/no-fly-zones.geojson";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();

        //Send request to the Http Client
        HttpResponse<String> response = null;
        try {
            response = client.send(request, BodyHandlers.ofString());
        } catch (ConnectException e){
            System.out.println("Fatal error: Unable to connect to " + machine + " at port " + port + ".");
            System.exit(1); // Exit the application
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        FeatureCollection fc = FeatureCollection.fromJson(response.body());
        List<Feature> fcList = fc.features();

        for (Feature f : fcList){
            Geometry g = f.geometry();
            Polygon p = (Polygon)g;

        }
    }

}
