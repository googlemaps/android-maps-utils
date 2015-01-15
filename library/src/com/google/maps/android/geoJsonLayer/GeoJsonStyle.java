package com.google.maps.android.geoJsonLayer;

/**
 * Class used to apply styles for the GeoJsonFeature objects
 */
public interface GeoJsonStyle {

    public String getGeometryType();

    public boolean isVisible();

    public void setVisible(boolean visible);

}
