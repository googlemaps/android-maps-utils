package com.google.maps.android.geoJsonLayer;

/**
 * Created by juliawong on 12/29/14.
 */
public class LineStringStyle implements Style {

    private int mColor;

    private boolean mGeodesic;

    private boolean mVisible;

    private float mWidth;

    private float mZIndex;

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
    }

    public boolean isGeodesic() {
        return mGeodesic;
    }

    public void setGeodesic(boolean geodesic) {
        mGeodesic = geodesic;
    }

    public float getWidth() {
        return mWidth;
    }

    public void setWidth(float width) {
        mWidth = width;
    }

    public float getZIndex() {
        return mZIndex;
    }

    public void setZIndex(float ZIndex) {
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
