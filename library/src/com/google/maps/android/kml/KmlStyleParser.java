package com.google.maps.android.kml;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by lavenderch on 1/12/15.
 */
public class KmlStyleParser {

    private final HashMap<String, KmlStyle> mStyles;

    private final HashMap<String, String> mStyleMaps;

    private static final String STYLE_TAG = "styleUrl";

    private XmlPullParser mParser;

    private int scale;

    public KmlStyleParser(XmlPullParser parser) {
        scale = 1;
        mStyles = new HashMap<String, KmlStyle>();
        mStyleMaps = new HashMap<String, String>();
        mParser = parser;
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
            mStyles.put(styleId, styleProperties);
        }
        //Adds a default style
        mStyles.put(null, new KmlStyle());
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
    public HashMap<String, KmlStyle> getStyles() {
        return mStyles;
    }

    /**
     * Gets the hashmap of StyleMap objects
     * @return hashmap of KmlStyleMap objects
     */
    public  HashMap<String, String> getStyleMaps() { return mStyleMaps; }

}
