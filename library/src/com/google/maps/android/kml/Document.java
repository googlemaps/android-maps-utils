package com.google.maps.android.kml;

import android.content.Context;

import android.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;


public class Document {

    private XmlPullParser parser;
    private ArrayList<Style> styles;
    private ArrayList<Placemark> placemarks;

    private int POLYGON_TYPE = 0;
    private int LINESTRING_TYPE = 1;
    private int POINT_TYPE = 2;

    private int INNER_BOUNDARY = 0;
    private int OUTER_BOUNDARY = 1;

    public Document (XmlPullParser parser) {
        this.parser = parser;
        this.styles = new ArrayList<Style>();
        this.placemarks = new ArrayList<Placemark>();
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
                        style.setValues("styleID", p.getAttributeValue(null, "id"));
                        assignStyle(p, style);
                        this.styles.add(style);
                    } else if (name.equals ("Placemark")) {
                        Placemark placemark = new Placemark();
                        ArrayList<Coordinate> c = new ArrayList<Coordinate>();
                        assignPlacemark(p, placemark, c);
                        this.placemarks.add(placemark);
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
     * @param co            An arraylist of coordinates
     **********************************/
    public void assignPlacemark(XmlPullParser p, Placemark placemark, ArrayList<Coordinate> co) {
        try {
            int eventType = p.getEventType();
            String name = p.getName();
            if(eventType == XmlPullParser.START_TAG) {
                if (name.equalsIgnoreCase("NAME") || name.equalsIgnoreCase("STYLEURL") ||
                        name.equalsIgnoreCase("DESCRIPTION") || name.equalsIgnoreCase("PHONENUMBER") ||
                        name.equalsIgnoreCase("ADDRESS") || name.equalsIgnoreCase("VISIBILITY")) {
                    placemark.setValues(name, p.nextText());

                } if (name.equals("LineString")) {
                    Coordinate c = new Coordinate();
                    c.setType(LINESTRING_TYPE);
                    assignCoordinates(p, c, placemark, co);
                } else if (name.equals("Point")) {
                    Coordinate c = new Coordinate();
                    c.setType(POINT_TYPE);
                    assignCoordinates(p, c, placemark,co);
                } else if (name.equals("Polygon")) {
                    Coordinate c = new Coordinate();
                    c.setType(POLYGON_TYPE);
                    assignPolygon(p, c, placemark, co, name);
                }
            }
            if (!(eventType == XmlPullParser.END_TAG && name.equals("Placemark"))) {
                p.next();
                assignPlacemark(p, placemark, co);
            }
        } catch (Exception e){

        }

    }

    public void assignPolygon (XmlPullParser p, Coordinate c,
    Placemark placemark, ArrayList<Coordinate> coordinates, String closingTag) {

        try {
            int eventType = p.getEventType();
            String name = p.getName();
            if(eventType == XmlPullParser.START_TAG) {
                if (name.equals("outerBoundaryIs")) {
                    c = new Coordinate();
                    c.setType(POLYGON_TYPE);
                    c.setBoundary(OUTER_BOUNDARY);
                } else if (name.equals("innerBoundaryIs")) {
                    c = new Coordinate();
                    c.setType(POLYGON_TYPE);
                    c.setBoundary(INNER_BOUNDARY);
                } else if (name.equals("coordinates")) {
                    assignCoordinates(p, c, placemark, coordinates);
                }
            }
            if (!(eventType == XmlPullParser.END_TAG && name.equals(closingTag))) {
                p.next();
                assignPolygon(p, c, placemark, coordinates, closingTag);
            }
        } catch (Exception e){

        }
    }




    /**********************************
     * Reads input from a parser
     *
     * @param c     Newly created coordinate class\
     * @param p     XML Pull Parser
     *
     **********************************/

    public void assignCoordinates (XmlPullParser p, Coordinate c,
        Placemark placemark, ArrayList<Coordinate> coordinates) {

        try {
            int eventType = p.getEventType();
            String name = p.getName();
            if(eventType == XmlPullParser.START_TAG) {
               if (name.equals("coordinates")) {

                   c.setCoordinateList(p.nextText());
               }
            } else if (!(eventType == XmlPullParser.END_TAG)) {
                p.next();
                assignCoordinates(p, c, placemark, coordinates);
            }
                coordinates.add(c);
                placemark.setLine(coordinates);

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
            if(eventType == XmlPullParser.START_TAG) {
                style.setValues(p.getName(), p.nextText());
            } else if (!(eventType == XmlPullParser.END_TAG && p.getName().equals("Style"))) {
                p.next();
                assignStyle(p, style);
            }
        } catch (Exception e){

        }
    }

    public ArrayList<Placemark> getPlacemarks() {
        return this.placemarks;
    }
    public ArrayList<Style> getStyles() {
        return this.styles;
    }
}
