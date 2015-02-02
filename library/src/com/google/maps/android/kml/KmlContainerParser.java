package com.google.maps.android.kml;

import com.google.android.gms.maps.model.GroundOverlay;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.xmlpull.v1.XmlPullParser.START_TAG;
import static org.xmlpull.v1.XmlPullParser.END_TAG;

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

    private KmlContainer mContainer;

    private KmlStyleParser mStyleParser;

    /* package */ KmlContainerParser() {
        mStyleParser = new KmlStyleParser();
        mFeatureParser = new KmlFeatureParser();
        mContainer = null;
    }

    /**
     * Obtains a Container object (created if a Document or Folder start tag is read by the
     * XmlPullParser) and assigns specific elements read from the XmlPullParser to the container.
     */

    public void createContainer(XmlPullParser mParser) throws XmlPullParserException, IOException {
        mContainer = assignPropertiesToContainer(mParser);
    }

    /**
     * Creates a new KmlContainer objects and assigns specific elements read from the XmlPullParser
     * to the new KmlContainer.
     *
     * @param mParser   XmlPullParser object reading from a KML file
     * @return  KmlContainer object with properties read from the XmlPullParser
     */
    /* package */ KmlContainer assignPropertiesToContainer(XmlPullParser mParser)
                  throws XmlPullParserException, IOException {
        String startTag = mParser.getName();
        String containerId = null;
        HashMap<String, String> containerProperties = new HashMap<String, String>();
        HashMap<String, KmlStyle> containerStyles = new HashMap<String, KmlStyle>();
        HashMap<KmlPlacemark, Object>  containerPlacemarks = new HashMap<KmlPlacemark, Object>();
        ArrayList<KmlContainer> nestedContainers = new ArrayList<KmlContainer>();
        HashMap<String, String> containerStyleMaps = new HashMap<String, String>();
        HashMap<KmlGroundOverlay, GroundOverlay> containerGroundOverlays
                = new HashMap<KmlGroundOverlay, GroundOverlay>();

        if (mParser.getAttributeValue(null, "id") != null) {
            containerId = mParser.getAttributeValue(null, "id");
        }

        mParser.next();
        int eventType = mParser.getEventType();
        while (!(eventType == END_TAG && mParser.getName().equals(startTag))) {
            if (eventType == START_TAG) {
                if (mParser.getName().matches(CONTAINER_REGEX)) {
                    nestedContainers.add(assignPropertiesToContainer(mParser));
                } else if (mParser.getName().matches(PROPERTY_REGEX)) {
                    containerProperties.put(mParser.getName(), mParser.nextText());
                } else if (mParser.getName().equals(STYLE_MAP)) {
                    setContainerStyleMap(mParser, containerStyleMaps);
                } else if (mParser.getName().equals(STYLE)) {
                    setContainerStyle(containerStyles, mParser);
                } else if (mParser.getName().equals(PLACEMARK)) {
                    setContainerPlacemark(containerPlacemarks, mParser);
                } else if (mParser.getName().equals(EXTENDED_DATA)) {
                    setExtendedDataProperties(containerProperties, mParser);
                } else if (mParser.getName().equals(GROUND_OVERLAY)) {
                    mFeatureParser.createGroundOverlay(mParser);
                    KmlGroundOverlay kmlGroundOverlay = mFeatureParser.getGroundOverlay();
                    containerGroundOverlays.put(kmlGroundOverlay, null);
                }
            }
            eventType = mParser.next();
        }

        return new KmlContainer(containerProperties, containerStyles, containerPlacemarks,
                containerStyleMaps, nestedContainers, containerGroundOverlays, containerId);
    }

    /**
     * Creates a new style map and assigns values from the XmlPullParser parser
     * and stores it into the container.
     *
     */
    /* package */ void setContainerStyleMap(XmlPullParser mParser,
     HashMap<String, String> containerStyleMap) throws XmlPullParserException, IOException {
        mStyleParser.createStyleMap(mParser);
        containerStyleMap.putAll(mStyleParser.getStyleMaps());
    }

    /**
     * Assigns properties given as an extended data element, which are obtained from an
     * XmlPullParser and stores it in a container, Untyped <Data> only, no <SimpleData>
     * or <Schema>, and entity replacements of the form $[dataName] are unsupported.
     *
     */
    /* package */ void setExtendedDataProperties(HashMap<String, String> mContainerProperties,
    XmlPullParser mParser) throws XmlPullParserException, IOException {
        String propertyKey = null;
        int eventType = mParser.getEventType();
        while (!(eventType == END_TAG && mParser.getName().equals(EXTENDED_DATA))) {
            if (eventType == START_TAG) {
                if (mParser.getName().equals("Data")) {
                    propertyKey = mParser.getAttributeValue(null, "name");
                } else if (mParser.getName().equals("value") && propertyKey != null) {
                    mContainerProperties.put(propertyKey, mParser.nextText());
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
     */
    /* package */ void setContainerStyle(HashMap<String, KmlStyle> containerStyles,
    XmlPullParser mParser) throws XmlPullParserException, IOException {
        Boolean hasStyleId = mParser.getAttributeValue(null, "id") != null;
        if (hasStyleId) {
            mStyleParser.createStyle(mParser);
            String styleId = mStyleParser.getStyle().getStyleId();
            KmlStyle style = mStyleParser.getStyle();
            containerStyles.put(styleId, style);
        }
    }

    /**
     * Creates a new placemark object  and assigns specific elements read from the XmlPullParser
     * to the Placemark and stores this into the given Container.
     *
     */
    /* package */ void setContainerPlacemark(HashMap<KmlPlacemark, Object> containerPlacemarks,
        XmlPullParser mParser) throws XmlPullParserException, IOException {
            containerPlacemarks.put(KmlFeatureParser.createPlacemark(mParser), null);
    }

    /**
     * Retrieves this KmlContainer instance
     *
     * @return KmlContainer instance if it exists, null otherwise
     */
    /* package */ KmlContainer getContainer() {
        return mContainer;
    }


}