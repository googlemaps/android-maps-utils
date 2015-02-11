package com.google.maps.android.geojson;

/**
 * Class used to apply styles for the GeoJsonFeature objects
 */
interface GeoJsonStyle {

    /**
     * Gets the type of geometries this style can be applied to
     *
     * @return type of geometries this style can be applied to
     */
    public String[] getGeometryType();

    public boolean isVisible();

    public void setVisible(boolean visible);

}
