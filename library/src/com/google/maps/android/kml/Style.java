package com.google.maps.android.kml;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.graphics.Color;

/**
 * Created by lavenderc on 12/2/14.
 *
 * Represents the defined styles in the KML document
 */
public class Style {

    private final MarkerOptions mMarkerOptions;

    private final PolylineOptions mPolylineOptions;

    private final PolygonOptions mPolygonOptions;

    private boolean mFill = true;

    private boolean mOutline = true;

    /**
     * Creates a new Style object
     */
    public Style() {
        mMarkerOptions = new MarkerOptions();
        mPolylineOptions = new PolylineOptions();
        mPolygonOptions = new PolygonOptions();
    }

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

    public void setHeading(float heading) {
        mMarkerOptions.rotation(heading);
    }

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

    // TODO: think about whether we really need a deep copy of the Options

    /**
     * Creates a new MarkerOptions object
     *
     * @return new MarkerOptions
     */
    public MarkerOptions getMarkerOptions() {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.rotation(mMarkerOptions.getRotation());
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
}
