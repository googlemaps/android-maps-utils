package com.google.maps.android.kml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by lavenderch on 1/12/15.
 */
public class KmlStyleParser {

    private final HashMap<String, String> mStyleMaps;

    private static final String STYLE_TAG = "styleUrl";

    private XmlPullParser mParser;

    private int scale;

    private KmlStyle mStyle;

    public KmlStyleParser(XmlPullParser parser) {
        scale = 1;
        mStyleMaps = new HashMap<String, String>();
        mParser = parser;
        mStyle = new KmlStyle();
    }

    /**
     * Parses the IconStyle, LineStyle and PolyStyle tags into a KmlStyle object
     */
    public void createStyle() throws IOException, XmlPullParserException {
        // Indicates if any valid style tags have been found
        Boolean isValidStyle = false;
        KmlStyle styleProperties = new KmlStyle();
        // Append # to a local styleUrl
        String styleId = "#" + mParser.getAttributeValue(null, "id");
        styleProperties.setStyleId(styleId);
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

    public boolean createIconStyle(KmlStyle style) throws XmlPullParserException, IOException {
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("IconStyle"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals("scale")) {
                    setIconScale(style);
                } else if (mParser.getName().equals("heading")) {
                    setIconHeading(style);
                } else if (mParser.getName().equals("Icon")) {
                    setIconUrl(style);
                } else if (mParser.getName().equals("hotSpot")) {
                    setIconHotSpot(style);
                } else if (mParser.getName().equals("color")) {
                    setIconColor(style);
                } else if (mParser.getName().equals("colorMode")) {
                    setIconColorMode(style);
                }
            }
            eventType = mParser.next();
        }
        return true;
    }

    /**
     * Parses the StyleMap property and stores the id and the normal style tag
     */
    public void createStyleMap() throws XmlPullParserException, IOException {
        // Indicates if a normal style is to be stored
        Boolean isNormalKey = false;
        // Append # to style id
        String styleId = "#" + mParser.getAttributeValue(null, "id");
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("StyleMap"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals("key") && mParser.nextText().equals("normal")) {
                    isNormalKey = true;
                } else if (mParser.getName().equals(STYLE_TAG) && isNormalKey) {
                    mStyleMaps.put(styleId, mParser.nextText());
                    isNormalKey = false;
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
        String text = null;
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("BalloonStyle"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().equals("text")) {
                    text = mParser.nextText();
                }
            }
            eventType = mParser.next();
        }
        style.setInfoWindow(text);
        return true;
    }

    private void setIconScale(KmlStyle style) throws XmlPullParserException, IOException {
        String scaleString = mParser.nextText();
        Double scaleDouble = Double.parseDouble(scaleString);
        style.setIconScale(scaleDouble);
    }

    /**
     * Sets the icon heading for the style
     * @param style Style to set the icon heading to
     */
    private void setIconHeading (KmlStyle style)  throws XmlPullParserException, IOException {
        String iconHeadingString = mParser.nextText();
        Float iconHeadingFloat = Float.parseFloat(iconHeadingString);
        style.setHeading(iconHeadingFloat);
    }

    private void setIconColorMode(KmlStyle style)  throws XmlPullParserException, IOException {
        style.setColorMode("Point", mParser.nextText());
    }

    /**
     * Sets the icon url for the style
     *
     * @param style Style to set the icon url to
     */

    private void setIconUrl (KmlStyle style) throws XmlPullParserException, IOException {
        int eventType = mParser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && mParser.getName().equals("Icon"))) {
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
        yValue =  Float.parseFloat(mParser.getAttributeValue(null, "y"));
        xUnits =  mParser.getAttributeValue(null, "xunits");
        yUnits = mParser.getAttributeValue(null, "yunits");
        style.setHotSpot(xValue, yValue, xUnits, yUnits);
    }

    private void setIconColor(KmlStyle style)  throws XmlPullParserException, IOException {
       String colorString = mParser.nextText();
       style.setMarkerColor(colorString);
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
                if (mParser.getName().equals("color")) {
                    style.setOutlineColor(mParser.nextText());
                } else if (mParser.getName().equals("width")) {
                    style.setWidth(Float.valueOf(mParser.nextText()));
                } else if (mParser.getName().equals("colorMode")) {
                    style.setColorMode("LineString", mParser.nextText());
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
                if (mParser.getName().equals("color")) {
                    style.setFillColor(mParser.nextText());
                } else if (mParser.getName().equals("outline")) {
                    style.setOutline(Boolean.parseBoolean(mParser.nextText()));
                } else if (mParser.getName().equals("fill")) {
                    style.setFill(Boolean.parseBoolean(mParser.nextText()));
                } else if (mParser.getName().equals("colorMode")) {
                    style.setColorMode("Polygon", mParser.nextText());
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
    public KmlStyle getStyle() {
        return mStyle;
    }

    /**
     * Gets the hashmap of StyleMap objects
     * @return hashmap of KmlStyleMap objects
     */
    public  HashMap<String, String> getStyleMaps() { return mStyleMaps; }

}
