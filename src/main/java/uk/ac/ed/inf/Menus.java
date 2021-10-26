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
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;

/**
 * Class related to getting details about the shops and their menu's by connecting
 * to a web server.
 */
public class Menus {
    private String machine;
    private String port;

    /**
     * Immutable single client to be used for all requests
     */
    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * Class to help deserialise the JSON record
     */
    public class ShopDetails{
        String name;
        String location;
        List<Menu> menu;

        public class Menu{
            String item;
            int pence;
        }
    }

    /**
     * Constructor method
     *
     * @param machine machine to be used in HTTP requests
     * @param port port to be use din HTTP requests
     */
    public Menus (String machine, String port){
        this.machine = machine;
        this.port = port;
    }

    /**
     * Calculates the total cost of having the given items delivered (including
     * the standard delivery charge of 50p).
     *
     * @param args items to be delivered (as a variable number of strings)
     * @return cost of delivery
     * @throws ConnectException a connect error occurred because the webserver is
     *         not running or not running on the correct port
     * @throws IOException an Input/Output error occurred
     * @throws InterruptedException an interrupt error occurred
     */
    public int getDeliveryCost (String ...args){
        //Standard delivery charge
        int total = 50;

        //Perform request (HttpRequest assumes that it is a GET request by default)
        String urlString = "http://" + machine + ":" + port + "/menus/menus.json";
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

        //Deserialise response
        Type listType = new TypeToken<ArrayList<ShopDetails>>() {}.getType();
        ArrayList<ShopDetails> shopList = new Gson().fromJson(response.body(), listType);

        //Calculate total cost of having items delivered
        for (String param : args) {
            for (ShopDetails shop : shopList){
                for (ShopDetails.Menu food : shop.menu){
                    if (food.item.equals(param)){
                        total += food.pence;
                    }
                }
            }
        }

        return total;
    }
}
