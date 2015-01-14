package com.google.maps.android.geoJsonLayer;

public interface GeoJsonStyle {
    public String getGeometryType();

    public boolean isVisible();

    public void setVisible(boolean visible);
}
