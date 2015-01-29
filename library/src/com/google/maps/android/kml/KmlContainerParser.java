package com.google.maps.android.kml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Parses the container of a given KML file into a KmlContainer object
 */
/* package */ class KmlContainerParser {

    private final static int START_TAG = XmlPullParser.START_TAG;

    private final static int END_TAG = XmlPullParser.END_TAG;

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
     * Creates a new Container object (created if a Document or Folder start tag is read by the
     * XmlPullParser) and assigns specific elements read from the XmlPullParser to the container.
     */

    /* package */ void createContainer() throws XmlPullParserException, IOException {
        KmlContainer kmlContainer = new KmlContainer();
        assignContainerProperties(kmlContainer);
        mContainer = kmlContainer;
    }

    /**
     * Obtains relevant values from an XML start tag and assigns these values to variables
     * within a KmlContainer class
     *
     * @param kmlContainer Container to store XML start tags and its corresponding value
     */
    /* package */ void assignContainerProperties(KmlContainer kmlContainer)
            throws XmlPullParserException, IOException {
        String startTag = mParser.getName();
        mParser.next();
        int eventType = mParser.getEventType();
        while (!(eventType == END_TAG && mParser.getName().equals(startTag))) {
            if (eventType == START_TAG) {
                if (mParser.getName().matches(CONTAINER_REGEX)) {
                    setNestedContainerObject(kmlContainer);
                } else if (mParser.getName().matches(PROPERTY_REGEX)) {
                    setContainerProperty(kmlContainer);
                } else if (mParser.getName().equals(STYLE_MAP)) {
                    setContainerStyleMap(kmlContainer);
                } else if (mParser.getName().equals(STYLE)) {
                    setContainerStyle(kmlContainer);
                } else if (mParser.getName().equals(PLACEMARK)) {
                    setContainerPlacemark(kmlContainer);
                } else if (mParser.getName().equals(EXTENDED_DATA)) {
                    setExtendedDataProperties(kmlContainer);
                } else if (mParser.getName().equals(GROUND_OVERLAY)) {
                    mFeatureParser.createGroundOverlay();
                    KmlGroundOverlay kmlGroundOverlay = mFeatureParser.getGroundOverlay();
                    kmlContainer.addGroundOverlay(kmlGroundOverlay);
                }
            }
            eventType = mParser.nextToken();
        }
    }

    /**
     * Creates a new Container object (created if a Document or Folder start tag is read by the
     * XmlPullParser) and assigns specific elements read from the XmlPullParser to the container.
     *
     * @param kmlFolder Stores new container object
     */
    /* package */ void setNestedContainerObject(KmlContainer kmlFolder)
            throws XmlPullParserException, IOException {
        KmlContainer container = new KmlContainer();
        assignContainerProperties(container);
        kmlFolder.addChildContainer(container);
    }

    /**
     * Creates a new style map and assigns values from the input parser
     * and stores it into the container.
     *
     * @param kmlContainer Stores new style map
     */
    /* package */ void setContainerStyleMap(KmlContainer kmlContainer)
            throws XmlPullParserException, IOException {
        KmlStyleParser styleParser = new KmlStyleParser(mParser);
        styleParser.createStyleMap();
        kmlContainer.setStyleMap(styleParser.getStyleMaps());
    }

    /**
     * Assigns properties which are obtained from an XmlPullParser and stores it into
     * the container. Only <name>, <description>, <visibility> and <open> are supported as
     * properties.
     *
     * @param kmlContainer Stores properties
     */
    /* package */ void setContainerProperty(KmlContainer kmlContainer)
            throws XmlPullParserException, IOException {
        String propertyName = mParser.getName();
        String propertyValue = mParser.nextText();
        kmlContainer.setProperty(propertyName, propertyValue);
    }

    /**
     * Assigns properties given as an extended data element, which are obtained from an
     * XmlPullParser and stores it in a container, Untyped <Data> only, no <SimpleData>
     * or <Schema>, and entity replacements of the form $[dataName] are unsupported.
     *
     * @param kmlContainer container to store extended data properties
     */
        /* package */ void setExtendedDataProperties(KmlContainer kmlContainer)
            throws XmlPullParserException, IOException {
        String propertyKey = null;
        int eventType = mParser.getEventType();
        while (!(eventType == END_TAG && mParser.getName().equals(EXTENDED_DATA))) {
            if (eventType == START_TAG) {
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
     * Creates a new default Kml Style with a specified ID (given as an attribute value in the
     * start tag) and assigns specific elements read from the XmlPullParser to the Style. A new
     * style is not created if it does not have an ID.
     *
     * @param kmlContainer Stores styles
     */
    /* package */ void setContainerStyle(KmlContainer kmlContainer)
            throws XmlPullParserException, IOException {
        Boolean hasStyleId = mParser.getAttributeValue(null, "id") != null;
        if (hasStyleId) {
            KmlStyleParser styleParser = new KmlStyleParser(mParser);
            styleParser.createStyle();
            String styleId = styleParser.getStyle().getStyleId();
            KmlStyle style = styleParser.getStyle();
            kmlContainer.setStyle(styleId, style);
        }
    }

    /**
     * Creates a new placemark object  and assigns specific elements read from the XmlPullParser
     * to the Placemark and stores this into the given Container.
     *
     * @param kmlContainer Container object to store the Placemark object
     */
    /* package */ void setContainerPlacemark(KmlContainer kmlContainer)
            throws XmlPullParserException, IOException {
        mFeatureParser.createPlacemark();
        if (mFeatureParser.getPlacemark() != null) {
            kmlContainer.setPlacemark(mFeatureParser.getPlacemark(), null);
        }
    }

    /**
     * Retrieves this container
     *
     * @return container Container to get
     */
    /* package */ KmlContainer getContainer() {
        return mContainer;
    }


}