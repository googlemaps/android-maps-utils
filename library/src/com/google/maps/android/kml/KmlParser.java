package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by juliawong on 1/7/15.
 */
public class KmlParser {

    private static final String PROPERTY_TAG_REGEX = "name|description|visibility";

    private static final String STYLE_TAG = "styleUrl";

    private static final String GEOMETRY_TAG_REGEX = "Point|LineString|Polygon|MultiGeometry";

    private static final int LONGITUDE = 0;

    private static final int LATITUDE = 1;

    private final HashMap<String, KmlStyle> mStyles;

    private final ArrayList<KmlPlacemark> mPlacemarks;

    private final XmlPullParser mParser;

    /**
     * Creates a new KmlParser object
     *
     * @param parser parser containing the KML file to parse
     */
    public KmlParser(XmlPullParser parser) {
        mStyles = new HashMap<String, KmlStyle>();
        // Add a default style
        mStyles.put(null, new KmlStyle());
        mPlacemarks = new ArrayList<KmlPlacemark>();
        mParser = parser;
    }

    /**
     * Parses the KML file and stores the created KmlStyle and KmlPlacemark
     */
    public void parseKml() throws XmlPullParserException, IOException {
        int eventType = mParser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals("Style")) {
                    createStyle();
                }
                if (mParser.getName().equals("Placemark")) {
                    createPlacemark();
                }
            }
            eventType = mParser.next();
        }
    }

    /**
     * Parses the IconStyle, LineStyle and PolyStyle tags into a KmlStyle object
     */
    private void createStyle() throws IOException, XmlPullParserException {
        // Indicates if any valid style tags have been found
        Boolean isValidStyle = false;
        KmlStyle styleProperties = new KmlStyle();
        // Append # to style id
        String styleId = "#" + mParser.getAttributeValue(null, "id");
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("Style"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals("IconStyle")) {
                    createIconStyle(styleProperties);
                    isValidStyle = true;
                } else if (mParser.getName().equals("LineStyle")) {
                    createLineStyle(styleProperties);
                    isValidStyle = true;
                } else if (mParser.getName().equals("PolyStyle")) {
                    createPolyStyle(styleProperties);
                    isValidStyle = true;
                }
            }
            eventType = mParser.next();
        }

        // Check if supported styles are added, unsupported styles are not saved
        if (isValidStyle) {
            mStyles.put(styleId, styleProperties);
        }
    }

    /**
     * Sets relevant styling properties to the KmlStyle object that are found in the IconStyle tag
     * Supported tags include scale, heading, Icon, href, hotSpot
     *
     * @param style Style object to add properties to
     */
    private void createIconStyle(KmlStyle style) throws XmlPullParserException, IOException {
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("IconStyle"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals("scale")) {
                    // TODO implement support for scale
                } else if (mParser.getName().equals("heading")) {
                    style.setHeading(Float.parseFloat(mParser.nextText()));
                } else if (mParser.getName().equals("Icon")) {
                    // Iterate until end icon tag found
                    while (!(eventType == XmlPullParser.END_TAG && mParser.getName()
                            .equals("Icon"))) {
                        // find inner child href tag
                        if (eventType == XmlPullParser.START_TAG && mParser.getName()
                                .equals("href")) {
                            style.setIconUrl(mParser.nextText());
                        }
                        eventType = mParser.next();
                    }
                } else if (mParser.getName().equals("hotSpot")) {
                    style.setHotSpot(Float.parseFloat(mParser.getAttributeValue(null, "x")),
                            Float.parseFloat(mParser.getAttributeValue(null, "y")),
                            mParser.getAttributeValue(null, "xunits"),
                            mParser.getAttributeValue(null, "yunits"));
                }
            }
            eventType = mParser.next();
        }
    }

    /**
     * Sets relevant styling properties to the KmlStyle object that are found in the LineStyle tag
     * Supported tags include color, width
     *
     * @param style Style object to add properties to
     */
    private void createLineStyle(KmlStyle style) throws XmlPullParserException, IOException {
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("LineStyle"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals("color")) {
                    style.setOutlineColor(mParser.nextText());
                } else if (mParser.getName().equals("width")) {
                    style.setWidth(Float.valueOf(mParser.nextText()));
                }
            }
            eventType = mParser.next();
        }
    }

    /**
     * Sets relevant styling properties to the KmlStyle object that are found in the PolyStyle tag
     * Supported tags include color, outline, fill
     *
     * @param style Style object to add properties to
     */
    private void createPolyStyle(KmlStyle style) throws XmlPullParserException, IOException {
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("PolyStyle"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals("color")) {
                    style.setFillColor(mParser.nextText());
                } else if (mParser.getName().equals("outline")) {
                    style.setOutline(Boolean.parseBoolean(mParser.nextText()));
                } else if (mParser.getName().equals("fill")) {
                    style.setFill(Boolean.parseBoolean(mParser.nextText()));
                }
            }
            eventType = mParser.next();
        }
    }


    /**
     * Creates a KmlPlacemark object for each placemark detected if they contain a geometry. Also
     * stores styles and properties for the given placemark.
     */
    private void createPlacemark() throws IOException, XmlPullParserException {
        String style = null;
        HashMap<String, String> properties = new HashMap<String, String>();
        KmlGeometry geometry = null;

        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("Placemark"))) {
            String tagName = mParser.getName();
            if (eventType == XmlPullParser.START_TAG) {
                if (tagName.equals(STYLE_TAG)) {
                    style = mParser.nextText();
                } else if (tagName.matches(GEOMETRY_TAG_REGEX)) {
                    geometry = createGeometry(tagName);
                } else if (tagName.matches(PROPERTY_TAG_REGEX)) {
                    properties.put(tagName, mParser.nextText());
                }
            }
            eventType = mParser.next();
        }
        // If there is no geometry associated with the Placemark then we do not add it
        if (geometry != null) {
            mPlacemarks.add(new KmlPlacemark(geometry, style, properties));
        }
    }

    /**
     * Creates a new KmlGeometry object of type Point, LineString, Polygon or MultiGeometry
     *
     * @param geometryType type of geometry object to create
     */
    private KmlGeometry createGeometry(String geometryType)
            throws IOException, XmlPullParserException {
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals(geometryType))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals("Point")) {
                    return createPoint();
                } else if (mParser.getName().equals("LineString")) {
                    return createLineString();
                } else if (mParser.getName().equals("Polygon")) {
                    return createPolygon();
                } else if (mParser.getName().equals("MultiGeometry")) {
                    return createMultiGeometry();
                }
            }
            eventType = mParser.next();
        }
        return null;
    }

    /**
     * Convert a string coordinate from a string into a LatLng object
     *
     * @param coordinateString coordinate string to convert from
     * @return LatLng object created from given coordinate string
     */
    private LatLng convertToLatLng(String coordinateString) {
        // Lat and Lng are separated by a ,
        String[] coordinate = coordinateString.split(",");
        Double lat = Double.parseDouble(coordinate[LATITUDE]);
        Double lon = Double.parseDouble(coordinate[LONGITUDE]);
        return new LatLng(lat, lon);
    }

    /**
     * Convert a string of coordinates into an array of LatLngs
     *
     * @param coordinatesString coordinates string to convert from
     * @return array of LatLng objects created from the given coordinate string array
     */
    private ArrayList<LatLng> convertToLatLngArray(String coordinatesString) {
        ArrayList<LatLng> coordinatesArray = new ArrayList<LatLng>();
        // Need to trim to avoid whitespace around the coordinates such as tabs
        String[] coordinates = coordinatesString.trim().split("(\\s+)");
        for (String coordinate : coordinates) {
            coordinatesArray.add(convertToLatLng(coordinate));
        }
        return coordinatesArray;
    }

    /**
     * Creates a new KmlPoint object
     *
     * @return KmlPoint object
     */
    private KmlPoint createPoint() throws XmlPullParserException, IOException {
        LatLng coordinate = null;
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("Point"))) {
            if (eventType == XmlPullParser.START_TAG && mParser.getName().equals("coordinates")) {
                coordinate = convertToLatLng(mParser.nextText());
            }
            eventType = mParser.next();
        }
        return new KmlPoint(coordinate);
    }

    /**
     * Creates a new KmlLineString object
     *
     * @return KmlLineString object
     */
    private KmlLineString createLineString() throws XmlPullParserException, IOException {
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("LineString"))) {
            if (eventType == XmlPullParser.START_TAG && mParser.getName().equals("coordinates")) {
                coordinates = convertToLatLngArray(mParser.nextText());
            }
            eventType = mParser.next();
        }
        return new KmlLineString(coordinates);
    }

    /**
     * Creates a new KmlPolygon object. Parses only one outer boundary and no or many inner
     * boundaries containing the coordinates.
     *
     * @return KmlPolygon object
     */
    private KmlPolygon createPolygon() throws XmlPullParserException, IOException {
        // Indicates if an outer boundary needs to be defined
        Boolean isOuterBoundary = false;
        // Indicates if an inner boundary needs to be defined
        Boolean isInnerBoundary = false;
        ArrayList<LatLng> outerBoundaryCoordinates = new ArrayList<LatLng>();
        ArrayList<ArrayList<LatLng>> innerBoundaryCoordinates = new ArrayList<ArrayList<LatLng>>();

        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("Polygon"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals("outerBoundaryIs")) {
                    isOuterBoundary = true;
                } else if (mParser.getName().equals("innerBoundaryIs")) {
                    isInnerBoundary = true;
                } else if (mParser.getName().equals("coordinates")) {
                    if (isOuterBoundary) {
                        outerBoundaryCoordinates = convertToLatLngArray(mParser.nextText());
                        isOuterBoundary = false;
                    } else if (isInnerBoundary) {
                        innerBoundaryCoordinates.add(convertToLatLngArray(mParser.nextText()));
                        isInnerBoundary = false;
                    }
                }
            }
            eventType = mParser.next();
        }
        return new KmlPolygon(outerBoundaryCoordinates, innerBoundaryCoordinates);
    }

    /**
     * Creates a new KmlMultiGeometry object
     *
     * @return KmlMultiGeometry object
     */
    private KmlMultiGeometry createMultiGeometry() throws XmlPullParserException, IOException {
        ArrayList<KmlGeometry> geometries = new ArrayList<KmlGeometry>();
        // Get next otherwise have an infinite loop
        int eventType = mParser.next();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("MultiGeometry"))) {
            if (eventType == XmlPullParser.START_TAG && mParser.getName()
                    .matches(GEOMETRY_TAG_REGEX)) {
                geometries.add(createGeometry(mParser.getName()));
            }
            eventType = mParser.next();
        }
        return new KmlMultiGeometry(geometries);
    }

    /**
     * Gets the hashmap of KmlStyle objects
     *
     * @return hashmap of KmlStyle objects
     */
    public HashMap<String, KmlStyle> getStyles() {
        return mStyles;
    }

    /**
     * Gets the array of KmlPlacemark objects
     *
     * @return array of KmlPlacemark objects
     */
    public ArrayList<KmlPlacemark> getPlacemarks() {
        return mPlacemarks;
    }
}
