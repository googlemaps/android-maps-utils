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

    private KmlFeatureParser placemarkParser;

    private KmlContainerParser containerParser;

    private  XmlPullParser mParser;

    private HashMap<KmlPlacemark, Object> mPlacemarks;

    private final static String STYLE_START_TAG = "Style";

    private final static String STYLE_MAP_START_TAG = "StyleMap";

    private final static String PLACEMARK_START_TAG = "Placemark";

    private final static String GROUND_OVERLAY_START_TAG = "GroundOverlay";

    private final static String CONTAINER_START_TAG_REGEX = "Folder|Document";

    private ArrayList<KmlContainerInterface> mFolders;

    private HashMap<String, KmlStyle> mStyles;

    private ArrayList<KmlGroundOverlay> mGroundOverlays;


    /**
     * Creates a new KmlParser object
     *
     * @param parser parser containing the KML file to parse
     */
    public KmlParser(XmlPullParser parser) {
        mParser = parser;
        mPlacemarks = new HashMap<KmlPlacemark, Object>();
        mFolders = new ArrayList<KmlContainerInterface>();
        mStyles = new HashMap<String, KmlStyle>();
        styleParser = new KmlStyleParser(mParser);
        placemarkParser = new KmlFeatureParser(mParser);
        containerParser= new KmlContainerParser(mParser);
        mGroundOverlays = new ArrayList<KmlGroundOverlay>();
    }

    /**
     * Parses the KML file and stores the created KmlStyle and KmlPlacemark
     */
    public void parseKml() throws XmlPullParserException, IOException {
        int eventType = mParser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().matches(CONTAINER_START_TAG_REGEX)) {
                    containerParser.createContainer();
                    mFolders.add(containerParser.getContainer());
                }if (mParser.getName().equals(STYLE_START_TAG)) {
                    styleParser.createStyle();
                    mStyles.put(styleParser.getStyle().getStyleId(), styleParser.getStyle());
                } if (mParser.getName().equals(STYLE_MAP_START_TAG)) {
                    styleParser.createStyleMap();
                } if (mParser.getName().equals(PLACEMARK_START_TAG)) {
                    placemarkParser.createPlacemark();
                    if (placemarkParser != null) mPlacemarks.put(placemarkParser.getPlacemark(), null);
                } if (mParser.getName().equals(GROUND_OVERLAY_START_TAG)) {
                    placemarkParser.createGroundOverlay();
                    mGroundOverlays.add(placemarkParser.getGroundOverlay());
                }
            }
            eventType = mParser.next();
        }
    }


    public HashMap<String, KmlStyle> getStyles() {
        //TODO: Need to put an empty new style, can probably be put somewhere better
        mStyles.put(null, new KmlStyle());
        return mStyles;
    }

    public HashMap<KmlPlacemark, Object> getPlacemarks() {
        return mPlacemarks;
    }

    public  HashMap<String, String> getStyleMaps() { return styleParser.getStyleMaps(); }

    public ArrayList<KmlContainerInterface> getFolders() {
        return mFolders;
    }

    public ArrayList<KmlGroundOverlay> getGroundOverlays() { return mGroundOverlays; }
}
