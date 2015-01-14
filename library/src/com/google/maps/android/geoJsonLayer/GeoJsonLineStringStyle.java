package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.PolylineOptions;

/**
 * A class that allows for GeoJsonLineString objects to be styled and for these styles to be
 * translated into a PolylineOptions object
 */
public class GeoJsonLineStringStyle implements GeoJsonStyle {

    private final static String GEOMETRY_TYPE_REGEX = "LineString|MultiLineString|GeometryCollection";

    private final PolylineOptions mPolylineOptions;

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
     * Gets the color of the GeoJsonLineString
     *
     * @return color of the GeoJsonLineString
     */
    public int getColor() {
        return mPolylineOptions.getColor();
    }

    /**
     * Sets the color of the GeoJsonLineString
     *
     * @param color color value of the GeoJsonLineString
     */
    public void setColor(int color) {
        mPolylineOptions.color(color);
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
    }

    /**
     * Gets the width of the GeoJsonLineString
     *
     * @return width of the GeoJsonLineString
     */
    public float getWidth() {
        return mPolylineOptions.getWidth();
    }

    /**
     * Sets the width of the GeoJsonLineString
     *
     * @param width width value of the GeoJsonLineString
     */
    public void setWidth(float width) {
        mPolylineOptions.width(width);
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
    }

    /**
     * Gets a new PolylineOptions object containing styles for the GeoJsonLineString
     *
     * @return new PolylineOptions object
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
