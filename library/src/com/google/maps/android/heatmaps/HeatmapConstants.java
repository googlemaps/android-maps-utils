package com.google.maps.android.heatmaps;

import android.graphics.Color;

/**
 * Constants for heatmaps
 */
public class HeatmapConstants {

    /**
     * Default size of a color map for the heatmap
     */
    public final static int HEATMAP_COLOR_MAP_SIZE = 1001;

    /**
     * Default tile dimensions
     */
    public final static int HEATMAP_TILE_SIZE = 256;

    /**
     * Default radius for convolution
     */
    public static final int DEFAULT_HEATMAP_RADIUS = 10;

    /**
     * Default opacity of heatmap overlay
     */
    public static final double DEFAULT_HEATMAP_OPACITY = 0.7;

    /**
     * Default gradient for heatmap.
     * Copied from Javascript version.
     * Array of colors, in int form.
     */
    public final static int[] DEFAULT_HEATMAP_GRADIENT = {
            //a, r, g, b / r, g, b
            Color.argb(0, 102, 255, 0),  // green (invisible)
            Color.argb(255/3 ,102, 255, 0),  // 1/3rd invisible
            Color.argb(255/3*2, 147, 255, 0), // 2/3rd invisible
            Color.rgb(193, 255, 0),
            Color.rgb(238, 255, 0),  // yellow
            Color.rgb(244, 227, 0),
            Color.rgb(249, 198, 0),
            Color.rgb(255, 170, 0),  // orange
            Color.rgb(255, 113, 0),
            Color.rgb(255, 57, 0),
            Color.rgb(255, 0, 0)     // red
            };

}
