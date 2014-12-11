package com.google.maps.android.kml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by lavenderc on 12/3/14.
 *
 * Represents a placemark which is either a point, linestring or polygon
 * Stores the properties about the placemark including coordinates
 */
public class Placemark {

    private static final int POLYGON_TYPE = 0;

    private static final int LINESTRING_TYPE = 1;

    private static final int POINT_TYPE = 2;

    private static final int INNER_BOUNDARY = 0;

    private static final int OUTER_BOUNDARY = 1;


    private final ArrayList<Coordinate> mLine = new ArrayList<Coordinate>();

    private final HashMap<String, String> mValues = new HashMap<String, String>();

    /**
     * Takes in a XMLPullParser containing properties for a parser and saves relevant properties
     *
     * @param p reads input from designated source
     */
    public void placemarkProperties(XmlPullParser p) throws XmlPullParserException, IOException {
        int eventType = p.getEventType();
        // Iterate through document until the closing placemark is reached
        while (!(eventType == XmlPullParser.END_TAG && p.getName().equals("Placemark"))) {
            String name = p.getName();
            // For some reason name.matches only works if you nest it inside the statement below.
            if (eventType == XmlPullParser.START_TAG) {
                if (name.matches("name|description|visibility|styleUrl")) {
                    setValue(name, p.nextText());
                }
                // outerBoundaryIs and innerBoundaryIs refer to the polygon boundaries
                else if (name.matches("LineString|Point|outerBoundaryIs|innerBoundaryIs")) {
                    Coordinate c = new Coordinate();
                    if (name.equals("LineString")) {
                        c.setType(LINESTRING_TYPE);
                    } else if (name.equals("Point")) {
                        c.setType(POINT_TYPE);
                    } else if (name.equals("outerBoundaryIs")) {
                        c.setType(POLYGON_TYPE);
                        c.setBoundary(OUTER_BOUNDARY);
                    } else if (name.equals("innerBoundaryIs")) {
                        c.setType(POLYGON_TYPE);
                        c.setBoundary(INNER_BOUNDARY);
                    }
                    c.coordinateProperties(p);
                    mLine.add(c);
                }
            }
            eventType = p.next();
        }
    }

    /**
     * Takes in two strings, key and value, whereby the key is the name of the tag and the value
     * is the text, and puts them into a hashmap.
     *
     * EXAMPLE USE: <name>Hello World</name>
     * key = name, value = Hello World.
     *
     * @param key   The string value which we use to access value
     * @param value The string value which we want to access
     */
    public void setValue(String key, String value) {
        mValues.put(key, value);
    }

    /**
     * Retrieves a value from a hash map using a given key
     *
     * @param key The name of the value which we want to retrieve
     * @return The value which was inserted using the key, otherwise null
     */
    public String getValue(String key) {
        return mValues.get(key);
    }


    /**
     * Retrieves the ArrayList of Coordinate classes (list of multiple latlng lists) that this
     * placemark has
     *
     * @return An ArrayList of Coordinate classes
     */

    public ArrayList<Coordinate> getLine() {
        return mLine;
    }
}
