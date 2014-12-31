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

    @Override
    public void createCoordinates(String text) {
        ArrayList<LatLng> mPointList = new ArrayList<LatLng>();
        String[] lines = text.trim().split("(\\s+)");
        for (String point : lines) {
            String[] coordinate = point.split(",");
            mPointList.add(Placemark.convertToLatLng(coordinate));
            setGeometry(mPointList);
        }
    }

    public void setGeometry(Object geometry) {
        ArrayList<LatLng> mPointList = ((ArrayList<LatLng>) geometry);
        if (polygonBoundaries == null) {
            polygonBoundaries = new HashMap<ArrayList<LatLng>, Integer>();
            polygonBoundaries.put(mPointList, OUTER_BOUNDARY);
        } else {
            polygonBoundaries.put(mPointList, INNER_BOUNDARY);
        }
     }

    @Override
    public Object getGeometry() {
        return polygonBoundaries;
    }
}
