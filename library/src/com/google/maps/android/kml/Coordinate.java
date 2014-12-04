package com.google.maps.android.kml;

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

    private LatLng coordinate;
    private ArrayList<LatLng> coordinateList;

    public Coordinate() {
        coordinateList = new ArrayList<LatLng>();
        type = UNINITIALIZED;
        boundary = UNINITIALIZED;
    }
    /**********************************
        @param bBoundary Integer value which corresponds to either polygon, line or point type.
        INNER_BOUNDARY = 0;
        OUTER_BOUNDARY = 1;
     **********************************/
    public void setBoundary (int bBoundary) {
        boundary = bBoundary;
    }

    /**********************************
        @param tType Integer value which corresponds to either polygon, line or point type.
        POLYGON_TYPE = 0;
        LINESTRING_TYPE = 1;
        POINT_TYPE = 2;
     **********************************/
    public void setType(int tType) {
        type = tType;
    }

    /**********************************
        Receives a list of coordinates from a string value, and assigns a latlng
        value to each pair of latitude and longitude points in a line, separated by the comma
        delimiter. The method ignores any coordinates which follow the second item in the array.
        @param text String input in the format:
                    <lat>,<lon>
                    <lat>,<lon>
                    <lat>,<lon>
     **********************************/
    public void setCoordinateList (String text) {
        String[] lines = text.split("\n");
        for (String point: lines) {
            String[] coordinate = point.split(",");
            if (coordinate.length > 2) {
                coordinateList.add(convertToLatLng(coordinate));
            }
        }
    }

    /**********************************
         Receives a pair of coordinate values which are separated by a comma and assigns a latlng
         value to it. The method then returns this latlng value.
         @return    lat     LatLng value
     **********************************/
    public LatLng convertToLatLng (String[] coordinate) {
        Double latDouble = Double.parseDouble(coordinate[LATITUDE]);
        Double lonDouble = Double.parseDouble(coordinate[LONGITUDE]);
        LatLng latLng = new LatLng(latDouble,lonDouble);
        return latLng;
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
