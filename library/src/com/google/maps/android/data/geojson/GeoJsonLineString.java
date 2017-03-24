package com.google.maps.android.data.geojson;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.data.LineString;

import java.util.List;

/**
 * A GeoJsonLineString geometry represents a number of connected {@link
 * com.google.android.gms.maps.model.LatLng}s.
 */
public class GeoJsonLineString extends LineString {
    private final List<Double> mAltitudes;

    /**
     * Creates a new GeoJsonLineString object
     *
     * @param coordinates list of coordinates of GeoJsonLineString to store
     */
    public GeoJsonLineString(List<LatLng> coordinates) {
        this(coordinates, null);
    }

    /**
     * Creates a new GeoJsonLineString object
     *
     * @param coordinates array of coordinates
     * @param altitudes array of altitudes
     */
    public GeoJsonLineString(List<LatLng> coordinates, List<Double> altitudes) {
        super(coordinates);

        this.mAltitudes = altitudes;
    }

    /**
     * Gets the type of geometry. The type of geometry conforms to the GeoJSON 'type'
     * specification.
     *
     * @return type of geometry
     */
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

    /**
     * Gets the altitudes of the GeoJsonLineString
     *
     * @return list of altitudes of the GeoJsonLineString
     */
    public List <Double> getAltitudes() {
        return mAltitudes;
    }
}
