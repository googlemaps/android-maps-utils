package com.google.maps.android.data.kml;

import com.google.android.gms.maps.model.GroundOverlay;
import com.google.maps.android.data.Feature;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.xmlpull.v1.XmlPullParser.END_TAG;
import static org.xmlpull.v1.XmlPullParser.START_TAG;

/**
 * Parses the container of a given KML file into a KmlContainer object
 */
/* package */ class KmlContainerParser {

    private final static String PROPERTY_REGEX = "name|description|visibility|open|address|phoneNumber";

    private final static String CONTAINER_REGEX = "Folder|Document";

    private final static String PLACEMARK = "Placemark";

    private final static String STYLE = "Style";

    private final static String STYLE_MAP = "StyleMap";

    private final static String EXTENDED_DATA = "ExtendedData";

    private final static String GROUND_OVERLAY = "GroundOverlay";

    private final static String UNSUPPORTED_REGEX = "altitude|altitudeModeGroup|altitudeMode|" +
            "begin|bottomFov|cookie|displayName|displayMode|displayMode|end|expires|extrude|flyToView|" +
            "gridOrigin|httpQuery|leftFov|linkDescription|linkName|linkSnippet|listItemType|maxSnippetLines|" +
            "maxSessionLength|message|minAltitude|minFadeExtent|minLodPixels|minRefreshPeriod|maxAltitude|" +
            "maxFadeExtent|maxLodPixels|maxHeight|maxWidth|near|overlayXY|range|" +
            "refreshMode|refreshInterval|refreshVisibility|rightFov|roll|rotationXY|screenXY|shape|" +
            "sourceHref|state|targetHref|tessellate|tileSize|topFov|viewBoundScale|viewFormat|" +
            "viewRefreshMode|viewRefreshTime|when";

    /**
     * Obtains a Container object (created if a Document or Folder start tag is read by the
     * XmlPullParser) and assigns specific elements read from the XmlPullParser to the container.
     */

    /* package */
    static KmlContainer createContainer(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        return assignPropertiesToContainer(parser);
    }

    /**
     * Creates a new KmlContainer objects and assigns specific elements read from the XmlPullParser
     * to the new KmlContainer.
     *
     * @param parser XmlPullParser object reading from a KML file
     * @return KmlContainer object with properties read from the XmlPullParser
     */
    private static KmlContainer assignPropertiesToContainer(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        String startTag = parser.getName();
        String containerId = null;
        HashMap<String, String> containerProperties = new HashMap<String, String>();
        HashMap<String, KmlStyle> containerStyles = new HashMap<String, KmlStyle>();
        HashMap<? extends Feature, Object> containerPlacemarks = new HashMap<>();
        ArrayList<KmlContainer> nestedContainers = new ArrayList<KmlContainer>();
        HashMap<String, String> containerStyleMaps = new HashMap<String, String>();
        HashMap<KmlGroundOverlay, GroundOverlay> containerGroundOverlays
                = new HashMap<KmlGroundOverlay, GroundOverlay>();

        if (parser.getAttributeValue(null, "id") != null) {
            containerId = parser.getAttributeValue(null, "id");
        }

        parser.next();
        int eventType = parser.getEventType();
        while (!(eventType == END_TAG && parser.getName().equals(startTag))) {
            if (eventType == START_TAG) {
                if (parser.getName().matches(UNSUPPORTED_REGEX)) {
                    KmlParser.skip(parser);
                } else if (parser.getName().matches(CONTAINER_REGEX)) {
                    nestedContainers.add(assignPropertiesToContainer(parser));
                } else if (parser.getName().matches(PROPERTY_REGEX)) {
                    containerProperties.put(parser.getName(), parser.nextText());
                } else if (parser.getName().equals(STYLE_MAP)) {
                    setContainerStyleMap(parser, containerStyleMaps);
                } else if (parser.getName().equals(STYLE)) {
                    setContainerStyle(parser, containerStyles);
                } else if (parser.getName().equals(PLACEMARK)) {
                    setContainerPlacemark(parser, (HashMap<KmlPlacemark, Object>) containerPlacemarks);
                } else if (parser.getName().equals(EXTENDED_DATA)) {
                    setExtendedDataProperties(parser, containerProperties);
                } else if (parser.getName().equals(GROUND_OVERLAY)) {
                    containerGroundOverlays
                            .put(KmlFeatureParser.createGroundOverlay(parser), null);
                }
            }
            eventType = parser.next();
        }

        return new KmlContainer(containerProperties, containerStyles, (HashMap<KmlPlacemark, Object>) containerPlacemarks,
                containerStyleMaps, nestedContainers, containerGroundOverlays, containerId);
    }

    /**
     * Creates a new style map and assigns values from the XmlPullParser parser
     * and stores it into the container.
     */
    private static void setContainerStyleMap(XmlPullParser parser,
            HashMap<String, String> containerStyleMap) throws XmlPullParserException, IOException {
        containerStyleMap.putAll(KmlStyleParser.createStyleMap(parser));
    }

    /**
     * Assigns properties given as an extended data element, which are obtained from an
     * XmlPullParser and stores it in a container, Untyped data only, no simple data
     * or schema, and entity replacements of the form $[dataName] are unsupported.
     */
    private static void setExtendedDataProperties(XmlPullParser parser,
            HashMap<String, String> mContainerProperties)
            throws XmlPullParserException, IOException {
        String propertyKey = null;
        int eventType = parser.getEventType();
        while (!(eventType == END_TAG && parser.getName().equals(EXTENDED_DATA))) {
            if (eventType == START_TAG) {
                if (parser.getName().equals("Data")) {
                    propertyKey = parser.getAttributeValue(null, "name");
                } else if (parser.getName().equals("value") && propertyKey != null) {
                    mContainerProperties.put(propertyKey, parser.nextText());
                    propertyKey = null;
                }
            }
            eventType = parser.next();
        }
    }

    /**
     * Creates a new default Kml Style with a specified ID (given as an attribute value in the
     * start tag) and assigns specific elements read from the XmlPullParser to the Style. A new
     * style is not created if it does not have an ID.
     */
    private static void setContainerStyle(XmlPullParser parser,
            HashMap<String, KmlStyle> containerStyles) throws XmlPullParserException, IOException {
        if (parser.getAttributeValue(null, "id") != null) {
            KmlStyle style = KmlStyleParser.createStyle(parser);
            String styleId = style.getStyleId();
            containerStyles.put(styleId, style);
        }
    }

    /**
     * Creates a new placemark object  and assigns specific elements read from the XmlPullParser
     * to the Placemark and stores this into the given Container.
     */
    private static void setContainerPlacemark(XmlPullParser parser,
            HashMap<KmlPlacemark, Object> containerPlacemarks)
            throws XmlPullParserException, IOException {
        containerPlacemarks.put(KmlFeatureParser.createPlacemark(parser), null);
    }

}