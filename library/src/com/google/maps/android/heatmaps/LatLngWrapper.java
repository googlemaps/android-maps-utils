package com.google.maps.android.heatmaps;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;
import com.google.maps.android.quadtree.PointQuadTree;

/**
 * A wrapper class that can be used in a PointQuadTree
 * Created from a LatLng and optional intensity: point coordinates of the LatLng and the intensity
 * value can be accessed from it later.
 */
public class LatLngWrapper implements PointQuadTree.Item {

    /**
     * Default intensity to use when intensity not specified
     */
    public static final double DEFAULT_INTENSITY = 10;

    /**
     * Projection to use for points
     * Converts LatLng to (x, y) coordinates using a SphericalMercatorProjection
     */
    public static final SphericalMercatorProjection mProjection =
            new SphericalMercatorProjection(HeatmapConstants.HEATMAP_TILE_SIZE);

    private Point mPoint;

    private double mIntensity;

    /**
     * Constructor
     * @param latLng LatLng to add to wrapper
     * @param intensity Intensity to use: should be greater than 0
     *                  Default value is 10.
     *                  This represents the "importance" or "value" of this particular point
     *                  Higher intensity values map to higher colours.
     *                  Intensity is additive: having two points of intensity 10 at the same
     *                      location is identical to having one of intensity 20.
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
        this(latLng, DEFAULT_INTENSITY);
    }

    public Point getPoint() { return mPoint; }

    public double getIntensity() { return mIntensity; }

}