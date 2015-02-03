package com.google.maps.android.geojson;

/**
 * Class used to apply styles for the GeoJsonFeature objects
 */
interface GeoJsonStyle {

    public String getGeometryType();

    public boolean isVisible();

    public void setVisible(boolean visible);

}
