package com.google.maps.android.kml;

import android.content.Context;

import android.R;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

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

    public Document(XmlPullParser parser) {
        this.parser = parser;
        this.styles = new ArrayList<Style>();
        this.placemarks = new ArrayList<Placemark>();
    }

    /**
     * *******************************
     * Generates style values when a parser to a text is given.
     * New styles with different features are created when a new ID is given
     * ********************************
     */

    public void readKMLData() {
        XmlPullParser p = this.parser;
        String name;
        try {
            int eventType = p.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                name = p.getName();
                if (eventType == XmlPullParser.START_TAG) {
                    if (name.equals("Style")) {
                        Style style = new Style();
                        style.setValues("styleID", p.getAttributeValue(null, "id"));
                        style.styleProperties(p);
                        this.styles.add(style);
                    } else if (name.equals("Placemark")) {
                        Placemark placemark = new Placemark();
                        placemark.placemarkProperties(p);
                        this.placemarks.add(placemark);
                    }
                }
                eventType = p.next();
            }
        } catch (Exception e) {

        }
    }


    /***
     * Eventually we want this function to create all the markers / polygons and add them to the map.
     * For now we are just printing out certain values.
     */

    public void printKMLData() {
        for (Style s: styles) {
            System.out.println( s.getValues("styleID") + " " + s.getValues("color"));
        }
        for (Placemark p: placemarks) {
            System.out.println(p.getValues("name"));
            for (Coordinate c: p.getLine()) {
                System.out.println(c.getCoordinateList().size());
            }
        }
    }
}
