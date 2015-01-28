package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Parses the feature of a given KML file into a KmlPlacemark or KmlGroundOverlay object
 */
/* package */ class KmlFeatureParser {

    private final static String GEOMETRY_REGEX = "Point|LineString|Polygon|MultiGeometry";

    private final static int LONGITUDE_INDEX = 0;

    private final static int LATITUDE_INDEX = 1;

    private final static String PROPERTY_REGEX = "name|description|visibility";

    private final static String BOUNDARY_REGEX = "outerBoundaryIs|innerBoundaryIs";

    private final static String EXTENDED_DATA = "ExtendedData";

    private final static String STYLE_URL_TAG = "styleUrl";

    private final static String STYLE_TAG = "Style";

    private final XmlPullParser mParser;

    private KmlPlacemark mPlacemark;

    public KmlFeatureParser(XmlPullParser parser) {
        mParser = parser;
        mPlacemark = null;
    }

    /**
     * Creates a KmlPlacemark object for each basic_placemark detected if they contain a geometry.
     * Also
     * stores styles and properties for the given basic_placemark.
     */
    /* package */ void createPlacemark() throws IOException, XmlPullParserException {
        String styleId = null;
        KmlStyle inlineStyle = null;
        HashMap<String, String> properties = new HashMap<String, String>();
        KmlGeometry geometry = null;
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("Placemark"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals(STYLE_URL_TAG)) {
                    styleId = mParser.nextText();
                } else if (mParser.getName().matches(GEOMETRY_REGEX)) {
                    geometry = createGeometry(mParser.getName());
                } else if (mParser.getName().matches(PROPERTY_REGEX)) {
                    properties.put(mParser.getName(), mParser.nextText());
                } else if (mParser.getName().equals(EXTENDED_DATA)) {
                    properties.putAll(setExtendedDataProperties());
                } else if (mParser.getName().equals(STYLE_TAG)) {
                    KmlStyleParser styleParser = new KmlStyleParser(mParser);
                    styleParser.createStyle();
                    inlineStyle = styleParser.getStyle();
                }
            }
            eventType = mParser.next();
        }
        // If there is no geometry associated with the Placemark then we do not add it
        if (geometry != null) {
            mPlacemark = new KmlPlacemark(geometry, styleId, inlineStyle, properties);
        }
    }

    /* package */ KmlGroundOverlay createGroundOverlay()
            throws IOException, XmlPullParserException {
        // TODO: add support for color
        KmlGroundOverlay groundOverlay = new KmlGroundOverlay();
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("GroundOverlay"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals("Icon")) {
                    while (!(eventType == XmlPullParser.END_TAG && mParser.getName()
                            .equals("Icon"))) {
                        if (eventType == XmlPullParser.START_TAG && mParser.getName()
                                .equals("href")) {
                            groundOverlay.setImageUrl(mParser.nextText());
                        }
                        eventType = mParser.next();
                    }
                } else if (mParser.getName().equals("LatLonBox")) {
                    groundOverlay.setLatLngBox(createLatLonBox(groundOverlay));
                } else if (mParser.getName().equals("drawOrder")) {
                    groundOverlay.setDrawOrder(Float.parseFloat(mParser.nextText()));
                } else if (mParser.getName().equals("visibility")) {
                    groundOverlay.setVisibility(Integer.parseInt(mParser.nextText()));
                } else if (mParser.getName().equals("ExtendedData")) {
                    groundOverlay.setProperties(setExtendedDataProperties());
                }
            }
            eventType = mParser.next();
        }
        return groundOverlay;
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
     * Adds untyped name value pairs parsed from the ExtendedData
     */
    private HashMap<String, String> setExtendedDataProperties()
            throws XmlPullParserException, IOException {
        HashMap<String, String> properties = new HashMap<String, String>();
        String propertyKey = null;
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals(EXTENDED_DATA))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals("Data")) {
                    propertyKey = mParser.getAttributeValue(null, "name");
                } else if (mParser.getName().equals("value") && propertyKey != null) {
                    properties.put(propertyKey, mParser.nextText());
                    propertyKey = null;
                }
            }
            eventType = mParser.next();
        }
        return properties;
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
        Double lat = Double.parseDouble(coordinate[LATITUDE_INDEX]);
        Double lon = Double.parseDouble(coordinate[LONGITUDE_INDEX]);
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
        ArrayList<LatLng> outerBoundaryCoordinates = new ArrayList<LatLng>();
        ArrayList<ArrayList<LatLng>> innerBoundaryCoordinates = new ArrayList<ArrayList<LatLng>>();
        int eventType = mParser.getEventType();

        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("Polygon"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().matches(BOUNDARY_REGEX)) {
                    isOuterBoundary = mParser.getName().equals("outerBoundaryIs");
                } else if (mParser.getName().equals("coordinates")) {
                    if (isOuterBoundary) {
                        outerBoundaryCoordinates = convertToLatLngArray(mParser.nextText());
                    } else {
                        innerBoundaryCoordinates.add(convertToLatLngArray(mParser.nextText()));
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
                    .matches(GEOMETRY_REGEX)) {
                geometries.add(createGeometry(mParser.getName()));
            }
            eventType = mParser.next();
        }
        return new KmlMultiGeometry(geometries);
    }

    private LatLngBounds createLatLonBox(KmlGroundOverlay groundOverlay)
            throws XmlPullParserException, IOException {
        Double north = 0.0;
        Double south = 0.0;
        Double east = 0.0;
        Double west = 0.0;

        int eventType = mParser.next();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("LatLonBox"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals("north")) {
                    north = Double.parseDouble(mParser.nextText());
                } else if (mParser.getName().equals("south")) {
                    south = Double.parseDouble(mParser.nextText());
                } else if (mParser.getName().equals("east")) {
                    east = Double.parseDouble(mParser.nextText());
                } else if (mParser.getName().equals("west")) {
                    west = Double.parseDouble(mParser.nextText());
                } else if (mParser.getName().equals("rotation")) {
                    groundOverlay.setRotation(Float.parseFloat(mParser.nextText()));
                }
            }
            eventType = mParser.next();
        }
        return createLatLngBounds(north, south, east, west);
    }

    /**
     * Given a set of four latLng coordinates, creates a LatLng Bound
     *
     * @param north North coordinate of the bounding box
     * @param south South coordinate of the bounding box
     * @param east  East coordinate of the bounding box
     * @param west  West coordinate of the bounding box
     */
    private LatLngBounds createLatLngBounds(Double north, Double south, Double east, Double west) {
        LatLng southWest = new LatLng(south, west);
        LatLng northEast = new LatLng(north, east);
        return new LatLngBounds(southWest, northEast);
    }

    /* package */ KmlPlacemark getPlacemark() {
        return mPlacemark;
    }
}
