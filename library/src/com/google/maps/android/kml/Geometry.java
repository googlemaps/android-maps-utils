package com.google.maps.android.kml;

import android.util.Xml;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by lavenderch on 12/29/14.
 */
public interface Geometry {

    public static final int LATITUDE = 0;

    public static final int LONGITUDE = 1;

    public static final int INNER_BOUNDARY = 0;

    public static final int OUTER_BOUNDARY = 1;

    public LatLng latLng = null;

    public void parseGeometry (XmlPullParser parser) throws XmlPullParserException, IOException;

    public void setGeometry (String text);

    public Object getGeometry();

    public LatLng convertToLatLng(String[] text);

}
