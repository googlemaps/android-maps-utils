package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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
public class PolygonProperties implements Geometry {


    private HashMap< ArrayList<LatLng>, Integer> polygonBoundaries;

    @Override
    public void parseGeometry(XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("LinearRing"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().equals("coordinates")) {
                    parser.next();
                    setGeometry(parser.getText());
                }
            }
            eventType = parser.next();
        }
    }

    @Override
    public void setGeometry(String text) {
        ArrayList<LatLng> mPointList = new ArrayList<LatLng>();
        String[] lines = text.trim().split("(\\s+)");
        for (String point : lines) {
            String[] coordinate = point.split(",");
            Double latDouble = Double.parseDouble(coordinate[LONGITUDE]);
            Double lonDouble = Double.parseDouble(coordinate[LATITUDE]);
            mPointList.add(new LatLng(latDouble, lonDouble));
        }
        if (polygonBoundaries == null) {
            polygonBoundaries = new HashMap<ArrayList<LatLng>, Integer>();
            polygonBoundaries.put(mPointList, OUTER_BOUNDARY);
        } else {
            polygonBoundaries.put(mPointList, INNER_BOUNDARY);
        }
    }




    @Override
    public LatLng convertToLatLng(String[] text) {
        return null;
    }

    @Override
    public Object getGeometry() {
        return polygonBoundaries;
    }
}
