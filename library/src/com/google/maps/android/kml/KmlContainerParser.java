package com.google.maps.android.kml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Parses the container of a given KML file into a KmlContainer object
 */
/* package */ class KmlContainerParser {

    private final static String PROPERTY_TAG_REGEX = "name|description|visibility|open";

    private final static String CONTAINER_START_TAG_REGEX = "Folder|Document";

    private final static String PLACEMARK_START_TAG = "Placemark";

    private final static String STYLE_START_TAG = "Style";

    private final static String STYLE_MAP_START_TAG = "StyleMap";

    private final static String GROUND_OVERLAY_START_TAG = "GroundOverlay";

    private KmlContainer mContainer;

    private final XmlPullParser mParser;

    /* package */ KmlContainerParser(XmlPullParser parser) {
        mParser = parser;
        mContainer = null;
    }

    /**
     * Creates a new folder and adds this to an ArrayList of folders
     */

    /* package */ void createContainer() throws XmlPullParserException, IOException {
        KmlContainer folder = new KmlContainer();
        assignFolderProperties(folder);
        mContainer = folder;
    }

    /**
     * Takes a parser and assigns variables to a Folder instances
     *
     * @param kmlFolder Folder to assign variables to
     */
    /* package */ void assignFolderProperties(KmlContainer kmlFolder)
            throws XmlPullParserException, IOException {
        String startTag = mParser.getName();
        mParser.next();
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals(startTag))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().matches(CONTAINER_START_TAG_REGEX)) {
                    createContainerObject(kmlFolder);
                } else if (mParser.getName().matches(PROPERTY_TAG_REGEX)) {
                    setContainerProperty(kmlFolder);
                } else if (mParser.getName().equals(STYLE_MAP_START_TAG)) {
                    createContainerStyleMap(kmlFolder);
                } else if (mParser.getName().equals(STYLE_START_TAG)) {
                    createContainerStyle(kmlFolder);
                } else if (mParser.getName().equals(PLACEMARK_START_TAG)) {
                    createContainerPlacemark(kmlFolder);
                } else if (mParser.getName().equals(GROUND_OVERLAY_START_TAG)) {
                    //TODO: Ground overlay in containers
                }
            }
            eventType = mParser.next();
        }
    }

    /**
     * Creates a new container object
     *
     * @param kmlFolder Stores new container object
     */
    private void createContainerObject(KmlContainer kmlFolder)
            throws XmlPullParserException, IOException {
        KmlContainer container = new KmlContainer();
        assignFolderProperties(container);
        kmlFolder.addChildContainer(container);
    }

    /**
     * Creates a new hash map representing a style map
     *
     * @param kmlFolder Stores hash map
     */
    private void createContainerStyleMap(KmlContainer kmlFolder)
            throws XmlPullParserException, IOException {
        KmlStyleParser styleParser = new KmlStyleParser(mParser);
        styleParser.createStyleMap();
        kmlFolder.setStyleMap(styleParser.getStyleMaps());
    }

    /**
     * Sets a property value in folder
     *
     * @param kmlFolder Stores property
     */
    private void setContainerProperty(KmlContainer kmlFolder)
            throws XmlPullParserException, IOException {
        String propertyName = mParser.getName();
        String propertyValue = mParser.nextText();
        kmlFolder.setProperty(propertyName, propertyValue);
    }

    /**
     * Creates a new kml style
     *
     * @param kmlFolder stores the new kml style
     */
    private void createContainerStyle(KmlContainer kmlFolder)
            throws XmlPullParserException, IOException {
        KmlStyleParser styleParser = new KmlStyleParser(mParser);
        styleParser.createStyle();
        kmlFolder.setStyle(styleParser.getStyle().getStyleId(), styleParser.getStyle());
    }

    /**
     * Creates a new placemark
     *
     * @param kmlFolder folder to store placemark
     */
    private void createContainerPlacemark(KmlContainer kmlFolder)
            throws XmlPullParserException, IOException {
        KmlFeatureParser placemarkParser = new KmlFeatureParser(mParser);
        placemarkParser.createPlacemark();
        if (placemarkParser.getPlacemark() != null) {
            kmlFolder.setPlacemark(placemarkParser.getPlacemark(), null);
        }
    }

    /**
     * @return List of containers
     */
    /* package */ KmlContainer getContainer() {
        return mContainer;
    }


}