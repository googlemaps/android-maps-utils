package com.google.maps.android.data.geojson;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.data.Point;

/**
 * A GeoJsonPoint geometry contains a single {@link com.google.android.gms.maps.model.LatLng}.
 */
public class GeoJsonPoint extends Point {
    private final Double mAltitude;

    /**
     * Creates a new GeoJsonPoint
     *
     * @param coordinates coordinates of GeoJsonPoint to store
     */
    public GeoJsonPoint(LatLng coordinates) {
        this(coordinates, null);
    }

    /**
     * Creates a new GeoJsonPoint
     *
     * @param coordinates coordinates of the KmlPoint
     * @param altitude altitude of the KmlPoint
     */
    public GeoJsonPoint(LatLng coordinates, Double altitude) {
        super(coordinates);

        this.mAltitude = altitude;
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
     * Gets the coordinates of the GeoJsonPoint
     *
     * @return coordinates of the GeoJsonPoint
     */
    public LatLng getCoordinates() {
        return getGeometryObject();
    }

    /**
     * Gets the altitude of the GeoJsonPoint
     *
     * @return altitude of the GeoJsonPoint
     */
    public Double getAltitude() {
        return mAltitude;
    }
}