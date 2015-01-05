package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by lavenderc on 12/3/14.
 *
 * Represents a placemark which is either a point, linestring or polygon
 * Stores the properties about the placemark including coordinates
 */
public class Placemark {

    private HashMap<String, String> mPlacemarkProperties;

    private ArrayList<Geometry> mMultigeometry;

    //TODO: One Geometry Object

    private Polygon mPolygon;

    private LineString mLineString;

    private Point mPoint;

    public static final int LATITUDE = 1;

    public static final int LONGITUDE = 0;


    public void setStyle(XmlPullParser parser, String name) throws XmlPullParserException, IOException {
        if (parser.getAttributeCount() != 0) {
            setProperties(name, parser.getAttributeValue(null, "id"));
        } else {
            setProperties(name, parser.nextText());
        }
    }

    public void setProperties (String propertyName, String propertyValue) {
        if (mPlacemarkProperties == null) mPlacemarkProperties = new HashMap<String, String>();
        mPlacemarkProperties.put(propertyName, propertyValue);
    }

    public void setGeometry(String type, String text) {
        if (type.equals("Point")) {
            mPoint = new Point();
            mPoint.createCoordinates(text);
        } else if (type.equals("LineString")) {
            mLineString = new LineString();
            mLineString.createCoordinates(text);
        } else if (type.equals("Polygon")) {
            if (mPolygon == null) mPolygon = new Polygon();
            mPolygon.createCoordinates(text);
        }
    }

    public void setMultigeometry(String type, String text) {

        if (mMultigeometry == null) mMultigeometry = new ArrayList<Geometry>();

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
            polygon .createCoordinates(text);
            mMultigeometry.add(polygon);
        }
    }

    public HashMap<String, String> getProperties() {
        return mPlacemarkProperties;
    }

    public Polygon getPolygon() {
        return mPolygon;
    }

    public LineString getPolyline() {
        return mLineString;
    }

    public Point getPoint() {
        return mPoint;
    }

    public ArrayList<Geometry> getMultigeometry() {
        return mMultigeometry;
    }

    public static LatLng convertToLatLng(String[] coordinate) {
        Double latDouble = Double.parseDouble(coordinate[LATITUDE]);
        Double lonDouble = Double.parseDouble(coordinate[LONGITUDE]);
        return new LatLng(latDouble, lonDouble);
    }

}
