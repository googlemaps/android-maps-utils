package com.google.maps.android.kml;

import org.xmlpull.v1.XmlPullParser;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Document class allows for users to input their KML data and output it onto the map
 */
public class Document {

    private final XmlPullParser mParser;

    private HashMap<String, Style> mStyles;

    private ArrayList<Placemark> mPlacemarks;

    /**
     * Constructs a new Document object
     *
     * @param parser XmlPullParser loaded with the KML file
     */
    public Document(XmlPullParser parser) {
        this.mParser = parser;
        this.mStyles = new HashMap<String, Style>();
        this.mPlacemarks = new ArrayList<Placemark>();
    }

    /**
     * Generates style values when a mParser to a text is given.
     * New mStyles with different features are created when a new ID is given
     */
    public void readKMLData() {
        XmlPullParser p = this.mParser;
        String name;
        try {
            int eventType = p.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                name = p.getName();
                if (eventType == XmlPullParser.START_TAG) {
                    if (name.equals("Style")) {
                        Style style = new Style();
                        String styleUrl = p.getAttributeValue(null, "id");
                        style.styleProperties(p);
                        mStyles.put(styleUrl, style);
                    } else if (name.equals("Placemark")) {
                        Placemark placemark = new Placemark();
                        placemark.placemarkProperties(p);
                        this.mPlacemarks.add(placemark);
                    }
                }
                eventType = p.next();
            }
        } catch (Exception e) {
            Log.e("ERROR", e.toString());
        }
    }


    /**
     * Eventually we want this function to create all the markers / polygons and add them to the
     * map.
     * For now we are just printing out certain values.
     */

    public void printKMLData() {
        // Print out all the styles
        for (String s : mStyles.keySet()) {
            System.out.println(s);
        }

        for (Placemark p : mPlacemarks) {
            // Print style name and the related style object
            System.out.println(p.getValues("styleUrl"));
            System.out.println(mStyles.get(p.getValues("styleUrl")));
            for (Coordinate c : p.getLine()) {
                //System.out.println(c.getCoordinateList().toString());
            }
        }
    }
}
