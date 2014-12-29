package com.google.maps.android.kml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Created by lavenderc on 12/3/14.
 *
 * Represents a placemark which is either a point, linestring or polygon
 * Stores the properties about the placemark including coordinates
 */
public class PlacemarkProperties {

    private static final int INNER_BOUNDARY = 0;

    private static final int OUTER_BOUNDARY = 1;

    private HashMap<String, String> mPlacemarkProperties;

    private PolygonProperties mPolygonProperty;

    private LineStringProperties mLineStringProperty;

    private PointProperties mPointProperty;

    private ArrayList<Object> mMultiGeometryProperty;

    private static Logger LOGGER = Logger.getLogger("InfoLogging");
    

    /**
     * Takes in a XMLPullParser containing properties for a parser and saves relevant properties
     *
     * @param parser reads input from designated source
     */
    public void placemarkProperties(XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        // Iterate through document until the closing placemark is reached
        while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("Placemark"))) {
            String name = parser.getName();
            // For some reason name.matches only works if you nest it inside the statement below.
            if (eventType == XmlPullParser.START_TAG) {
                if (isStyle(name)) {
                    setStyle(parser, name);
                } else if (isGeometry(name)) {
                    setGeometry(parser, name);
                }
            }
            eventType = parser.next();
        }
    }

    /**
     * Determines whether the tag name is a style property keyword
     * @param name  The text within the start tag
     * @return  true if text equals name, description, visibility or styleUrl
     */
    private boolean isStyle(String name) {
        if (name.equals("name") || name.equals("description") || name.equals("visibility")
                || name.equals("styleUrl")) return true;
        return false;
    }

    /**
     * Sets all the relevant style properties
     * @param parser    XmlPullParser reading in a KML stream
     * @param name      The text within the start tag
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void setStyle(XmlPullParser parser, String name) throws XmlPullParserException, IOException {
        //TODO: Check if name, description or visibility has any edge cases we need to test for
        String styleUrl;
        if (name.equals("name") || name.equals("description") || name.equals("visibility")) {
            setProperties(name, parser.nextText());
        } else if (name.equals("styleUrl")) {
            styleUrl = parser.getAttributeValue(null, "id");
            if (parser.getAttributeCount() != 0) {
                setProperties(name, parser.getAttributeValue(null, "id"));
            } else {
                styleUrl = parser.nextText();
                setProperties(name, styleUrl);
            }
        }
    }

    /**
     * Determines whether the tag name is a geometry property keyword
     * @param name  The text within the start tag
     * @return  true if the text equals linestring, polygon, point or multigeometry
     */
    private boolean isGeometry(String name) {
        if (name.equals("LineString") || name.equals("Polygon") || name.equals("Point")
                || name.equals("MultiGeometry")) return true;
        return false;
    }



    private void setGeometry(XmlPullParser parser, String name) throws XmlPullParserException, IOException {
        if (name.equals("LineString")) {
            setLineString(parser);
        } else if (name.equals("Polygon")) {
            setPolygon(parser);
        } else if (name.equals("Point")) {
            setMarker(parser);
        } else if (name.equals("MultiGeometry")) {
            //setMultiGeometry(parser);
        }
    }



    /**
     * Creates a polygon property class when the starting "Polygon" tag is detected in the parser
     * @param parser XmlPullParser XmlPullParser reading in a KML stream
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void setPolygon (XmlPullParser parser) throws XmlPullParserException, IOException {
        mPolygonProperty = new PolygonProperties();
        int eventType = parser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("Polygon"))) {
            String name = parser.getName();
            if (eventType == XmlPullParser.START_TAG) {
                if (name.equals("outerBoundaryIs")) {
                    mPolygonProperty.parseGeometry(parser);
                } else if (name.equals("innerBoundaryIs")) {
                    mPolygonProperty.parseGeometry(parser);
                }
            }
            eventType = parser.next();
        }
    }

    /**
     * Creates a line string property calss when the starting "Linestring" tag is detected in the parser
     * @param parser XmlPullParser reading in a KML stream
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void setLineString (XmlPullParser parser) throws XmlPullParserException, IOException {
        mLineStringProperty = new LineStringProperties();
        int eventType = parser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("LineString"))) {
            if (eventType == XmlPullParser.START_TAG) {
                mLineStringProperty.parseGeometry(parser);
            }
            eventType = parser.next();
        }
    }
    /**
     * Creates a marker property calss when the starting "Point" tag is detected in the parser
     * @param parser XmlPullParser reading in a KML stream
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void setMarker(XmlPullParser parser) throws XmlPullParserException, IOException {
        mPointProperty = new PointProperties();
        int eventType = parser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("Point"))) {
            if (eventType == XmlPullParser.START_TAG) {
                mPointProperty.parseGeometry(parser);
            }
            eventType = parser.next();
        }
    }

    /**
     * Takes in two strings, key and value, whereby the key is the name of the tag and the value
     * is the text, and puts them into a hashmap.
     *
     * EXAMPLE USE: <name>Hello World</name>
     * key = name, value = Hello World.
     *
     * @param propertyName   The string value which we use to access value
     * @param propertyValue The string value which we want to access
     */
    public void setProperties (String propertyName, String propertyValue) {
        if (mPlacemarkProperties == null) mPlacemarkProperties = new HashMap<String, String>();
        mPlacemarkProperties.put(propertyName, propertyValue);
    }

    /**
     * Retrieves a value from a hash map using a given key
     *
     * @param propertyName The name of the value which we want to retrieve
     * @return The value which was inserted using the key, otherwise null
     */
    public String getProperty (String propertyName) {
        if (propertyName.equals("name") || propertyName.equals("description") || propertyName.equals("visibility") ||
            propertyName.equals("styleUrl")) {
            if (mPlacemarkProperties.containsKey(propertyName)) {
                return mPlacemarkProperties.get(propertyName);
            }
        }
        System.out.println("Requesting property which doesn't exist");
        return null;
    }

    /**
     * Returns a hashmap of properties
     * @return Hashmap of properties, or null if it does not exist
     */
    public HashMap<String, String> getProperties() {
        return mPlacemarkProperties;
    }

    /**
     * @return PolygonProperies object, otherwise null
     */
    public PolygonProperties getPolygon() {
        return mPolygonProperty;
    }

    /**
     * @return LineStringProperties object, otherwise null
     */
    public LineStringProperties getPolyline() {
        return mLineStringProperty;
    }

    public PointProperties getPoint() {
        return mPointProperty;
    }

}
