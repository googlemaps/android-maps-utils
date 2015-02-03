package com.google.maps.android.kml;

import com.google.android.gms.maps.model.GroundOverlay;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Parses a given KML file into KmlStyle, KmlPlacemark, KmlGroundOverlay and KmlContainer objects
 */
/* package */ class KmlParser {

    private final static String STYLE = "Style";

    private final static String STYLE_MAP = "StyleMap";

    private final static String PLACEMARK = "Placemark";

    private final static String GROUND_OVERLAY = "GroundOverlay";

    private final static String CONTAINER_REGEX = "Folder|Document";

    private final XmlPullParser mParser;

    private final HashMap<KmlPlacemark, Object> mPlacemarks;

    private final ArrayList<KmlContainer> mContainers;

    private final HashMap<String, KmlStyle> mStyles;

    private final HashMap<String, String> mStyleMaps;

    private final HashMap<KmlGroundOverlay, GroundOverlay> mGroundOverlays;

    /**
     * Creates a new KmlParser object
     *
     * @param parser parser containing the KML file to parse
     */
    /* package */ KmlParser(XmlPullParser parser) {
        mParser = parser;
        mPlacemarks = new HashMap<KmlPlacemark, Object>();
        mContainers = new ArrayList<KmlContainer>();
        mStyles = new HashMap<String, KmlStyle>();
        mStyleMaps = new HashMap<String, String>();
        mGroundOverlays = new HashMap<KmlGroundOverlay, GroundOverlay>();
    }

    /**
     * Parses the KML file and stores the created KmlStyle and KmlPlacemark
     */
    /* package */ void parseKml() throws XmlPullParserException, IOException {
        int eventType = mParser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().matches(CONTAINER_REGEX)) {
                    mContainers.add(KmlContainerParser.createContainer(mParser));
                }
                if (mParser.getName().equals(STYLE)) {
                    KmlStyle style = KmlStyleParser.createStyle(mParser);
                    mStyles.put(style.getStyleId(), style);
                }
                if (mParser.getName().equals(STYLE_MAP)) {
                    mStyleMaps.putAll(KmlStyleParser.createStyleMap(mParser));
                }
                if (mParser.getName().equals(PLACEMARK)) {
                    mPlacemarks.put(KmlFeatureParser.createPlacemark(mParser), null);
                }
                if (mParser.getName().equals(GROUND_OVERLAY)) {
                    mGroundOverlays.put(KmlFeatureParser.createGroundOverlay(mParser), null);
                }
            }
            eventType = mParser.next();
        }
    }

    /**
     * @return List of styles created by the parser
     */
    /* package */ HashMap<String, KmlStyle> getStyles() {
        //TODO: Need to put an empty new style, can probably be put somewhere better
        mStyles.put(null, new KmlStyle());
        return mStyles;
    }

    /**
     * @return List of basic_placemark object created by the parser
     */
    /* package */ HashMap<KmlPlacemark, Object> getPlacemarks() {
        return mPlacemarks;
    }

    /**
     * @return A list of stylemaps created by the parser
     */
    /* package */ HashMap<String, String> getStyleMaps() {
        return mStyleMaps;
    }

    /**
     * @return List of folder objects created by the parser
     */
    /* package */ ArrayList<KmlContainer> getContainers() {
        return mContainers;
    }

    /**
     * @return hashmap of ground overlays created by the parser
     */
    /* package */ HashMap<KmlGroundOverlay, GroundOverlay> getGroundOverlays() {
        return mGroundOverlays;
    }
}
