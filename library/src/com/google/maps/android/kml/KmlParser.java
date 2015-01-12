package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by juliawong on 1/7/15.
 */
public class KmlParser {

    private final KmlStyleParser styleParser;

    private final KmlPlacemarkParser placemarkParser;

    private final XmlPullParser mParser;

    private final static String STYLE_START_TAG = "Style";

    private final static String STYLE_MAP_START_TAG = "StyleMap";

    private final static String PLACEMARK_START_TAG = "Placemark";


    /**
     * Creates a new KmlParser object
     *
     * @param parser parser containing the KML file to parse
     */
    public KmlParser(XmlPullParser parser) {
        mParser = parser;
        styleParser = new KmlStyleParser(parser);
        placemarkParser = new KmlPlacemarkParser(parser);
    }

    /**
     * Parses the KML file and stores the created KmlStyle and KmlPlacemark
     */
    public void parseKml() throws XmlPullParserException, IOException {
        int eventType = mParser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals(STYLE_START_TAG)) {
                    styleParser.createStyle();
                } else if (mParser.getName().equals(STYLE_MAP_START_TAG)) {
                    styleParser.createStyleMap();
                } else if (mParser.getName().equals(PLACEMARK_START_TAG)) {
                    placemarkParser.createPlacemark();
                }
            }
            eventType = mParser.next();
        }
    }

    public HashMap<String, KmlStyle> getStyles() {
        return styleParser.getStyles();
    }

    public ArrayList<KmlPlacemark> getPlacemarks() {
        return placemarkParser.getPlacemarks();
    }

    public  HashMap<String, String> getStyleMaps() { return styleParser.getStyleMaps(); }
}
