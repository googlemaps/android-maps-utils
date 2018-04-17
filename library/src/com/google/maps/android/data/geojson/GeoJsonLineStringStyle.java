package com.google.maps.android.data.geojson;

import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.data.Style;

import java.util.Arrays;
import java.util.List;

/**
 * A class that allows for GeoJsonLineString objects to be styled and for these styles to be
 * translated into a PolylineOptions object. {@see
 * <a href="https://developer.android.com/reference/com/google/android/gms/maps/model/PolylineOptions.html">
 * PolylineOptions docs</a> for more details about the options.}
 */
public class GeoJsonLineStringStyle extends Style implements GeoJsonStyle {

    private final static String[] GEOMETRY_TYPE = {"LineString", "MultiLineString",
            "GeometryCollection"};

    /**
     * Creates a new LineStringStyle object
     */
    public GeoJsonLineStringStyle() {
        mPolylineOptions = new PolylineOptions();
    }

    /** {@inheritDoc} */
    @Override
    public String[] getGeometryType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets the color of the GeoJsonLineString as a 32-bit ARGB color
     *
     * @return color of the GeoJsonLineString
     */
    public int getColor() {
        return mPolylineOptions.getColor();
    }

    /**
     * Sets the color of the GeoJsonLineString as a 32-bit ARGB color
     *
     * @param color color value of the GeoJsonLineString
     */
    public void setColor(int color) {
        mPolylineOptions.color(color);
        styleChanged();
    }
    
    /**
     * Gets the clickability setting for this Options object
     * 
     * @return true if the GeoJsonLineString is clickable; false if it is not
     */
    public boolean isClickable() {
        return mPolylineOptions.isClickable();
    }
    
    /**
     * Specifies whether this GeoJsonLineString is clickable
     * 
     * @param clickable  - new clickability setting for the GeoJsonLineString
     */
    public void setClickable(boolean clickable) {
        mPolylineOptions.clickable(clickable);
        styleChanged();
    }

    /**
     * Gets whether the GeoJsonLineString is geodesic
     *
     * @return true if GeoJsonLineString is geodesic, false otherwise
     */
    public boolean isGeodesic() {
        return mPolylineOptions.isGeodesic();
    }

    /**
     * Sets whether the GeoJsonLineString is geodesic
     *
     * @param geodesic true if GeoJsonLineString is geodesic, false otherwise
     */
    public void setGeodesic(boolean geodesic) {
        mPolylineOptions.geodesic(geodesic);
        styleChanged();
    }

    /**
     * Gets the width of the GeoJsonLineString in screen pixels
     *
     * @return width of the GeoJsonLineString
     */
    public float getWidth() {
        return mPolylineOptions.getWidth();
    }

    /**
     * Sets the width of the GeoJsonLineString in screen pixels
     *
     * @param width width value of the GeoJsonLineString
     */
    public void setWidth(float width) {
        setLineStringWidth(width);
        styleChanged();
    }

    /**
     * Gets the z index of the GeoJsonLineString
     *
     * @return z index of the GeoJsonLineString
     */
    public float getZIndex() {
        return mPolylineOptions.getZIndex();
    }

    /**
     * Sets the z index of the GeoJsonLineString
     *
     * @param zIndex z index value of the GeoJsonLineString
     */
    public void setZIndex(float zIndex) {
        mPolylineOptions.zIndex(zIndex);
        styleChanged();
    }

    /**
     * Gets whether the GeoJsonLineString is visible
     *
     * @return true if the GeoJsonLineString visible, false if not visible
     */
    @Override
    public boolean isVisible() {
        return mPolylineOptions.isVisible();
    }

    /**
     * Sets whether the GeoJsonLineString is visible
     *
     * @param visible true if the GeoJsonLineString is visible, false if not visible
     */
    @Override
    public void setVisible(boolean visible) {
        mPolylineOptions.visible(visible);
        styleChanged();
    }

    /**
     * Notifies the observers, GeoJsonFeature objects, that the style has changed. Indicates to the
     * GeoJsonFeature that it should check whether a redraw is needed for the feature.
     */
    private void styleChanged() {
        setChanged();
        notifyObservers();
    }

    /**
     * Gets a new PolylineOptions object containing styles for the GeoJsonLineString
     *
     * @return new PolylineOptions object
     */
    public PolylineOptions toPolylineOptions() {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(mPolylineOptions.getColor());
        polylineOptions.clickable(mPolylineOptions.isClickable());
        polylineOptions.geodesic(mPolylineOptions.isGeodesic());
        polylineOptions.visible(mPolylineOptions.isVisible());
        polylineOptions.width(mPolylineOptions.getWidth());
        polylineOptions.zIndex(mPolylineOptions.getZIndex());
        polylineOptions.pattern(getPattern());
        return polylineOptions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("LineStringStyle{");
        sb.append("\n geometry type=").append(Arrays.toString(GEOMETRY_TYPE));
        sb.append(",\n color=").append(getColor());
        sb.append(",\n clickable=").append(isClickable());
        sb.append(",\n geodesic=").append(isGeodesic());
        sb.append(",\n visible=").append(isVisible());
        sb.append(",\n width=").append(getWidth());
        sb.append(",\n z index=").append(getZIndex());
        sb.append(",\n pattern=").append(getPattern());
        sb.append("\n}\n");
        return sb.toString();
    }

    /**
     * Gets the pattern of the GeoJsonLineString
     *
     * @return  line style of GeoJsonLineString
     */
    public List<PatternItem> getPattern() {
        return mPolylineOptions.getPattern();
    }

    /**
     * Sets the pattern of the GeoJsonLineString
     *
     * @param pattern line style of GeoJsonLineString
     */
    public void setPattern(List<PatternItem> pattern) {
        mPolylineOptions.pattern(pattern);
        styleChanged();
    }

}
