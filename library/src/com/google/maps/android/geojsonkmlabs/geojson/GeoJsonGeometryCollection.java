package com.google.maps.android.geojsonkmlabs.geojson;
import com.google.maps.android.geojsonkmlabs.Geometry;
import com.google.maps.android.geojsonkmlabs.MultiGeometry;
import java.util.List;

/**
 * A GeoJsonGeometryCollection geometry contains a number of GeoJsonGeometry objects.
 */
public class GeoJsonGeometryCollection extends MultiGeometry {
    /**
     * Creates a new GeoJsonGeometryCollection object
     *
     * @param geometries array of GeoJsonGeometry objects to add to the GeoJsonGeometryCollection
     */
    public GeoJsonGeometryCollection(List<Geometry> geometries) {
        super(geometries);
        setGeometryType("GeometryCollection");
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
     * Gets the stored GeoJsonGeometry objects
     *
     * @return stored GeoJsonGeometry objects
     */
    public List<Geometry> getGeometries() { return getGeometryObject(); }
}