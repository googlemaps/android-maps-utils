/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
public class WeightedLatLng implements PointQuadTree.Item {

    /**
     * Default intensity to use when intensity not specified
     */
    public static final double DEFAULT_INTENSITY = 1;

    /**
     * Projection to use for points
     * Converts LatLng to (x, y) coordinates using a SphericalMercatorProjection
     */
    private static final SphericalMercatorProjection sProjection =
            new SphericalMercatorProjection(HeatmapTileProvider.WORLD_WIDTH);

    private Point mPoint;

    private double mIntensity;

    /**
     * Constructor
     *
     * @param latLng    LatLng to add to wrapper
     * @param intensity Intensity to use: should be greater than 0
     *                  Default value is 1.
     *                  This represents the "importance" or "value" of this particular point
     *                  Higher intensity values map to higher colours.
     *                  Intensity is additive: having two points of intensity 1 at the same
     *                  location is identical to having one of intensity 2.
     */
    public WeightedLatLng(LatLng latLng, double intensity) {
        mPoint = sProjection.toPoint(latLng);
        if (intensity >= 0) mIntensity = intensity;
        else mIntensity = DEFAULT_INTENSITY;
    }

    /**
     * Constructor that uses default value for intensity
     *
     * @param latLng LatLng to add to wrapper
     */
    public WeightedLatLng(LatLng latLng) {
        this(latLng, DEFAULT_INTENSITY);
    }

    public Point getPoint() {
        return mPoint;
    }

    public double getIntensity() {
        return mIntensity;
    }

}
