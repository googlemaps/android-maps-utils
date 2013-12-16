package com.google.maps.android.heatmaps;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;
import com.google.maps.android.quadtree.PointQuadTree;

/**
 * Created by minicat on 12/13/13.
 */
public class LatLngWrapper implements PointQuadTree.Item {

    /**
     * Default intensity to use when intensity not specified
     */
    public static final double DEFAULT_INTENSITY = 10;

    /**
     * Projection to use for points
     */
    public static final SphericalMercatorProjection mProjection =
            new SphericalMercatorProjection(HeatmapConstants.HEATMAP_TILE_SIZE);

    private Point mPoint;

    private double mIntensity;

    /**
     * Constructor
     * @param latLng LatLng to add to wrapper
     * @param intensity Intensity to use: should be greater than 0
     */
    public LatLngWrapper(LatLng latLng, double intensity) {
        mPoint = mProjection.toPoint(latLng);
        if (intensity >= 0) mIntensity = intensity;
        else mIntensity = DEFAULT_INTENSITY;
    }

    /**
     * Constructor that uses default value for intensity
     * @param latLng LatLng to add to wrapper
     */
    public LatLngWrapper(LatLng latLng) {
        mPoint = mProjection.toPoint(latLng);
        mIntensity = DEFAULT_INTENSITY;
    }

    public Point getPoint() { return mPoint; }

    public double getIntensity() { return mIntensity; }

}