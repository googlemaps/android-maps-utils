package com.google.maps.android.heatmaps;

import android.graphics.Color;

/**
 * Constants for heatmaps
 */
public class HeatmapConstants {

    /**
     * Assumed map size
     */
    public static final int SCREEN_SIZE = 1280;

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
     * Alternative radius for convolution
     */
    public static final int ALT_HEATMAP_RADIUS = 5;

    /**
     * Default opacity of heatmap overlay
     */
    public static final double DEFAULT_HEATMAP_OPACITY = 0.7;

    /**
     * Alternative opacity of heatmap overlay
     */
    public static final double ALT_HEATMAP_OPACITY = 0.2;

    /**
     * Default gradient for heatmap.
     * Copied from Javascript version.
     * Array of colors, in int form.
     */
    public final static int[] DEFAULT_HEATMAP_GRADIENT = {
            //a, r, g, b / r, g, b
            Color.argb(0, 102, 255, 0),  // green (invisible)
            Color.argb(255/3*2 ,102, 255, 0),  // 2/3rds invisible
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
     * Alternative heatmap gradient (blue -> red)
     * Copied from Javascript version
     */
    public final static int[] ALT_HEATMAP_GRADIENT = {
            Color.argb(0, 0, 255, 255),// transparent
            Color.argb(255/3*2, 0, 255, 255),
            Color.rgb(0, 191, 255),
            Color.rgb(0, 127, 255),
            Color.rgb(0, 63, 255),
            Color.rgb(0, 0, 255),
            Color.rgb(0, 0, 223),
            Color.rgb(0, 0, 191),
            Color.rgb(0, 0, 159),
            Color.rgb(0, 0, 127),
            Color.rgb(63, 0, 91),
            Color.rgb(127, 0, 63),
            Color.rgb(191, 0, 31),
            Color.rgb(255, 0, 0)
            };
}
