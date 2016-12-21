package com.google.maps.android.geojsonkmlabs;

import java.util.List;

public class MultiGeometry implements Geometry {

    private static final String GEOMETRY_TYPE = "MultiGeometry";

    private List<Geometry> mGeometries;

    public MultiGeometry(List<Geometry> geometries) {
        if (geometries == null) {
            throw new IllegalArgumentException("Geometries cannot be null");
        }
        mGeometries = geometries;
    }






    /**
     * Gets the type of geometry
     *
     * @return type of geometry
     */
    public String getGeometryType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets the stored geometry object
     *
     * @return geometry object
     */
    public List<Geometry> getGeometryObject() {
        return mGeometries;
    }

}
