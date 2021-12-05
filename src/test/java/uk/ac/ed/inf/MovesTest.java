package uk.ac.ed.inf;

import com.mapbox.geojson.Polygon;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;

public class MovesTest {
    public static void main(String[] args) {
        //Get perimeter
        NoFlyZones nfz = new NoFlyZones("localhost","9898");
        ArrayList<Polygon> polys = nfz.getPolygons();
        ArrayList<Line2D> lines = nfz.getPerimeter(polys);

        Moves moves = new Moves();
        Landmarks landmarks = new Landmarks("localhost","9898");
        Menus menus = new Menus("localhost","9898");

        //false
        Line2D edgeOfPolygon = new Line2D.Double(-3.1916, 55.9437, -3.1909758, 55.9452678);
        System.out.println(moves.isIntersect(edgeOfPolygon, lines));
        //true
        Line2D willIntersect = new Line2D.Double(-3.1916, 55.9437, -3.1884, 55.9454);
        System.out.println(moves.isIntersect(willIntersect, lines));
        //false
        Line2D wontIntersect = new Line2D.Double(-3.1916, 55.9437, -3.1913, 55.9456);
        System.out.println(moves.isIntersect(wontIntersect, lines));

        LongLat curr = new LongLat(-3.186874, 55.944494);//AT

        ArrayList<String> items = new ArrayList<>();
        items.add("Ham and mozzarella Italian roll");
        menus.getDeliveryCost(items);

        /**for (LongLat l : locations.keySet()){
            System.out.println(l.longitude);
        }**/
    }
}
