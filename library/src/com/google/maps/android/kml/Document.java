package com.google.maps.android.kml;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;

/**
 * Document class allows for users to input their KML data and output it onto the map
 */
public class Document {

    private XmlPullParser parser;

    private ArrayList<Style> styles;

    private ArrayList<Placemark> placemarks;

    /**
     * Constructs a new Document object
     *
     * @param parser XmlPullParser loaded with the KML file
     */
    public Document(XmlPullParser parser) {
        this.parser = parser;
        this.styles = new ArrayList<Style>();
        this.placemarks = new ArrayList<Placemark>();
    }

    /**
     * Generates style values when a parser to a text is given.
     * New styles with different features are created when a new ID is given
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


    /**
     * Eventually we want this function to create all the markers / polygons and add them to the
     * map.
     * For now we are just printing out certain values.
     */

    public void printKMLData() {
        for (Style s : styles) {
            System.out.println(s.getValues("styleID") + " " + s.getValues("color"));
        }

        for (Placemark p : placemarks) {
            System.out.println(p.getValues("name"));
            for (Coordinate c : p.getLine()) {
                // This will only work once coordinate class can parse in coords
                System.out.println(c.getCoordinateList().size());
            }
        }
    }
}
