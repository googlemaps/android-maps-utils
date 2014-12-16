package com.google.maps.android.kml;

import android.content.Context;
import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Document class allows for users to input their KML data and output it onto the map
 */
public class ImportKML {

    private final XmlPullParser mParser;

    private final HashMap<String, Style> mStyles;

    private final ArrayList<Object> mObjects;

    private final ArrayList<Placemark> mPlacemarks;

    private final GoogleMap mMap;

    private static final int UNINITIALIZED = -1;

    private static final int POLYGON_TYPE = 0;

    private static final int LINESTRING_TYPE = 1;

    private static final int POINT_TYPE = 2;

    private static final int INNER_BOUNDARY = 0;

    private static final int OUTER_BOUNDARY = 1;

    private static final int LATITUDE = 0;

    private static final int LONGITUDE = 1;

    private static final int HASH_POSITION = 1;

    private final static int HEXADECIMAL_COLOR_RADIX = 16;

    private int mType;

    private int mBoundary;

    private ArrayList<Object> mOptions;

    private boolean isVisible;

    private String name;

    private ArrayList<LatLng> mCoordinateList;

    private InputStream stream;


    /**
     * Constructs a new Document object
     *
     * @param map    Map object
     * @param parser XmlPullParser loaded with the KML file
     */
    public ImportKML(GoogleMap map, XmlPullParser parser) {
        this.mParser = parser;
        this.mMap = map;
        this.mStyles = new HashMap<String, Style>();
        this.mPlacemarks = new ArrayList<Placemark>();
        this.mObjects = new ArrayList<Object>();
        this.isVisible = true;
        this.mOptions = new ArrayList<Object>();
    }

    /**
     * @param map                Map object
     * @param resourceId         Raw resource KML file
     * @param applicationContext Application context object
     */
    public ImportKML(GoogleMap map, int resourceId, Context applicationContext)
            throws XmlPullParserException {
        this.mMap = map;
        this.mStyles = new HashMap<String, Style>();
        this.mPlacemarks = new ArrayList<Placemark>();
        this.stream = applicationContext.getResources().openRawResource(resourceId);
        this.mParser = createXmlParser(this.stream);
        this.mObjects = new ArrayList<Object>();
        this.mOptions = new ArrayList<Object>();
        this.isVisible = true;
    }

    /**
     * Creates a new XmlPullParser object
     *
     * @param stream Character input stream representing the KML file
     * @return XmlPullParser object to allow for the KML document to be parsed
     */
    private XmlPullParser createXmlParser(InputStream stream) {
        XmlPullParserFactory factory = null;
        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(stream, null);
            return parser;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Generates style values when a mParser to a text is given.
     * New mStyles with different features are created when a new ID is given
     */
    public void importKML() throws XmlPullParserException, IOException {
        this.mParser.require(XmlPullParser.START_DOCUMENT, null, null);
        this.mParser.next();
        this.mParser.require(XmlPullParser.START_TAG, null, "kml");
        int eventType = this.mParser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {

            if (eventType == XmlPullParser.START_TAG) {
                boolean isStyle = this.mParser.getName().equals("Style");
                boolean isPlacemark = this.mParser.getName().equals("Placemark");
                if (isStyle) createStyle(this.mParser);
                else if (isPlacemark) createPlacemark(this.mParser);
            }
            eventType = this.mParser.next();
        }
        assignStyles();
        this.mParser.require(XmlPullParser.END_DOCUMENT, null, null);
    }

    /**
     * Creates a new Style and puts it into a HashMap. Key value is the style id specified in the tag,
     * Value is a newly created Style class
     * @param mParser XmlPullParser object for KML document parsing
     * @throws IOException
     * @throws XmlPullParserException
     */

    private void createStyle(XmlPullParser mParser) throws IOException, XmlPullParserException {
        Style style = new Style();
        String styleUrl = "#" + mParser.getAttributeValue(null, "id");
        style.styleProperties(mParser);
        mStyles.put(styleUrl, style);
    }

    /**
     * Creates a new Placemark and puts it into a HashMap. Key value is the style id specified in the tag,
     * Value is a newly created Placemark class
     * @param mParser XmlPullParser object for KML document parsing
     * @throws IOException
     * @throws XmlPullParserException
     */

    private void createPlacemark(XmlPullParser mParser) throws IOException, XmlPullParserException {
        Placemark placemark = new Placemark();
        placemark.placemarkProperties(mParser);
        this.mPlacemarks.add(placemark);
    }

    /**
     * Retreives values from Placemarks and Styles, if they exist, and creates a geometry option o
     * object with appropriate properties
     */
    private void assignStyles() {
        for (Placemark placemark: mPlacemarks) {
            for (Coordinate coordinates: placemark.getPoints()) {
                if (coordinates.getType() == LINESTRING_TYPE) {
                    mOptions.add(assignLineOptions(placemark, coordinates));
                } else if (coordinates.getType() == POLYGON_TYPE) {
                    if (coordinates.getBoundary() == INNER_BOUNDARY) {
                        mOptions.add(assignPolygonOptions(placemark, coordinates, INNER_BOUNDARY));
                    } else {
                        mOptions.add(assignPolygonOptions(placemark, coordinates, OUTER_BOUNDARY));
                    }

                } else if (coordinates.getType() == POINT_TYPE) {
                    ArrayList<MarkerOptions> markerOptions = assignMarkerOptions(placemark, coordinates);
                    for(MarkerOptions markers: markerOptions){
                        mOptions.add(markers);
                    }
                }
            }
        }
    }


    /**
     * Adds geometry options options onto the map. The geometry object itself is stored in another
     * data structure for retrieval later
     */
    public void addKMLData() {
        for (Object objects: mOptions) {
            if (objects instanceof PolylineOptions) {
                mObjects.add(mMap.addPolyline((PolylineOptions) objects));
            } else if (objects instanceof PolygonOptions) {
                mObjects.add(mMap.addPolygon((PolygonOptions) objects));
            } else if (objects instanceof MarkerOptions) {
                mObjects.add(mMap.addMarker((MarkerOptions) objects));
            }
        }
    }

    /**
     *
     * @param placemark
     * @param coordinates
     * @return
     */
    public PolylineOptions assignLineOptions(Placemark placemark, Coordinate coordinates) {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(coordinates.getCoordinateList());
        boolean hasStyleURL =  placemark.getProperties().containsKey("styleUrl");
        if (hasStyleURL) {
            boolean isStyleSpecified = mStyles.containsKey(placemark.getValue("styleUrl"));
            if (isStyleSpecified) {
                HashMap<String, String> polyLineProperties = mStyles.get(placemark.getValue("styleUrl")).getPolylineOptions();
                if (polyLineProperties.containsKey("color")) {

                    polylineOptions.color(Color.parseColor(polyLineProperties.get("color")));
                    System.out.println(polylineOptions.getColor());
                }
                if (polyLineProperties.containsKey("width")) {
                    Float width = Float.parseFloat(polyLineProperties.get("width"));
                    polylineOptions.width(width);
                }
            }
        }
        return polylineOptions;
    }

    public PolygonOptions assignPolygonOptions(Placemark placemark, Coordinate coordinates, int linearPosition) {
        PolygonOptions polygonOptions = new PolygonOptions();
        if (linearPosition == OUTER_BOUNDARY) {
            polygonOptions.addAll(coordinates.getCoordinateList());
        } else {
            polygonOptions.addHole(coordinates.getCoordinateList());
        }

        boolean hasStyleURL =  placemark.getProperties().containsKey("styleUrl");
        if (hasStyleURL) {
            boolean isStyleSpecified = mStyles.containsKey(placemark.getValue("styleUrl"));
            if (isStyleSpecified) {
                HashMap<String, String> polygonProperties = mStyles.get(placemark.getValue("styleUrl")).getPolygonOptions();
                if (polygonProperties.containsKey("strokeColor")) {
                    polygonOptions.strokeColor(Color.parseColor(polygonProperties.get("strokeColor")));
                } if (polygonProperties.containsKey("strokeWidth")) {
                    Float width = Float.parseFloat(polygonProperties.get("strokeWidth"));
                    polygonOptions.strokeWidth(width);
                } if (polygonProperties.containsKey("fillColor")) {
                    Float width = Float.parseFloat(polygonProperties.get("fillColor"));
                    polygonOptions.strokeWidth(width);
                }
            }
        }

        return polygonOptions;
    }

    public ArrayList<MarkerOptions> assignMarkerOptions (Placemark placemark, Coordinate coordinates) {
        ArrayList<MarkerOptions> m = new ArrayList<MarkerOptions>();
        for (LatLng point: coordinates.getCoordinateList()) {
            MarkerOptions marker = new MarkerOptions();
            marker.position(point);
            m.add(marker);
        }
        return m;
    }



    public void removeKMLData() {
        this.isVisible = !this.isVisible;
        for (Object mObject: mObjects) {
            if (mObject instanceof Polygon) {
                ((Polygon) mObject).remove();
            } else if (mObject instanceof Polyline){
                ((Polyline) mObject).remove();
            } else if (mObject instanceof Marker) {
                ((Marker) mObject).remove();
            }
        }
    }

    public void toggleKMLData() {
        this.isVisible = !this.isVisible;
        for (Object mObject: mObjects) {
            if (mObject instanceof Polygon) {
                ((Polygon) mObject).setVisible(this.isVisible);
            } else if (mObject instanceof Polyline){
                ((Polyline) mObject).setVisible(this.isVisible);
            } else if (mObject instanceof Marker) {
                ((Marker) mObject).setVisible(this.isVisible);
            }
        }
    }
}
