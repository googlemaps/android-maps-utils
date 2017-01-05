package com.google.maps.android.geojsonkmlabs;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Observable;

public abstract class Style extends Observable {

    protected final MarkerOptions mMarkerOptions;

    protected final PolylineOptions mPolylineOptions;

    protected final PolygonOptions mPolygonOptions;

    public Style() {
        mMarkerOptions = new MarkerOptions();
        mPolylineOptions = new PolylineOptions();
        mPolygonOptions = new PolygonOptions();
    }

    /**
     * Gets the rotation of a marker in degrees clockwise about the marker's anchor
     *
     * @return rotation of the Point
     */
    public float getRotation() {
        return mMarkerOptions.getRotation();
    }

    /**
     * Sets the rotation / heading of the Point in degrees clockwise about the marker's anchor
     *
     * @param rotation Decimal representation of the rotation value of the Point
     */
    public void setMarkerRotation(float rotation) {
        mMarkerOptions.rotation(rotation);
    }

    /**
     * Sets the hotspot / anchor point of a marker
     *
     * @param x      x point of a marker position
     * @param y      y point of a marker position
     * @param xUnits units in which the x value is specified
     * @param yUnits units in which the y value is specified
     */
    public void setMarkerHotSpot(float x, float y, String xUnits, String yUnits) {
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
     * Sets the width of the LineString in screen pixels
     *
     * @param width width value of the LineString
     */
    public void setLineStringWidth(float width) {
        mPolylineOptions.width(width);
    }

    /**
     * Sets the stroke width of the Polygon in screen pixels
     *
     * @param strokeWidth stroke width value of the Polygon
     */
    public void setPolygonStrokeWidth(float strokeWidth) {
        mPolygonOptions.strokeWidth(strokeWidth);
    }

    /**
     * Sets the fill color of the Polygon as a 32-bit ARGB color
     *
     * @param fillColor fill color value of the Polygon
     */
    public void setPolygonFillColor(int fillColor) {
        mPolygonOptions.fillColor(fillColor);
    }
}