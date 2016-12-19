package com.google.maps.android.geojsonkmlabs;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Polygon implements Geometry {

    private final static String GEOMETRY_TYPE = "Polygon";

    private final List<LatLng> mOuterBoundaryCoordinates;

    private final List<? extends List<LatLng>> mInnerBoundaryCoordinates;

    public Polygon(List<LatLng> outerBoundaryCoordinates, List<? extends List<LatLng>> innerBoundaryCoordinates) {
        mOuterBoundaryCoordinates = outerBoundaryCoordinates; //can be null
        mInnerBoundaryCoordinates = innerBoundaryCoordinates; //correlates to main coordinates for GJ
    }

    /** {@inheritDoc} */
    public String getGeometryType() {
        return GEOMETRY_TYPE;
    }

    public Object getGeometryObject() { //TODO
        return null;
    }

}
