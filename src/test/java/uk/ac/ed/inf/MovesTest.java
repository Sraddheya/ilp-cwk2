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
        ArrayList<Line2D> perimeter = nfz.getPerimeter(polys);

        Moves moves = new Moves();
        Landmarks landmarks = new Landmarks("localhost","9898");
        Menus menus = new Menus("localhost","9898");

        //false
        //Line2D edgeOfPolygon = new Line2D.Double(-3.1916, 55.9437, -3.1909758, 55.9452678);
        //System.out.println(moves.isIntersect(edgeOfPolygon, lines));
        //true
        //Line2D willIntersect = new Line2D.Double(-3.1916, 55.9437, -3.1884, 55.9454);
        //System.out.println(moves.isIntersect(willIntersect, lines));
        //false
        //Line2D wontIntersect = new Line2D.Double(-3.1916, 55.9437, -3.1913, 55.9456);
        //System.out.println(moves.isIntersect(wontIntersect, lines));

        LongLat inter = new LongLat(-3.191594, 55.943658);//soderberg
        LongLat currll = new LongLat(-3.18933, 55.943389);//GSQ west
        ArrayList<LongLat> landmarksll = landmarks.getLandmarks();
        LongLat intermediate = moves.getIntermediate(currll, landmarksll, perimeter);
        System.out.println(inter.latitude==intermediate.latitude);

        LongLat inter1 = new LongLat(-3.186199, 55.945734);//Beirut
        LongLat currll1 = new LongLat(-3.186874, 55.944494);//AT
        LongLat intermediate1 = moves.getIntermediate(currll1, landmarksll, perimeter);
        System.out.println(inter1.latitude==intermediate1.latitude);
    }
}
