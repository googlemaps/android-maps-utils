package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.PolygonOptions;

/**
 * Created by juliawong on 12/29/14.
 */
public class PolygonStyle implements Style {

    private PolygonOptions mPolygonOptions;

    private int mFillColor;

    private boolean mGeodesic;

    private int mStrokeColor;

    private float mStrokeWidth;

    private boolean mVisible;

    private float mZIndex;

    public int getFillColor() {
        return mFillColor;
    }

    public void setFillColor(int fillColor) {
        mFillColor = fillColor;
        mPolygonOptions.fillColor(mFillColor);
    }

    public boolean isGeodesic() {
        return mGeodesic;
    }

    public void setGeodesic(boolean geodesic) {
        mGeodesic = geodesic;
        mPolygonOptions.geodesic(mGeodesic);
    }

    public int getStrokeColor() {
        return mStrokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        mStrokeColor = strokeColor;
        mPolygonOptions.strokeColor(mStrokeColor);
    }

    public float getStrokeWidth() {
        return mStrokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        mStrokeWidth = strokeWidth;
        mPolygonOptions.strokeWidth(mStrokeWidth);
    }

    public float isZIndex() {
        return mZIndex;
    }

    public void setZIndex(float ZIndex) {
        mZIndex = ZIndex;
        mPolygonOptions.zIndex(mZIndex);
    }

    @Override
    public boolean isVisible() {
        return mVisible;
    }

    @Override
    public void setVisible(boolean visible) {
        mVisible = visible;
        mPolygonOptions.visible(mVisible);
    }

    public PolygonOptions getPolygonOptions() {
        return mPolygonOptions;
    }
}
