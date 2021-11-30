package uk.ac.ed.inf;

import com.mapbox.geojson.*;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.awt.geom.Line2D;
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
        return polygons;
    }

    public ArrayList<Line2D> getPerimeter(ArrayList<Polygon> polygons){
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
