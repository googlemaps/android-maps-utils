package com.google.maps.android.kml;

import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by lavenderc on 12/2/14.
 *
 * Represents the defined styles in the KML document
 */
public class Style {

    private final PolylineOptions mPolylineOptions;

    private final PolygonOptions mPolygonOptions;

    private final static int POLYGON_TRANSPARENT_COLOR = 0x00000000;

    private final static int POLYGON_NO_OUTLINE_WIDTH = 0;

    private final static int HEXADECIMAL_COLOR_RADIX = 16;

    private boolean fill = true;

    private boolean outline = true;


    public Style() {

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
        while (!(eventType == XmlPullParser.END_TAG && p.getName().equals("Style"))) {
            if (eventType == XmlPullParser.START_TAG && p.getName().equals("LineStyle")) {
                parseLineStyle(p);
            }
            else if (eventType == XmlPullParser.START_TAG && p.getName().equals("PolyStyle")) {
                parsePolyStyle(p);
            }
            eventType = p.next();
        }

        checkFill();
        checkOutline();
    }

    /**
     * Adds style properties to the PolylineOptions object mPolylineOptions
     * @param p XMLPullParser reads all input for the LineStyle
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void parseLineStyle(XmlPullParser p) throws XmlPullParserException, IOException {
        String color;
        String width;
        int eventType = p.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && p.getName().equals("LineStyle"))) {
            // Assign relevant properties to mPolylineOptions
            if (eventType == XmlPullParser.START_TAG) {
                if (p.getName().equals("color")) {
                    color = p.nextText();
                    mPolylineOptions.color((int) Long.parseLong(color, HEXADECIMAL_COLOR_RADIX));
                    mPolygonOptions.strokeColor((int) Long.parseLong(color, HEXADECIMAL_COLOR_RADIX));
                }
                else if (p.getName().equals("colorMode")) {
                    // TODO: Implement a function to handle colorMode
                }
                else if (p.getName().equals("width")) {
                    width = p.nextText();
                    mPolylineOptions.width(Float.parseFloat(width));
                    mPolygonOptions.strokeWidth(Float.parseFloat(width));
                }
            }
            eventType = p.next();
        }
    }

    /**
     * Adds style properties to the PolygonOptions object mPolygonOptions
     * @param p XMLPullParser reads all input for the PolyStyle
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void parsePolyStyle(XmlPullParser p) throws XmlPullParserException, IOException {

        int eventType = p.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && p.getName().equals("PolyStyle"))) {
            // Assign relevant properties to mPolygonOptions
            if (eventType == XmlPullParser.START_TAG) {
                if (p.getName().equals("color")) {
                    mPolygonOptions.fillColor(
                            (int) Long.parseLong(p.nextText(), HEXADECIMAL_COLOR_RADIX));
                }
                else if (p.getName().equals("colorMode")) {
                    // TODO: Implement a function to handle colorMode
                }
                else if (p.getName().equals("fill")) {
                    fill = false;
                }
                else if (p.getName().equals("outline")) {
                    outline = false;
                }
            }
            eventType = p.next();
        }
    }

    /**
     * Checks if there is no fill for the Polygon and makes transparent
     */
    private void checkFill() {
        if (!fill) {
            mPolygonOptions.fillColor(POLYGON_TRANSPARENT_COLOR);
        }
    }

    /**
     * Checks if there is no outline for the Polygon and removes outline
     */
    private void checkOutline() {
        if (!outline) {
            mPolygonOptions.strokeWidth(POLYGON_NO_OUTLINE_WIDTH);
        }
    }

    /**
     * Gets a PolylineOptions object containing the property styles parsed from the KML file
     * Used for LineString
     * @return PolylineOptions object with defined options
     */
    public PolylineOptions getPolylineOptions() {
        return mPolylineOptions;
    }

    /**
     * Gets a PolygonOptions object containing the property styles parsed from the KML file
     * Used for LinearRing
     * @return PolygonOptions object with defined options
     */
    public PolygonOptions getPolygonOptions() {
        return mPolygonOptions;
    }
}
