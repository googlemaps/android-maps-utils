package com.google.maps.android.kml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by lavenderch on 1/14/15.
 */
public class KmlContainerParser {

    private static final String PROPERTY_TAG_REGEX = "name|description|visibility";

    private ArrayList<KmlContainer> mContainers;

    private XmlPullParser mParser;

    private KmlFeatureParser placemarkParser;

    private KmlStyleParser styleParser;

    private final static String PLACEMARK_START_TAG = "Placemark";

    private final static String STYLE_START_TAG = "Style";

    private final static String FOLDER_START_TAG = "Folder";

    public KmlContainerParser(XmlPullParser parser) {
        mParser = parser;
        mContainers = new  ArrayList<KmlContainer>();
    }

    public void createFolder() throws XmlPullParserException, IOException {
        HashMap<String, String> properties = new HashMap<String, String>();
        mParser.next();
        int eventType = mParser.getEventType();
        placemarkParser = new KmlFeatureParser(mParser);
        styleParser = new KmlStyleParser(mParser);
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("Folder"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().matches(PROPERTY_TAG_REGEX)) {
                    properties.put(mParser.getName(), mParser.nextText());
                } if (mParser.getName().equals(STYLE_START_TAG)) {
                    styleParser.createStyle();
                }else if (mParser.getName().equals(PLACEMARK_START_TAG)) {
                    placemarkParser.createPlacemark();
                } else if (mParser.getName().equals(FOLDER_START_TAG)) {
                    createFolder();
                }
            }
            eventType = mParser.next();
        }
        mContainers.add(setContainer());
    }

    private KmlContainer setContainer () {
        KmlContainer container = new KmlContainer();
        for (KMLFeature placemark : placemarkParser.getPlacemarks().keySet()) {
            container.addPlacemarks(placemark, null);
        }
        container.setStyles(styleParser.getStyles());
        return container;
    }

    public  ArrayList<KmlContainer> getContainers() {
        return mContainers;
    }


}
