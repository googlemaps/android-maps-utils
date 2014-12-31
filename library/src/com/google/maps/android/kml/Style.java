package com.google.maps.android.kml;

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

    private boolean fill = true;

    private boolean outline = true;


    public Style() {
        mPolylineOptions = new HashMap<String, String>();
        mPolygonOptions = new HashMap<String, String>();
    }

    /**
     * Takes in a XMLPullParser containing properties for a parser and saves relevant properties
     *
     * @param p XMLPullParser reads input from designated source
     */
    public void styleProperties(XmlPullParser p) throws XmlPullParserException, IOException {
        int eventType = p.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && p.getName().equals("Style"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (eventType == XmlPullParser.START_TAG && p.getName().equals("LineStyle")) {
                    parseLineStyle(p);
                } else if (eventType == XmlPullParser.START_TAG && p.getName().equals("PolyStyle")) {
                    parsePolyStyle(p);
                }

                assignStyleProperties(p);
            }
            eventType = p.next();
        }
       checkStyleSettings();
    }

    private void parseLineStyle (XmlPullParser p) throws XmlPullParserException, IOException {

        String color;
        String width;
        int eventType = p.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && p.getName().equals("LineStyle"))) {
            // Assign relevant properties to mPolylineOptions
            if (eventType == XmlPullParser.START_TAG) {
                if (p.getName().equals("color")) {
                    color = p.nextText();
                    color = "#" + color;
                    mPolylineOptions.put("color", color);
                    mPolygonOptions.put("strokeColor", color);

                } else if (p.getName().equals("colorMode")) {
                    // TODO: Implement a function to handle colorMode
                } else if (p.getName().equals("width")) {
                    width = p.nextText();
                    mPolylineOptions.put("width", width);
                    mPolygonOptions.put("strokeWidth", width);
                }
            }
            eventType = p.next();
        }


    }

    private void parsePolyStyle (XmlPullParser p)  throws XmlPullParserException, IOException {
        int eventType = p.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && p.getName().equals("PolyStyle"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (p.getName().equals("color")) {
                    String color = p.nextText();
                    color = "#" + color;
                    mPolygonOptions.put("fillColor", color);
                } else if (p.getName().equals("colorMode")) {
                    // TODO: Implement a function to handle colorMode
                } else if (p.getName().equals("fill")) {
                    fill = false;
                } else if (p.getName().equals("outline")) {
                    outline = false;
                }
            }
            eventType = p.next();
        }
    }



    private void assignStyleProperties(XmlPullParser p) throws XmlPullParserException, IOException {
        if (p.getName().equals("color")) {
            String color = p.nextText();
            String subcolor = "#" + color.substring(0, 6);
            mPolylineOptions.put("color", subcolor);
            mPolygonOptions.put("color", subcolor);
        } else if (p.getName().equals("width")) {
            String width = p.nextText();
            mPolylineOptions.put("width", width);
            mPolygonOptions.put("width", width);
        } else if (p.getName().equals("fill")) {
            fill = false;
        } else if (p.getName().equals("outline")) {
            outline = false;
        }
    }


    /**
     * Checks if there is no outline for the Polygon and removes outline
     * Checks if there is no fill for the Polygon and makes transparent
     */
    private void checkStyleSettings() {
        if (!outline) mPolygonOptions.put("strokeWidth", "none");
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
