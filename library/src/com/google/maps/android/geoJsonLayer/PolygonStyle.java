package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.PolygonOptions;

/**
 * Created by juliawong on 12/29/14.
 */
public class PolygonStyle implements Style {

    private final static String GEOMETRY_TYPE_REGEX = "Polygon|MultiPolygon";

    private PolygonOptions mPolygonOptions;

    private int mFillColor;

    private boolean mGeodesic;

    private int mStrokeColor;

    private float mStrokeWidth;

    private boolean mVisible;

    private float mZIndex;

    public static String getGeometryType() {
        return GEOMETRY_TYPE_REGEX;
    }

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

    public float getZIndex() {
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PolygonStyle{");
        sb.append("\n geometry type=").append(GEOMETRY_TYPE_REGEX);
        sb.append(",\n fill color=").append(mFillColor);
        sb.append(",\n geodesic=").append(mGeodesic);
        sb.append(",\n stroke color=").append(mStrokeColor);
        sb.append(",\n visible=").append(mVisible);
        sb.append(",\n z index=").append(mZIndex);
        sb.append("\n}\n");
        return sb.toString();
    }
}
