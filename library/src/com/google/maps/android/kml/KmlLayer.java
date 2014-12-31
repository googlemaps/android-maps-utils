package com.google.maps.android.kml;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Document class allows for users to input their KML data and output it onto the map
 */
public class KmlLayer {

    /**
     * XML Pull Parser, which reads in a KML Document
     */
    private XmlPullParser mParser;

    /**
     * Hashmap of Style classes. The key is a string value which represents the id of the Style in
     * the KML document.
     */
    private HashMap<String, Style> mStyles;

    /**
     * A collection of GoogleMap objects (Polygon, Polyline, Marker) which have been put on the map
     */
    private ArrayList<Object> mObjects;

    /**
     * A collection of Placemark objects
     */
    private ArrayList<Placemark> mPlacemark;

    /**
     * A collection of GoogleMap options (PolygonOptions, PolylineOptions, MarkerOptions)
     */
    private ArrayList<Object> mOptions;

    /**
     * A Google Map
     */
    private GoogleMap mMap;

    private static final int INNER_BOUNDARY = 0;

    private static final int OUTER_BOUNDARY = 1;

    public KmlLayer(GoogleMap map, InputStream stream) throws XmlPullParserException, JSONException, IOException {
        this.mParser = convertUrlToParser(stream);
        this.mMap = map;
        this.mStyles = new HashMap<String, Style>();
        this.mPlacemark = new ArrayList<Placemark>();
        this.mObjects = new ArrayList<Object>();
        this.mOptions = new ArrayList<Object>();
    }

    public void setKmlData() throws XmlPullParserException, JSONException, IOException {
        importKML();
        assignStyles();
        addKmlLayerToMap();
    }

    public XmlPullParser convertUrlToParser(InputStream stream) throws JSONException, XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(stream, null);
        return parser;
    }

    private static boolean isProperty(String startTag) {
        return (startTag.equals("name") || startTag.equals("description") || startTag.equals("visibility"));
    }


    private static boolean isStyle(String name) {
        return name.equals("styleUrl");
    }

    private static boolean isGeometry(String name) {
        return (name.equals("LineString") || name.equals("Polygon") || name.equals("Point"));
    }

    private static boolean isMultiGeometry (String name) {
        return (name.equals("MultiGeometry"));
    }

    private void importKML() throws XmlPullParserException, IOException {
        int eventType = mParser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals("Style")) createStyle();
                if (mParser.getName().equals("Placemark")) createPlacemark();
            }
            eventType = mParser.next();
        }
    }

    private void createStyle() throws IOException, XmlPullParserException {
        Style styleProperties = new Style();
        String styleUrl = "#" + mParser.getAttributeValue(null, "id");
        styleProperties.styleProperties(mParser);
        mStyles.put(styleUrl, styleProperties);
    }

    private void createPlacemark() throws IOException, XmlPullParserException {
        Placemark placemark = new Placemark();
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("Placemark"))) {
            String name = mParser.getName();
            if (eventType == XmlPullParser.START_TAG) {
                if (isStyle(name)) placemark.setStyle(mParser, name);
                else if (isGeometry(name)) createGeometry(name, placemark);
                else if (isProperty(name)) placemark.setProperties(name, mParser.nextText());
                else if (isMultiGeometry(name)) createMultiGeometry(placemark);
            }
            eventType = mParser.next();
        }
        mPlacemark.add(placemark);
    }


    private void createGeometry(String name, Placemark placemark) throws IOException, XmlPullParserException {
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals(name))) {
            if (eventType == XmlPullParser.START_TAG && mParser.getName().equals("coordinates")) {
                mParser.next();
                if (name.equals("LineString")) {
                    placemark.setLineString(mParser.getText());
                } else if (name.equals("Polygon")) {
                    placemark.setPolygon(mParser.getText());
                } else if (name.equals("Point")) {
                    placemark.setPoint(mParser.getText());
                }
            }
            eventType = mParser.next();
        }
    }


    private void createMultiGeometry (Placemark placemark) throws IOException, XmlPullParserException {
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("MultiGeometry"))) {
            if (eventType == XmlPullParser.START_TAG) {
               String name = mParser.getName();
                if (isGeometry(name)) {
                    while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals(name))) {
                        if (eventType == XmlPullParser.START_TAG && mParser.getName().equals("coordinates")) {
                            mParser.next();
                            placemark.setMultigeometry(name, mParser.getText());
                        }
                        eventType = mParser.next();
                    }
                }
            }
            eventType = mParser.next();
        }
    }


    private void assignStyles() {
        for (Placemark placemark : mPlacemark) {
            if (placemark.getPolygon() != null) {
                PolygonOptions polygonOptions = new PolygonOptions();
                addPolygonCoordinates(placemark.getPolygon(), polygonOptions);
                addPolygonStyle(placemark, polygonOptions);
                mOptions.add(polygonOptions);
            } else if (placemark.getPolyline() != null) {
                PolylineOptions polylineOptions = new PolylineOptions();
                addPolylineCoordinates(placemark.getPolyline(), polylineOptions);
                addPolylineStyle(placemark, polylineOptions);
                mOptions.add(polylineOptions);
            } else if (placemark.getPoint() != null) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position((LatLng) placemark.getPoint().getGeometry());
                mOptions.add(markerOptions);
            } else if (placemark.getMultigeometry() != null) {
                for (Object object: placemark.getMultigeometry()) {
                    if (object instanceof Polygon) {
                        PolygonOptions polygonOptions = new PolygonOptions();
                        addPolygonCoordinates((Polygon) object, polygonOptions);
                        mOptions.add(polygonOptions);
                    } else if (object instanceof  Point) {
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position((LatLng)((Point) object).getGeometry());
                        mOptions.add(markerOptions);
                    } else if (object instanceof LineString) {
                        PolylineOptions polylineOptions = new PolylineOptions();
                        addPolylineCoordinates((LineString) object, polylineOptions);
                        mOptions.add(polylineOptions);
                    }
                }
            }
        }
    }


    private void addPolylineCoordinates(LineString lineString, PolylineOptions polylineOptions) {
        ArrayList<LatLng> lineStringPoint = ((ArrayList<LatLng>) lineString.getGeometry());
        polylineOptions.addAll(lineStringPoint);
    }

    private void addPolylineStyle(Placemark placemark, PolylineOptions polylineOptions) {
        HashMap<String, String> polyLineProperties = mStyles.get(placemark.getProperties().get("styleUrl")).getPolylineOptions();
        if (polyLineProperties.containsKey("color")) {
            polylineOptions.color(Color.parseColor(polyLineProperties.get("color")));
        } if (polyLineProperties.containsKey("width")) {
            Float width = Float.parseFloat(polyLineProperties.get("width"));
            polylineOptions.width(width);
        }
    }

    private void addPolygonCoordinates(Polygon polygon, PolygonOptions polygonOptions) {
        HashMap<ArrayList<LatLng>, Integer> poly = ((HashMap<ArrayList<LatLng>, Integer>) polygon.getGeometry());
        for (Map.Entry<ArrayList<LatLng>, Integer> p : poly.entrySet()) {
            if (p.getValue() == OUTER_BOUNDARY) {
                polygonOptions.addAll(p.getKey());
            } else if (p.getValue() == INNER_BOUNDARY) {
                polygonOptions.addHole(p.getKey());
            }
        }
    }

    private void addPolygonStyle(Placemark placemark, PolygonOptions polygonOptions) {
        HashMap<String, String> polygonProperties = mStyles.get(placemark.getProperties().get("styleUrl")).getPolylineOptions();
        if (polygonProperties.containsKey("strokeColor")) {
            polygonOptions.strokeColor(Color.parseColor(polygonProperties.get("strokeColor")));
        } if (polygonProperties.containsKey("strokeWidth")) {
            Float width = Float.parseFloat(polygonProperties.get("strokeWidth"));
            polygonOptions.strokeWidth(width);
        } if (polygonProperties.containsKey("fillColor")) {
            Float width = Float.parseFloat(polygonProperties.get("fillColor"));
            polygonOptions.strokeWidth(width);
        } if (polygonProperties.containsKey("visible")) {
            Boolean isVisible = Boolean.parseBoolean(polygonProperties.get("visible"));
            polygonOptions.visible(isVisible);
        }
    }


    private void addKmlLayerToMap() {
        System.out.println(mOptions.size());
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
}
