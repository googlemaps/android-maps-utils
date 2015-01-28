package com.google.maps.android.kml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Parses the container of a given KML file into a KmlContainer object
 */
/* package */ class KmlContainerParser {

    private final static String PROPERTY_REGEX = "name|description|visibility|open";

    private final static String CONTAINER_REGEX = "Folder|Document";

    private final static String PLACEMARK = "Placemark";

    private final static String STYLE = "Style";

    private final static String STYLE_MAP = "StyleMap";

    private final static String EXTENDED_DATA = "ExtendedData";

    private final static String GROUND_OVERLAY = "GroundOverlay";

    private final KmlFeatureParser mFeatureParser;

    private final XmlPullParser mParser;

    private KmlContainer mContainer;

    /* package */ KmlContainerParser(XmlPullParser parser) {
        mParser = parser;
        mFeatureParser = new KmlFeatureParser(parser);
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
                if (mParser.getName().matches(CONTAINER_REGEX)) {
                    setNestedContainerObject(kmlFolder);
                } else if (mParser.getName().matches(PROPERTY_REGEX)) {
                    setContainerProperty(kmlFolder);
                } else if (mParser.getName().equals(STYLE_MAP)) {
                    setContainerStyleMap(kmlFolder);
                } else if (mParser.getName().equals(STYLE)) {
                    setContainerStyle(kmlFolder);
                } else if (mParser.getName().equals(PLACEMARK)) {
                    setContainerPlacemark(kmlFolder);
                } else if (mParser.getName().equals(EXTENDED_DATA)) {
                    setExtendedDataProperties(kmlFolder);
                } else if (mParser.getName().equals(GROUND_OVERLAY)) {
                    KmlGroundOverlay kmlGroundOverlay = mFeatureParser.createGroundOverlay();
                    kmlFolder.addGroundOverlay(kmlGroundOverlay);
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
    /* package */ void setNestedContainerObject(KmlContainer kmlFolder)
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
    /* package */ void setContainerStyleMap(KmlContainer kmlFolder)
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
    /* package */ void setContainerProperty(KmlContainer kmlFolder)
            throws XmlPullParserException, IOException {
        String propertyName = mParser.getName();
        String propertyValue = mParser.nextText();
        kmlFolder.setProperty(propertyName, propertyValue);
    }

    /**
     * Adds untyped name value pairs parsed from the ExtendedData
     *
     * @param kmlContainer folder to add properties to
     */
        /* package */ void setExtendedDataProperties(KmlContainer kmlContainer)
            throws XmlPullParserException, IOException {
        String propertyKey = null;
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals(EXTENDED_DATA))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals("Data")) {
                    propertyKey = mParser.getAttributeValue(null, "name");
                } else if (mParser.getName().equals("value") && propertyKey != null) {
                    kmlContainer.setProperty(propertyKey, mParser.nextText());
                    propertyKey = null;
                }
            }
            eventType = mParser.next();
        }
    }

    /**
     * Creates a new kml style
     *
     * @param kmlContainer stores the new kml style
     */
    /* package */ void setContainerStyle(KmlContainer kmlContainer)
            throws XmlPullParserException, IOException {
        if (mParser.getAttributeValue(null, "id") != null) {
            // Don't parse inline styles
            KmlStyleParser styleParser = new KmlStyleParser(mParser);
            styleParser.createStyle();
            kmlContainer.setStyle(styleParser.getStyle().getStyleId(), styleParser.getStyle());
        }
    }

    /**
     * Creates a new basic_placemark
     *
     * @param kmlContainer folder to store basic_placemark
     */
    /* package */ void setContainerPlacemark(KmlContainer kmlContainer)
            throws XmlPullParserException, IOException {
        mFeatureParser.createPlacemark();
        if (mFeatureParser.getPlacemark() != null) {
            kmlContainer.setPlacemark(mFeatureParser.getPlacemark(), null);
        }
    }

    /**
     * @return container
     */
    /* package */ KmlContainer getContainer() {
        return mContainer;
    }


}