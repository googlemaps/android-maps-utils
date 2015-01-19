package com.google.maps.android.kml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by lavenderch on 1/14/15.
 */
public class KmlContainerParser {

    private static final String PROPERTY_TAG_REGEX = "name|description|visibility|open";

    private KmlContainer mContainer;

    private XmlPullParser mParser;

    private final static String PLACEMARK_START_TAG = "Placemark";

    private final static String STYLE_START_TAG = "Style";

    private final static String FOLDER_START_TAG = "Folder";

    private final static String CONTAINER_START_TAG_REGEX = "Folder|Document";

    private final static String STYLE_MAP_START_TAG = "StyleMap";

    public KmlContainerParser(XmlPullParser parser) {
        mParser = parser;
        mContainer = null;
    }

    /**
     * Creates a new folder and adds this to an ArrayList of folders
     */

    public void createContainer() throws XmlPullParserException, IOException {
        KmlContainer folder = new KmlContainer();
        assignFolderProperties(folder);
        mContainer = folder;
    }

    /**
     * Takes a parser and assigns variables to a Folder instances
     * @param kmlFolder Folder to assign variables to
     */
    public void assignFolderProperties(KmlContainer kmlFolder)
            throws XmlPullParserException, IOException {
        mParser.next();
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG &&
                mParser.getName().matches(CONTAINER_START_TAG_REGEX))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals(FOLDER_START_TAG)) {
                    KmlContainer container = new KmlContainer();
                    kmlFolder.addChildContainer(container);
                    assignFolderProperties(container);
                } else if (mParser.getName().matches(PROPERTY_TAG_REGEX)) {
                    kmlFolder.setProperty(mParser.getName(), mParser.nextText());
                } else if (mParser.getName().equals(STYLE_MAP_START_TAG)) {
                    KmlStyleParser styleParser = new KmlStyleParser(mParser);
                    styleParser.createStyleMap();
                    kmlFolder.setStyleMap(styleParser.getStyleMaps());
                } else if (mParser.getName().equals(STYLE_START_TAG)) {
                    KmlStyleParser styleParser = new KmlStyleParser(mParser);
                    styleParser.createStyle();
                    kmlFolder.setStyle(styleParser.getStyle().getStyleId(), styleParser.getStyle());
                }  else if (mParser.getName().equals(PLACEMARK_START_TAG)) {
                    KmlPlacemarkParser placemarkParser = new KmlPlacemarkParser(mParser);
                    placemarkParser.createPlacemark();
                    if (placemarkParser.getPlacemark() != null) {
                        kmlFolder.setPlacemark(placemarkParser.getPlacemark(), null);
                    }
                }
            }
            eventType = mParser.next();
        }
    }

    /**
     * @return List of containers
     */
    public KmlContainer getContainer() {
        return mContainer;
    }


}