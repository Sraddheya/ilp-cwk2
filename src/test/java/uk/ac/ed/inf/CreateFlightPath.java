package uk.ac.ed.inf;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class CreateFlightPath
{
    private static final String MACHINE = "localhost";
    private static final String WEBPORT = "9898";
    private static final String JDBCPORT = "9876";
    private static final String TESTDATE = "2023-12-31";


    public static void main( String[] args ) {

        /**
         //Create databases
         Delivery delivery = new Delivery(MACHINE, JDBCPORT);
         FlightPath flightPath = new FlightPath(MACHINE, JDBCPORT);

         if (!delivery.createDelivery() || !flightPath.createFlightPath()) {
         //Error message if databases could not be made
         System.out.println("ERROR: Database tables could not be made");
         }**/

        Menus menus = new Menus("localhost","9898");
        OrderDetails orderDetails = new OrderDetails("localhost", "9876");
        Moves moves = new Moves();

        //Get perimeter
        NoFlyZones nfz = new NoFlyZones("localhost","9898");
        ArrayList<Polygon> polys = nfz.getPolygons();
        ArrayList<Line2D> perimeter = nfz.getPerimeter(polys);

        LongLat curr = new LongLat(-3.1869, 55.9445);//AT
        LongLat dest = new LongLat(-3.1882, 55.9436);//George square

        Queue<LongLat> q = new LinkedList<>();
        LongLat temp1 = new LongLat(-3.1861, 55.9447);//nile valley
        LongLat temp2 = new LongLat(-3.1913, 55.9456);//greggs
        q.add(temp1);
        q.add(temp2);

        LongLat tempDest = q.peek();
        while (!q.isEmpty()){
            Line2D line = new Line2D.Double(curr.longitude, curr.latitude, tempDest.longitude, tempDest.latitude);
            if (moves.isIntersect(line, perimeter)){
                tempDest = closest landmark
            } else {
                fly
                q.poll();
                LongLat tempDest = q.peek();
            }

        }


    }
}
