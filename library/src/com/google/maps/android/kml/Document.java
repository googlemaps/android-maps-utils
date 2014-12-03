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
    private String name;



    public Document (XmlPullParser parser) {
        this.parser = parser;
        this.styles = new HashMap<String, Style>();
        this.placemarks = new HashMap<String, Placemark>();
    }


    public void setPlacemark() {

    }

    public void setStyles() {
        String currentStyle = "";
        try {
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                name = parser.getName();
                if(eventType == XmlPullParser.START_TAG) {
                    if (name.equals("Style")) {
                        currentStyle = parser.getAttributeValue(null, "id");
                        this.styles.put(currentStyle, new Style( parser.getAttributeValue(null, "id")));
                    } else if (name.equals("color")) {
                        this.styles.get(currentStyle).setLineColor(parser.nextText());
                    } else if (name.equals("width")) {
                        this.styles.get(currentStyle).setLineWidth(Integer.parseInt(parser.nextText()));
                    } else if (name.equals("fill")) {
                        this.styles.get(currentStyle).setPolyFillColor(parser.nextText());
                    } else if (name.equals("outline")) {
                        this.styles.get(currentStyle).setOutline(Boolean.getBoolean(parser.nextText()));
                    } else if (name.equals("colorMode")) {
                        this.styles.get(currentStyle).setColorMode(parser.nextText());
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {

        }
    }

    public HashMap<String, Placemark> getPlacemarks() {
        return this.placemarks;
    }

    public HashMap<String, Style> getStyles() {
        return  this.styles;
    }
}
