package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by juliawong on 12/29/14.
 *
 * A class that allows for LineString objects to be styled and for these styles to be translated
 * into a PolylineOptions object
 */
public class GeoJsonLineStringStyle implements GeoJsonStyle {

    private final static String GEOMETRY_TYPE_REGEX = "Line|MultiLine";

    private PolylineOptions mPolylineOptions;

    /**
     * Creates a new LineStringStyle object
     */
    public GeoJsonLineStringStyle() {
        mPolylineOptions = new PolylineOptions();
    }

    /**
     * Gets the type of geometries this style can be applied to
     *
     * @return type of geometries this style can be applied to
     */
    @Override
    public String getGeometryType() {
        return GEOMETRY_TYPE_REGEX;
    }

    /**
     * Gets the color
     *
     * @return color
     */
    public int getColor() {
        return mPolylineOptions.getColor();
    }

    /**
     * Sets the color
     *
     * @param color color value
     */
    public void setColor(int color) {
        mPolylineOptions.color(color);
    }

    /**
     * Gets whether the LineString is geodesic
     *
     * @return true if geodesic, false otherwise
     */
    public boolean isGeodesic() {
        return mPolylineOptions.isGeodesic();
    }

    /**
     * Sets whether the LineString is geodesic
     *
     * @param geodesic true if geodesic, false otherwise
     */
    public void setGeodesic(boolean geodesic) {
        mPolylineOptions.geodesic(geodesic);
    }

    /**
     * Gets the width
     *
     * @return width
     */
    public float getWidth() {
        return mPolylineOptions.getWidth();
    }

    /**
     * Sets the width
     *
     * @param width width value
     */
    public void setWidth(float width) {
        mPolylineOptions.width(width);
    }

    /**
     * Gets the z index
     *
     * @return z index
     */
    public float getZIndex() {
        return mPolylineOptions.getZIndex();
    }

    /**
     * Sets the z index
     *
     * @param zIndex z index value
     */
    public void setZIndex(float zIndex) {
        mPolylineOptions.zIndex(zIndex);
    }

    /**
     * Gets whether the LineString is visible
     *
     * @return true if visible, false if not visible
     */
    public boolean isVisible() {
        return mPolylineOptions.isVisible();
    }

    /**
     * Sets whether the LineString is visible
     *
     * @param visible true if visible, false if not visible
     */
    public void setVisible(boolean visible) {
        mPolylineOptions.visible(visible);
    }

    /**
     * Gets a new PolylineOptions object
     *
     * @return new PolylineOptons object
     */
    public PolylineOptions getPolylineOptions() {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(mPolylineOptions.getColor());
        polylineOptions.geodesic(mPolylineOptions.isGeodesic());
        polylineOptions.visible(mPolylineOptions.isVisible());
        polylineOptions.width(mPolylineOptions.getWidth());
        polylineOptions.zIndex(mPolylineOptions.getZIndex());
        return polylineOptions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("LineStringStyle{");
        sb.append("\n geometry type=").append(GEOMETRY_TYPE_REGEX);
        sb.append(",\n color=").append(getColor());
        sb.append(",\n geodesic=").append(isGeodesic());
        sb.append(",\n visible=").append(isVisible());
        sb.append(",\n width=").append(getWidth());
        sb.append(",\n z index=").append(getZIndex());
        sb.append("\n}\n");
        return sb.toString();
    }
}
