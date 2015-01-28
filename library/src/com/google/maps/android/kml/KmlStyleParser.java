package com.google.maps.android.kml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;

/**
 * Parses the styles of a given KML file into a KmlStyle object
 */
/* package */ class KmlStyleParser {

    private final static String STYLE_TAG = "styleUrl";

    private final static String ICON_STYLE_HEADING = "heading";

    private final static String ICON_STYLE_URL = "Icon";

    private final static String ICON_STYLE_SCALE = "scale";

    private final static String ICON_STYLE_HOTSPOT = "hotSpot";

    private final static String COLOR_STYLE_COLOR = "color";

    private final static String COLOR_STYLE_MODE = "colorMode";

    private final static String STYLE_MAP_KEY = "key";

    private final static String STYLE_MAP_NORMAL_STYLE = "normal";

    private final static String LINE_STYLE_WIDTH = "width";

    private final static String POLY_STYLE_OUTLINE = "outline";

    private final static String POLY_STYLE_FILL = "fill";

    private final HashMap<String, String> mStyleMaps;

    private final XmlPullParser mParser;

    private KmlStyle mStyle;


    /* package */ KmlStyleParser(XmlPullParser parser) {
        mStyleMaps = new HashMap<String, String>();
        mParser = parser;
        mStyle = new KmlStyle();
    }

    /**
     * Parses the IconStyle, LineStyle and PolyStyle tags into a KmlStyle object
     */
    /* package */ void createStyle() throws IOException, XmlPullParserException {
        // Indicates if any valid style tags have been found
        Boolean isValidStyle = false;
        KmlStyle styleProperties = new KmlStyle();
        if (mParser.getAttributeValue(null, "id") != null) {
            // Append # to a local styleUrl
            String styleId = "#" + mParser.getAttributeValue(null, "id");
            styleProperties.setStyleId(styleId);
        }
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("Style"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals("IconStyle")) {
                    isValidStyle = createIconStyle(styleProperties);
                } else if (mParser.getName().equals("LineStyle")) {
                    isValidStyle = createLineStyle(styleProperties);
                } else if (mParser.getName().equals("PolyStyle")) {
                    isValidStyle = createPolyStyle(styleProperties);
                } else if (mParser.getName().equals("BalloonStyle")) {
                    isValidStyle = createBalloonStyle(styleProperties);
                }
            }
            eventType = mParser.next();
        }

        // Check if supported styles are added, unsupported styles are not saved
        if (isValidStyle) {
            mStyle = styleProperties;
        }
    }

    /**
     * Adds icon properties to a KmlStyle
     *
     * @param style Style to apply properties to
     * @return true if icon style has been set
     */
    /* package */ boolean createIconStyle(KmlStyle style)
            throws XmlPullParserException, IOException {
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("IconStyle"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals(ICON_STYLE_HEADING)) {
                    style.setHeading(Float.parseFloat(mParser.nextText()));
                } else if (mParser.getName().equals(ICON_STYLE_URL)) {
                    setIconUrl(style);
                } else if (mParser.getName().equals(ICON_STYLE_HOTSPOT)) {
                    setIconHotSpot(style);
                } else if (mParser.getName().equals(ICON_STYLE_SCALE)) {
                    style.setIconScale(Double.parseDouble(mParser.nextText()));
                } else if (mParser.getName().equals(COLOR_STYLE_COLOR)) {
                    style.setMarkerColor(mParser.nextText());
                } else if (mParser.getName().equals(COLOR_STYLE_MODE)) {
                    style.setIconColorMode(mParser.nextText());
                }
            }
            eventType = mParser.next();
        }
        return true;
    }

    /**
     * Parses the StyleMap property and stores the id and the normal style tag
     */
    /* package */ void createStyleMap() throws XmlPullParserException, IOException {
        // Indicates if a normal style is to be stored
        Boolean isNormalStyleMapValue = false;
        // Append # to style id
        String styleId = "#" + mParser.getAttributeValue(null, "id");
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("StyleMap"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals(STYLE_MAP_KEY)
                        && mParser.nextText().equals(STYLE_MAP_NORMAL_STYLE)) {
                    isNormalStyleMapValue = true;
                } else if (mParser.getName().equals(STYLE_TAG) && isNormalStyleMapValue) {
                    mStyleMaps.put(styleId, mParser.nextText());
                    isNormalStyleMapValue = false;
                }
            }
            eventType = mParser.next();
        }
    }

    /**
     * Sets relevant styling properties to the KmlStyle object that are found in the IconStyle tag
     * Supported tags include scale, heading, Icon, href, hotSpot
     *
     * @param style Style object to add properties to
     */
    private boolean createBalloonStyle(KmlStyle style) throws XmlPullParserException, IOException {
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("BalloonStyle"))) {
            if (eventType == XmlPullParser.START_TAG && mParser.getName().equals("text")) {
                style.setInfoWindowText(mParser.nextText());
            }
            eventType = mParser.next();
        }
        return true;
    }

    /**
     * Sets the icon url for the style
     *
     * @param style Style to set the icon url to
     */

    private void setIconUrl(KmlStyle style) throws XmlPullParserException, IOException {
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals(ICON_STYLE_URL))) {
            if (eventType == XmlPullParser.START_TAG && mParser.getName().equals("href")) {
                style.setIconUrl(mParser.nextText());
            }
            eventType = mParser.next();
        }
    }

    /**
     * Sets the hot spot for the icon
     *
     * @param style Style object to apply hotspot properties to
     */
    private void setIconHotSpot(KmlStyle style) {
        Float xValue, yValue;
        String xUnits, yUnits;
        xValue = Float.parseFloat(mParser.getAttributeValue(null, "x"));
        yValue = Float.parseFloat(mParser.getAttributeValue(null, "y"));
        xUnits = mParser.getAttributeValue(null, "xunits");
        yUnits = mParser.getAttributeValue(null, "yunits");
        style.setHotSpot(xValue, yValue, xUnits, yUnits);
    }

    /**
     * Sets relevant styling properties to the KmlStyle object that are found in the LineStyle tag
     * Supported tags include color, width
     *
     * @param style Style object to add properties to
     */
    private boolean createLineStyle(KmlStyle style) throws XmlPullParserException, IOException {
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("LineStyle"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals(COLOR_STYLE_COLOR)) {
                    style.setOutlineColor(mParser.nextText());
                } else if (mParser.getName().equals(LINE_STYLE_WIDTH)) {
                    style.setWidth(Float.valueOf(mParser.nextText()));
                } else if (mParser.getName().equals(COLOR_STYLE_MODE)) {
                    style.setLineColorMode(mParser.nextText());
                }
            }
            eventType = mParser.next();
        }
        return true;
    }

    /**
     * Sets relevant styling properties to the KmlStyle object that are found in the PolyStyle tag
     * Supported tags include color, outline, fill
     *
     * @param style Style object to add properties to
     */
    private boolean createPolyStyle(KmlStyle style) throws XmlPullParserException, IOException {
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("PolyStyle"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals(COLOR_STYLE_COLOR)) {
                    style.setFillColor(mParser.nextText());
                } else if (mParser.getName().equals(POLY_STYLE_OUTLINE)) {
                    style.setOutline(Boolean.parseBoolean(mParser.nextText()));
                } else if (mParser.getName().equals(POLY_STYLE_FILL)) {
                    style.setFill(Boolean.parseBoolean(mParser.nextText()));
                } else if (mParser.getName().equals(COLOR_STYLE_MODE)) {
                    style.setPolyColorMode(mParser.nextText());
                }
            }
            eventType = mParser.next();
        }
        return true;
    }

    /**
     * Gets the hashmap of KmlStyle objects
     *
     * @return hashmap of KmlStyle objects
     */
    /* package */ KmlStyle getStyle() {
        return mStyle;
    }

    /**
     * Gets the hashmap of StyleMap objects
     *
     * @return hashmap of KmlStyleMap objects
     */
    /* package */ HashMap<String, String> getStyleMaps() {
        return mStyleMaps;
    }

}
