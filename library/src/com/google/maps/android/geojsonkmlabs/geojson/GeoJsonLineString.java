package com.google.maps.android.geojsonkmlabs.geojson;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geojsonkmlabs.LineString;

import java.util.List;

/**
 * A GeoJsonLineString geometry represents a number of connected {@link
 * com.google.android.gms.maps.model.LatLng}s.
 */
public class GeoJsonLineString extends LineString {

    /**
     * Creates a new GeoJsonLineString object
     *
     * @param coordinates list of coordinates of GeoJsonLineString to store
     */
    public GeoJsonLineString(List<LatLng> coordinates) {
        super(coordinates);
    }

    /** {@inheritDoc} */
    public String getType() {
        return getGeometryType();
    }

    /**
     * Gets the coordinates of the GeoJsonLineString
     *
     * @return list of coordinates of the GeoJsonLineString
     */
    public List<LatLng> getCoordinates() {
        return getGeometryObject();
    }

}
