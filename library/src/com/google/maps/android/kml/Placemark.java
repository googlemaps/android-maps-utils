package com.google.maps.android.kml;

import org.xmlpull.v1.XmlPullParser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by lavenderc on 12/3/14.
 */
public class Placemark {

    private ArrayList<Coordinate> line;

    private HashMap<String, String> values;

    public Placemark() {
        line = null;
        values = new HashMap<String, String>();
    }


    public void setValues (String key, String value) {
        values.put(key, value);
    }

    public String getValues (String key) {
        return values.get(key);
    }

    public void setLine(ArrayList<Coordinate> coordinates) {
        line = coordinates;
    }
    public ArrayList<Coordinate> getLine () {
        return line;
    }



}
