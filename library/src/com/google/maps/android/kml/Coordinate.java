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
         Recieves an integer value, 0 or 1, which determines whether the line is an inner boundary
         or an outer boundary for polygon coordinates.
     **********************************/
    public void setBoundary (int bBoundary) {
        boundary = bBoundary;
    }

    /**********************************
         Recieves an integer value, 0, 1 or 2, which determines whether the coordinates are a polygon,
         line or point
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

    }

    /**********************************
         Receives a pair of coordinate values which are separated by a comma and assigns a latlng
         value to it. The method then returns this latlng value.
         @param     pair    String input in the format <lat>,<lon>
         @return    lat     LatLng value
     **********************************/
    public LatLng convertToLatLng (String pair) {
        return null;
    }

    public int getBoundary() {
        return boundary;
    }

    public int getType () {
        return type;
    }

    public ArrayList<LatLng> getCoordinateList () {
        return null;
    }



}
