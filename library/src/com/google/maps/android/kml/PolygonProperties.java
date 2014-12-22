package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Represents a series of coordinates in a placemark
 */
public class PolygonProperties {

    private static final int LATITUDE = 0;

    private static final int LONGITUDE = 1;

    private HashMap< ArrayList<LatLng>, Integer> polygonBoundaries;

    private static Logger LOGGER = Logger.getLogger("InfoLogging");

    /**
     * Creates a new hashmap of latlng points, with the key value being either an inner boundary or
     * an outer boundary.
     */
    public PolygonProperties() {
        polygonBoundaries = new HashMap< ArrayList<LatLng>, Integer>();
    }

    /**
     * Takes in a XMLPullParser containing properties for a parser and saves relevant properties
     *
     * @param p XMLPullParser which reads input from designated source
     */
    public void pointProperties (XmlPullParser p, int type) throws XmlPullParserException, IOException {
        int eventType = p.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && p.getName().equals("coordinates"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (p.getName().equals("coordinates")) {
                    p.next();
                    setPointList(p.getText(), type);
                }
            }
            eventType = p.next();
        }
    }

    /**
     * Converts text to latlng points to add to an array
     * @param text a list of latlng points represented as a string
     */
    private void setPointList(String text, int boundaryType) {
        ArrayList<LatLng> mPointList = new ArrayList<LatLng>();
        String[] lines = text.trim().split("(\\s+)");
        if (!isCoordinateEmpty(lines)) {
            for (String point : lines) {
                String[] coordinate = point.split(",");
                if (isValidCoordinateLength(coordinate) && isValidCoordinateType(coordinate)) {
                    mPointList.add(convertToLatLng(coordinate));
                }
            }
        }
        polygonBoundaries.put(mPointList, boundaryType);
    }

    /**
     * Determines whether a list of strings given have at least two points
     * @param coordinate A list of strings given
     * @return true if it contains a lat or long point, false otherwise
     */
    public boolean isValidCoordinateLength (String[] coordinate) {
        if (coordinate.length > 1) return true;
        return false;
    }

    /**
     * Determines whether a list of strings given converts to the correct format
     * @param coordinate The tuple or pair of coordinates given
     * @return  true if it can be converted in a double, false otherwise
     */
    public boolean isValidCoordinateType (String[] coordinate) {
        try {
            Double.parseDouble(coordinate[LONGITUDE]);
            Double.parseDouble(coordinate[LATITUDE]);
            return true;
        } catch (NumberFormatException e) {
            LOGGER.info("Coordinate not in valid format!");
            return false;
        }
    }

    public boolean isCoordinateEmpty(String[] lines) {
        if (lines.length == 0) return true;
        return false;
    }


    /**
     * Receives a pair of coordinate values which are separated by a comma and assigns a latLng
     * value to it. The method then returns this latLng value. The method ignores any integers
     * which are found after the second element of the array. If lon or lat values are greater
     * than their respective geographic boundaries, then it is set to the maximum possible value.
     *
     * @param coordinate An array of integer values, individually representing coordinates
     * @return lat     LatLng value or null if param is a null string or isn't an integer
     */
    private LatLng convertToLatLng(String[] coordinate) {
        Double latDouble = Double.parseDouble(coordinate[LONGITUDE]);
        Double lonDouble = Double.parseDouble(coordinate[LATITUDE]);
        return new LatLng(latDouble, lonDouble);
    }

    /**
     * Retrieves an Arraylist of LatLng points
     *
     * @return an Arraylist of LatLng points, null if not set.
     */
    public HashMap<ArrayList<LatLng>, Integer> getPolygonPoints() {
        return polygonBoundaries;
    }

    /**
     * Determines if points for the polygon exist; for KML to be valid, a polygon must have an
     * outer boundary specified.
     * @return true if points for polygons exist, false otherwise
     */
    private boolean hasPolygonPoints () {
        return (polygonBoundaries != null);
    }

    private boolean isValidPolygonKML() {
        //TODO: Create a test for valid polygon using these parameters:
        //Only one outer boundary, if inner boundary, must exist outer boundary
        //If inner boundary, then must be contained in outer boundary, a bit hard to do. IDK.
        //Each coordinate in a boundary must be at least 4 points
        return true;
    }

}
