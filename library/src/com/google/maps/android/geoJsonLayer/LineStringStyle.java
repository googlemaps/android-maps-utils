package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by juliawong on 12/29/14.
 */
public class LineStringStyle implements Style {

    private final static String GEOMETRY_TYPE_REGEX = "Line|MultiLine";

    private PolylineOptions mPolylineOptions;

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
        mPolylineOptions.color(mColor);
    }

    public boolean isGeodesic() {
        return mGeodesic;
    }

    public void setGeodesic(boolean geodesic) {
        mGeodesic = geodesic;
        mPolylineOptions.geodesic(mGeodesic);
    }

    public float getWidth() {
        return mWidth;
    }

    public void setWidth(float width) {
        mWidth = width;
        mPolylineOptions.width(mWidth);
    }

    public float getZIndex() {
        return mZIndex;
    }

    public void setZIndex(float ZIndex) {
        mZIndex = ZIndex;
        mPolylineOptions.zIndex(mZIndex);
    }

    @Override
    public boolean isVisible() {
        return mVisible;
    }

    @Override
    public void setVisible(boolean visible) {
        mVisible = visible;
        mPolylineOptions.visible(mVisible);
    }

    public PolylineOptions getPolylineOptions() {
        return mPolylineOptions;
    }

    public static String getGeometryType() {
        return GEOMETRY_TYPE_REGEX;
    }
}
