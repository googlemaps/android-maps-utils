package com.google.maps.android.kml;

import com.google.android.gms.maps.GoogleMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Document class allows for users to input their KML data and output it onto the map
 */
public class Document {

    private final XmlPullParser mParser;

    private final HashMap<String, Style> mStyles;

    private final ArrayList<Placemark> mPlacemarks;

    private final GoogleMap mMap;

    /**
     * Constructs a new Document object
     * @param map Map object
     * @param parser XmlPullParser loaded with the KML file
     */
    public Document(GoogleMap map, XmlPullParser parser) {
        this.mParser = parser;
        this.mMap = map;
        this.mStyles = new HashMap<String, Style>();
        this.mPlacemarks = new ArrayList<Placemark>();
    }

    /**
     *
     * @param map Map object
     * @param resourceId Raw resource KML file
     * @param applicationContext Application context object
     * @throws XmlPullParserException
     */
    public Document(GoogleMap map, int resourceId, Context applicationContext)
            throws XmlPullParserException {
        this.mMap = map;
        this.mStyles = new HashMap<String, Style>();
        this.mPlacemarks = new ArrayList<Placemark>();
        InputStream stream = applicationContext.getResources().openRawResource(resourceId);

        this.mParser = createXmlParser(stream);
    }

    /**
     * Creates a new XmlPullParser object
     * @param stream Character input stream representing the KML file
     * @return XmlPullParser object to allow for the KML document to be parsed
     */
    private XmlPullParser createXmlParser (InputStream stream) {
        XmlPullParserFactory factory = null;
        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(stream, null);
            return parser;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Generates style values when a mParser to a text is given.
     * New mStyles with different features are created when a new ID is given
     */
    public void readKMLData() {
        XmlPullParser p = this.mParser;
        String name;
        try {
            // Check if this is the start of the document
            p.require(XmlPullParser.START_DOCUMENT, null, null);
            p.next();
            // Check for a kml tag
            p.require(XmlPullParser.START_TAG, null, "kml");
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
            // Check for an ending kml tag and the end of the document
            p.require(XmlPullParser.END_DOCUMENT, null, null);
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

//        for (Placemark p : mPlacemarks) {
//            // Print style name and the related style object
//            // Take into consideration that styleName may be null or the styleUrl you try to fetch
//            // may not exist :o
//            String styleName = p.getValue("styleUrl");
//            if (styleName != null && styleName.startsWith("#")) {
//                styleName = styleName.substring(1);
//                if (mStyles.containsKey(styleName))
//                // Print style hashmap
//                {
//                    Log.i(p.getValue("name"), mStyles.get(styleName).toString());
//                } else
//                // Print style name
//                {
//                    Log.i(p.getValue("name"), styleName);
//                }
//            } else {
//                Log.i(p.getValue("name"), "default style");
//            }
//
//            Log.i("Placemark line array", Integer.toString(p.getLine().size()));
//            for (Coordinate c : p.getLine()) {
//                System.out.println(c.getCoordinateList().toString());
//            }
//        }
    }

    // TODO: Implement this function
    public void removeKMLData() {
        for (Placemark p : mPlacemarks) {
            // Call remove on each object
        }
    }
}
