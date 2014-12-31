package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by juliawong on 12/29/14.
 */
public class PointStyle implements Style {

    private final static String GEOMETRY_TYPE_REGEX = "Point|MultiPoint";

    private MarkerOptions mMarkerOptions;

    private float mAlpha;

    private float mAnchorU;

    private float mAnchorV;

    private boolean mDraggable;

    private boolean mFlat;

    private float mInfoWindowAnchorU;

    private float mInfoWindowAnchorV;

    private float mRotation;

    private String mSnippet;

    private String mTitle;

    private boolean mVisible;

    public static String getGeometryType() {
        return GEOMETRY_TYPE_REGEX;
    }

    public float getAlpha() {
        return mAlpha;
    }

    public void setAlpha(float alpha) {
        mAlpha = alpha;
        mMarkerOptions.alpha(mAlpha);
    }

    public float getAnchorU() {
        return mAnchorU;
    }

    public float getAnchorV() {
        return mAnchorV;
    }

    public void setAnchor(float anchorU, float anchorV) {
        mAnchorU = anchorU;
        mAnchorV = anchorV;
        mMarkerOptions.anchor(mAnchorU, mAnchorV);
    }

    public boolean isDraggable() {
        return mDraggable;
    }

    public void setDraggable(boolean draggable) {
        mDraggable = draggable;
        mMarkerOptions.draggable(mDraggable);
    }

    public boolean isFlat() {
        return mFlat;
    }

    public void setFlat(boolean flat) {
        mFlat = flat;
        mMarkerOptions.flat(mFlat);
    }

    public float getInfoWindowAnchorU() {
        return mInfoWindowAnchorU;
    }

    public float getInfoWindowAnchorV() {
        return mInfoWindowAnchorV;
    }

    public void setInfoWindowAnchor(float infoWindowAnchorU, float infoWindowAnchorV) {
        mInfoWindowAnchorU = infoWindowAnchorU;
        mInfoWindowAnchorV = infoWindowAnchorV;
        mMarkerOptions.infoWindowAnchor(mInfoWindowAnchorU, mInfoWindowAnchorV);
    }

    public float getRotation() {
        return mRotation;
    }

    public void setRotation(float rotation) {
        mRotation = rotation;
        mMarkerOptions.rotation(mRotation);
    }

    public String getSnippet() {
        return mSnippet;
    }

    public void setSnippet(String snippet) {
        mSnippet = snippet;
        mMarkerOptions.snippet(mSnippet);
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
        mMarkerOptions.title(mTitle);
    }

    @Override
    public boolean isVisible() {
        return mVisible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.mVisible = visible;
        mMarkerOptions.visible(mVisible);
    }

    public MarkerOptions getMarkerOptions() {
        return mMarkerOptions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PointStyle{");
        sb.append("\n geometry type=").append(GEOMETRY_TYPE_REGEX);
        sb.append(",\n alpha=").append(mAlpha);
        sb.append(",\n anchor U=").append(mAnchorU);
        sb.append(",\n anchor V=").append(mAnchorV);
        sb.append(",\n draggable=").append(mDraggable);
        sb.append(",\n flat=").append(mFlat);
        sb.append(",\n info window anchor U=").append(mInfoWindowAnchorU);
        sb.append(",\n info window anchor V=").append(mInfoWindowAnchorV);
        sb.append(",\n rotation=").append(mRotation);
        sb.append(",\n snippet=").append(mSnippet);
        sb.append(",\n title=").append(mTitle);
        sb.append(",\n visible=").append(mVisible);
        sb.append("\n}\n");
        return sb.toString();
    }
}
