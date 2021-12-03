package uk.ac.ed.inf;

import com.mapbox.geojson.Polygon;

import java.awt.geom.Line2D;
import java.util.ArrayList;

public class MovesTest {
    public static void main(String[] args) {
        //Get perimeter
        NoFlyZones nfz = new NoFlyZones("localhost","9898");
        ArrayList<Polygon> polys = nfz.getPolygons();
        ArrayList<Line2D> lines = nfz.getPerimeter(polys);

        Moves moves = new Moves();

        //false
        Line2D edgeOfPolygon = new Line2D.Double(-3.1916, 55.9437, -3.1909758, 55.9452678);
        System.out.println(moves.isIntersect(edgeOfPolygon, lines));
        //true
        Line2D willIntersect = new Line2D.Double(-3.1916, 55.9437, -3.1884, 55.9454);
        System.out.println(moves.isIntersect(willIntersect, lines));
        //false
        Line2D wontIntersect = new Line2D.Double(-3.1916, 55.9437, -3.1913, 55.9456);
        System.out.println(moves.isIntersect(wontIntersect, lines));

        System.out.println((int) Math.round(199/10.0) * 10);//200
        System.out.println((int) Math.round(191/10.0) * 10);//190

        

    }
}
