package com.google.maps.android.geojsonkmlabs.geojson;

import com.google.maps.android.geojsonkmlabs.Geometry;
import com.google.maps.android.geojsonkmlabs.MultiGeometry;

import java.util.ArrayList;
import java.util.List;

/**
 * A GeoJsonMultiLineString geometry contains a number of {@link GeoJsonLineString}s.
 */
public class GeoJsonMultiLineString extends MultiGeometry {
    /**
     * Creates a new GeoJsonMultiLineString object
     *
     * @param geoJsonLineStrings list of GeoJsonLineStrings to store
     */
    public GeoJsonMultiLineString(List<GeoJsonLineString> geoJsonLineStrings) {
        super(geoJsonLineStrings);
        setGeometryType("MultiLineString");
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
     * Gets a list of GeoJsonLineStrings
     *
     * @return list of GeoJsonLineStrings
     */
    public List<GeoJsonLineString> getLineStrings() {
        List<Geometry> geometryList = getGeometryObject();
        ArrayList<GeoJsonLineString> geoJsonLineStrings = new ArrayList<GeoJsonLineString>();
        for (Geometry geometry : geometryList) {
            GeoJsonLineString lineString = (GeoJsonLineString) geometry;
            geoJsonLineStrings.add(lineString);
        }

        return geoJsonLineStrings;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getGeometryType()).append("{");
        sb.append("\n LineStrings=").append(getGeometryObject());
        sb.append("\n}\n");
        return sb.toString();
    }

}
