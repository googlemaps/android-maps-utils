package com.google.maps.android.kml;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.graphics.Color;

import java.util.HashMap;

/**
 * Represents the defined styles in the KML document
 */
public class KmlStyle {

    private final static int NORMAL_COLOR_MODE = 0;

    private final static int RANDOM_COLOR_MODE = 1;

    private final static int HSV_VALUES = 3;

    private final static int HUE_VALUE = 0;

    private final MarkerOptions mMarkerOptions;

    private final PolylineOptions mPolylineOptions;

    private final PolygonOptions mPolygonOptions;

    private final HashMap<String, String> mBalloonOptions;

    private boolean mFill = true;

    private boolean mOutline = true;

    private String mIconUrl;

    private double mScale;

    private final HashMap<String, Integer> mColorModeOptions;

    private String mStyleId;

    /**
     * Creates a new Style object
     */
    public KmlStyle() {
        mStyleId = null;
        mMarkerOptions = new MarkerOptions();
        mPolylineOptions = new PolylineOptions();
        mPolygonOptions = new PolygonOptions();
        mBalloonOptions = new HashMap<String, String>();
        mColorModeOptions = new HashMap<String, Integer>();
        mScale = 1.0;
    }

    /**
     * @return the string representing a style id, null otherwise
     */
    public String getStyleId() {
        return mStyleId;
    }

    /**
     * @param styleId style id for this style
     */
    public void setStyleId(String styleId) {
        mStyleId = styleId;
    }

    /**
     * Get whether the Polygon has a fill
     *
     * @return true if there is a fill, false if no fill
     */
    public boolean isFill() {
        return mFill;
    }

    /**
     * Sets whether the Polygon will have a fill
     *
     * @param fill true if fill is set, false if no fill
     */
    public void setFill(boolean fill) {
        mFill = fill;
    }

    /**
     * Sets the fill color for Polygons
     *
     * @param color fill color to set
     */
    public void setFillColor(String color) {
        // Add # to allow for mOutline color to be parsed correctly
        mPolygonOptions.fillColor(Color.parseColor("#" + color));
    }

    /**
     * Sets the marker color
     *
     * @param stringColor string of color to set
     */
    public void setMarkerColor(String stringColor) {
        float[] hsvValues = new float[HSV_VALUES];
        // make hexadecimal representation
        int integerColor = Color.parseColor("#" + stringColor);
        // make hexadecimal representation into hsv values, store in array
        Color.colorToHSV(integerColor, hsvValues);
        // first element is the hue value
        float hue = hsvValues[HUE_VALUE];
        mMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(hue));
    }

    /**
     * Sets the heading for Points. This is also known as rotation.
     *
     * @param heading heading to set
     */
    public void setHeading(float heading) {
        mMarkerOptions.rotation(heading);
    }

    // TODO support pixel and inset for custom marker images

    /**
     * Sets the hotspot for Points. This is also known as anchor point.
     *
     * @param x      x value of a point on the icon
     * @param y      y value of a point on the icon
     * @param xUnits units in which the x value is specified
     * @param yUnits units in which the y value is specified
     */
    public void setHotSpot(float x, float y, String xUnits, String yUnits) {
        // TODO(lavenderch): improve support for this, ignore default marker
        float xAnchor = 0.5f;
        float yAnchor = 1.0f;
        // Set x coordinate
        if (xUnits.equals("fraction")) {
            xAnchor = x;
        }
        if (yUnits.equals("fraction")) {
            yAnchor = y;
        }

        mMarkerOptions.anchor(xAnchor, yAnchor);
    }

    /**
     * Gets the icon url
     *
     * @return icon url
     */
    public String getIconUrl() {
        return mIconUrl;
    }

    /**
     * Sets the icon url
     *
     * @param iconUrl icon url to set
     */
    public void setIconUrl(String iconUrl) {
        mIconUrl = iconUrl;
        if (!mIconUrl.startsWith("http://")) {
            // Icon stored locally
            mMarkerOptions.icon(BitmapDescriptorFactory.fromPath(iconUrl));
        }
    }

    /**
     * Gets the icon scale
     *
     * @return scale value
     */
    public double getIconScale() {
        return mScale;
    }

    /**
     * Sets the icon scale
     *
     * @param scale scale value
     */
    public void setIconScale(double scale) {
        mScale = scale;
    }

    /**
     * Sets the text for the info window; no other ballonstyles are supported
     *
     * @param text text to put in an info window
     */
    public void setInfoWindow(String text) {
        mBalloonOptions.put("text", text);
    }

    /**
     * Sets the color mode; either random or normal
     *
     * @param geometryType    geometry type which colormode is applied to
     * @param colorModeString string representing the mode; either random or normal
     */
    public void setColorMode(String geometryType, String colorModeString) {
        if (colorModeString.equals("normal")) {
            mColorModeOptions.put(geometryType, NORMAL_COLOR_MODE);
        } else {
            mColorModeOptions.put(geometryType, RANDOM_COLOR_MODE);
        }
    }

    /**
     * Gets the color mode
     *
     * @param geometryType type of geometry which has a color mode
     * @return color mode, 1 for random, 0 or normal
     */
    public int getColorMode(String geometryType) {
        return mColorModeOptions.get(geometryType);
    }

    /**
     * Determines if the geometry has a random color mode
     *
     * @param geometryType geometry type which colormode is applied to
     * @return boolean value, true if the geometry has a colormode, false otherwise
     */
    public boolean hasColorMode(String geometryType) {
        return mColorModeOptions.containsKey(geometryType);
    }

    /**
     * Gets whether the Polygon has an outline
     *
     * @return true if Polygon has an outline, false if no outline
     */
    public boolean isOutline() {
        return mOutline;
    }

    /**
     * Sets whether the Polygon will have an outline
     *
     * @param outline true if there is an outline, false if no outline
     */
    public void setOutline(boolean outline) {
        mOutline = outline;
    }

    /**
     * Sets outline color for Polylines and Polygons
     *
     * @param color outline color to set
     */
    public void setOutlineColor(String color) {
        // Add # to allow for mOutline color to be parsed correctly
        mPolylineOptions.color(Color.parseColor("#" + color));
        mPolygonOptions.strokeColor(Color.parseColor("#" + color));
    }

    /**
     * Sets the width of the outline for Polylines and Polygons
     *
     * @param width width of outline to set
     */
    public void setWidth(Float width) {
        mPolylineOptions.width(width);
        mPolygonOptions.strokeWidth(width);
    }

    /**
     * Creates a new MarkerOptions object
     *
     * @return new MarkerOptions
     */
    public MarkerOptions getMarkerOptions() {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.rotation(mMarkerOptions.getRotation());
        markerOptions.icon(mMarkerOptions.getIcon());
        return markerOptions;
    }

    public HashMap<String, String> getBalloonOptions() {
        return mBalloonOptions;
    }

    /**
     * Creates a new PolylineOptions object
     *
     * @return new PolylineOptions
     */
    public PolylineOptions getPolylineOptions() {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(mPolylineOptions.getColor());
        polylineOptions.width(mPolylineOptions.getWidth());
        return polylineOptions;
    }

    /**
     * Creates a new PolygonOptions object
     *
     * @return new PolygonOptions
     */
    public PolygonOptions getPolygonOptions() {
        PolygonOptions polygonOptions = new PolygonOptions();
        if (mFill) {
            polygonOptions.fillColor(mPolygonOptions.getFillColor());
        }
        if (mOutline) {
            polygonOptions.strokeColor(mPolygonOptions.getStrokeColor());
            polygonOptions.strokeWidth(mPolygonOptions.getStrokeWidth());
        }
        return polygonOptions;
    }
}
