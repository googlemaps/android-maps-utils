package com.google.maps.android.kml;

import android.util.Xml;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by lavenderc on 12/2/14.
 *
 * Represents the defined styles in the KML document
 */
public class Style {

    private final  HashMap<String, String> mPolylineOptions;

    private final HashMap<String, String> mPolygonOptions;

    private boolean fill;

    private boolean outline;

    public Style() {
        mPolylineOptions = new HashMap<String, String>();
        mPolygonOptions = new HashMap<String, String>();
        outline = true;
        fill = true;
    }

    public void setOutlineColor (String color) {
        mPolylineOptions.put("color", "#" + color);
        mPolygonOptions.put("strokeColor", color);
    }

    public void setWidth (String width) {
        mPolylineOptions.put("width", width);
        mPolygonOptions.put("strokeWidth", width);
    }


    public void setOutline (boolean value) {
        if (!outline) mPolygonOptions.put("strokeWidth", "none");
    }

    public void setFill (boolean value) {
        if (!fill) mPolygonOptions.put("fillColor", "none");
    }

    /**
     * Gets a PolylineOptions object containing the property styles parsed from the KML file
     * Used for LineString
     *
     * @return PolylineOptions object with defined options
     */
    public HashMap<String, String> getPolylineOptions() {
        return mPolylineOptions;
    }

    /**
     * Gets a PolygonOptions object containing the property styles parsed from the KML file
     * Used for LinearRing
     *
     * @return PolygonOptions object with defined options
     */
    public HashMap<String, String> getPolygonOptions() {
        return mPolygonOptions;
    }
}
