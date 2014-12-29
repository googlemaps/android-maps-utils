package com.google.maps.android.geoJsonLayer;

/**
 * Created by juliawong on 12/29/14.
 */
public class PolygonStyle implements Style {

    private int mFillColor;

    private boolean mGeodesic;

    private int mStrokeColor;

    private float mStrokeWidth;

    private boolean mVisible;

    private boolean mZIndex;

    public int getFillColor() {
        return mFillColor;
    }

    public void setFillColor(int fillColor) {
        mFillColor = fillColor;
    }

    public boolean isGeodesic() {
        return mGeodesic;
    }

    public void setGeodesic(boolean geodesic) {
        mGeodesic = geodesic;
    }

    public int getStrokeColor() {
        return mStrokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        mStrokeColor = strokeColor;
    }

    public float getStrokeWidth() {
        return mStrokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        mStrokeWidth = strokeWidth;
    }

    public boolean isZIndex() {
        return mZIndex;
    }

    public void setZIndex(boolean ZIndex) {
        mZIndex = ZIndex;
    }

    @Override
    public boolean isVisible() {
        return mVisible;
    }

    @Override
    public void setVisible(boolean visible) {
        mVisible = visible;
    }
}
