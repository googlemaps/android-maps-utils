package com.google.maps.android.kml;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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

    private static final int GEOMETRY_TYPE = 0;

    private static final int MULTIGEOMETRY_TYPE = 1;

    /*TODO:
        Figure out a better place to put convertToLatLng in the Placemark class
        createMultiGeometry and createGeometry maybe can be the same class?
        Multigeometry currently doesnt implement style, only the coordinates
        Implement StyleMap.
        Implement BalloonStyle (Equivalent in GoogleMaps is IconWindow)
        Implement LabelStyle (Equivalent is IconGenerator Utility Library)
        Test Multigeometry.
    */

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
        assignGeometryOptions();
        addKmlLayerToMap(mMap);
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

    /**
     * Recieves input from a parser and creates a Style class if a Substyle start tag is detected,
     * or a Placemark class if a Feature class is detected.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
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

    /**
     * Recieves input from a parser and creates a LineStyle class to add to a Style class if
     * a LineStyle start tag is detected, or creates a PolyStyle class to add to a Style class id
     * a PolyStyle start tag is detected.
     * @throws IOException
     * @throws XmlPullParserException
     */

    private void createStyle() throws IOException, XmlPullParserException {
        Style styleProperties = new Style();
        String styleUrl = "#" + mParser.getAttributeValue(null, "id");
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("Style"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (eventType == XmlPullParser.START_TAG && mParser.getName().equals("LineStyle")) {
                    createLineStyle(styleProperties);
                } else if (eventType == XmlPullParser.START_TAG && mParser.getName().equals("PolyStyle")) {
                    createPolyStyle(styleProperties);
                }
            }
            eventType = mParser.next();
        }
        mStyles.put(styleUrl, styleProperties);
    }

    /**
     * Recieves input from a parser and adds a property if its corresponding start tag is detected
     * @param style Style class to add properties to
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void createLineStyle(Style style) throws XmlPullParserException, IOException {
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("LineStyle"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals("color")) {
                    style.setOutlineColor(mParser.nextText());
                } else if (mParser.getName().equals("width")) {
                    style.setWidth(mParser.nextText());
                }
            }
            eventType = mParser.next();
        }
    }

    /**
     * Recieves input from a parser and adds a property if its corresponding start tag is detected
     * @param style Style class to add properties to
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void createPolyStyle (Style style) throws XmlPullParserException, IOException {
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("PolyStyle"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals("color")) {
                    style.getPolygonOptions().put("fillColor", "#" + mParser.nextText());
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
     * Recieves input from a parser. If a style start tag is detected with an id, it sets the style
     * for the placemark. If a Geometry, or Multigeometry start tag is detected, it creates the
     * corresponding classes. If a property start tag is detected, then it adds those properties to
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    private void createPlacemark() throws IOException, XmlPullParserException {
        Placemark placemark = new Placemark();
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("Placemark"))) {
            String tagName = mParser.getName();
            if (eventType == XmlPullParser.START_TAG) {
                if (isStyle(tagName)) {
                    placemark.setStyle(mParser, tagName);
                } else if (isGeometry(tagName)) {
                    createGeometry(GEOMETRY_TYPE, tagName, placemark);
                } else if (isProperty(tagName)) {
                    placemark.setProperties(tagName, mParser.nextText());
                } else if (isMultiGeometry(tagName)){
                    createMultiGeometry(placemark);
                }
            }
            eventType = mParser.next();
        }
        mPlacemark.add(placemark);
    }

    /**
     * Recieves input from a parser and sets a Geometry object to a Placemark class.
     * @param type  Integer representation; either parsed as a single geometry object or part of a multigeometry object
     * @param tagName  Geometry object tagName; Polygon, LineStyle, Point
     * @param placemark Placemark class to add geometry objects to
     * @throws IOException
     * @throws XmlPullParserException
     */
    private void createGeometry(int type, String tagName, Placemark placemark) throws IOException, XmlPullParserException {
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
     * @param placemark
     * @throws IOException
     * @throws XmlPullParserException
     */
    private void createMultiGeometry (Placemark placemark) throws IOException, XmlPullParserException {
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
     * Recieves an ArrayList of Placemark objects and retrieves its geometry object; then creates a
     * corresponding GoogleMapsOptions objects; and assigns styles and coordinates to this option
     * to store in an ArrayList of GoogleMapsOptions.
     */
    private void assignGeometryOptions() {
        for (Placemark placemark : mPlacemark) {
            if (placemark.getPolygon() != null) {
                PolygonOptions polygonOptions = new PolygonOptions();
                addPolygonCoordinates(placemark.getPolygon(), polygonOptions);
                addPolygonStyle(mStyles.get(placemark.getProperties().get("styleUrl")).getPolygonOptions(), polygonOptions);
                mOptions.add(polygonOptions);
            } else if (placemark.getPolyline() != null) {
                PolylineOptions polylineOptions = new PolylineOptions();
                addPolylineCoordinates(placemark.getPolyline(), polylineOptions);
                addPolylineStyle(mStyles.get(placemark.getProperties().get("styleUrl")).getPolylineOptions(), polylineOptions);
                mOptions.add(polylineOptions);
            } else if (placemark.getPoint() != null) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position((LatLng) placemark.getPoint().getGeometry());
                mOptions.add(markerOptions);
            } else if (placemark.getMultigeometry() != null) {
                assignMultipleGeometryOptions(placemark);
            }
        }
    }

    /**
     * Recieves an ArrayList of objects in a MultiGeometry. It then creates a corresponding GoogleMapsOptions
     * object, assigns styles and coordinates to this option and stores it in an ArrayList of GoogleMapsOptions
     * @param placemark
     */
    private void assignMultipleGeometryOptions (Placemark placemark) {
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


    /**
     * Adds an ArrayList of LatLng points from a LineString class to a PolylineOptions object
     * @param lineString    A LineString class to retrieve LatLng points from
     * @param polylineOptions   A PolylineOptions Object to add LatLng points to
     */
    private void addPolylineCoordinates(LineString lineString, PolylineOptions polylineOptions) {
        ArrayList<LatLng> lineStringPoint = ((ArrayList<LatLng>) lineString.getGeometry());
        polylineOptions.addAll(lineStringPoint);
    }

    /**
     * Retrieves a HashMap of strings which represent PolylineOption properties and adds them to
     * a PolylineOption Object,
     *
     * @param polylineProperties A HashMap of strings which represent PolylineOption properties
     * @param polylineOptions   A PolylineOptions Object to add properties to
     */
    private void addPolylineStyle(HashMap<String, String> polylineProperties, PolylineOptions polylineOptions) {
        if (polylineProperties.containsKey("color")) {
            polylineOptions.color(Color.parseColor(polylineProperties.get("color")));
        } if (polylineProperties.containsKey("width")) {
            Float width = Float.parseFloat(polylineProperties.get("width"));
            polylineOptions.width(width);
        }
    }

    /**
     * Retrieves a HashMap of strings which represent PolygonOption properties and adds them to
     * a PolygonOption Object,
     *
     * @param polygonProperties A HashMap of strings which represent PolygonOption properties
     * @param polygonOptions   A PolygonOptions Object to add properties to
     */
    private void addPolygonStyle(HashMap<String, String> polygonProperties, PolygonOptions polygonOptions) {
        if (polygonProperties.containsKey("strokeColor")) {
            polygonOptions.strokeColor(Color.parseColor(polygonProperties.get("strokeColor")));
        } if (polygonProperties.containsKey("strokeWidth")) {
            Float width = Float.parseFloat(polygonProperties.get("strokeWidth"));
            polygonOptions.strokeWidth(width);
        } if (polygonProperties.containsKey("fillColor")) {
            polygonOptions.fillColor(Color.parseColor(polygonProperties.get("fillColor")));
        } if (polygonProperties.containsKey("visible")) {
            Boolean isVisible = Boolean.parseBoolean(polygonProperties.get("visible"));
            polygonOptions.visible(isVisible);
        }
    }

    /**
     *  Adds an ArrayList of LatLng points to a PolygonOption, retrieved from a HashMap. If the
     *  value of the ArrayList is set as an outer boundary, then the LatLng points are added as
     *  a polygon coordinate. If the value of the ArrayList is set as an inner boundary, then the
     *  LatLng points are added as a polygon hole.
     *
     * @param polygon   A KML Object to retrieve LatLng points from
     * @param polygonOptions    A GoogleMap Polygon to add LatLng points to
     */
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

    /**
     * Retrieves a list of GoogleMapOptions from an ArrayList and adds them to a GoogleMap which
     * is defined. It also stores instances of a Polyline, Polygon, or Marker object which is created
     * from adding the GoogleMapOptions to the GoogleMap and is stored in an ArrayList of Objects.
     */
    private void addKmlLayerToMap(GoogleMap googleMap) {
        for (Object objects: mOptions) {
            if (objects instanceof PolylineOptions) {
                mObjects.add(googleMap.addPolyline((PolylineOptions) objects));
            } else if (objects instanceof PolygonOptions) {
                mObjects.add(googleMap.addPolygon((PolygonOptions) objects));
            } else if (objects instanceof MarkerOptions) {
                mObjects.add(googleMap.addMarker((MarkerOptions) objects));
            }
        }
    }

    /**
     * @return  Retrieves an ArrayList of Objects; which are instances of either a Polyline, Polygon or
     * Marker object which have been added to a defined GoogleMap.
     */
    private ArrayList<Object> getMapObjects() {
        return mObjects;
    }
}
