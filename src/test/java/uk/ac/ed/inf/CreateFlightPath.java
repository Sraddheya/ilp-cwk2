package uk.ac.ed.inf;
import com.mapbox.geojson.Polygon;

import java.awt.geom.Line2D;
import java.util.ArrayList;

public class CreateFlightPath {

    private static final String MACHINE = "localhost";
    private static final String WEBPORT = "9898";
    private static final String JDBCPORT = "9876";
    private static final String TESTDATE = "2023-12-31";

    public static void main( String[] args ){
        //Get perimeter
        NoFlyZones nfz = new NoFlyZones(MACHINE, WEBPORT);
        ArrayList<Polygon> polys = nfz.getPolygons();
        ArrayList<Line2D> lines = nfz.getPerimeter(polys);
    }
}
