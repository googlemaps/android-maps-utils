package com.google.maps.android.heatmaps;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;
import com.google.maps.android.quadtree.PointQuadTree;

/**
 * Created by minicat on 12/13/13.
 */
public class LatLngWrapper implements PointQuadTree.Item {
    private Point mPoint;

    private double mIntensity;

    public LatLngWrapper(LatLng latLng, double intensity, SphericalMercatorProjection projection) {
        mPoint = projection.toPoint(latLng);
        mIntensity = intensity;
    }
    public Point getPoint() { return mPoint; }

    public double getIntensity() { return mIntensity; }
}