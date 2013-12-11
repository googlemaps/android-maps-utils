package com.google.maps.android.heatmaps;

/**
 * Created by minicat on 12/11/13.
 */
public class HeatmapConstants {

    /**
     * Default size of a color map for the heatmap
     */
    public final static int HEATMAP_COLOR_MAP_SIZE = 1001;


    /**
     * Default gradient for heatmap.
     * Copied from Javascript version.
     */
    public final static int[][] DEFAULT_HEATMAP_GRADIENT = {
            //r, g, b, a
            {102, 255, 0, 0},  // green (invisible)
            {102, 255, 0, 1},  // green
            {147, 255, 0, 1},
            {193, 255, 0, 1},
            {238, 255, 0, 1},  // yellow
            {244, 227, 0, 1},
            {249, 198, 0, 1},
            {255, 170, 0, 1},  // orange
            {255, 113, 0, 1},
            {255, 57, 0, 1},
            {255, 0, 0, 1}     // red
            };

}
