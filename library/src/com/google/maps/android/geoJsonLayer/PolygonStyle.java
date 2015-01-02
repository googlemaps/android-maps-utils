package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.PolygonOptions;

/**
 * Created by juliawong on 12/29/14.
 *
 * A class that allows for Polygon objects to be styled and for these styles to be translated into
 * a
 * PolyognOptions object
 */
public class PolygonStyle implements Style {

    private final static String GEOMETRY_TYPE_REGEX = "Polygon|MultiPolygon";

    private PolygonOptions mPolygonOptions;

    /**
     * Creates a new PolygonStyle object
     */
    public PolygonStyle() {
        mPolygonOptions = new PolygonOptions();
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
     * Gets the fill color
     *
     * @return fill color
     */
    public int getFillColor() {
        return mPolygonOptions.getFillColor();
    }

    /**
     * Sets the fill color
     *
     * @param fillColor fill color value
     */
    public void setFillColor(int fillColor) {
        mPolygonOptions.fillColor(fillColor);
    }

    /**
     * Gets whether the object is geodesic
     *
     * @return true if geodesic, false if not geodesic
     */
    public boolean isGeodesic() {
        return mPolygonOptions.isGeodesic();
    }

    /**
     * Sets whether the object is geodesic
     *
     * @param geodesic true if geodesic, false if not geodesic
     */
    public void setGeodesic(boolean geodesic) {
        mPolygonOptions.geodesic(geodesic);
    }

    /**
     * Gets the stroke color
     *
     * @return stroke color
     */
    public int getStrokeColor() {
        return mPolygonOptions.getStrokeColor();
    }

    /**
     * Sets the stroke color
     *
     * @param strokeColor stroke color value
     */
    public void setStrokeColor(int strokeColor) {
        mPolygonOptions.strokeColor(strokeColor);
    }

    /**
     * Gets the stroke width
     *
     * @return stroke width
     */
    public float getStrokeWidth() {
        return mPolygonOptions.getStrokeWidth();
    }

    /**
     * Sets the stroke width
     *
     * @param strokeWidth stroke width value
     */
    public void setStrokeWidth(float strokeWidth) {
        mPolygonOptions.strokeWidth(strokeWidth);
    }

    /**
     * Gets the z index
     *
     * @return z index
     */
    public float getZIndex() {
        return mPolygonOptions.getZIndex();
    }

    /**
     * Sets the z index
     *
     * @param zIndex z index value
     */
    public void setZIndex(float zIndex) {
        mPolygonOptions.zIndex(zIndex);
    }

    /**
     * Gets whether the Polygon is visible
     *
     * @return true if visible, false if not visible
     */
    public boolean isVisible() {
        return mPolygonOptions.isVisible();
    }

    /**
     * Sets whether the Polygon is visible
     *
     * @param visible true if visible, false if not visible
     */
    public void setVisible(boolean visible) {
        mPolygonOptions.visible(visible);
    }

    /**
     * Gets the PolygonOptions object
     *
     * @return PolygonOptions object
     */
    public PolygonOptions getPolygonOptions() {
        return mPolygonOptions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PolygonStyle{");
        sb.append("\n geometry type=").append(GEOMETRY_TYPE_REGEX);
        sb.append(",\n fill color=").append(getFillColor());
        sb.append(",\n geodesic=").append(isGeodesic());
        sb.append(",\n stroke color=").append(getStrokeColor());
        sb.append(",\n stroke width=").append(getStrokeWidth());
        sb.append(",\n visible=").append(isVisible());
        sb.append(",\n z index=").append(getZIndex());
        sb.append("\n}\n");
        return sb.toString();
    }
}
