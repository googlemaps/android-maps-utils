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


    public void setBoundary (int boundary) {
    }

    public void setType(int type) {
    }

    public int getBoundary() {
        return UNINITIALIZED;
    }

    public int getType () {
        return UNINITIALIZED;
    }

    public ArrayList<LatLng> getCoordinateList () {
        return null;
    }

    /**********************************
        Receives a list of coordinates from a string value, and assigns a latlng
        value to each pair of latitude and longitude points in a line, separated by the comma
        delimiter. The method ignores any coordinates which follow the second item in the array.
     **********************************/

    public void setCoordinateList (String text) {
    }

    /**********************************
     Receives a pair of coordinate values which are separated by a comma and assigns a latlng
     value to it. The method then returns this latlng value.
     **********************************/

    public LatLng convertToLatLng (String pair) {
        return null;
    }



}
