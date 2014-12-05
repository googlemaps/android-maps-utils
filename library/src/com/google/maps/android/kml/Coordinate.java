package com.google.maps.android.kml;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;


public class Coordinate {

    private int UNINITIALIZED = -1;

    private int POLYGON_TYPE = 0;
    private int LINESTRING_TYPE = 1;
    private int POINT_TYPE = 2;

    private int INNER_BOUNDARY = 0;
    private int OUTER_BOUNDARY = 1;

    private int LATITUDE = 0;
    private int LONGITUDE = 1;

    private int type;
    private int boundary;

    private ArrayList<LatLng> coordinateList;

    public Coordinate() {
        coordinateList = null;
        type = UNINITIALIZED;
        boundary = UNINITIALIZED;
    }

    /**********************************
     * Takes an integer value from 0 to 1 and sets it to the corresponding boundary (either outer
     * or inner). The boundary is uninitialized if the type is not set to polygon.
     * @param bBoundary Integer value which corresponds to an inner boundary or outer boundary
     *                  INNER_BOUNDARY = 0;
     *                  OUTER_BOUNDARY = 1;
     **********************************/

    public void setBoundary (int bBoundary) {
        if ((bBoundary == INNER_BOUNDARY || bBoundary == OUTER_BOUNDARY) && (type == POLYGON_TYPE)) {
            boundary = bBoundary;
        } else if (type != POLYGON_TYPE) {
            System.out.println("Polygon type expected! An inner or outer boundary cannot be set if " +
                    "your coordinate type is line string or point. Please check your KML input");
            boundary = UNINITIALIZED;
        } else {
            System.out.println("Inner boundary or outer boundary expected!");
            boundary = UNINITIALIZED;
        }
    }

    /**********************************
        @param tType Integer value which corresponds to either polygon, line or point type.
        POLYGON_TYPE = 0;
        LINESTRING_TYPE = 1;
        POINT_TYPE = 2;
     **********************************/
    public void setType(int tType) {
        if (tType == POLYGON_TYPE || tType == LINESTRING_TYPE || tType == POINT_TYPE) {
            type = tType;
        } else {
            System.out.println("Line, String or Point type expected!");
            type = UNINITIALIZED;
        }
    }

    /**********************************
        Receives a list of coordinates from a string value, and assigns a latlng
        value to each pair of latitude and longitude points in a line, separated by the comma
        delimiter. The method ignores any lines which may be in the incorrect format, ie
        empty lines, lines without a comma, etc.
        @param text String input in the format:
                    <lat>,<lon>
                    <lat>,<lon>
                    <lat>,<lon>
     **********************************/
    public void setCoordinateList (String text) {
        coordinateList = new ArrayList<LatLng>();
        String[] lines = text.split("\n");
        for (String point: lines) {
            String[] coordinate = point.split(",");
            if (coordinate.length > 2) {
                coordinateList.add(convertToLatLng(coordinate));
            }
        }
    }

    /**********************************
        Receives a pair of coordinate values which are separated by a comma and assigns a latLng
        value to it. The method then returns this latLng value. The method ignores any integers
        which are found after the second element of the array. If lon or lat values are greater
        than their respective geographic boundaries, then it is set to the maximum possible value.
        Returns null if:
        × Param is not an integer value
        × Param is a null string

         @param      coordinate  An array of integer values, individually representing coordinates
         @return    lat     LatLng value
     **********************************/
    public LatLng convertToLatLng (String[] coordinate) {
        try {
            Double latDouble = Double.parseDouble(coordinate[LATITUDE]);
            Double lonDouble = Double.parseDouble(coordinate[LONGITUDE]);
            LatLng latLng = new LatLng(latDouble,lonDouble);
            return latLng;
        } catch (NumberFormatException e) {
           System.out.println("Non-integer value found in coordinate tag!");
        } catch (NullPointerException e) {
            System.out.println("No value found in between coordinate tags!");
        }
        return null;
    }

    public int getBoundary() {
        return boundary;
    }

    public int getType () {
        return type;
    }

    public ArrayList<LatLng> getCoordinateList () {
        return coordinateList;
    }



}
