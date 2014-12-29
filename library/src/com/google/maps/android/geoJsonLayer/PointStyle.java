package com.google.maps.android.geoJsonLayer;

/**
 * Created by juliawong on 12/29/14.
 */
public class PointStyle implements Style {

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

    public float getAlpha() {
        return mAlpha;
    }

    public void setAlpha(float alpha) {
        mAlpha = alpha;
    }

    public float getAnchorU() {
        return mAnchorU;
    }

    public void setAnchorU(float anchorU) {
        mAnchorU = anchorU;
    }

    public float getAnchorV() {
        return mAnchorV;
    }

    public void setAnchorV(float anchorV) {
        mAnchorV = anchorV;
    }

    public boolean isDraggable() {
        return mDraggable;
    }

    public void setDraggable(boolean draggable) {
        mDraggable = draggable;
    }

    public boolean isFlat() {
        return mFlat;
    }

    public void setFlat(boolean flat) {
        mFlat = flat;
    }

    public float getInfoWindowAnchorU() {
        return mInfoWindowAnchorU;
    }

    public void setInfoWindowAnchorU(float infoWindowAnchorU) {
        mInfoWindowAnchorU = infoWindowAnchorU;
    }

    public float getInfoWindowAnchorV() {
        return mInfoWindowAnchorV;
    }

    public void setInfoWindowAnchorV(float infoWindowAnchorV) {
        mInfoWindowAnchorV = infoWindowAnchorV;
    }

    public float getRotation() {
        return mRotation;
    }

    public void setRotation(float rotation) {
        mRotation = rotation;
    }

    public String getSnippet() {
        return mSnippet;
    }

    public void setSnippet(String snippet) {
        mSnippet = snippet;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    @Override
    public boolean isVisible() {
        return mVisible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.mVisible = visible;
    }
}
