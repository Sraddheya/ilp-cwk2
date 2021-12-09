package uk.ac.ed.inf;

/**
 * Class to handle methods concerning the longitude and latitude of the drone.
 */

public class LongLat {
    /**
     * Immutable constant variables to mark the edges of the confinement area.
     */
    public final double LONG_MAX = -3.184319;
    public final double lONG_MIN = -3.192473;
    public final double LAT_MAX = 55.946233;
    public final double LAT_MIN = 55.942617;

    /**
     * Immutable constant variables for the length one move can take
     */
    public final double LENGTH = 0.00015;

    public double longitude;
    public double latitude;

    /**
     * Constructor method
     *
     * @param longitude longitude of the start location of the drone
     * @param latitude latitude of the start location of the drone
     */
    public LongLat (double longitude, double latitude){
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /**
     * Returns true if the current point is within the confinement area.
     *
     * @return true if the current point is within the confinement area.
     */
    public boolean isConfined (){
        if (longitude <= LONG_MAX && longitude >= lONG_MIN
            && latitude <= LAT_MAX && latitude >= LAT_MIN){
            return true;
        }
        return false;
    }

    /**
     * Calculates pythagorean distance between the given point and the
     * current point.
     *
     * @param point to calculate distance between
     * @return distance between the two points
     */
    public double distanceTo (LongLat point){
        double distance = Math.sqrt(Math.pow((longitude-point.longitude), 2) + Math.pow((latitude-point.latitude), 2));
        return distance;
    }

    /**
     * Determines if the given point and current point are 'close' according
     * to the definition on page 3 of the coursework specification.
     *
     * "point1 is 'close' to point2 if the distance between point1 and point2 is
     * strictly less than the distance tolerance of 0.00015 degrees."
     *
     * @param point that we need to find out if we are 'close' to
     * @return true if the two points are "close"
     */
    public boolean closeTo (LongLat point){
        if (distanceTo(point) <= LENGTH){
            return true;
        }
        return false;
    }

    /**
     * Calculates the new coordinates of the drone after it makes a 'move' in
     * the direction of the given angle where we are following the definition
     * of a 'move' given on page 3 of the coursework specification.
     *
     * @param angle the angle the drone moves in the direction of
     * @return the new coordinates of the drone
     */
    public LongLat nextPosition (int angle){
        //Initialising new coordinate to return
        LongLat new_point = new LongLat(longitude, latitude);

        if (angle==-999){
            //Do nothing because drone is hovering so it remain in the same position
        } else {
            if ((angle % 10) == 0){
                //Calculate new coordinate
                double x = LENGTH * Math.cos(Math.toRadians(angle));
                double y = LENGTH * Math.sin(Math.toRadians(angle));
                new_point.longitude += x;
                new_point.latitude += y;
            }
            else {
                //Error message because angle does not satisfy the definition of a 'move'
                System.err.println("ERROR: Drone can only be sent in a direction which is a multiple of 10 degrees");
            }
        }
        return new_point;
    }
}
