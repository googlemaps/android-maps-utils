package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by lavenderc on 12/3/14.
 *
 * Represents a placemark which is either a point, linestring, polygon or multigeometry
 * Stores the properties about the placemark including coordinates
 */
public class Placemark {

    public static final int LATITUDE = 1;

    public static final int LONGITUDE = 0;

    private HashMap<String, String> mPlacemarkProperties;

    private ArrayList<Geometry> mMultigeometry;

    private Geometry mGeometry;

    private String mStyle;

    public static LatLng convertToLatLng(String[] coordinate) {
        Double latDouble = Double.parseDouble(coordinate[LATITUDE]);
        Double lonDouble = Double.parseDouble(coordinate[LONGITUDE]);
        return new LatLng(latDouble, lonDouble);
    }

    public String getStyle() {
        return mStyle;
    }

    public void setStyle(String style) {
        mStyle = style;
    }

    public void setProperties(String propertyName, String propertyValue) {
        if (mPlacemarkProperties == null) {
            mPlacemarkProperties = new HashMap<String, String>();
        }
        mPlacemarkProperties.put(propertyName, propertyValue);
    }

    public void setGeometry(String type, String text) {
        if (type.equals("Point")) {
            mGeometry = new Point();
            mGeometry.createCoordinates(text);
        } else if (type.equals("LineString")) {
            mGeometry = new LineString();
            mGeometry.createCoordinates(text);
        } else if (type.equals("Polygon")) {
            if (mGeometry == null) {
                mGeometry = new Polygon();
            }
            mGeometry.createCoordinates(text);
        }
    }

    public void setMultigeometry(String type, String text) {
        if (mMultigeometry == null) {
            mMultigeometry = new ArrayList<Geometry>();
        }

        if (type.equals("Point")) {
            Point point = new Point();
            point.createCoordinates(text);
            mMultigeometry.add(point);
        } else if (type.equals("LineString")) {
            LineString lineString = new LineString();
            lineString.createCoordinates(text);
            mMultigeometry.add(lineString);
        } else if (type.equals("Polygon")) {
            Polygon polygon = new Polygon();
            polygon.createCoordinates(text);
            mMultigeometry.add(polygon);
        }
    }

    public HashMap<String, String> getProperties() {
        return mPlacemarkProperties;
    }

    public Geometry getGeometry() {
        return mGeometry;
    }

    public ArrayList<Geometry> getMultigeometry() {
        return mMultigeometry;
    }

}
