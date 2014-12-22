package com.google.maps.android.kml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
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

                //TODO: Check if name, description or visibility has any edge cases we need to test for
                if (name.equals("name") || name.equals("description") || name.equals("visibility")) {
                    setProperties(name, parser.nextText());
                } else if (name.equals("styleUrl")) {
                    //TODO: Style url can either be in the tags or they can be an id
                    if (parser.getAttributeValue(null, "id") != null) {
                        if (isValidStyleUrl(parser.getAttributeValue(null, "id"))) {
                            setProperties(name, parser.getAttributeValue(null, "id"));
                        }
                    } else {
                        String styleUrl = parser.nextText();
                        if (isValidStyleUrl(parser.nextText())) {
                            setProperties(name, styleUrl);
                        }
                    }
                }

                if (name.equals("LineString")) {
                    setLineString(parser);
                } else if (name.equals("Polygon")) {
                    setPolygon(parser);
                } else if (name.equals("Point")) {
                    setMarker(parser);
                }
            }
                eventType = parser.next();
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
                    mPolygonProperty.pointProperties(parser, OUTER_BOUNDARY);
                } else if (name.equals("innerBoundaryIs")) {
                    mPolygonProperty.pointProperties(parser, INNER_BOUNDARY);
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
                mLineStringProperty.pointProperties(parser);
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
        //TODO: Do marker options
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
     * Checks that the styleUrl starts with #, as we do not currently support importing kml
     * files from other sources
     * @param styleUrl  string which represents name of the styleurl
     * @return  true if styleUrl has a "#" at the beginning, false otherwise
     */
    private boolean isValidStyleUrl(String styleUrl) {
        return styleUrl.startsWith("#");
    }

    /**
     * Checks that there is only one geometry object in this placemark class
     * @return true if one or no geometry object, otherwise false
     */
    public boolean isValidGeometryObject() {
        if (mLineStringProperty != null && mPolygonProperty != null) return false;
        if (mLineStringProperty == null && mPolygonProperty != null) return true;
        if (mLineStringProperty != null && mPolygonProperty == null) return true;
        if (mLineStringProperty != null && mPolygonProperty != null) return false;
        return true;
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

}
