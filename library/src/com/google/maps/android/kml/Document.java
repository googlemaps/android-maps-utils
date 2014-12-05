package com.google.maps.android.kml;

import android.content.Context;

import android.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;


public class Document {

    private XmlPullParser parser;
    private HashMap<String, Style> styles;
    private HashMap<String, Placemark> placemarks;

    private int LATITUDE = 0;
    private int LONGITUDE = 1;

    public Document (XmlPullParser parser) {
        this.parser = parser;
        this.styles = new HashMap<String, Style>();
        this.placemarks = new HashMap<String, Placemark>();
    }
    /**********************************
     Generates style values when a parser to a text is given.
     New styles with different features are created when a new ID is given
     **********************************/

    public void readKMLData() {
        XmlPullParser p = this.parser;
        String name;
        try {
            int eventType = p.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                name = p.getName();
                if(eventType == XmlPullParser.START_TAG) {
                    if (name.equals("Style")) {
                        Style style =  new Style();
                        style.setStyleID(p.getAttributeValue(null, "id"));
                        assignStyle(p, style);
                        this.styles.put(style.getStyleID(), style);
                    } else if (name.equals ("Placemark")) {
                        Placemark placemark = new Placemark();
                        assignPlacemark(p, placemark);
                        this.placemarks.put(placemark.getName(), placemark);
                    }
                }
                eventType = p.next();
            }
        } catch (Exception e) {

        }
    }

    /**********************************
     * Reads input from a parser, then assigns values to a placemark classed based on the name
     * of the tag for which the text is defined in
     *
     * @param  placemark    newly created placemark class
     * @param p             An XML parser which contains the input to be read
     **********************************/
    public void assignPlacemark(XmlPullParser p, Placemark placemark) {
        try {
            int eventType = p.getEventType();
            String name = p.getName();
            if(eventType == XmlPullParser.START_TAG) {
                if (name.equals("name")) {
                    placemark.setName(p.nextText());
                } else if (name.equals("styleUrl")) {
                    placemark.setStyleURL(p.nextText());
                } else if (name.equals("description")) {
                    placemark.setDescription(p.nextText());
                } else if (name.equals("phoneNumber")) {
                    placemark.setPhoneNumber(p.nextText());
                } else if (name.equals("address")) {
                    placemark.setAddress(p.nextText());
                } else if (name.equals("visibility")) {
                    placemark.setVisibility(p.nextText());
                } else if (name.equals("LineString") || name.equals("Point") || name.equals("Polygon")) {
                   Coordinate c = new Coordinate();
                   assignCoordinates(p, c, name);
                }
            }
            if (!(eventType == XmlPullParser.END_TAG && name.equals("Placemark"))) {
                p.next();
                assignPlacemark(p, placemark);
            }
        } catch (Exception e){

        }

    }

    /**********************************
     * Reads input from a parser
     *
     * @param c     Newly created coordinate class\
     * @param p     XML Pull Parser
     * @param closingTag    String value which represents the point at which to stop reading input
     **********************************/

    public void assignCoordinates (XmlPullParser p, Coordinate c, String closingTag) {
        try {
            int eventType = p.getEventType();
            String name = p.getName();
            if(eventType == XmlPullParser.START_TAG) {
               if (name.equals("coordinates")) {
                   c.setCoordinateList(p.nextText());
               }
            }
            if (!(eventType == XmlPullParser.END_TAG && name.equals(closingTag))) {
                p.next();
                assignCoordinates(p, c, closingTag);
            }
        } catch (Exception e){

        }
    }

    /**********************************
     * Reads input from a parser, then assigns values to a style class based on the start
     * tag for which the tag content is defined in.
     *
     * @param  style   Style class, newly created
     * @param p    An XML parser which contains the input to be read
     **********************************/
    public void assignStyle(XmlPullParser p, Style style) {
        try {
            int eventType = p.getEventType();
            String name = p.getName();
            if(eventType == XmlPullParser.START_TAG) {
                if (name.equals("color")) {
                    style.setLineColor(p.nextText());
                } else if (name.equals("width")) {
                    style.setLineWidth(Integer.parseInt(p.nextText()));
                } else if (name.equals("fill")) {
                    style.setPolyFillColor(p.nextText());
                } else if (name.equals("outline")) {
                    style.setOutline(Boolean.getBoolean(p.nextText()));
                } else if (name.equals("colorMode")) {
                    style.setColorMode(p.nextText());
                }
            }
            if (!(eventType == XmlPullParser.END_TAG && name.equals("Style"))) {
                p.next();
                assignStyle(p, style);
            }
        } catch (Exception e){

        }
    }


    /**********************************
     * Returns a hashmap of placemarks, for which the keys are the name of the placemark
     * and the values are a placemark class.
     **********************************/

    public HashMap<String, Placemark> getPlacemarks() {
        return this.placemarks;
    }

    /**********************************
     * Returns a hashmap of styles, for which the keys are the name of the style
     * and the values are a style class.
     **********************************/

    public HashMap<String, Style> getStyles() {
        return  this.styles;
    }
}
