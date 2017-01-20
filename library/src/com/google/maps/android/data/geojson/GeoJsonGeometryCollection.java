package com.google.maps.android.data.geojson;
import com.google.maps.android.data.Geometry;
import com.google.maps.android.data.MultiGeometry;
import java.util.List;

/**
 * A GeoJsonGeometryCollection geometry contains a number of GeoJson Geometry objects.
 */
public class GeoJsonGeometryCollection extends MultiGeometry {
    /**
     * Creates a new GeoJsonGeometryCollection object
     *
     * @param geometries array of Geometry objects to add to the GeoJsonGeometryCollection
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
     * Gets the stored Geometry objects
     *
     * @return stored Geometry objects
     */
    public List<Geometry> getGeometries() { return getGeometryObject(); }
}