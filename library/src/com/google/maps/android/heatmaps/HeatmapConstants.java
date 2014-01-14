package com.google.maps.android.heatmaps;

import android.graphics.Color;

/**
 * Constants for heatmaps
 */
public class HeatmapConstants {

    /**
     * Default tile dimensions
     */
    public static final int HEATMAP_TILE_SIZE = 512;

    /**
     * Default radius for convolution
     */
    public static final int DEFAULT_HEATMAP_RADIUS = 20;

    /**
     * Default opacity of heatmap overlay
     */
    public static final double DEFAULT_HEATMAP_OPACITY = 0.7;

    /**
     * Default gradient for heatmap.
     * Copied from Javascript version.
     * Array of colors, in int form.
     */
    public static final int[] DEFAULT_HEATMAP_GRADIENT = {
            //a, r, g, b / r, g, b
            Color.argb(0, 102, 255, 0),  // green (invisible)
            Color.argb(255 / 3 * 2, 102, 255, 0),  // 2/3rds invisible
            Color.rgb(147, 255, 0),
            Color.rgb(193, 255, 0),
            Color.rgb(238, 255, 0),  // yellow
            Color.rgb(244, 227, 0),
            Color.rgb(249, 198, 0),
            Color.rgb(255, 170, 0),  // orange
            Color.rgb(255, 113, 0),
            Color.rgb(255, 57, 0),
            Color.rgb(255, 0, 0)     // red
    };

    /**
     * Default (and minimum possible) minimum zoom level at which to calculate maximum intensities
     */
    public static final int DEFAULT_MIN_ZOOM = 5;

    /**
     * Default (and maximum possible) maximum zoom level at which to calculate maximum intensities
     */
    public static final int DEFAULT_MAX_ZOOM = 9;

    /**
     * Maximum zoom level possible on a map.
     */
    public static final int MAX_ZOOM_LEVEL = 22;

    /**
     * Minimum radius value.
     */
    public static final int MIN_RADIUS = 10;

    /**
     * Maximum radius value.
     */
    public static final int MAX_RADIUS = 50;
}
