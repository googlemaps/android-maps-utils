package com.google.maps.android.geojson;

import com.google.android.gms.maps.model.PolygonOptions;

import java.util.Arrays;
import java.util.Observable;

/**
 * A class that allows for GeoJsonPolygon objects to be styled and for these styles to be
 * translated into a PolygonOptions object. {@see
 * <a href="https://developer.android.com/reference/com/google/android/gms/maps/model/PolygonOptions.html">
 * PolygonOptions docs</a> for more details about the options.}
 */
public class GeoJsonPolygonStyle extends Observable implements GeoJsonStyle {

    private final static String[] GEOMETRY_TYPE = {"Polygon", "MultiPolygon", "GeometryCollection"};

    private final PolygonOptions mPolygonOptions;

    /**
     * Creates a new PolygonStyle object
     */
    public GeoJsonPolygonStyle() {
        mPolygonOptions = new PolygonOptions();
    }

    /** {@inheritDoc} */
    @Override
    public String[] getGeometryType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets the fill color of the GeoJsonPolygon as a 32-bit ARGB color
     *
     * @return fill color of the GeoJsonPolygon
     */
    public int getFillColor() {
        return mPolygonOptions.getFillColor();
    }

    /**
     * Sets the fill color of the GeoJsonPolygon as a 32-bit ARGB color
     *
     * @param fillColor fill color value of the GeoJsonPolygon
     */
    public void setFillColor(int fillColor) {
        mPolygonOptions.fillColor(fillColor);
        styleChanged();
    }

    /**
     * Gets whether the GeoJsonPolygon is geodesic
     *
     * @return true if GeoJsonPolygon is geodesic, false if not geodesic
     */
    public boolean isGeodesic() {
        return mPolygonOptions.isGeodesic();
    }

    /**
     * Sets whether the GeoJsonPolygon is geodesic
     *
     * @param geodesic true if GeoJsonPolygon is geodesic, false if not geodesic
     */
    public void setGeodesic(boolean geodesic) {
        mPolygonOptions.geodesic(geodesic);
        styleChanged();
    }

    /**
     * Gets the stroke color of the GeoJsonPolygon as a 32-bit ARGB color
     *
     * @return stroke color of the GeoJsonPolygon
     */
    public int getStrokeColor() {
        return mPolygonOptions.getStrokeColor();
    }

    /**
     * Sets the stroke color of the GeoJsonPolygon as a 32-bit ARGB color
     *
     * @param strokeColor stroke color value of the GeoJsonPolygon
     */
    public void setStrokeColor(int strokeColor) {
        mPolygonOptions.strokeColor(strokeColor);
        styleChanged();
    }

    /**
     * Gets the stroke width of the GeoJsonPolygon in screen pixels
     *
     * @return stroke width of the GeoJsonPolygon
     */
    public float getStrokeWidth() {
        return mPolygonOptions.getStrokeWidth();
    }

    /**
     * Sets the stroke width of the GeoJsonPolygon in screen pixels
     *
     * @param strokeWidth stroke width value of the GeoJsonPolygon
     */
    public void setStrokeWidth(float strokeWidth) {
        mPolygonOptions.strokeWidth(strokeWidth);
        styleChanged();
    }

    /**
     * Gets the z index of the GeoJsonPolygon
     *
     * @return z index of the GeoJsonPolygon
     */
    public float getZIndex() {
        return mPolygonOptions.getZIndex();
    }

    /**
     * Sets the z index of the GeoJsonPolygon
     *
     * @param zIndex z index value of the GeoJsonPolygon
     */
    public void setZIndex(float zIndex) {
        mPolygonOptions.zIndex(zIndex);
        styleChanged();
    }

    /**
     * Gets whether the GeoJsonPolygon is visible
     *
     * @return true if GeoJsonPolygon is visible, false if not visible
     */
    @Override
    public boolean isVisible() {
        return mPolygonOptions.isVisible();
    }

    /**
     * Sets whether the GeoJsonPolygon is visible
     *
     * @param visible true if GeoJsonPolygon is visible, false if not visible
     */
    @Override
    public void setVisible(boolean visible) {
        mPolygonOptions.visible(visible);
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
     * Gets a new PolygonOptions object containing styles for the GeoJsonPolygon
     *
     * @return new PolygonOptions object
     */
    public PolygonOptions toPolygonOptions() {
        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.fillColor(mPolygonOptions.getFillColor());
        polygonOptions.geodesic(mPolygonOptions.isGeodesic());
        polygonOptions.strokeColor(mPolygonOptions.getStrokeColor());
        polygonOptions.strokeWidth(mPolygonOptions.getStrokeWidth());
        polygonOptions.visible(mPolygonOptions.isVisible());
        polygonOptions.zIndex(mPolygonOptions.getZIndex());
        return polygonOptions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PolygonStyle{");
        sb.append("\n geometry type=").append(Arrays.toString(GEOMETRY_TYPE));
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
