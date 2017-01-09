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
     * Gets a list of GeoJsonLineStrings
     *
     * @return list of GeoJsonLineStrings
     */
    public List<GeoJsonLineString> getLineStrings() {
        //convert list of Geometry types to list of GeoJsonLineString types
        List<Geometry> geometryList = getGeometryObject();
        ArrayList<GeoJsonLineString> geoJsonLineStrings = new ArrayList<GeoJsonLineString>();
        for (Geometry geometry : geometryList) {
            GeoJsonLineString lineString = (GeoJsonLineString) geometry;
            geoJsonLineStrings.add(lineString);
        }

        return geoJsonLineStrings;
    }
}
