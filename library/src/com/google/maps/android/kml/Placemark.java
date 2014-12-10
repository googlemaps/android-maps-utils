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

    private final int POLYGON_TYPE = 0;

    private final int LINESTRING_TYPE = 1;

    private final int POINT_TYPE = 2;


    private ArrayList<Coordinate> line = new ArrayList<Coordinate>();

    private HashMap<String, String> values = new HashMap<String, String>();

    /**
     * Takes in a XMLPullParser containing properties for a parser and saves relevant properties
     *
     * @param p reads input from designated source
     * @throws XmlPullParserException
     * @throws IOException
     */
    public void placemarkProperties(XmlPullParser p) throws XmlPullParserException, IOException {
        int eventType = p.getEventType();
        // Iterate through document until the closing placemark is reached
        while (!(eventType == XmlPullParser.END_TAG && p.getName().equals("Placemark"))) {
            String name = p.getName();
            //For some reason name.matches only works if you nest it inside the statement below.
            if (eventType == XmlPullParser.START_TAG) {
                if (name.matches("name|styleURL|description|phoneNumber|address|visibility")) {
                    setValues(name, p.nextText());
                } else if (name.matches("LineString|Point|Polygon")) {
                    Coordinate c = new Coordinate();
                    if (name.equals("LineString")) {
                        c.setType(LINESTRING_TYPE);
                    } else if (name.equals("Point")) {
                        c.setType(POINT_TYPE);
                    } else if (name.equals("Polygon")) {
                        c.setType(POLYGON_TYPE);
                    }
                    c.coordinateProperties(p);
                    line.add(c);
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
    public void setValues(String key, String value) {
        values.put(key, value);
    }

    /**
     * Retrieves values from a hash map using a key
     *
     * @param key The name of the value which we want to retrieve
     * @return The value which was inserted using the key, otherwise null
     */
    public String getValues(String key) {
        return values.get(key);
    }


    /**
     * Retrieves the ArrayList of Coordinate classes (list of multiple latlng lists) that this
     * placemark has
     *
     * @return An ArrayList of Coordinate classes
     */

    public ArrayList<Coordinate> getLine() {
        return line;
    }
}
