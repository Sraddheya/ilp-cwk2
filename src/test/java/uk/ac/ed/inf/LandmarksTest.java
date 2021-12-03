package uk.ac.ed.inf;

import java.util.ArrayList;

public class LandmarksTest {
    public static void main(String[] args) {
        Landmarks landmarks = new Landmarks("localhost","9898");
        ArrayList<LongLat> ll = landmarks.getLandmarks();
        for (LongLat i : ll){
            System.out.println(i.longitude);
        }
    }
}
