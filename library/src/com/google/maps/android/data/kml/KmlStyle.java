package com.google.maps.android.data.kml;

import android.graphics.Color;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.data.Style;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * Represents the defined styles in the KML document
 */
public class  KmlStyle extends Style {

    private final static int HSV_VALUES = 3;

    private final static int HUE_VALUE = 0;

    private final static int INITIAL_SCALE = 1;

    private final HashMap<String, String> mBalloonOptions;

    private final HashSet<String> mStylesSet;

    private boolean mFill = true;

    private boolean mOutline = true;

    private String mIconUrl;

    private double mScale;

    private String mStyleId;

    private boolean mIconRandomColorMode;

    private boolean mLineRandomColorMode;

    private boolean mPolyRandomColorMode;

    private float mMarkerColor;

    /**
     * Creates a new KmlStyle object
     */
    /* package */ KmlStyle() {
        super();
        mStyleId = null;
        mBalloonOptions = new HashMap<String, String>();
        mStylesSet = new HashSet<String>();
        mScale = INITIAL_SCALE;
        mMarkerColor = 0;
        mIconRandomColorMode = false;
        mLineRandomColorMode = false;
        mPolyRandomColorMode = false;
    }

    /**
     * Sets text found for an info window
     *
     * @param text Text for an info window
     */
    /* package */ void setInfoWindowText(String text) {
        mBalloonOptions.put("text", text);
    }

    /**
     * Gets the id for the style
     *
     * @return Style Id, null otherwise
     */
    /* package */ String getStyleId() {
        return mStyleId;
    }

    /**
     * Sets id for a style
     *
     * @param styleId  Id for the style
     */
    /* package */ void setStyleId(String styleId) {
        mStyleId = styleId;
    }

    /**
     * Checks if a given style (for a marker, linestring or polygon) has been set
     *
     * @param style style to check if set
     * @return True if style was set, false otherwise
     */
    public boolean isStyleSet(String style) {
        return mStylesSet.contains(style);
    }

    /**
     * Gets whether the Polygon fill is set
     *
     * @return True if there is a fill for the polygon, false otherwise
     */
    public boolean hasFill() {
        return mFill;
    }

    /**
     * Sets whether the Polygon has a fill
     *
     * @param fill True if the polygon fill is set, false otherwise
     */
    public void setFill(boolean fill) {
        mFill = fill;
    }

    /**
     * Gets the scale for a marker icon
     *
     * @return scale value
     */
    /* package */ double getIconScale() {
        return mScale;
    }

    /**
     * Sets the scale for a marker icon
     *
     * @param scale scale value
     */
    /* package */ void setIconScale(double scale) {
        mScale = scale;
        mStylesSet.add("iconScale");
    }

    /**
     * Gets whether the Polygon outline is set
     *
     * @return True if the polygon outline is set, false otherwise
     */
    public boolean hasOutline() {
        return mOutline;
    }

    /**
     * Gets whether a BalloonStyle has been set
     *
     * @return True if a BalloonStyle has been set, false otherwise
     */
    public boolean hasBalloonStyle() {
        return mBalloonOptions.size() > 0;
    }

    /**
     *  Sets whether the Polygon has an outline
     *
     * @param outline True if the polygon outline is set, false otherwise
     */
    /* package */ void setOutline(boolean outline) {
        mOutline = outline;
        mStylesSet.add("outline");
    }

    /**
     * Gets the url for the marker icon
     *
     * @return Url for the marker icon, null otherwise
     */
    public String getIconUrl() {
        return mIconUrl;
    }

    /**
     *  Sets the url for the marker icon
     *
     * @param iconUrl Url for the marker icon
     */
    /* package */ void setIconUrl(String iconUrl) {
        mIconUrl = iconUrl;
        mStylesSet.add("iconUrl");
    }

    /**
     * Sets the fill color for a KML Polygon using a String
     *
     * @param color Fill color for a KML Polygon as a String
     */
    /* package */ void setFillColor(String color) {
        // Add # to allow for mOutline color to be parsed correctly
        int polygonColorNum = (Color.parseColor("#" + convertColor(color)));
        setPolygonFillColor(polygonColorNum);
        mStylesSet.add("fillColor");
    }

    /**
     * Sets the color for a marker
     *
     * @param color Color for a marker
     */
    /* package */ void setMarkerColor(String color) {
        int integerColor = Color.parseColor("#" + convertColor(color));
        mMarkerColor = getHueValue(integerColor);
        mMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(mMarkerColor));
        mStylesSet.add("markerColor");
    }

    /**
     * Gets the hue value from a color
     *
     * @param integerColor Integer representation of a color
     * @return Hue value from a color
     */
    private static float getHueValue (int integerColor) {
        float[] hsvValues = new float[HSV_VALUES];
        Color.colorToHSV(integerColor, hsvValues);
        return hsvValues[HUE_VALUE];
    }

    /**
     * Converts a color format of the form AABBGGRR to AARRGGBB
     *
     * @param color Color of the form AABBGGRR
     * @return Color of the form AARRGGBB
     */
    private static String convertColor(String color) {
        String newColor;
        if (color.length() > 6) {
            newColor = color.substring(0, 2) + color.substring(6, 8)
                    + color.substring(4, 6)+ color.substring(2, 4);
        } else {
            newColor = color.substring(4, 6) + color.substring(2, 4) +
                    color.substring(0, 2);
        }
        // Maps exports KML colors with a leading 0 as a space.
        if (newColor.substring(0, 1).equals(" ")) {
            newColor = "0" + newColor.substring(1, newColor.length());
        }
        return newColor;
    }

    /**
     * Sets the rotation / heading for a marker
     *
     * @param heading Decimal representation of a rotation value
     */
    /* package */ void setHeading(float heading) {
        setMarkerRotation(heading);
        mStylesSet.add("heading");
    }

    /**
     * Sets the hotspot / anchor point of a marker
     *
     * @param x      x point of a marker position
     * @param y      y point of a marker position
     * @param xUnits units in which the x value is specified
     * @param yUnits units in which the y value is specified
     */
    /* package */ void setHotSpot(float x, float y, String xUnits, String yUnits) {
        setMarkerHotSpot(x,y, xUnits, yUnits);
        mStylesSet.add("hotSpot");
    }

    /**
     * Sets the color mode for a marker. A "random" color mode sets the color mode to true,
     * a "normal" colormode sets the color mode to false.
     *
     * @param colorMode A "random" or "normal" color mode
     */
    /* package */ void setIconColorMode(String colorMode) {
        mIconRandomColorMode = colorMode.equals("random");
        mStylesSet.add("iconColorMode");
    }

    /**
     * Checks whether the color mode for a marker is true / random
     *
     * @return True if the color mode is true, false otherwise
     */
    /* package */ boolean isIconRandomColorMode() {
        return mIconRandomColorMode;
    }

    /**
     * Sets the color mode for a polyline. A "random" color mode sets the color mode to true,
     * a "normal" colormode sets the color mode to false.
     *
     * @param colorMode A "random" or "normal" color mode
     */
    /* package */ void setLineColorMode(String colorMode) {
        mLineRandomColorMode = colorMode.equals("random");
        mStylesSet.add("lineColorMode");
    }

    /**
     * Checks whether the color mode for a polyline is true / random
     *
     * @return True if the color mode is true, false otherwise
     */
    public boolean isLineRandomColorMode() {
        return mLineRandomColorMode;
    }

    /**
     * Sets the color mode for a polygon. A "random" color mode sets the color mode to true,
     * a "normal" colormode sets the color mode to false.
     *
     * @param colorMode A "random" or "normal" color mode
     */
    /* package */ void setPolyColorMode(String colorMode) {
        mPolyRandomColorMode = colorMode.equals("random");
        mStylesSet.add("polyColorMode");
    }

    /**
     * Checks whether the color mode for a polygon is true / random
     *
     * @return True if the color mode is true, false otherwise
     */
    /* package */
    public boolean isPolyRandomColorMode() {
        return mPolyRandomColorMode;
    }

    /**
     * Sets the outline color for a Polyline and a Polygon using a String
     *
     * @param color Outline color for a Polyline and a Polygon represented as a String
     */
    /* package */ void setOutlineColor(String color) {
        // Add # to allow for mOutline color to be parsed correctly
        mPolylineOptions.color(Color.parseColor("#" + convertColor(color)));
        mPolygonOptions.strokeColor(Color.parseColor("#" + convertColor(color)));
        mStylesSet.add("outlineColor");
    }

    /**
     * Sets the line width for a Polyline and a Polygon
     *
     * @param width Line width for a Polyline and a Polygon
     */
    /* package */ void setWidth(Float width) {
        setLineStringWidth(width);
        setPolygonStrokeWidth(width);
        mStylesSet.add("width");
    }

    /**
     * Gets the balloon options
     *
     * @return Balloon Options
     */
    public HashMap<String, String> getBalloonOptions() {
        return mBalloonOptions;
    }

    /**
     * Creates a new marker option from given properties of an existing marker option
     *
     * @param originalMarkerOption An existing MarkerOption instance
     * @param iconRandomColorMode  True if marker color mode is random, false otherwise
     * @param markerColor          Color of the marker
     * @return A new MarkerOption
     */
    private static MarkerOptions createMarkerOptions(MarkerOptions originalMarkerOption,
            boolean iconRandomColorMode, float markerColor) {
        MarkerOptions newMarkerOption = new MarkerOptions();
        newMarkerOption.rotation(originalMarkerOption.getRotation());
        newMarkerOption.anchor(originalMarkerOption.getAnchorU(), originalMarkerOption.getAnchorV());
        if (iconRandomColorMode) {
            float hue = getHueValue(computeRandomColor((int) markerColor));
            originalMarkerOption.icon(BitmapDescriptorFactory.defaultMarker(hue));
        }
        newMarkerOption.icon(originalMarkerOption.getIcon());
        return newMarkerOption;
    }

    /**
     * Creates a new PolylineOption from given properties of an existing PolylineOption
     * @param originalPolylineOption An existing PolylineOption instance
     * @return A new PolylineOption
     */
    private static PolylineOptions createPolylineOptions (PolylineOptions originalPolylineOption) {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(originalPolylineOption.getColor());
        polylineOptions.width(originalPolylineOption.getWidth());
        return polylineOptions;
    }

    /**
     *Creates a new PolygonOption from given properties of an existing PolygonOption
     * @param originalPolygonOption An existing PolygonOption instance
     * @param isFill Whether the fill for a polygon is set
     * @param isOutline Whether the outline for a polygon is set
     * @return  A new PolygonOption
     */
    private static PolygonOptions createPolygonOptions (PolygonOptions originalPolygonOption,
            boolean isFill, boolean isOutline) {
        PolygonOptions polygonOptions = new PolygonOptions();
        if (isFill) {
            polygonOptions.fillColor(originalPolygonOption.getFillColor());
        }
        if (isOutline) {
            polygonOptions.strokeColor(originalPolygonOption.getStrokeColor());
            polygonOptions.strokeWidth(originalPolygonOption.getStrokeWidth());
        }
        return polygonOptions;
    }

    /**
     * Gets a MarkerOption
     *
     * @return  A new MarkerOption
     */
    public MarkerOptions getMarkerOptions() {
        return createMarkerOptions(mMarkerOptions, isIconRandomColorMode(), mMarkerColor);
    }

    /**
     * Gets a PolylineOption
     *
     * @return new PolylineOptions
     */
    public PolylineOptions getPolylineOptions() {
        return createPolylineOptions(mPolylineOptions);
    }

    /**
     * Gets a PolygonOption
     *
     * @return new PolygonOptions
     */
    public PolygonOptions getPolygonOptions() {
        return createPolygonOptions(mPolygonOptions, mFill, mOutline);
    }

    /**
     * Computes a random color given an integer. Algorithm to compute the random color can be
     * found in https://developers.google.com/kml/documentation/kmlreference#colormode
     *
     * @param color Color represented as an integer
     * @return Integer representing a random color
     */
     public static int computeRandomColor(int color) {
        Random random = new Random();
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        //Random number can only be computed in range [0, n)
        if (red != 0) {
            red = random.nextInt(red);
        }
        if (blue != 0) {
            blue = random.nextInt(blue);
        }
        if (green != 0) {
            green = random.nextInt(green);
        }
        return Color.rgb(red, green, blue);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Style").append("{");
        sb.append("\n balloon options=").append(mBalloonOptions);
        sb.append(",\n fill=").append(mFill);
        sb.append(",\n outline=").append(mOutline);
        sb.append(",\n icon url=").append(mIconUrl);
        sb.append(",\n scale=").append(mScale);
        sb.append(",\n style id=").append(mStyleId);
        sb.append("\n}\n");
        return sb.toString();
    }
}
