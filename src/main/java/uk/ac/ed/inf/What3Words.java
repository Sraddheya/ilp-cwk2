package uk.ac.ed.inf;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class What3Words
{
    //CHANGE FROM STATIC
    private static String machine = "localhost";
    private static String port = "9898";

    /**
     * Immutable single client to be used for all requests
     */
    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * Class to help deserialise the JSON record
     */
    public static class details{
        String country;
        cdet square;
        String nearestPlace;
        ll coordinates;
        String words;
        String language;
        String map;

        public static class cdet{
            ll southwest;
            ll northeast;
        }

        public static class ll{
            double lng;
            double lat;
        }
    }

    public What3Words (String machine, String port){
        this.machine = machine;
        this.port = port;
    }

    public LongLat wToLonLat(String w3w){
        //Perform request (HttpRequest assumes that it is a GET request by default)
        String urlString = "http://" + machine + ":" + port + "/words/blocks/found/civic/details.json";
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
        Type listType = new TypeToken<What3Words.details>() {}.getType();
        What3Words.details c = new Gson().fromJson(response.body(),listType);

        return new LongLat(c.coordinates.lng, c.coordinates.lat);
    }

    public static void main( String[] args ) {


    }
}
