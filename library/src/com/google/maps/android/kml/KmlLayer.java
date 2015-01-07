package com.google.maps.android.kml;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Document class allows for users to input their KML data and output it onto the map
 */
public class KmlLayer {

    private static final int INNER_BOUNDARY = 0;

    private static final int OUTER_BOUNDARY = 1;

    private static final int GEOMETRY_TYPE = 0;

    private static final int MULTIGEOMETRY_TYPE = 1;

    /**
     * Hashmap of Style classes. The key is a string value which represents the id of the Style in
     * the KML document.
     */
    private final HashMap<String, Style> mStyles;

    // TODO: change to hashmap where key is Geometry and value is the map object

    /**
     * A collection of GoogleMap objects (Polygon, Polyline, Marker) which have been put on the map
     */
    private final ArrayList<Object> mMapObjects;

    /**
     * A collection of Placemark objects
     */
    private final ArrayList<Placemark> mPlacemark;

    /**
     * A Google Map
     */
    private final GoogleMap mMap;

    /**
     * XML Pull Parser, which reads in a KML Document
     */
    private XmlPullParser mParser;

    /*TODO:
        Figure out a better place to put convertToLatLng in the Placemark class - create a parser class
        Implement IconStyle (IconStyle is the style class for Point classes) - in progress
        Multigeometry currently doesn't implement style, only the coordinates
        Add Geometry constructors to take in coords
        Implement StyleMap.
        Implement BalloonStyle (Equivalent in GoogleMaps is IconWindow)
        Implement LabelStyle (Equivalent is IconGenerator Utility Library)
        Test Multigeometry - can be recursive
    */

    /**
     * Creates a new KmlLayer object
     *
     * @param map        GoogleMap object
     * @param resourceId Raw resource KML file
     * @param context    Context object
     * @throws XmlPullParserException if file cannot be parsed
     */
    public KmlLayer(GoogleMap map, int resourceId, Context context)
            throws XmlPullParserException {
        this.mMap = map;
        this.mStyles = new HashMap<String, Style>();
        // Add a default style
        mStyles.put(null, new Style());
        this.mPlacemark = new ArrayList<Placemark>();
        this.mMapObjects = new ArrayList<Object>();
        InputStream stream = context.getResources().openRawResource(resourceId);
        this.mParser = createXmlParser(stream);
    }

    /**
     * Creates a new KmlLayer object
     *
     * @param map    GoogleMap object
     * @param stream InputStream containing KML file
     * @throws XmlPullParserException if file cannot be parsed
     */
    public KmlLayer(GoogleMap map, InputStream stream)
            throws XmlPullParserException {
        this.mMap = map;
        this.mStyles = new HashMap<String, Style>();
        // Add a default style
        mStyles.put(null, new Style());
        this.mPlacemark = new ArrayList<Placemark>();
        this.mMapObjects = new ArrayList<Object>();
        this.mParser = createXmlParser(stream);
    }

    /**
     * Gets whether the given tag is a KML property we are interested in
     *
     * @param startTag tag name to check
     * @return true if KML property of interest, false otherwise
     */
    private static boolean isProperty(String startTag) {
        return startTag.matches("name|description|visibility");
    }

    /**
     * Gets whether the given string is a style tag
     *
     * @param name string to check
     * @return true if style tag, false otherwise
     */
    private static boolean isStyle(String name) {
        return name.equals("styleUrl");
    }

    /**
     * Gets whether the given string is a type of KML geometry
     *
     * @param name string to check
     * @return true if geometry, false otherwise
     */
    private static boolean isGeometry(String name) {
        return name.matches("LineString|Polygon|Point");
    }

    /**
     * Gets whether the given string is of type MultiGeometry
     *
     * @param name string to check
     * @return true if MultiGeometry, false otherwise
     */
    private static boolean isMultiGeometry(String name) {
        return (name.equals("MultiGeometry"));
    }

    /**
     * Adds the KML data to the map
     *
     * @throws XmlPullParserException if KML file cannot be parsed
     * @throws IOException            if KML file cannot be opened
     */
    public void setKmlData() throws XmlPullParserException, IOException {
        importKML();
        addToMap();
    }

    /**
     * Creates a new XmlPullParser to allow for the KML file to be parsed
     *
     * @param stream InputStream containing KML file
     * @return XmlPullParser containing the KML file
     * @throws XmlPullParserException if KML file cannot be parsed
     */
    public XmlPullParser createXmlParser(InputStream stream) throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(stream, null);
        return parser;
    }

    /**
     * Recieves input from a parser and creates a Style class if a Substyle start tag is detected,
     * or a Placemark class if a Feature class is detected.
     */
    private void importKML() throws XmlPullParserException, IOException {
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
     * Recieves input from a parser and creates a LineStyle class to add to a Style class if
     * a LineStyle start tag is detected, or creates a PolyStyle class to add to a Style class id
     * a PolyStyle start tag is detected.
     */

    private void createStyle() throws IOException, XmlPullParserException {
        Style styleProperties = new Style();
        // Append # to style id
        String styleId = "#" + mParser.getAttributeValue(null, "id");
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("Style"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals("IconStyle")) {
                    createIconStyle(styleProperties);
                } else if (mParser.getName().equals("PolyStyle")) {
                    createPolyStyle(styleProperties);
                } else if (mParser.getName().equals("LineStyle")) {
                    createLineStyle(styleProperties);
                }
            }
            eventType = mParser.next();
        }
        mStyles.put(styleId, styleProperties);
    }

    /**
     * Recieves input from a parser and adds a property if its corresponding start tag is detected
     *
     * @param style Style object to add properties to
     */
    private void createIconStyle(Style style) throws XmlPullParserException, IOException {
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("IconStyle"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals("scale")) {
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
     * Recieves input from a parser and adds a property if its corresponding start tag is detected
     *
     * @param style Style object to add properties to
     */
    private void createLineStyle(Style style) throws XmlPullParserException, IOException {
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
     * Receives input from a parser and adds a property if its corresponding start tag is detected
     *
     * @param style Style object to add properties to
     */
    private void createPolyStyle(Style style) throws XmlPullParserException, IOException {
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
     * Receives input from a parser. If a style start tag is detected with an id, it sets the style
     * for the placemark. If a Geometry, or MultiGeometry start tag is detected, it creates the
     * corresponding classes. If a property start tag is detected, then it adds those properties to
     */
    private void createPlacemark() throws IOException, XmlPullParserException {
        Boolean hasGeometry = false;
        Placemark placemark = new Placemark();
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("Placemark"))) {
            String tagName = mParser.getName();
            if (eventType == XmlPullParser.START_TAG) {
                if (isStyle(tagName)) {
                    placemark.setStyle(mParser.nextText());
                } else if (isGeometry(tagName)) {
                    createGeometry(GEOMETRY_TYPE, tagName, placemark);
                    hasGeometry = true;
                } else if (isProperty(tagName)) {
                    placemark.setProperties(tagName, mParser.nextText());
                } else if (isMultiGeometry(tagName)) {
                    createMultiGeometry(placemark);
                    hasGeometry = true;
                }
            }
            eventType = mParser.next();
        }
        // If there is no geometry associated with the Placemark then we do not add it
        if (hasGeometry) {
            mPlacemark.add(placemark);
        }
    }

    /**
     * Recieves input from a parser and sets a Geometry object to a Placemark class.
     *
     * @param type      Integer representation; either parsed as a single geometry object or part
     *                  of a multigeometry object
     * @param tagName   Geometry object tagName; Polygon, LineStyle, Point
     * @param placemark Placemark class to add geometry objects to
     */
    private void createGeometry(int type, String tagName, Placemark placemark)
            throws IOException, XmlPullParserException {
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals(tagName))) {
            if (eventType == XmlPullParser.START_TAG && mParser.getName().equals("coordinates")) {
                if (type == GEOMETRY_TYPE) {
                    placemark.setGeometry(tagName, mParser.nextText());
                } else if (type == MULTIGEOMETRY_TYPE) {
                    placemark.setMultigeometry(tagName, mParser.nextText());
                }
            }
            eventType = mParser.next();
        }
    }

    /**
     * Recieves input from a parser and sets a MultiGeometry object to a Placemark class.
     */
    private void createMultiGeometry(Placemark placemark)
            throws IOException, XmlPullParserException {
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("MultiGeometry"))) {
            if (eventType == XmlPullParser.START_TAG) {
                String geometryName = mParser.getName();
                if (isGeometry(geometryName)) {
                    createGeometry(MULTIGEOMETRY_TYPE, geometryName, placemark);
                }
            }
            eventType = mParser.next();
        }
    }

    /**
     * Receives an ArrayList of Placemark objects and retrieves its geometry object; then creates a
     * corresponding GoogleMaps Options objects; and assigns styles and coordinates to this option
     * to store in an ArrayList of GoogleMapsOptions.
     */
    private void addToMap() {
        for (Placemark placemark : mPlacemark) {
            Style style = mStyles.get(placemark.getStyle());
            // Check if the style is stored
            if (style != null) {
                String geometryType = placemark.getGeometry().getType();
                Geometry geometry = placemark.getGeometry();
                if (geometryType.equals("Point")) {
                    mMapObjects.add(addPointToMap((Point) geometry, style));
                } else if (geometryType.equals("LineString")) {
                    mMapObjects.add(addLineStringToMap((LineString) geometry, style));
                } else if (geometryType.equals("Polygon")) {
                    mMapObjects.add(addPolygonToMap((Polygon) geometry, style));
                } else if (geometryType.equals("MultiGeometry")) {
                    addMultiGeometryToMap(placemark);
                }
            } else {
                Log.i("Style not found", placemark.getStyle());
            }
        }
    }

    /**
     * Addsa a KML Point to the map as a Marker by combining the styling and coordinates
     *
     * @param point contains coordinates for the Marker
     * @param style contains relevant styling properties for the Marker
     * @return Marker object
     */
    private Marker addPointToMap(Point point, Style style) {
        MarkerOptions markerOptions = style.getMarkerOptions();
        markerOptions.position((LatLng) point.getGeometry());
        Marker marker = mMap.addMarker(markerOptions);
        if (style.getIconUrl() != null) {
            new IconImageDownload(marker).execute(style.getIconUrl());
        }
        return marker;
    }

    /**
     * Adds a KML LineString to the map as a Polyline by combining the styling and coordinates
     *
     * @param lineString contains coordinates for the Polyline
     * @param style      contains relevant styling properties for the Polyline
     * @return Polyline object
     */
    private Polyline addLineStringToMap(LineString lineString, Style style) {
        PolylineOptions polylineOptions = style.getPolylineOptions();
        polylineOptions.addAll((Iterable<LatLng>) lineString.getGeometry());
        return mMap.addPolyline(polylineOptions);
    }

    /**
     * Adds a KML Polygon to the map as a Polygon by combining the styling and coordinates
     *
     * @param polygon contains coordinates for the Polygon
     * @param style   contains relevant styling properties for the Polygon
     * @return Polygon object
     */
    private com.google.android.gms.maps.model.Polygon addPolygonToMap(Polygon polygon,
            Style style) {
        PolygonOptions polygonOptions = style.getPolygonOptions();
        HashMap<ArrayList<LatLng>, Integer> poly = (HashMap<ArrayList<LatLng>, Integer>) polygon
                .getGeometry();
        for (Map.Entry<ArrayList<LatLng>, Integer> p : poly.entrySet()) {
            if (p.getValue() == OUTER_BOUNDARY) {
                polygonOptions.addAll(p.getKey());
            } else if (p.getValue() == INNER_BOUNDARY) {
                polygonOptions.addHole(p.getKey());
            }
        }
        return mMap.addPolygon(polygonOptions);
    }

    // TODO: implement this method once MultiGeometry class is created

    /**
     * Recieves an ArrayList of objects in a MultiGeometry. It then creates a corresponding
     * GoogleMapsOptions
     * object, assigns styles and coordinates to this option and stores it in an ArrayList of
     * GoogleMapsOptions
     */
    private void addMultiGeometryToMap(Placemark placemark) {
        Style style = mStyles.get(placemark.getStyle());
        ArrayList<Geometry> geometries = (ArrayList<Geometry>) placemark.getGeometry()
                .getGeometry();
        for (Geometry geometry : geometries) {
            if (style != null) {
                String geometryType = geometry.getType();
                if (geometryType.equals("Point")) {
                    mMapObjects.add(addPointToMap((Point) geometry, style));
                } else if (geometryType.equals("LineString")) {
                    mMapObjects.add(addLineStringToMap((LineString) geometry, style));
                } else if (geometryType.equals("Polygon")) {
                    mMapObjects.add(addPolygonToMap((Polygon) geometry, style));
                }
            } else {
            }
        }
    }


    /**
     * @return Retrieves an ArrayList of Objects; which are instances of either a Polyline, Polygon
     * or Marker object which have been added to a defined GoogleMap.
     */
    private ArrayList<Object> getMapObjects() {
        return mMapObjects;
    }

    /**
     * Downloads images for use as marker icons
     */
    private class IconImageDownload extends AsyncTask<String, Void, Bitmap> {

        final Marker mMarker;

        public IconImageDownload(Marker marker) {
            mMarker = marker;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String imageUrl = params[0];
            try {
                return BitmapFactory.decodeStream((InputStream) new URL(imageUrl).getContent());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
        }
    }
}
