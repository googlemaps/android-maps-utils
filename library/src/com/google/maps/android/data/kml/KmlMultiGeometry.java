package com.google.maps.android.data.kml;

import com.google.maps.android.data.Geometry;
import com.google.maps.android.data.MultiGeometry;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a KML MultiGeometry. Contains an array of Geometry objects.
 */
public class KmlMultiGeometry extends MultiGeometry {
    /**
     * Creates a new MultiGeometry object
     *
     * @param geometries array of Geometry objects contained in the MultiGeometry
     */
    public KmlMultiGeometry(ArrayList<Geometry> geometries) {
        super(geometries);
    }

    /**
     * Gets an ArrayList of Geometry objects
     *
     * @return ArrayList of Geometry objects
     */
    public ArrayList<Geometry> getGeometryObject() {
        List<Geometry> geometriesList = super.getGeometryObject();
        return new ArrayList<>(geometriesList);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getGeometryType()).append("{");
        sb.append("\n geometries=").append(getGeometryObject());
        sb.append("\n}\n");
        return sb.toString();
    }
}