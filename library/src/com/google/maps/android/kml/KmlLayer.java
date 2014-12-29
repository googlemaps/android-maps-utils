package com.google.maps.android.kml;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
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

    private XmlPullParser mParser;

    private HashMap<String, StyleProperties> mStyles;

    private ArrayList<Object> mObjects;

    private ArrayList<PlacemarkProperties> mPlacemarkProperties;

    private GoogleMap mMap;

    private static final int INNER_BOUNDARY = 0;

    private static final int OUTER_BOUNDARY = 1;

    private ArrayList<Object> mOptions;

    //TODO: MAJOR TODOS:
    //Implement Icon and IconStyle
    //Figure out if Point has a style
    //Implement Multigeometry


    /**
     * Constructs a new Document object
     *
     * @param map    Map object
     */
    public KmlLayer(GoogleMap map, InputStream stream) throws XmlPullParserException, JSONException, IOException {
        this.mParser = convertUrlToParser(stream);
        this.mMap = map;
        setKmlData();
    }


    private void setKmlData() throws XmlPullParserException, JSONException, IOException {
        this.mStyles = new HashMap<String, StyleProperties>();
        this.mPlacemarkProperties = new ArrayList<PlacemarkProperties>();
        this.mObjects = new ArrayList<Object>();
        this.mOptions = new ArrayList<Object>();
        importKML();
        assignStyles();
        addKmlLayerToMap();
    }

    private XmlPullParser convertUrlToParser (InputStream stream) throws JSONException, XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(stream, null);
        return parser;
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
        StyleProperties styleProperties = new StyleProperties();
        String styleUrl = "#" + mParser.getAttributeValue(null, "id");
        styleProperties.styleProperties(mParser);
        mStyles.put(styleUrl, styleProperties);
    }

    /**
     * Creates a new Placemark and puts it into a HashMap. Key value is the style id specified in the tag,
     * Value is a newly created Placemark class
     * @param mParser XmlPullParser object for KML document parsing
     * @throws IOException
     * @throws XmlPullParserException
     */
    private void createPlacemark(XmlPullParser mParser) throws IOException, XmlPullParserException {
        PlacemarkProperties placemarkProperties = new PlacemarkProperties();
        placemarkProperties.placemarkProperties(mParser);
        this.mPlacemarkProperties.add(placemarkProperties);
    }

    /**
     * Retreives values from Placemarks and Styles, if they exist, and creates a geometry option o
     * object with appropriate properties
     */
    private void assignStyles() {
        for (PlacemarkProperties placemarkProperties : mPlacemarkProperties) {
            if (placemarkProperties.getPolygon() != null) {
                mOptions.add(assignPolygonOptions(placemarkProperties));
            } else if (placemarkProperties.getPolyline() != null) {
                mOptions.add(assignLineOptions(placemarkProperties));
            } else if (placemarkProperties.getPoint() != null) {
                mOptions.add(assignMarkerOptions(placemarkProperties));
            }
        }
        System.out.println(mOptions.size());
    }

    /**
     *
     * @param placemarkProperties
     * @return
     */
    private PolylineOptions assignLineOptions(PlacemarkProperties placemarkProperties) {
        PolylineOptions polylineOptions = new PolylineOptions();

        ArrayList<LatLng> lineStringPoint = ((ArrayList<LatLng>)placemarkProperties.getPolyline().getGeometry());
        polylineOptions.addAll(lineStringPoint);

        boolean hasStyleURL =  placemarkProperties.getProperties().containsKey("styleUrl");
        if (hasStyleURL) {
            boolean isStyleSpecified = mStyles.containsKey(placemarkProperties.getProperty("styleUrl"));
            if (isStyleSpecified) {
                HashMap<String, String> polyLineProperties = mStyles.get(placemarkProperties.getProperty("styleUrl")).getPolylineOptions();
                if (polyLineProperties.containsKey("color")) {
                    polylineOptions.color(Color.parseColor(polyLineProperties.get("color")));
                }
                if (polyLineProperties.containsKey("width")) {
                    Float width = Float.parseFloat(polyLineProperties.get("width"));
                    polylineOptions.width(width);
                }
            }
        }
        return polylineOptions;
    }

    private MarkerOptions assignMarkerOptions(PlacemarkProperties placemarkProperties) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position((LatLng) placemarkProperties.getPoint().getGeometry());
        return markerOptions;
    }


    /**
     * Gets a placemark class which has a polygon option and creates a Google Maps PolygonOptions.
     * It then adds in corresponding values. Currently only supports adding:
     * Stroke color, stroke width, fill color, is visible, outer points, inner points (holes
     * @param placemarkProperties
     * @return
     */

    private PolygonOptions assignPolygonOptions(PlacemarkProperties placemarkProperties) {
        PolygonOptions polygonOptions = new PolygonOptions();


        HashMap< ArrayList<LatLng>, Integer> one = ((HashMap< ArrayList<LatLng>, Integer>) placemarkProperties.getPolygon().getGeometry());


        for (Map.Entry<ArrayList<LatLng>, Integer> p: one.entrySet()) {
            if (p.getValue() == OUTER_BOUNDARY) {
                polygonOptions.addAll(p.getKey());
            } else if (p.getValue() == INNER_BOUNDARY) {
                polygonOptions.addHole(p.getKey());
            }
        }

        boolean hasStyleURL =  placemarkProperties.getProperties().containsKey("styleUrl");
        if (hasStyleURL) {
            boolean isStyleSpecified = mStyles.containsKey(placemarkProperties.getProperty("styleUrl"));
            if (isStyleSpecified) {
                HashMap<String, String> polygonProperties = mStyles.get(placemarkProperties.getProperty("styleUrl")).getPolygonOptions();
                if (polygonProperties.containsKey("strokeColor")) {
                    polygonOptions.strokeColor(Color.parseColor(polygonProperties.get("strokeColor")));
                } if (polygonProperties.containsKey("strokeWidth")) {
                    Float width = Float.parseFloat(polygonProperties.get("strokeWidth"));
                    polygonOptions.strokeWidth(width);
                } if (polygonProperties.containsKey("fillColor")) {
                    Float width = Float.parseFloat(polygonProperties.get("fillColor"));
                    polygonOptions.strokeWidth(width);
                } if (polygonProperties.containsKey("visible")) {
                    //TODO: See if we actually support boolean values, havent checked.
                    Boolean isVisible = Boolean.parseBoolean(polygonProperties.get("visible"));
                    polygonOptions.visible(isVisible);
                }
            }
        }
        return polygonOptions;
    }


    /**
     * Adds geometry options options onto the map. The geometry object itself is stored in another
     * data structure for retrieval later
     */
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
