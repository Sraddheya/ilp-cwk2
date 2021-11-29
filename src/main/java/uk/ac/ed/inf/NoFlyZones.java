package uk.ac.ed.inf;

import com.mapbox.geojson.*;

import java.io.IOException;
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
    }

    public ArrayList<Polygon> getPolygons(){

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

        ArrayList<Polygon> polygons = new ArrayList<>();

        for (Feature f : fcList){
            Geometry g = f.geometry();
            Polygon p = (Polygon)g;
            polygons.add(p);
        }

        /**
        for (Polygon p : polygons){
            System.out.println(p.coordinates().contains(Point.fromLngLat( 55.944377, -3.1906419)));
            System.out.println(p.coordinates().contains(Point.fromLngLat( -3.1906419, 55.944377)));
            System.out.println(polygons.contains(Point.fromLngLat( -3.191749, 55.943093)));
            System.out.println(polygons.contains(Point.fromLngLat( 55.943093 , -3.191749)));

        }

        //LongLat x = new LongLat(-3.189904, 55.944377);

        //System.out.println(polygons.contains(Point.fromLngLat( -3.191749, 55.943093)));
        //System.out.println(polygons.contains(Point.fromLngLat( 55.943093 , -3.191749)));

        //System.out.println(polygons.get(0).toString());**/

        return polygons;
    }
}
