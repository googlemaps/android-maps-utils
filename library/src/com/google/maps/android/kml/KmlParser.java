package com.google.maps.android.kml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by juliawong on 1/7/15.
 */
public class KmlParser {

    private KmlStyleParser styleParser;

    private KmlPlacemarkParser placemarkParser;

    private KmlFolderParser containerParser;

    private  XmlPullParser mParser;

    private HashMap<KmlPlacemark, Object> mPlacemarks;

    private final static String STYLE_START_TAG = "Style";

    private final static String STYLE_MAP_START_TAG = "StyleMap";

    private final static String PLACEMARK_START_TAG = "Placemark";

    private final static String CONTAINER_START_TAG = "Folder";

    private ArrayList<KmlFolder> mFolders;


    /**
     * Creates a new KmlParser object
     *
     * @param parser parser containing the KML file to parse
     */
    public KmlParser(XmlPullParser parser) {
        mParser = parser;
        mPlacemarks = new HashMap<KmlPlacemark, Object>();
        mFolders = new ArrayList<KmlFolder>();
        styleParser = new KmlStyleParser(mParser);
        placemarkParser = new KmlPlacemarkParser(mParser);
        containerParser= new KmlFolderParser(mParser);
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
                } if (mParser.getName().equals(STYLE_MAP_START_TAG)) {
                    styleParser.createStyleMap();
                } if (mParser.getName().equals(PLACEMARK_START_TAG)) {
                    placemarkParser.createPlacemark();
                    mPlacemarks.put(placemarkParser.getPlacemark(), null);
                } if (mParser.getName().equals(CONTAINER_START_TAG)) {
                    containerParser.createContainer();
                    mFolders.add(containerParser.getContainer());
                }
            }
            eventType = mParser.next();
        }
    }


    public HashMap<String, KmlStyle> getStyles() {
        return styleParser.getStyles();
    }

    public HashMap<KmlPlacemark, Object> getPlacemarks() {
        return mPlacemarks;
    }

    public  HashMap<String, String> getStyleMaps() { return styleParser.getStyleMaps(); }

    public ArrayList<KmlFolder> getFolders() {
        return mFolders;
    }
}
