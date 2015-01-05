package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

/*
                                                     *
  *                                                          *
                               *                  *        .--.
   \/ \/  \/  \/                                        ./   /=*
     \/     \/      *            *                ...  (_____)
      \ ^ ^/                                       \ \_((^o^))-.     *
      (o)(O)--)--------\.                           \   (   ) \  \._.
      |    |  ||================((~~~~~~~~~~~~~~~~~))|   ( )   |     \
       \__/             ,|        \. * * * * * * ./  (~~~~~~~~~~~)    \
*        ||^||\.____./|| |          \___________/     ~||~~~~|~'\____/ *
         || ||     || || A            ||    ||          ||    |
  *      <> <>     <> <>          (___||____||_____)   ((~~~~~|   *
  *      http://www.asciiworld.com/-Santa-Claus-.html
 */

/**
 * Represents a series of coordinates in a placemark
 */
public class Polygon implements Geometry {


    private HashMap< ArrayList<LatLng>, Integer> polygonBoundaries;

    /**
     * Recieves text, with each line representing a tuple coordinates seperated by a comma
     * (longitude, latitude, altitude) This method converts these tuples into LatLng points,
     * and ignores the altitude component
     * @param text
     */
    public void createCoordinates(String text) {
        ArrayList<LatLng> mPointList = new ArrayList<LatLng>();
        String[] lines = text.trim().split("(\\s+)");
        for (String point : lines) {
            String[] coordinate = point.split(",");
            mPointList.add(Placemark.convertToLatLng(coordinate));
        }
        setGeometry(mPointList);
    }


    /**
     * Creates a new ArrayList of LatLng points and a HashMap of these ArrayLists, if it has
     * not been created already. The HashMap keys are the ArrayList of points, and the values
     * are a number, 0 or 1 which represent whether the point is an inner boundary or an outer
     * boundary.
     *
     * @param geometry  An object which represents a LatLng point to add to an ArrayList
     */
    public void setGeometry(Object geometry) {
        ArrayList<LatLng> mPointList = ((ArrayList<LatLng>) geometry);
        if (polygonBoundaries == null) {
            polygonBoundaries = new HashMap<ArrayList<LatLng>, Integer>();
            polygonBoundaries.put(mPointList, OUTER_BOUNDARY);
        } else {
            polygonBoundaries.put(mPointList, INNER_BOUNDARY);
        }
     }

    /**
     * @return A HashMap of an ArrayList of LatLng points
     */
    public Object getGeometry() {
        return polygonBoundaries;
    }
}
