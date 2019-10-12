package com.google.maps.android.data.geojson;

/**
 * Class used to apply styles for the
 * {@link com.google.maps.android.data.geojson.GeoJsonFeature GeoJsonFeature} objects
 */
interface GeoJsonStyle {

    /**
     * Gets the type of geometries this style can be applied to
     *
     * @return type of geometries this style can be applied to
     */
    String[] getGeometryType();

    boolean isVisible();

    void setVisible(boolean visible);

}
