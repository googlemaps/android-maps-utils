package com.google.maps.android.geojsonkmlabs;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Polygon implements Geometry {

    private final static String GEOMETRY_TYPE = "Polygon";

    /** {@inheritDoc} */
    public String getGeometryType() {
        return GEOMETRY_TYPE;
    }

    public Object getGeometryObject() { //TODO
        return null;
    }

}
