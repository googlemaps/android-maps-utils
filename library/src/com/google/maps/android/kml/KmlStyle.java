package com.google.maps.android.kml;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.HashMap;

/**
 * Created by lavenderc on 12/2/14.
 *
 * Represents the defined styles in the KML document
 */
public class KmlStyle {

    private final MarkerOptions mMarkerOptions;

    private final PolylineOptions mPolylineOptions;

    private final PolygonOptions mPolygonOptions;

    private HashMap<String, String> mBalloonOptions;

    private boolean mFill = true;

    private boolean mOutline = true;

    private String mIconUrl;

    private double mScale;

    private HashMap<String, Integer> mColorModeOptions;

    private static int NORMAL_COLOR_MODE = 0;

    private static int RANDOM_COLOR_MODE = 1;

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

    public void setStyleId (String styleId) {
        mStyleId = styleId;
    }

    public String getStyleId () {
        return mStyleId;
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

    public void setMarkerColor (String color) {
        float[] hsvValues = new float[3];
        Color.colorToHSV(Color.parseColor("#" + color), hsvValues);
        float hue = hsvValues[0];
        mMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(hue));
    }

    /**
     * Sets the heading for Points. This is also known as rotation
     *
     * @param heading heading to set
     */
    public void setHeading(float heading) {
        mMarkerOptions.rotation(heading);
    }

    // TODO support pixel and inset for custom marker images

    /**
     * Sets the hotspot for Points. This is also known as anchor point
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

    public void setIconScale(double scale) {
        mScale = scale;
    }

    public double getIconScale() {
        return mScale;
    }

    public void setInfoWindow(String text) {
        mBalloonOptions.put("text", text);
    }

    public void setColorMode(String geometryType, String colorModeString) {
        if (colorModeString.equals("normal")) {
            mColorModeOptions.put(geometryType, NORMAL_COLOR_MODE);
        } else {
            mColorModeOptions.put(geometryType, RANDOM_COLOR_MODE);
        }
    }

    public int getColorMode(String geometryType) {
        return mColorModeOptions.get(geometryType);
    }

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
