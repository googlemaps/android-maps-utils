package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by juliawong on 12/29/14.
 *
 * A class that allows for Point objects to be styled and for these styles to be translated into a
 * MarkerOptions object
 */
public class PointStyle implements Style {

    private final static String GEOMETRY_TYPE_REGEX = "Point|MultiPoint";

    private MarkerOptions mMarkerOptions;

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
     * Gets the alpha
     *
     * @return alpha
     */
    public float getAlpha() {
        return mMarkerOptions.getAlpha();
    }

    /**
     * Sets the alpha
     *
     * @param alpha alpha value
     */
    public void setAlpha(float alpha) {
        mMarkerOptions.alpha(alpha);
    }

    /**
     * Gets the Anchor U coordinate
     *
     * @return Anchor U coordinate
     */
    public float getAnchorU() {
        return mMarkerOptions.getAnchorU();
    }

    /**
     * Gets the Anchor V coordinate
     *
     * @return Anchor V coordinate
     */
    public float getAnchorV() {
        return mMarkerOptions.getAnchorV();
    }

    /**
     * Sets the Anchor U and V coordinates
     *
     * @param anchorU Anchor U coordinate
     * @param anchorV Anchor V coordinate
     */
    public void setAnchor(float anchorU, float anchorV) {
        mMarkerOptions.anchor(anchorU, anchorV);
    }

    /**
     * Gets whether the Point is draggable
     *
     * @return true if draggable, false if not draggable
     */
    public boolean isDraggable() {
        return mMarkerOptions.isDraggable();
    }

    /**
     * Sets the Point to be draggable
     *
     * @param draggable true if draggable, false if not draggable
     */
    public void setDraggable(boolean draggable) {
        mMarkerOptions.draggable(draggable);
    }

    /**
     * Gets whether the Point is flat
     *
     * @return true if flat, false if not flat
     */
    public boolean isFlat() {
        return mMarkerOptions.isFlat();
    }

    /**
     * Sets the Point to be flat
     *
     * @param flat true if flat, false if not flat
     */
    public void setFlat(boolean flat) {
        mMarkerOptions.flat(flat);
    }

    /**
     * Gets the info window anchor U coordinate
     *
     * @return info window anchor U coordinate
     */
    public float getInfoWindowAnchorU() {
        return mMarkerOptions.getInfoWindowAnchorU();
    }

    /**
     * Gets the info window anchor V coordinate
     *
     * @return info window anchor V coordinate
     */
    public float getInfoWindowAnchorV() {
        return mMarkerOptions.getInfoWindowAnchorV();
    }

    /**
     * Sets the info window anchor U and V coordinates
     *
     * @param infoWindowAnchorU info window anchor U coordinate
     * @param infoWindowAnchorV info window anchor V coordinate
     */
    public void setInfoWindowAnchor(float infoWindowAnchorU, float infoWindowAnchorV) {

        mMarkerOptions.infoWindowAnchor(infoWindowAnchorU, infoWindowAnchorV);
    }

    /**
     * Gets the rotation
     *
     * @return rotation
     */
    public float getRotation() {
        return mMarkerOptions.getRotation();
    }

    /**
     * Sets the rotation
     *
     * @param rotation rotation value
     */
    public void setRotation(float rotation) {
        mMarkerOptions.rotation(rotation);
    }

    /**
     * Gets the snippet
     *
     * @return snippet
     */
    public String getSnippet() {
        return mMarkerOptions.getSnippet();
    }

    /**
     * Sets the snippet
     *
     * @param snippet sets the snippet value
     */
    public void setSnippet(String snippet) {
        mMarkerOptions.snippet(snippet);
    }

    /**
     * Gets the title
     *
     * @return title
     */
    public String getTitle() {
        return mMarkerOptions.getTitle();
    }

    /**
     * Sets the title
     *
     * @param title title value
     */
    public void setTitle(String title) {
        mMarkerOptions.title(title);
    }

    /**
     * Gets whether the Point is visible
     *
     * @return true if visible, false if not visible
     */
    public boolean isVisible() {
        return mMarkerOptions.isVisible();
    }

    /**
     * Sets whether the point is visible
     *
     * @param visible true if visible, false if not visible
     */
    public void setVisible(boolean visible) {
        mMarkerOptions.visible(visible);
    }

    /**
     * Gets the MarkerOptions object
     *
     * @return MarkerOptions object
     */
    public MarkerOptions getMarkerOptions() {
        return mMarkerOptions;
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
