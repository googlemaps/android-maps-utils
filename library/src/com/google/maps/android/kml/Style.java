package com.google.maps.android.kml;

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

    private final HashMap<String, String> mPolyStyle;

    private final HashMap<String, String> mLineStyle;

    private PolylineOptions mPolylineOptions;

    private PolygonOptions mPolygonOptions;


    public Style() {
        mPolyStyle = new HashMap<String, String>();
        mLineStyle = new HashMap<String, String>();
        mPolylineOptions = new PolylineOptions();
        mPolygonOptions = new PolygonOptions();

    }

    /**
     * Takes in a XMLPullParser containing properties for a parser and saves relevant properties
     *
     * @param p XMLPullParser reads input from designated source
     */
    public void styleProperties(XmlPullParser p) throws XmlPullParserException, IOException {
        int eventType = p.getEventType();
        // Iterate through document until closing style tag is reached
        while (!(eventType == XmlPullParser.END_TAG && p.getName().equals("Style"))) {
            // Parse LineStyle properties and place into mLineStyle
            if (eventType == XmlPullParser.START_TAG && p.getName().equals("LineStyle")) {
                // Iterate over the property tags
                eventType = p.next();
                while (!(eventType == XmlPullParser.END_TAG && p.getName().equals("LineStyle"))) {
                    // Check if it is a valid property tag of LineStyle
                    if (eventType == XmlPullParser.START_TAG &&
                            p.getName().matches("color|colorMode|width")) {
                        mLineStyle.put(p.getName(), p.nextText());
                    }
                    eventType = p.next();
                }
            }
            // Parse PolyStyle properties and place into mPolyStyle
            else if (eventType == XmlPullParser.START_TAG && p.getName().equals("PolyStyle")) {
                // Iterate over the property tags
                eventType = p.next();
                while (!(eventType == XmlPullParser.END_TAG && p.getName().equals("PolyStyle"))) {
                    // Check if it is a valid property tag of PolyStyle
                    if (eventType == XmlPullParser.START_TAG &&
                            p.getName().matches("color|colorMode|fill|outline")) {
                        mPolyStyle.put(p.getName(), p.nextText());
                    }
                    eventType = p.next();
                }
            }
            eventType = p.next();
        }
    }

    /**
     * Gets a PolylineOptions object containing the property styles parsed from the KML file
     * Used for LineString
     * @return PolylineOptions object with defined options
     */
    public PolylineOptions getPolylineOptions() {
        if (mLineStyle.containsKey("color"))
            mPolylineOptions.color(Integer.parseInt(mLineStyle.get("color")));
        // TODO: implement colorMode
        //if (mLineStyle.containsKey("colorMode"))
        if (mLineStyle.containsKey("width"))
            mPolylineOptions.width(Float.parseFloat(mLineStyle.get("width")));
        return mPolylineOptions;
    }

    /**
     * Gets a PolygonOptions object containing the property styles parsed from the KML file
     * Used for LinearRing
     * @return PolygonOptions object with defined options
     */
    public PolygonOptions getPolygonOptions() {
        if (mPolyStyle.containsKey("color"))
            mPolygonOptions.fillColor(Integer.parseInt(mPolyStyle.get("color")));
        // TODO: implement colorMode
        //if (mPolyStyle.containsKey("colorMode"))
        if (mPolyStyle.containsKey("outline")) {
            if (mLineStyle.containsKey("color"))
                mPolygonOptions.strokeColor(Integer.parseInt(mLineStyle.get("color")));
            if (mLineStyle.containsKey("width"))
                mPolygonOptions.strokeWidth(Integer.parseInt(mLineStyle.get("width")));
        }
        return mPolygonOptions;
    }

    /**
     * Used to check if there are no elements in both style hash maps
     *
     * @return true if both style hash maps are empty, false otherwise
     */
    public boolean isEmpty() {
        return mPolyStyle.isEmpty() && mLineStyle.isEmpty();
    }
}
