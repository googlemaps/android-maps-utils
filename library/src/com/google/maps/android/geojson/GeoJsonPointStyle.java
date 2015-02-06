package com.google.maps.android.geojson;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Observable;

/**
 * A class that allows for GeoJsonPoint objects to be styled and for these styles to be translated
 * into a MarkerOptions object
 */
public class GeoJsonPointStyle extends Observable implements GeoJsonStyle {

    private final static String GEOMETRY_TYPE_REGEX = "Point|MultiPoint|GeometryCollection";

    private final MarkerOptions mMarkerOptions;


    /**
     * Creates a new PointStyle object
     */
    public GeoJsonPointStyle() {
        mMarkerOptions = new MarkerOptions();
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
     * Gets the alpha of the Point
     *
     * @return alpha of the Point
     */
    public float getAlpha() {
        return mMarkerOptions.getAlpha();
    }

    /**
     * Sets the alpha of the GeoJsonPoint
     *
     * @param alpha alpha value of the GeoJsonPoint
     */
    public void setAlpha(float alpha) {
        mMarkerOptions.alpha(alpha);
        styleChanged();
    }

    /**
     * Gets the Anchor U coordinate of the GeoJsonPoint
     *
     * @return Anchor U coordinate of the GeoJsonPoint
     */
    public float getAnchorU() {
        return mMarkerOptions.getAnchorU();
    }

    /**
     * Gets the Anchor V coordinate of the GeoJsonPoint
     *
     * @return Anchor V coordinate of the GeoJsonPoint
     */
    public float getAnchorV() {
        return mMarkerOptions.getAnchorV();
    }

    /**
     * Sets the Anchor U and V coordinates of the GeoJsonPoint
     *
     * @param anchorU Anchor U coordinate of the GeoJsonPoint
     * @param anchorV Anchor V coordinate of the GeoJsonPoint
     */
    public void setAnchor(float anchorU, float anchorV) {
        mMarkerOptions.anchor(anchorU, anchorV);
        styleChanged();
    }

    /**
     * Gets whether the GeoJsonPoint is draggable
     *
     * @return true if GeoJsonPoint is draggable, false if not draggable
     */
    public boolean isDraggable() {
        return mMarkerOptions.isDraggable();
    }

    /**
     * Sets the GeoJsonPoint to be draggable
     *
     * @param draggable true if GeoJsonPoint is draggable, false if not draggable
     */
    public void setDraggable(boolean draggable) {
        mMarkerOptions.draggable(draggable);
        styleChanged();
    }

    /**
     * Gets whether the GeoJsonPoint is flat
     *
     * @return true if GeoJsonPoint is flat, false if not flat
     */
    public boolean isFlat() {
        return mMarkerOptions.isFlat();
    }

    /**
     * Sets the GeoJsonPoint to be flat
     *
     * @param flat true if GeoJsonPoint is flat, false if not flat
     */
    public void setFlat(boolean flat) {
        mMarkerOptions.flat(flat);
        styleChanged();
    }

    /**
     * Gets a bitmap image for the GeoJsonPoint
     *
     * @return bitmap descriptor for the GeoJsonPoint
     */
    public BitmapDescriptor getIcon() {
        return mMarkerOptions.getIcon();
    }

    /**
     * Sets a bitmap image for the GeoJsonPoint
     *
     * @param bitmap bitmap descriptor for the GeoJsonPoint
     */
    public void setIcon(BitmapDescriptor bitmap) {
        mMarkerOptions.icon(bitmap);
        styleChanged();
    }

    /**
     * Gets the info window anchor U coordinate of the GeoJsonPoint
     *
     * @return info window anchor U coordinate of the GeoJsonPoint
     */
    public float getInfoWindowAnchorU() {
        return mMarkerOptions.getInfoWindowAnchorU();
    }

    /**
     * Gets the info window anchor V coordinate of the GeoJsonPoint
     *
     * @return info window anchor V coordinate of the GeoJsonPoint
     */
    public float getInfoWindowAnchorV() {
        return mMarkerOptions.getInfoWindowAnchorV();
    }

    /**
     * Sets the info window anchor U and V coordinates of the GeoJsonPoint
     *
     * @param infoWindowAnchorU info window anchor U coordinate of the GeoJsonPoint
     * @param infoWindowAnchorV info window anchor V coordinate of the GeoJsonPoint
     */
    public void setInfoWindowAnchor(float infoWindowAnchorU, float infoWindowAnchorV) {
        mMarkerOptions.infoWindowAnchor(infoWindowAnchorU, infoWindowAnchorV);
        styleChanged();
    }

    /**
     * Gets the rotation of the GeoJsonPoint
     *
     * @return rotation of the GeoJsonPoint
     */
    public float getRotation() {
        return mMarkerOptions.getRotation();
    }


    /**
     * Sets the rotation of the GeoJsonPoint
     *
     * @param rotation rotation value of the GeoJsonPoint
     */
    public void setRotation(float rotation) {
        mMarkerOptions.rotation(rotation);
        styleChanged();
    }

    /**
     * Gets the snippet of the GeoJsonPoint
     *
     * @return snippet of the GeoJsonPoint
     */
    public String getSnippet() {
        return mMarkerOptions.getSnippet();
    }

    /**
     * Sets the snippet of the GeoJsonPoint
     *
     * @param snippet sets the snippet value of the GeoJsonPoint
     */
    public void setSnippet(String snippet) {
        mMarkerOptions.snippet(snippet);
        styleChanged();
    }

    /**
     * Gets the title of the GeoJsonPoint
     *
     * @return title of the GeoJsonPoint
     */
    public String getTitle() {
        return mMarkerOptions.getTitle();
    }

    /**
     * Sets the title of the GeoJsonPoint
     *
     * @param title title value of the GeoJsonPoint
     */
    public void setTitle(String title) {
        mMarkerOptions.title(title);
        styleChanged();
    }

    /**
     * Gets whether the GeoJsonPoint is visible
     *
     * @return true if GeoJsonPoint is visible, false if not visible
     */
    @Override
    public boolean isVisible() {
        return mMarkerOptions.isVisible();
    }

    /**
     * Sets whether the GeoJsonPoint is visible
     *
     * @param visible true if GeoJsonPoint is visible, false if not visible
     */
    @Override
    public void setVisible(boolean visible) {
        mMarkerOptions.visible(visible);
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
     * Gets a new MarkerOptions object containing styles for the GeoJsonPoint
     *
     * @return new MarkerOptions object
     */
    public MarkerOptions toMarkerOptions() {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.alpha(mMarkerOptions.getAlpha());
        markerOptions.anchor(mMarkerOptions.getAnchorU(), mMarkerOptions.getAnchorV());
        markerOptions.draggable(mMarkerOptions.isDraggable());
        markerOptions.flat(mMarkerOptions.isFlat());
        markerOptions.icon(mMarkerOptions.getIcon());
        markerOptions.infoWindowAnchor(mMarkerOptions.getInfoWindowAnchorU(),
                mMarkerOptions.getInfoWindowAnchorV());
        markerOptions.rotation(mMarkerOptions.getRotation());
        markerOptions.snippet(mMarkerOptions.getSnippet());
        markerOptions.title(mMarkerOptions.getTitle());
        markerOptions.visible(mMarkerOptions.isVisible());
        return markerOptions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PointStyle{");
        sb.append("\n geometry type=").append(GEOMETRY_TYPE_REGEX);
        sb.append(",\n alpha=").append(getAlpha());
        sb.append(",\n anchor U=").append(getAnchorU());
        sb.append(",\n anchor V=").append(getAnchorV());
        sb.append(",\n draggable=").append(isDraggable());
        sb.append(",\n flat=").append(isFlat());
        sb.append(",\n info window anchor U=").append(getInfoWindowAnchorU());
        sb.append(",\n info window anchor V=").append(getInfoWindowAnchorV());
        sb.append(",\n rotation=").append(getRotation());
        sb.append(",\n snippet=").append(getSnippet());
        sb.append(",\n title=").append(getTitle());
        sb.append(",\n visible=").append(isVisible());
        sb.append("\n}\n");
        return sb.toString();
    }
}
