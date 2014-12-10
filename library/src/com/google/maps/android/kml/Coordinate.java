package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Represents a series of coordinates in a placemark
 */
public class Coordinate {

    private final int UNINITIALIZED = -1;

    private final int POLYGON_TYPE = 0;

    private final int LINESTRING_TYPE = 1;

    private final int POINT_TYPE = 2;

    private final int INNER_BOUNDARY = 0;

    private final int OUTER_BOUNDARY = 1;

    private final int LATITUDE = 0;

    private final int LONGITUDE = 1;

    private int type;

    private int boundary;

    private ArrayList<LatLng> coordinateList;

    /**
     * Creates a new coordinate
     */
    public Coordinate() {
        coordinateList = null;
        type = UNINITIALIZED;
        boundary = UNINITIALIZED;
    }

    /**
     * Takes in a XMLPullParser containing properties for a parser and saves relevant properties
     *
     * @param p XMLPullParser which reads input from designated source
     * @throws XmlPullParserException
     * @throws IOException
     */
    public void coordinateProperties(XmlPullParser p) throws XmlPullParserException, IOException {
        int eventType = p.getEventType();
        String name = p.getName();

        // Iterate through the document until the closing coordinates tag is reached
        while (!(eventType == XmlPullParser.END_TAG && p.getName().equals("coordinates"))) {
            name = p.getName();
            // Check if the current tag is the beginning of a coordinate tag
            if (eventType == XmlPullParser.START_TAG) {
                if (name.equals("coordinates")) {
                    //TODO: Assign coordinates, do the things.

                    /*
                     setCoordinateList(p);
                     */
                }
            }
            eventType = p.next();
        }
    }

    /**
     * Takes an integer value from 0 to 1 and sets it to the corresponding boundary (either outer
     * or inner). The boundary is uninitialized if the type is not set to polygon.
     *
     * @param bBoundary Integer value which corresponds to an inner boundary or outer boundary
     *                  INNER_BOUNDARY = 0;
     *                  OUTER_BOUNDARY = 1;
     */
    public void setBoundary(int bBoundary) {
        if ((bBoundary == INNER_BOUNDARY || bBoundary == OUTER_BOUNDARY) && (type
                == POLYGON_TYPE)) {
            boundary = bBoundary;
        } else if (type != POLYGON_TYPE) {
            System.out
                    .println("Polygon type expected! An inner or outer boundary cannot be set if " +
                            "your coordinate type is line string or point. Please check your KML input");
            boundary = UNINITIALIZED;
        } else {
            System.out.println("Inner boundary or outer boundary expected!");
            boundary = UNINITIALIZED;
        }
    }

    /**
     * Assigns the type of placemark this coordinate object belongs to
     *
     * @param tType Integer value which corresponds to either polygon, line or point type.
     *              POLYGON_TYPE = 0;
     *              LINESTRING_TYPE = 1;
     *              POINT_TYPE = 2;
     */
    public void setType(int tType) {
        if (tType == POLYGON_TYPE || tType == LINESTRING_TYPE || tType == POINT_TYPE) {
            type = tType;
        } else {
            System.out.println("Line, String or Point type expected!");
            type = UNINITIALIZED;
        }
    }

    /**
     * Receives a list of coordinates from a string value, and assigns a latlng
     * value to each pair of latitude and longitude points in a line, separated by the comma
     * delimiter. The method ignores any lines which may be in the incorrect format, ie
     * empty lines, lines without a comma, etc.
     *
     * @param text String input in the format:
     *             <lat>,<lon>
     *             <lat>,<lon>
     *             <lat>,<lon>
     */
    public void setCoordinateList(String text) {
        coordinateList = new ArrayList<LatLng>();
        String[] lines = text.split("\n");
        for (String point : lines) {
            String[] coordinate = point.split(",");
            if (coordinate.length > 2) {
                coordinateList.add(convertToLatLng(coordinate));
            }
        }
    }

    /**
     * Receives a pair of coordinate values which are separated by a comma and assigns a latLng
     * value to it. The method then returns this latLng value. The method ignores any integers
     * which are found after the second element of the array. If lon or lat values are greater
     * than their respective geographic boundaries, then it is set to the maximum possible value.
     * Returns null if:
     * × Param is not an integer value
     * × Param is a null string
     *
     * @param coordinate An array of integer values, individually representing coordinates
     * @return lat     LatLng value
     */
    public LatLng convertToLatLng(String[] coordinate) {
        try {
            Double latDouble = Double.parseDouble(coordinate[LATITUDE]);
            Double lonDouble = Double.parseDouble(coordinate[LONGITUDE]);
            LatLng latLng = new LatLng(latDouble, lonDouble);
            return latLng;
        } catch (NumberFormatException e) {
            System.out.println("Non-integer value found in coordinate tag!");
        } catch (NullPointerException e) {
            System.out.println("No value found in between coordinate tags!");
        }
        return null;
    }

    /**
     * Retrieves the type of boundary (inner or outer) this coordinate possesses
     *
     * @return boundary type, represented by an integer or -1 if not set
     */
    public int getBoundary() {
        return boundary;
    }

    /**
     * Retrieves the coordinate type (poly, line or point) this coordinate posses
     *
     * @return coordinate type, represented by an integer or -1 if not set
     */
    public int getType() {
        return type;
    }

    /**
     * Retrieves an Arraylist of LatLng points
     *
     * @return an Arraylist of LatLng points, null if not set.
     */
    public ArrayList<LatLng> getCoordinateList() {
        return coordinateList;
    }


}
