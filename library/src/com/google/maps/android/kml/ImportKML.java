package com.google.maps.android.kml;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.util.Log;

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

    private int mType;

    private int mBoundary;

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

    private void createStyle(XmlPullParser mParser) throws IOException, XmlPullParserException {
        Style style = new Style();
        String styleUrl = mParser.getAttributeValue(null, "id");
        style.styleProperties(mParser);
        mStyles.put(styleUrl, style);
    }

    private void createPlacemark(XmlPullParser mParser) throws IOException, XmlPullParserException {
        Placemark placemark = new Placemark();
        placemark.placemarkProperties(mParser);
        this.mPlacemarks.add(placemark);
    }


    private void assignStyles() {
        for (Placemark mPlacemark : mPlacemarks) {
            String mStyleName = mPlacemark.getValue("styleUrl");
            if (mStyleName != null && mStyleName.startsWith("#")) {
                mStyleName = mStyleName.substring(HASH_POSITION);
                if (mStyles.containsKey(mStyleName)) {
                    for (Coordinate mCoordinate : mPlacemark.getLine()) {
                      addPointsToStyles(mStyleName, mCoordinate);
                    }
                }
            } else {
                Log.i(mPlacemark.getValue("name"), "default style");
            }
       }

    }

    private void addPointsToStyles (String styleName, Coordinate coordinate) {
        if (coordinate.getType() == POLYGON_TYPE) {
            mStyles.get(styleName).getPolygonOptions().addAll(coordinate.getCoordinateList());
        } else if (coordinate.getType() == LINESTRING_TYPE) {
            mStyles.get(styleName).getPolylineOptions().addAll(coordinate.getCoordinateList());
        } else if (coordinate.getType() == POINT_TYPE) {
          //TODO: Implement Marker
        }
    }

    //TODO: Latitude and longitude can be reversed sometimes, you what mate
    public void addKMLData() {
        for (Style s: mStyles.values()) {
            mObjects.add(this.mMap.addPolyline(s.getPolylineOptions()));
        }
    }

    public void removeKMLData() {
        this.mMap.clear();
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
