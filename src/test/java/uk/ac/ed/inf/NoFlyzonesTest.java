package uk.ac.ed.inf;

import com.mapbox.geojson.Polygon;

import java.awt.geom.Line2D;
import java.util.ArrayList;

public class NoFlyzonesTest {
    public static void main(String[] args) {
        //Get perimeter
        NoFlyZones nfz = new NoFlyZones("localhost","9898");
        ArrayList<Polygon> polys = nfz.getPolygons();
        ArrayList<Line2D> lines = nfz.getPerimeter(polys);

        System.out.println(lines.get(0).getX1());//-3.1909758
        System.out.println(lines.get(0).getX2());//-3.1909195
    }
}
