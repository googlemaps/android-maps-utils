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

    public static String getGeometryType() {
        return GEOMETRY_TYPE_REGEX;
    }

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("LineStringStyle{");
        sb.append("\n geometry type=").append(GEOMETRY_TYPE_REGEX);
        sb.append(",\n color=").append(mColor);
        sb.append(",\n geodesic=").append(mGeodesic);
        sb.append(",\n visible=").append(mVisible);
        sb.append(",\n width=").append(mWidth);
        sb.append(",\n z index=").append(mZIndex);
        sb.append("\n}\n");
        return sb.toString();
    }
}
