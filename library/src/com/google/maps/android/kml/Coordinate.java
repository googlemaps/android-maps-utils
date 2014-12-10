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

    private static final int sUNINITIALIZED = -1;

    private static final int POLYGON_TYPE = 0;

    private static final int LINESTRING_TYPE = 1;

    private static final int POINT_TYPE = 2;

    private static final int INNER_BOUNDARY = 0;

    private static final int OUTER_BOUNDARY = 1;

    private static final int LATITUDE = 0;

    private static final int LONGITUDE = 1;

    private int mType;

    private int mBoundary;

    private ArrayList<LatLng> mCoordinateList;

    /**
     * Creates a new coordinate
     */
    public Coordinate() {
        mCoordinateList = null;
        mType = sUNINITIALIZED;
        mBoundary = sUNINITIALIZED;
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
     * Takes an integer value from 0 to 1 and sets it to the corresponding mBoundary (either outer
     * or inner). The mBoundary is uninitialized if the mType is not set to polygon.
     *
     * @param bBoundary Integer value which corresponds to an inner mBoundary or outer mBoundary
     *                  INNER_BOUNDARY = 0;
     *                  OUTER_BOUNDARY = 1;
     */
    public void setBoundary(int bBoundary) {
        if ((bBoundary == INNER_BOUNDARY || bBoundary == OUTER_BOUNDARY) && (mType
                == POLYGON_TYPE)) {
            mBoundary = bBoundary;
        } else if (mType != POLYGON_TYPE) {
            System.out
                    .println("Polygon mType expected! An inner or outer mBoundary cannot be set if " +
                            "your coordinate mType is line string or point. Please check your KML input");
            mBoundary = sUNINITIALIZED;
        } else {
            System.out.println("Inner mBoundary or outer mBoundary expected!");
            mBoundary = sUNINITIALIZED;
        }
    }

    /**
     * Assigns the mType of placemark this coordinate object belongs to
     *
     * @param tType Integer value which corresponds to either polygon, line or point mType.
     *              POLYGON_TYPE = 0;
     *              LINESTRING_TYPE = 1;
     *              POINT_TYPE = 2;
     */
    public void setType(int tType) {
        if (tType == POLYGON_TYPE || tType == LINESTRING_TYPE || tType == POINT_TYPE) {
            mType = tType;
        } else {
            System.out.println("Line, String or Point mType expected!");
            mType = sUNINITIALIZED;
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
        mCoordinateList = new ArrayList<LatLng>();
        String[] lines = text.split("\n");
        for (String point : lines) {
            String[] coordinate = point.split(",");
            if (coordinate.length > 2) {
                mCoordinateList.add(convertToLatLng(coordinate));
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
     * Retrieves the mType of mBoundary (inner or outer) this coordinate possesses
     *
     * @return mBoundary mType, represented by an integer or -1 if not set
     */
    public int getBoundary() {
        return mBoundary;
    }

    /**
     * Retrieves the coordinate mType (poly, line or point) this coordinate posses
     *
     * @return coordinate mType, represented by an integer or -1 if not set
     */
    public int getType() {
        return mType;
    }

    /**
     * Retrieves an Arraylist of LatLng points
     *
     * @return an Arraylist of LatLng points, null if not set.
     */
    public ArrayList<LatLng> getCoordinateList() {
        return mCoordinateList;
    }


}
