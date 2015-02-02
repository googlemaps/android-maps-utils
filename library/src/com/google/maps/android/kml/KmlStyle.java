package com.google.maps.android.kml;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.graphics.Color;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Represents the defined styles in the KML document
 */
public class KmlStyle {

    private final static int HSV_VALUES = 3;

    private final static int HUE_VALUE = 0;

    private final static int INITIAL_SCALE = 1;

    private final MarkerOptions mMarkerOptions;

    private final PolylineOptions mPolylineOptions;

    private final PolygonOptions mPolygonOptions;

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

    private int mMarkerColor;


    /**
     * Creates a new Style object
     */
    public KmlStyle() {
        mStyleId = null;
        mMarkerOptions = new MarkerOptions();
        mPolylineOptions = new PolylineOptions();
        mPolygonOptions = new PolygonOptions();
        mBalloonOptions = new HashMap<String, String>();
        mStylesSet = new HashSet<String>();
        mScale = INITIAL_SCALE;
        mMarkerColor = Color.parseColor("#ffffff");
        mIconRandomColorMode = false;
        mLineRandomColorMode = false;
        mPolyRandomColorMode = false;
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
     * Checks if a given style has been set
     *
     * @param style style to check if set
     * @return true if style was set, false otherwise
     */
    public boolean isStyleSet(String style) {
        return mStylesSet.contains(style);
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
        mStylesSet.add("iconScale");
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
     * Gets whether the style has balloonstyle properties
     *
     * @return true if this style has balloonstyle properties, false otherwise
     */
    public boolean hasBalloonStyle() {
        return mBalloonOptions.size() > 0;
    }

    /**
     * Sets whether the Polygon will have an outline
     *
     * @param outline true if there is an outline, false if no outline
     */
    public void setOutline(boolean outline) {
        mOutline = outline;
        mStylesSet.add("outline");
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
        mStylesSet.add("iconUrl");
    }

    /**
     * Sets the fill color for Polygons
     *
     * @param color fill color to set
     */
    public void setFillColor(String color) {
        // Add # to allow for mOutline color to be parsed correctly
        mPolygonOptions.fillColor(Color.parseColor("#" + color));
        mStylesSet.add("fillColor");
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
        mMarkerColor = integerColor;
        // make hexadecimal representation into hsv values, store in array
        Color.colorToHSV(integerColor, hsvValues);
        // first element is the hue value
        float hue = hsvValues[HUE_VALUE];
        mMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(hue));
        mStylesSet.add("markerColor");
    }

    /**
     * Sets the heading for Points. This is also known as rotation.
     *
     * @param heading heading to set
     */
    public void setHeading(float heading) {
        mMarkerOptions.rotation(heading);
        mStylesSet.add("heading");
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
        mStylesSet.add("hotSpot");
    }

    public void setIconColorMode(String colorMode) {
        mIconRandomColorMode = colorMode.equals("random");
        mStylesSet.add("iconColorMode");
    }

    public boolean isIconRandomColorMode() {
        return mIconRandomColorMode;
    }

    public void setLineColorMode(String colorMode) {
        mLineRandomColorMode = colorMode.equals("random");
        mStylesSet.add("lineColorMode");
    }

    public boolean isLineRandomColorMode() {
        return mLineRandomColorMode;
    }

    public void setPolyColorMode(String colorMode) {
        mPolyRandomColorMode = colorMode.equals("random");
        mStylesSet.add("polyColorMode");
    }

    public boolean isPolyRandomColorMode() {
        return mPolyRandomColorMode;
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
        mStylesSet.add("outlineColor");
    }

    /**
     * Sets the width of the outline for Polylines and Polygons
     *
     * @param width width of outline to set
     */
    public void setWidth(Float width) {
        mPolylineOptions.width(width);
        mPolygonOptions.strokeWidth(width);
        mStylesSet.add("width");
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
        if (isIconRandomColorMode()) {
            int randomColor = KmlRenderer.computeRandomColor(mMarkerColor);
            float[] hsvValues = new float[HSV_VALUES];
            // make hexadecimal representation into hsv values, store in array
            Color.colorToHSV(randomColor, hsvValues);
            // first element is the hue value
            float hue = hsvValues[HUE_VALUE];
            mMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(hue));
        }
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
        sb.append(",\n fill=").append(mFill);
        sb.append(",\n outline=").append(mOutline);
        sb.append(",\n icon url=").append(mIconUrl);
        sb.append(",\n scale=").append(mScale);
        sb.append(",\n style id=").append(mStyleId);
        sb.append("\n}\n");
        return sb.toString();
    }
}
