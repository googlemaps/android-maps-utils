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

    private final static int INITIAL_SCALE = 1;

    private final MarkerOptions mMarkerOptions;

    private final PolylineOptions mPolylineOptions;

    private final PolygonOptions mPolygonOptions;

    private final HashMap<String, String> mBalloonOptions;

    private final HashMap<String, Integer> mColorModeOptions;

    private boolean mFill = true;

    private boolean mOutline = true;

    private String mIconUrl;

    private double mScale;

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
        mScale = INITIAL_SCALE;
    }

    /**
     * Sets the text for the info window; no other ballonstyles are supported
     *
     * @param text text to put in an info window
     */
    public void setInfoWindowText(String text) {
        mBalloonOptions.put("text", text);
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
    public boolean hasFill() {
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
     * Gets whether the Polygon has an outline
     *
     * @return true if Polygon has an outline, false if no outline
     */
    public boolean hasOutline() {
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
     * Gets the icon url
     *
     * @return icon url
     */
    public String getIconUrl() {
        return mIconUrl;
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

    /**
     * Sets the hotspot for Points. This is also known as anchor point.
     *
     * @param x      x value of a point on the icon
     * @param y      y value of a point on the icon
     * @param xUnits units in which the x value is specified
     * @param yUnits units in which the y value is specified
     */
    public void setHotSpot(float x, float y, String xUnits, String yUnits) {
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

    public HashMap<String, String> getBalloonOptions() {
        return mBalloonOptions;
    }

    /**
     * Creates a new MarkerOptions object
     *
     * @return new MarkerOptions
     */
    public MarkerOptions getMarkerOptions() {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.rotation(mMarkerOptions.getRotation());
        markerOptions.anchor(mMarkerOptions.getAnchorU(), mMarkerOptions.getAnchorV());

        markerOptions.icon(mMarkerOptions.getIcon());
        return markerOptions;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Style").append("{");
        sb.append("\n balloon options=").append(mBalloonOptions);
        sb.append(",\n color mode options=").append(mColorModeOptions);
        sb.append(",\n fill=").append(mFill);
        sb.append(",\n outline=").append(mOutline);
        sb.append(",\n icon url=").append(mIconUrl);
        sb.append(",\n scale=").append(mScale);
        sb.append(",\n style id=").append(mStyleId);
        sb.append("\n}\n");
        return sb.toString();
    }
}
